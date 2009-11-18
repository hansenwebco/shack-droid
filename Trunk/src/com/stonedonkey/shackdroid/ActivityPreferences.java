package com.stonedonkey.shackdroid;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ActivityPreferences extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
	

		Helper.SetWindowState(getWindow(),this);
	
		
		     
		super.onCreate(savedInstanceState);

	
		
		addPreferencesFromResource(R.xml.preferences);
		
	}

}
