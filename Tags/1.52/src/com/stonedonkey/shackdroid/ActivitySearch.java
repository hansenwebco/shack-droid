package com.stonedonkey.shackdroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ActivitySearch extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		setContentView(R.layout.search);

		final Button postButton = (Button) findViewById(R.id.ButtonSearch);
		postButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// action
				DoSearch();
			}
		});
	
		
	}
	protected void DoSearch()
	{
		
		//String searchTerm = extras.getString("searchTerm");
		//String author = extras.getString("author");
		//String parentAuthor = extras.getString("parentAuthor");
		
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
