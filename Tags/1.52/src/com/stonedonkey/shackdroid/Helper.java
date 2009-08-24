package com.stonedonkey.shackdroid;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

}
