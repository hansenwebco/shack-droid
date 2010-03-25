package com.stonedonkey.shackdroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.widget.Toast;

public class ActivityPreferences extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		Helper.SetWindowState(getWindow(),this);
		     
		super.onCreate(savedInstanceState);
		this.setTitle("ShackDroid - Settings");
		
		addPreferencesFromResource(R.xml.preferences);
		
		Preference customPref = (Preference) findPreference("allowCheckShackMessages");
		customPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
		

			// make sure we have a password and username as well as have enabled
			// shack messages before we enable checking for Shack Messages in the background
			// this doesn't take care if the user removes their name after they
			// check this setting..
			// TODO: handle if the user removes username or password
			@Override
			public boolean onPreferenceChange(Preference arg0, Object arg1) {

				// make sure we are allowed and that we have a login and password
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				boolean allowSMs = prefs.getBoolean("allowShackMessages", false);
				String login = prefs.getString("shackLogin", "");
				String password = prefs.getString("shackPassword", "");
				
				if (login.length() > 0 && password.length() > 0 && allowSMs) {
					return true;
				}
				else { 
					Toast toast = Toast.makeText(getBaseContext(),"Enter your login and password to enable.",Toast.LENGTH_SHORT);
					toast.show();
					return false;
				}
			}
			});
	
	}
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,	Preference preference) {
	
		Boolean result = super.onPreferenceTreeClick(preferenceScreen, preference);
		Boolean checkAllowSMSService = Helper.CheckAllowSMService(this);

		if (preference.getKey().equals("allowCheckShackMessages") || preference.getKey().equals("allowShackMessages"))
		{
			if (checkAllowSMSService)
			{
				Toast toast = Toast.makeText(this,"ShackMessage Service Started",Toast.LENGTH_SHORT);
				toast.show();
				startService(new Intent(this, ActivityShackDroidServices.class));
			}
			else
			{
				Toast toast  = Toast.makeText(this,"ShackMessage Service Stopped",Toast.LENGTH_SHORT);
				toast.show();
				stopService(new Intent(this, ActivityShackDroidServices.class));
			}
		}
		return result;
	}
}
