package com.stonedonkey.shackdroid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;

public class ActivityCamera extends Activity {
	
	String _fileUri;
	Uri _localFileUri;
	byte[] _pictureData;
	boolean _scaleImage = false;
	private boolean _highResAvailable;
	private boolean _askToPost = false;
	private AsyncTask<byte[], String, String> uploadTask;
	
	public static final String UPLOADED_FILE_URL = "uploadedfileurl";
	public static final String TEMP_PICTURE_LOCATION = "/Android/data/com.stonedonkey.shackdroid/files/";
	private static final int CAMERA_TAKE_PIC = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//If we have a calling intent see if it's passed us a content URI of an existing image
		// if not then assume we're taking a nice new photo
		Intent caller = getIntent();
		Uri uri = caller.getData();
		if (uri == null && caller.hasExtra(Intent.EXTRA_STREAM)){
			uri = (Uri)caller.getExtras().get(Intent.EXTRA_STREAM);

			_askToPost = true;
		}
		
		if (uri != null) {
			new CompressShareAsyncTask().execute(uri);
		}
		else {
			//File pictureLoc = new File(getExternalFilesDir(), "temp.jpg");		// API level 8 only
			//File pictureLoc = new File(getCacheDir(), "temp.jpg");				// Can't write to this from Camera app

			File pictureLoc;
			pictureLoc = new File(Environment.getExternalStorageDirectory(), TEMP_PICTURE_LOCATION);
			
			// SD card is mounted, hopefully we're all good
			if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
				prepareAndLaunchCamera(pictureLoc);
			} else if (Build.MODEL.equalsIgnoreCase("ADR6300")) {
				// Special case for HTC Incredible.  The device has 8GB of internal storage 
				// and has symlink /emmc -> /mnt/emmc.  If the SD card is not mounted,
				// /sdcard SHOULD point to /mnt/emmc (in order to preserve compatibility with 
				// Environment.getExternalStorageDirectory()), but the method still returns
				// /sdcard.  Thanks HTC, you numbnuts.
				
				// TODO: Check if one of the newer API calls return the "correct" path,
				// as the Incredible now ships with 2.2.
				
				// More info at source.android.com/compatibility/android-2.2-cdd.pdf
				// Page 17, section 8.15
	
				Log.d("ShackDroid", "Incredible workaround.  No SD card mounted, writing to internal storage.");
				
				// Cannot find a way to get /mnt/emmc path programmatically.  So it'll have to
				// be hard-coded.  Is TEMP_PICTURE_LOCATION even the right directory when stored
				// in the internal storage?  Who the hell knows!
				pictureLoc = new File("/emmc", TEMP_PICTURE_LOCATION);
				prepareAndLaunchCamera(pictureLoc);
			} else {
				// No SD card mounted, no special cases accounted for.
				Log.d("ShackDroid", "SD card not mounted");
				Toast.makeText(getApplicationContext(), "SD card not present or ready.", Toast.LENGTH_SHORT).show();
				
				finish();
			}
		}
	}
	
	private void prepareAndLaunchCamera(File pictureLoc) {
		// Create directories if they do not exist.
		pictureLoc.mkdirs();
		
		// Hide folder from media scanner
		try {
			File nomedia = new File(pictureLoc, ".nomedia");
			nomedia.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}	

		pictureLoc = new File(pictureLoc, "temp.jpg");
		_localFileUri = Uri.fromFile(pictureLoc);
		
		Intent camera = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		camera.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, _localFileUri);
		
		Log.d("ShackDroid", _localFileUri.toString());
		
		startActivityForResult(camera, CAMERA_TAKE_PIC);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_TAKE_PIC) {
			if (resultCode == RESULT_OK) {
				//new CompressAsyncTask().execute(_localFileUri);
				new CompressShareAsyncTask().execute(_localFileUri);
			} else if (resultCode == RESULT_CANCELED) {
				// Just close the activity
				setResult(RESULT_CANCELED);
				finish();
			}
		}
	}


    protected Dialog onCreateDialog(int id) {
    	switch(id){
	    	case 0:
	    		ProgressDialog loadingContent = new ProgressDialog(this);
		    	loadingContent.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		    	loadingContent.setTitle("Uploading file");
		    	loadingContent.setMessage("Please wait...");
		    	loadingContent.setCancelable(true);
		    	
		    	loadingContent.setOnCancelListener(new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface arg0) {
						if (uploadTask != null) {
							uploadTask.cancel(true);
							Log.d("ShackDroid", "Canceled Upload Process");
						}
					
						finish();
					}
		    		
		    	});

		    	return loadingContent;
	    	case 1:
	    		ProgressDialog compressingContent = new ProgressDialog(this);
	    		compressingContent.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	    		compressingContent.setTitle("Compressing image");
	    		compressingContent.setMessage("Please wait...");
	    		compressingContent.setCancelable(false);

		    	return compressingContent;
	    	case 2:
	    		AlertDialog askToPost = new AlertDialog.Builder(this)
	    			.setTitle("")
	    			.setMessage("Post a new thread, or copy to clipboard?")
	    			.setNeutralButton("Clip",new AlertDialog.OnClickListener(){
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
  						    clipboard.setText(_fileUri);
							finish();
						}})
	    			.setPositiveButton("Yes", new AlertDialog.OnClickListener(){
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							Intent newPost = new Intent(getApplicationContext(), ActivityPost.class);
							newPost.putExtra(ActivityPost.UPLOADED_FILE_URL, _fileUri);
							newPost.putExtra("storyID", Helper.GetCurrentChattyStoryID());
							newPost.putExtra("postID", "");  // send black string for new posts
							startActivity(newPost);
							finish();
						}})
	    			.setNegativeButton("No", new AlertDialog.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}})
	    			.create();
	    		
	    		return askToPost;
    	}
    	return null;
    }

    class CompressShareAsyncTask extends AsyncTask<Uri, byte[], byte[]>{

		@Override
		protected byte[] doInBackground(Uri... params) {
			
			
			// Ripped from:  http://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue/823966#823966

			try {
			
				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(getContentResolver().openInputStream(params[0]), null, o);
	
				final int REQUIRED_SIZE = 800;
				
				int width_tmp = o.outWidth, height_tmp = o.outHeight;
				int scale = 1;
				while (true)
				{
					if (width_tmp/2< REQUIRED_SIZE || height_tmp/2< REQUIRED_SIZE)
						break;
					
					width_tmp/=2;
					height_tmp/=2;
					scale*=2;
				}
				
				// Decode with inSampleSize which prevents the memory error..
				BitmapFactory.Options o2 = new BitmapFactory.Options();
				o2.inSampleSize = scale;
				
				final Bitmap pic = BitmapFactory.decodeStream(getContentResolver().openInputStream(params[0]), new Rect(-1,-1,-1,1), o2);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				pic.compress(CompressFormat.JPEG, 75, bos);
				return bos.toByteArray();
				
				
			
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
			
			
			
			/*
			
			//First we find out how big the pic is
			//Then set setup our environment variables - scale factor etc
			//Then we configure our compressor
			//Then we compress and return.
			
			Options options = new Options();
			
			
			options.inJustDecodeBounds = true;
			Bitmap pic;
			int imgHeight; 
			int imgWidth;
			try {
				pic = BitmapFactory.decodeStream(getContentResolver().openInputStream(params[0]), new Rect(-1,-1,-1,1), options);
				
				imgHeight = options.outHeight;
				imgWidth = options.outWidth;
				
				Setup(options.outWidth, options.outHeight);
				options.inJustDecodeBounds = false;
			} catch (FileNotFoundException e2) {
				return null;
			}

			int compressionAmount = 95;
			
			//Try and scale down 2 == half size, 4 == quarter size etc.
			//options.inSampleSize = (int)_scaleAmount;
			
			if (_extraCompressionNeeded){
				compressionAmount = 90;
			}
			
			try {
				pic = BitmapFactory.decodeStream(getContentResolver().openInputStream(params[0]), new Rect(-1,-1,-1,1), null);
				
				if (_scaleImage) {
					double ratio = (double) MAX_IMAGE_AREA / (double)(imgHeight * imgWidth);
					int scaledHeight = (int)(imgHeight * ratio);
					int scaledWidth = (int)(imgWidth * ratio);
					
					pic = Bitmap.createScaledBitmap(pic, scaledWidth, scaledHeight, true);
				}
				
				ByteArrayOutputStream compressed = new ByteArrayOutputStream();
				pic.compress(CompressFormat.JPEG, compressionAmount, compressed);  //Get it down
				pic.recycle();
				_pictureData = compressed.toByteArray();
				try {
					compressed.close();
				} catch (IOException e) {
					e.printStackTrace();
				}				
				
			} catch (FileNotFoundException e1) {
				return null;
			}catch(OutOfMemoryError err){
				return null;
			}

			return _pictureData;
			
			
			*/
			
		}
		
		protected void onPreExecute(){
			//findViewById(R.id.takePicture).setEnabled(false);
			showDialog(1);
		}
		
		protected void onPostExecute(byte[] result) {
		   
			try {
				dismissDialog(1);
			}
			catch(Exception ex)
			{
				// dismissing dialogs sometimes fails, this traps it and ignores its 
			}
		    if (result != null){
		    	uploadTask = new UploadAsyncTask().execute(result);
		    	//new UploadAsyncTask().execute(result);
		    }
		    else{
		    	Toast.makeText(getApplicationContext(), "Error compressing", Toast.LENGTH_SHORT);
		    	finish();
		    }
		}		
	}

	
	class UploadAsyncTask extends AsyncTask<byte[], String, String>{

		@Override
		protected String doInBackground(byte[]... params) {

			HttpClient httpClient = new DefaultHttpClient();
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			try {
				byte[] data = params[0];
				
				if (_highResAvailable){
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					String login = prefs.getString("shackLogin", "");
					String password = prefs.getString("shackPassword", "");
					
					if (login.length() > 0 && password.length() > 0){
						HttpPost req = new HttpPost("http://chattypics.com/users.x?act=login_go");
						List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
						
						nameValuePairs.add(new BasicNameValuePair("user_name", login));
						nameValuePairs.add(new BasicNameValuePair("user_password", password));
						req.setEntity(new UrlEncodedFormEntity(nameValuePairs));
						
						 String response = httpClient.execute(req, responseHandler);
						 
						if (!response.contains("You have successfully been logged in")){
							Log.e("chattypics login failure", response);
							// Do something here to re-size the image again?
						}
					}
				}
				
				HttpPost request = new HttpPost("http://chattypics.com/upload2.php");
				request.setHeader("Referer", "http://chattypics.com/");
				
				//List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
				//nameValuePairs.add(new BasicNameValuePair("filename","droidUpload.jpg"));
				
				// *Tried changing this to encode data.  Hangs on setEntity() :(
				// Maybe we need to escape the [ and ]
				//nameValuePairs.add(new BasicNameValuePair("userfile[]",Base64.encodeBytes(data)));
				//request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				MultipartEntity  entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				//entity.addPart("filename",new StringBody("droidUpload.jpg"));
				entity.addPart("type", new StringBody("direct"));
				entity.addPart("userfile[]", new InputStreamBody(new ByteArrayInputStream(data), "droidUpload.jpg"));
				request.setEntity(entity);


				String response = httpClient.execute(request,responseHandler);

				// Tested with: http://www.fileformat.info/tool/regex.htm
				Pattern p = Pattern.compile("http\\:\\/\\/chattypics\\.com\\/viewer\\.x\\?file=.*?\\.jpg");
				Matcher m = p.matcher(response);
				
				if (m.find()){
					String url = m.group();
					if (URLUtil.isValidUrl(url)){
						return url;
					}
					else{
						return null;
					}
				}
				else{
					return null;
				}
			} catch (Exception e) {
				if (e.getMessage() != null)
					Log.e("ShackDroid" , e.getMessage());
					
				Log.e("ShackDroid", "Error on camera upload");
				return null;
			}
			finally {
				httpClient.getConnectionManager().shutdown();
			}
		}
		
		protected void onPreExecute(){
			showDialog(0);
		}
		protected void onPostExecute(String result) {
		    dismissDialog(0);
		 	if (result != null){
		 		if (_askToPost){
		 			_fileUri = result;
		 			showDialog(2);
		 		}
		 		else{
					Intent passback = new Intent();
					passback.putExtra(ActivityCamera.UPLOADED_FILE_URL, result);
					setResult(RESULT_OK, passback);
					finish(); // close down and send the result we have set.
		 		}
		 	}
		 	else{
		 		setResult(RESULT_CANCELED);
		 		finish(); // close down and send the result we have set.
		 	}
			
				
		}

	}		
}