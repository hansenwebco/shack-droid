package com.stonedonkey.shackdroid;

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
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.text.style.BackgroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.util.Linkify;
import android.text.util.Linkify.MatchFilter;
import android.text.util.Linkify.TransformFilter;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.BufferType;

/**
 * @author markh
 *
 */
public class ActivityThreadedView extends ListActivity implements Runnable {

	private static final int POST_REPLY = 0;
	private ArrayList<ShackPost> posts;
	private String storyID;
	private String postID;
	private String errorText = "";
	private int currentPosition = 0;
	private boolean spoilerText= false;
	private Boolean threadLoaded = true;
	private Boolean isNWS = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		Helper.SetWindowState(getWindow(),this);

		setContentView(R.layout.thread);
		this.setTitle("ShackDroid - View Thread");

		if (getIntent() != null && 
				getIntent().getAction() != null && 
				getIntent().getAction().equals(Intent.ACTION_VIEW)){
			Uri uri = getIntent().getData();
			postID = uri.getQueryParameter("id");
			storyID = null; //TODO: Actually get this some how
			
		}		
		else{
			Bundle extras = this.getIntent().getExtras();
			postID = extras.getString("postID");
			storyID = extras.getString("storyID");
			isNWS = extras.getBoolean("isNWS");
		}
		if (savedInstanceState == null) 
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
	@Override
    public void onWindowFocusChanged(boolean hasFocus) { 
		// Adjust the scroll view based on the size of the screen
		// this doesn't account for the titlebar or the statusbar
		// no methods appear to be available to determine them 
		ScrollView sv = (ScrollView) findViewById(R.id.textAreaScroller);
		TextView tv = (TextView)findViewById(R.id.TextViewThreadAuthor);
		
		int statusTitleBar = 0; // TODO: really would like to not hardcode this
		
		int offset = tv.getTotalPaddingTop() + tv.getHeight() +  sv.getTop() ;
		int height = getWindowManager().getDefaultDisplay().getHeight();

		sv.getLayoutParams().height = (height - offset - statusTitleBar) / 2;
		sv.requestLayout();		
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
			ListView lv = getListView();
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
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage("loading, please wait...");
			dialog.setTitle(null);
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			return dialog;
		}
		}
		return null;

	}
	private void fillSaxData(String postID) {
		// show a progress dialog
		showDialog(1);

		// use the class run() method to do work
		Thread thread = new Thread(this); 
		thread.start();
	}

	@Override
	public void run() {

		Comparator<ShackPost> byPostID = new SortByPostIDComparator();
		Comparator<ShackPost> byOrderID = new SortByOrderIDComparator();
		threadLoaded = false;
		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String feedURL = prefs.getString("shackFeedURL", getString(R.string.default_api));
			
			// TODO: Once Squeegy updates his api to work with NWS
			//       we can remove this.
			if (isNWS) 
				feedURL = "http://shackapi.stonedonkey.com";
			
			URL url = new URL(feedURL + "/thread/" + postID	+ ".xml");

			// Get a SAXParser from the SAXPArserFactory.
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			//  Get the XMLReader of the SAXParser we created.
			XMLReader xr = sp.getXMLReader();
			// Create a new ContentHandler and apply it to the XML-Reader
			SaxHandlerTopicView saxHandler = new SaxHandlerTopicView(this,"threaded");
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
			int postsSize = posts.size();
			for (int x=0;x<postsSize;x++)
			{
				tempPost = posts.get(x);
				tempPost.setPostIndex(x);
			}

			// set the order back to the original sort
			Collections.sort(posts,byOrderID);
			
			
		} catch (Exception ex) {
			ex.printStackTrace(System.out);
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
			if (posts != null && posts.size() > 0 )
				if (postID.equalsIgnoreCase(posts.get(0).getPostID()) == false)
					for(int x=0;x<posts.size();x++)
						if (posts.get(x).getPostID().equalsIgnoreCase(postID)){
							currentPosition = x;
							break;
						}


			ShowData();
		}
	};
	
	private void UpdatePostText(int position, Boolean addSpoilerMarkers)
	{
		TextView tv = (TextView) findViewById(R.id.TextViewPost);
		String postText = ParseShackText(posts.get(position).getPostText(),addSpoilerMarkers);
		
		Spanned parsedText = Html.fromHtml(postText,null, new TagHandler(){
			int startPos = 0;
			@Override
			public void handleTag(boolean opening, String tag, Editable output,
					XMLReader xmlReader) {
				
				if(tag.equals("s")){
					if (opening){
						startPos = output.length();
					}
					else{
						StrikethroughSpan strike = new StrikethroughSpan();
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
		
		tv.setText(parsedText,BufferType.SPANNABLE);
		//tv.setText(Html.fromHtml(postText),BufferType.SPANNABLE);

		
		if (addSpoilerMarkers == true) 
			SpoilerTextView();
				// TODO: This was causing links to break for some reason, for instance
		// http://pancake_humper.shackspace.com/
		// it removes the pancake_ and goes to http://humper.shackspace... bug in linkify maybe??
		Linkify.addLinks(tv, Linkify.ALL); // make all hyperlinks clickable

//		Pattern shackURLMatcher = Pattern.compile("href=\"http://www\\.shacknews\\.com/laryn\\.x\\?id=([0-9]*)#itemanchor_([0-9]*)(.*?)\">");
//		String threadView = "content://com.stonedonkey.shackdroid/ActivityThreadedView";
//		Linkify.addLinks(tv,shackURLMatcher,threadView,new ShackURLMatchFilter(), new ShackURLTransform());
//		

		ShackPost post = posts.get(position);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String login = prefs.getString("shackLogin", "");

		int fontSize = Integer.parseInt(prefs.getString("fontSize", "12"));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

		Typeface face = Typeface.createFromAsset(this.getAssets(), "fonts/arial.ttf");
		tv.setTypeface(face);


		TextView posterName = (TextView) findViewById(R.id.TextViewThreadAuthor);
		posterName.setText(post.getPosterName());
		posterName.setTypeface(face);

		if (login.equalsIgnoreCase(post.getPosterName()))
			posterName.setTextColor(Color.parseColor("#00BFF3"));
		else
			posterName.setTextColor(Color.parseColor("#FFBA00"));

		TextView postDate = (TextView) findViewById(R.id.TextViewThreadViewPostDate);
		postDate.setText(Helper.FormatShackDate(post.getPostDate()));
		postDate.setTypeface(face);

		String postCat = post.getPostCategory();
		setPostCategoryIcon(postCat);

		postID = post.getPostID();
		
		ShackDroidStats.AddPostsViewed(this);

	}

	@SuppressWarnings("unchecked")
	private void ShowData() {

		if (posts != null)
		{
			// this is where we bind our fancy ArrayList of posts
			AdapterThreadedView tva = new AdapterThreadedView(this,	R.layout.thread_row, posts,currentPosition);
			setListAdapter(tva);

			UpdatePostText(currentPosition,true);

			ListView lv = getListView();
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
			RelativeLayout layout = (RelativeLayout)findViewById(R.id.RelativeLayoutThread);
			layout.setBackgroundColor(Color.parseColor("#222222"));

			// add a listener for removing spoilers and maybe adding "copy" functionality later
			TextView tvpost = (TextView)findViewById(R.id.TextViewPost);
			tvpost.setOnCreateContextMenuListener(
					new OnCreateContextMenuListener() {
						@Override
						public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
							menu.setHeaderTitle("Post Options");
							menu.add(0,1,0,"Copy Post Url to Clipboard");
							if (spoilerText == true) {
								menu.add(0, 10, 0, "Remove Spoiler");
								//menu.add(0, 11, 0, "Copy Text"); // might be useful one day
							}
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
		AdapterThreadedView tva = (AdapterThreadedView) getListAdapter();
		tva.setSelectedRow(position);

		currentPosition = position;
		l.setFocusableInTouchMode(true);
		//l.setChoiceMode(1);
		//l.setItemChecked(position, true);
		//l.setSelection(position);

		ScrollView sv = (ScrollView) findViewById(R.id.textAreaScroller);
		sv.scrollTo(0, 0); 

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
		TextView tv = (TextView) findViewById(R.id.TextViewPost);
		String text = tv.getText().toString();
		int end = 0;
		while (text.indexOf("!!-",end) >= 0)
		{
			int start = text.indexOf("!!-",end);
			end = text.indexOf("-!!",start);
			Spannable str = (Spannable) tv.getText();
			str.setSpan(new BackgroundColorSpan(Color.parseColor("#383838")), start, end+3,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			spoilerText = true;
		}
	}

	private void setPostCategoryIcon(String postCat)
	{
		ImageView img = (ImageView)findViewById(R.id.ImageViewCatTopic);
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
		Intent intent;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String login = "";
		switch (item.getItemId()) {
		case 0:	// Launch post form
			intent = new Intent();
			intent.setClass(this, ActivityPost.class);
			intent.putExtra("storyID", storyID);
			intent.putExtra("postID",postID);
			startActivityForResult(intent,POST_REPLY);
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
		switch (item.getItemId())
		{
			case 10: 
				RemoveSpoiler();
				return true;
			case 1: {
				//http://www.shacknews.com/laryn.x?id=23004466
				String url = "http://www.shacknews.com/laryn.x?id=" + postID;
				ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				clipboard.setText(url);
				Toast.makeText(this, "Link to post copied to clipboard.", Toast.LENGTH_SHORT).show();
			}
			case 2: {
				
				String linkID = posts.get(currentPosition).getPostID();
				
				//http://www.shacknews.com/laryn.x?id=23005222#itemanchor_23005222
				String url = "http://www.shacknews.com/laryn.x?id=" + linkID + "#itemanchor_" + linkID;
				ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				clipboard.setText(url);
				Toast.makeText(this, "Link to post copied to clipboard.", Toast.LENGTH_SHORT).show();
			}
		}
		
	return false;

	}


	private void LaunchNotesIntent()
	{
		Intent intent = new Intent();
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
		return super.onKeyDown(keyCode, event);
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

