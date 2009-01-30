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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class ActivityMessages extends ListActivity implements Runnable {

	private ProgressDialog pd;
	private ArrayList<ShackMessage> messages;
	private String box= "Inbox";
	private int totalPages = 1;
	private String totalResults = "0";
	private int currentPage = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle("Shack Messages - " + box);
		
		setContentView(R.layout.messages);
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
		
			URL url = new URL("http://shackapi.stonedonkey.com/messages/?username=" + login + "&password=" + password + "&box=" + box.toLowerCase() + "&page=" + currentPage);
			

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
			totalPages = Integer.parseInt(saxHandler.getTotalPages());
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

		setTitle("Shack Messages - " + box + " - " + currentPage + " of " + this.totalPages + " (" + this.totalResults + ")");

		// this is where we bind our fancy ArrayList of posts
		if (messages != null) {
		AdapterMessages tva = new AdapterMessages(this, messages,R.layout.messages_row);
		setListAdapter(tva);
		}
		else
		{
			new AlertDialog.Builder(this).setTitle("Error")
			.setPositiveButton("OK", null).setMessage(
					"There was an error retrieving your messages, check your login information or try again later.")
			.show();
		}
	}
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		
		ShackMessage msg = messages.get(position);
		
		Intent intent = new Intent();
		intent.setClass(this, ActivityViewMessage.class);
		intent.putExtra("msg", msg);
		startActivity(intent);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		
		menu.add(1, 0 ,0,"Prev").setIcon(R.drawable.menu_back);
		menu.add(1, 1 ,1,"Next").setIcon(R.drawable.menu_forward);
		menu.add(1, 2, 6, "Home").setIcon(R.drawable.menu_home);
		menu.add(2, 3, 3, "Send Msg").setIcon(R.drawable.menu_message);
		menu.add(2, 4, 4, "Refresh").setIcon(R.drawable.menu_reload);
		menu.add(2, 6, 2, "Folder").setIcon(R.drawable.menu_folder);
		
		menu.findItem(0).setEnabled(false);

		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Context context = this;
		Intent intent;
		switch (item.getItemId()) {
		case 0: // Launch post form
			this.SetPaging(-1);
			fillSaxData();
			return true;
		case 1: // refresh
			this.SetPaging(1);
			fillSaxData();
			return true;
		case 2: // show settings dialog
			intent = new Intent();
			intent.setClass(this, ActivityTopicView.class);
			startActivity(intent);
			return true;
		case 3:
			intent = new Intent();
			intent.setClass(this, ActivityPostMessage.class);
			startActivity(intent);
			return true;
		case 4:
			fillSaxData();
			return true;
		case 5: 
			intent = new Intent();
			intent.setClass(this, ActivityPreferences.class);
			startActivity(intent);
			return true;
		case 6:
			showDialog(1);
			return true;			
		}
		return false;
	}
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {

		if (this.currentPage <= 1) // previous enabled
			menu.findItem(0).setEnabled(false); // previous
		 else 
			menu.findItem(0).setEnabled(true); // previous


		if (this.currentPage >= this.totalPages) // next enabled
			menu.findItem(1).setEnabled(false);
		else
			menu.findItem(1).setEnabled(true);

		return super.onMenuOpened(featureId, menu);
	}	
	@Override
    protected Dialog onCreateDialog(int id) {
        switch (id) 
        {
        case 1:
        	
        	
        	String[] boxes = new String[3];
        	boxes[0] = "Inbox";
        	boxes[1] = "Outbox";
        	boxes[2] = "Archived";
        	
        	return new AlertDialog.Builder(ActivityMessages.this)
            .setTitle("Choose Feed")
            .setSingleChoiceItems(boxes, 0, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	switch (whichButton){
                	case 1:
                		box = "Outbox";
                		break;
                	case 2: 
                		box = "Archive";
                		break;
                	default:
                		box = "Inbox";
                	}
                }
            }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                		currentPage =1;
                      	fillSaxData(); // reload
                  
                }
            }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    /* User clicked No so do some stuff */
                }
            }).create();
        }
		return null;
    }	
	private void SetPaging(Integer increment) {

		// set current page
		if ((currentPage + increment >= 1)
				&& (currentPage + increment <= totalPages))
			currentPage = currentPage + increment;
	}
}
