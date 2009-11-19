package com.stonedonkey.shackdroid;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;



public class ActivitySearchTabs extends TabActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	Helper.SetWindowState(getWindow(),this);
		
	final TabHost tabHost = getTabHost();
	tabHost.addTab(tabHost.newTabSpec("Search").setIndicator("Search").setContent(new Intent(this,ActivitySearch.class).putExtra("view", "search")));
	tabHost.addTab(tabHost.newTabSpec("Vanity").setIndicator("Vanity").setContent(new Intent(this,ActivitySearch.class).putExtra("view", "vanity")));
	tabHost.addTab(tabHost.newTabSpec("Parent").setIndicator("Parent").setContent(new Intent(this,ActivitySearch.class).putExtra("view", "parent")));
	tabHost.addTab(tabHost.newTabSpec("Yours").setIndicator("Yours").setContent(new Intent(this,ActivitySearch.class).putExtra("view", "yours")));
	
	//int tab = getTabHost().getCurrentTab();
	
	//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	//String username = prefs.getString("shackLogin", "");
	
	
	
	}	
}

