package com.stonedonkey.shackdroid;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;

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
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.Html.TagHandler;
import android.text.method.ScrollingMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.util.Linkify;
import android.text.util.Linkify.MatchFilter;
import android.text.util.Linkify.TransformFilter;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.BufferType;

import com.stonedonkey.shackdroid.ShackGestureListener.ShackGestureEvent;
import com.stonedonkey.shackdroid.ShackPopup.ShackPopupEvent;

/**
 * @author markh
 *
 */
public class ActivityThreadedView extends ListActivity implements Runnable, ShackGestureEvent, ShackPopupEvent {

	private static final int POST_REPLY = 0;
	private ArrayList<ShackPost> posts;
	private String storyID;
	private String postID;
	private String errorText = "";
	private int currentPosition = 0;
	private boolean spoilerText= false;
	private Boolean threadLoaded = true;
	private Boolean isNWS = false;

	private  SharedPreferences prefs;// = PreferenceManager.getDefaultSharedPreferences(this);
	private  String login;// = prefs.getString("shackLogin", "");	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		Helper.SetWindowState(getWindow(),this);
		
		boolean screenOn = PreferenceManager.getDefaultSharedPreferences(this)
							.getBoolean("keepScreenOn", false);
		if (screenOn){
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		
		ShackGestureListener listener = Helper.setGestureEnabledContentView(R.layout.thread, this);
		if (listener != null){
			listener.addListener(this);
		}
		
		
		this.setTitle("ShackDroid - View Thread");
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		login = prefs.getString("shackLogin", "");
		int fontSize = Integer.parseInt(prefs.getString("fontSize", "12"));
		Typeface face = Typeface.createFromAsset(this.getAssets(), "fonts/arial.ttf");
		
		TextView tv = (TextView)findViewById(R.id.TextViewPost);
		TextView posterName = (TextView)findViewById(R.id.TextViewThreadAuthor);
		TextView postDate = (TextView)findViewById(R.id.TextViewThreadViewPostDate);
		
		tv.setMovementMethod(new ScrollingMovementMethod());
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
		tv.setTypeface(face);
		posterName.setTypeface(face);
		postDate.setTypeface(face);
		
		if (getIntent() != null && 
				getIntent().getAction() != null && 
				getIntent().getAction().equals(Intent.ACTION_VIEW)){
			final Uri uri = getIntent().getData();

			postID = uri.getQueryParameter("id");
			storyID = null; //TODO: Actually get this some how
			
		}		
		else{
			final Bundle extras = this.getIntent().getExtras();
			postID = extras.getString("postID");
			storyID = extras.getString("storyID");
			isNWS = extras.getBoolean("isNWS");
		}
		if (savedInstanceState == null){ 
			try {
			fillSaxData(postID);
			}
			catch (Exception ex)
			{
				new AlertDialog.Builder(this).setTitle("Error")
				.setPositiveButton("OK", null).setMessage(
						"There was an error or could not connect to the API.")
				.show();
			}
		}

		pop = new ShackPopup();
		pop.addListener(this);
		w = pop.Init(this, w);
		
		// disabling for now.
		ImageView b = (ImageView)findViewById(R.id.ivPopupButton);
		b.setVisibility(View.GONE);
		b =null;
		if (b != null){
			//b.setVisibility(View.GONE);
			
			b.setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View arg0) {
					if (w.isShowing()){
						w.dismiss();
					}
					else{
						int wi = getWindowManager().getDefaultDisplay().getWidth();
						int hi = getWindowManager().getDefaultDisplay().getHeight();
						int extra = (hi>wi?24:-2);
						
						if (pop.isSmallScreen){
							if (extra >0){
								extra -= 30;
							}
							wi = (wi/2) - 120;
							hi = ((hi/2) - 64) - extra;							
						}
						else{
							wi = (wi/2) - 240;
							hi = ((hi/2) - 130) - extra;
						}
						w.showAtLocation(arg0, Gravity.NO_GRAVITY, wi, hi);
						//w.showAsDropDown(arg0, 0, -2);
					}
				}});
			
			
		}
	}
	ShackPopup pop;
	PopupWindow w;
	@Override
    public void onWindowFocusChanged(boolean hasFocus) { 
		// Adjust the scroll view based on the size of the screen
		// this doesn't account for the titlebar or the statusbar
		// no methods appear to be available to determine them 
		final TextView sv = (TextView) findViewById(R.id.TextViewPost);
		final TextView tv = (TextView)findViewById(R.id.TextViewThreadAuthor);
		
		final int statusTitleBar = 0; // TODO: really would like to not hardcode this
		
		final int offset = tv.getTotalPaddingTop() + tv.getHeight() +  sv.getTop() ;
		final int height = getWindowManager().getDefaultDisplay().getHeight();

		sv.getLayoutParams().height = (height - offset - statusTitleBar) / 2;
		sv.requestLayout();		
		
		final TextView spacer = (TextView) findViewById(R.id.tvSpacer);
		spacer.setBackgroundColor(Color.parseColor("#333333"));
		
	}

	@Override 
	public void onSaveInstanceState(Bundle savedInstanceState)
	{

		try {
			dismissDialog(1);
		} catch (Exception ex) {
			// dialog could not be killed for some reason
		}

		savedInstanceState.putSerializable("posts", posts);
		savedInstanceState.putString("storyID", storyID);
		savedInstanceState.putString("postID", postID);
		savedInstanceState.putInt("currentPosition", currentPosition);
		savedInstanceState.putBoolean("threadLoaded", threadLoaded);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) 
	{
		posts = (ArrayList<ShackPost>) savedInstanceState.getSerializable("posts");
		storyID = savedInstanceState.getString("storyID");
		postID = savedInstanceState.getString("postID");
		currentPosition = savedInstanceState.getInt("currentPosition");
		threadLoaded = savedInstanceState.getBoolean("threadLoaded");


		if (threadLoaded == true && posts != null) { 
			ShowData();
			final ListView lv = getListView();
			lv.setSelection(currentPosition);
		}
		else
		{
			fillSaxData(postID);
		}

		savedInstanceState.clear();

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

			dialog.setOnCancelListener(new OnCancelListener(){
				@Override
				public void onCancel(DialogInterface arg0) {
					finish();
				}
			});
			
			return dialog;
		}
		}
		return null;

	}
	private void fillSaxData(String postID) {
		// show a progress dialog
		showDialog(1);

		// use the class run() method to do work
		final Thread thread = new Thread(this); 
		thread.start();
	}

	@Override
	public void run() {

		final Comparator<ShackPost> byPostID = new SortByPostIDComparator();
		final Comparator<ShackPost> byOrderID = new SortByOrderIDComparator();
		threadLoaded = false;
		try {
			
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String feedURL = prefs.getString("shackFeedURL", getString(R.string.default_api));
			
			// TODO: Once Squeegy updates his api to work with NWS
			//       we can remove this.
			if (isNWS) 
				feedURL = "http://shackapi.stonedonkey.com";
			
			final URL url = new URL(feedURL + "/thread/" + postID	+ ".xml");

			// Get a SAXParser from the SAXPArserFactory.
			final SAXParserFactory spf = SAXParserFactory.newInstance();
			final SAXParser sp = spf.newSAXParser();

			//  Get the XMLReader of the SAXParser we created.
			final XMLReader xr = sp.getXMLReader();
			// Create a new ContentHandler and apply it to the XML-Reader
			final SaxHandlerTopicView saxHandler = new SaxHandlerTopicView(this,"threaded");
			xr.setContentHandler(saxHandler);

			// Parse the xml-data from our URL.
			xr.parse(new InputSource(HttpHelper.HttpRequestWithGzip(url.toString(),this)));

			// Our ExampleHandler now provides the parsed data to us.
			posts = saxHandler.GetParsedPosts();

			// from various portions of the app you can get here without a 
			// story id, we retrieve this in the sax call, so mide as well
			// set it here if it's missing
			if (storyID == null)
				storyID = saxHandler.getStoryID();
			 
			// the following sorts are what are used to highlight the last ten posts.  We add an index to the
			// array by sorting them by post id, then adding the index, then sorting them back	
			// sort our posts by PostID
			Collections.sort(posts,byPostID);

			// set the index on them based on order
			ShackPost tempPost = null;
			final int postsSize = posts.size();
			for (int x=0;x<postsSize;x++)
			{
				tempPost = posts.get(x);
				tempPost.setPostIndex(x);
			}

			// set the order back to the original sort
			Collections.sort(posts,byOrderID);
			
			
		} catch (Exception ex) {
			Log.e("ShackDroid", "Unable to parse story " + this.postID);
			errorText = "An error occurred connecting to API.";
		}
		threadLoaded = true;


		progressBarHandler.sendEmptyMessage(0);
	}

	private Handler progressBarHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				dismissDialog(1);
			}
			catch (Exception ex)
			{
				// dialog could not be killed for some reason
			}

			// if we are provided a postID that is not the same as the first
			// item we need to find it and setit
			if (posts != null && posts.size() > 0 && postID != null)
				if (postID.equalsIgnoreCase(posts.get(0).getPostID()) == false)
					for(int x=0;x<posts.size();x++)
						if (posts.get(x).getPostID().equalsIgnoreCase(postID)){
							currentPosition = x;
							break;
						}


			ShowData();
			UpdateWatchedPosts();
		}
	};
	
	@SuppressWarnings("unchecked")
	private void UpdateWatchedPosts()
	{
		// TODO: the opening and saving of the cache file needs to be moved to a func
		ArrayList<ShackPost> watchCache = null;
		if (getFileStreamPath("watch.cache").exists()) {

			try {
				final FileInputStream fileIn = openFileInput("watch.cache");
				final ObjectInputStream in = new ObjectInputStream(fileIn);
				watchCache = (ArrayList<ShackPost>)in.readObject();
				in.close();
				fileIn.close();
			}
			catch (Exception ex){ Log.e("ShackDroid", "Thread Error Loading watch.cache"); }
		}


		if (watchCache != null && watchCache.size() > 0)
		{
			// TODO: Not sure a loop is neccessary here
			for(int counter= 0; counter < watchCache.size();counter++) {
				if (watchCache.get(counter).getPostID().equals(postID))
				{
					final int replyCount = posts.size() - 1;
					watchCache.get(counter).setOriginalReplyCount(replyCount);
					watchCache.get(counter).setReplyCount(replyCount);
					break;
				}
			}
		}
		try {
			final FileOutputStream fos = openFileOutput("watch.cache",MODE_PRIVATE);
			final ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(watchCache);
			os.close();
			fos.close();
		} catch (Exception e) {
		}


	}
	private void UpdatePostText(int position, Boolean addSpoilerMarkers)
	{
		if (posts == null) // can't bind an empty list of posts
			return; 
		
		final TextView tv = (TextView) findViewById(R.id.TextViewPost);
		final String postText = ParseShackText(posts.get(position).getPostText(),addSpoilerMarkers);
		
		final Spanned parsedText = Html.fromHtml(postText,null, new TagHandler(){
			int startPos = 0;
			@Override
			public void handleTag(boolean opening, String tag, Editable output,
					XMLReader xmlReader) {
				
				if(tag.equals("s")){
					if (opening){
						startPos = output.length();
					}
					else{
						final StrikethroughSpan strike = new StrikethroughSpan();
						/*
						// This doesn't work and the colour is really dark normally :(
						TextPaint p = new TextPaint();
						p.setStrokeWidth(2);
						p.setColor(Color.RED);
						p.setFlags(TextPaint.STRIKE_THRU_TEXT_FLAG);
						strike.updateDrawState(p);
						*/
						output.setSpan(strike, startPos, output.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
				}
				
			}});
		
		if (tv != null)
			tv.setText(parsedText,BufferType.NORMAL);
		//tv.setText(Html.fromHtml(postText),BufferType.SPANNABLE);

		if (addSpoilerMarkers == true) 
			SpoilerTextView();
				// TODO: This was causing links to break for some reason, for instance
		// http://pancake_humper.shackspace.com/
		// it removes the pancake_ and goes to http://humper.shackspace... bug in linkify maybe??
		Linkify.addLinks(tv, Linkify.ALL); // make all hyperlinks clickable
		tv.setClickable(false);
		tv.scrollTo(0,0);
		tv.requestLayout();		
//		Pattern shackURLMatcher = Pattern.compile("href=\"http://www\\.shacknews\\.com/laryn\\.x\\?id=([0-9]*)#itemanchor_([0-9]*)(.*?)\">");
//		String threadView = "content://com.stonedonkey.shackdroid/ActivityThreadedView";
//		Linkify.addLinks(tv,shackURLMatcher,threadView,new ShackURLMatchFilter(), new ShackURLTransform());
//		
		/*
		Log.i("height", String.valueOf(tv.getHeight()));
		tv.forceLayout();
		findViewById(R.id.textAreaScroller).forceLayout();
		Log.i("height", String.valueOf(tv.getHeight()));
		*/
		final ShackPost post = posts.get(position);
		final TextView posterName = (TextView) findViewById(R.id.TextViewThreadAuthor);
		posterName.setText(post.getPosterName());
		

		if (login.equalsIgnoreCase(post.getPosterName()))
			posterName.setTextColor(Color.parseColor("#00BFF3"));
		else
			posterName.setTextColor(Color.parseColor("#FFBA00"));

		final TextView postDate = (TextView) findViewById(R.id.TextViewThreadViewPostDate);
		postDate.setText(Helper.FormatShackDate(post.getPostDate()));

		final String postCat = post.getPostCategory();
		setPostCategoryIcon(postCat);

		postID = post.getPostID();

		
		ShackDroidStats.AddPostsViewed(this);
	}

	@SuppressWarnings("unchecked")
	private void ShowData() {

		if (posts != null)
		{
			// this is where we bind our fancy ArrayList of posts
			final AdapterThreadedView tva = new AdapterThreadedView(this,	R.layout.thread_row, posts,currentPosition);
			setListAdapter(tva);

			UpdatePostText(currentPosition,true);

			final ListView lv = getListView();
			lv.setSelection(currentPosition);

			
			lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			    @Override
			    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
			      menu.setHeaderTitle("Options");
			      menu.add(0, 2, 0, "Copy Post Url to Clipboard");
			      menu.add(0,-1,0,"Cancel");
			    }
			  }); 
			
			
			// set the post background color to be more "shack" like
			final RelativeLayout layout = (RelativeLayout)findViewById(R.id.RelativeLayoutThread);
			layout.setBackgroundColor(Color.parseColor("#222222"));
			
			// add a listener for removing spoilers and maybe adding "copy" functionality later
			final TextView tvpost = (TextView)findViewById(R.id.TextViewPost);
			tvpost.setOnCreateContextMenuListener(
					new OnCreateContextMenuListener() {
						@Override
						public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
							menu.setHeaderTitle("Post Options");
							
							if (spoilerText == true) {
								menu.add(0, 10, 0, "Remove Spoiler");
								//menu.add(0, 11, 0, "Copy Text"); // might be useful one day
							}
							menu.add(0,1,0,"Copy Post Url to Clipboard");
							menu.add(0, -1, 0, "Cancel");
						}
					});
			

		}
		else
		{
			if (errorText.length() > 0) {
				new AlertDialog.Builder(this).setTitle("Error").setPositiveButton("OK", null)
				.setMessage(errorText).show();
			}
		}

	}


	@SuppressWarnings("unchecked")
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		w.dismiss();

		// NOTE: ListView's don't show the current selection when in TouchMode
		//       so horray for hacks... because this is "intended" behavior.

		TextView threadPreview = null;
		View vi = (View) l.getChildAt(currentPosition - l.getFirstVisiblePosition());

		if (vi != null)
			threadPreview = (TextView)vi.findViewById(R.id.TextViewThreadPreview);
		if (threadPreview != null)
			threadPreview.setBackgroundColor(Color.TRANSPARENT);

		vi = (View) l.getChildAt(position - l.getFirstVisiblePosition());
		if (vi != null)
			threadPreview = (TextView)vi.findViewById(R.id.TextViewThreadPreview);
		if (threadPreview != null)
			threadPreview.setBackgroundColor(Color.parseColor("#274FD3"));

		// tell our adapter what the current row is, this is used to rehighlight
		// the current topic during scrolling
		final AdapterThreadedView tva = (AdapterThreadedView) getListAdapter();
		tva.setSelectedRow(position);

		currentPosition = position;
		l.setFocusableInTouchMode(true);
		//l.setChoiceMode(1);
		//l.setItemChecked(position, true);
		//l.setSelection(position);

		//final ScrollView sv = (ScrollView) findViewById(R.id.textAreaScroller);
		//sv.scrollTo(0, 0); 

		UpdatePostText(position,true);
	}
	private void RemoveSpoiler()
	{
		UpdatePostText(currentPosition,false);
	}
	private void SpoilerTextView()
	{
		// We have to use the Spannable interface to handle spoilering text
		// not the best but works.
		spoilerText = false;
		final TextView tv = (TextView) findViewById(R.id.TextViewPost);
		final String text = tv.getText().toString();
		int end = 0;
		while (text.indexOf("!!-",end) >= 0)
		{
			final int start = text.indexOf("!!-",end);
			end = text.indexOf("-!!",start);
			Spannable str = (Spannable) tv.getText();
			str.setSpan(new BackgroundColorSpan(Color.parseColor("#383838")), start, end+3,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			spoilerText = true;
		}
	}

	private void setPostCategoryIcon(String postCat)
	{
		final ImageView img = (ImageView)findViewById(R.id.ImageViewCatTopic);
		//TODO: clean this up a little / also duplicated in Topic View Adapter ick
		if (postCat.equals("offtopic")) 
			img.setImageResource(R.drawable.offtopic);
		else if (postCat.equals("nws"))
			img.setImageResource(R.drawable.nws);
		else if (postCat.equals("political")) 
			img.setImageResource(R.drawable.political);
		else if (postCat.equals("stupid")) 
			img.setImageResource(R.drawable.stupid);
		else if (postCat.equals("informative"))
			img.setImageResource(R.drawable.interesting);	
		else{
			// chazums, commented out as throwing an exception
			// img.setImageResource(-1);
			img.setImageDrawable(null);
		}
	}
	private String ParseShackText(String text,boolean addSpoilerMarkers) {

		//Convert the shack spans into HTML fonts since our TextView can convert stuff to HTML
		// not sure if this is the best or most efficient, but works.
		text = text.replaceAll("<span class=\"jt_red\">(.*?)</span>", "<font color=\"#ff0000\">$1</font>");	
		text = text.replaceAll("<span class=\"jt_green\">(.*?)</span>",	"<font color=\"#8dc63f\">$1</font>");
		text = text.replaceAll("<span class=\"jt_pink\">(.*?)</span>", "<font color=\"#f49ac1\">$1</font>");
		text = text.replaceAll("<span class=\"jt_olive\">(.*?)</span>",	"<font color=\"#808000\">$1</font>");
		text = text.replaceAll("<span class=\"jt_fuchsia\">(.*?)</span>", "<font color=\"#c0ffc0\">$1</font>");
		text = text.replaceAll("<span class=\"jt_yellow\">(.*?)</span>", "<font color=\"#ffde00\">$1</font>");
		text = text.replaceAll("<span class=\"jt_blue\">(.*?)</span>", "<font color=\"#44aedf\">$1</font>");
		text = text.replaceAll("<span class=\"jt_lime\">(.*?)</span>",	"<font color=\"#c0ffc0\">$1</font>");
		text = text.replaceAll("<span class=\"jt_orange\">(.*?)</span>", "<font color=\"#f7941c\">$1</font>");
		text = text.replaceAll("<span class=\"jt_bold\">(.*?)</span>", "<b>$1</b>");
		text = text.replaceAll("<span class=\"jt_italic\">(.*?)</span>", "<i>$1</i>");
		text = text.replaceAll("<span class=\"jt_underline\">(.*?)</span>", "<u>$1</u>");
		text = text.replaceAll("<span class=\"jt_strike\">(.*?)</span>", "<s>$1</s>");

		// You can only do "highlights" on the actual TextView itself, so we mark up spoilers 
		// !!-text-!! like so, and then handle it on the appling text to the TextView
		if (addSpoilerMarkers == true) {
			text = text.replaceAll("<span class=\"jt_spoiler\"(.*?)>(.*?)</span>",
			"<font color=\"#383838\">!!-$2-!!</font>");
		}
		return text;
	}

	// menu creation
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(1, 0, 4, "Reply").setIcon(R.drawable.menu_reply);
		//menu.add(1, 1, 1, "Settings").setIcon(R.drawable.menu_settings);
		menu.add(1, 2, 1, "Back").setIcon(R.drawable.menu_back);
		menu.add(1, 3, 2, "Refresh").setIcon(R.drawable.menu_reload);

		SubMenu sub = menu.addSubMenu(1, 4, 3, "LOL/INF/UNF/TAG").setIcon(R.drawable.menu_lolinf);
		sub.add(0,8,0,"LOL Post");
		sub.add(0,9,1, "INF Post");
		sub.add(0,11,1, "UNF Post");
		sub.add(0,12,1, "TAG Post");
		sub.add(0,10,2, "Cancel");

		//sub = menu.addSubMenu(1, 5, 5, "ShackMarks").setIcon(R.drawable.menu_mark);
		//sub.add(0,6,0,"View saved ShackMarks");
		//sub.add(0,7,1, "Save to ShackMarks").setIcon(R.drawable.menu_shacktags);

		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		w.dismiss();
		Intent intent;
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String login = "";
		switch (item.getItemId()) {
		case 0:	// Launch post form
			doReply();
			return true;
		case 1:
			// show settings dialog
			intent = new Intent();
			intent.setClass(this, ActivityPreferences.class);
			startActivity(intent);
			return true;
		case 2:
			finish();
			return true;
		case 3:
			this.currentPosition =0;
			this.fillSaxData(postID);
			return true;
		case 6:
			LaunchNotesIntent();
			return true;
		case 7: //sub menu for ShackMarks
			HandlerExtendedSites.AddRemoveShackMark(this, postID,false);
			return true;
		case 8:
			// lol post
			login = prefs.getString("shackLogin", "");	
			HandlerExtendedSites.INFLOLPost(this,login,postID,"LOL");
			return true;
		case 9:
			// inf post
			login = prefs.getString("shackLogin", "");	
			HandlerExtendedSites.INFLOLPost(this,login,postID,"INF");
			return true;
		case 10: // cancel lol/unf/tag/ing
			return true;
		case 11:
			// unf post
			login = prefs.getString("shackLogin", "");	
			HandlerExtendedSites.INFLOLPost(this,login,postID,"UNF");
			return true;
		case 12:
			// tag post
			login = prefs.getString("shackLogin", "");	
			HandlerExtendedSites.INFLOLPost(this,login,postID,"TAG");
			return true;
			
		}
		return false;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		w.dismiss();
		switch (item.getItemId())
		{
			case 10: 
				RemoveSpoiler();
				return true;
			case 1: {
				//http://www.shacknews.com/laryn.x?id=23004466
				final String url = "http://www.shacknews.com/laryn.x?id=" + postID;
				final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				clipboard.setText(url);
				Toast.makeText(this, "Link to post copied to clipboard.", Toast.LENGTH_SHORT).show();
			}
			case 2: {
				
				String linkID = posts.get(currentPosition).getPostID();
				
				//http://www.shacknews.com/laryn.x?id=23005222#itemanchor_23005222
				final String url = "http://www.shacknews.com/laryn.x?id=" + linkID + "#itemanchor_" + linkID;
				final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				clipboard.setText(url);
				Toast.makeText(this, "Link to post copied to clipboard.", Toast.LENGTH_SHORT).show();
			}
		}
		
	return false;

	}


	private void doReply(){
		Intent intent = new Intent();
		intent.setClass(this, ActivityPost.class);
		intent.putExtra("storyID", storyID);
		intent.putExtra("postID",postID);
		startActivityForResult(intent,POST_REPLY);		
	}
	
	private void LaunchNotesIntent()
	{
		final Intent intent = new Intent();
		intent.setClass(this, ActivityShackMarks.class);
		startActivity(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch(requestCode){
		case POST_REPLY:
			if (resultCode == RESULT_OK) // only refresh on posts
				fillSaxData(postID);

			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/*	
		ListView lv = getListView();



		if (keyCode == 54) {
			if (lv.getCount() >= currentPosition+2 ){
				currentPosition++;
				this.UpdatePostText(currentPosition, true);
				lv.setSelection(currentPosition);
			}

		}
		 */
		if (keyCode == KeyEvent.KEYCODE_BACK && w.isShowing()){
			w.dismiss();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	public void eventRaised(int eventType) {
		switch(eventType){
			case ShackGestureListener.BACKWARD:
				finish();
				break;
			case ShackGestureListener.REFRESH:
				fillSaxData(postID);
				break;
		}
		
	}

	@Override
	public void PopupEventRaised(int eventType) {
		
		switch(eventType){
		case ShackPopup.MESSAGE:
			ShackPost post = posts.get(currentPosition);//.getPostText();
			Intent intent = new Intent();
			intent.putExtra("postto", post.getPosterName());
			intent.setClass(this, ActivityPostMessage.class);
			startActivity(intent);			
			break;
		case ShackPopup.REFRESH:
			fillSaxData(postID);
			break;
		case ShackPopup.REPLY:
			doReply();
			break;
		case ShackPopup.SPOIL:
			RemoveSpoiler();
			break;
		}
		w.dismiss();
	}
}/**
 * Used to sort the post array based on the ID
 */

class SortByPostIDComparator implements Comparator<ShackPost>
{
	@Override
	public int compare(ShackPost object1, ShackPost object2) {
		return Integer.valueOf(object2.getPostID()) - Integer.valueOf(object1.getPostID());
	}

	/**
	 * Used to sort the post array based on the OrderID
	 */}
class SortByOrderIDComparator implements Comparator<ShackPost>
{
	@Override
	public int compare(ShackPost object1, ShackPost object2) {
		return object1.getOrderID() - object2.getOrderID();
	}

}
class ShackURLMatchFilter implements MatchFilter {
	@Override
	public boolean acceptMatch(CharSequence s, int start, int end) {
		return true;
	}
}
class ShackURLTransform implements TransformFilter {

	@Override
	public String transformUrl(Matcher match, String url) {

		//String test = url;
		
		return null;
	}
}

