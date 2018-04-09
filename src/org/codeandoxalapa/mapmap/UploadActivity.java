package org.codeandoxalapa.mapmap;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
//import org.apache.http.Header;
import org.codeandoxalapa.mapmap.R;
import org.codeandoxalapa.mapmap.TransitWandProtos.Upload;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import com.loopj.android.http.RequestParams;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
//import cz.msebera.android.httpclient.Header;

public class UploadActivity extends Activity {

	private ByteArrayInputStream dataStream = null;
	private int count = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);
	
		if(RouteCapture.checkNumberListFiles(UploadActivity.this) == 0) {
			Toast.makeText(UploadActivity.this, "No hay rutas para enviar.", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		final FilenameFilter fileNameFilter = RouteCapture.getFileNameFilterRoute();
		
		TextView textView3 = (TextView) findViewById(R.id.textView3);
		Typeface font3 = Typeface.createFromAsset(getAssets(), "fonts/bebasneue_bold.ttf");
		textView3.setTypeface(font3);
		
		TextView textView2 = (TextView) findViewById(R.id.textView2);
		Typeface font4 = Typeface.createFromAsset(getAssets(), "fonts/bebasneue_bold.ttf");
		textView2.setTypeface(font4);
		
		
		TextView routeCountText = (TextView) findViewById(R.id.routeCountText);		
		routeCountText.setText(getFilesDir().listFiles(fileNameFilter).length + "");
		
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/bebasneue_bold.ttf");
        routeCountText.setTypeface(font);
		
		
		
		Long dataSize = 0l;
		
		Upload.Builder uploadBuilder = Upload.newBuilder();
		uploadBuilder.setUnitId(0l);
		uploadBuilder.setUploadId(0);
		
		
		for(File f : getFilesDir().listFiles(fileNameFilter)) {
			
			dataSize += f.length();
			
			DataInputStream dataInputStream = null;
			
			try {
				dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
				
				Upload.Route pbRouteData = Upload.Route.parseDelimitedFrom(dataInputStream);
				
				dataInputStream.close();
				
				uploadBuilder.addRoute(pbRouteData);
			
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if(dataInputStream != null) {
					try {
						dataInputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		dataStream = new ByteArrayInputStream(uploadBuilder.build().toByteArray());
		
		
		DecimalFormat distanceFormat = new DecimalFormat( "#,##0.00" );
		
		String dataSizeFormated = "";
		
		if(dataSize / 1024 / 1024 > 1) {
			dataSizeFormated = distanceFormat.format(dataSize / 1024 / 1024) + "Mb";
		}
		else {
			dataSizeFormated = distanceFormat.format(dataSize / 1024) + "Kb";
		}
		
		TextView dataSizeText = (TextView) findViewById(R.id.dataSizeText);	
		Typeface font2 = Typeface.createFromAsset(getAssets(), "fonts/bebasneue_bold.ttf");
		dataSizeText.setTypeface(font2);
		dataSizeText.setText(dataSizeFormated);
		
		
		
		
		View.OnClickListener listener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				switch (v.getId()) {
				
					case R.id.uploadButton:
						
						if(CaptureService.imei == null)
				        {
				        	TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
				    		CaptureService.imei = telephonyManager.getDeviceId();
				        }
						
						ImageButton uploadButton = (ImageButton) findViewById(R.id.uploadButton);
						uploadButton.setVisibility(View.GONE);
						
						ProgressBar progressSpinner = (ProgressBar) findViewById(R.id.progressBar);
						progressSpinner.setVisibility(View.VISIBLE);
						
						RequestParams params = new RequestParams();
				    	
				    	params.put("imei", CaptureService.imei);
				    	params.put("data", dataStream);
				    
						try {
							dataStream.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						AsyncHttpClient client = new AsyncHttpClient();
						client.setTimeout(240 * 1000);
						client.setUserAgent("tw");
						client.post(CaptureService.URL_BASE + "upload", params,  new AsyncHttpResponseHandler() {
							
							@Override
							public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
								// TODO Auto-generated method stub
						    	ProgressBar progressSpinner = (ProgressBar) findViewById(R.id.progressBar);
								progressSpinner.setVisibility(View.GONE);
						    	
						    	ImageButton uploadButton = (ImageButton) findViewById(R.id.uploadButton);
								uploadButton.setVisibility(View.VISIBLE);
								
						    	Toast.makeText(UploadActivity.this, "Deshabilitado para subir información a Mapaton, revisa tu conexión a internet.", Toast.LENGTH_SHORT).show();
							}

							@Override
							public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
								// TODO Auto-generated method stub
								try {

						    		Toast.makeText(UploadActivity.this, "Data Mapatón cargada.", Toast.LENGTH_SHORT).show();
						    	}
						    	catch(Exception e) {		
						    		
						    		ProgressBar progressSpinner = (ProgressBar) findViewById(R.id.progressBar);
									progressSpinner.setVisibility(View.GONE);
							    	
							    	ImageButton uploadButton = (ImageButton) findViewById(R.id.uploadButton);
									uploadButton.setVisibility(View.VISIBLE);
									
						    		Toast.makeText(UploadActivity.this, "Deshabilitado para subir información a Mapaton, revisa tu conexión a internet.", Toast.LENGTH_SHORT).show();
						    	}
							}
						});	
						
						sendImagetoMapaton();
						
						break;
				
					 default:
						 break;
				}
			}
		   };
		 
		   ImageButton uploadButton = (ImageButton) findViewById(R.id.uploadButton);
		   uploadButton.setOnClickListener(listener);
	}
		
	private void sendImagetoMapaton() {
		
		List<RouteStop> Liststops = new ArrayList<RouteStop>();
		String paths = "";
		String name  = "";
		String description  = "";
		String imei = "";
		
		final FilenameFilter fileNameFilter = RouteCapture.getFileNameFilterRoute();
		
		final int numberFiles = getFilesDir().listFiles(fileNameFilter).length;
				
		for(File f : getFilesDir().listFiles(fileNameFilter)) {
			
			final File fileToDelete = f;
			DataInputStream dataInputStream = null;
			
			try {
				
				dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
				
				Upload.Route pbRouteData = Upload.Route.parseDelimitedFrom(dataInputStream);
				
				RouteCapture rc = RouteCapture.deseralize(pbRouteData);
				paths = rc.path;
				name  = rc.name;
				description  = rc.description;
				Liststops = rc.stops;
				//imei = rc.imei;
				imei = CaptureService.imei;
				
				RequestParams params = new RequestParams();
				
				Integer index = 0;
				
				List<String> signalStop = new ArrayList<String>(Liststops.size());
				for (RouteStop st : Liststops){
					signalStop.add(String.valueOf(st.signalStop));
					signalStop.add(String.valueOf(st.location.getLatitude()));
					signalStop.add(String.valueOf(st.location.getLongitude()));
					params.put("stopsignal_"+index, String.valueOf(signalStop));
					index++;
					signalStop.clear();
				}
				
		    	params.put("imagen", paths);
		    	params.put("name", name);
		    	params.put("description", description);
		    	params.put("stopsAmount",index.toString());
		    	params.put("imei", imei);
		    	 params.setUseJsonStreamer(true);
		    	
		    	Header[] headers = new Header[1];
		        headers[0] = (Header) new BasicHeader("Content-Type", "application/json");
		    	
				AsyncHttpClient client = new AsyncHttpClient();
				client.post(this, "https://mapaton.org/manage_img/post-img-route-stop-json.php", headers, params, 
		        		RequestParams.APPLICATION_JSON, new AsyncHttpResponseHandler(){
					
					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						// TODO Auto-generated method stub
						Toast.makeText(UploadActivity.this, "No fue posible subir la imagen al servidor.", Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
						// TODO Auto-generated method stub
						try {
				    		
				    		Toast.makeText(UploadActivity.this, "Data Mapatón cargada.", Toast.LENGTH_SHORT).show();
				    		fileToDelete.delete();
				    		count++;
				    		if(count == numberFiles){
				    			UploadActivity.this.finish();
				    		}
				    	}
				    	catch(Exception e) {
				    		Toast.makeText(UploadActivity.this, "Deshabilitado para subir información de Mapatón, revisa tu conexión a internet.", Toast.LENGTH_SHORT).show();
				    	}
					}
				});
			
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if(dataInputStream != null) {
					try {
						dataInputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
}