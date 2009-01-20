package com.stonedonkey.shackdroid;

import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class ActivitySearchResults extends ListActivity implements Runnable {
	private ProgressDialog pd;
	private ArrayList<ShackSearch> searchResults;
	private String searchTerm;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle extras = this.getIntent().getExtras();
		searchTerm = extras.getString("searchTerm");
		//String author = extras.getString("author");
		//String parentAuthor = extras.getString("parentAuthor");
		
		fillSaxData();
		

	}
	private void fillSaxData() {
		// show a progress dialog
		pd = ProgressDialog.show(this, null, "Searching the Shack...", true, true); 
			
		// use the class run() method to do work
		Thread thread = new Thread(this); 
		thread.start();
	}
	@Override
	public void run() {
		try {
			
			URL url = new URL("http://shackapi.stonedonkey.com/search/?SearchTerm=" + searchTerm);
		
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
		// this is where we bind our fancy ArrayList of posts
		AdapterSearchResults tva = new AdapterSearchResults(this, searchResults,R.layout.searchresults_row);
		setListAdapter(tva);
	}

}
