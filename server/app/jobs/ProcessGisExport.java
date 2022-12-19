package jobs;

import models.transit.TripPattern;
import models.transit.TripPatternStop;
import org.apache.commons.io.FileUtils;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import play.Logger;
import play.Play;
import play.jobs.Job;
import utils.DirectoryZip;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessGisExport extends Job {

    private final List<Long> patternIds;
    private final String timestamp;

    public ProcessGisExport(List<Long> patternIds, String timestamp) {
        this.patternIds = patternIds;
        this.timestamp = timestamp;
    }

    public void doJob() throws InterruptedException {

        File outputDirectory = new File(Play.configuration.getProperty("application.exportDataDirectory"), timestamp);
        File outputZipFile = new File(Play.configuration.getProperty("application.exportDataDirectory"), timestamp + "_SHP.zip");

        try {
            if (!outputDirectory.exists()) {
                outputDirectory.mkdir();
            }

            if (outputZipFile.exists()) {
                outputZipFile.delete();
            }

            processStops(outputDirectory, timestamp);
            processRoute(outputDirectory, timestamp);

            DirectoryZip.zip(outputDirectory, outputZipFile);
            FileUtils.deleteDirectory(outputDirectory);
        } catch (Exception e) {
            Logger.error("Unable to process GIS export: ", e.toString());
            e.printStackTrace();
        }

    }

    private void processStops(File outputDirectory, String exportName) throws Exception {

        File outputShapefile = new File(outputDirectory, exportName + "_stops.shp");

        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("url", outputShapefile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore dataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        dataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84);

        SimpleFeatureType STOP_TYPE = DataUtilities.createType(
                "Stop",
                "location:Point:srid=4326," +
                        "STOP_ID:String," +
                        "ROUTE_ID:String," +
                        "SEQUENCE:Integer," +
                        "TRAVEL_S:Integer," +
                        "DWELL_S:Integer," +
                        "ARRIVAL_S:Integer," +
                        "ARRIVAL_T:String," +
                        "DEPARTUR_S:Integer," +
                        "DEPARTUR_T:String," +
                        "BOARD:Integer," +
                        "ALIGHT:Integer," +
                        "PASSENGERS:Integer"
        );

        SimpleFeatureCollection collection = FeatureCollections.newCollection();
        SimpleFeatureBuilder featureBuilder = null;
        dataStore.createSchema(STOP_TYPE);
        featureBuilder = new SimpleFeatureBuilder(STOP_TYPE);

        for (Long patternId : this.patternIds) {
            TripPattern tripPattern = TripPattern.findById(patternId);
            List<TripPatternStop> patternStops = TripPatternStop.find("pattern = ? order by stopSequence", tripPattern).fetch();

            long cumulativeTime = 0;
            long passengers = 0;
            Date routeStartTime = tripPattern.route.captureTime;

            for (TripPatternStop tripPatternStop : patternStops) {
                featureBuilder.add(tripPatternStop.stop.location);
                featureBuilder.add(tripPatternStop.stop.id.toString());
                featureBuilder.add(tripPattern.route.id.toString());
                featureBuilder.add(tripPatternStop.stopSequence);
                featureBuilder.add(tripPatternStop.defaultTravelTime);
                featureBuilder.add(tripPatternStop.defaultDwellTime);

                if (tripPatternStop.defaultTravelTime != null) {
                    cumulativeTime += tripPatternStop.defaultTravelTime;
                }
                featureBuilder.add(cumulativeTime);
                Date arrivalTimestamp = new Date(routeStartTime.getTime() + (cumulativeTime * 1000));
                featureBuilder.add(arrivalTimestamp.toGMTString());

                if (tripPatternStop.defaultDwellTime != null){
                    cumulativeTime += tripPatternStop.defaultDwellTime;
                }
                featureBuilder.add(cumulativeTime);
                Date departureTimestamp = new Date(routeStartTime.getTime() + (cumulativeTime * 1000));
                featureBuilder.add(departureTimestamp.toGMTString());

                featureBuilder.add(tripPatternStop.board);
                featureBuilder.add(tripPatternStop.alight);

                passengers += tripPatternStop.board;
                passengers -= tripPatternStop.alight;
                passengers = passengers < 0 ? 0 : passengers;
                featureBuilder.add(passengers);

                SimpleFeature feature = featureBuilder.buildFeature(null);
                collection.add(feature);
            }
        }

        Transaction transaction = new DefaultTransaction("create");

        String typeName = dataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);

        if (featureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
            featureStore.setTransaction(transaction);
            featureStore.addFeatures(collection);
            transaction.commit();
            transaction.close();
        } else {
            throw new Exception(typeName + " does not support read/write access");
        }
    }

    private void processRoute(File outputDirectory, String exportName) throws Exception {

        File outputShapefile = new File(outputDirectory, exportName + "_route.shp");

        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("url", outputShapefile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore dataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        dataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84);

        SimpleFeatureType ROUTE_TYPE = DataUtilities.createType(
                "Route",                   // <- the name for our feature type
                "route:LineString:srid=4326," +
                        "ROUTE_ID:String," +
                        "PHONE_ID:String," +
                        "PHONE_OWNER:String," +
                        "NAME:String," +
                        "DESC:String," +
                        "NOTES:String," +
                        "START:String"
        );

        SimpleFeatureCollection collection = FeatureCollections.newCollection();
        SimpleFeatureBuilder featureBuilder = null;
        dataStore.createSchema(ROUTE_TYPE);
        featureBuilder = new SimpleFeatureBuilder(ROUTE_TYPE);

        for (Long patternId : this.patternIds) {
            TripPattern tp = TripPattern.findById(patternId);
            if (tp.shape == null) {
                return;
            }
            featureBuilder.add(tp.shape.shape);
            featureBuilder.add(tp.route.id.toString());
            featureBuilder.add(tp.route.phone.unitId);
            featureBuilder.add(tp.route.phone.userName != null ? tp.route.phone.userName.toUpperCase() : "");
            featureBuilder.add(tp.route.routeLongName != null ? tp.route.routeLongName.toUpperCase() : "");
            featureBuilder.add(tp.route.routeDesc != null ? tp.route.routeDesc.toUpperCase() : "");
            featureBuilder.add(tp.route.routeNotes != null ? tp.route.routeNotes.toUpperCase() : "");
            featureBuilder.add(tp.route.captureTime != null ? tp.route.captureTime.toGMTString() : "");
            SimpleFeature feature = featureBuilder.buildFeature(null);
            collection.add(feature);
        }

        Transaction transaction = new DefaultTransaction("create");
        String typeName = dataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);

        if (featureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
            featureStore.setTransaction(transaction);
            featureStore.addFeatures(collection);
            transaction.commit();
            transaction.close();
        } else {
            throw new Exception(typeName + " does not support read/write access");
        }
    }
}

