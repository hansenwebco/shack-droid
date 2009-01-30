package com.stonedonkey.shackdroid;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ActivityPostMessage extends Activity {

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
				finish();
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
		// TODO Auto-generated method stub
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
			

		} catch (Exception e) {

			//errorPostHandler.sendEmptyMessage(3);
			//TextView errorText = (TextView)findViewById(R.id.TextViewPostError);
			//errorText.setText("There was an error submitting your post.");
			//e.printStackTrace();
		}
	}
}
