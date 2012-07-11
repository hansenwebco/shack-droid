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
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ListView;

public class ActivityLimeriffic extends ListActivity {

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

		// get the list of topics
		GetChattyAsyncTask chatty = new GetChattyAsyncTask(this);
		chatty.execute();

	}

	private void ShowData() {

		if (posts != null) {
			Hashtable<String, String> tempHash = null;

			// storyName is set during FillData above
			setTitle("ShackDroid - " + storyName + " - " + currentPage.toString() + " of " + this.storyPages.toString());

			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			final String login = prefs.getString("shackLogin", "");
			final int fontSize = Integer.parseInt(prefs.getString("fontSize", "12"));

			try {
				postCounts = GetPostCache();
			}
			catch (Exception ex) {

			}
			if (postCounts != null)
				tempHash = new Hashtable<String, String>(postCounts);

			final AdapterLimerifficTopic tva = new AdapterLimerifficTopic(getApplicationContext(), R.layout.lime_topic_row, posts, login, fontSize, tempHash);

			final ListView lv = getListView();
			lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				@Override
				public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
					menu.setHeaderTitle("Options");
					menu.add(0, 1, 0, "Copy Post Url to Clipboard");
					menu.add(0, 2, 0, "Watch Thread");
					menu.add(0, 3, 0, "Thread Expires In?");
					menu.add(0, 4, 0, "Shacker's Chatty Profile");
				}
			});

			setListAdapter(tva);

			// update the reply counts for the listing of topics
			try {
				UpdatePostCache();

			}
			catch (Exception e) {

			}

		}
		else {
			if (errorText.length() > 0) {

				try {
					new AlertDialog.Builder(this).setTitle("Error").setPositiveButton("OK", null).setMessage(errorText).show();
				}
				catch (Exception ex) {
					// could not create a alert for the error for one reason
					// or another
					Log.e("ShackDroid", "Unable to create error alert ActivityTopicView:468");
				}
			}
		}
		threadLoaded = true;
	}

	@SuppressWarnings("unchecked")
	public Hashtable<String, String> GetPostCache() throws StreamCorruptedException, IOException {
		if (getFileStreamPath("posts.cache").exists()) {

			Hashtable<String, String> postCounts = null;

			// if the day is different we delete and recreate the file
			final long lastMod = getFileStreamPath("posts.cache").lastModified();
			final Date lastDateMod = new Date(lastMod);
			final Date currentDate = new Date();
			if (lastDateMod.getDay() != currentDate.getDay()) {
				getFileStreamPath("posts.cache").delete();
				return null;
			}

			final FileInputStream fileIn = openFileInput("posts.cache");
			final ObjectInputStream in = new ObjectInputStream(fileIn);
			try {
				postCounts = (Hashtable<String, String>) in.readObject();
			}
			catch (ClassNotFoundException e) {
				return null; // fail boat?
			}
			in.close();
			fileIn.close();

			return postCounts;
		}
		else
			return null;
	}

	public void UpdatePostCache() throws StreamCorruptedException, IOException {
		if (postCounts == null)
			postCounts = new Hashtable<String, String>();

		for (int x = 0; x < posts.size(); x++)
			postCounts.put(posts.get(x).getPostID(), posts.get(x).getReplyCount());

		final FileOutputStream fos = openFileOutput("posts.cache", MODE_PRIVATE);
		final ObjectOutputStream os = new ObjectOutputStream(fos);
		os.writeObject(postCounts);
		os.close();
		fos.close();
	}

	class GetChattyAsyncTask extends AsyncTask<String, Void, Void> {
		protected ProgressDialog dialog;
		protected Context c;

		public GetChattyAsyncTask(Context context) {
			this.c = context;
		}

		@Override
		protected Void doInBackground(String... params) {

			try {
				final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
				final String feedURL = prefs.getString("shackFeedURL", getString(R.string.default_api));
				final URL url;

				if (loadStoryID != null) {
					if (currentPage > 1)
						url = new URL(feedURL + "/" + loadStoryID + "." + currentPage.toString() + ".xml");
					else
						url = new URL(feedURL + "/" + loadStoryID + ".xml");
				}
				else {
					if (currentPage > 1)
						url = new URL(feedURL + "/" + storyID + "." + currentPage.toString() + ".xml");
					else
						url = new URL(feedURL + "/index.xml");
				}

				// Get a SAXParser from the SAXPArserFactory.
				final SAXParserFactory spf = SAXParserFactory.newInstance();
				final SAXParser sp = spf.newSAXParser();

				// Get the XMLReader of the SAXParser we created.
				final XMLReader xr = sp.getXMLReader();
				// Create a new ContentHandler and apply it to the XML-Reader
				final SaxHandlerTopicView saxHandler = new SaxHandlerTopicView(c, "topic");
				xr.setContentHandler(saxHandler);

				// Parse the xml-data from our URL.
				xr.parse(new InputSource(HttpHelper.HttpRequestWithGzip(url.toString(), c)));

				// Our ExampleHandler now provides the parsed data to us.
				posts = saxHandler.GetParsedPosts();
				storyID = saxHandler.getStoryID();
				storyName = saxHandler.getStoryTitle();
				storyPages = saxHandler.getStoryPageCount();

				if (storyPages == 0) // XML returns a 0 for stories with only
										// one
										// page
					storyPages = 1;

			}
			catch (Exception ex) {
				// TODO: implement error handling
			}

			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			dialog = ProgressDialog.show(ActivityLimeriffic.this, "Loading", "La la la la la", true, true);
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			ShowData();
			dialog.dismiss();
		}

	}

}
