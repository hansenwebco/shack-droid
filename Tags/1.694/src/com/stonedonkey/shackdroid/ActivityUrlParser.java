package com.stonedonkey.shackdroid;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class ActivityUrlParser extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//TODO:  This is a totally bullshit way of doing this.  Ought to be a broadcast receiver or something. anything. just not this.
		
	if (getIntent() != null && 
			getIntent().getAction() != null && 
			getIntent().getAction().equals(Intent.ACTION_VIEW)){
		final Uri uri = getIntent().getData();

		String storyID = uri.getQueryParameter("story");
		String postID = uri.getQueryParameter("id");
		
		if (storyID != null && storyID.length() > 0){
			Intent i = new Intent(this, ActivityTopicView.class);
			i.putExtra("StoryID", storyID);
			startActivity(i);
		}
		else if (postID != null && postID.length() > 0){
			Intent i = new Intent(this, ActivityThreadedView.class);
			i.putExtra("postID", postID);
			startActivity(i);			
		}
		else{
			finish();
		}
	}
	}
}
