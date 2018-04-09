package org.codeandoxalapa.mapmap;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import org.codeandoxalapa.mapmap.R;
import android.view.View.OnClickListener;

public class NewActivity extends Activity implements OnClickListener{
	
	private static Intent serviceIntent;
    private CaptureService captureService;
    public ImageView img;
	RouteImage routeImage = null;
	
	private final ServiceConnection caputreServiceConnection = new ServiceConnection()
    {

        public void onServiceDisconnected(ComponentName name)
        {
        	captureService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service)
        {
        	captureService = ((CaptureService.CaptureServiceBinder) service).getService();
        	//com.conveyal.transitwand.CaptureServiceaptureService.setServiceClient(CaptureActivity.this);
        }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new);
		
		serviceIntent = new Intent(this, CaptureService.class);
        startService(serviceIntent);
        
        bindService(serviceIntent, caputreServiceConnection, Context.BIND_AUTO_CREATE);
		 
        //BOTON CONTINUAR - CONTINUE BUTTON
        ImageButton wandButton = (ImageButton) findViewById(R.id.ContinueButton);
        //Button wandButton = (Button) findViewById(R.id.ContinueButton);
        wandButton.setOnClickListener(this);
        
        //CREAR FOTOS - CREATE PHOTO
        ImageButton btn = (ImageButton) findViewById(R.id.btnCaptura);
        //Button btn = (Button)findViewById(R.id.btnCaptura);
        btn.setOnClickListener(this);
		img = (ImageView)findViewById(R.id.imagenBus);
		routeImage = new RouteImage();
		
		// Typeface
		Typeface font3 = Typeface.createFromAsset(getAssets(), "fonts/bebasneue_bold.ttf");
		
		TextView descriptionText = (TextView) findViewById(R.id.descriptionText);
		descriptionText.setTypeface(font3);
		
		TextView totalPasssengerCount = (TextView) findViewById(R.id.totalPasssengerCount);
		totalPasssengerCount.setTypeface(font3);
		
		TextView textView1 = (TextView) findViewById(R.id.textView1);
		textView1.setTypeface(font3);
		
		TextView textView2 = (TextView) findViewById(R.id.textView2);
		textView2.setTypeface(font3);
		
		TextView textView54 = (TextView) findViewById(R.id.textView54);
		textView54.setTypeface(font3);
		
	}
	
	@Override
	public void onClick(View v) {
		int id;
		id = v.getId();
		switch(id)
		{
			case R.id.btnCaptura:
				
				final CharSequence[] option = {"Tomar Foto", "Escoger desde galería", "Cancelar"};
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
			
			case R.id.ContinueButton:
				
				if(!captureService.capturing)
					createNewCapture();
				else {
					updateCapture();
				}
				Intent settingsIntent = new Intent(NewActivity.this, CaptureActivity.class);
				startActivity(settingsIntent);
				
			break;
			
			default:
			break;

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
		}
	}

	private void createNewCapture() {
		
		synchronized(this)	{ 
					
			EditText routeName = (EditText) findViewById(R.id.routeName);
			EditText routeDescription = (EditText) findViewById(R.id.routeDescription);
			EditText fieldNotes = (EditText) findViewById(R.id.fieldNotes);
			EditText vehicleCapacity = (EditText) findViewById(R.id.vehicleCapacity);
			EditText vehicleType = (EditText) findViewById(R.id.vehicleType);
			
			captureService.newCapture(routeName.getText().toString(), routeDescription.getText().toString(), 
					fieldNotes.getText().toString(), vehicleType.getText().toString(), vehicleCapacity.getText().toString(), routeImage.pathImage.toString());			

		}		
	}
	
	private void updateCapture() {
		
		synchronized(this)	{ 
			
			EditText routeName = (EditText) findViewById(R.id.routeName);
			EditText routeDescription = (EditText) findViewById(R.id.routeDescription);
			EditText fieldNotes = (EditText) findViewById(R.id.fieldNotes);
			EditText vehicleCapacity = (EditText) findViewById(R.id.vehicleCapacity);
			EditText vehicleType = (EditText) findViewById(R.id.vehicleType);
			
			captureService.currentCapture.setRouteName(routeName.getText().toString());
			captureService.currentCapture.description = routeDescription.getText().toString();
			captureService.currentCapture.notes = fieldNotes.getText().toString();
			captureService.currentCapture.vehicleCapacity = vehicleCapacity.getText().toString();
			captureService.currentCapture.vehicleType = vehicleType.getText().toString();
			captureService.currentCapture.path = routeImage.pathImage;
			captureService.currentCapture.imei = captureService.imei;
			
		}		
	}
}
