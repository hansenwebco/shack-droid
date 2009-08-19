package com.stonedonkey.shackdroid;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ActivityPostMessage extends Activity implements Runnable {

	ShackMessage msg = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.messages_post);

		Bundle extras = this.getIntent().getExtras();
		if (extras != null) {
			msg = (ShackMessage) extras.getSerializable("message");

			if (msg !=null)
			{
				EditText to = (EditText)findViewById(R.id.EditTextSMTo);
				to.setText(msg.getName());

				String msgSub= msg.getMsgSubject();
				EditText sub = (EditText)findViewById(R.id.EditTextSMSubject);
				if (msgSub.startsWith("Re: ") == false)
					sub.setText("Re: " + msgSub);
				else
					sub.setText(msgSub);


				EditText post = (EditText)findViewById(R.id.EditTextSMMessage);
				StringBuilder quote = new StringBuilder();
				quote.append("\n\n/[" + msg.getName() + " Wrote:\n");
				quote.append("-----\n");
				quote.append(msg.getMsgText().replace("<br><br>","\n").replaceAll("\\<.*?\\>","") + "\n");
				quote.append("-----]/");
				post.setText(quote.toString());
			}

		}

		// onClick for Post
		final Button postButton = (Button) findViewById(R.id.ButtonSMSubmit);
		postButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// action
				PostShackMessage();
				
			}
		});

		// onClick for Post
		final Button cancelButton = (Button) findViewById(R.id.ButtonSMCancel);
		cancelButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// action
				finish();

			}
		});



	}
	private void PostShackMessage()
	{
		showDialog(1);

		// use the class run() method to do work
		Thread thread = new Thread(this); 
		thread.start();
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch (id) {
		case 1: {
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage("Sending message, please wait...");
			dialog.setTitle(null);
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			return dialog;
		}
		}
		return null;
	}
	@Override
	public void run() {
		// get the login and password for user out of our preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String login = prefs.getString("shackLogin", "");
		String password = prefs.getString("shackPassword", "");

		EditText to = (EditText)findViewById(R.id.EditTextSMTo);
		EditText subject = (EditText)findViewById(R.id.EditTextSMSubject);
		EditText body = (EditText)findViewById(R.id.EditTextSMMessage);


		// create a URL to post to
		try {

			String data = URLEncoder.encode("username", "UTF-8") + "="
			+ URLEncoder.encode(login, "UTF-8") + "&"
			+ URLEncoder.encode("password", "UTF-8") + "="
			+ URLEncoder.encode(password, "UTF-8") + "&"
			+ URLEncoder.encode("subject", "UTF-8") + "="
			+ URLEncoder.encode(subject.getText().toString(), "UTF-8") + "&"
			+ URLEncoder.encode("to", "UTF-8") + "="
			+ URLEncoder.encode(to.getText().toString(), "UTF-8") + "&"
			+ URLEncoder.encode("body", "UTF-8") + "="
			+ URLEncoder.encode(body.getText().toString(), "UTF-8");
		
			
			// post to ShackNews
			URL url = new URL("http://shackapi.stonedonkey.com/messages/send/");
			String userPassword = login + ":" + password;
			String encoding = Base64.encodeBytes(userPassword.getBytes());
												
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("Authorization","Basic " + encoding);
			conn.setDoOutput(true);

			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

			wr.write(data);
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
			
			if (result.contains("error_username_missing") == true) {
				errorPostHandler.sendEmptyMessage(0);
			}
			else if (result.contains("error_password_missing") == true) {
				errorPostHandler.sendEmptyMessage(0);
			}
			else if (result.contains("error_message_to_missing") == true) {
				errorPostHandler.sendEmptyMessage(1);
			}
			else if (result.contains("error_message_subject_missing") == true) {
				errorPostHandler.sendEmptyMessage(2);
			}
			else if (result.contains("error_message_body_missing") == true) {
				errorPostHandler.sendEmptyMessage(3);
			}			
			else if (result.contains("error_communication_authentication") == true) {
				errorPostHandler.sendEmptyMessage(0);
			}
			else if (result.contains("error_communication_send") == true) {
				errorPostHandler.sendEmptyMessage(4);
			}								
			else
			{
			progressBarHandler.sendEmptyMessage(0);
			return;
			}


		} catch (Exception e) {

			errorPostHandler.sendEmptyMessage(3);
			//TextView errorText = (TextView)findViewById(R.id.TextViewPostError);
			//errorText.setText("There was an error submitting your post.");
			//e.printStackTrace();
		}
		progressBarHandler.sendEmptyMessage(0);
		
		
	}
	private Handler progressBarHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// we implement a handler because most UI items 
			// won't update within a thread
			try {
				
				dismissDialog(1);
			}
			catch (Exception ex)
			{
				// TODO : .dismiss is failing on the initial startup, something to do with the
				//        windows manager... this is a hacky fix.. :(  
				//String fail = ex.getMessage();
				//fillDataSAX();
				finish();
				return;
			}
			finish();
		}
	};	
	private Handler errorPostHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// we implement a handler because most UI items 
			// won't update within a thread
			try {
				dismissDialog(1);
			}
			catch (Exception ex)
			{
				// TODO : .dismiss is failing on the initial startup, something to do with the
				//        windows manager... this is a hacky fix.. :(  
				
			}
			TextView errorText = (TextView)findViewById(R.id.TextViewSMErrorMessage);
			switch (msg.what){
			case 0:
				errorText.setText("Login failed, please check your username and password.");
				break;
			case 1: 
				errorText.setText("Please enter the user to send the message to.");
				break;
			case 2:
				errorText.setText("Please enter a subject for your message.");
				break;
			case 3:
				errorText.setText("You cannot send a blank message.");
				break;
			case 4: 
				errorText.setText("Problem contacting Shack News please try again later.");
				break;
			}
			
		
		}
	};	
}
