package com.stonedonkey.shackdroid;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ActivityViewMessage extends Activity {

	ShackMessage msg = null;
	static Typeface face;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_message);

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
	 
	 //TextView msglbl =(TextView)findViewById(R.id.TextViewViewMsgLabelMessage);
	 //msglbl.setText(Html.fromHtml("<b>Message:</b>"));

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
		case 2:
			finish();
			return true;
			
		}
		return false;
	}
}	
	

