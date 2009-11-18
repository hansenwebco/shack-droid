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
		DateFormat dfm = new SimpleDateFormat("MMM d, y hh:mmaa z");
		Date conDate = null;
		SimpleDateFormat format= null;
		String fixedDate  = null;

		try {  
			conDate = dfm.parse(unformattedDate);
			format = new SimpleDateFormat("M.d.yy h:mm a");
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
