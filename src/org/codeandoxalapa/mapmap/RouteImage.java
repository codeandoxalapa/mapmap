package org.codeandoxalapa.mapmap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;

public class RouteImage {
	
	public String pathImage = "not image load";
	public String mPath;
	public Uri path;
    public String APP_DIRECTORY = "MapMap/";
    public String MEDIA_DIRECTORY = APP_DIRECTORY + "MapatonXalapa2016";
    public final int PHOTO_CODE = 100;
    public final int SELECT_PICTURE = 200;
    public Bitmap bitmap;
    public int typeAction;
    
    public Object[] openGallery()
    {
    	Object[] arr = new Object[3];
    	
    	arr[0] = true;
    	Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.setType("image/*");
		intent = intent.createChooser(intent, "Selecciona una foto");
		arr[1] = intent;
		arr[2] = SELECT_PICTURE;
		
    	return arr;
    }
    
    public Object[] openCamera() 
    {
    	Object[] arr = new Object[3];
    	arr[0] = false;
		File file = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
		boolean isDirectoryCreated = file.exists();
		
		if(!isDirectoryCreated)
		{
			isDirectoryCreated = file.mkdirs();
		}
		
		if(isDirectoryCreated)
		{
			Long timestamp = System.currentTimeMillis() / 1000;
			String imageName = timestamp.toString() + ".jpg";
			this.mPath = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY + File.separator + imageName;
			
			File  newFile = new File(this.mPath);
			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
			
			arr[0] = true;
			arr[1] = cameraIntent;
			arr[2] = PHOTO_CODE;
		}
		
		return arr;
	}
    
    public String getRealPathFromURI(Context context, Uri contentURI) 
    {
	    String result;
	    Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
	    
	    if (cursor == null) 
	    { // Source is Dropbox or other similar local file path
	        result = contentURI.getPath();
	    } 
	    else 
	    { 
	        cursor.moveToFirst(); 
	        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA); 
	        result = cursor.getString(idx);
	        cursor.close();
	    }
	    
	    return result;
	}
    
    public RouteImage typeActionPhoto(int requestCode, Context context, Intent data)
    {
    	switch (requestCode)
		{
			case PHOTO_CODE:
				this.path = null;
				this.bitmap = BitmapFactory.decodeFile(this.mPath);
				this.pathImage = convertImage(this.mPath);
				this.typeAction = PHOTO_CODE;
			break;
			
			case SELECT_PICTURE:
				Uri path = data.getData();
				this.path = path;
				String uri = getRealPathFromURI(context, path);
				this.pathImage = convertImage(uri);
				this.typeAction = SELECT_PICTURE;
			break;
		}
    	
    	return this;
    }
    
    private String convertImage(String uri)
    {
		File imagefile = new File(uri);
		FileInputStream fis = null;
		
		try 
		{
		    fis = new FileInputStream(imagefile);
		} 
		catch (FileNotFoundException e) 
		{
		    e.printStackTrace();
		}

		Bitmap bm = BitmapFactory.decodeStream(fis);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		bm.compress(Bitmap.CompressFormat.JPEG, 100 , baos);    
		byte[] b = baos.toByteArray(); 
		String encImage = Base64.encodeToString(b, Base64.DEFAULT);
		
		return encImage;
    }
    
}
