package com.stonedonkey.shackdroid;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class ActivityTopicView extends ListActivity implements Runnable {

	private ArrayList<ShackPost> posts;
	private String storyID = null;
	private String storyName;
	private String errorText = "";
	private Integer currentPage = 1;
	private Integer storyPages = 1;
	private String loadStoryID = null;
	private Boolean threadLoaded = true;
	private Hashtable<String, String> postCounts = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Helper.SetWindowState(getWindow(),this);
		
		setContentView(R.layout.topics);

		Bundle extras = this.getIntent().getExtras();
		if (extras != null)
			loadStoryID = extras.getString("StoryID");
		
		if (savedInstanceState == null) {
			try {
				// setRequestedOrientation(ActivityInfo.
				// SCREEN_ORIENTATION_LANDSCAPE);
				fillDataSAX();
			} catch (Exception e) {
				new AlertDialog.Builder(this).setTitle("Error")
						.setPositiveButton("OK", null).setMessage(
								"There was an error connecting to the API.")
						.show();
				try {
					dismissDialog(1);
				} catch (Exception ex) {
					// dialog could not be killed for some reason
				}
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

		try {
			dismissDialog(1);
		} catch (Exception ex) {
			// dialog could not be killed for some reason
		}

		// Dear Android devs... please make this more of a pain in the ass for
		// orientation changes.. KAHNNN!!!!
		savedInstanceState.putSerializable("posts", posts);
		savedInstanceState.putString("storyName", storyName);
		savedInstanceState.putInt("currentPage", currentPage);
		savedInstanceState.putInt("storyPages", storyPages);
		savedInstanceState.putString("storyID", storyID);
		savedInstanceState.putBoolean("threadLoaded", threadLoaded);

	}
	@SuppressWarnings("unchecked")
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		storyName = savedInstanceState.getString("storyName");
		currentPage = savedInstanceState.getInt("currentPage");
		storyPages = savedInstanceState.getInt("storyPages");
		storyID = savedInstanceState.getString("storyID");
		posts = (ArrayList<ShackPost>) savedInstanceState.getSerializable("posts");
		threadLoaded = savedInstanceState.getBoolean("threadLoaded");
		
		// If we change orientation in the middle of a thread loading we end up with 
		// the last loaded posts, this forces a new pull on orientation change.
		if (threadLoaded == false)
			fillDataSAX();  

		threadLoaded = true;
		savedInstanceState.clear(); // we'll resave it if we do something again
		
			ShowData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(1, 5, 1, "Prev Page").setIcon(R.drawable.menu_back);
		menu.add(1, 4, 2, "Next Page").setIcon(R.drawable.menu_forward);
		menu.add(2, 1, 3, "Refresh").setIcon(R.drawable.menu_reload);
		menu.add(2, 0, 4, "New Post").setIcon(R.drawable.menu_addpost);
		menu.add(1, 3, 5, "First Page").setIcon(R.drawable.menu_top);
		menu.add(2, 2, 6, "Menu").setIcon(R.drawable.menu_home);
		menu.findItem(5).setEnabled(false);

		return true;
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {

		if (this.currentPage <= 1) // previous enabled
		{
			menu.findItem(3).setEnabled(false); // home
			menu.findItem(5).setEnabled(false); // previous
		} else {
			menu.findItem(3).setEnabled(true); // home
			menu.findItem(5).setEnabled(true); // previous
		}
		if (this.currentPage >= this.storyPages) // next enabled
			menu.findItem(4).setEnabled(false);
		else
			menu.findItem(4).setEnabled(true);

		return super.onMenuOpened(featureId, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Context context = this;
		Intent intent;
		switch (item.getItemId()) {
		case 0: // Launch post form
			intent = new Intent();
			intent.setClass(this, ActivityPost.class);
			intent.putExtra("storyID", storyID);
			intent.putExtra("postID", "");
			startActivity(intent);
			return true;
		case 1: // refresh
			fillDataSAX();
			return true;
		case 2: // show settings dialog
			intent = new Intent();
			intent.setClass(this, ActivityMainMenu.class);
			startActivity(intent);
			return true;
		case 3: // home
			currentPage = 1;
			fillDataSAX();
			return true;
		case 4: // forward a page
			SetPaging(1);
			fillDataSAX();
			return true;
		case 5: // previous page
			SetPaging(-1);
			fillDataSAX();
			return true;
		case 6:
			intent = new Intent();
			intent.setClass(this, ActivityShackMarks.class);
			startActivity(intent);
			return true;
		case 7:
			intent = new Intent();
			intent.setClass(this, ActivityRSS.class);
			startActivity(intent);
			return true;
		case 8:
//			String message = "Unable to complete version check, please try again later.";
//
//			int result = HandlerExtendedSites.VersionCheck(this);
//
//			if (result == 1)
//				message = "NEW SHACKDROID VERSION!\n http://www.stonedonkey.com/ShackDroid/Latest";
//			else if (result == 0)
//				message = "ShackDroid is up to date.";
//
//			new AlertDialog.Builder(this).setTitle("Version Check")
//					.setPositiveButton("OK", null).setMessage(message).show();
//			return true;
		case 9:
			intent = new Intent();
			intent.setClass(this,ActivitySearch.class);
			startActivity(intent);
			return true;
		case 10:  // show shack messages if they enabled them
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			boolean allowSMs = prefs.getBoolean("allowShackMessages", false);
			
			if (allowSMs)
			{
			intent = new Intent();
			intent.setClass(this,ActivityMessages.class);
			startActivity(intent);
			return true;
			}
			else
			{
				new AlertDialog.Builder(this).setTitle("Information")
				.setPositiveButton("OK", null).setMessage(
						"Shack Messages posts your credentials to the API " +
						"instead of directly ShackNews.\n\n If you agree with this " +
						"you can enable this feature under \"Settings\"." )
				.show();
				
			return true;
			}
				
		}
	
		return false;
	}

	private void SetPaging(Integer increment) {

		// set current page
		if ((currentPage + increment >= 1)
				&& (currentPage + increment <= storyPages))
			currentPage = currentPage + increment;
	}

	private void fillDataSAX() {

		showDialog(1);

		// use the class run() method to do work
		Thread thread = new Thread(this);
		thread.start();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 1: {
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage("loading, please wait...");
			dialog.setTitle(null);
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			return dialog;
			}
		}
		return null;
	}
	public void run() {
		 
		threadLoaded = false;
		try {

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			String feedURL = prefs.getString("shackFeedURL",
					getString(R.string.default_api));
			URL url;

			if (loadStoryID != null) {
				if (currentPage > 1)
					url = new URL(feedURL + "/" + loadStoryID + "."
							+ this.currentPage.toString() + ".xml");
				else
					url = new URL(feedURL + "/" + loadStoryID + ".xml");
			} else {
				if (currentPage > 1)
					url = new URL(feedURL + "/" + this.storyID + "."
							+ this.currentPage.toString() + ".xml");
				else
					url = new URL(feedURL + "/index.xml");
			}

			// Get a SAXParser from the SAXPArserFactory.
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			// Get the XMLReader of the SAXParser we created.
			XMLReader xr = sp.getXMLReader();
			// Create a new ContentHandler and apply it to the XML-Reader
			SaxHandlerTopicView saxHandler = new SaxHandlerTopicView(this,"topic");
			xr.setContentHandler(saxHandler);

			// Parse the xml-data from our URL.
			xr.parse(new InputSource(url.openStream()));

			// Our ExampleHandler now provides the parsed data to us.
			posts = saxHandler.GetParsedPosts();
			storyID = saxHandler.getStoryID();
			storyName = saxHandler.getStoryTitle();
			storyPages = saxHandler.getStoryPageCount();

			if (storyPages == 0) // XML returns a 0 for stories with only one
									// page
				storyPages = 1;

		} catch (Exception ex) {
			ex.printStackTrace(System.out);
			errorText = "An error occurred connecting to API.";
		}
		
		progressBarHandler.sendEmptyMessage(0);
	}

	private Handler progressBarHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// we implement a handler because most UI items
			// won't update within a thread
			try {
				dismissDialog(1);
			} catch (Exception ex) {
			}
			ShowData();
		}
	};

	private void ShowData()  {

		if (posts != null) {
			// storyName is set during FillData above
			setTitle("ShackDroid - " + storyName + " - "
					+ currentPage.toString() + " of "
					+ this.storyPages.toString());

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String login = prefs.getString("shackLogin", "");
			int fontSize = Integer.parseInt(prefs.getString("fontSize", "12"));

			try {
				postCounts = GetPostCache();
			} catch (Exception ex) {

			}


			// TODO: Passing this as a new HashTable seems very ugly and a waste of memory
			// Unfortunately I can't find a a way to get the Adapter to update before I call
			// the UpdatePostCache below.. that update occurs before the ListAdapter is set
			// apparently.  I can't find anything else to put the Update , so for now we'll
			// create a new Hashtable.. ick.
			
			//chazums maybe tva.notifyDataSetChanged() ?  
			Hashtable<String,String> tempHash = null;
			if (postCounts != null)
				tempHash = new Hashtable<String,String>(postCounts);

			AdapterTopicView tva = new AdapterTopicView(getApplicationContext(),R.layout.topic_row, posts, login, fontSize,tempHash);
			
			setListAdapter(tva);
			
			// update the reply counts for the listing of topics
			try {
				UpdatePostCache();
			} catch (Exception e) {

			}

		} else {
			if (errorText.length() > 0) {
				new AlertDialog.Builder(this).setTitle("Error")
				.setPositiveButton("OK", null).setMessage(errorText)
				.show();
			}
		}

		threadLoaded = true;
	}

	@SuppressWarnings("unchecked")
	public Hashtable<String, String> GetPostCache() throws StreamCorruptedException, IOException
	{
		if (getFileStreamPath("posts.cache").exists()) {

			Hashtable<String, String> postCounts = null;
			
			// if the day is different we delete and recreate the file
			long lastMod = getFileStreamPath("posts.cache").lastModified();
			Date lastDateMod = new Date(lastMod); 
			Date currentDate = new Date();
			if (lastDateMod.getDay() != currentDate.getDay())
			{
				getFileStreamPath("posts.cache").delete();
				return null;
			} 
						
			FileInputStream fileIn = openFileInput("posts.cache");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			try {
				postCounts = (Hashtable<String, String>)in.readObject();
			} catch (ClassNotFoundException e) {
				return null; // fail boat?
			}
			in.close(); 
			fileIn.close();
				
			
			return postCounts;
		}
		else 
			return null;
	}
	 
	public void UpdatePostCache() throws StreamCorruptedException, IOException
	{
		if (postCounts == null)
			postCounts = new Hashtable<String, String>();
		
		for(int x= 0; x < posts.size();x++)
			postCounts.put(posts.get(x).getPostID(), posts.get(x).getReplyCount());
		
		FileOutputStream fos = openFileOutput("posts.cache",MODE_PRIVATE);
		ObjectOutputStream os = new ObjectOutputStream(fos);
		os.writeObject(postCounts);
		os.close();
		fos.close();
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		String cat = posts.get(position).getPostCategory();
		
		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), ActivityThreadedView.class);
		intent.putExtra("postID", Long.toString(id)); // the value must be a string
		intent.putExtra("storyID", storyID);
		if (cat.equalsIgnoreCase("nws"))
			intent.putExtra("isNWS", true);
		else
			intent.putExtra("isNWS",false);
		
		startActivity(intent);
	}
}
