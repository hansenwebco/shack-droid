package com.stonedonkey.shackdroid;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Html.TagHandler;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.StrikethroughSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.stonedonkey.shackdroid.ShackGestureListener.ShackGestureEvent;

public class FragmentThreadedView extends ListFragment implements Runnable, ShackGestureEvent {

	private static final int POST_REPLY = 0;
	private ArrayList<ShackPost> posts;
	private String storyID;
	private String postID;
	private String errorText = "";
	private int currentPosition = 0;
	private Boolean threadLoaded = true;
	private Boolean isNWS = false;

	private SharedPreferences prefs;
	private String login;

	private ProgressDialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		this.setHasOptionsMenu(true);
		this.setMenuVisibility(false);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		Fragment topicView = (Fragment) getFragmentManager().findFragmentById(R.id.MixedTopics);
		if (topicView ==null)
			inflater.inflate(R.menu.thread_menu_actionbar, menu);	
		else
			inflater.inflate(R.menu.thread_menu, menu);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		return inflater.inflate(R.layout.thread, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (posts != null && getListView() != null && posts.size() >= currentPosition) {
			UpdatePostText(currentPosition, true);
		}

		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		login = prefs.getString("shackLogin", "");

		if (getActivity().getIntent() != null && getActivity().getIntent().getAction() != null && getActivity().getIntent().getAction().equals(Intent.ACTION_VIEW)) {
			final Uri uri = getActivity().getIntent().getData();

			setPostID(uri.getQueryParameter("id"));
			setStoryID(null);
		}
		else {
			final Bundle extras = getActivity().getIntent().getExtras();
			if (extras == null)
				return;
			
			setPostID(extras.getString("postID"));
			setStoryID(extras.getString("storyID"));
			setIsNWS(extras.getBoolean("isNWS"));
		}
		if (savedInstanceState == null && getPostID() != null) {
			try {
				fillSaxData(getPostID());
			}
			catch (Exception ex) {
				new AlertDialog.Builder(getActivity()).setTitle("Error").setPositiveButton("OK", null).setMessage("There was an error or could not connect to the API.").show();
			}
		}

	}

	public void fillSaxData(String postID) {
		// show a progress dialog
		// getActivity().showDialog(1);
		dialog = ProgressDialog.show(getActivity(), null, "Loading thread...", true, true);

		// use the class run() method to do work
		final Thread thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run() {

		final Comparator<ShackPost> byPostID = new Helper.SortByPostIDComparator();
		final Comparator<ShackPost> byOrderID = new Helper.SortByOrderIDComparator();
		threadLoaded = false;
		try {

			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			String feedURL = prefs.getString("shackFeedURL", getString(R.string.default_api));

			// TODO: Once Squeegy updates his api to work with NWS
			// we can remove this.
			if (getIsNWS())
				feedURL = "http://shackapi.stonedonkey.com";

			final URL url = new URL(feedURL + "/thread/" + getPostID() + ".xml");

			// Get a SAXParser from the SAXPArserFactory.
			final SAXParserFactory spf = SAXParserFactory.newInstance();
			final SAXParser sp = spf.newSAXParser();

			// Get the XMLReader of the SAXParser we created.
			final XMLReader xr = sp.getXMLReader();

			// Create a new ContentHandler and apply it to the XML-Reader
			final SaxHandlerTopicView saxHandler = new SaxHandlerTopicView(getActivity(), "threaded");
			xr.setContentHandler(saxHandler);

			// Parse the xml-data from our URL.
			xr.parse(new InputSource(HttpHelper.HttpRequestWithGzip(url.toString(), getActivity())));

			// Our ExampleHandler now provides the parsed data to us.
			posts = saxHandler.GetParsedPosts();

			// from various portions of the app you can get here without a
			// story id, we retrieve this in the sax call, so mide as well
			// set it here if it's missing
			if (getStoryID() == null)
				setStoryID(saxHandler.getStoryID());

			// the following sorts are what are used to highlight the last ten
			// posts. We add an index to the
			// array by sorting them by post id, then adding the index, then
			// sorting them back
			// sort our posts by PostID
			Collections.sort(posts, byPostID);

			// set the index on them based on order
			ShackPost tempPost = null;
			final int postsSize = posts.size();
			for (int x = 0; x < postsSize; x++) {
				tempPost = posts.get(x);
				tempPost.setPostIndex(x);
			}

			// set the order back to the original sort
			Collections.sort(posts, byOrderID);

		}
		catch (Exception ex) {
			Log.e("ShackDroid", "Unable to parse story " + this.getPostID());
			errorText = "An error occurred connecting to API.";
		}
		threadLoaded = true;

		progressBarHandler.sendEmptyMessage(0);
	}

	private Handler progressBarHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				// getActivity().dismissDialog(1);
				dialog.dismiss();
			}
			catch (Exception ex) {
				// dialog could not be killed for some reason
			}

			// if we are provided a postID that is not the same as the first
			// item we need to find it and setit
			if (posts != null && posts.size() > 0 && getPostID() != null)
				if (getPostID().equalsIgnoreCase(posts.get(0).getPostID()) == false)
					for (int x = 0; x < posts.size(); x++)
						if (posts.get(x).getPostID().equalsIgnoreCase(getPostID())) {
							setCurrentPosition(x);
							break;
						}

			ShowData();
			setMenuVisibility(true);
			UpdateWatchedPosts();

		}
	};

	@SuppressWarnings("unchecked")
	private void UpdateWatchedPosts() {
		// TODO: the opening and saving of the cache file needs to be moved to a
		// func
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
				Log.e("ShackDroid", "Thread Error Loading watch.cache");
			}
		}

		try {
			if (watchCache != null && watchCache.size() > 0 && posts != null) {
				// TODO: Not sure a loop is necessary here
				for (int counter = 0; counter < watchCache.size(); counter++) {
					if (watchCache.get(counter).getPostID().equals(getPostID())) {
						final int replyCount = posts.size() - 1;
						watchCache.get(counter).setOriginalReplyCount(replyCount);
						watchCache.get(counter).setReplyCount(replyCount);
						break;
					}
				}
			}
		}
		catch (Exception ex) {
			Log.e("ShackDroid", "Error UpdateWatchedPosts() in ActivityThreadedView.java");
		}

		try {
			synchronized (Helper.dataLock) {
				final FileOutputStream fos = getActivity().openFileOutput("watch.cache", getActivity().MODE_PRIVATE);
				final ObjectOutputStream os = new ObjectOutputStream(fos);
				os.writeObject(watchCache);
				os.close();
				fos.close();
			}
		}
		catch (Exception e) {
		}

	}

	private void UpdatePostText(int position, Boolean addSpoilerMarkers) {
		if (posts == null || posts.size() < position - 1) // can't bind an empty list of posts
			return;

		final TextView tv = (TextView) getActivity().findViewById(R.id.TextViewPost);
		final String postText = Helper.ParseShackText(posts.get(position).getPostText(), addSpoilerMarkers);

		final Spanned parsedText = Html.fromHtml(postText, imgGetter, new TagHandler() {
			int startPos = 0;

			@Override
			public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {

				if (tag.equals("s")) {
					if (opening) {
						startPos = output.length();
					}
					else {
						final StrikethroughSpan strike = new StrikethroughSpan();
						/*
						 * // This doesn't work and the colour is really dark normally :( TextPaint p = new TextPaint(); p.setStrokeWidth(2); p.setColor(Color.RED); p.setFlags(TextPaint.STRIKE_THRU_TEXT_FLAG); strike.updateDrawState(p);
						 */
						output.setSpan(strike, startPos, output.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
				}

			}
		});

		ScrollView sv = (ScrollView) getActivity().findViewById(R.id.textAreaScroller);
		if (sv != null) {
			sv.scrollTo(0, 0);
		}

		if (tv != null) {
			tv.setText(parsedText, BufferType.NORMAL);
		}
		// tv.setText(Html.fromHtml(postText),BufferType.SPANNABLE);

		if (addSpoilerMarkers == true)
			SpoilerTextView();

		// TODO: This was causing links to break for some reason, for instance
		// http://pancake_humper.shackspace.com/
		// it removes the pancake_ and goes to http://humper.shackspace... bug
		// in linkify maybe??
		Linkify.addLinks(tv, Linkify.ALL); // make all hyperlinks clickable
		tv.setClickable(false);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		// tv.scrollTo(0,0);
		tv.requestLayout();

		final ShackPost post = posts.get(position);
		final TextView posterName = (TextView) getActivity().findViewById(R.id.TextViewThreadAuthor);
		posterName.setText(post.getPosterName());

		if (login.equalsIgnoreCase(post.getPosterName()))
			posterName.setTextColor(Color.parseColor("#00BFF3"));
		else
			posterName.setTextColor(Color.parseColor("#0099CC"));

		final TextView postDate = (TextView) getActivity().findViewById(R.id.TextViewThreadViewPostDate);
		// postDate.setText(Helper.FormatShackDate(post.getPostDate()));
		postDate.setText(Helper.FormatShackDateToTimePassed(post.getPostDate()) + " ago");

		final String postCat = post.getPostCategory();
		setPostCategoryIcon(postCat);

		setPostID(post.getPostID());

		ShackDroidStats.AddPostsViewed(getActivity());
	}

	private void ShowData() {

		if (posts != null) {
			// this is where we bind our fancy ArrayList of posts
			final AdapterThreadedView tva = new AdapterThreadedView(getActivity(), R.layout.thread_row, posts, getCurrentPosition());
			setListAdapter(tva);

			if (posts.size() > 0) {
				final ImageView threadTime = (ImageView) getActivity().findViewById(R.id.ImageViewThreadTimer);
				threadTime.setImageResource(Helper.GetTimeLeftDrawable(posts.get(0).getPostDate()));
			}

			UpdatePostText(getCurrentPosition(), true);

			final ListView lv = getListView();
			lv.setSelection(getCurrentPosition());

			lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				@Override
				public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
					menu.setHeaderTitle("Options");
					menu.add(0, 2, 0, "Copy Post Url to Clipboard");
					menu.add(0, 4, 0, "Shacker's Chatty Profile");
					// menu.add(0, -1, 0, "Cancel");
				}
			});

			// set the post background color to be more "shack" like
			// final LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.RelativeLayoutThread);
			// layout.setBackgroundColor(Color.parseColor("#222222"));

			// add a listener for removing spoilers and maybe adding "copy"
			// functionality later
			final TextView tvpost = (TextView) getActivity().findViewById(R.id.TextViewPost);
			tvpost.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				@Override
				public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
					menu.setHeaderTitle("Post Options");
					menu.add(0, 1, 0, "Copy Post Url to Clipboard");
					menu.add(0, 3, 0, "Thread Expires In?");
					menu.add(0, 4, 0, "Shacker's Chatty Profile");
					// menu.add(0, -1, 0, "Cancel"); //unnecessary? with back
					// button and click outside of options..
				}
			});

		}
		else {
			try {
				if (errorText.length() > 0) {
					new AlertDialog.Builder(getActivity()).setTitle("Error").setPositiveButton("OK", null).setMessage(errorText).show();
				}
			}
			catch (Exception ex) {
				// problem throwing up alert

			}
		}

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		TextView threadPreview = null;
		View vi = (View) l.getChildAt(getCurrentPosition() - l.getFirstVisiblePosition());

		if (vi != null)
			threadPreview = (TextView) vi.findViewById(R.id.TextViewThreadPreview);
		if (threadPreview != null)
			threadPreview.setBackgroundColor(Color.TRANSPARENT);

		vi = (View) l.getChildAt(position - l.getFirstVisiblePosition());
		if (vi != null)
			threadPreview = (TextView) vi.findViewById(R.id.TextViewThreadPreview);
		if (threadPreview != null)
			threadPreview.setBackgroundColor(Color.parseColor("#274FD3"));

		// tell our adapter what the current row is, this is used to rehighlight
		// the current topic during scrolling
		final AdapterThreadedView tva = (AdapterThreadedView) getListAdapter();
		tva.setSelectedRow(position);

		setCurrentPosition(position);
		l.setFocusableInTouchMode(true);

		UpdatePostText(position, true);
	}

	private void setListItemPosition(int direction) {

		if ((getCurrentPosition() + direction) < 0)
			return;

		ListView l = getListView();

		if (getCurrentPosition() + direction >= l.getCount())
			return;

		TextView threadPreview = null;

		View vi = (View) l.getChildAt(getCurrentPosition() - l.getFirstVisiblePosition());
		if (vi != null)
			threadPreview = (TextView) vi.findViewById(R.id.TextViewThreadPreview);
		if (threadPreview != null)
			threadPreview.setBackgroundColor(Color.TRANSPARENT);
		else {

		}

		int position = getCurrentPosition();

		final int rows = l.getLastVisiblePosition() - l.getFirstVisiblePosition();

		l.setFocusableInTouchMode(true);
		l.setSelection(position + direction + -(rows / 2));

		l.refreshDrawableState();

		vi = null;
		threadPreview = null; // clear the last thread selection
		vi = (View) l.getChildAt((position + direction) - l.getFirstVisiblePosition());
		if (vi != null)
			threadPreview = (TextView) vi.findViewById(R.id.TextViewThreadPreview);
		if (threadPreview != null)
			threadPreview.setBackgroundColor(Color.parseColor("#274FD3"));

		// tell our adapter what the current row is, this is used to rehighlight
		// the current topic during scrolling
		final AdapterThreadedView tva = (AdapterThreadedView) getListAdapter();
		tva.setSelectedRow(position + direction);

		setCurrentPosition(position + direction);

		UpdatePostText(position + direction, true);

	}

	private void SpoilerTextView() {
		// We have to use the Spannable interface to handle spoilering text
		// not the best but works.
		final TextView tv = (TextView) getActivity().findViewById(R.id.TextViewPost);

		final String text = tv.getText().toString();

		// early out if there are no spoilers in post
		if (text.indexOf("!!-") == -1)
			return;

		// need to allow clicks on this TextView
		tv.setMovementMethod(LinkMovementMethod.getInstance());

		// avoid annoying orange flicker when clicking on spoiler
		tv.setHighlightColor(Color.parseColor("#222222"));

		// replace end marker with start marker so we can split on only one of
		// the markers
		String components[] = text.replaceAll("-!!", "!!-").split("!!-");

		StringBuilder cleanText = new StringBuilder();
		ArrayList<int[]> spoilerSections = new ArrayList<int[]>();

		// build up a list of spoiler sections and a clean version of the post
		// text (no !!- or -!! markers)
		for (int i = 0; i < components.length; i++) {
			if (i % 2 == 1) // odd strings in the list are the spoilers
			{
				// store start and length of spoiler section
				spoilerSections.add(new int[] { cleanText.length(), components[i].length() });
			}

			cleanText.append(components[i]);
		}

		tv.setText(cleanText);

		// add clickable spans to each spoiler section
		Spannable str = (Spannable) tv.getText();
		for (int[] section : spoilerSections)
			str.setSpan(new SpoilerSpan(tv), section[0], section[0] + section[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

	private void setPostCategoryIcon(String postCat) {
		final TextView viewCat = (TextView) getActivity().findViewById(R.id.TextViewThreadModTag);

		viewCat.setVisibility(View.VISIBLE);

		if (postCat.equals("offtopic")) {
			viewCat.setText("offtopic");
			viewCat.setBackgroundColor(Color.parseColor("#444444"));
		}
		else if (postCat.equals("nws")) {
			viewCat.setText("nws");
			viewCat.setBackgroundColor(Color.parseColor("#CC0000"));
		}
		else if (postCat.equals("political")) {
			viewCat.setText("political");
			viewCat.setBackgroundColor(Color.parseColor("#FF8800"));
		}
		else if (postCat.equals("stupid")) {
			viewCat.setText("stupid");
			viewCat.setBackgroundColor(Color.parseColor("#669900"));
		}
		else if (postCat.equals("informative")) {
			viewCat.setText("interesting");
			viewCat.setBackgroundColor(Color.parseColor("#0099CC"));
		}
		else
			viewCat.setVisibility(View.GONE);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId())
		{
		case R.id.ThreadRefresh:
			this.setCurrentPosition(0);
			this.fillSaxData(getPostID());
			return true;
		case R.id.ThreadLOL:
			login = prefs.getString("shackLogin", "");
			HandlerExtendedSites.INFLOLPost(getActivity(), login, getPostID(), "LOL");
			return true;
		case R.id.ThreadINF:
			login = prefs.getString("shackLogin", "");
			HandlerExtendedSites.INFLOLPost(getActivity(), login, getPostID(), "INF");
			return true;
		case R.id.ThreadTag:
			login = prefs.getString("shackLogin", "");
			HandlerExtendedSites.INFLOLPost(getActivity(), login, getPostID(), "TAG");
			return true;
		case R.id.ThreadUNF:
			login = prefs.getString("shackLogin", "");
			HandlerExtendedSites.INFLOLPost(getActivity(), login, getPostID(), "UNF");
			return true;
		case R.id.ThreadReply:
			doReply();
			return true;
		case R.id.ThreadSettings:
			Intent intent = new Intent();
			intent.setClass(getActivity(), ActivityPreferences.class);
			startActivity(intent);
			return true;
		default:
			return false;
		}

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		switch (item.getItemId())
		{
		case 1:
		{
			// http://www.shacknews.com/chatty?id=25445895
			final String url = "http://www.shacknews.com/chatty?id=" + getPostID();
			final ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(getActivity().CLIPBOARD_SERVICE);
			clipboard.setText(url);
			Toast.makeText(getActivity(), "Link to post copied to clipboard.", Toast.LENGTH_SHORT).show();
		}
		case 2:
		{

			String linkID = posts.get(getCurrentPosition()).getPostID();

			// http://www.shacknews.com/laryn.x?id=23005222#itemanchor_23005222
			final String url = "http://www.shacknews.com/chatty?id=" + linkID + "#itemanchor_" + linkID;
			final ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(getActivity().CLIPBOARD_SERVICE);
			clipboard.setText(url);
			Toast.makeText(getActivity(), "Link to post copied to clipboard.", Toast.LENGTH_SHORT).show();
		}
		case 3:
		{
			String datePosted = posts.get(getCurrentPosition()).getPostDate();

			new AlertDialog.Builder(getActivity()).setTitle("Thread Will Expire In:").setMessage(Helper.GetTimeLeftString(datePosted)).setNegativeButton("OK", null).show();

			return true;
		}
		case 4:
		{
			String shackname = posts.get(getCurrentPosition()).getPosterName();

			Intent intent = new Intent();
			intent.putExtra("shackname", shackname);
			intent.setClass(getActivity(), ActivityProfile.class);
			startActivity(intent);

			return true;
		}
		}

		return false;

	}

	private void doReply() {
		Intent intent = new Intent();
		intent.setClass(getActivity(), ActivityPost.class);
		intent.putExtra("storyID", getStoryID());
		intent.putExtra("postID", getPostID());
		startActivityForResult(intent, POST_REPLY);
	}

	private void LaunchNotesIntent() {
		final Intent intent = new Intent();
		intent.setClass(getActivity(), ActivityShackMarks.class);
		startActivity(intent);
	}

	private ImageGetter imgGetter = new Html.ImageGetter() {
		@Override
		public Drawable getDrawable(String source) {
			Drawable drawable = null;

			try {

				InputStream is = (InputStream) new URL(source).getContent();
				drawable = Drawable.createFromStream(is, "youtubes");
				// Important
				drawable.setBounds(0, 0, drawable.getIntrinsicWidth() * 2, drawable.getIntrinsicHeight() * 2);

			}
			catch (Exception ex) {
				// String exc = ex.getMessage();
				// int i = 1;
			}
			return drawable;
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode)
		{
		case POST_REPLY:
			if (resultCode == getActivity().RESULT_OK) // only refresh on posts
				fillSaxData(getPostID());

			break;
		}
	}

	// @Override
	// public boolean dispatchKeyEvent(KeyEvent event) {
	// if (!prefs.getBoolean("enableVolumeNav", false))
	// return super.dispatchKeyEvent(event);
	//
	// int action = event.getAction();
	// int keyCode = event.getKeyCode();
	//
	// switch (keyCode) {
	// case KeyEvent.KEYCODE_VOLUME_UP:
	// if (action == KeyEvent.ACTION_DOWN)
	// setListItemPosition(-1);
	//
	// return true;
	// case KeyEvent.KEYCODE_VOLUME_DOWN:
	// if (action == KeyEvent.ACTION_DOWN)
	// setListItemPosition(1);
	//
	// return true;
	// default:
	// return super.dispatchKeyEvent(event);
	// }
	// }

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// /*
	// ListView lv = getListView();
	//
	//
	//
	// if (keyCode == 54) {
	// if (lv.getCount() >= currentPosition+2 ){
	// currentPosition++;
	// this.UpdatePostText(currentPosition, true);
	// lv.setSelection(currentPosition);
	// }
	//
	// }
	// */
	// if (keyCode == KeyEvent.KEYCODE_BACK && w.isShowing()){
	// w.dismiss();
	// return true;
	// }
	// return super.onKeyDown(keyCode, event);
	// }
	@Override
	public void eventRaised(int eventType) {
		switch (eventType)
		{
		case ShackGestureListener.BACKWARD:
			getActivity().finish();
			break;
		case ShackGestureListener.REFRESH:
			fillSaxData(getPostID());
			break;
		}

	}

	public String getPostID() {
		return postID;
	}

	public void setPostID(String postID) {
		this.postID = postID;
	}

	public String getStoryID() {
		return storyID;
	}

	public void setStoryID(String storyID) {
		this.storyID = storyID;
	}

	public Boolean getIsNWS() {
		return isNWS;
	}

	public void setIsNWS(Boolean isNWS) {
		this.isNWS = isNWS;
	}

	public int getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(int currentPosition) {
		this.currentPosition = currentPosition;
	}
}
