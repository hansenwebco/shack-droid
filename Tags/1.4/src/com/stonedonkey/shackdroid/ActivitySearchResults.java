package com.stonedonkey.shackdroid;

import java.net.URL;
import java.net.URLEncoder;
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

public class ActivitySearchResults extends ListActivity implements Runnable {
	private ProgressDialog pd;
	private ArrayList<ShackSearch> searchResults;
	private String searchTerm = "";
	private String author = "";
	private String parentAuthor = "";
	private String totalPages = "1";
	private String totalResults ="0";
	private int currentPage = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle extras = this.getIntent().getExtras();
		searchTerm = extras.getString("searchTerm");
		author = extras.getString("author");
		parentAuthor = extras.getString("parentAuthor");
		
		fillSaxData();
		

	}
	private void fillSaxData() {
		// show a progress dialog
		pd = ProgressDialog.show(this, null, "Loading search results...", true, true); 
			
		// use the class run() method to do work
		Thread thread = new Thread(this); 
		thread.start();
		
		
		
	}
	@Override
	public void run() {
		try {
			
			URL url = new URL("http://shackapi.stonedonkey.com/search/?SearchTerm=" + URLEncoder.encode(searchTerm,"UTF-8") + "&Author="+ author + "&ParentAuthor=" + parentAuthor + "&page=" + currentPage); 
		
			// Get a SAXParser from the SAXPArserFactory. 
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			// Get the XMLReader of the SAXParser we created. 
			XMLReader xr = sp.getXMLReader();
			
			// Create a new ContentHandler and apply it to the XML-Reader 
			SaxHandlerSearchResults saxHandler = new SaxHandlerSearchResults();
			xr.setContentHandler(saxHandler);

			// Parse the xml-data from our URL. 
			xr.parse(new InputSource(url.openStream()));
	
			// get the RSS items
			searchResults = saxHandler.getSearchResults();
			totalPages = saxHandler.getTotalPages();
			totalResults = saxHandler.getTotalResults();
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		
		setTitle("Search Results - " + currentPage + " of " + this.totalPages + " - " + this.totalResults + " results.");
		
		// this is where we bind our fancy ArrayList of posts
		AdapterSearchResults tva = new AdapterSearchResults(this, searchResults,R.layout.searchresults_row);
		setListAdapter(tva);
	}
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		ShowShackPost(position);
	}
	private void ShowShackPost(long position) {

		ShackSearch search = searchResults.get((int) position);
		String postID = search.getId();
		String storyID = search.getStoryID();
		
		Intent intent = new Intent();
		intent.setClass(this, ActivityThreadedView.class);
		intent.putExtra("postID", postID); // the value must be a string
		intent.putExtra("storyID", storyID);
		startActivity(intent);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(1, 0, 1, "Prev Page").setIcon(R.drawable.menu_back);
		menu.add(1, 1, 2, "Next Page").setIcon(R.drawable.menu_forward);
		menu.add(1, 2, 3, "Home").setIcon(R.drawable.menu_home);
		menu.add(1, 3, 4, "Back").setIcon(R.drawable.menu_search);
		
		menu.findItem(0).setEnabled(false);
		
		return true;
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		// Context context = this;
		Intent intent;
		switch (item.getItemId()) {
		case 0: // prev page
			SetPaging(-1);
			fillSaxData();
			return true;
		case 1: // next page
			SetPaging(1);
			fillSaxData();
			return true;
		case 2: // back to main chatty
			intent = new Intent();
			intent.setClass(this, ActivityTopicView.class);
			startActivity(intent);
		case 3: // home
			finish();
			return true;
		}
		return false;
	}
	private void SetPaging(Integer increment) {

		// set current page
		if ((currentPage + increment >= 1) && (currentPage + increment <= Integer.parseInt(totalPages)))
			currentPage = currentPage + increment;
	
		
	}
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		
		// don't allow previous if on page 1
		if (currentPage == 1)
			menu.findItem(0).setEnabled(false);
		else
			menu.findItem(0).setEnabled(true);
		
		// don't allow paging past last item.
		if (currentPage == Integer.parseInt(totalPages))
			menu.findItem(1).setEnabled(false);
		else
			menu.findItem(1).setEnabled(true);
			
		
		return super.onMenuOpened(featureId, menu);
	}

}
