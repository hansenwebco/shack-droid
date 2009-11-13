package com.stonedonkey.shackdroid;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AdapterThreadedView<TopicRow> extends BaseAdapter {

	private Context context;
	private List<ShackPost> topicList;
	private int rowResouceID;
	static Typeface face;
	private int selectedRow = 0;
	
	public AdapterThreadedView(Context context,int rowResouceID, List<ShackPost> topicList,int selectedRow ){
		this.context = context;
		this.topicList = topicList;
		this.rowResouceID = rowResouceID;
		this.selectedRow = selectedRow;
		
		face = Typeface.createFromAsset(context.getAssets(), "fonts/arial.ttf");
	}
	
	public void setSelectedRow(int selectedRow) {
		this.selectedRow = selectedRow;
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
		threadPreview.setTypeface(face);
		
		if (threadPreview != null)
		{
		
			// TODO: This is ATROCIOUS find a better way.
			String pad ="";
			int postIndent = post.getIndent(); // avoid multiple lookups
			for(int i=0;i<postIndent;i++)
				pad = pad + "   ";	
			
			String postText = post.getPostPreview();
		
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			String login = prefs.getString("shackLogin", "");
			Boolean highlightThread = prefs.getBoolean("highlightUserThreads", true);
			int fontSize = Integer.parseInt(prefs.getString("fontSize", "12"));
			 
			// show this users posts as blue
			if (post.getPostIndex() > 9)
				threadPreview.setTextColor(Color.parseColor("#777777"));
			
			
			if (highlightThread == true)
				if (post.getPosterName().toString().equalsIgnoreCase(login))
					threadPreview.setTextColor(Color.parseColor("#00BFF3"));

			if (position == selectedRow )
				threadPreview.setBackgroundColor(Color.parseColor("#274FD3"));


		
			//postText = pad + postText;
			threadPreview.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
			threadPreview.setText(pad + postText);
			
		}
		return v;
	}


}
