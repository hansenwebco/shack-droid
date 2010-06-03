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
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.stonedonkey.shackdroid.ShackGestureListener.ShackGestureEvent;

public class ActivityTopicView extends ListActivity implements Runnable, ShackGestureEvent {

	private ArrayList<ShackPost> posts;
	private String storyID = null;
	private String storyName;
	private String errorText = "";
	private Integer currentPage = 1;
	private Integer storyPages = 1;
	private String loadStoryID = null;
	private Boolean threadLoaded = true;
	private Hashtable<String, String> postCounts = null;
	private ShackPost bookmarkedPost;
	private ArrayList<ShackPost> watchCache = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Helper.SetWindowState(getWindow(),this);
		final ShackGestureListener listener = Helper.setGestureEnabledContentView(R.layout.topics, this);
		if (listener != null){
			listener.addListener(this);
		}

		final Bundle extras = this.getIntent().getExtras();
		if (extras != null)
			loadStoryID = extras.getString("StoryID");
		
		if (savedInstanceState == null) {
			try {
				// setRequestedOrientation(ActivityInfo.
				// SCREEN_ORIENTATION_LANDSCAPE);
				fillDataSAX();
			} catch (Exception e) {
				new AlertDialog.Builder(this).setTitle("Error")
						.setPositiveButton("OK", null).setMessage(
								"There was an error connecting to the API.")
						.show();
				try {
					dismissDialog(1);
				} catch (Exception ex) {
					// dialog could not be killed for some reason
				}
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

		try {
			//UpdatePostCache();
			dismissDialog(1);
		} catch (Exception ex) {
			// dialog could not be killed for some reason
		}

		// Dear Android devs... please make this more of a pain in the ass for
		// orientation changes.. KAHNNN!!!!
		savedInstanceState.putSerializable("posts", posts);
		savedInstanceState.putString("storyName", storyName);
		savedInstanceState.putInt("currentPage", currentPage);
		savedInstanceState.putInt("storyPages", storyPages);
		savedInstanceState.putString("storyID", storyID);
		savedInstanceState.putBoolean("threadLoaded", threadLoaded);
		

	}
	@SuppressWarnings("unchecked")
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		storyName = savedInstanceState.getString("storyName");
		currentPage = savedInstanceState.getInt("currentPage");
		storyPages = savedInstanceState.getInt("storyPages");
		storyID = savedInstanceState.getString("storyID");
		posts = (ArrayList<ShackPost>) savedInstanceState.getSerializable("posts");
		threadLoaded = savedInstanceState.getBoolean("threadLoaded");
		
		// If we change orientation in the middle of a thread loading we end up with 
		// the last loaded posts, this forces a new pull on orientation change.
		if (threadLoaded == false)
			fillDataSAX();  

		threadLoaded = true;
		savedInstanceState.clear(); // we'll resave it if we do something again
		
		ShowData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(1, 5, 1, "Prev Page").setIcon(R.drawable.menu_back);
		menu.add(1, 4, 2, "Next Page").setIcon(R.drawable.menu_forward);
		menu.add(2, 1, 3, "Refresh").setIcon(R.drawable.menu_reload);
		menu.add(2, 0, 4, "New Post").setIcon(R.drawable.menu_addpost);
		menu.add(1, 3, 5, "First Page").setIcon(R.drawable.menu_top);
		//menu.add(2, 2, 6, "Menu").setIcon(R.drawable.menu_delete);
		menu.findItem(5).setEnabled(false);

		return true;
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {

		if (this.currentPage <= 1) // previous enabled
		{
			menu.findItem(3).setEnabled(false); // home
			menu.findItem(5).setEnabled(false); // previous
		} else {
			menu.findItem(3).setEnabled(true); // home
			menu.findItem(5).setEnabled(true); // previous
		}
		if (this.currentPage >= this.storyPages) // next enabled
			menu.findItem(4).setEnabled(false);
		else
			menu.findItem(4).setEnabled(true);

		return super.onMenuOpened(featureId, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Context context = this;
		Intent intent;
		switch (item.getItemId()) {
		case 0: // Launch post form
			intent = new Intent();
			intent.setClass(this, ActivityPost.class);
			intent.putExtra("storyID", storyID);
			intent.putExtra("postID", "");
			startActivity(intent);
			return true;
		case 1: // refresh
			fillDataSAX();
			return true;
		case 2: // show settings dialog
			intent = new Intent();
			intent.setClass(this, ActivityInfoViewer.class);
			startActivity(intent);
			return true;
		case 3: // home
			currentPage = 1;
			fillDataSAX();
			return true;
		case 4: // forward a page
			SetPaging(1);
			fillDataSAX();
			return true;
		case 5: // previous page
			SetPaging(-1);
			fillDataSAX();
			return true;
		case 6:
			intent = new Intent();
			intent.setClass(this, ActivityShackMarks.class);
			startActivity(intent);
			return true;
		case 7:
			intent = new Intent();
			intent.setClass(this, ActivityRSS.class);
			startActivity(intent);
			return true;
		case 8:
//			String message = "Unable to complete version check, please try again later.";
//
//			int result = HandlerExtendedSites.VersionCheck(this);
//
//			if (result == 1)
//				message = "NEW SHACKDROID VERSION!\n http://www.stonedonkey.com/ShackDroid/Latest";
//			else if (result == 0)
//				message = "ShackDroid is up to date.";
//
//			new AlertDialog.Builder(this).setTitle("Version Check")
//					.setPositiveButton("OK", null).setMessage(message).show();
//			return true;
		case 9:
			intent = new Intent();
			intent.setClass(this,ActivitySearch.class);
			startActivity(intent);
			return true;
		case 10:  // show shack messages if they enabled them
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			boolean allowSMs = prefs.getBoolean("allowShackMessages", false);
			
			if (allowSMs)
			{
			intent = new Intent();
			intent.setClass(this,ActivityMessages.class);
			startActivity(intent);
			return true;
			}
			else
			{
				new AlertDialog.Builder(this).setTitle("Information")
				.setPositiveButton("OK", null).setMessage(
						"Shack Messages posts your credentials to the API " +
						"instead of directly ShackNews.\n\n If you agree with this " +
						"you can enable this feature under \"Settings\"." )
				.show();
				
			return true;
			}
				
		}
	
		return false;
	}

	private void SetPaging(Integer increment) {

		// set current page
		if ((currentPage + increment >= 1)
				&& (currentPage + increment <= storyPages))
			currentPage = currentPage + increment;
	}

	private void fillDataSAX() {

		showDialog(1);

		// use the class run() method to do work
		Thread thread = new Thread(this);
		thread.start();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 1: {
			final ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage("loading, please wait...");
			dialog.setTitle(null);
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			return dialog;
			}
		}
		return null;
	}

	public void run() {
		
		if (posts != null){
			try {
				UpdatePostCache();
			} catch (Exception e) {
			}
		}
		
		threadLoaded = false;
		try {

			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			final String feedURL = prefs.getString("shackFeedURL",getString(R.string.default_api));
			final URL url;

			if (loadStoryID != null) {
				if (currentPage > 1)
					url = new URL(feedURL + "/" + loadStoryID + "."
							+ this.currentPage.toString() + ".xml");
				else
					url = new URL(feedURL + "/" + loadStoryID + ".xml");
			} else {
				if (currentPage > 1)
					url = new URL(feedURL + "/" + this.storyID + "."
							+ this.currentPage.toString() + ".xml");
				else
					url = new URL(feedURL + "/index.xml");
			}

			// Get a SAXParser from the SAXPArserFactory.
			final SAXParserFactory spf = SAXParserFactory.newInstance();
			final SAXParser sp = spf.newSAXParser();

			// Get the XMLReader of the SAXParser we created.
			final XMLReader xr = sp.getXMLReader();
			// Create a new ContentHandler and apply it to the XML-Reader
			final SaxHandlerTopicView saxHandler = new SaxHandlerTopicView(this,"topic");
			xr.setContentHandler(saxHandler);

			// Parse the xml-data from our URL.
			xr.parse(new InputSource(HttpHelper.HttpRequestWithGzip(url.toString(),this)));

			// Our ExampleHandler now provides the parsed data to us.
			posts = saxHandler.GetParsedPosts();
			storyID = saxHandler.getStoryID();
			storyName = saxHandler.getStoryTitle();
			storyPages = saxHandler.getStoryPageCount();

			if (storyPages == 0) // XML returns a 0 for stories with only one page
				storyPages = 1;

		} catch (Exception ex) {
			Log.e("ShackDroid", "Error parsing story" + storyID);
			errorText = "An error occurred connecting to API.";
		}
		
		
		progressBarHandler.sendEmptyMessage(0);
	}

	private Handler progressBarHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// we implement a handler because most UI items
			// won't update within a thread
			try {
				dismissDialog(1);
			} catch (Exception ex) {
			}
			ShowData();
		}
	};

	

	private void ShowData()  {

		if (posts != null) {
			// storyName is set during FillData above
			setTitle("ShackDroid - " + storyName + " - "
					+ currentPage.toString() + " of "
					+ this.storyPages.toString());

			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			final String login = prefs.getString("shackLogin", "");
			final int fontSize = Integer.parseInt(prefs.getString("fontSize", "12"));

			try {
				postCounts = GetPostCache();
			} catch (Exception ex) {

			}

			// TODO: Passing this as a new HashTable seems very ugly and a waste of memory
			// Unfortunately I can't find a a way to get the Adapter to update before I call
			// the UpdatePostCache below.. that update occurs before the ListAdapter is set
			// apparently.  I can't find anything else to put the Update , so for now we'll
			// create a new Hashtable.. ick.
			
			//chazums maybe tva.notifyDataSetChanged() ?  
			Hashtable<String,String> tempHash = null;
			if (postCounts != null)
				tempHash = new Hashtable<String,String>(postCounts);

			final AdapterTopicView tva = new AdapterTopicView(getApplicationContext(),R.layout.topic_row, posts, login, fontSize,tempHash);
			
			new BookmarkAsyncTask().execute(new Object());
			
			//TODO: do another check if not in the first page.
			final ListView lv = getListView();
		
			lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			    @Override
			    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
			      menu.setHeaderTitle("Options");
			      //menu.add(0, 0, 0, "Show Story");
			      menu.add(0, 1, 0, "Copy Post Url to Clipboard");
			      menu.add(0, 2, 0, "Watch Thread");
			      menu.add(0,-1,0,"Cancel");
			    }
			  }); 
			
			setListAdapter(tva);
			
			/* chazums attempted fix for issue
			// update the reply counts for the listing of topics
			try {
				UpdatePostCache();
			} catch (Exception e) {

			}
			*/
		} else {
			if (errorText.length() > 0) {
				new AlertDialog.Builder(this).setTitle("Error")
				.setPositiveButton("OK", null).setMessage(errorText)
				.show();
			}
		}

		threadLoaded = true;
		setWatchedPosts();
	
		
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		// get a reference to to the ContextMenu it tells you what
		// position on  the listview was clicked.

		
		switch (item.getItemId()) {
		case 0:
			try {
				final Intent intent = new Intent();
				intent.putExtra("action", "story");
				intent.putExtra("id", Integer.parseInt(storyID));
				intent.setClass(this, ActivityInfoViewer.class);
				startActivity(intent);
			}
			catch (Exception ex)
			{
				
			}
			return true;
		case 1: // copy url to clipboard

			try 
			{
				final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
				final int itemPosition = info.position;
				
				final String postID = posts.get(itemPosition).getPostID();
				
				//http://www.shacknews.com/laryn.x?id=23004466
				final String url = "http://www.shacknews.com/laryn.x?id=" + postID;
				final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				clipboard.setText(url);
				Toast.makeText(this, "Link to post copied to clipboard.", Toast.LENGTH_SHORT).show();
				
			}
			catch (Exception ex)
			{
				Log.e("ShackDroid", "Error copying link to clipboard");
			}
			
			
			return true;
		case 2:
			final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

			final int itemPosition = info.position;
			final ShackPost bookmarkedPost = posts.get(itemPosition);

			//setBookmarkedPost();

			// TODO: Refactor Bookmarking

			// 1. Take the post and add it to some sort of object and save either the
			//    object or the resulting object and export it as XML that matches the
			//    API.

			ArrayList<ShackPost> watchCache = null;
			if (getFileStreamPath("watch.cache").exists()) {

				try {
					final FileInputStream fileIn = openFileInput("watch.cache");
					final ObjectInputStream in = new ObjectInputStream(fileIn);
					watchCache = (ArrayList<ShackPost>)in.readObject();
					in.close();
					fileIn.close();
				}
				catch (Exception ex){ Log.e("ShackDroid", "Error Loading watch.cache"); }
			}
			
			boolean inCache = false;
			if (watchCache == null || watchCache.size() == 0)
			{
				watchCache = new ArrayList<ShackPost>();
				//watchCache.add(bookmarkedPost);
			}
			else // do we already have this post in our collection
			{
				for(int counter= 0; counter < watchCache.size();counter++) {
					if (watchCache.get(counter).getPostID().equals(bookmarkedPost.getPostID()))
					{
						inCache = true;
						break;
					}
				}
			}

			if (inCache == false) {
				// reuse the postindex to store story for these... I didn't 
				// want to add a new field just for this function... probably should
				bookmarkedPost.setPostIndex(Integer.parseInt(storyID)); 
				watchCache.add(bookmarkedPost);
			
				// save our cache back to the users system.
				try {
					saveWatchCache(watchCache);

					final Toast toast = Toast.makeText(getBaseContext(),"Topic now being watched.",Toast.LENGTH_SHORT);
					toast.show();
				}
				catch (Exception ex ){
					Log.e("ShackDroid", "Error Saving watch.cache");
				}
			}
			else
			{
				final Toast toast = Toast.makeText(getBaseContext(),"Topic already being watched.",Toast.LENGTH_SHORT);
				toast.show();
			}

			// 2. After adding it to the collection bind it to a list view in the tray
			//    The ListView can use the same view as the TopicView and the same
			//    SaxParser etc
			setWatchedPosts();

			// 3. Update the user when threads get updates.. how do to this hrm.

			return true;			
		}

		return false;

	}
	
	@SuppressWarnings("unchecked")
	private void setWatchedPosts(){

		// TODO: CLEAN THIS UP!
		
		try {
			final FileInputStream fileIn = openFileInput("watch.cache");
			final ObjectInputStream in = new ObjectInputStream(fileIn);
			watchCache = (ArrayList<ShackPost>)in.readObject();
			in.close();
			fileIn.close();
		}
		catch (Exception ex){ Log.e("ShackDroid", "Error Loading watch.cache"); }
		
		final SlidingDrawer s = (SlidingDrawer)findViewById(R.id.SlidingDrawer01);
		
		if (watchCache == null || watchCache.size() == 0)
		{
			s.setVisibility(View.GONE);
		}
		else
		{
			s.setVisibility(View.VISIBLE);
			
			ListView v = (ListView) findViewById(R.id.ListViewWatchedThreads);
			v.removeAllViewsInLayout();

			try {
				postCounts = GetPostCache();
			} catch (Exception ex) {

			}
			v.setAdapter(new AdapterTopicView(this, R.layout.topic_row, watchCache, "stonedonkey", 14, postCounts));
		
			v.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					
					ShackPost post = watchCache.get(position);
					
					final  Intent intent = new Intent();
					intent.setClass(getApplicationContext(), ActivityThreadedView.class);
					intent.putExtra("postID", post.getPostID()); // the value must be a string
					intent.putExtra("storyID", post.getPostIndex());
					if (post.getPostCategory().equalsIgnoreCase("nws"))
						intent.putExtra("isNWS", true);
					else
						intent.putExtra("isNWS",false);
					
					startActivity(intent);		
				}
			});
			
			v.setOnItemLongClickListener(new OnItemLongClickListener()
			{
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					try {

						final int pos = position;
						
						new AlertDialog.Builder(view.getContext())
						.setTitle("Remove Watched Topic")
						.setMessage("Are you sure you wish to remove this watched topic?")
						.setPositiveButton("YES",  new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
						
								watchCache.remove(pos);
				 			    try {
									saveWatchCache(watchCache);
								} catch (Exception e) {
									
								}
								// close the tray if there are no more messages
				 			    if (watchCache.size() <= 0)
									s.close();
				 			    // relist the items in the users cache
								setWatchedPosts();
							}
						})
						.setNegativeButton("NO", null).show();
					}
					catch (Exception ex)
					{
						Log.e("ShackDroid","Error removing topic from watch list");
					}
					return true;
				}
			});
		}
	}
	
	private void saveWatchCache(ArrayList<ShackPost> watchCache) throws IOException
	{
		final FileOutputStream fos = openFileOutput("watch.cache",MODE_PRIVATE);
		final ObjectOutputStream os = new ObjectOutputStream(fos);
		os.writeObject(watchCache);
		os.close();
		fos.close();
	}
	
	@SuppressWarnings("unchecked")
	public Hashtable<String, String> GetPostCache() throws StreamCorruptedException, IOException
	{
		if (getFileStreamPath("posts.cache").exists()) {

			Hashtable<String, String> postCounts = null;
			
			// if the day is different we delete and recreate the file
			final long lastMod = getFileStreamPath("posts.cache").lastModified();
			final Date lastDateMod = new Date(lastMod); 
			final Date currentDate = new Date();
			if (lastDateMod.getDay() != currentDate.getDay())
			{
				getFileStreamPath("posts.cache").delete();
				return null;
			} 
						
			final FileInputStream fileIn = openFileInput("posts.cache");
			final ObjectInputStream in = new ObjectInputStream(fileIn);
			try {
				postCounts = (Hashtable<String, String>)in.readObject();
			} catch (ClassNotFoundException e) {
				return null; // fail boat?
			}
			in.close(); 
			fileIn.close();
				
			
			return postCounts;
		}
		else 
			return null;
	}
	 
	public void UpdatePostCache() throws StreamCorruptedException, IOException
	{
		if (postCounts == null)
			postCounts = new Hashtable<String, String>();
		
		for(int x= 0; x < posts.size();x++)
			postCounts.put(posts.get(x).getPostID(), posts.get(x).getReplyCount());
		
		final FileOutputStream fos = openFileOutput("posts.cache",MODE_PRIVATE);
		final ObjectOutputStream os = new ObjectOutputStream(fos);
		os.writeObject(postCounts);
		os.close();
		fos.close();
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		final String cat = posts.get(position).getPostCategory();
		
		final  Intent intent = new Intent();
		intent.setClass(getApplicationContext(), ActivityThreadedView.class);
		intent.putExtra("postID", Long.toString(id)); // the value must be a string
		intent.putExtra("storyID", storyID);
		if (cat.equalsIgnoreCase("nws"))
			intent.putExtra("isNWS", true);
		else
			intent.putExtra("isNWS",false);
		
		startActivity(intent);
	}

	@Override
	public void eventRaised(int eventType) {
		switch(eventType){
			case ShackGestureListener.FORWARD:
				SetPaging(1);
				fillDataSAX();				
				break;
			case ShackGestureListener.BACKWARD:
				if (this.currentPage == 1)
					finish();
				else {
					SetPaging(-1);
					fillDataSAX();
				}
								
				break;
			case ShackGestureListener.REFRESH:
				fillDataSAX();
				break;				
		}
	}
	
	class BookmarkAsyncTask extends AsyncTask<Object, Object, Boolean>{

		@Override
		protected Boolean doInBackground(Object... params) {
			if (bookmarkedPost != null){
				for (ShackPost s : posts){
					if (s.getPostID().equals(bookmarkedPost.getPostID())){
						if (Integer.parseInt(s.getReplyCount()) >
								Integer.parseInt(bookmarkedPost.getReplyCount())){
							bookmarkedPost = s;
							return true;
						}
						break;
					}
				}
				
				final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				String feedURL = prefs.getString("shackFeedURL", getString(R.string.default_api));							
				ArrayList<ShackPost> tmp = Helper.getPostTreeById(feedURL, 
						true,//bookmarkedPost.getPostCategory().equals("nws"), 
						bookmarkedPost.getPostID(), 
						getApplicationContext());
				
				if (!tmp.get(0).getReplyCount().equals(bookmarkedPost.getReplyCount())){
					bookmarkedPost = tmp.get(0);
					return true;
				}
			}
			return false;
		}
		
		protected void onPostExecute(Boolean result) {
			if (result){
				ImageView v = (ImageView)findViewById(R.id.handle);
				v.setImageResource(R.drawable.slidernew);				
				setWatchedPosts();
			}
		}
	}	
}


