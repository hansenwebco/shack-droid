package com.stonedonkey.shackdroid;

import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ActivityViewMessage extends Activity {

	ShackMessage msg = null;
	static Typeface face;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Helper.SetWindowState(getWindow(),this);
		
		setContentView(R.layout.view_message);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int fontSize = Integer.parseInt(prefs.getString("fontSize", "12"));
		
		face = Typeface.createFromAsset(this.getAssets(), "fonts/arial.ttf");
		Bundle extras = this.getIntent().getExtras();
		if (extras != null)
			msg = (ShackMessage) extras.getSerializable("msg");

	TextView auth =(TextView)findViewById(R.id.TextViewViewMsgAuthor);
	auth.setText(Html.fromHtml("<b><font color='#ffba00'>Author:</font></b> " + msg.getName()));
	auth.setTypeface(face);	
	
	 TextView sub =(TextView)findViewById(R.id.TextViewViewMsgSubject);
	 sub.setText(Html.fromHtml("<b><font color='#ffba00'>Subject:</font></b> " + msg.getMsgSubject()));
	sub.setTypeface(face);
	 
	 
	 TextView date =(TextView)findViewById(R.id.TextViewViewMsgDate);
	 date.setText(Html.fromHtml("<b><font color='#ffba00'>Date:</font></b> " + msg.getMsgDate()));
	 date.setTypeface(face);
	 
	 TextView content =(TextView)findViewById(R.id.TextViewViewMsgContent);
	 content.setText(Html.fromHtml(msg.getMsgText().replaceAll("(\r\n|\r|\n|\n\r)", "")));
	 content.setTypeface(face);
	content.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
	Linkify.addLinks(content, Linkify.ALL);
	
	String login = prefs.getString("shackLogin", "");
	String password = prefs.getString("shackPassword", "");
	
	if (msg.getMessageStatus() != "read")
		new ShackMessageMarkRead(login,password,msg.getMsgID()).execute();
	
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(1, 0 ,0,"Reply").setIcon(R.drawable.menu_reply);
		menu.add(1,1 ,1,"Back").setIcon(R.drawable.menu_back);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Context context = this;
		Intent intent;
		switch (item.getItemId()) {
		case 0: // Launch post form
			intent = new Intent();
			intent.setClass(this, ActivityPostMessage.class);
			intent.putExtra("message", msg);
			startActivity(intent);
			return true;
		case 1:
			finish();
			return true;
			
		}
		return false;
	}
}	
class ShackMessageMarkRead extends AsyncTask<Void,Void,Integer>{

	private String _login;
	private String _password;
	private String _messageID;
	
	public ShackMessageMarkRead(String login, String password, String messageID)
	{
		this._login = login;
		this._password = password;
		this._messageID = messageID;
	}
	
	
	@Override
	protected Integer doInBackground(Void... arg0) {
	
		try {
			String userPassword = _login + ":" + _password;
			String encoding = Base64.encodeBytes(userPassword.getBytes());
			
			URL url = new URL("http://shackapi.stonedonkey.com/messages/read/?username=" + _login +"&password=" + _password +"&messageid=" + _messageID);
			
			URLConnection uc = url.openConnection();
			uc.setRequestProperty("Authorization", "Basic " + encoding);
			uc.getInputStream();
		}
		catch (Exception ex) {
		
		}
		
		
		return null;
		
	}
	
}
	

