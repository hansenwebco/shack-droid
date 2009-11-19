package com.stonedonkey.shackdroid;

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
	
	public static void SetWindowState(Window window,Context context)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (prefs.getBoolean("allowFullScreen", false)) {
			window.requestFeature(Window.FEATURE_NO_TITLE);
			window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

}
