package com.stonedonkey.shackdroid;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class ActivityViewMessage extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_message);

		ShackMessage msg = null;
		
		Bundle extras = this.getIntent().getExtras();
		if (extras != null)
			msg = (ShackMessage) extras.getSerializable("msg");

	TextView auth =(TextView)findViewById(R.id.TextViewViewMsgAuthor);
	auth.setText(Html.fromHtml("<b><font color='#ffba00'>Author:</font></b> " + msg.getName()));
		
	 TextView sub =(TextView)findViewById(R.id.TextViewViewMsgSubject);
	 sub.setText(Html.fromHtml("<b><font color='#ffba00'>Subject:</font></b> " + msg.getMsgSubject()));
		
	 TextView date =(TextView)findViewById(R.id.TextViewViewMsgDate);
	 date.setText(Html.fromHtml("<b><font color='#ffba00'>Date:</font></b> " + msg.getMsgDate()));
	 
	 TextView content =(TextView)findViewById(R.id.TextViewViewMsgContent);
	 content.setText(Html.fromHtml(msg.getMsgText().replaceAll("<br/><br/>","<br/>")));
		

	}
}	
	

