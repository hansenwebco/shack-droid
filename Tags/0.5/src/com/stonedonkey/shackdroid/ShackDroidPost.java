package com.stonedonkey.shackdroid;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

public class ShackDroidPost extends Activity {

	private String postID;
	private String storyID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.post);

		if (savedInstanceState != null) {
			// savedInstanceState.getLong("storyID");
			postID = savedInstanceState.getString("postID");
			storyID = savedInstanceState.getString("storyID");
		} 
		else 
		{
			Bundle extras = this.getIntent().getExtras();
			postID = extras.getString("postID");
			storyID = extras.getString("storyID");
		}
		
		// configure our TextField
		EditText et = (EditText) findViewById(R.id.EditTextPost);
		et.setVerticalScrollBarEnabled(true);

		// onClick for Post
		final Button postButton = (Button) findViewById(R.id.ButtonPost);
		postButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// action
				DoShackPost();
			}
		});

		// onClick for Post
		final Button cancelButton = (Button) findViewById(R.id.ButtonCancel);
		cancelButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// action
				finish();
				
			}
		});

	}

	private void DoShackPost() {
		// get the login and password for user out of our preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String login = prefs.getString("shackLogin", "");
		String password = prefs.getString("shackPassword", "");

		EditText ev = (EditText) findViewById(R.id.EditTextPost);
		String postText = ev.getText().toString();


		
		// create a URL to post to
		try {

			String data = URLEncoder.encode("iuser", "UTF-8") + "="
						+ URLEncoder.encode(login, "UTF-8") + "&"
						+ URLEncoder.encode("ipass", "UTF-8") + "="
						+ URLEncoder.encode(password, "UTF-8") + "&"
						+ URLEncoder.encode("group", "UTF-8") + "="
						+ URLEncoder.encode(storyID, "UTF-8") + "&"
						+ URLEncoder.encode("body", "UTF-8") + "="
						+ URLEncoder.encode(postText, "UTF-8");

			if (postID.length() > 0)
			{
				data = data + "&" + URLEncoder.encode("parent", "UTF-8") + "="
				+ URLEncoder.encode(postID, "UTF-8");
			}
			
			// post to ShackNews
			URL url = new URL("http://www.shacknews.com/extras/post_laryn_iphone.x");
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);

			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

			wr.write(data);
			wr.flush();

			// Capture reponse for handling
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			String result = "";
			while ((line = rd.readLine()) != null) {
				result = result + line;
			}
			wr.close();
			rd.close();

			// show an error messages to the user if needed
			// Shack sends back a <script> with a bunch of script.. so.. we look for error messages.. eh...
			TextView errorText = (TextView)findViewById(R.id.TextViewPostError);
			if (result.contains("You must be logged in to post") == true)
				errorText.setText("Login failed, please check your username and password.");
			else if (result.contains("Please post something with more than 5 characters.") == true)
				errorText.setText("Please post something with more than 5 characters.");
			else if (result.contains("Please wait a few minutes before trying to post again.") == true)
				errorText.setText("Please wait a few minutes before trying to post again.");
			else  // no errors
			{
				if (postID.length() > 0 && storyID.length() > 0) // back to thread
				{
					Intent intent = new Intent();
					intent.putExtra("postID", postID);
					intent.putExtra("storyID", storyID);
					// pass a result back to the ShackDroidThread.java letting
					// it know our work is done.
					setResult(RESULT_OK,intent); 
					finish();
					return;
				}
				else  // back to the main view
				{
					Intent intent = new Intent();
					intent.setClass(this, ShackDroid.class);
					startActivity(intent);
				}
				finish(); // this will be replaced with an intent
			}
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			TextView errorText = (TextView)findViewById(R.id.TextViewPostError);
			errorText.setText("There was an error submitting your post.");
			e.printStackTrace();
		}

	}
	// menu creation
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
	
		menu.add(1, 0, 0, "Settings");
		menu.add(1, 1, 0, "Tags");

		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case 0:
			// show settings dialog
			intent = new Intent();
			intent.setClass(this, ShackDroidPreferences.class);
			startActivity(intent);
			return true;
		
		case 1:
			TableLayout tv = (TableLayout)findViewById(R.id.TableLayoutShackTags);
			tv.setVisibility(0);
		}
		return false;
	}
}
