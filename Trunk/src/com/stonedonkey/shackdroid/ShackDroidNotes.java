package com.stonedonkey.shackdroid;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;

public class ShackDroidNotes extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.notes);
		
		this.setTitle("ShackMarks");
		
		ShackDroidNotesManager nm = new ShackDroidNotesManager(this);
		nm.open();
		
		Cursor cur = nm.GetAllNotes();
		

			NotesViewAdapter nva = new NotesViewAdapter(this,R.layout.notes_row,cur);	
			setListAdapter(nva);
	
	}

}
