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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ActivityRSS extends ListActivity implements Runnable {

	private ArrayList<ShackRSS> rssItems = null; 
	private ProgressDialog pd;
	private int feedID = 0;
	private String feedURL = "http://feed.shacknews.com/shackfeed.xml";
	private String feedDesc = "Front Page";

	
	URL url;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
				
		super.onCreate(savedInstanceState);
		Helper.SetWindowState(getWindow(),this);
		
		setContentView(R.layout.rss);
		fillSaxData();
	
		registerForContextMenu(getListView());
	}
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
		
		menu.setHeaderTitle("RSS Story Options");
		menu.add(0, 3, 0, "View Story");
		menu.add(0, 4, 0, "View Comments");
		menu.add(0, -1, 0, "Cancel");
		
	};			
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}



	private void fillSaxData() {
		// show a progress dialog
		pd = ProgressDialog.show(this, null, "Loading RSS feed...", true, true); 
		
		this.setTitle("ShackDroidRSS - " + feedDesc);
		
		// use the class run() method to do work
		Thread thread = new Thread(this); 
		thread.start();
	}
	@Override
	public void run() {
	
	
		try {
			url = new URL(feedURL);
		
			// Get a SAXParser from the SAXPArserFactory. 
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			// Get the XMLReader of the SAXParser we created. 
			XMLReader xr = sp.getXMLReader();
			
			// Create a new ContentHandler and apply it to the XML-Reader 
			SaxHandlerRSSFeed saxHandler = new SaxHandlerRSSFeed(this);
			xr.setContentHandler(saxHandler);

			// Parse the xml-data from our URL. 
			xr.parse(new InputSource(url.openStream()));
	
			// get the RSS items
			rssItems = saxHandler.getRssItems();
		
		} catch (Exception e) {

		}
		progressBarHandler.sendEmptyMessage(0);
		
		
	}
	private Handler progressBarHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// we implement a handler because most UI items 
			// won't update within a thread
			try {
			pd.dismiss();
			}
			catch (Exception ex)
			{
			
			}
			
			ShowData();
		}
	};
	
	private void ShowData() {
		// this is where we bind our fancy ArrayList of posts
		AdapterRSSView tva = new AdapterRSSView(this, rssItems,R.layout.rss_row);
		setListAdapter(tva);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		
		menu.add(0, 1, 1, "Home").setIcon(R.drawable.menu_home);
		menu.add(0, 2, 1, "Choose Feed").setIcon(R.drawable.menu_rss_feed);
		
		return super.onCreateOptionsMenu(menu);
		
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId())
		{	
			case 1:
				finish();
				return true;
			case 2:
				// do menu
				showDialog(1);
				return true;
		}		
		
		return false;
	}
	 @Override
	public boolean onContextItemSelected(MenuItem item) {

		    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			int itemPosition = info.position;
			Intent intent;
			

			switch (item.getItemId()) {
				case 3:
					String storyURL = rssItems.get(itemPosition).getLink();
					String storyTitle = rssItems.get(itemPosition).getTitle();
					intent = new Intent();
					intent.putExtra("URL",storyURL);
					intent.putExtra("Title",storyTitle);
					intent.setClass(this, ActivityWebView.class);
					startActivity(intent);
					return true;
					
				case 4: 
					intent = new Intent();
				
					String link = rssItems.get(itemPosition).getLink();
					String[] story = link.split("/");
					String storyID = story[story.length-1];
					intent.putExtra("StoryID",storyID );
					intent.setClass(this, ActivityTopicView.class);
					startActivity(intent);
					return true;
					
			}
			return false;
			
	}
	@Override
	    protected Dialog onCreateDialog(int id) {
	        switch (id) 
	        {
	        case 1:
	        	return new AlertDialog.Builder(ActivityRSS.this)
                .setTitle("Choose Feed")
                .setSingleChoiceItems(R.array.feeds, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    	feedID = whichButton;
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
 
                        	
                        	String[] urls = getResources().getStringArray(R.array.feedsURls);
                        	feedURL = urls[feedID];
                        	String[] feeds = getResources().getStringArray(R.array.feeds);
                        	feedDesc = feeds[feedID];
                        	fillSaxData(); // reload
                      
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked No so do some stuff */
                    }
                })
               .create(); 
	        }
			return null;
        }


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		v.showContextMenu();
	}


	
}