package com.stonedonkey.shackdroid;

import java.util.List;

import android.content.Context;
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
		
		//Typeface face = Typeface.createFromAsset(context.getAssets(), "fonts/andalemo.ttf");
		//threadPreview.setTypeface(face);
		
		
		if (threadPreview != null)
		{
			// TODO: This is ATROCIOUS find a better way.
			String pad ="";
			for(int i=0;i<post.getIndent();i++)
				pad = pad + "  ";	
			
			String postText= post.getPostPreview();
			
			if (postText.length() > 46)
				postText = pad + post.getPostPreview().substring(0, 47-(post.getIndent() * 2)) + "...";
			else
				postText= pad + post.getPostPreview();

			threadPreview.setText(postText);
		}
		return v;
	}
}
