package com.stonedonkey.shackdroid;

import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

public class ActivityMessages extends ListActivity implements Runnable {

	private ProgressDialog pd;
	private ArrayList<ShackMessage> messages;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		fillSaxData();


	}
	private void fillSaxData() {
		// show a progress dialog
		pd = ProgressDialog.show(this, null, "Loading Shack Messages...", true, true); 

		// use the class run() method to do work
		Thread thread = new Thread(this); 
		thread.start();
	}

	@Override
	public void run() {
		try {

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String login = prefs.getString("shackLogin", "");
			String password = prefs.getString("shackPassword", "");
		
			URL url = new URL("http://shackapi.stonedonkey.com/messages/?username=" + login + "&password=" + password );
			

			// Get a SAXParser from the SAXPArserFactory. 
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			// Get the XMLReader of the SAXParser we created. 
			XMLReader xr = sp.getXMLReader();

			// Create a new ContentHandler and apply it to the XML-Reader 
			SaxHandlerMessages saxHandler = new SaxHandlerMessages();
			xr.setContentHandler(saxHandler);

			// Parse the xml-data from our URL. 
			xr.parse(new InputSource(url.openStream()));

			// get the Message items
			messages = saxHandler.getMessages();
			//totalPages = saxHandler.getTotalPages();
			//totalResults = saxHandler.getTotalResults();

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

		//setTitle("Search Results - " + currentPage + " of " + this.totalPages + " - " + this.totalResults + " results.");

		// this is where we bind our fancy ArrayList of posts
		AdapterMessages tva = new AdapterMessages(this, messages,R.layout.messages_row);
		setListAdapter(tva);
	}

}
