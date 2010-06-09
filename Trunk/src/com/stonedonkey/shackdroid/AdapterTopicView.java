package com.stonedonkey.shackdroid;

import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AdapterTopicView extends BaseAdapter {

	//private Context context;
	private final List<ShackPost> topicList;
	private final int rowResouceID;
	private final String shackLogin;
	private final Typeface face;
	private final int fontSize;
	private final Hashtable<String,String> postCache;
	private final String showAuthor;
	private final Resources r; 
	private int totalNewPosts = 0;
	
	LayoutInflater inflate;// = LayoutInflater.from(context);
	public AdapterTopicView(Context context,int rowResouceID, List<ShackPost> topicList, String shackLogin,int fontSize,Hashtable<String,String> postCache ){
		this.topicList = topicList;
		this.rowResouceID = rowResouceID;
		this.shackLogin = shackLogin;
		this.fontSize = fontSize;
		this.postCache = postCache;
		this.r = context.getResources();
		
	    face = Typeface.createFromAsset(context.getAssets(), "fonts/arial.ttf");
	   	    
	    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	    showAuthor = prefs.getString("showAuthor","count");
	    
	    inflate = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		return topicList.size();
	}
	@Override
	public Object getItem(int position) {
		return topicList.get(position);
	}
	@Override
	public long getItemId(int position) {
		//return position;
		final ShackPost post = topicList.get(position);
		return Long.parseLong(post.getPostID());
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		TextView tmp;
		final View v;
		
		final ShackPost post = topicList.get(position);
		
		if (convertView == null){
			v = inflate.inflate(rowResouceID,parent,false);
		}
		else{
			v = convertView;
		}
			
		// bind the TextViews to the items in our data source
		tmp = (TextView)v.findViewById(R.id.TextViewPosterName);
		tmp.setTypeface(face);
		
		if (tmp != null)
			tmp.setText(post.getPosterName());
		
		if (shackLogin.equalsIgnoreCase(post.getPosterName()))
			tmp.setTextColor(Color.parseColor("#00BFF3"));
		else
			tmp.setTextColor(Color.parseColor("#ffba00"));

		
		tmp = (TextView)v.findViewById(R.id.TextViewDatePosted);
		tmp.setTypeface(face);
		if (tmp != null) {
			tmp.setText(Helper.FormatShackDate(post.getPostDate()));
			//postDate.setText(post.getPostDate());
		}
			
		tmp = (TextView)v.findViewById(R.id.TextViewReplyCount);
		tmp.setTypeface(face);
		tmp.setText(post.getReplyCount());
		
		if (showAuthor.equalsIgnoreCase("count") &&  post.getIsAuthorInThread())
			tmp.setTextColor(Color.parseColor("#0099CC"));
		else
			tmp.setTextColor(Color.parseColor("#FFFFFF"));

		if (post.getOrginalReplyCount() != null) // we are showing watched messages
		{
	
			final TextView postNewCount = (TextView)v.findViewById(R.id.TextViewNewPosts);
			Integer newPosts = 0;
			
			if (postCache != null && postCache.get(post.getPostID()) != null ) {
				final String cacheposts = postCache.get(post.getPostID());
				newPosts = Integer.parseInt(cacheposts) - post.getOrginalReplyCount();
			}
			
			if (newPosts > 0) {
				totalNewPosts = totalNewPosts + newPosts;
				postNewCount.setTypeface(face);
				postNewCount.setText("+" + newPosts.toString());
			}
			else
				postNewCount.setText(null);
			
						
		}
 
		else // show the number of new posts since the last refresh
		{
			if (postCache != null && postCache.get(post.getPostID()) != null ) {

				final String cacheposts = postCache.get(post.getPostID());
				final Integer newPosts = Integer.parseInt(post.getReplyCount()) - Integer.parseInt(cacheposts);

				final TextView postNewCount = (TextView)v.findViewById(R.id.TextViewNewPosts);
				if (newPosts > 0 && Integer.parseInt(post.getReplyCount()) > 0) {
					postNewCount.setTypeface(face);
					postNewCount.setText("+" + newPosts.toString());
				}
				else
					postNewCount.setText(null);

			}
			else // we don't have a cached version of this in the post cache reset the view
			{
				final TextView postNewCount = (TextView)v.findViewById(R.id.TextViewNewPosts);
				postNewCount.setText(null);
			}
		}
			
		tmp = (TextView)v.findViewById(R.id.TextViewPostText);
		tmp.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
		tmp.setTypeface(face);
		if (tmp != null)
		{
			String preview = post.getPostPreview();
			if (preview.length() > 99)
				preview= preview.substring(0,99);
			
			tmp.setText(preview);
		}
				
		final ImageView img = (ImageView)v.findViewById(R.id.ImageViewCat);
			
		final RelativeLayout tr = (RelativeLayout)v.findViewById(R.id.TopicRow);
		if (showAuthor.equalsIgnoreCase("topic") && post.getIsAuthorInThread())
		{
			 final Drawable d = r.getDrawable(R.drawable.background_gradient_blue);
			 tr.setBackgroundDrawable(d);
		}
		else
			tr.setBackgroundDrawable(null);

		
		// TODO: clean this up a little / also replicated in ShackDroidThread ick
		final String postCat = post.getPostCategory();
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
			img.setImageDrawable(null); // remove it if it's not being set
				
		return v;
	}



	public int getTotalNewPosts() {
		return totalNewPosts;
	}
	
}
