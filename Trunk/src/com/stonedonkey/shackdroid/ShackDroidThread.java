package com.stonedonkey.shackdroid;

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
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ShackDroidThread extends ListActivity implements Runnable {

	private static final int POST_REPLY = 0;
	private ArrayList<ShackPost> posts;
	private String storyID;
	private String postID;
	private ProgressDialog pd;
	private String errorText = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.thread);
		this.setTitle("ShackDroid - View Thread");
		
		if (savedInstanceState != null) {
			// savedInstanceState.getLong("storyID");
			postID = savedInstanceState.getString("postID");
			storyID = savedInstanceState.getString("storyID");
		} 
		else 
		{
			Bundle extras = this.getIntent().getExtras();
			postID = extras.getString("postID");
			storyID = extras.getString("storyID");
		}

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
		
		try {
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String feedURL = prefs.getString("shackFeedURL", "http://shackchatty.com");
			URL url = new URL(feedURL + "/thread/" + postID	+ ".xml");

			/* Get a SAXParser from the SAXPArserFactory. */
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xr = sp.getXMLReader();
			/* Create a new ContentHandler and apply it to the XML-Reader */
			TopicViewSaxHandler saxHandler = new TopicViewSaxHandler(this);
			xr.setContentHandler(saxHandler);

			/* Parse the xml-data from our URL. */
			xr.parse(new InputSource(url.openStream()));
			/* Parsing has finished. */

			/* Our ExampleHandler now provides the parsed data to us. */
			posts = saxHandler.GetParsedPosts();

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
	
	private void ShowData() {
	
		if (posts != null)
		{
		// this is where we bind our fancy ArrayList of posts
		ThreadedViewAdapter tva = new ThreadedViewAdapter(this,
				R.layout.thread_row, posts);

		setListAdapter(tva);

		TextView tv = (TextView) findViewById(R.id.TextViewPost);

		// TODO: Consolidate this with the call when the list view is
		// clicked.. to sloppy
		String postText = ParseShackText(posts.get(0).getPostText());
		tv.setText(Html.fromHtml(postText));
		Linkify.addLinks(tv, Linkify.ALL); // make all hyperlinks clickable

		ShackPost post = posts.get(0);

		TextView posterName = (TextView) findViewById(R.id.TextViewThreadAuthor);
		posterName.setText(post.getPosterName());

		TextView postDate = (TextView) findViewById(R.id.TextViewThreadViewPostDate);
		postDate.setText(post.getPostDate());

		String postCat = post.getPostCategory();
		setPostCategoryIcon(postCat);
		
		// set the post background color to be more "shack" like
		RelativeLayout layout = (RelativeLayout)findViewById(R.id.RelativeLayoutThread);
		layout.setBackgroundColor(Color.parseColor("#222222"));
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
		super.onListItemClick(l, v, position, id);

		//l.setChoiceMode(1);
		l.setItemChecked(position, true);
				
		ScrollView sv = (ScrollView) findViewById(R.id.textAreaScroller);
		sv.scrollTo(0, 0); 

		TextView tv = (TextView) findViewById(R.id.TextViewPost);

		String postText = posts.get(position).getPostText();
		postID = posts.get(position).getPostID();
		
		postText = ParseShackText(postText);

		// TODO: Consolidate with the load to make this better, sloppy.
		tv.setText(Html.fromHtml(postText));
		Linkify.addLinks(tv, Linkify.ALL); // make all hyperlinks clickable

		TextView posterName = (TextView) findViewById(R.id.TextViewThreadAuthor);
		posterName.setText(posts.get(position).getPosterName());

		TextView postDate = (TextView) findViewById(R.id.TextViewThreadViewPostDate);
		postDate.setText(posts.get(position).getPostDate());
		
		String postCat = posts.get(position).getPostCategory();
		setPostCategoryIcon(postCat);

	}
	private void setPostCategoryIcon(String postCat)
	{
		ImageView img = (ImageView)findViewById(R.id.ImageViewCatTopic);
		//TODO: clean this up a little / also duplicated in Topic View Adapter ick
		if (postCat.equals("offtopic"))  {
			img.setImageResource(R.drawable.offtopic);
			//tr.setBackgroundColor(Color.parseColor("#081407"));
		}
		else if (postCat.equals("nws"))
			img.setImageResource(R.drawable.nws);
		else if (postCat.equals("political")) {
			img.setImageResource(R.drawable.political);
			//tr.setBackgroundColor(Color.parseColor("#211D1A"));
		}
		else if (postCat.equals("stupid")) {
			img.setImageResource(R.drawable.stupid);
			//tr.setBackgroundColor(Color.GREEN);
		}
		else if (postCat.equals("informative"))
			img.setImageResource(R.drawable.interesting);	
		else
			img.setImageResource(-1);
	}
	private String ParseShackText(String text) {

		// TODO: probably a better way of doing this than a mass replace
		text = text.replaceAll("<span class=\"jt_red\">(.*?)</span>",
				"<font color=\"#ff0000\">$1</font>");
		text = text.replaceAll("<span class=\"jt_green\">(.*?)</span>",
				"<font color=\"#8dc63f\">$1</font>");
		text = text.replaceAll("<span class=\"jt_pink\">(.*?)</span>",
				"<font color=\"#f49ac1\">$1</font>");
		text = text.replaceAll("<span class=\"jt_olive\">(.*?)</span>",
				"<font color=\"#808000\">$1</font>");
		text = text.replaceAll("<span class=\"jt_fuchsia\">(.*?)</span>",
				"<font color=\"#c0ffc0\">$1</font>");
		text = text.replaceAll("<span class=\"jt_yellow\">(.*?)</span>",
				"<font color=\"#ffde00\">$1</font>");
		text = text.replaceAll("<span class=\"jt_blue\">(.*?)</span>",
				"<font color=\"#44aedf\">$1</font>");
		text = text.replaceAll("<span class=\"jt_lime\">(.*?)</span>",
				"<font color=\"#c0ffc0\">$1</font>");
		text = text.replaceAll("<span class=\"jt_orange\">(.*?)</span>",
				"<font color=\"#f7941c\">$1</font>");
		text = text.replaceAll("<span class=\"jt_bold\">(.*?)</span>",
				"<b>$1</b>");
		text = text.replaceAll("<span class=\"jt_italic\">(.*?)</span>",
				"<i>$1</i>");

		return text;
	}

	// menu creation
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(1, 0, 0, "Reply").setIcon(R.drawable.menu_reply);
		menu.add(1, 1, 3, "Settings").setIcon(R.drawable.menu_settings);
		menu.add(1, 2, 2, "Back").setIcon(R.drawable.menu_back);
		menu.add(2, 3, 1, "Refresh").setIcon(R.drawable.menu_reload);
		menu.add(2, 4, 4, "LOL/INF").setIcon(R.drawable.menu_lolinf);
		menu.add(2, 5, 5, "Mark").setIcon(R.drawable.menu_mark);

		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case 0:
		
			
			// Launch post form
			intent = new Intent();
			intent.setClass(this, ShackDroidPost.class);
			intent.putExtra("storyID", storyID);
			intent.putExtra("postID",postID);
			//startActivity(intent);
			startActivityForResult(intent,POST_REPLY);
			return true;
		
			
		case 1:
			// show settings dialog
			intent = new Intent();
			intent.setClass(this, ShackDroidPreferences.class);
			startActivity(intent);
			return true;
		case 2:
			finish();
			return true;
		case 3:
			this.fillSaxData(postID);
			return true;
		}
		return false;
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

}