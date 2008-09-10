package com.stonedonkey.shackdroid;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ShackDroidNotes extends ListActivity {

	private Cursor notesCursor;
	private long itemPosition;

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
						menu.setHeaderTitle("ShackMark Options");
						menu.add(0, 0, 0, "View ShackMark");
						menu.add(0, 1, 0, "Delete ShackMark");
					}
				});
 
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
		// get a reference tot he ContextMenu it tells you what
		// position on  the listview was clicked.
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			
		itemPosition = info.position;
		
		switch (item.getItemId()) {
		case 0:
			ShowShackNotePost(itemPosition);
			return true;
		case 1: // delete note
			DeleteShackNote(itemPosition);
			return true;
		}
		return false;

		// return super.onContextItemSelected(item);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		ShowShackNotePost(itemPosition);

	}



	private void ShowShackNotePost(long position) {
		notesCursor.moveToPosition(Integer.valueOf((int) position));

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

	public void DeleteShackNote(long position) {
		// long testing = getListView().getSelectedItemPosition();

		ShackDroidNotesManager nm = new ShackDroidNotesManager(this);
		nm.open();

		notesCursor.moveToPosition(Integer.valueOf((int) position));
		long rowID = notesCursor.getLong(notesCursor
				.getColumnIndexOrThrow("_id"));

		nm.DeleteNote(rowID);

		notesCursor = nm.GetAllNotes();
		NotesViewAdapter nva = new NotesViewAdapter(this, R.layout.notes_row,
				notesCursor);
		setListAdapter(nva);

		nm.close();

	}
}
