package controllers;

import au.com.bytecode.opencsv.CSVWriter;
import com.conveyal.transitwand.TransitWandProtos.Upload;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jobs.ProcessGisExport;
import models.Phone;
import models.transit.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.mvc.Http;
import utils.DirectoryZip;
import utils.PBUtils;
import utils.TripPatternSerializer;
import utils.TripPatternShapeSerializer;

import java.io.*;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Application extends Controller {


    public static void index(Boolean invalid) {
        render(invalid);
    }

    public static void export(String unitId) throws InterruptedException {

        Phone p = Phone.find("unitId = ?", unitId).first();

        List<TripPattern> tripPatterns = TripPattern.find("route.phone = ?", p).fetch();

        ArrayList<Long> patternIds = new ArrayList<Long>();

        for (TripPattern pattern : tripPatterns) {
            patternIds.add(pattern.id);
        }

        ok();
    }

    public static void exportAll() throws InterruptedException {

        List<TripPattern> tripPatterns = TripPattern.findAll();
    	/*for(TripPattern tp: tripPatterns) {
    		ProcessGisExport gisExport = new ProcessGisExport(tp.id);
    		gisExport.doJob();
    	}*/

        ok();
    }


    public static void register(String imei, String userName) {
        if(imei == null) {
            Logger.error("El IMEI es null");
            badRequest();
        }
        String _userName = userName != null ? userName.toUpperCase() : "Anónimo";
        Phone p = Phone.registerImei(imei, _userName);
        renderJSON(p);
    }

    public static void upload(String imei, File data) {

        try {
            Logger.info("\n");
            Logger.info("----------------------------------------");
            Logger.info("UPLOAD >>>");
            Logger.info("----------------------------------------");
            if(imei == null) {
                Logger.error("El Phone ID es null.");
                badRequest();
            }

            Phone phone = Phone.find("imei = ?", imei).first();
            if (phone == null) {
                Logger.error("No existe el teléfono con el IMEI " + imei);
                phone = Phone.find("unitid = ?", imei).first();
                if(phone == null) {
                    Logger.error("No existe el teléfono con el UnitID " + imei);
                    badRequest();
                } else {
                    Logger.info("Unit ID: " + imei);
                }
            } else {
                Logger.info("IMEI: " + imei);
            }
            Logger.info("Unidad: " + phone.unitId + " - " + phone.userName);

            File pbFile = new File(Play.configuration.getProperty("application.uploadDataDirectory"), imei + "_" + new Date().getTime() + ".pb");
            Logger.info("PB File: " + pbFile.toString());
            data.renameTo(pbFile);

            byte[] dataFrame = new byte[(int) pbFile.length()];
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(pbFile)));
            dataInputStream.read(dataFrame);
            Upload upload = Upload.parseFrom(dataFrame);

            for (Upload.Route r : upload.getRouteList()) {
                Logger.debug("Procesando ruta: " + r.getRouteName());
                Logger.debug("\tDescripción: " + r.getRouteDescription());
                Logger.debug("\tNotas: " + r.getRouteNotes());
                Logger.debug("\tNo. puntos: " + r.getPointCount());
                Logger.debug("\tNo. paradas: " + r.getStopCount());
                if (r.getPointList().size() <= 1) {
                    Logger.info("La ruta tiene 1 o menos puntos mapeados. No se guarda.");
                    continue;
                }

                Agency agency = Agency.find("gtfsAgencyId = ?", "DEFAULT").first();
                Route route = new Route("", r.getRouteName().toUpperCase(), RouteType.BUS, r.getRouteDescription().toUpperCase(), agency);
                route.phone = phone;
                route.routeNotes = r.getRouteNotes().toUpperCase();
                route.vehicleCapacity = r.getVehicleCapacity();
                route.vehicleType = r.getVehicleType();
                route.captureTime = new Date(r.getStartTime());
                route.save();

                List<String> points = new ArrayList<String>();

                Integer pointSequence = 1;
                for (Upload.Route.Point p : r.getPointList()) {
                    points.add(Double.toString(p.getLon()) + " " + Double.toString(p.getLat()));
                    RoutePoint.addRoutePoint(p, route.id, pointSequence);
                    pointSequence++;
                }

                String linestring = "LINESTRING(" + StringUtils.join(points, ", ") + ")";

                BigInteger tripShapeId = TripShape.nativeInsert(TripShape.em(), "", linestring, 0.0);

                TripPattern tripPattern = new TripPattern();
                tripPattern.route = route;
                tripPattern.headsign = r.getRouteName().toUpperCase();
                tripPattern.shape = TripShape.findById(tripShapeId.longValue());
                tripPattern.save();

                int sequenceId = 0;

                for (Upload.Route.Stop s : r.getStopList()) {
                    BigInteger stopId = Stop.nativeInsert(Stop.em(), s);
                    TripPatternStop tps = new TripPatternStop();
                    tps.stop = Stop.findById(stopId.longValue());
                    tps.stopSequence = sequenceId;
                    tps.defaultTravelTime = s.getArrivalTimeoffset();
                    tps.defaultDwellTime = s.getDepartureTimeoffset() - s.getArrivalTimeoffset();
                    tps.pattern = tripPattern;
                    tps.board = s.getBoard();
                    tps.alight = s.getAlight();
                    tps.save();
                    sequenceId++;
                }
            }
            Logger.info("Rutas procesadas: " + upload.getRouteList().size());
            dataInputStream.close();
            ok();
        } catch (Exception e) {
            Logger.error("Error uploading routes: " + e.getMessage());
            Logger.error("Stack Trace:\n" + Arrays.toString(e.getStackTrace()));
            badRequest();
        }

    }

    public static void view(String unitId) {
        if (unitId == null) {
            index(true);
        }
        Phone p = Phone.find("unitId = ?", unitId).first();
        if (p == null) {
            index(true);
        }
        List<TripPattern> patterns = TripPattern.find("route.phone = ? ORDER BY id DESC", p).fetch();
        render(p, patterns);
    }

    public static void list(String unitId) {
        Http.Header hd = new Http.Header();
        hd.name = "Access-Control-Allow-Origin";
        hd.values = new ArrayList<String>();
        hd.values.add("*");
        Http.Response.current().headers.put("Access-Control-Allow-Origin", hd);

        if (unitId == null) {
            badRequest();
        }
        Phone p = Phone.find("unitId = ?", unitId).first();
        if (p == null) {
            badRequest();
        }
        List<TripPattern> patterns = TripPattern.find("route.phone = ? ORDER BY id DESC", p).fetch();
        for(TripPattern tp : patterns) {
            Logger.debug(tp.headsign);
        }
        Gson gson = new GsonBuilder().registerTypeAdapter(TripPattern.class, new TripPatternSerializer()).serializeSpecialFloatingPointValues().serializeNulls().create();
        renderJSON(gson.toJson(patterns));
    }

    public static void pattern(Long patternId) {
        if (patternId == null) {
            badRequest();
        }
        Http.Header hd = new Http.Header();
        hd.name = "Access-Control-Allow-Origin";
        hd.values = new ArrayList<String>();
        hd.values.add("*");
        Http.Response.current().headers.put("Access-Control-Allow-Origin", hd);
        TripPattern pattern = TripPattern.findById(patternId);
        Gson gson = new GsonBuilder().registerTypeAdapter(TripPattern.class, new TripPatternShapeSerializer()).serializeSpecialFloatingPointValues().serializeNulls().create();
        renderJSON(gson.toJson(pattern));
    }

    public static void exportGis(String unitId) throws InterruptedException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_hhmmss");
        String timestamp = sdf.format(new Date());
        ArrayList<Long> patterns = new ArrayList<Long>();
        String[] unitIds = unitId.split(",");

        for (String id : unitIds) {
            Phone p = Phone.find("unitId = ?", id).first();
            if (p != null) {
                List<Route> unitRouteList = Route.find("phone = ?", p).fetch();
                for (Route r : unitRouteList) {
                    List<TripPattern> tps = TripPattern.find("route = ?", r).fetch();
                    for (TripPattern tp : tps) {
                        patterns.add(tp.id);
                    }
                }
            }
        }

        ProcessGisExport pge = new ProcessGisExport(patterns, timestamp);
        pge.doJob();
        redirect("/mapmap/public/data/exports/" + timestamp + "_SHP.zip");
    }

    public static void exportGTFS(String unitId) throws InterruptedException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_hhmmss");
        String timestamp = sdf.format(new Date());
        ArrayList<Long> patterns = new ArrayList<Long>();
        String[] unitIds = unitId.split(",");
        Long ident = 0L;

        for (String id : unitIds) {
            Phone p = Phone.find("unitId = ?", id).first();
            if (p != null) {
                List<Route> unitRouteList = Route.find("phone = ?", p).fetch();
                for (Route r : unitRouteList) {
                    List<TripPattern> tps = TripPattern.find("route = ?", r).fetch();
                    for (TripPattern tp : tps) {
                        patterns.add(tp.id);
                        ident = tp.id;
                    }
                }
            }
        }

        //ProcessGtfsSnapshotExport pge = new ProcessGtfsSnapshotExport(patterns);
        // ProcessGtfsSnapshotExport pge = new ProcessGtfsSnapshotExport(ident, patterns, timestamp);
        //pge.doJob();
        redirect("/mapmap/public/data/exports/" + timestamp + "_GTFS.zip");
    }

    public static void exportCsv(String unitId) throws IOException {

        ArrayList<Route> routes = new ArrayList<Route>();

        String[] unitIds = unitId.split(",");

        for (String id : unitIds) {
            Phone p = Phone.find("unitId = ?", id).first();
            if (p != null) {
                List<Route> unitRouteList = Route.find("phone = ?", p).fetch();
                routes.addAll(unitRouteList);
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_hhmmss");
        String timestamp = sdf.format(new Date());

        File outputDirectory = new File(Play.configuration.getProperty("application.exportDataDirectory"), timestamp);
        File outputZipFile = new File(Play.configuration.getProperty("application.exportDataDirectory"), timestamp + "_CSV.zip");

        // write routes
        File routesFile = new File(outputDirectory, timestamp + "_routes.csv");
        File stopsFile = new File(outputDirectory, timestamp + "_stops.csv");

        if (!outputDirectory.exists()) {
            outputDirectory.mkdir();
        }

        if (outputZipFile.exists()) {
            outputZipFile.delete();
        }

        FileWriter routesCsv = new FileWriter(routesFile);
        CSVWriter rotuesCsvWriter = new CSVWriter(routesCsv);

        FileWriter stopsCsv = new FileWriter(stopsFile);
        CSVWriter stopsCsvWriter = new CSVWriter(stopsCsv);

        String[] routesHeader = "unit_id, route_id, route_name, route_description, field_notes, vehicle_type, vehicle_capacity, start_capture, end_capture, duration".split(",");
        String[] stopsHeader = "route_id, stop_sequence, lat, lon, travel_time, dwell_time, board, allright".split(",");

        rotuesCsvWriter.writeNext(routesHeader);
        stopsCsvWriter.writeNext(stopsHeader);

        for (Route r : routes) {

            List<RoutePoint> routePoints = RoutePoint.find("route = ? order by sequence", r).fetch();
            Integer duration = 0;
            for (RoutePoint routePoint : routePoints) {
                duration += routePoint.timeOffset;
            }
            Date endTime = r.captureTime != null ? new Date(r.captureTime.getTime() + (duration * 1000)) : null;
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

            String[] routeData = new String[routesHeader.length];
            routeData[0] = r.phone.unitId;
            routeData[1] = r.id.toString();
            routeData[2] = r.routeLongName;
            routeData[3] = r.routeDesc;
            routeData[4] = r.routeNotes;
            routeData[5] = r.vehicleType;
            routeData[6] = r.vehicleCapacity;
            routeData[7] = (r.captureTime != null) ? sdf2.format(r.captureTime) : "";
            routeData[8] = (endTime != null) ? sdf2.format(endTime) : "";
            routeData[9] = duration.toString();

            rotuesCsvWriter.writeNext(routeData);

            List<TripPatternStop> stops = TripPatternStop.find("pattern.route = ?", r).fetch();

            System.out.println("route: " + r.id.toString());

            for (TripPatternStop s : stops) {

                String[] stopData = new String[stopsHeader.length];

                stopData[0] = r.id.toString();
                stopData[1] = s.stopSequence.toString();
                stopData[2] = "" + s.stop.location.getCoordinate().y;
                stopData[3] = "" + s.stop.location.getCoordinate().x;
                stopData[4] = "" + s.defaultTravelTime;
                stopData[5] = "" + s.defaultDwellTime;
                stopData[6] = "" + s.board;
                stopData[7] = "" + s.alight;

                stopsCsvWriter.writeNext(stopData);

            }
        }

        rotuesCsvWriter.flush();
        rotuesCsvWriter.close();

        stopsCsvWriter.flush();
        stopsCsvWriter.close();

        DirectoryZip.zip(outputDirectory, outputZipFile);
        FileUtils.deleteDirectory(outputDirectory);

        redirect("/mapmap/public/data/exports/" + timestamp + "_CSV.zip");
    }

    public static void pbx(String unitId) {
        if(unitId == null) {
            badRequest();
        }
        Phone p = Phone.find("unitId = ?", unitId).first();
        String imei = p.imei;

        File uploadsDir = new File("public/data/uploads");
        File[] files = uploadsDir.listFiles();
        List<String> userFiles = new ArrayList<String>();
        Map<String, Upload> uploads = new TreeMap<String, Upload>();
        for (File file : files) {
            if (file.getName().contains(imei)) {
                Upload upload = PBUtils.parsePB(Play.configuration.getProperty("application.uploadDataDirectory") + file.getName());
                if(upload != null) {
                    uploads.put(file.getName(), upload);
                }
                userFiles.add(file.getName());
            }
        }
        render(p, uploads, userFiles);
    }

    private static String pbFileNameToFormatedDate(String pbFileName) {
        int underscoreIndex = pbFileName.indexOf('_');
        int dotIndex = pbFileName.indexOf('.');
        if (underscoreIndex != -1 && dotIndex != -1) {
            String secondPart = pbFileName.substring(underscoreIndex + 1, dotIndex);
            long miliseconds = Long.parseLong(secondPart);
            Date fecha = new Date(miliseconds);
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return formatter.format(fecha);
        }
        return "";
    }
}
