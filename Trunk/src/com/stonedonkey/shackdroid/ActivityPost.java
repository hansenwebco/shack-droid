package com.stonedonkey.shackdroid;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

public class ActivityPost extends Activity implements Runnable {

	private String postID;
	private String storyID;
	private String selectedShackTagOpen = "";
	private String selectedShackTagClose = "";
	
	public static final String UPLOADED_FILE_URL = "uploadedfileurl"; // TODO: Needs more global...
	private static final int CAMERA_RESULT = 0;
	private static final int UPLOAD_RESULT = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Helper.SetWindowState(getWindow(),this);
		
		setContentView(R.layout.post);
		setTitle("ShackDroid - Post");
		//Log.e("ShackDroid",android.os.Build.VERSION.SDK);
		ShowRulesWarning();

		final Typeface face = Typeface.createFromAsset(getAssets(), "fonts/arial.ttf");
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final int fontSize = Integer.parseInt(prefs.getString("fontSize", "12"));
		
		
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
		
		et.setTypeface(face);
		et.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
		
		// onClick for Post
		final Button postButton = (Button) findViewById(R.id.ButtonPost);
		postButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// action
				ShackDroidStats.AddPostsMade(getBaseContext());
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

		final Button cameraButton = (Button)findViewById(R.id.ButtonCamera);
		cameraButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				

				Intent intent = new Intent();
				intent.setClass(getBaseContext(), ActivityCamera.class);
				startActivityForResult(intent,CAMERA_RESULT);
				
				
			}
		});

		final Button uploadButton = (Button)findViewById(R.id.ButtonUpload);
		uploadButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				

				Intent intent = new Intent(Intent.ACTION_PICK);
				intent.setType("image/*");
				//intent.setClass(getBaseContext(), ActivityCamera.class);
				startActivityForResult(intent,UPLOAD_RESULT);
				
				
			}
		});		
		SetShackTagAttributes();
		
		Intent i = getIntent();
		if (i.hasExtra(UPLOADED_FILE_URL)){
			et.setText(i.getStringExtra(UPLOADED_FILE_URL));
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch(requestCode){
		case CAMERA_RESULT:
			if (resultCode == RESULT_OK) {
				TextView tv = (TextView)findViewById(R.id.EditTextPost);
				final String currentText = tv.getText().toString(); 
				String url = data.getStringExtra(UPLOADED_FILE_URL);
				if (currentText != null && currentText.length() > 0)
					tv.setText(currentText + "\n\n"  + url);
				else
					tv.setText(url);
			}

			break;
		case UPLOAD_RESULT:
			if (resultCode == RESULT_OK) {
				String url = data.getDataString();
				Intent camera = new Intent();
				camera.setClass(this, ActivityCamera.class);
				camera.setData(Uri.parse(url));
				//intent.putExtra(ActivityCamera.EXISTING_FILE_URI, url);
				startActivityForResult(camera, CAMERA_RESULT);
				
			}
			break;
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

		try {

			// pd.dismiss();
			dismissDialog(2);
		} catch (Exception ex) {
			// dialog could not be killed for some reason
		}
		
		TextView tv = (TextView)findViewById(R.id.EditTextPost);
		savedInstanceState.putString("postText", tv.getText().toString());
		
	}
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		Bundle extras = this.getIntent().getExtras();
		postID = extras.getString("postID");
		storyID = extras.getString("storyID");
		
		TextView tv = (TextView)findViewById(R.id.EditTextPost);
		if (savedInstanceState != null)
			tv.setText(savedInstanceState.getString("postText"));
		
	}
	
	// Add Listeners for Tag Buttons
	private void SetShackTagAttributes()
	{
		// shacktag : red
		final Button tagRed = (Button) findViewById(R.id.ButtonRed);
		tagRed.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ShackTagText("r{","}r");
			}
		});
		// shacktag : green
		final Button tagGreen = (Button) findViewById(R.id.ButtonGreen);
		tagGreen.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ShackTagText("g{","}g");
			}
		});
		// shacktag : blue
		final Button tagBlue = (Button) findViewById(R.id.ButtonBlue);
		tagBlue.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ShackTagText("b{","}b");
			}
		});
		// shacktag : yellow
		final Button tagYellow = (Button) findViewById(R.id.ButtonYellow);
		tagYellow.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ShackTagText("y{","}y");
			}
		});
		// shacktag : lime
		final Button tagLime = (Button) findViewById(R.id.ButtonLime);
		tagLime.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ShackTagText("l[","]l");
			}
		});
		// shacktag : orange
		final Button tagOrange = (Button) findViewById(R.id.ButtonOrange);
		tagOrange.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ShackTagText("n[","]n");
			}
		});
		// shacktag : multisync (pink)
		final Button tagMultiSync = (Button) findViewById(R.id.ButtonMultisync);
		tagMultiSync.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ShackTagText("p[","]p");
			}
		});
		// shacktag : olive
		final Button tagOlive = (Button) findViewById(R.id.ButtonOlive);
		tagOlive.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ShackTagText("e[","]e");
			}
		});
		// shacktag : italics
		final Button tagItalics = (Button) findViewById(R.id.ButtonItalics);
		tagItalics.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ShackTagText("/[","]/");
			}
		});
		// shacktag : italics
		final Button tagBold = (Button) findViewById(R.id.ButtonBold);
		tagBold.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ShackTagText("b[","]b");
			}
		});
		// shacktag : quote
		final Button tagQuote = (Button) findViewById(R.id.ButtonQuote);
		tagQuote.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ShackTagText("q[","]q");
			}
		});
		// shacktag : quote
		final Button tagSample = (Button) findViewById(R.id.ButtonSample);
		tagSample.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ShackTagText("s[","]s");
			}
		});
		// shacktag : underline
		final Button tagUnderline = (Button) findViewById(R.id.ButtonUnderline);
		tagUnderline.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ShackTagText("_[","]_");
			}
		});
		// shacktag : strike
		final Button tagStrike = (Button) findViewById(R.id.ButtonStike);
		tagStrike.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ShackTagText("-[","]-");
			}
		});
		// shacktag : strike
		final Button tagSpoiler = (Button) findViewById(R.id.ButtonSpoil);
		tagSpoiler.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ShackTagText("o[","]o");
			}
		});
		// shacktag : code
		final Button tagCode = (Button) findViewById(R.id.ButtonCode);
		tagCode.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ShackTagText("/{{","}}/");
			}
		});
	}
	
	private void ShackTagText(String tagOpen, String tagClose)
	{	
		// if we have selected text we shack tag it...
		TextView tv = (TextView)findViewById(R.id.EditTextPost);
		if (tv.hasSelection())
		{
			int start = tv.getSelectionStart();
			int end = tv.getSelectionEnd();
			
			// selections can be made backwards, in which case
			// the start becomes > the end.. lets flip them.
			if (start > end)
				{
					Integer hold = end;
					end = start;
					start = hold;
				}
			// get selected text	
			String selection = tagOpen +  tv.getText().toString().substring(start,end) + tagClose;
			
			// get all text
			String text =  tv.getText().toString();
			
			// replace with shack tag
			text = text.substring(0, start) + selection + text.substring(end);
			
			tv.setText(text);
			

		}
		else // prompt
		{
			selectedShackTagOpen = tagOpen;
			selectedShackTagClose = tagClose;
			showDialog(1);
		}
		//return;
	}
	

	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch (id) {
			case 1:
				// Ripped from AlertDialogSamples.java SDK examples
				// This example shows how to add a custom layout to an AlertDialog
				LayoutInflater factory = LayoutInflater.from(this);
				final View textEntryView = factory.inflate(R.layout.text_entry_dialog, null);
				return new AlertDialog.Builder(ActivityPost.this)
				.setTitle("Enter text to ShackTag")
				.setView(textEntryView)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
	
						/* User clicked OK so do some stuff */
						EditText tv = (EditText)findViewById(R.id.EditTextPost);
						String text = tv.getText().toString();
	
						TextView shackTag = (TextView)textEntryView.findViewById(R.id.TextViewShackTagText);
						String shackTagText = shackTag.getText().toString();
						shackTag.setText(""); // reset entry box
	
						Integer cursorPosition = tv.getSelectionStart();
	
						text = text.substring(0,cursorPosition) + selectedShackTagOpen + shackTagText + selectedShackTagClose + text.substring(cursorPosition);
						tv.setText(text);
	
						tv.setSelection(text.length());
					}
				})
				.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						/* User clicked cancel so do some stuff */
					}
				}).create();	
			case 2: {
				ProgressDialog dialog = new ProgressDialog(this);
				dialog.setMessage("Posting, please wait...");
				dialog.setTitle(null);
				dialog.setIndeterminate(true);
				dialog.setCancelable(false);
				return dialog;
			}
		}
		return null;
	}
	 
	// menu creation
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
	
		menu.add(1, 0, 0, "Settings").setIcon(R.drawable.menu_settings);
		menu.add(1, 1, 0, "Tags").setIcon(R.drawable.menu_shacktags);

		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case 0:
			// show settings dialog
			intent = new Intent();
			intent.setClass(this, ActivityPreferences.class);
			startActivity(intent);
			return true;
		
		case 1:
			TableLayout tv = (TableLayout)findViewById(R.id.TableLayoutShackTags);
			tv.setVisibility(0);
		}
		return false;
	}
	private void DoShackPost() {
		
		//pd = ProgressDialog.show(this, null, "Posting, please wait...", true,false); 
		//pd.setIcon(R.drawable.shack_logo);
		showDialog(2);
		
		// use the class run() method to do work
		Thread thread = new Thread(this); 
		thread.start();
	}
	 
	@Override
	public void run() {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String login = prefs.getString("shackLogin", "");
		String password = prefs.getString("shackPassword", "");

		EditText ev = (EditText) findViewById(R.id.EditTextPost);
		String postText = ev.getText().toString();
		
		// create a URL to post to
		try {

			String data = URLEncoder.encode("content_type_id", "UTF-8") + "="
						+ URLEncoder.encode("17", "UTF-8") + "&"
						+ URLEncoder.encode("content_id", "UTF-8") + "="
						+ URLEncoder.encode(storyID, "UTF-8") + "&"
						+ URLEncoder.encode("body", "UTF-8") + "="
						+ URLEncoder.encode(postText, "UTF-8");

			if (postID != null && postID.length() > 0)
			{
				data = data + "&" + URLEncoder.encode("parent_id", "UTF-8") + "="
				+ URLEncoder.encode(postID, "UTF-8");
			}
			
		

	
		String userPassword = login + ":" + password;

		String encoding = Base64.encodeBytes(userPassword.getBytes());
		
		
			// post to ShackNews
			URL url = new URL("http://new.shacknews.com/api/chat/create/17.json");
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("Authorization", "Basic " + encoding);
			conn.setRequestProperty("User-Agent", Helper.getUserAgentString(this));

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
			if (result.contains("You must be logged in to post") == true) {
				errorPostHandler.sendEmptyMessage(0);
			}
			else if (result.contains("Please post something with more than 5 characters.") == true)
				errorPostHandler.sendEmptyMessage(1);
			else if (result.contains("Please wait a few minutes before trying to post again.") == true)
				errorPostHandler.sendEmptyMessage(2);
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

					progressBarHandler.sendEmptyMessage(0);
					return;
				}
				else  // back to the main view
				{
					Intent intent = new Intent();
					intent.putExtra("StoryID",storyID );
					intent.setClass(this, ActivityTopicView.class);
					startActivity(intent);
				}
				progressBarHandler.sendEmptyMessage(0);
			}
	
		} catch (Exception e) {
			errorPostHandler.sendEmptyMessage(3);
		}
	}
private void ShowRulesWarning() {
		
		try {
			SharedPreferences settings=getPreferences(0);

			// NOTE: debugging resets value
			//SharedPreferences.Editor editor = settings.edit();
			//editor.putBoolean("hideRulesWarning", false);
			//editor.commit(); 

			boolean hideRulesWarning = settings.getBoolean("hideRulesWarning", false);
			if (!hideRulesWarning)
			{
				final String msg = "WARNING!\n\nThis app is just one portal to a much larger community.  If you are new here tap \"Rules\" to read up on what to do and what not to do.  Improper conduct may lead to unpleasant experiences and getting banned by community moderators.\n\nLastly, use the text formatting tags sparingly.  Please.";

				new AlertDialog.Builder(this)
				.setTitle("IMPORTANT!")
				.setPositiveButton("OK",null)
				.setNegativeButton("Hide",new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						SharedPreferences settings=getPreferences(0);
						SharedPreferences.Editor editor = settings.edit();
						editor.putBoolean("hideRulesWarning", true);
						editor.commit(); 
					}
				})
				.setNeutralButton("Rules", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(Uri.parse("http://www.shacknews.com/extras/guidelines.x"));
						startActivity(i);
					}
				})
				.setMessage(msg).show();
			}
		}
		catch (Exception ex)
		{
			// do nothing
		}
		
	}
	private Handler progressBarHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				dismissDialog(2);
			}
			catch (Exception ex)
			{
				finish();
				return;
			}
			finish();
		}
	};
	
	private Handler errorPostHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			try {
				dismissDialog(2);
			}
			catch (Exception ex) {
			
			}
			
			TextView errorText = (TextView)findViewById(R.id.TextViewPostError);
			switch (msg.what){
			case 0:
				errorText.setText("Login failed, please check your username and password.");
				break;
			case 1: 
				errorText.setText("Please post something with more than 5 characters.");
				break;
			case 2:
				errorText.setText("Please wait a few minutes before trying to post again.");
				break;
			case 3:
				errorText.setText("There was an error submitting your post, try again later.");
				break;
			}
		}
	};
}
