package com.stonedonkey.shackdroid;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;

public class ActivityMainMenu extends ListActivity  {
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	     super.onCreate(savedInstanceState);
		
	     ArrayList<ShackMenuItem> menu = new ArrayList<ShackMenuItem>();
	     
	     menu.add(new ShackMenuItem("Latest Chatty","It gets you chicks!",R.drawable.menu_delete));
	     menu.add(new ShackMenuItem("Shack RSS", "The Mos Eisley of chatties.",R.drawable.menu_forward));
	     menu.add(new ShackMenuItem("Shack Search","For all your vanity needs.",R.drawable.menu_delete));
	     menu.add(new ShackMenuItem("Shack Marks","Pr0n Stash.",R.drawable.menu_delete));
	     menu.add(new ShackMenuItem("Shack Messages","Stuff to shocking for even the Shack.",R.drawable.menu_delete));
	     menu.add(new ShackMenuItem("Settings","Hay guys, am I doing this right?",R.drawable.menu_delete));
	     menu.add(new ShackMenuItem("Version Check","Donkeh finally did something new!?!",R.drawable.menu_delete));
	     
	     AdapterMainMenu mm = new AdapterMainMenu(this,R.layout.mainmenu_row, menu);
	     setListAdapter(mm);
	     
	     //AdapterNotesView nva = new AdapterNotesView(this, R.layout.notes_row,
					//notesCursor);
			//setListAdapter(nva);
	     
		 //setListAdapter(new ArrayAdapter<String>(this,
         //android.R.layout.simple_list_item_1, mStrings));
         //getListView().setTextFilterEnabled(true);

         
	}
		
}
