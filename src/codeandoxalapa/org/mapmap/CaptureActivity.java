package codeandoxalapa.org.mapmap;

import java.text.DecimalFormat;
import java.util.Date;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.view.Menu;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class CaptureActivity extends Activity implements ICaptureActivity {
	
	private static Intent serviceIntent;
    private CaptureService captureService;
    
    private Vibrator vibratorService;
    
    private static final String LOGTAG = "LogsAndroid";
    private CheckBox checkStopValidator;
    
    
    private final ServiceConnection caputreServiceConnection = new ServiceConnection()
    {

        public void onServiceDisconnected(ComponentName name)
        {
        	captureService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service)
        {
        	captureService = ((CaptureService.CaptureServiceBinder) service).getService();
        	captureService.setCaptureActivity(CaptureActivity.this);  
        	
        	initButtons();
 		   
        	updateRouteName();
 		   	updateDistance();
 		    updateDuration();
 		   	updateStopCount();
 		   	updatePassengerCountDisplay();
        }
    };

    
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capture);
		
		// Start the service in case it isn't already running
		
		serviceIntent = new Intent(this, CaptureService.class);
        startService(serviceIntent);
        
        bindService(serviceIntent, caputreServiceConnection, Context.BIND_AUTO_CREATE);
        CaptureService.boundToService = true;
        
        vibratorService = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        
        TextView routeNameText = (TextView) findViewById(R.id.routeNameText);
        TextView gpsStatus = (TextView) findViewById(R.id.gpsStatus);
        TextView notesText = (TextView) findViewById(R.id.notesText);
        TextView uploadText = (TextView) findViewById(R.id.uploadText);
        TextView TextView5 = (TextView) findViewById(R.id.TextView5);
        TextView captureChronometer = (TextView) findViewById(R.id.captureChronometer);
        TextView distanceText = (TextView) findViewById(R.id.distanceText);
        TextView stopsText = (TextView) findViewById(R.id.stopsText);
        TextView totalPasssengerCount = (TextView) findViewById(R.id.totalPasssengerCount);
        TextView text1 = (TextView) findViewById(R.id.text1);
        TextView textView1 = (TextView) findViewById(R.id.textView1);
        
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/bebasneue_bold.ttf");
        Typeface onramp = Typeface.createFromAsset(getAssets(), "fonts/onramp.ttf");
        Typeface fontRegular = Typeface.createFromAsset(getAssets(), "fonts/bebasneue_regular.ttf");
        
        routeNameText.setTypeface(font);
        gpsStatus.setTypeface(font);
        notesText.setTypeface(fontRegular);
        uploadText.setTypeface(fontRegular);
        TextView5.setTypeface(fontRegular);
        captureChronometer.setTypeface(font);
        distanceText.setTypeface(font);
        stopsText.setTypeface(font);
        totalPasssengerCount.setTypeface(onramp);
        text1.setTypeface(fontRegular);
        textView1.setTypeface(fontRegular);
		
		// setup button listeners
		
		View.OnClickListener listener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				switch (v.getId()) {
				
					case R.id.StartCaptureButton:
			
						if(!captureService.capturing) {
							startCapture();
						}
						
						break;
						
					case R.id.StopCaptureButton:
						
						if(captureService.capturing) {
						
							DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
							    @Override
							    public void onClick(DialogInterface dialog, int which) {
							        switch (which){
							        case DialogInterface.BUTTON_POSITIVE:
							        	stopCapture();
							            break;
	
							        case DialogInterface.BUTTON_NEGATIVE:
							            //No button clicked
							            break;
							        }
							    }
							};
	
							AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
							builder.setMessage("�Quieres finalizar el mapeo de esta ruta?").setPositiveButton("Si", dialogClickListener)
							    .setNegativeButton("No", dialogClickListener).show();
							
						}
				
						break;
					
					case R.id.transitStopButton:
						
						transitStop();
						
						break;
					
					case R.id.checkStopValidator:
						
						checkStopSignal();
		             
					break;
						
					case R.id.PassengerAlightButton:
						
						passengerAlight();
						
						break;
					
					case R.id.PassengerBoardButton:
						
						passengerBoard();
						
						break;
						
					 default:
						 break;
				}
			}
		   };
		   
		   
		   checkStopValidator  = (CheckBox) findViewById(R.id.checkStopValidator);
		   checkStopValidator.setOnClickListener(listener);
		   checkStopValidator.setEnabled(false);
		   
		   ImageButton startCaptureButton = (ImageButton) findViewById(R.id.StartCaptureButton);
		   startCaptureButton.setOnClickListener(listener);
		   
		   ImageButton stopCaptureButton = (ImageButton) findViewById(R.id.StopCaptureButton);
		   stopCaptureButton.setOnClickListener(listener);		 
		   
		   ImageButton transitStopButton = (ImageButton) findViewById(R.id.transitStopButton);
		   transitStopButton.setOnClickListener(listener);		 
		   
		   ImageButton passengerAlightButton = (ImageButton) findViewById(R.id.PassengerAlightButton);
		   passengerAlightButton.setOnClickListener(listener);		 
		   
		   ImageButton passengerBoardButton = (ImageButton) findViewById(R.id.PassengerBoardButton);
		   passengerBoardButton.setOnClickListener(listener);
		  		 
		   ImageView passengerImageView = (ImageView) findViewById(R.id.passengerImageView);
		   passengerImageView.setAlpha(128);
		   
		   initButtons();
		   
		   updateRouteName();
		   updateDistance();
		   updateStopCount();
		   updatePassengerCountDisplay();
		   
	}

	
	private void initButtons() {
		
		if(captureService == null) {
			ImageButton startCaptureButton = (ImageButton) findViewById(R.id.StartCaptureButton);
			startCaptureButton.setImageResource(R.drawable.start_button);
			
			ImageButton stopCaptureButton = (ImageButton) findViewById(R.id.StopCaptureButton);
			stopCaptureButton.setImageResource(R.drawable.stop_button_gray);
		}
		
		else if(captureService.capturing) {
			ImageButton startCaptureButton = (ImageButton) findViewById(R.id.StartCaptureButton);
			startCaptureButton.setImageResource(R.drawable.start_button_gray);
			
			ImageButton stopCaptureButton = (ImageButton) findViewById(R.id.StopCaptureButton);
			stopCaptureButton.setImageResource(R.drawable.stop_button);
		}
		else if(!captureService.capturing && captureService.currentCapture == null) {

			ImageButton startCaptureButton = (ImageButton) findViewById(R.id.StartCaptureButton);
			startCaptureButton.setImageResource(R.drawable.start_button_gray);
			
			ImageButton stopCaptureButton = (ImageButton) findViewById(R.id.StopCaptureButton);
			stopCaptureButton.setImageResource(R.drawable.stop_button_gray);
		}
		
		if(!captureService.capturing && captureService.currentCapture == null && captureService.atStop()) {

			ImageButton transitCaptureButton = (ImageButton) findViewById(R.id.transitStopButton);
			transitCaptureButton.setImageResource(R.drawable.transit_stop_button_red);
			
		}
	}
	
	private void startCapture() {
		
		if(captureService != null)
		{
			try { 
				captureService.startCapture();
			} catch(NoCurrentCaptureException e) {
				Intent settingsIntent = new Intent(CaptureActivity.this, NewActivity.class);
				startActivity(settingsIntent);
				return;
			}
			
			vibratorService.vibrate(100);
			
			Toast.makeText(CaptureActivity.this, "Iniciar Trazado..." ,Toast.LENGTH_SHORT).show();
			
			((Chronometer) findViewById(R.id.captureChronometer)).setBase(SystemClock.elapsedRealtime());
			((Chronometer) findViewById(R.id.captureChronometer)).start();
			
			initButtons();
		}
	}
	
	private void stopCapture() {
		
		if(captureService != null) {
			
			vibratorService.vibrate(100);
			
			if(captureService.currentCapture.points.size() > 0)
				Toast.makeText(CaptureActivity.this, "Trazado completo.", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(CaptureActivity.this, "No hay datos almacenados, ruta cancelada.", Toast.LENGTH_SHORT).show();
			
			captureService.stopCapture();
		}
		// else handle 
		
		
		
		((Chronometer) findViewById(R.id.captureChronometer)).stop();
		
		initButtons();
		
		Intent finishCaptureIntent = new Intent(CaptureActivity.this, MainActivity.class);
		finishCaptureIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(finishCaptureIntent);
	}
	
	private void passengerAlight() {
		
		if(captureService != null && captureService.capturing) {
			if(captureService.currentCapture.totalPassengerCount == 0)	
				return;
			
			vibratorService.vibrate(5);
			
			captureService.currentCapture.totalPassengerCount--;
			
			captureService.currentCapture.alightCount++;
			updatePassengerCountDisplay();
		}
	}
	
	private void passengerBoard() { 
		
		if(captureService != null && captureService.capturing) {
			captureService.currentCapture.totalPassengerCount++;
			
			vibratorService.vibrate(5);
			
			captureService.currentCapture.boardCount++;
			updatePassengerCountDisplay();
		}
	}
	
	public void triggerTransitStopDepature() {
		
		if(captureService != null) {
			if(captureService.atStop()) {
				transitStop();
			}
		}
	}
	
	private void transitStop() {
		
		if(captureService != null && captureService.capturing) {
			try {
				if(captureService.atStop()) {
					checkStopValidator.setEnabled(false);
					
					boolean banSignal = false;
					if(checkStopValidator.isChecked()){
						banSignal = true;
					}else{
						banSignal = false;
					}
					
					captureService.departStopStop(captureService.currentCapture.boardCount, captureService.currentCapture.alightCount, banSignal);
					captureService.currentCapture.alightCount = 0;
					captureService.currentCapture.boardCount = 0;
					
					
					ImageButton transitCaptureButton = (ImageButton) findViewById(R.id.transitStopButton);
					transitCaptureButton.setImageResource(R.drawable.transit_stop_button);
					
					
					vibratorService.vibrate(25);
					
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					vibratorService.vibrate(25);
					
					Toast.makeText(CaptureActivity.this, "Salida de la parada", Toast.LENGTH_SHORT).show();
				}
				else {
					checkStopValidator.setEnabled(true);
					captureService.ariveAtStop();
					
					ImageButton transitCaptureButton = (ImageButton) findViewById(R.id.transitStopButton);
					transitCaptureButton.setImageResource(R.drawable.transit_stop_button_red);
					
					vibratorService.vibrate(25);
					
					Toast.makeText(CaptureActivity.this, "Llegada a la parada", Toast.LENGTH_SHORT).show();
				}
				
				updatePassengerCountDisplay();
				
				updateStopCount();
				checkStopValidator.setChecked(false);
			} 
			catch(NoGPSFixException e) {
				 Toast.makeText(CaptureActivity.this, "GPS bloqueado no puede marcar paradas.", Toast.LENGTH_SHORT).show();
			}
		}
	
	}
	
	private void checkStopSignal(){
		if(captureService != null && captureService.capturing) {
				StringBuffer OUTPUT = new StringBuffer();
				if(checkStopValidator.isChecked()){
					OUTPUT.append("Señalizada");
				}else{
					OUTPUT.append("No Señalizada");
				}
                Toast.makeText(CaptureActivity.this, OUTPUT.toString(),Toast.LENGTH_LONG).show();
		}
	}
	
	private void updateRouteName() {
		TextView routeNameText = (TextView) findViewById(R.id.routeNameText);
		
		routeNameText.setText("Trazo: " + captureService.currentCapture.name);
	}
	
	private void updateStopCount() {
		TextView stopsText = (TextView) findViewById(R.id.stopsText);

		stopsText.setText(captureService.currentCapture.stops.size() + "");
	}
	
	public void updateDistance() {

		TextView distanceText = (TextView) findViewById(R.id.distanceText);
	
		DecimalFormat distanceFormat = new DecimalFormat( "#,##0.00" );
	
		distanceText.setText(distanceFormat.format((double)(captureService.currentCapture.distance) / 1000) + "km");
	
	}
	
	public void updateDuration() {
		if(captureService.capturing) {
			((Chronometer) findViewById(R.id.captureChronometer)).setBase(captureService.currentCapture.startMs);
			((Chronometer) findViewById(R.id.captureChronometer)).start();
		}
		else {
			((Chronometer) findViewById(R.id.captureChronometer)).setBase(SystemClock.elapsedRealtime());
		}
	}
	
	public void updateGpsStatus() {
		TextView distanceText = (TextView) findViewById(R.id.gpsStatus);
		
		distanceText.setText(captureService.getGpsStatus());
	}
	
	private void updatePassengerCountDisplay() {
		
		if(captureService != null && captureService.capturing) {
			
			TextView totalPassengerCount = (TextView) findViewById(R.id.totalPasssengerCount);
			TextView alightingPassengerCount = (TextView) findViewById(R.id.alightingPassengerCount);
			TextView boardingPassengerCount = (TextView) findViewById(R.id.boardingPassengerCount);
			
			totalPassengerCount.setText(captureService.currentCapture.totalPassengerCount.toString());
			
			if(captureService.currentCapture.alightCount > 0)
				alightingPassengerCount.setText("-" + captureService.currentCapture.alightCount.toString());
			else
				alightingPassengerCount.setText("0");
			
			if(captureService.currentCapture.boardCount > 0)
				boardingPassengerCount.setText("+" + captureService.currentCapture.boardCount.toString());
			else
				boardingPassengerCount.setText("0");
		}
	}
}
