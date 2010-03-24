package com.stonedonkey.shackdroid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class ActivityPreferences extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		Helper.SetWindowState(getWindow(),this);
		     
		super.onCreate(savedInstanceState);
		this.setTitle("ShackDroid - Settings");
		
		addPreferencesFromResource(R.xml.preferences);
		
		
		
		
	}
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		
	
		if (preference.getKey().equals("allowShackMessages"))
		{
			
			
			SharedPreferences settings= PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			SharedPreferences.Editor editor = settings.edit();
	        editor.putBoolean("allowCheckShackMessages", false);
	        editor.commit(); 
	        
	        // TODO: hacky hackerson // no other way to reset preference screen?
	        startActivity(getIntent());
	        finish();
		}
        
		return super.onPreferenceTreeClick(preferenceScreen, preference);
		
			
		
		
		
	}

}
