package com.stonedonkey.shackdroid;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

public class FragmentActivityTopic extends FragmentActivity {

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		
		setContentView(R.layout.mixed);
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, 0, 1, "New Post").setIcon(R.drawable.menu_addpost);
		menu.add(0, 1, 2, "Refresh").setIcon(R.drawable.menu_reload);

		return true;
	}
	
}
