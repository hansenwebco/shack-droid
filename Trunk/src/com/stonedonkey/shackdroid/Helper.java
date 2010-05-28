package com.stonedonkey.shackdroid;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.gesture.GestureOverlayView;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
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
			//return "";
		}
		
		//Thu Apr 08 06:38:00 -0700 2010  -- squeegy updated his api format to this around 4/2010
		if (fixedDate == null) {
			try { 
				DateFormat dfm = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
				Date conDate = null;
				SimpleDateFormat format= null;
				
				conDate = dfm.parse(unformattedDate);
				format = new SimpleDateFormat("M.d.yy h:mma");
				fixedDate = format.format(conDate);

			} catch (ParseException e) {
				return "";
			}
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
	public static int CheckForNewShackMessages(Context context) throws IOException, SAXException, ParserConfigurationException
	{

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String login = prefs.getString("shackLogin", "");
		String password = prefs.getString("shackPassword", "");
	
		if (login.length() == 0 || password.length() == 0)
			return -1;
		
		URL url = new URL ("http://shackapi.stonedonkey.com/messages/?box=inbox&page=1");
		String userPassword = login + ":" + password;

		String encoding = Base64.encodeBytes(userPassword.getBytes());
		
		URLConnection uc = url.openConnection();
		uc.setRequestProperty("Authorization", "Basic " + encoding);
		uc.setRequestProperty("User-Agent", Helper.getUserAgentString(context));
		
		// Get a SAXParser from the SAXPArserFactory. 
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();

		// Get the XMLReader of the SAXParser we created. 
		XMLReader xr = sp.getXMLReader();

		// Create a new ContentHandler and apply it to the XML-Reader 
		SaxHandlerMessages saxHandler = new SaxHandlerMessages();
		xr.setContentHandler(saxHandler);

		// Parse the xml-data from our URL. // note not using compression  
		xr.parse(new InputSource(uc.getInputStream()));

		// get the Message items
		ArrayList<ShackMessage> messages = saxHandler.getMessages();
		
		// check to see if we got any results back from the inbox
		// if not were out of here
		int  totalResults = -1;
		try {
			totalResults = Integer.parseInt(saxHandler.getTotalResults());
		}
		catch (Exception ex){
			Log.e("ShackDroid", "Error Getting Total Results: " + ex.getMessage());
		} // total results not found
		
		
		
		if (totalResults == 0)		
			return -1;
		
		// first retrieve our last known message id
		int lastMessageID = GetLastShackMessageId(context);
		
		;
		
		// if the last known message id is less than the current message in the
		// box then we have a change, count the number of unread messages in the box
		ShackMessage msg = messages.get(0);
		int currentMessageID = -1;
		try {
		currentMessageID = Integer.parseInt(msg.getMsgID());
		}
		catch (Exception ex) {	
			
			
		}
		
		int newMessageCount = 0;
		

		
		for	(int i=0; i< messages.size();i++)
		{
			msg = messages.get(i);
			if (msg.getMessageStatus().equals("unread"))
				newMessageCount++;
			else
				break;
		}
				
		Log.d("ShackDroid", "Last Msg: " + String.valueOf(lastMessageID));
		Log.d("ShackDroid", "Current Msg: " + String.valueOf(currentMessageID));
		
		if (lastMessageID < currentMessageID && lastMessageID >= 0 && messages.get(0).getMessageStatus().equals("unread") )	
		{
			
			Log.d("ShackDroid", "FIRE ALERT!");
			
			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager nm = (NotificationManager)context.getSystemService(ns);
		
			int icon = R.drawable.shack_logo;
			CharSequence tickerText = "New Shack Message";
			
			Notification note = new Notification(icon,tickerText,0);
		
			CharSequence contentTitle = "New Shack Message";
			CharSequence contentText = newMessageCount +  " unread messages";
			Intent notificationIntent = new Intent(context, ActivityMessages.class);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
			
			note.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
			note.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
			
			note.ledARGB = 0xFF800080;
			note.ledOnMS = 100;
			note.ledOffMS = 100;
			//note.defaults = Notification.DEFAULT_SOUND;
			note.sound = Uri.parse("android.resource://com.stonedonkey.shackdroid/" + R.raw.alert1);
			nm.notify(1,note);

		}
		
		
		
		if (currentMessageID > 0)
			UpdateLastShackMessageId(context,currentMessageID);
		
		return newMessageCount;
	}
	public static void UpdateLastShackMessageId(Context context, int messageID) throws IOException
	{
		try {
		FileOutputStream fos = context.openFileOutput("shackmessage.cache",Context.MODE_PRIVATE);
		ObjectOutputStream os = new ObjectOutputStream(fos);
		os.writeInt(messageID);
		os.close();
		fos.close();
		}
		catch (Exception ex)
		{
			Log.e("ShackDroid", "Error Saving Last Shack Message: " + ex.getMessage());
		}
	}
	public static int GetLastShackMessageId(Context context) throws StreamCorruptedException, IOException
	{
		int lastMessageID = 0;
		try {
			if (context.getFileStreamPath("shackmessage.cache").exists()) {
							
				FileInputStream fileIn = context.openFileInput("shackmessage.cache");
				ObjectInputStream in = new ObjectInputStream(fileIn);
				lastMessageID = in.readInt();
				in.close();
				fileIn.close();
			}
		}
		catch (Exception ex) { 
			
			Log.e("ShackDroid", "Error Loading Last Shack Message: " + ex.getMessage());
		}
		
		return lastMessageID;
	}

	public static ShackGestureListener setGestureEnabledContentView(int resourceId, Activity activity){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		
		// Add a gesture listener if we're 1.6 or greater.
		if (Integer.parseInt(android.os.Build.VERSION.SDK) > 3 && prefs.getBoolean("useGestures", false)){
			GestureOverlayView v = new GestureOverlayView(activity);
			
			// Wrapper class to parse between gesture events and some consts.
			ShackGestureListener l = new ShackGestureListener(activity);
			v.setEventsInterceptionEnabled(true);
			v.setGestureVisible(prefs.getBoolean("gestureVisible", false)); // set this to true to see what you draw;
			v.addOnGesturePerformedListener(l);
			
			//Add the original view as child (this gesture view sits over the top)
			v.addView(LayoutInflater.from(activity).inflate(resourceId, null));
			activity.setContentView(v);
			return l;
		}
		else{
			activity.setContentView(resourceId);
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
	public static boolean CheckAllowSMService(Context context)
	{
		// make sure we are allowed and that we have a login and password
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean allowSMs = prefs.getBoolean("allowShackMessages", false);
		boolean allowCheckShackMessages = prefs.getBoolean("allowCheckShackMessages", false);
		String login = prefs.getString("shackLogin", "");
		String password = prefs.getString("shackPassword", "");
		
		if (allowSMs == false ||  allowCheckShackMessages == false || login.length() == 0 || password.length() == 0 )
			return false;
		else
			return true;
	}
	
	public static void setSMAlarm(Context context){
		
		AlarmManager m = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pi; //= PendingIntent.getBroadcast(context, 0, new Intent(context, ShackDroidServicesReceiver.class), PendingIntent.FLAG_NO_CREATE);
		
		//Get an inexact alarm so we fall in line with other apps waking the phone.  will run every 15 mins....ish.
		// If we change it to AlarmManager.ELAPSED_REALTIME it doesn't run when the phone is off.....I think.
		
		// TODO: This re-fires if already active, it'd be nice to not have it start again if this
		//       function is called.. not sure how to do that just yet.
		
		//if (pi == null) // pi is null if we don't have an existing matching intent
		//{
			pi = PendingIntent.getBroadcast(context, 0, new Intent(context, ShackDroidServicesReceiver.class), 0);
			m.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1500, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi);
			Log.d("ShackDroid", "SM Alarm Set");
		//}
		//else {
		//		Log.d("ShackDroid", "SM Alarm Already Set - Using Existing");
		//}
		
	}
	
	public static void clearSMAlarm(Context context){
		AlarmManager m = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(context, ShackDroidServicesReceiver.class), 0);
		
		
			m.cancel(pi);
			Log.d("ShackDroid", "Canceled SM Alarms");
		
	}
	public static String getUserAgentString(Context context)
	{
		return "ShackDroid/" + context.getString(R.string.version_id);
	}
	
	public static String getShackStory(int storyID,Context context) throws JSONException
	{
		
		// TODO: we really need a generic call for these HTTP connections where
		// we simply retreive data from a URL
		String result = "";
		URL url;
		try {
			url = new URL("http://shackapi.stonedonkey.com/stories/" + storyID + ".json");
			URLConnection conn = url.openConnection();
			HttpURLConnection httpConnection = (HttpURLConnection) conn;

			httpConnection.setRequestProperty("User-Agent", Helper.getUserAgentString(context));
			
			int responseCode = httpConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {

				InputStream is = httpConnection.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				
				String line = "";
				
				while ((line = reader.readLine()) != null) {
					result = result + line + "\n";
				}
			}

		} catch (Exception e) {

			result = "";
		}
		
		if (result.length() > 0)
		{
			JSONObject json = new JSONObject(result);
			final String body = json.getString("body");
			final String name = json.getString("name");
		
			return "<h3>" + name + "</h3>" + body;
		}
		else
			return "Story could not be loaded at this time.";
		
	}
	

}
