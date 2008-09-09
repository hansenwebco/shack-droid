package com.stonedonkey.shackdroid;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.ListView;

public class ShackDroidNotes extends ListActivity {

	private Cursor notesCursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.notes);

		this.setTitle("ShackMarks");

		ShackDroidNotesManager nm = new ShackDroidNotesManager(this);
		nm.open();

		notesCursor = nm.GetAllNotes();

		NotesViewAdapter nva = new NotesViewAdapter(this, R.layout.notes_row,
				notesCursor);
		setListAdapter(nva);

		nm.close();

		// add a listener to the default view to handle holding down
		// on the screen to view or delete.. default cilck is still view.
		getListView().setOnCreateContextMenuListener(
				new OnCreateContextMenuListener() {
					@Override
					public void onCreateContextMenu(ContextMenu menu, View v,
							ContextMenuInfo menuInfo) {
						menu.add(0, 0, 0, "View ShackMark");
						menu.add(0, 0, 0, "Delete ShackMark");
					}
				});
		// Add click action
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View v,	int position, long id) {
						// Clicking an item starts editing it
						
							DeleteShackMark(id);
							
						
					}

				});

	}

	private void DeleteShackMark(Long deleteID)
	{
		
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		notesCursor.moveToPosition(position);

		String postID = notesCursor.getString(notesCursor
				.getColumnIndexOrThrow("threadID"));
		String storyID = notesCursor.getString(notesCursor
				.getColumnIndexOrThrow("storyID"));

		Intent intent = new Intent();
		intent.setClass(this, ShackDroidThread.class);
		intent.putExtra("postID", postID); // the value must be a string
		intent.putExtra("storyID", storyID);
		startActivity(intent);

	}

}
