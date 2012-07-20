package com.stonedonkey.shackdroid;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class FragmentActivityThread extends FragmentActivity {

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);

		if (arg0 == null) {
			FragmentThreadedView frag = new FragmentThreadedView();
			frag.setArguments(getIntent().getExtras());
			getSupportFragmentManager().beginTransaction().replace(android.R.id.content, frag).commit();
		}

	}

}
