package com.stonedonkey.shackdroid;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class ActivityLOLTabs extends TabActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	Helper.SetWindowState(getWindow(),this);
	
	final TabHost tabHost = getTabHost();
	tabHost.addTab(tabHost.newTabSpec("LOL").setIndicator("LOL").setContent(new Intent(this,ActivityLOL.class).putExtra("view", "LOL")));
	tabHost.addTab(tabHost.newTabSpec("INF").setIndicator("INF").setContent(new Intent(this,ActivityLOL.class).putExtra("view", "INF")));
	tabHost.addTab(tabHost.newTabSpec("TAG").setIndicator("TAG").setContent(new Intent(this,ActivityLOL.class).putExtra("view", "TAG")));
	tabHost.addTab(tabHost.newTabSpec("UNF").setIndicator("UNF").setContent(new Intent(this,ActivityLOL.class).putExtra("view", "UNF")));
	}
	
}
