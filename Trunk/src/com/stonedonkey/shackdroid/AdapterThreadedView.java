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

	private final List<ShackPost> topicList;
	private final int rowResouceID;
	private static Typeface face;
	private int selectedRow = 0;
	private final String login;
	private final Boolean highlightThread;
	private final int fontSize;
	private TextView threadPreview;
	private final LayoutInflater inflate;
	
	public AdapterThreadedView(Context context,int rowResouceID, List<ShackPost> topicList,int selectedRow ){
		this.topicList = topicList;
		this.rowResouceID = rowResouceID;
		this.selectedRow = selectedRow;
		
		face = Typeface.createFromAsset(context.getAssets(), "fonts/arial.ttf");
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		login = prefs.getString("shackLogin", "");
		highlightThread = prefs.getBoolean("highlightUserThreads", true);
		fontSize = Integer.parseInt(prefs.getString("fontSize", "12"));
		inflate = LayoutInflater.from(context);
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
		final ShackPost post = topicList.get(position);
		return Long.parseLong(post.getPostID());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final View v;
		final ShackPost post = topicList.get(position);
		if (convertView == null){
			v = inflate.inflate(rowResouceID,parent,false);
		}
		else{
			v = convertView;
		}
		
		// bind the TextViews to the items in our datasource
		threadPreview = (TextView)v.findViewById(R.id.TextViewThreadPreview);
				
		if (threadPreview != null)
		{
			threadPreview.setTypeface(face);

			// chazums
			// Now just moves the text box to the right instead of padding text.
			final int postIndent = 7* post.getIndent(); // avoid multiple lookups
	
			threadPreview.setPadding(postIndent, threadPreview.getPaddingTop(), threadPreview.getPaddingBottom(), threadPreview.getPaddingRight());
			final String postText = post.getPostPreview();
			 
			// show this users posts as blue
			 if (highlightThread == true && post.getPosterName().toString().equalsIgnoreCase(login))
					threadPreview.setTextColor(Color.parseColor("#00BFF3"));
			 else if (post.getPostIndex() > 9)
				threadPreview.setTextColor(Color.parseColor("#777777"));
			 else
				threadPreview.setTextColor(Color.parseColor("#FFFFFF"));
			
			
			if (position == selectedRow )
				threadPreview.setBackgroundColor(Color.parseColor("#274FD3"));
			else
				threadPreview.setBackgroundColor(Color.TRANSPARENT);
		
			//postText = pad + postText;
			threadPreview.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
			threadPreview.setText(postText);
			
		}
		return v;
	}
}
