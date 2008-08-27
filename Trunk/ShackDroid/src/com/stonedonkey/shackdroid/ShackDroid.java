package com.stonedonkey.shackdroid;

import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

/**
 * @author stonedonkey
 * 
 */
public class ShackDroid extends ListActivity implements Runnable {

	private ArrayList<ShackPost> posts;
	private ProgressDialog pd;
	private String storyID = null;
	private String storyName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.topics);

		
		try {
			fillDataSAX();
		} catch (Exception e) {
			// // TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

	// Override the onCreateOptionsMenu to provide our own custome
	// buttons.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, 0, 0, "New Post");
		menu.add(0, 1, 0, "Refresh");
		menu.add(0, 2, 0, "Settings");
		menu.add(0, 3, 0, "Next Page");
		menu.add(0, 4, 0, "Prev Page");
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//Context context = this;
		Intent intent;
		switch (item.getItemId()) {
		case 0:

			// Launch post form
			intent = new Intent();
			intent.setClass(this, ShackDroidPost.class);
			intent.putExtra("storyID", storyID);
			intent.putExtra("postID","");
			startActivity(intent);			

			return true;
		case 1:
			fillDataSAX();
			return true;
		case 2:
			// show settings dialog
			intent = new Intent();
			intent.setClass(this, ShackDroidPreferences.class);
			startActivity(intent);
			return true;
		}
		return false;
	}

	private void fillDataSAX() {

		// show a progress dialog
		pd = ProgressDialog.show(this, null, "Loading chatty...", true,	false); 
		
		// use the class run() method to do work
		Thread thread = new Thread(this); 
		thread.start();
	}
	
	public void run() {
		try {
			//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			//String feedURL = prefs.getString("shackFeedURL", "");
			
			URL url = new URL("http://shackchatty.com" + "/index.xml");

			/* Get a SAXParser from the SAXPArserFactory. */
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xr = sp.getXMLReader();
			/* Create a new ContentHandler and apply it to the XML-Reader */
			TopicViewSaxHandler saxHandler = new TopicViewSaxHandler();
			xr.setContentHandler(saxHandler);

			/* Parse the xml-data from our URL. */
			xr.parse(new InputSource(url.openStream()));
			/* Parsing has finished. */

			/* Our ExampleHandler now provides the parsed data to us. */
			posts = saxHandler.GetParsedPosts();
			storyID = saxHandler.getStoryID();
			storyName= saxHandler.getStoryTitle();

		} catch (Exception ex) {
			ex.printStackTrace(System.out);
		}
		progressBarHandler.sendEmptyMessage(0);
	}
	
	private Handler progressBarHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// we implement a handler because most UI items 
			// won't update within a thread
			pd.dismiss();
			ShowData();
		}
	};
	
	private void ShowData() {
		
		// storyName is set during FillData above
		setTitle("ShackDroid - " + storyName );
		
		// this is where we bind our fancy ArrayList of posts
		TopicViewAdapter tva = new TopicViewAdapter(this, R.layout.topic_row,
				posts);

		setListAdapter(tva);
	}

	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Intent intent = new Intent();
		intent.setClass(this, ShackDroidThread.class);
		intent.putExtra("postID", Long.toString(id)); // the value must be a string
		intent.putExtra("storyID", storyID);
		startActivity(intent);
	}


}
