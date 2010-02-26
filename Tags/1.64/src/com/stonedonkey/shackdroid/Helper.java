package com.stonedonkey.shackdroid;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;

public class Helper {

	public static String FormatShackDate(String unformattedDate) 
	{

		String fixedDate  = null;
		try { 
			DateFormat dfm = new SimpleDateFormat("MMM d, y hh:mmaa z");
			Date conDate = null;
			SimpleDateFormat format= null;

			conDate = dfm.parse(unformattedDate);
			format = new SimpleDateFormat("M.d.yy h:mma");
			fixedDate = format.format(conDate);

		} catch (ParseException e) {
			return "";
		}
		return fixedDate;
	}
	// TODO: We can probably combine these i'm just lazy today
	public static String FormShackRSSDate(String unformattedDate)
	{
		String fixedDate  = null;
		//2009-11-19T10:10-06:00
		try {  
			DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'h:mmZ");
			Date conDate = null;
			SimpleDateFormat format= null;

			conDate = dfm.parse(unformattedDate);
			format = new SimpleDateFormat("M.d.yy h:mma");
			fixedDate = format.format(conDate);

		} catch (ParseException e) {
			return "";
		}
		return fixedDate;
	}
	public static String FormatLOLDate(String unformattedDate)
	{
		String fixedDate  = null;
		//Wed, 24 Feb 2010 09:49:00 -0800
		try {  
			DateFormat dfm = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
			Date conDate = null;
			SimpleDateFormat format= null;

			conDate = dfm.parse(unformattedDate);
			format = new SimpleDateFormat("M.d.yy h:mma");
			fixedDate = format.format(conDate);

		} catch (ParseException e) {
			return "";
		}
		return fixedDate;
	}
	public static String GetCurrentChattyStoryID()
	{
		// NOTE: testing thread
		//return "61661;" 
		
		try {
			URL url = new URL("http://www.shacknews.com/latestchatty.x");
			URLConnection connection = url.openConnection();
			HttpURLConnection httpConnection = (HttpURLConnection)connection;
			httpConnection.connect();
			int response = httpConnection.getResponseCode();

			if (response == 200) {
			String location = httpConnection.getURL().toString();
			String id =  location.substring(location.lastIndexOf("=")+1);
			
			return id;
			}
			else
				return null;
			
		} catch (Exception e) {
			return null;
		}		
	}
	public static void SetWindowState(Window window,Context context)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		//int orientation = prefs.getInt("orientation", 0);
		
		
		if (prefs.getBoolean("allowFullScreen", false)) {
			window.requestFeature(Window.FEATURE_NO_TITLE);
			window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

}
