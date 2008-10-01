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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
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
	private String selectedShackTagOpen = "";
	private String selectedShackTagClose = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.post);
		setTitle("ShackDroid - Post");

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
		SetShackTagAttributes();
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
			TextView errorText = (TextView)findViewById(R.id.TextViewPostError);
			errorText.setText("There was an error submitting your post.");
			e.printStackTrace();
		}

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
	        return new AlertDialog.Builder(ShackDroidPost.this)
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
