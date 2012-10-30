package com.stonedonkey.shackdroid;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.stonedonkey.shackdroid.ActivityTopicView.WatchedThreadsAsyncTask;
import com.stonedonkey.shackdroid.ShackGestureListener.ShackGestureEvent;

public class FragmentTopicView extends ListFragment implements ShackGestureEvent {

	private ArrayList<ShackPost> posts;
	private String storyID = null;

	private String errorText = "";
	private Integer currentPage = 1;
	private Integer storyPages = 1;
	private String loadStoryID = null;
	private Boolean threadLoaded = true;
	private Hashtable<String, String> postCounts = null;
	private AdapterLimerifficTopic tva;
	private ArrayList<ShackPost> watchCache = null;

	public FragmentTopicView() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setRetainInstance(true);
		this.setHasOptionsMenu(true);

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId())
		{
		case 0:
			try {
				final Intent intent = new Intent();
				intent.putExtra("action", "story");
				intent.putExtra("id", Integer.parseInt(storyID));
				intent.setClass(getActivity(), ActivityInfoViewer.class);
				startActivity(intent);
			}
			catch (Exception ex) {

			}
			return true;
		case 1: // copy url to clipboard

			try {
				final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
				final int itemPosition = info.position;

				final String postID = posts.get(itemPosition).getPostID();

				// http://www.shacknews.com/chatty?id=25445895
				final String url = "http://www.shacknews.com/chatty?id=" + postID;
				final ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setText(url);
				Toast.makeText(getActivity(), "Link to post copied to clipboard.", Toast.LENGTH_SHORT).show();

			}
			catch (Exception ex) {
				Log.e("ShackDroid", "Error copying link to clipboard");
			}

			return true;
		case 2:
			final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

			final int itemPosition = info.position;
			final ShackPost bookmarkedPost = posts.get(itemPosition);

			// setBookmarkedPost();

			// TODO: Refactor Bookmarking

			// 1. Take the post and add it to some sort of object and save
			// either the
			// object or the resulting object and export it as XML that matches
			// the
			// API.

			ArrayList<ShackPost> watchCache = null;
			if (getActivity().getFileStreamPath("watch.cache").exists()) {

				try {
					synchronized (Helper.dataLock) {
						final FileInputStream fileIn = getActivity().openFileInput("watch.cache");
						final ObjectInputStream in = new ObjectInputStream(fileIn);
						watchCache = (ArrayList<ShackPost>) in.readObject();
						in.close();
						fileIn.close();
					}
				}
				catch (Exception ex) {
					Log.e("ShackDroid", "Error Loading watch.cache");
				}
			}

			boolean inCache = false;
			if (watchCache == null || watchCache.size() == 0) {
				watchCache = new ArrayList<ShackPost>();
				// watchCache.add(bookmarkedPost);
			}
			else
			// do we already have this post in our collection
			{
				for (int counter = 0; counter < watchCache.size(); counter++) {
					if (watchCache.get(counter).getPostID().equals(bookmarkedPost.getPostID())) {
						inCache = true;
						break;
					}
				}

			}

			if (inCache == false) {
				// reuse the postindex to store story for these... I didn't
				// want to add a new field just for this function... probably
				// should
				bookmarkedPost.setPostIndex(Integer.parseInt(storyID));
				bookmarkedPost.setOriginalReplyCount(Integer.parseInt(bookmarkedPost.getReplyCount()));
				watchCache.add(bookmarkedPost);

				// save our cache back to the users system.
				try {
					saveWatchCache(watchCache);

					final Toast toast = Toast.makeText(getActivity(), "Topic now being watched.", Toast.LENGTH_SHORT);
					toast.show();
				}
				catch (Exception ex) {
					Log.e("ShackDroid", "Error Saving watch.cache");
				}
			}
			else {
				final Toast toast = Toast.makeText(getActivity(), "Topic already being watched.", Toast.LENGTH_SHORT);
				toast.show();
			}

			// 2. After adding it to the collection bind it to a list view in
			// the tray
			// The ListView can use the same view as the TopicView and the same
			// SaxParser etc
			setWatchedPosts(false);

			// 3. Update the user when threads get updates.. how do to this hrm.

			return true;
		case 3:

			final AdapterContextMenuInfo inf = (AdapterContextMenuInfo) item.getMenuInfo();
			final int itemPos = inf.position;
			final ShackPost post = posts.get(itemPos);

			new AlertDialog.Builder(getActivity()).setTitle("Thread Will Expire In:").setMessage(Helper.GetTimeLeftString(post.getPostDate())).setNegativeButton("OK", null).show();

			return true;

		case 4:
			final AdapterContextMenuInfo inf2 = (AdapterContextMenuInfo) item.getMenuInfo();
			final int itemPos2 = inf2.position;
			final String shackname = posts.get(itemPos2).getPosterName();

			final Intent intent = new Intent();
			intent.putExtra("shackname", shackname);
			intent.setClass(getActivity(), ActivityProfile.class);
			startActivity(intent);

			return true;

		}

		return false;
	}

	private void saveWatchCache(ArrayList<ShackPost> watchCache) throws IOException {
		synchronized (Helper.dataLock) {
			final FileOutputStream fos = getActivity().openFileOutput("watch.cache", getActivity().MODE_PRIVATE);
			final ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(watchCache);
			os.close();
			fos.close();
		}
	}

	@SuppressWarnings("unchecked")
	private void setWatchedPosts(Boolean loadMissingThreads) {

		// TODO: CLEAN THIS UP!

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		final int fontSize = Integer.parseInt(prefs.getString("fontSize", "12"));
		final String login = prefs.getString("shackLogin", "");

		try {
			synchronized (Helper.dataLock) {
				final FileInputStream fileIn = getActivity().openFileInput("watch.cache");
				final ObjectInputStream in = new ObjectInputStream(fileIn);
				watchCache = (ArrayList<ShackPost>) in.readObject();
				in.close();
				fileIn.close();
			}
		}
		catch (Exception ex) {
			Log.e("ShackDroid", "Error Loading watch.cache");
		}

		final SlidingDrawer s = (SlidingDrawer) getActivity().findViewById(R.id.SlidingDrawer01);

		if (watchCache == null || watchCache.size() == 0) {
			s.setVisibility(View.GONE);
		}
		else {
			s.setVisibility(View.VISIBLE);

			final ListView v = (ListView) getActivity().findViewById(R.id.ListViewWatchedThreads);
			v.removeAllViewsInLayout();

			try {
				postCounts = GetPostCache();
			}
			catch (Exception ex) {

			}

			// TODO: should probably run this in an async task me thinks
			// Thread thread = new Thread(this);
			// thread.start();

			final AdapterLimerifficTopic adapter = new AdapterLimerifficTopic(getActivity(), R.layout.lime_topic_row, watchCache, login, postCounts);
			v.setAdapter(adapter);

			v.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

					final String cat = watchCache.get(position).getPostCategory();

					FragmentThreadedView threadView = (FragmentThreadedView) getFragmentManager().findFragmentById(R.id.MixedThreads);
					if (threadView == null) {
						Intent intent = new Intent(getActivity(), FragmentActivityThread.class);

						intent.putExtra("postID", Long.toString(id)); // the value must be a
																		// string
						intent.putExtra("storyID", storyID);

						if (cat.equalsIgnoreCase("nws"))
							intent.putExtra("isNWS", true);
						else
							intent.putExtra("isNWS", false);

						startActivity(intent);
					}
					else {
						threadView.setPostID(Long.toString(id));
						threadView.setStoryID(storyID);
						threadView.setCurrentPosition(0);
						if (cat.equalsIgnoreCase("nws"))
							threadView.setIsNWS(true);
						else
							threadView.setIsNWS(false);

						threadView.fillSaxData(Long.toString(id));

					}

					// ShackPost post = watchCache.get(position);
					//
					// final Intent intent = new Intent();
					// intent.setClass(getActivity(), ActivityThreadedView.class);
					// intent.putExtra("postID", post.getPostID()); // the value
					// // must be a
					// // string
					// intent.putExtra("storyID", post.getPostIndex());
					// if (post.getPostCategory().equalsIgnoreCase("nws"))
					// intent.putExtra("isNWS", true);
					// else
					// intent.putExtra("isNWS", false);
					//
					// startActivity(intent);
				}
			});

			v.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					try {

						final int pos = position;

						new AlertDialog.Builder(view.getContext()).setTitle("Remove Watched Topic").setMessage("Are you sure you wish to remove this watched topic?").setPositiveButton("YES", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {

								watchCache.remove(pos);
								try {
									saveWatchCache(watchCache);
								}
								catch (Exception e) {

								}
								// close the tray if there are no more
								// messages
								if (watchCache.size() <= 0)
									s.close();
								// relist the items in the users cache
								setWatchedPosts(false);
							}
						}).setNegativeButton("NO", null).show();
					}
					catch (Exception ex) {
						Log.e("ShackDroid", "Error removing topic from watch list");
					}
					return true;
				}
			});

			try {
				updateWatchedPosts(postCounts, loadMissingThreads);
			}
			catch (Exception e) {
				Log.e("ShackDroid", "Error updating unwatched posts.");
			}

		}
	}

	private void updateWatchedPosts(Hashtable<String, String> tempHash, Boolean loadMissingThreads) throws StreamCorruptedException, IOException, ClassNotFoundException {
		// check to see if the post is in our current load of posts, and if not
		// call it via the api and get the total replies
		final TextView handle = (TextView) getActivity().findViewById(R.id.TextViewTrayHandle);
		handle.setText("Refreshing...");

		new WatchedThreadsAsyncTask(tempHash, posts, loadMissingThreads).execute();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		return inflater.inflate(R.layout.topics, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (posts == null) {
			currentPage = 1;
			GetChattyAsyncTask chatty = new GetChattyAsyncTask(getActivity());
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
					GetChattyAsyncTask chatty = new GetChattyAsyncTask(getActivity());
					chatty.execute();

				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

		});

		if (threadLoaded) {

		}

		RotateAnimation anim = new RotateAnimation(0f, 359f, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		anim.setInterpolator(new LinearInterpolator());
		anim.setRepeatCount(Animation.INFINITE);
		anim.setDuration(700);

		ImageView loader = (ImageView) getView().findViewById(R.id.ImageViewTopicLoader);
		loader.setAnimation(anim);

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		// return super.onCreateOptionsMenu(menu);

		// MenuInflater i = getSupportMenuInflater();
		inflater.inflate(R.menu.topic_menu, menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId())
		{
		case R.id.topic_menu_newpost: // Launch post form
			intent = new Intent();
			intent.setClass(getActivity(), ActivityPost.class);
			intent.putExtra("storyID", storyID);
			intent.putExtra("postID", "");
			startActivity(intent);
			return true;
		case R.id.topic_menu_refresh: // refresh
			currentPage = 1;
			// get the list of topics
			tva = null;
			posts.clear();
			GetChattyAsyncTask chatty = new GetChattyAsyncTask(getActivity());
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

		FragmentThreadedView threadView = (FragmentThreadedView) getFragmentManager().findFragmentById(R.id.MixedThreads);
		if (threadView == null) {
			Intent intent = new Intent(getActivity(), FragmentActivityThread.class);

			intent.putExtra("postID", Long.toString(id)); // the value must be a
															// string
			intent.putExtra("storyID", storyID);

			if (cat.equalsIgnoreCase("nws"))
				intent.putExtra("isNWS", true);
			else
				intent.putExtra("isNWS", false);

			startActivity(intent);
		}
		else {
			threadView.setPostID(Long.toString(id));
			threadView.setStoryID(storyID);
			threadView.setCurrentPosition(0);
			if (cat.equalsIgnoreCase("nws"))
				threadView.setIsNWS(true);
			else
				threadView.setIsNWS(false);

			threadView.fillSaxData(Long.toString(id));

		}

	}

	private void ShowData() {

		if (posts != null) {
			Hashtable<String, String> tempHash = null;

			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			final String login = prefs.getString("shackLogin", "");

			try {
				postCounts = GetPostCache();
			}
			catch (Exception ex) {

			}
			if (postCounts != null)
				tempHash = new Hashtable<String, String>(postCounts);

			if (tva == null) {
				tva = new AdapterLimerifficTopic(getActivity(), R.layout.lime_topic_row, posts, login, tempHash);
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
					new AlertDialog.Builder(getActivity()).setTitle("Error").setPositiveButton("OK", null).setMessage(errorText).show();
				}
				catch (Exception ex) {
					// could not create a alert for the error for one reason
					// or another
					Log.e("ShackDroid", "Unable to create error alert ActivityTopicView:468");
				}
			}
		}
		threadLoaded = true;

		setWatchedPosts(true);
	}

	@SuppressWarnings("unchecked")
	public Hashtable<String, String> GetPostCache() throws StreamCorruptedException, IOException {
		if (getActivity().getFileStreamPath("posts.cache").exists()) {

			Hashtable<String, String> postCounts = null;

			// if the day is different we delete and recreate the file
			final long lastMod = getActivity().getFileStreamPath("posts.cache").lastModified();
			final Date lastDateMod = new Date(lastMod);
			final Date currentDate = new Date();
			if (lastDateMod.getDay() != currentDate.getDay()) {
				getActivity().getFileStreamPath("posts.cache").delete();
				return null;
			}

			final FileInputStream fileIn = getActivity().openFileInput("posts.cache");
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

		final FileOutputStream fos = getActivity().openFileOutput("posts.cache", getActivity().MODE_PRIVATE);
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
					posts.addAll(posts.size(), newPosts);

				}

				storyID = saxHandler.getStoryID();

				storyPages = saxHandler.getStoryPageCount();

				if (storyPages == 0) // XML returns a 0 for stories with only one page
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
				dialog = ProgressDialog.show(getActivity(), null, "Loading Chatty", true, true);
			else
				SetLoaderVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			ShowData();

			SetLoaderVisibility(View.GONE);

			try {
				if (dialog.isShowing())
					dialog.dismiss();
			}
			catch (Exception ex) {
			}

		}

	}

	protected void SetLoaderVisibility(int visibility) {
		RelativeLayout loader = (RelativeLayout) getView().findViewById(R.id.TopicLoader);
		loader.setVisibility(visibility);
	}

	@Override
	public void eventRaised(int eventType) {
		// TODO Auto-generated method stub

	}

	class WatchedThreadsAsyncTask extends AsyncTask<Object, Object, Boolean> {

		Hashtable<String, String> tempHash;
		Boolean loadMissingThreads;
		private ArrayList<ShackPost> watchCache;
		private ArrayList<ShackPost> posts;
		private Boolean timeOutMissingThreads = false;

		int newPosts = 0;

		public WatchedThreadsAsyncTask(Hashtable<String, String> tempHash, ArrayList<ShackPost> posts, Boolean loadMissingThreads) {
			this.tempHash = tempHash;
			this.loadMissingThreads = loadMissingThreads;
			this.posts = posts;

			// see if we've checked missing threads over 5 minutes ago
			// if so we'll check them again, if not we'll leave them
			// until a load of over 5 mins happens
			final SharedPreferences settings = getActivity().getPreferences(0);
			SharedPreferences.Editor editor = settings.edit();
			long lastCheck = settings.getLong("lastMissingThreadLoad", 0);

			Calendar currentDate = Calendar.getInstance();
			if (currentDate.getTimeInMillis() - lastCheck > 300000) // 5 mins
			{
				timeOutMissingThreads = true;

				editor.putLong("lastMissingThreadLoad", currentDate.getTimeInMillis());
				editor.commit();
			}

		}

		@SuppressWarnings("unchecked")
		@Override
		protected Boolean doInBackground(Object... arg0) {

			try {
				synchronized (Helper.dataLock) {
					final FileInputStream fileIn = getActivity().openFileInput("watch.cache");
					final ObjectInputStream in = new ObjectInputStream(fileIn);
					watchCache = (ArrayList<ShackPost>) in.readObject();
					in.close();
					fileIn.close();
				}
			}
			catch (Exception e) {
				// TODO: handle exception
			}

			for (int counter = 0; counter < watchCache.size(); counter++) {
				ShackPost post = watchCache.get(counter);

				// check to see if the post is in our current load of posts, and
				// if not
				// call it via the api and get the total replies
				if (posts != null && loadMissingThreads && timeOutMissingThreads) {
					Boolean postFound = false;
					for (int counterTwo = 0; counterTwo < posts.size(); counterTwo++) {
						ShackPost postTemp = posts.get(counterTwo);
						if (postTemp.getPostID().equals(post.getPostID())) {
							postFound = true;
							break;
						}
					}
					if (!postFound) {
						Integer replies = Helper.getThreadReplyCount(Integer.parseInt(post.getPostID()), getActivity());
						tempHash.put(post.getPostID(), replies.toString());
					}
				}

				if (tempHash != null) {
					String cacheCount = tempHash.get(post.getPostID());
					if (cacheCount != null) {
						final int change = Integer.parseInt(cacheCount) - Integer.parseInt(post.getReplyCount());
						if (change > 0)
							newPosts = newPosts + change;
					}
				}
			}

			try {
				final FileOutputStream fos = getActivity().openFileOutput("posts.cache", getActivity().MODE_PRIVATE);
				final ObjectOutputStream os = new ObjectOutputStream(fos);
				os.writeObject(tempHash);
				os.close();
				fos.close();
			}
			catch (Exception ex) {
				Log.e("ShackDroid", "Error saving watch cache");
			}

			return null;

		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			String topicString = "topic";
			String postsString = "reply";
			if (watchCache.size() > 1)
				topicString = "topics";

			if (newPosts > 1)
				postsString = "replies";

			final TextView handle = (TextView) getActivity().findViewById(R.id.TextViewTrayHandle);
			if (newPosts == 0)
				handle.setText("Watching " + watchCache.size() + " " + topicString);
			else
				handle.setText("Watching " + watchCache.size() + " " + topicString + " / " + newPosts + " new " + postsString);

		}

	}
}
