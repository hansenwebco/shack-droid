package com.stonedonkey.shackdroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ActivitySearch extends Activity {

	private String view;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Helper.SetWindowState(getWindow(),this);

		Bundle extras = this.getIntent().getExtras();
		view = extras.getString("view");
		
		setContentView(R.layout.search);

		
		EditText search =(EditText)findViewById(R.id.EditTextSearch);
		EditText author = (EditText)findViewById(R.id.EditTextByUser);
		EditText parent = (EditText)findViewById(R.id.EditTextParentAuthor);
		
		search.setText(null);
		author.setText(null);
		parent.setText(null);
		search.setEnabled(true);
		author.setEnabled(true);
		parent.setEnabled(true);
		
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String username = prefs.getString("shackLogin", "");
		
		if (view == "vanity") {
			search.setText(username);
			search.setEnabled(false);
		}
		else if (view == "parent")
		{
			parent.setText(username);
			parent.setEnabled(false);
		}
		else if (view == "yours")
		{
			author.setText(username);
			author.setEnabled(false);
		}
		
		final Button postButton = (Button) findViewById(R.id.ButtonSearch);
		postButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				
				// update statistics
				if (view == "vanity") 
					ShackDroidStats.AddVanitySearch(getApplicationContext());
				else 
					ShackDroidStats.AddShackSearch(getApplicationContext());				

				// do search
				DoSearch();
			}
		});
	
		
	}
	protected void DoSearch()
	{

		
		EditText search =(EditText)findViewById(R.id.EditTextSearch);
		EditText author = (EditText)findViewById(R.id.EditTextByUser);
		EditText parent = (EditText)findViewById(R.id.EditTextParentAuthor);
				
		Intent intent = new Intent();
		intent.setClass(this, ActivitySearchResults.class);
		intent.putExtra("searchTerm" , search.getText().toString()); 
		intent.putExtra("author" , author.getText().toString());
		intent.putExtra("parentAuthor" , parent.getText().toString());
		
		startActivity(intent);		
	}

}
