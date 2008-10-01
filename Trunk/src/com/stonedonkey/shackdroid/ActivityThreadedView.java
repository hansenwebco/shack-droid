package com.stonedonkey.shackdroid;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.text.util.Linkify;
import android.view.ContextMenu;
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
	private ProgressDialog pd;
	private String errorText = "";
	private int currentPosition = 0;
	private boolean spoilerText= false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.thread);
		this.setTitle("ShackDroid - View Thread");
		
		Bundle extras = this.getIntent().getExtras();
		postID = extras.getString("postID");
		storyID = extras.getString("storyID");
	
		fillSaxData(postID);

	}

	private void fillSaxData(String postID) {
		// show a progress dialog
		pd = ProgressDialog.show(this, null, "Loading thread...", true,	true); 
		
		// use the class run() method to do work
		Thread thread = new Thread(this); 
		thread.start();
	}
	
	@Override
	public void run() {
		
		Comparator<ShackPost> byPostID = new SortByPostIDComparator();
		Comparator<ShackPost> byOrderID = new SortByOrderIDComparator();
		
		try {
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String feedURL = prefs.getString("shackFeedURL", "http://shackchatty.com");
			URL url = new URL(feedURL + "/thread/" + postID	+ ".xml");

		//* Get a SAXParser from the SAXPArserFactory./
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

		//  Get the XMLReader of the SAXParser we created./
			XMLReader xr = sp.getXMLReader();
			//* Create a new ContentHandler and apply it to the XML-Reader/
			SaxHandlerTopicView saxHandler = new SaxHandlerTopicView(this);
			xr.setContentHandler(saxHandler);

		//* Parse the xml-data from our URL./
			xr.parse(new InputSource(url.openStream()));
		

		//* Our ExampleHandler now provides the parsed data to us./
			posts = saxHandler.GetParsedPosts();
		
			// the folowing sorts are what are used to highlight the last ten posts.  We add an index to the
			// array by sorting them by post id, then adding the index, then sorting them back	
			// sort our posts by PostID
			Collections.sort(posts,byPostID);
			
			// set the index on them based on order
			ShackPost tempPost = null;
			for (int x=0;x<posts.size();x++)
			{
				tempPost = posts.get(x);
				tempPost.setPostIndex(x);
			}
			
			// set the order back to the orginal sort
			Collections.sort(posts,byOrderID);

		} catch (Exception ex) {
			ex.printStackTrace(System.out);
			errorText = "An error occurred connecting to API.";
		}
		progressBarHandler.sendEmptyMessage(0);
	}
	
	private Handler progressBarHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// we implement a handler because most UI items 
			// won't update within a thread
			pd.dismiss();
			ShowData();
		}
	};
	
	private void UpdatePostText(int position, Boolean addSpoilerMarkers)
	{
		TextView tv = (TextView) findViewById(R.id.TextViewPost);
		String postText = ParseShackText(posts.get(position).getPostText(),addSpoilerMarkers);
		tv.setText(Html.fromHtml(postText),BufferType.EDITABLE);
	
			if (addSpoilerMarkers == true)
			SpoilerTextView();
			Linkify.addLinks(tv, Linkify.ALL); // make all hyperlinks clickable
		
		ShackPost post = posts.get(position);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String login = prefs.getString("shackLogin", "");		
	
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
		postDate.setText(post.getPostDate());
		postDate.setTypeface(face);

		String postCat = post.getPostCategory();
		setPostCategoryIcon(postCat);
	}
	
	private void ShowData() {
	
		if (posts != null)
		{
		// this is where we bind our fancy ArrayList of posts
		AdapterThreadedView tva = new AdapterThreadedView(this,	R.layout.thread_row, posts);
		setListAdapter(tva);

		UpdatePostText(0,true);
		
		// set the post background color to be more "shack" like
		RelativeLayout layout = (RelativeLayout)findViewById(R.id.RelativeLayoutThread);
		layout.setBackgroundColor(Color.parseColor("#222222"));
		
		// add a listner for removing spoilers and maybe adding "copy" functionality later
		TextView tvpost = (TextView)findViewById(R.id.TextViewPost);
		tvpost.setOnCreateContextMenuListener(
				new OnCreateContextMenuListener() {
					@Override
					public void onCreateContextMenu(ContextMenu menu, View v,
							ContextMenuInfo menuInfo) {
						if (spoilerText == true) {
						menu.setHeaderTitle("Post Options");
						menu.add(0, 10, 0, "Remove Spoiler");
						//menu.add(0, 11, 0, "Copy Text"); // might be useful one day
						menu.add(0, -1, 0, "Cancel");
						}
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


	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

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
		while (text.indexOf("!!-",end) > 0)
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
		else
			img.setImageResource(-1);
	}
	private String ParseShackText(String text,boolean addSpoilerMarkers) {

		//Convert the shack spans into HTML fonts since our TextView can convert stuff to HTML
		// not sure if this is the best or most efficent, but works.e
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

		menu.add(1, 0, 3, "Reply").setIcon(R.drawable.menu_reply);
		menu.add(1, 1, 1, "Settings").setIcon(R.drawable.menu_settings);
		menu.add(1, 2, 2, "Back").setIcon(R.drawable.menu_back);
		menu.add(1, 3, 4, "Refresh").setIcon(R.drawable.menu_reload);

		SubMenu sub = menu.addSubMenu(1, 4, 1, "LOL/INF").setIcon(R.drawable.menu_lolinf);
		sub.add(0,8,0,"LOL Post");
		sub.add(0,9,1, "INF Post");
		
		sub = menu.addSubMenu(1, 5, 5, "ShackMarks").setIcon(R.drawable.menu_mark);
		sub.add(0,6,0,"View saved ShackMarks");
		sub.add(0,7,1, "Save to ShackMarks").setIcon(R.drawable.menu_shacktags);
		
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
			this.fillSaxData(postID);
			return true;
		case 6:
			LaunchNotesIntent();
			return true;
		case 7: //sub menu for ShackMarks
			ShackDroidNotesManager nm = new ShackDroidNotesManager(this);
			nm.open();
			
			TextView poster = (TextView)findViewById(R.id.TextViewThreadAuthor);
			TextView postDate = (TextView)findViewById(R.id.TextViewThreadViewPostDate);
			
			ShackPost shackPost= posts.get(currentPosition);
			String previewText = shackPost.getPostPreview();
			
			long result = nm.CreateNote(postID, previewText, poster.getText().toString(), postDate.getText().toString(),"NWS",storyID);
			
			// notes manager returns an ID if it worked
			if (result > 0)
				new AlertDialog.Builder(this).setTitle("ShackMark").setPositiveButton("OK", null)
				.setMessage("This post has been saved to your ShackMarks.").show();
			else
				new AlertDialog.Builder(this).setTitle("ShackMark").setPositiveButton("OK", null)
				.setMessage("There was a problem saving your mark.").show();

			nm.close();
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
		}
		return false;
	}


	private void LaunchNotesIntent()
	{
		Intent intent = new Intent();
		intent.setClass(this, ActivityNotes.class);
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
