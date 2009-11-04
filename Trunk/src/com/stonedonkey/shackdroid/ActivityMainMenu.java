package com.stonedonkey.shackdroid;

import java.util.ArrayList;
import java.util.Random;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class ActivityMainMenu extends ListActivity  {
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	     super.onCreate(savedInstanceState);
		
	     ArrayList<ShackMenuItem> menu = new ArrayList<ShackMenuItem>();
	     
	     menu.add(new ShackMenuItem("Latest Chatty","It gets you chicks, and diseases.",R.drawable.menu2_latestchatty));
	     menu.add(new ShackMenuItem("Shack RSS", "The \"Mos Eisley\" of chatties.",R.drawable.menu2_rss));
	     menu.add(new ShackMenuItem("Shack Search","For all your vanity needs.",R.drawable.menu2_search));
	     menu.add(new ShackMenuItem("Shack Marks","Your mobile tranny porn Stash.",R.drawable.menu2_shackmarks2));
	     menu.add(new ShackMenuItem("Shack Messages","Stuff too shocking for even the Shack.",R.drawable.menu2_shackmessages2));
	     menu.add(new ShackMenuItem("Settings","Hay guys, am I doing this right?",R.drawable.menu2_settings));
	     menu.add(new ShackMenuItem("Version Check","stonedonkey finally did something new!?!",R.drawable.menu2_vercheck));
	     menu.add(new ShackMenuItem("Camera","Pics + Divx of STFU",R.drawable.menu2_settings));
	     
	     AdapterMainMenu mm = new AdapterMainMenu(this,R.layout.mainmenu_row, menu);
     	     

	     
	     String[] urls = getResources().getStringArray(R.array.titles);
	     int titles =urls.length;
	     Random r = new Random();
	     setTitle("ShackDroid - " + urls[r.nextInt(titles)]);

	     
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
				intent.setClass(this,ActivitySearch.class);
				break;
			
			}
			case 3: 
			{
				intent.setClass(this,ActivityShackMarks.class);
				break;
			}
			case 4:
			{
				intent.setClass(this,ActivityMessages.class);
				
				break;
			}
			case 5:
			{
				intent.setClass(this,ActivityPreferences.class);
				break;
			}
			case 6:
			{
				// TODO: Rework new version check this is not very useful
				String message = "Unable to complete version check, please try again later.";

				int result = HandlerExtendedSites.VersionCheck(this);

				if (result == 1)
					message = "NEW SHACKDROID VERSION!\n http://www.stonedonkey.com/ShackDroid/Latest";
				else if (result == 0)
					message = "ShackDroid is up to date.";

				new AlertDialog.Builder(this).setTitle("Version Check").setPositiveButton("OK", null).setMessage(message).show();

				break;
			}
			case 7:
			{
				intent.setClass(this,ActivityCamera.class);
				break;
			}
			
		}	
		
		if (position != 6) {
			startActivity(intent);
			
		}
	}
}
