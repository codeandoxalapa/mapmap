package org.codeandoxalapa.mapmap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

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
    	byte[] imageBytes = null;
    	switch (requestCode)
		{
			case PHOTO_CODE:
				this.path = null;
				this.pathImage = convertImage(this.mPath);
				imageBytes = Base64.decode(this.pathImage, Base64.DEFAULT);
				this.bitmap = decodeSampledBitmapFromByteArray(imageBytes, 100, 100);
				this.typeAction = PHOTO_CODE;
			break;
			
			case SELECT_PICTURE:
				Uri path = data.getData();
				this.path = path;
				String uri = getRealPathFromURI(context, path);
				this.pathImage = convertImage(uri);
				imageBytes = Base64.decode(this.pathImage, Base64.DEFAULT);
				this.bitmap = decodeSampledBitmapFromByteArray(imageBytes, 100, 100);
				this.typeAction = SELECT_PICTURE;
			break;
		}
    	
    	return this;
    }
    
    private String convertImage(String uri)
    {
		File imagefile = new File(uri);
		FileInputStream fis = null;
		FileInputStream fis2 = null;
        String encImage = "";
		
		try 
		{
		    fis = new FileInputStream(imagefile);
		    fis2 = new FileInputStream(imagefile);
		    encImage = encodeBytesStringToBase64(fis, fis2, 800, 800, 50);
		} 
		catch (FileNotFoundException e) 
		{
		    e.printStackTrace();
		}
		catch (OutOfMemoryError e)
		{
			e.printStackTrace();
			encImage = encodeBytesStringToBase64(fis, fis2, 400, 400, 30);
		}
		catch(Exception e){
			e.printStackTrace();
			Log.e("File Input Strem", e.getMessage());
		}
		
		Log.d("V", "Ubicación de la imagen o foto: "+uri);
		Log.d("V", "tamaño de string base 64: "+ encImage.length());
		return encImage;
    }
    
    public String encodeBytesStringToBase64(FileInputStream fis, FileInputStream fis2, int reqWidth, int reqHeight, int bitmapCompressQuality){
    	Bitmap bitmap = decodeSampledBitmapFromFileInputStream(fis, fis2,  null,reqWidth, reqHeight);
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, bitmapCompressQuality, baos);
    	byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
    
    public static Bitmap decodeSampledBitmapFromFileInputStream(FileInputStream fis, FileInputStream fis2, Rect resId,
            int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(fis, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(fis2, resId, options);
    }
    
    public static Bitmap decodeSampledBitmapFromByteArray(byte[] data, int   reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    //options.inPurgeable = true; 
	    BitmapFactory.decodeByteArray(data, 0, data.length, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeByteArray(data, 0, data.length, options);
	}
    
    public static int calculateInSampleSize( BitmapFactory.Options options, int reqWidth, int reqHeight) {
    	// Raw height and width of image
    	final int height = options.outHeight;
    	final int width = options.outWidth;
    	int inSampleSize = 1;

    	if (height > reqHeight || width > reqWidth) {

    		final int halfHeight = height / 2;
    		final int halfWidth = width / 2;

    		// Calculate the largest inSampleSize value that is a power of 2 and keeps both
    		// height and width larger than the requested height and width.
    		while ((halfHeight / inSampleSize) >= reqHeight
    				&& (halfWidth / inSampleSize) >= reqWidth) {
    			inSampleSize *= 2;
    		}
    	}

    	return inSampleSize;
    }
}
