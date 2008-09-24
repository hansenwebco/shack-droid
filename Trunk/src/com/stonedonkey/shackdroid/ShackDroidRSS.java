package com.stonedonkey.shackdroid;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.ListActivity;
import android.os.Bundle;

public class ShackDroidRSS extends ListActivity implements Runnable {

	private ArrayList<ShackRSS> rssItems = null; 
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	
		
		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//String feedURL = prefs.getString("shackFeedURL", "http://shackchatty.com");
		URL url;
		
		setContentView(R.layout.rss);
		
		
		try {
			url = new URL("http://feed.shacknews.com/shackfeed.xml");
		
			// Get a SAXParser from the SAXPArserFactory. 
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			// Get the XMLReader of the SAXParser we created. 
			XMLReader xr = sp.getXMLReader();
			
			// Create a new ContentHandler and apply it to the XML-Reader 
			RSSFeedSaxHandler saxHandler = new RSSFeedSaxHandler(this);
			xr.setContentHandler(saxHandler);

			// Parse the xml-data from our URL. 
			xr.parse(new InputSource(url.openStream()));
	
			// get the RSS items
			rssItems = saxHandler.getRssItems();
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	
		// this is where we bind our fancy ArrayList of posts
		RSSViewAdapter tva = new RSSViewAdapter(this, rssItems,R.layout.rss_row);
		setListAdapter(tva);

		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	
}