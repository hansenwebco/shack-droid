package com.stonedonkey.shackdroid;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ThreadedViewAdapter extends BaseAdapter {

	private Context context;
	private List<ShackPost> topicList;
	private int rowResouceID;
	
	public ThreadedViewAdapter(Context context,int rowResouceID, List<ShackPost> topicList ){
		this.context = context;
		this.topicList = topicList;
		this.rowResouceID = rowResouceID;
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
		ShackPost post = topicList.get(position);
		return Long.parseLong(post.getPostID());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ShackPost post = topicList.get(position);
		LayoutInflater inflate = LayoutInflater.from(context);
		
		View v = inflate.inflate(rowResouceID,parent,false);
		
		// bind the TextViews to the items in our datasource
		TextView threadPreview = (TextView)v.findViewById(R.id.TextViewThreadPreview);
		
		Typeface face = Typeface.createFromAsset(context.getAssets(), "fonts/Monospace821.TTF");
		threadPreview.setTypeface(face);
		
		
		
		if (threadPreview != null)
		{
			// TODO: This is ATROCIOUS find a better way.
			String pad ="";
			for(int i=0;i<post.getIndent();i++)
				pad = pad + "  ";	
			
			String postText= post.getPostPreview();
		
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			String login = prefs.getString("shackLogin", "");
			Boolean highlightThread = prefs.getBoolean("highlightUserThreads", true);
			 
			// show this users posts as blue
			if (post.getPostIndex() > 9)
				threadPreview.setTextColor(Color.parseColor("#777777"));
			
			
			if (highlightThread == true)
				if (post.getPosterName().toString().equalsIgnoreCase(login))
					threadPreview.setTextColor(Color.parseColor("#00BFF3"));
			
			// we can use the epipsize on the actual layout but it looks pretty
			// crappy.. this looks better ATM
			try
			{
				if (pad.length() >= 39)
					postText = pad.substring(0,36) + "...";
				else if (pad.length() + postText.length() >= 39)
					postText = pad + postText.substring(0, 39-(post.getIndent() * 2)) + "...";
				else
					postText= pad + postText;
				
				threadPreview.setText(postText);
			}
			catch (Exception ex)
			{
				// something went bad with parsing this text.. empty post
			}
			
		}
		return v;
	}
}
