package com.stonedonkey.shackdroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

public class ActivityLOLTabs extends TabActivity {

	private int lolView = -1;
	private int viewID = 0;
	
	private static final int LOL_VIEW_LATEST = 0;
	private static final int LOL_VIEW_YOU_WROTE = 1;
	private static final int LOL_VIEW_YOU_LOLD = 2;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	Helper.SetWindowState(getWindow(),this);
	
	Bundle extras = this.getIntent().getExtras();
	if (extras != null)
		lolView = extras.getInt("LOLView");
	
	final TabHost tabHost = getTabHost();
		
	if (lolView == -1 || lolView == LOL_VIEW_LATEST) {
		setTitle("ShackLOLs - Last 24 Hours");
		tabHost.addTab(tabHost.newTabSpec("LOL").setIndicator("LOL").setContent(new Intent(this,ActivityLOL.class).putExtra("view", "LOL").putExtra("lolView", 0)));
		tabHost.addTab(tabHost.newTabSpec("INF").setIndicator("INF").setContent(new Intent(this,ActivityLOL.class).putExtra("view", "INF").putExtra("lolView", 0)));
		tabHost.addTab(tabHost.newTabSpec("TAG").setIndicator("TAG").setContent(new Intent(this,ActivityLOL.class).putExtra("view", "TAG").putExtra("lolView", 0)));
		tabHost.addTab(tabHost.newTabSpec("UNF").setIndicator("UNF").setContent(new Intent(this,ActivityLOL.class).putExtra("view", "UNF").putExtra("lolView", 0)));
	}
	else if (lolView == LOL_VIEW_YOU_WROTE)
	{
		setTitle("ShackLOLs - Stuff You Wrote");
		tabHost.addTab(tabHost.newTabSpec("LOL").setIndicator("LOL").setContent(new Intent(this,ActivityLOL.class).putExtra("view", "LOL").putExtra("lolView", 1)));
		tabHost.addTab(tabHost.newTabSpec("INF").setIndicator("INF").setContent(new Intent(this,ActivityLOL.class).putExtra("view", "INF").putExtra("lolView", 1)));
		tabHost.addTab(tabHost.newTabSpec("TAG").setIndicator("TAG").setContent(new Intent(this,ActivityLOL.class).putExtra("view", "TAG").putExtra("lolView", 1)));
		tabHost.addTab(tabHost.newTabSpec("UNF").setIndicator("UNF").setContent(new Intent(this,ActivityLOL.class).putExtra("view", "UNF").putExtra("lolView", 1)));
	}
	else if (lolView == LOL_VIEW_YOU_LOLD)
	{
		setTitle("ShackLOLs - Stuff You LOL'd");
		tabHost.addTab(tabHost.newTabSpec("LOL").setIndicator("LOL").setContent(new Intent(this,ActivityLOL.class).putExtra("view", "LOL").putExtra("lolView", 2)));
		tabHost.addTab(tabHost.newTabSpec("INF").setIndicator("INF").setContent(new Intent(this,ActivityLOL.class).putExtra("view", "INF").putExtra("lolView", 2)));
		tabHost.addTab(tabHost.newTabSpec("TAG").setIndicator("TAG").setContent(new Intent(this,ActivityLOL.class).putExtra("view", "TAG").putExtra("lolView", 2)));
		tabHost.addTab(tabHost.newTabSpec("UNF").setIndicator("UNF").setContent(new Intent(this,ActivityLOL.class).putExtra("view", "UNF").putExtra("lolView", 2)));
	}

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, 0, 1, "Menu").setIcon(R.drawable.menu_home);
		//menu.add(0, 1, 2, "Refresh").setIcon(R.drawable.menu_reload);
		menu.add(0, 2, 2, "View").setIcon(R.drawable.menu_folder);
		return true;
		
	}	
	@Override
    protected Dialog onCreateDialog(int id) {
        switch (id) 
        {
        case 1:
        	return new AlertDialog.Builder(ActivityLOLTabs.this)
            .setTitle("Choose Feed")
            .setSingleChoiceItems(R.array.lolViews, 0, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	viewID = whichButton;
                }
            })
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                     ChangeView(viewID);         
                }
            }) 
            .setNegativeButton("CANCEL", null).create(); 
        }
		return null;
    }	
	protected void ChangeView(int viewID)
	{
		// TODO: we are forcing a recall of this activity to do different
		//       view, I couldn't find a way to force this via code, ugly
		//       but it does work.
		Intent intent = new Intent();
		intent.setClass(this, ActivityLOLTabs.class);
		intent.putExtra("LOLView",viewID);
		startActivity(intent);
		finish();       
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// TODO: be nice to figure out the refresh and how to handle 
		//       recalling tabs already created
		switch (item.getItemId()) {
		case 0: // menu
			finish();
			return true;
		case 1: // refresh
			return true;
		case 2: // view
			showDialog(1);
			return true;

		}
		return false;
	}
}
