package com.stonedonkey.shackdroid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.Toast;

public class ActivityCamera extends Activity implements AutoFocusCallback, SurfaceHolder.Callback, PictureCallback {
	
	String _fileUri;
	Camera _cam;
	boolean _takingPicture;
	byte[] _pictureData;
	double _scaleAmount = 2; // half size
	private boolean _highResAvailable;
	private boolean _extraCompressionNeeded;
	private boolean _askToPost = false;
	private AsyncTask<byte[], String, String> uploadTask;
	
	public static final String UPLOADED_FILE_URL = "uploadedfileurl";
	
	private static final int MODE_TAKING_PICTURE = 0;
	private static final int MODE_SHOWING_PICTURE = 1;	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent caller = getIntent();
		setContentView(R.layout.camera);
		
		//If we have a calling intent see if it's passed us a content URI of an existing image
		// if not then assume we're taking a nice new photo
		Uri uri = caller.getData();
		if (uri == null && caller.hasExtra(Intent.EXTRA_STREAM)){
			uri = (Uri)caller.getExtras().get(Intent.EXTRA_STREAM);

			_askToPost = true;
		}
		
		if (uri != null){
			new CompressShareAsyncTask().execute(uri);
		}
		else{
			SurfaceHolder holder = ((SurfaceView)findViewById(R.id.SurfaceView01)).getHolder();
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			holder.addCallback(this);
	
			SetupButtons(MODE_TAKING_PICTURE);				
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

    private void Setup(int width, int height){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String login = prefs.getString("shackLogin", "");
		String password = prefs.getString("shackPassword", "");
		
		int size = width * height; 
		
		//3 possible states
		// 1: Can login (3Mb file max) so we're ok whatever at 95% compression
		// 2: No login and greater than 3Megapixels so we need to try and scale down to 3Mp AND compress to 90%
		// 3: No login 3Megapixel camera so we need to compress to 90%
		if (login.length() > 0 && password.length() > 0){
			_highResAvailable = true;
		}
		else if (size > 3145728){ //3 megapixels
			_extraCompressionNeeded = true;
			_scaleAmount = Math.floor(width / 2048);
		}
		else{
			_extraCompressionNeeded = true;
		}    	
    }
    
    @Override 
    protected void onPause(){
    	super.onPause();
    	_pictureData = null;
    }

	@Override
	public void onAutoFocus(boolean arg0, Camera arg1) {
		if (arg0 && _takingPicture){
			arg1.takePicture(null, null, this);
		}
		else if (arg0 == false){
			_takingPicture = false;
			Toast.makeText(this, "Unable to focus", Toast.LENGTH_SHORT);
		}
	}

	
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {
		
		Camera.Parameters parameters= _cam.getParameters();

		
		// NOTE: So, if you have functions in this Activity that aren't available in 1.5 or 1.6 the whole thing
		//       just bombs, so I moved them to their own static method in HelperAPI4... I have no idea if theres
		//       another way.. now.. will it work?
		if (Integer.parseInt(android.os.Build.VERSION.SDK) <=4)
		{
			// Possible fix for 1.5 - 1.6
			parameters.setPreviewSize(parameters.getPreviewSize().width,parameters.getPreviewSize().height);	
		}
		else
		{
			_cam = HelperAPI4.setCameraParams(_cam);
		}

		parameters.setPictureFormat(PixelFormat.JPEG);

		_cam.setParameters(parameters);
		_cam.startPreview();
			//-- Must add the following callback to allow the camera to autofocus.
		_cam.autoFocus(new Camera.AutoFocusCallback(){
			@Override
			public void onAutoFocus(boolean success, Camera camera) {
				Log.d("ShackDroid", "isAutofocus " +	Boolean.toString(success));					
			}
		} );
		
        
        // TODO: this line seems to crash some phones see bug report.
        // http://code.google.com/p/android/issues/detail?id=7909
        //_cam.setParameters(parameters);
        //_cam.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		_cam = Camera.open();
		
		//Parameters params = _cam.getParameters();
		//params.setPictureFormat(PixelFormat.JPEG);

		//Setup(params.getPictureSize().width, params.getPictureSize().height);
		
		//_cam.setParameters(params);			
		try {
			_cam.setPreviewDisplay(holder);
			//_cam.startPreview();
			//_cam.autoFocus(this);				
		} catch (IOException e) {
			_cam.release();
			_cam = null;
		}		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		_cam.stopPreview();
		_cam.release();
		_cam = null;
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		_pictureData = data;
		_takingPicture = false;
		SetupButtons(MODE_SHOWING_PICTURE);
	}
	
	private void SetupButtons(int mode){
		switch(mode){
		case MODE_TAKING_PICTURE:
			((Button)findViewById(R.id.takePicture)).setText("Oh snap");
			((Button)findViewById(R.id.takePicture)).setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					_takingPicture = true;
					_cam.autoFocus(ActivityCamera.this);
				}
			});				

			((Button)findViewById(R.id.btnCancel)).setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					setResult(RESULT_CANCELED);
					finish();
				}});						
			break;
		case MODE_SHOWING_PICTURE:
			((Button)findViewById(R.id.takePicture)).setText("Upload");
			((Button)findViewById(R.id.takePicture)).setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					new CompressAsyncTask().execute(_pictureData);
					
				}
			});
			
			((Button)findViewById(R.id.btnCancel)).setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					_takingPicture = false;
					_pictureData = null;
					
					SetupButtons(MODE_TAKING_PICTURE);
					_cam.startPreview();
				}});				
			break;
		}
	}

	class CompressShareAsyncTask extends AsyncTask<Uri, byte[], byte[]>{

		

		@Override
		protected byte[] doInBackground(Uri... params) {
			//First we find out how big the pic is
			//Then set setup our environment variables - scale factor etc
			//Then we configure our compressor
			//Then we compress and return.
			
			Options options = new Options();
			
			options.inJustDecodeBounds = true;
			Bitmap pic;
			try {
				pic = BitmapFactory.decodeStream(getContentResolver().openInputStream(params[0]), new Rect(-1,-1,-1,1), options);
				
				Setup(options.outWidth, options.outHeight);
				options.inJustDecodeBounds = false;
			} catch (FileNotFoundException e2) {
				return null;
			}
			
			int compressionAmount = 95;
			
			//Try and scale down 2 == half size, 4 == quarter size etc.
			options.inSampleSize = (int)_scaleAmount;
			
			if (_extraCompressionNeeded){
				compressionAmount = 90;
			}
			
			try {
				pic = BitmapFactory.decodeStream(getContentResolver().openInputStream(params[0]), new Rect(-1,-1,-1,1), options);
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
		}
		
		protected void onPreExecute(){
			findViewById(R.id.takePicture).setEnabled(false);
			showDialog(1);
		}
		
		protected void onPostExecute(byte[] result) {
		    dismissDialog(1);
		    if (result != null){
		     uploadTask = new UploadAsyncTask().execute(result);
		    }
		    else{
		    	Toast.makeText(getApplicationContext(), "Error compressing", Toast.LENGTH_SHORT);
		    	finish();
		    }
		}		
	}
	
	class CompressAsyncTask extends AsyncTask<byte[], byte[], byte[]>{

		@Override
		protected byte[] doInBackground(byte[]... params) {
			Options options = new Options();
			
			int compressionAmount = 95;
			
			//Try and scale down 2 == half size, 4 == quarter size etc.
			options.inSampleSize = (int)_scaleAmount;
			
			if (_extraCompressionNeeded){
				compressionAmount = 90;
			}
			
			Bitmap pic = BitmapFactory.decodeByteArray(params[0], 0, params[0].length, options);

			ByteArrayOutputStream compressed = new ByteArrayOutputStream();
			pic.compress(CompressFormat.JPEG, compressionAmount, compressed);  //Get it down
			pic.recycle();
			
			//byte[] data;
			try{
				_pictureData = compressed.toByteArray();
				try {
					compressed.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			catch(OutOfMemoryError err){
				return null;
			}
			return _pictureData;
		}
		
		protected void onPreExecute(){
			findViewById(R.id.takePicture).setEnabled(false);
			showDialog(1);
		}
		
		protected void onPostExecute(byte[] result) {
		    dismissDialog(1);
		    if (result != null){
		    	new UploadAsyncTask().execute(result);
		    }
		    else{
		    	Toast.makeText(getApplicationContext(), "Error compressing", Toast.LENGTH_SHORT);		    
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
						HttpPost req = new HttpPost("http://www.shackpics.com/users.x?act=login_go");
						List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
						
						nameValuePairs.add(new BasicNameValuePair("user_name", login));
						nameValuePairs.add(new BasicNameValuePair("user_password", password));
						req.setEntity(new UrlEncodedFormEntity(nameValuePairs));
						
						 String response = httpClient.execute(req, responseHandler);
						if (!response.contains("You have successfully been logged in")){
							// Do something here to re-size the image again?
						}
					}
				}
				
				HttpPost request = new HttpPost("http://www.shackpics.com/upload.x");

				//List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
				//nameValuePairs.add(new BasicNameValuePair("filename","droidUpload.jpg"));
				
				// *Tried changing this to encode data.  Hangs on setEntity() :(
				// Maybe we need to escape the [ and ]
				//nameValuePairs.add(new BasicNameValuePair("userfile[]",Base64.encodeBytes(data)));
				//request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				MultipartEntity  entity = new MultipartEntity();
				entity.addPart("filename",new StringBody("droidUpload.jpg"));
				entity.addPart("userfile[]", new InputStreamBody(new ByteArrayInputStream(data), "droidUpload.jpg"));
				request.setEntity(entity);

				
				String response = httpClient.execute(request,responseHandler);

				// Tested with: http://www.fileformat.info/tool/regex.htm
				Pattern p = Pattern.compile("http\\:\\/\\/www\\.shackpics\\.com\\/viewer\\.x\\?file=.*?\\.jpg");
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