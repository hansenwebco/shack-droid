package com.stonedonkey.shackdroid;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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
		menu.add(new ShackMenuItem("Shack Messages","Stuff too shocking for even the Shack.",R.drawable.menu2_shackmessages2));
		menu.add(new ShackMenuItem("Shack LOLs","You are not as popular as these people.",R.drawable.menu2_lol2));//
		//menu.add(new ShackMenuItem("Shack Marks","Your mobile tranny porn Stash.",R.drawable.menu2_shackmarks2));
		menu.add(new ShackMenuItem("Settings","Hay guys, am I doing this right?",R.drawable.menu2_settings));
		menu.add(new ShackMenuItem("Version Check","stonedonkey finally did something new!?!",R.drawable.menu2_vercheck));
		menu.add(new ShackMenuItem("What's New","How we most recently broke this thing.",R.drawable.menu2_cone));
		menu.add(new ShackMenuItem("Stats","Keeping score, it's how you know you're better.",R.drawable.menu2_stats));

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

		CheckForUpdate(false);

	}

	private void CheckForUpdate(boolean force) {


		// have we seen this update?

		try {



			String vc = null;
			vc = getString(R.string.version_id);



			SharedPreferences settings=getPreferences(0);

			// NOTE: debugging resets value
			//SharedPreferences.Editor editor = settings.edit();
			//editor.putBoolean("hasSeenUpdatedVersion" + vc, false);
			//editor.commit(); 

			boolean hasSeenUpdatedVersion = settings.getBoolean("hasSeenUpdatedVersion" + vc, false);

			if (!hasSeenUpdatedVersion || force)
			{
				final String result = HandlerExtendedSites.WhatsNew(getResources().getString(R.string.version_id),this);

				new AlertDialog.Builder(this)
				.setTitle("What's New " + vc)
				.setPositiveButton("OK",null)
				.setMessage(result).show();

				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean("hasSeenUpdatedVersion" + vc, true);
				editor.commit(); 

			}

		}
		catch (Exception ex)
		{
			// do nothing
		}


	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Intent intent = new Intent();;
		switch (position) {
		case 0: // LatestChatty
		{
			ShackDroidStats.AddViewedChatty(this);
			intent.setClass(this, ActivityTopicView.class);
			break;
		}
		case 1: 
		{
			ShackDroidStats.AddViewedRssFeed(this);
			intent.setClass(this,ActivityRSS.class);
			break;
		}
		case 2: 
		{
			intent.setClass(this,ActivitySearchTabs.class);
			break;

		}
		case 4:  // shackLOL
		{
			ShackDroidStats.AddViewedShackLOLs(this);
			intent.setClass(this,ActivityLOLTabs.class);
			break;
		}
		case 3:
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			boolean allowSMs = prefs.getBoolean("allowShackMessages", false);

			if (allowSMs)
			{
				ShackDroidStats.AddViewShackMessage(this);
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
			newVersionCheck(true);


			break;
		}
		case 7:
		{
			CheckForUpdate(true);
			break;
			//intent.setClass(this,ActivitySearchTabs.class);
			//break;
		}
		case 8: {
			ShackDroidStats.AddViewedStats(this);
			intent.setClass(this,ActivityStats.class);
			break;
		}

		}	

		if (position != 6 &&  position !=7) {
			startActivity(intent);

		}
	}

	private void newVersionCheck(boolean showNoUpdate) {

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
		else if (showNoUpdate)
		{
			new AlertDialog.Builder(this)
			.setTitle("Version Check")
			.setPositiveButton("OK",null)
			.setMessage(message).show();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		//CheckForNewShackDroid(this);
		new CheckForNewShackDroidAsyncTask(this).execute();
	}	
}
class CheckForNewShackDroidAsyncTask extends AsyncTask<Void,Void,Integer>{

	private ActivityMainMenu context;
	public CheckForNewShackDroidAsyncTask(ActivityMainMenu context)
	{
		this.context = context; 

	}
	@Override
	protected Integer doInBackground(Void... arg0) {

		// LOL at MONSTER FUNCTIONSSLKJDSJDLKJ!!KLJ OMG Refactor much!
		
		boolean checkForUpdate = false;

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean allowCheck = prefs.getBoolean("allowCheckForNewVersion", true);
		
		if (!allowCheck)
			return null;
		
		Calendar currentDate = Calendar.getInstance();
		Calendar lastMessageDate;
		try {
			if (context.getFileStreamPath("versioncheck.cache").exists()) {

				FileInputStream fileIn = context.openFileInput("versioncheck.cache");
				ObjectInputStream in = new ObjectInputStream(fileIn);
				lastMessageDate =  (Calendar) in.readObject();
				in.close();
				fileIn.close();

				// 8640000 = 1 day
				if (currentDate.getTimeInMillis() - lastMessageDate.getTimeInMillis() > 14400000 )
					checkForUpdate = true;
			}
			else 
			{ 	checkForUpdate = true;
			}
		}
		catch (Exception ex) { 
			Log.e("ShackDroid", "Error Loading Last Update Date: " + ex.getMessage());
			return null;
		}
		if (checkForUpdate)
		{
			final String result = HandlerExtendedSites.VersionCheck(context);
			if (result != null && result != "*fail*") 
			{
				// if we find a new version bake some toast

				String ns = Context.NOTIFICATION_SERVICE;
				NotificationManager nm = (NotificationManager)context.getSystemService(ns);

				int icon = R.drawable.shack_logo;
				CharSequence tickerText = "ShackDroid Update Available";

				Notification note = new Notification(icon,tickerText,0);

				CharSequence contentTitle = "ShackDroid Update Available";
				CharSequence contentText = "Touch here to download!";

				Intent notificationIntent = new Intent("android.intent.action.VIEW", Uri.parse(result));
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
				note.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

				note.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
				note.ledARGB = 0xFF800080;
				note.ledOnMS = 100;
				note.ledOffMS = 100;
				//note.defaults = Notification.DEFAULT_SOUND;
				note.sound = Uri.parse("android.resource://com.stonedonkey.shackdroid/" + R.raw.alert1);
				nm.notify(1,note);


				// update our cache file
				try {
					FileOutputStream fos = context.openFileOutput("versioncheck.cache",Context.MODE_PRIVATE);
					ObjectOutputStream os = new ObjectOutputStream(fos);
					os.writeObject(currentDate);
					os.close();
					fos.close();
				}
				catch (Exception ex)
				{
					Log.e("ShackDroid", "Error Saving Last New Version Check: " + ex.getMessage());
				}				
			}

		}
		return null;
	}
}

