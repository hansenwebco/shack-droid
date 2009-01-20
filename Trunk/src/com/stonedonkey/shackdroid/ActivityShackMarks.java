package com.stonedonkey.shackdroid;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ActivityShackMarks extends ListActivity implements Runnable {

	ArrayList<ShackPost> posts;
	private long itemPosition;
	private ProgressDialog pd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notes);


		fillSaxData();
		
		// add a listener to the default view to handle holding down
		// on the screen to view or delete.. default cilck is still view.
		getListView().setOnCreateContextMenuListener(
				new OnCreateContextMenuListener() {
					@Override
					public void onCreateContextMenu(ContextMenu menu, View v,
							ContextMenuInfo menuInfo) {
						menu.setHeaderTitle("ShackMark Options");
						menu.add(0, 0, 0, "View ShackMark");
						menu.add(0, 1, 0, "Delete ShackMark");
					}
				});
	
	}
	
	private void fillSaxData()
	{
		
		// TODO: could probably move this to a helper class
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String shackLogin = prefs.getString("shackLogin", "");

		// make sure they have their username set
		if (shackLogin.length() == 0) {
			new AlertDialog.Builder(this).setTitle("Username Required")
					.setPositiveButton("OK", null).setMessage(
							"Please set your Login in settings.").show();
			return;
		}
		
		pd = ProgressDialog.show(this, null, "shackmarks loading, please wait...", true,false); 
		pd.setIcon(R.drawable.shack_logo);
		
		// use the class run() method to do work
		Thread thread = new Thread(this); 
		thread.start();
		

	
	
	}
	
	@Override
	public void run() {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String shackLogin = prefs.getString("shackLogin", "");

		// make sure they have their username set
		if (shackLogin.length() == 0) {
			new AlertDialog.Builder(this).setTitle("Username Required")
					.setPositiveButton("OK", null).setMessage(
							"Please set your Login in settings.").show();
		
			return;
		}
		
		try {
			URL url = new URL("http://socksandthecity.net/shackmarks/xml.php?user="	+ shackLogin);

			// Get a SAXParser from the SAXPArserFactory.
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			// Get the XMLReader of the SAXParser we created.
			XMLReader xr = sp.getXMLReader();

			// Create a new ContentHandler and apply it to the XML-Reader
			SaxHandlerShackMarks saxHandler = new SaxHandlerShackMarks();
			xr.setContentHandler(saxHandler);
		
			// ShackMarks server returns a blank document when no marks are present
			// so we check the stream before handing it to the parser
			InputStream inputStream =url.openStream();
			if (inputStream.available() > 0)
				xr.parse(new InputSource(inputStream));

			// Our ExampleHandler now provides the parsed data to us.
			posts = saxHandler.GetParsedPosts();
		

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			new AlertDialog.Builder(this).setTitle("ShackMarks Error")
					.setPositiveButton("OK", null).setMessage("Could not connect to ShackMarks server.")
					.show();

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
		AdapterShackMarks tva = new AdapterShackMarks(this,R.layout.notes_row, posts);
		setListAdapter(tva);
	}
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		ShowShackNotePost(position);
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		// get a reference tot he ContextMenu it tells you what
		// position on  the listview was clicked.
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			
		itemPosition = info.position;
		
		switch (item.getItemId()) {
		case 0:
			ShowShackNotePost(itemPosition);
			return true;
		case 1: // delete note
			DeleteShackNote(itemPosition);
			return true;
		}
		return false;

	}
	private void ShowShackNotePost(long position) {

		ShackPost post = posts.get((int) position);
		String postID = post.getPostID();
		String storyID = post.getPostID();
		
		Intent intent = new Intent();
		intent.setClass(this, ActivityThreadedView.class);
		intent.putExtra("postID", postID); // the value must be a string
		intent.putExtra("storyID", storyID);
		startActivity(intent);
	}

	public void DeleteShackNote(long position) {

		ShackPost post = posts.get((int) position);
		String postID = post.getPostID();
		
		HandlerExtendedSites.AddRemoveShackMark(this,postID, true);
		
		// TODO: can we just remove the post from the list view?
		fillSaxData();
	}


	
}
