package com.stonedonkey.shackdroid;

import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AdapterTopicView extends BaseAdapter {

	private Context context;
	private List<ShackPost> topicList;
	private int rowResouceID;
	private String shackLogin;
	static Typeface face;
	private Hashtable<String,String> d;
	
	public AdapterTopicView(Context context,int rowResouceID, List<ShackPost> topicList, String shackLogin,Hashtable<String,String> d ){
		this.context = context;
		this.topicList = topicList;
		this.rowResouceID = rowResouceID;
		this.shackLogin = shackLogin;
		this.d = d;
		
		 face = Typeface.createFromAsset(context.getAssets(), "fonts/arial.ttf");
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
		ShackPost post = topicList.get(position);
		return Long.parseLong(post.getPostID());
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ShackPost post = topicList.get(position);
		LayoutInflater inflate = LayoutInflater.from(context);
		
		
		View v = inflate.inflate(rowResouceID,parent,false);
	
		
		// bind the TextViews to the items in our datasource
		TextView posterName = (TextView)v.findViewById(R.id.TextViewPosterName);
		posterName.setTypeface(face);
		if (posterName != null)
			posterName.setText(post.getPosterName());
		
		if (shackLogin.equalsIgnoreCase(post.getPosterName()))
			posterName.setTextColor(Color.parseColor("#00BFF3"));
		
		TextView postDate = (TextView)v.findViewById(R.id.TextViewDatePosted);
		postDate.setTypeface(face);
		if (postDate != null)
			postDate.setText(post.getPostDate());
		
	
		// Sep 14, 2008 2:31pm CST  
		//DateFormat dfm = new SimpleDateFormat("MMM d, y hh:mmaa z");
		//Date conDate = null;
		//try {  
		//	conDate = dfm.parse(post.getPostDate());
		//} catch (ParseException e) {
		//	// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}   
	
		// TODO: this is getting called too late... updating ActivityTopicView
		TextView postReplyCount = (TextView)v.findViewById(R.id.TextViewReplyCount);
		postReplyCount.setTypeface(face);
		if (postReplyCount !=null)
			if (d.get(post.getPostID()) != null  && Long.parseLong(d.get(post.getPostID())) < Long.parseLong(post.getReplyCount())   )
				postReplyCount.setText(post.getReplyCount() + "(x)");
				else
					postReplyCount.setText(post.getReplyCount());	
			
		
		TextView postText = (TextView)v.findViewById(R.id.TextViewPostText);
		postText.setTypeface(face);
		if (postText != null)
			postText.setText(post.getPostPreview());
				
		ImageView img = (ImageView)v.findViewById(R.id.ImageViewCat);
				
		// TODO: clean this up a little / also replicated in ShackDroidThread ick
		String postCat = post.getPostCategory();
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
				
		return v;
	}
}
