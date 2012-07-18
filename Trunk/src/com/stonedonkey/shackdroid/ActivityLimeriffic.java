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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.stonedonkey.shackdroid.ShackGestureListener.ShackGestureEvent;

public class ActivityLimeriffic extends SherlockListActivity implements ShackGestureEvent {

	private ArrayList<ShackPost> posts;
	private String storyID = null;

	private String errorText = "";
	private Integer currentPage = 1;
	private Integer storyPages = 1;
	private String loadStoryID = null;
	private Boolean threadLoaded = true;
	private Hashtable<String, String> postCounts = null;
	private AdapterLimerifficTopic tva;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		final ShackGestureListener listener = Helper.setGestureEnabledContentView(R.layout.topics, this);
		if (listener != null) {
			listener.addListener(this);
		}

		if (savedInstanceState == null)
		{
			// 	get the list of topics
			GetChattyAsyncTask chatty = new GetChattyAsyncTask(this);
			chatty.execute();
		}

		ListView lv = getListView();
		lv.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

				// start loading the next page
				if (threadLoaded && firstVisibleItem + visibleItemCount >= totalItemCount && currentPage + 1 <= storyPages) {
				
					// get the list of topics
					currentPage++;
					GetChattyAsyncTask chatty = new GetChattyAsyncTask(getApplicationContext());
					chatty.execute();

				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

		});

		if (threadLoaded) {

		}

	}
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

		try
		{
			// UpdatePostCache();
			dismissDialog(1);
		} catch (Exception ex)
		{
			// dialog could not be killed for some reason
		}

		// Dear Android devs... please make this more of a pain in the ass for
		// orientation changes.. KAHNNN!!!!
		savedInstanceState.putSerializable("posts", posts);
		//savedInstanceState.putString("storyName", storyName);
		savedInstanceState.putInt("currentPage", currentPage);
		savedInstanceState.putInt("storyPages", storyPages);
		savedInstanceState.putString("storyID", storyID);
		savedInstanceState.putBoolean("threadLoaded", threadLoaded);
		savedInstanceState.putInt("scrollPos", getListView().getFirstVisiblePosition());
		

	}
	@SuppressWarnings("unchecked")
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		//storyName = savedInstanceState.getString("storyName");
		currentPage = savedInstanceState.getInt("currentPage");
		storyPages = savedInstanceState.getInt("storyPages");
		storyID = savedInstanceState.getString("storyID");
		posts = (ArrayList<ShackPost>) savedInstanceState.getSerializable("posts");
		threadLoaded = savedInstanceState.getBoolean("threadLoaded");

		// If we change orientation in the middle of a thread loading we end up
		// with
		// the last loaded posts, this forces a new pull on orientation change.
		if (threadLoaded == false) {
			GetChattyAsyncTask chatty = new GetChattyAsyncTask(getApplicationContext());
			chatty.execute();
		}

		threadLoaded = true;

		ShowData();

		final int position = savedInstanceState.getInt("scrollPos");
		final ListView lv = getListView();
		lv.requestFocusFromTouch();
		lv.setSelection(position);

		// savedInstanceState.clear(); // we'll resave it if we do something
		// again

	}	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// return super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.topic_menu, menu);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId())
		{
		case R.id.topic_menu_newpost: // Launch post form
			intent = new Intent();
			intent.setClass(this, ActivityPost.class);
			intent.putExtra("storyID", storyID);
			intent.putExtra("postID", "");
			startActivity(intent);
			return true;
		case R.id.topic_menu_refresh: // refresh
			currentPage = 1;
			// get the list of topics
			posts.clear();
			tva = null;
			GetChattyAsyncTask chatty = new GetChattyAsyncTask(this);
			chatty.execute();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		final String cat = posts.get(position).getPostCategory();

		final Intent intent = new Intent();
		intent.setClass(getApplicationContext(), ActivityThreadedView.class);
		intent.putExtra("postID", Long.toString(id)); // the value must be a
														// string
		intent.putExtra("storyID", storyID);
		if (cat.equalsIgnoreCase("nws"))
			intent.putExtra("isNWS", true);
		else
			intent.putExtra("isNWS", false);

		startActivity(intent);
	}

	private void ShowData() {

		if (posts != null) {
			Hashtable<String, String> tempHash = null;


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

			if (tva == null) {
				tva = new AdapterLimerifficTopic(getApplicationContext(), R.layout.lime_topic_row, posts, login, fontSize, tempHash);
				setListAdapter(tva);
			}
			else {
				tva.SetPosts(posts);
				tva.notifyDataSetChanged();
			}

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

			threadLoaded = false;

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
				SaxHandlerTopicView saxHandler = new SaxHandlerTopicView(c, "topic");

				xr.setContentHandler(saxHandler);

				// Parse the xml-data from our URL.
				xr.parse(new InputSource(HttpHelper.HttpRequestWithGzip(url.toString(), c)));

				// Our ExampleHandler now provides the parsed data to us.
				if (posts == null) {
					posts = saxHandler.GetParsedPosts();
				}
				else {
					ArrayList<ShackPost> newPosts = saxHandler.GetParsedPosts();
					newPosts.removeAll(posts);
					posts.addAll(posts.size(),newPosts );
					
				}

				storyID = saxHandler.getStoryID();
				
				storyPages = saxHandler.getStoryPageCount();

				if (storyPages == 0) // XML returns a 0 for stories with only
										// one page
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

			if (currentPage == 1)
				dialog = ProgressDialog.show(ActivityLimeriffic.this, null, "Loading Chatty", true, true);
			else
				SetLoaderVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			ShowData();

			SetLoaderVisibility(View.GONE);

			try {
				dialog.dismiss();
			}
			catch (Exception e) {
			}

		}

	}

	protected void SetLoaderVisibility(int visibility) {
		RelativeLayout loader = (RelativeLayout) findViewById(R.id.TopicLoader);
		loader.setVisibility(visibility);
	}

	@Override
	public void eventRaised(int eventType) {
		// TODO Auto-generated method stub

	}

}
