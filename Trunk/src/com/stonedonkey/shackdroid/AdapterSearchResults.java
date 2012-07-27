package com.stonedonkey.shackdroid;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AdapterSearchResults  extends BaseAdapter {

	private Context context;
	private ArrayList<ShackSearch> searchItems;
	private int rowResouceID;
	static Typeface face;
	static int fontSize = 12;
	
	public AdapterSearchResults(Context context, ArrayList<ShackSearch> rssItems, int rowResouceID)
	{
		this.context = context;
		this.searchItems = rssItems;
		this.rowResouceID = rowResouceID;

	}
	
	public void SetPosts(ArrayList<ShackSearch> searchItems)
	{
		this.searchItems = searchItems;
	}
	@Override
	public int getCount() {
		return searchItems.size();
	}

	@Override
	public Object getItem(int position) {
		return searchItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		ShackSearch searchItem = searchItems.get(position);
		return  Long.parseLong(searchItem.getId());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ShackSearch searchItem = searchItems.get(position);
		LayoutInflater inflate = LayoutInflater.from(context);
			
		View v = inflate.inflate(rowResouceID,parent,false);

		TextView postText = (TextView)v.findViewById(R.id.TextViewLimePostText);
		String result = searchItem.getResultText();
		result = result.replaceAll("(\r\n|\r|\n|\n\r)", "");
		postText.setText(result);
		
		TextView poster = (TextView)v.findViewById(R.id.TextViewLimeAuthor);
		poster.setText(searchItem.getAuthor());
		
		TextView datePosted = (TextView)v.findViewById(R.id.TextViewLimePostDate);
		String timePassed = Helper.FormatShackDateToTimePassed(searchItem.getDatePosted());
		datePosted.setText(timePassed);
		
		TextView tag = (TextView)v.findViewById(R.id.TextViewLimeModTag);
		tag.setVisibility(View.GONE);
		
		TextView newPosts = (TextView)v.findViewById(R.id.TextViewLimeNewPosts);
		newPosts.setVisibility(View.GONE);
		
		TextView posts = (TextView)v.findViewById(R.id.TextViewLimePosts);
		posts.setVisibility(View.GONE);
		
		
//		TextView auth = (TextView)v.findViewById(R.id.TextViewSearchAuthor);
//		auth.setText(searchItem.getAuthor());
//
//		
//		TextView dp =  (TextView)v.findViewById(R.id.TextViewSearchDatePosted);
//		dp.setText(searchItem.getDatePosted());
//
//		
//		TextView rt = (TextView)v.findViewById(R.id.TextViewSearchBody);
//		String result = searchItem.getResultText();
//		result = result.replaceAll("(\r\n|\r|\n|\n\r)", "");
//		rt.setText(result);
	
		
		
		return v;
	}

}
