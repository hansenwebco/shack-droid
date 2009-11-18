package com.stonedonkey.shackdroid;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TabHost;



public class ActivitySearchTabs extends TabActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	Helper.SetWindowState(getWindow(),this);
		
	final TabHost tabHost = getTabHost();
	tabHost.addTab(tabHost.newTabSpec("Search").setIndicator("Local").setContent(new Intent(this,ActivitySearch.class).putExtra("view", "search")));
	tabHost.addTab(tabHost.newTabSpec("Vanity").setIndicator("Vanity").setContent(new Intent(this,ActivitySearch.class).putExtra("view", "vanity")));
	tabHost.addTab(tabHost.newTabSpec("Parent").setIndicator("Parent").setContent(new Intent(this,ActivitySearch.class).putExtra("view", "parent")));
	tabHost.addTab(tabHost.newTabSpec("Yours").setIndicator("Yours").setContent(new Intent(this,ActivitySearch.class).putExtra("view", "yours")));
	
	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	String username = prefs.getString("shackLogin", "");
	
	if (username ==null || username.length() == 0) {
		//int test =tabHost.getChildCount();
		//tabHost.getChildAt(1).setEnabled(false);
		
	}
	
	}	
}

