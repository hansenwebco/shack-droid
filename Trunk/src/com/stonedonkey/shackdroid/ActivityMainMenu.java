package com.stonedonkey.shackdroid;

import java.util.ArrayList;
import java.util.Random;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;

public class ActivityMainMenu extends ListActivity  {
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	     super.onCreate(savedInstanceState);
		
	     Helper.SetWindowState(getWindow(),this);
	     
	     ArrayList<ShackMenuItem> menu = new ArrayList<ShackMenuItem>();
	     
	     menu.add(new ShackMenuItem("Latest Chatty","It gets you chicks, and diseases.",R.drawable.menu2_latestchatty));
	     menu.add(new ShackMenuItem("Shack RSS", "The \"Mos Eisley\" of chatties.",R.drawable.menu2_rss));
	     menu.add(new ShackMenuItem("Shack Search","For all your vanity needs.",R.drawable.menu2_search));
	     menu.add(new ShackMenuItem("Shack Marks","Your mobile tranny porn Stash.",R.drawable.menu2_shackmarks2));
	     menu.add(new ShackMenuItem("Shack Messages","Stuff too shocking for even the Shack.",R.drawable.menu2_shackmessages2));
	     menu.add(new ShackMenuItem("Settings","Hay guys, am I doing this right?",R.drawable.menu2_settings));
	     menu.add(new ShackMenuItem("Version Check","stonedonkey finally did something new!?!",R.drawable.menu2_vercheck));
	     //menu.add(new ShackMenuItem("Search v2","Tabs omg!",R.drawable.menu2_search));
	     
	     AdapterMainMenu mm = new AdapterMainMenu(this,R.layout.mainmenu_row, menu);
	    
	     //getListView().setDivider("#333333");
	     
	     String[] urls = getResources().getStringArray(R.array.titles);
	     int titles =urls.length;
	     Random r = new Random();
	     setTitle("ShackDroid - " + urls[r.nextInt(titles)]);
	     
	     ColorDrawable cd = new ColorDrawable(Color.parseColor("#333333"));
	     getListView().setDivider(cd);
	     getListView().setDividerHeight(1);
	     
	     setListAdapter(mm);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Intent intent = new Intent();;
		switch (position) {
			case 0: // LatestChatty
			{
				intent.setClass(this, ActivityTopicView.class);
				break;
			}
			case 1: 
			{
				intent.setClass(this,ActivityRSS.class);
				break;
			}
			case 2: 
			{
				intent.setClass(this,ActivitySearchTabs.class);
				break;
			
			}
			case 3: 
			{
				intent.setClass(this,ActivityShackMarks.class);
				break;
			}
			case 4:
			{
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				boolean allowSMs = prefs.getBoolean("allowShackMessages", false);
				
				if (allowSMs)
				{
				intent = new Intent();
				intent.setClass(this,ActivityMessages.class);
				startActivity(intent);
				return;
				}
				else
				{
					new AlertDialog.Builder(this).setTitle("Information")
					.setPositiveButton("OK", null).setMessage(
							"Shack Messages posts your credentials to the API " +
							"instead of directly ShackNews.\n\n If you agree with this " +
							"you can enable this feature under \"Settings\"." )
					.show();
					
				return;
				}
			}
			case 5:
			{
				intent.setClass(this,ActivityPreferences.class);
				break;
			}
			case 6:
			{
				String message = "Unable to complete version check, please try again later.";

				final String result = HandlerExtendedSites.VersionCheck(this);
				boolean updateAvail = false;
				
				if (result != null && result != "*fail*") {
					message = "GOOD NEWS EVERYBODY!\n\nA new version of ShackDroid is available, would you like to get it now?";
					updateAvail = true;
				}
				else if (result == null) // if we got null we're good if we got fail we failed
					message = "ShackDroid is up to date.";

				if (updateAvail) 
				{
				// show update dialog
				new AlertDialog.Builder(this)
				.setTitle("Version Check")
				.setPositiveButton("YES",  new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	 
                    	startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(result))); 
                    	finish();
                }
				})
				.setNegativeButton("NO", null).setMessage(message).show();

				}
				else
				{
					new AlertDialog.Builder(this)
					.setTitle("Version Check")
					.setPositiveButton("OK",null)
					.setMessage(message).show();
				}
					
				
				break;
			}
			case 7:
			{
				intent.setClass(this,ActivitySearchTabs.class);
				break;
			}
			
		}	
		
		if (position != 6) {
			startActivity(intent);
			
		}
	}
	protected void NewVersionCheck()
	{
		
	}
}
