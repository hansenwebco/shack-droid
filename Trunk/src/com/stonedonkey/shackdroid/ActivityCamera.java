package com.stonedonkey.shackdroid;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpClientConnection;

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
			
			ContentValues values = new ContentValues();
			values.put(Images.Media.TITLE, "title");
			values.put(Images.Media.BUCKET_ID, "test");
			values.put(Images.Media.DESCRIPTION, "test Image taken");
			values.put(Images.Media.MIME_TYPE, "image/jpeg");
			Uri uri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI,
					values);
			OutputStream outstream;
			try {
				outstream = getContentResolver().openOutputStream(uri);
				//x.compress(Bitmap.CompressFormat.JPEG, 70, outstream);
				
				
				URL url = null;
				HttpURLConnection conn;
				url = new URL("http://www.shackpics.com/upload.x");

				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setDoInput(true);
				
				conn.addRequestProperty("username", "");
				conn.addRequestProperty("password", "");
				conn.addRequestProperty("filename", "droidPhoneUpload.jpg");
				
				ByteArrayOutputStream baos= new ByteArrayOutputStream();
				x.compress(Bitmap.CompressFormat.JPEG, 70, baos);
				
				String imageData = Base64.encodeBytes(baos.toByteArray());
				conn.addRequestProperty("image",imageData);
								
				outstream.close();
				
				OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
				wr.flush();
				
				// Capture response for handling
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String line;
				String result = "";
				while ((line = rd.readLine()) != null) {
					result = result + line;
				}
				wr.close();
				rd.close();
				
				
			} catch (Exception e) {
				String fail = e.getMessage();
			String hold = "hold'";
			}
		}

	}
}