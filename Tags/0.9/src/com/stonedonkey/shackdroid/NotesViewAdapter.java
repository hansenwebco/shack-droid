package com.stonedonkey.shackdroid;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class NotesViewAdapter extends BaseAdapter {

	private Context context;
	private Cursor noteList;
	private int rowResouceID;
	
	public NotesViewAdapter(Context context,int rowResouceID, Cursor noteList){
		this.context = context;
		this.noteList = noteList;
		this.rowResouceID = rowResouceID;
	}
	
	@Override
	public int getCount() {
		return noteList.getCount();
	}

	@Override
	public Object getItem(int position) {
		return noteList.moveToPosition(position);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		LayoutInflater inflate = LayoutInflater.from(context);
		noteList.moveToPosition(position);
	
		View v = inflate.inflate(rowResouceID,parent,false);
		
		// bind the TextViews to the items in our datasource
		String noteValue = noteList.getString(noteList.getColumnIndexOrThrow("messagePreview"));
		TextView notePreview = (TextView)v.findViewById(R.id.TextViewNotesPreview);
		notePreview.setText(noteValue.toString());
		
		noteValue = noteList.getString(noteList.getColumnIndexOrThrow("postDate"));
		TextView noteDate = (TextView)v.findViewById(R.id.TextViewNotesDatePosted);
		noteDate.setText(noteValue.toString());
		
		noteValue = noteList.getString(noteList.getColumnIndexOrThrow("posterName"));
		TextView notePoster = (TextView)v.findViewById(R.id.TextViewNotesPosterName);
		notePoster.setText(noteValue.toString());
		
		return v;
	}
}
