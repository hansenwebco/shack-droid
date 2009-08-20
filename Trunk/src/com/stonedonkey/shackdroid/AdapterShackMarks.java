package com.stonedonkey.shackdroid;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AdapterShackMarks extends BaseAdapter {

	private Context context;
	private List<ShackPost> marks;
	private int rowResouceID;
	static Typeface face;
		
	public  AdapterShackMarks(Context context,int rowResouceID, List<ShackPost> marks)
	{
		this.context = context;
		this.rowResouceID = rowResouceID;
		this.marks = marks;
		
		face = Typeface.createFromAsset(context.getAssets(), "fonts/arial.ttf");
		
	}
	
	@Override
	public int getCount() {
		return marks.size();
	}

	@Override
	public Object getItem(int position) {
		return marks.get(position);
	}

	@Override
	public long getItemId(int position) {
		ShackPost post = marks.get(position);
		return Long.parseLong(post.getPostID());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (marks.size() == 0)
			return null;
		
		ShackPost post = marks.get(position);
		LayoutInflater inflate = LayoutInflater.from(context);
		
		View v = inflate.inflate(rowResouceID,parent,false);
		
	
		TextView posterName= (TextView)v.findViewById(R.id.TextViewNotesPosterName);
		posterName.setText(post.getPosterName());
		posterName.setTypeface(face);
		
		TextView postText = (TextView)v.findViewById(R.id.TextViewNotesPreview);
		postText.setText(post.getPostPreview());
		postText.setTypeface(face);
		
		TextView postDate = (TextView)v.findViewById(R.id.TextViewNotesDatePosted);
		postDate.setText(Helper.FormatShackDate(post.getPostDate()));
		postDate.setTypeface(face);
		
		return v;
	}

}
