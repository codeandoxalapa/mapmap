package org.codeandoxalapa.mapmap;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import org.codeandoxalapa.mapmap.R;
import org.codeandoxalapa.mapmap.TransitWandProtos.Upload;
import org.codeandoxalapa.mapmap.RouteImage;

import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class EditActivity extends Activity {
	
	public static int itemPosition = 0;
	public static Integer id;
	public static String name;
	public static String description;
	public static String notes;
	public static String vehicleCapacity;
	public static String vehicleType;
	public ImageView img;
	RouteImage routeImage = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new);
		
		FilenameFilter fileNameFilter = RouteCapture.getFileNameFilterRoute();
		
		final File f = getFilesDir().listFiles(fileNameFilter)[itemPosition];
	    DataInputStream dataInputStream = null;
	    RouteCapture rc = null;
	    
	   routeImage = new RouteImage();
		
		View.OnClickListener listener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				switch (v.getId()) {
					
				case R.id.ContinueButton:
					
					Log.v("Edit", "continue");
					updateCaptureFile(f);
					
					Intent intent = new Intent(ReviewActivity.DELETE_ITEM_ACTION);
		      	  	LocalBroadcastManager.getInstance(EditActivity.this).sendBroadcast(intent);
			    	EditActivity.this.finish();
					
			    	break;
			    	
				case R.id.btnCaptura:
					
					Log.v("Edit", "Image");
					final CharSequence[] option = {"Tomar Foto", "Escoger desde galería", "Cancelar"};
					final AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
					builder.setTitle("Selecciona una");
					builder.setItems(option, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) 
						{
							Object[] arr = null;
							if(option[which]=="Tomar Foto")
							{
								arr = routeImage.openCamera();
								if((Boolean) arr[0])
								{
									startActivityForResult( (Intent) arr[1], (Integer) arr[2] );
								}
								
							}
							else if(option[which]=="Escoger desde galería")
							{
								arr = routeImage.openGallery();
								startActivityForResult( (Intent) arr[1], (Integer) arr[2] );
							}
							else
							{
								dialog.dismiss();
							}
						}
					}); 
					builder.show();
					
					break;
					
				default:
					break;
				}
			}
		};
		 
		
		ImageButton wandButton = (ImageButton) findViewById(R.id.ContinueButton);
		wandButton.setOnClickListener(listener);
		
		ImageButton imageBtn = (ImageButton) findViewById(R.id.btnCaptura);
		imageBtn.setOnClickListener(listener);
		
		img = (ImageView)findViewById(R.id.imagenBus);
		
	    try{
	    	
			dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
			Upload.Route pbRouteData = Upload.Route.parseDelimitedFrom(dataInputStream);
			rc = RouteCapture.deseralize(pbRouteData);
			
			synchronized(this)	{
				   
			   name = rc.name;
			   description = rc.description;
			   notes = rc.notes;
			   vehicleCapacity = rc.vehicleCapacity;
			   vehicleType = rc.vehicleType;
			   routeImage.pathImage = rc.path;
			
			   EditText routeName = (EditText) findViewById(R.id.routeName);
			   EditText routeDescription = (EditText) findViewById(R.id.routeDescription);
			   EditText fieldNotes = (EditText) findViewById(R.id.fieldNotes);
			   EditText routeVehicleCapacity = (EditText) findViewById(R.id.vehicleCapacity);
			   EditText routeVehicleType = (EditText) findViewById(R.id.vehicleType);
			   ImageView imagenRoute = (ImageView)findViewById(R.id.imagenBus);
			
			   routeName.setText(name);
			   routeDescription.setText(description);
			   fieldNotes.setText(notes);
			   routeVehicleCapacity.setText(vehicleCapacity);
			   routeVehicleType.setText(vehicleType);
			   
			   if( !routeImage.pathImage.equalsIgnoreCase("not image load"))
			   {
				   //show imagen
				   byte[] decodedString = Base64.decode(routeImage.pathImage, Base64.DEFAULT);
				   //Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
				   Bitmap decodedByte = RouteImage.decodeSampledBitmapFromByteArray(decodedString, 100, 100);
				   imagenRoute.setImageBitmap(decodedByte);
			   }
			}
		
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == Activity.RESULT_OK)
		{
			routeImage = routeImage.typeActionPhoto(requestCode, getBaseContext(), data);
			
			img.setImageBitmap(routeImage.bitmap);
			
			/*if(routeImage.path == null)
			{
				img.setImageBitmap(routeImage.bitmap);
				//img.setImageResource(R.drawable.check);
			}
			else
			{
				img.setImageBitmap(routeImage.bitmap);
				//img.setImageURI(routeImage.path);
				//img.setImageResource(R.drawable.check);
			}*/
			
		}
	}
	
	private void updateCaptureFile(File f) {
		
    	FileOutputStream os;
    	DataInputStream dataInputStreamEdit = null;
    	RouteCapture rcEdit = null;
    	
    	try {
    		
    		dataInputStreamEdit = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
			Upload.Route pbRouteDataEdited = Upload.Route.parseDelimitedFrom(dataInputStreamEdit);
			rcEdit = RouteCapture.deseralize(pbRouteDataEdited);
			
			if(rcEdit.points.size() > 0) {
				
				synchronized(this)	{
					
					EditText routeName = (EditText) findViewById(R.id.routeName);
					EditText routeDescription = (EditText) findViewById(R.id.routeDescription);
				    EditText fieldNotes = (EditText) findViewById(R.id.fieldNotes);
				    EditText routeVehicleCapacity = (EditText) findViewById(R.id.vehicleCapacity);
				    EditText routeVehicleType = (EditText) findViewById(R.id.vehicleType);
					
					rcEdit.name = routeName.getText().toString();
					rcEdit.description = routeDescription.getText().toString();
					rcEdit.notes = fieldNotes.getText().toString();
					rcEdit.vehicleCapacity = routeVehicleCapacity.getText().toString();
					rcEdit.vehicleType = routeVehicleType.getText().toString();
					rcEdit.path = routeImage.pathImage;
				}
				
				Upload.Route routePb = rcEdit.seralize();
				os = new FileOutputStream(f, false); // overwrites file
				routePb.writeDelimitedTo(os);
				os.close();
			}
	    	
    	} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
	    	// TODO Auto-generated catch block
			e.printStackTrace();
	    } finally {
			if(dataInputStreamEdit != null) {
				try {
					dataInputStreamEdit.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	    }
	}
}
