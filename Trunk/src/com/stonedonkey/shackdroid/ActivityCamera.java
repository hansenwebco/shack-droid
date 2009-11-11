package com.stonedonkey.shackdroid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.URLUtil;
import android.widget.Button;

public class ActivityCamera extends Activity implements AutoFocusCallback, SurfaceHolder.Callback, PictureCallback {
	
	Uri _fileUri;
	Camera _cam;
	boolean _takingPicture;
	boolean _imageNeedsResize;
	byte[] _pictureData;
	double _scaleAmount = 0;
	
	public static final String UPLOADED_FILE_URL = "uploadedfileurl";
	private static final int MODE_TAKING_PICTURE = 0;
	private static final int MODE_SHOWING_PICTURE = 1;	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.camera);
		SurfaceHolder holder = ((SurfaceView)findViewById(R.id.SurfaceView01)).getHolder();
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		holder.addCallback(this);

		SetupButtons(MODE_TAKING_PICTURE);
	}

    protected Dialog onCreateDialog(int id) {
    	switch(id){
	    	case 0:
	    		ProgressDialog loadingContent = new ProgressDialog(this);
		    	loadingContent.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		    	loadingContent.setTitle("Uploading file");
		    	loadingContent.setMessage("Please wait...");
		    	loadingContent.setCancelable(false);

		    	return loadingContent;
	    	case 1:
	    		ProgressDialog compressingContent = new ProgressDialog(this);
	    		compressingContent.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	    		compressingContent.setTitle("Compressing image");
	    		compressingContent.setMessage("Please wait...");
	    		compressingContent.setCancelable(false);

		    	return compressingContent;		    	
    	}
    	return null;
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
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
        Camera.Parameters parameters = _cam.getParameters();
        parameters.setPreviewSize(width, height);
        _cam.setParameters(parameters);
        _cam.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		_cam = Camera.open();
		
		Parameters params = _cam.getParameters();
		params.setPictureFormat(PixelFormat.JPEG);

		int size = params.getPictureSize().height * params.getPictureSize().width; 
		
		if (size >= 3145728){ //3 megapixels
			_imageNeedsResize = true;
			_scaleAmount = Math.floor(params.getPictureSize().width / 2048);
		}
		
		_cam.setParameters(params);			
		try {
			_cam.setPreviewDisplay(holder);
			_cam.startPreview();
			_cam.autoFocus(this);				
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

	
	class CompressAsyncTask extends AsyncTask<byte[], byte[], byte[]>{

		@Override
		protected byte[] doInBackground(byte[]... params) {
			
			byte[] data = params[0];
			if (_imageNeedsResize){

				Options options = new Options();
				if (_scaleAmount > 1){
					//Try and scale down 2 == half size, 4 == quarter size etc.
					options.inSampleSize = (int)_scaleAmount;
				}
				else{
					options = null;
				}
				
				Bitmap pic = BitmapFactory.decodeByteArray(data, 0, data.length, options);

				ByteArrayOutputStream compressed = new ByteArrayOutputStream();
				pic.compress(CompressFormat.JPEG, 90, compressed);  //Get it down to 800k-900k

				data = compressed.toByteArray();
				pic.recycle();
				try {
					compressed.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return data;
		}
		
		protected void onPreExecute(){
			findViewById(R.id.takePicture).setEnabled(false);
			showDialog(1);
		}
		
		protected void onPostExecute(byte[] result) {
		    dismissDialog(1);
		    new UploadAsyncTask().execute(result);
		}		
	}
	
	class UploadAsyncTask extends AsyncTask<byte[], String, String>{

		@Override
		protected String doInBackground(byte[]... params) {

			HttpClient httpClient = new DefaultHttpClient();
			try {
				byte[] data = params[0];
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

				ResponseHandler<String> responseHandler = new BasicResponseHandler();
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
				Intent passback = new Intent();
				passback.putExtra(ActivityCamera.UPLOADED_FILE_URL, result);
				setResult(RESULT_OK, passback);
		 	}
		 	else{
		 		setResult(RESULT_CANCELED);	
		 	}
			finish(); // close down and send the result we have set.
				
		}

	}		
}