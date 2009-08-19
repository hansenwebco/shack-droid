package com.stonedonkey.shackdroid;

import java.net.URL;
import java.util.ArrayList;

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

/**
 * @author stonedonkey
 * 
 */
public class ActivityTopicView extends ListActivity implements Runnable {

	private ArrayList<ShackPost> posts;
	private String storyID = null;
	private String storyName;
	private String errorText = "";
	private Integer currentPage = 1;
	private Integer storyPages = 1;
	private String loadStoryID = null;
	private Boolean threadLoaded = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

			// pd.dismiss();
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

	// TODO: How do I make typesafe check.. how is typecheck formed?
	@SuppressWarnings("unchecked")
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		storyName = savedInstanceState.getString("storyName");
		currentPage = savedInstanceState.getInt("currentPage");
		storyPages = savedInstanceState.getInt("storyPages");
		storyID = savedInstanceState.getString("storyID");
		posts = (ArrayList<ShackPost>) savedInstanceState.getSerializable("posts");
		threadLoaded = savedInstanceState.getBoolean("threadLoaded");
		
		// TODO : If we change orientation in the middle of a thread loading we end up with 
		//        the last loaded posts, this forces a new pull on orientation change.
		if (threadLoaded == false)
			fillDataSAX();  

		threadLoaded = true;
		savedInstanceState.clear(); // we'll resave it if we do something again
		ShowData();
	}

	// Override the onCreateOptionsMenu to provide our own custom
	// buttons.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(1, 5, 1, "Prev Page").setIcon(R.drawable.menu_back);
		menu.add(1, 4, 2, "Next Page").setIcon(R.drawable.menu_forward);
		menu.add(1, 3, 3, "Home").setIcon(R.drawable.menu_message);
		menu.add(2, 0, 4, "New Post").setIcon(R.drawable.menu_addpost);
		menu.add(2, 1, 5, "Refresh").setIcon(R.drawable.menu_reload);
		menu.add(2, 2, 6, "Menu").setIcon(R.drawable.menu_home);
		//menu.add(2, 6, 7, "ShackMarks").setIcon(R.drawable.menu_addpost);
		//menu.add(2, 7, 8, "Shack RSS").setIcon(R.drawable.menu_reload);
		//menu.add(3, 8, 9, "Check Version");
		//menu.add(3, 9,10,"Shack Search");
		//menu.add(3, 10,11,"Shack Messages");

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
			intent.setClass(this, ActivityPreferences.class);
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
			String message = "Unable to complete version check, please try again later.";

			int result = HandlerExtendedSites.VersionCheck(this);

			if (result == 1)
				message = "NEW SHACKDROID VERSION!\n http://www.stonedonkey.com/ShackDroid/Latest";
			else if (result == 0)
				message = "ShackDroid is up to date.";

			new AlertDialog.Builder(this).setTitle("Version Check")
					.setPositiveButton("OK", null).setMessage(message).show();
			return true;
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

		// pd = ProgressDialog.show(this, null, "loading, please wait...",
		// true,false);
		// pd.setIcon(R.drawable.shack_logo);
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
			SaxHandlerTopicView saxHandler = new SaxHandlerTopicView(this);
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

				// pd.dismiss();
				dismissDialog(1);
			} catch (Exception ex) {
				// TODO : .dismiss is failing on the initial startup, something
				// to do with the
				// windows manager... this is a hacky fix.. :(
				// String fail = ex.getMessage();
				// fillDataSAX();
			}

			ShowData();
		}
	};

	private void ShowData() {

		if (posts != null) {
			// storyName is set during FillData above
			setTitle("ShackDroid - " + storyName + " - "
					+ currentPage.toString() + " of "
					+ this.storyPages.toString());

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			String login = prefs.getString("shackLogin", "");
			int fontSize = Integer.parseInt(prefs.getString("fontSize", "12"));

			// this is where we bind our fancy ArrayList of posts
			AdapterTopicView tva = new AdapterTopicView(this,
					R.layout.topic_row, posts, login, fontSize);
			setListAdapter(tva);

		} else {
			if (errorText.length() > 0) {
				new AlertDialog.Builder(this).setTitle("Error")
						.setPositiveButton("OK", null).setMessage(errorText)
						.show();
			}
		}
		threadLoaded = true;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Intent intent = new Intent();
		intent.setClass(this, ActivityThreadedView.class);
		intent.putExtra("postID", Long.toString(id)); // the value must be a
														// string
		intent.putExtra("storyID", storyID);
		startActivity(intent);
	}

}
