package com.stonedonkey.shackdroid;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AdapterLOL extends BaseAdapter {

	private ArrayList<ShackLOL> posts;
	private Context context;
	private int rowResouceID;
	static Typeface face;
	
	public AdapterLOL(Context context, ArrayList<ShackLOL> posts,int rowResouceID)
	{
		this.posts = posts;
		this.context = context;
		this.rowResouceID = rowResouceID;
		
		 face = Typeface.createFromAsset(context.getAssets(), "fonts/arial.ttf");
		
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return posts.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return posts.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return Long.parseLong(posts.get(arg0).getId());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	
		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		//int fontSize = Integer.parseInt(prefs.getString("fontSize", "12"));
		
		ShackLOL post = posts.get(position);
			
		LayoutInflater inflate = LayoutInflater.from(context);
		View v = inflate.inflate(rowResouceID,parent,false);
						
		// format and set post text
		String postBody = post.getBody();
		postBody = postBody.replaceAll("</?\\w++[^>]*+>", ""); // remove HTML tags
		//postBody = postBody.replaceAll("(\r\n|\r|\n|\n\r)", "");
		
		if (postBody.length() > 255)
			postBody = postBody.substring(0,255);
		
		TextView postText = (TextView)v.findViewById(R.id.TextViewLolPostText);
		postText.setText(postBody);
		postText.setTypeface(face);
		
		// set poster name
		TextView posterName = (TextView)v.findViewById(R.id.TextViewLolPosterName);
		posterName.setText(post.getAuthor());
		posterName.setTypeface(face);
		
		// Tag count
		TextView tagCount = (TextView)v.findViewById(R.id.TextViewLolTagCount);
		tagCount.setText(post.getTagCount() + "\n" + post.getTag().toUpperCase() + "s");
		tagCount.setTypeface(face);
		
		 // date posted
		TextView datePosted = (TextView)v.findViewById(R.id.TextViewLOLDatePosted);
		datePosted.setText(Helper.FormatLOLDate(post.getCommentDate()));
		datePosted.setTypeface(face);
		
		return v;
	}

	
	
}
