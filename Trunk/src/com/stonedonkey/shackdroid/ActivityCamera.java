package com.stonedonkey.shackdroid;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class ActivityCamera extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);
		Button cameraButton = (Button) findViewById(R.id.cameraButton);
		cameraButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ContentValues values = new ContentValues();
				values.put(Images.Media.TITLE, "title");
				values.put(Images.Media.BUCKET_ID, "test");
				values.put(Images.Media.DESCRIPTION, "test Image taken");
				Uri uri = getContentResolver().insert(
						Media.EXTERNAL_CONTENT_URI, values);
				Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
				intent.putExtra("output", uri.getPath());
				startActivityForResult(intent, 0);
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
			Bitmap x = (Bitmap) data.getExtras().get("data");
			((ImageView) findViewById(R.id.pictureView)).setImageBitmap(x);
			
			
			
			try {

		
	
				HttpClient httpClient = new DefaultHttpClient();
			
				HttpPost request = new HttpPost("http://www.shackpics.com/upload.x");
				MultipartEntity  entity = new MultipartEntity();
				entity.addPart("filename",new StringBody("droidUpload"));
				entity.addPart("userfile[]", new FileBody(null));
				request.setEntity(entity);
			
				HttpResponse response = httpClient.execute(request);
				int status = response.getStatusLine().getStatusCode();

				if (status != HttpStatus.SC_OK) {
				    // see above  
				} else {
				    // see above
				}

				  
				
			} catch (Exception e) {
				String fail = e.getMessage();
				String hold = "hold'";
			}
		}

	}
}