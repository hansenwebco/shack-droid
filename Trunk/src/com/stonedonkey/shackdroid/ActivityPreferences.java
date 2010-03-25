package com.stonedonkey.shackdroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

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
		
		Boolean result = super.onPreferenceTreeClick(preferenceScreen, preference);

//			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//			String login = prefs.getString("shackLogin", "");
//			String password = prefs.getString("shackPassword", "");
//			boolean allowCheckShackMessages = prefs.getBoolean("allowCheckShackMessages", false);
//			
//			if (allowCheckShackMessages && (login.length() == 0 || password.length() ==0))
//			{
//				Toast toast = Toast.makeText(this,"Your must set login and password for Shack Message notifications.",Toast.LENGTH_LONG);
//				toast.show();
//			}
		
		
	
		// start the service that checks for new shack messages
		if (Helper.CheckAllowSMService(this)) 
		{
			startService(new Intent(this, ActivityShackDroidServices.class));
			Toast toast = Toast.makeText(this,"ShackMessage Service Started",Toast.LENGTH_SHORT);
			toast.show();
		}
		else 
		{
			stopService(new Intent(this, ActivityShackDroidServices.class));
			Toast toast  = Toast.makeText(this,"ShackMessage Service Stopped",Toast.LENGTH_SHORT);
			toast.show();
		}
		
     
		return result;
		
			
		
		
		
	}
	

}
