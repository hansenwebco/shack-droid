package com.stonedonkey.shackdroid;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

public class ActivityStats extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.stats);
		
		try {
			ShackDroidStats stats = ShackDroidStats.GetUserStats(this);
		
			Typeface face = Typeface.createFromAsset(this.getAssets(), "fonts/arial.ttf");
			
			// chatty stats
			TextView tv = null; 
						
			tv = (TextView) findViewById(R.id.TextViewViewedChatty);
			tv.setTypeface(face);
			tv.setText(String.valueOf(stats.getViewedChatty()));
			
			tv = (TextView) findViewById(R.id.TextViewPostsViewed);
			tv.setTypeface(face);
			tv.setText(String.valueOf(stats.getPostsViewed()));
			
			tv = (TextView) findViewById(R.id.TextViewPostsMade);
			tv.setTypeface(face);
			tv.setText(String.valueOf(stats.getPostsMade()));
			

			// ShackDroid Features
			tv = (TextView) findViewById(R.id.TextViewShackSearches);
			tv.setText(String.valueOf(stats.getShackSearches()));
			
			tv = (TextView) findViewById(R.id.TextViewVanitySearches);
			tv.setText(String.valueOf(stats.getVanitySearches()));
			
			tv = (TextView) findViewById(R.id.TextViewViewedShackMessages);
			tv.setText(String.valueOf(stats.getViewedShackMessages()));
			
			tv = (TextView) findViewById(R.id.TextViewSentShackMessage);
			tv.setText(String.valueOf(stats.getSentShackMessage()));
						
			tv = (TextView) findViewById(R.id.TextViewCheckedForNewVersion);
			tv.setText(String.valueOf(stats.getCheckedForNewVersion()));
			
			tv = (TextView) findViewById(R.id.TextViewViewRSSFeed);
			tv.setText(String.valueOf(stats.getViewedRSSFeed()));			

			tv = (TextView) findViewById(R.id.TextViewViewedShackLOLs);
			tv.setText(String.valueOf(stats.getViewedShackLOLs()));
			
			tv = (TextView) findViewById(R.id.TextViewViewedStats);
			tv.setText(String.valueOf(stats.getViewedStats()));
			

			//ThomW LOLs Stats
			tv = (TextView) findViewById(R.id.TextViewLOLsMade);
			tv.setText(String.valueOf(stats.getLolsMade()));

			tv = (TextView) findViewById(R.id.TextViewINFsMade);
			tv.setText(String.valueOf(stats.getInfsMade()));

			tv = (TextView) findViewById(R.id.TextViewTAGsMade);
			tv.setText(String.valueOf(stats.getTagsMade()));			

			tv = (TextView) findViewById(R.id.TextViewUNFsMade);
			tv.setText(String.valueOf(stats.getUnfsMade()));	
			
			
		} catch (Exception e) {	}
		
		
		
		

	}
}