package com.stonedonkey.shackdroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.widget.Toast;

public class ActivityPreferences extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		Helper.SetWindowState(getWindow(),this);
		     
		super.onCreate(savedInstanceState);
		this.setTitle("ShackDroid - Settings");
				
		addPreferencesFromResource(R.xml.preferences);
		
		//final String versionCode = getString(R.string.version_id);
		final Preference version = (Preference)findPreference("version");
		
		
		try {
			final PackageInfo pi = this.getPackageManager().getPackageInfo(getPackageName(), 0);
			version.setTitle("Version " + pi.versionName + " - " + pi.versionCode);
		
		} catch (NameNotFoundException e) {
			Log.e("ShackDroid","Error retreving pacakge info for version in settings.");
		}
		

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

		if (preference.getKey().equals("NewVersion"))
		{
			newVersionCheck(true);
			return true;
		}
		if (preference.getKey().equals("Stats"))
		{
			final Intent intent = new Intent();
			ShackDroidStats.AddViewedStats(this);
			intent.setClass(this,ActivityStats.class);
			startActivity(intent);
			return true;
		}
		if (preference.getKey().equals("WhatsNew"))
		{
			CheckForUpdate(true);
			return true;
		}
		
		
		
		Boolean checkAllowSMSService = Helper.CheckAllowSMService(this);
		if (preference.getKey().equals("allowCheckShackMessages") || preference.getKey().equals("allowShackMessages"))
		{
			if (checkAllowSMSService)
			{
				Toast toast = Toast.makeText(this,"ShackMessage Service Started",Toast.LENGTH_SHORT);
				toast.show();
				Helper.setSMAlarm(getApplicationContext());
				//startService(new Intent(this, ActivityShackDroidServices.class));
			}
			else
			{
				Toast toast  = Toast.makeText(this,"ShackMessage Service Stopped",Toast.LENGTH_SHORT);
				toast.show();
				Helper.clearSMAlarm(getApplicationContext());
				//stopService(new Intent(this, ActivityShackDroidServices.class));
			}
		}
		return result;
	}
	private void CheckForUpdate(boolean force) {
		// have we seen this update?
		try {
			String vc = null;
			vc = getString(R.string.version_id);
			SharedPreferences settings=getPreferences(0);

			// NOTE: debugging resets value
			//SharedPreferences.Editor editor = settings.edit();
			//editor.putBoolean("hasSeenUpdatedVersion" + vc, false);
			//editor.commit(); 
			boolean hasSeenUpdatedVersion = settings.getBoolean("hasSeenUpdatedVersion" + vc, false);

			if (!hasSeenUpdatedVersion || force)
			{
				final String result = HandlerExtendedSites.WhatsNew(getResources().getString(R.string.version_id),this);

				new AlertDialog.Builder(this)
				.setTitle("What's New " + vc)
				.setPositiveButton("OK",null)
				.setMessage(result).show();

				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean("hasSeenUpdatedVersion" + vc, true);
				editor.commit(); 

			}
		}
		catch (Exception ex)
		{
			// do nothing
		}
	}
	
	
	private void newVersionCheck(boolean showNoUpdate) {

		String message = "Unable to complete version check, please try again later.";

		final String result = HandlerExtendedSites.VersionCheck(this);
		boolean updateAvail = false;

		if (result != null && result != "*fail*") {
			message = "GOOD NEWS EVERYBODY!\n\nA new version of ShackDroid is available, would you like to get it now?";
			updateAvail = true;
		}
		else if (result == null) // if we got null we're good if we got fail we failed
			message = "ShackDroid is up to date.";

		if (updateAvail) 
		{
			// show update dialog
			new AlertDialog.Builder(this)
			.setTitle("Version Check")
			.setPositiveButton("YES",  new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(result))); 
					finish();
				}
			})
			.setNegativeButton("NO", null).setMessage(message).show();

		}
		else if (showNoUpdate)
		{
			new AlertDialog.Builder(this)
			.setTitle("Version Check")
			.setPositiveButton("OK",null)
			.setMessage(message).show();
		}
	}
	
	
}
