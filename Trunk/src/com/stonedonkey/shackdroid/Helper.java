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
import java.util.Comparator;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.util.Linkify.MatchFilter;
import android.text.util.Linkify.TransformFilter;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class Helper {

	static final Object[] dataLock = new Object[0];

	public static String FormatShackDate(String unformattedDate) {
		String fixedDate = null;
		try {
			DateFormat dfm = new SimpleDateFormat("MMM d, y hh:mmaa z");
			Date conDate = null;
			SimpleDateFormat format = null;

			conDate = dfm.parse(unformattedDate);
			format = new SimpleDateFormat("M.d.yy h:mma");
			fixedDate = format.format(conDate);

		}
		catch (ParseException e) {
			// return "";
		}

		// Thu Apr 08 06:38:00 -0700 2010 -- squeegy updated his api format to this around 4/2010
		if (fixedDate == null) {
			try {
				DateFormat dfm = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
				Date conDate = null;
				SimpleDateFormat format = null;

				conDate = dfm.parse(unformattedDate);
				format = new SimpleDateFormat("M.d.yy h:mma");
				fixedDate = format.format(conDate);

			}
			catch (ParseException e) {
				return "";
			}
		}
		return fixedDate;
	}

	public static String FormatShackDateToTimePassed(String unformattedDate) {

		String textDate = "?";

		try {
			DateFormat dfm = new SimpleDateFormat("MMM d, y hh:mmaa z");
			Date datePost = (Date) dfm.parse(unformattedDate);
			Date dateNow = new Date();
			long seconds = 0;
			seconds = (dateNow.getTime() - datePost.getTime()) / 1000;

			if (seconds < 60) // less than a minute show seconds
			{
				textDate = Long.toString(seconds) + "s";
			}
			else if (seconds > 59 && seconds < 3600) // less than an hour show minutes
			{
				seconds = Math.round(seconds / 60);
				textDate = Long.toString(seconds) + "m";
			}
			else if (seconds > 3599 && seconds < 86400) // less than one day show hours and minutes?
			{
				long hours = Math.round(seconds / 3600);
				// long minutes = Math.round((seconds - (hours*3600))/60);
				// textDate = Long.toString(hours) + "h " + Long.toString(minutes) + "m";
				textDate = Long.toString(hours) + "h";
			}
			else if (seconds > 86401) {
				long days = Math.round(seconds/86400);
				textDate = Long.toString(days) + "d";
			}
		}

		catch (Exception e) {
			// TODO: handle parse error
		}

		return textDate;
	}

	// TODO: We can probably combine these i'm just lazy today
	public static String FormShackRSSDate(String unformattedDate) {
		String fixedDate = null;
		// 2009-11-19T10:10-06:00
		try {
			DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'h:mmZ");
			Date conDate = null;
			SimpleDateFormat format = null;

			conDate = dfm.parse(unformattedDate);
			format = new SimpleDateFormat("M.d.yy h:mma");
			fixedDate = format.format(conDate);

		}
		catch (ParseException e) {
			return "";
		}
		return fixedDate;
	}

	public static String FormatLOLDate(String unformattedDate) {
		String fixedDate = null;
		// Wed, 24 Feb 2010 09:49:00 -0800
		try {
			DateFormat dfm = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
			Date conDate = null;
			SimpleDateFormat format = null;

			conDate = dfm.parse(unformattedDate);
			format = new SimpleDateFormat("M.d.yy h:mma");
			fixedDate = format.format(conDate);

		}
		catch (ParseException e) {
			return "";
		}
		return fixedDate;
	}

	public static String GetCurrentChattyStoryID() {
		// NOTE: testing thread
		// return "61661;"

		try {
			URL url = new URL("http://www.shacknews.com/latestchatty.x");
			URLConnection connection = url.openConnection();
			HttpURLConnection httpConnection = (HttpURLConnection) connection;
			httpConnection.connect();
			int response = httpConnection.getResponseCode();

			if (response == 200) {
				String location = httpConnection.getURL().toString();
				String id = location.substring(location.lastIndexOf("=") + 1);

				return id;
			}
			else
				return null;

		}
		catch (Exception e) {
			return null;
		}
	}

	public static int CheckForNewShackMessages(Context context) throws IOException, SAXException, ParserConfigurationException {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String login = prefs.getString("shackLogin", "");
		String password = prefs.getString("shackPassword", "");

		if (login.length() == 0 || password.length() == 0)
			return -1;

		URL url = new URL("http://shackapi.stonedonkey.com/messages/?box=inbox&page=1");
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
		int totalResults = -1;
		try {
			totalResults = Integer.parseInt(saxHandler.getTotalResults());
		}
		catch (Exception ex) {
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

		for (int i = 0; i < messages.size(); i++) {
			msg = messages.get(i);
			if (msg.getMessageStatus().equals("unread"))
				newMessageCount++;
			else
				break;
		}

		Log.d("ShackDroid", "Last Msg: " + String.valueOf(lastMessageID));
		Log.d("ShackDroid", "Current Msg: " + String.valueOf(currentMessageID));

		if (lastMessageID < currentMessageID && lastMessageID >= 0 && messages.get(0).getMessageStatus().equals("unread")) {

			Log.d("ShackDroid", "FIRE ALERT!");

			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager nm = (NotificationManager) context.getSystemService(ns);

			int icon = R.drawable.shack_logo;
			CharSequence tickerText = "New Shack Message";

			Notification note = new Notification(icon, tickerText, 0);

			CharSequence contentTitle = "New Shack Message";
			CharSequence contentText = newMessageCount + " unread messages";
			Intent notificationIntent = new Intent(context, ActivityMessages.class);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

			note.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
			note.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;

			note.ledARGB = 0xFF800080;
			note.ledOnMS = 100;
			note.ledOffMS = 100;
			// note.defaults = Notification.DEFAULT_SOUND;
			note.sound = Uri.parse("android.resource://com.stonedonkey.shackdroid/" + R.raw.alert1);
			nm.notify(1, note);

		}

		if (currentMessageID > 0)
			UpdateLastShackMessageId(context, currentMessageID);

		return newMessageCount;
	}

	public static void UpdateLastShackMessageId(Context context, int messageID) throws IOException {
		try {
			FileOutputStream fos = context.openFileOutput("shackmessage.cache", Context.MODE_PRIVATE);
			ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeInt(messageID);
			os.close();
			fos.close();
		}
		catch (Exception ex) {
			Log.e("ShackDroid", "Error Saving Last Shack Message: " + ex.getMessage());
		}
	}

	public static int GetLastShackMessageId(Context context) throws StreamCorruptedException, IOException {
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

	// Stolen from ActivityThreadedView :(
	public static ArrayList<ShackPost> getPostTreeById(String feedURL, boolean isNWS, String postID, Context ctx) {
		try {
			if (isNWS)
				feedURL = "http://shackapi.stonedonkey.com";

			final URL url = new URL(feedURL + "/thread/" + postID + ".xml");

			// Get a SAXParser from the SAXPArserFactory.
			final SAXParserFactory spf = SAXParserFactory.newInstance();
			final SAXParser sp = spf.newSAXParser();

			// Get the XMLReader of the SAXParser we created.
			final XMLReader xr = sp.getXMLReader();

			// Create a new ContentHandler and apply it to the XML-Reader
			final SaxHandlerTopicView saxHandler = new SaxHandlerTopicView(ctx, "threaded");
			xr.setContentHandler(saxHandler);

			// Parse the xml-data from our URL.
			xr.parse(new InputSource(HttpHelper.HttpRequestWithGzip(url.toString(), ctx)));

			// Our ExampleHandler now provides the parsed data to us.
			return saxHandler.GetParsedPosts();
		}
		catch (Exception e) {

		}
		return null;
	}

	public static void SetWindowState(Window window, Context context) {
		SetWindowState(window, context, null);
	}

	public static void SetWindowState(Window window, Context context, Integer setOrientation) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		if (setOrientation == null) {
			int orientation = Integer.parseInt(prefs.getString("orientation", "4"));
			((Activity) context).setRequestedOrientation(orientation);
		}
		else
			((Activity) context).setRequestedOrientation(setOrientation);

		if (prefs.getBoolean("allowFullScreen", false)) {
			window.requestFeature(Window.FEATURE_NO_TITLE);
			window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

	public static ShackGestureListener setGestureEnabledContentView(int resourceId, Activity activity) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);

		// Add a gesture listener if we're 1.6 or greater.
		if (Integer.parseInt(android.os.Build.VERSION.SDK) > 3 && prefs.getBoolean("useGestures", false)) {
			return HelperAPI4.setGestureEnabledContentView(resourceId, activity);
		}
		else {
			activity.setContentView(resourceId);
			return null;
		}
	}

	public static boolean CheckAllowSMService(Context context) {
		// make sure we are allowed and that we have a login and password
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean allowSMs = prefs.getBoolean("allowShackMessages", false);
		boolean allowCheckShackMessages = prefs.getBoolean("allowCheckShackMessages", false);
		String login = prefs.getString("shackLogin", "");
		String password = prefs.getString("shackPassword", "");

		if (allowSMs == false || allowCheckShackMessages == false || login.length() == 0 || password.length() == 0)
			return false;
		else
			return true;
	}

	public static void setSMAlarm(Context context) {

		AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pi; // = PendingIntent.getBroadcast(context, 0, new Intent(context, ShackDroidServicesReceiver.class), PendingIntent.FLAG_NO_CREATE);

		// Get an inexact alarm so we fall in line with other apps waking the phone. will run every 15 mins....ish.
		// If we change it to AlarmManager.ELAPSED_REALTIME it doesn't run when the phone is off.....I think.

		// TODO: This re-fires if already active, it'd be nice to not have it start again if this
		// function is called.. not sure how to do that just yet.

		// if (pi == null) // pi is null if we don't have an existing matching intent
		// {
		pi = PendingIntent.getBroadcast(context, 0, new Intent(context, ShackDroidServicesReceiver.class), 0);
		m.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1500, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi);
		Log.d("ShackDroid", "SM Alarm Set");
		// }
		// else {
		// Log.d("ShackDroid", "SM Alarm Already Set - Using Existing");
		// }

	}

	public static void clearSMAlarm(Context context) {
		AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(context, ShackDroidServicesReceiver.class), 0);

		m.cancel(pi);
		Log.d("ShackDroid", "Canceled SM Alarms");

	}

	public static String getUserAgentString(Context context) {
		return "ShackDroid/" + context.getString(R.string.version_id);
	}

	public static int getThreadReplyCount(int threadId, Context context) {
		String result = "";
		URL url;
		try {
			url = new URL("http://shackapi.stonedonkey.com/thread/" + threadId + ".xml");
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
		}
		catch (Exception e) {
			result = "";
		}

		if (result.length() > 0) {
			try {
				// regex
				String regex = "reply_count=\"([0-9]*)\"";
				Pattern pattern = Pattern.compile(regex, Pattern.DOTALL | Pattern.MULTILINE);
				Matcher matcher = pattern.matcher(result);

				if (matcher.find()) {
					return Integer.parseInt(matcher.group(1));
				}
				else
					return 0;

			}
			catch (Exception e) {
				Log.e("ShackDroid", "Error parsing thread");
			}
		}
		return 0;
	}

	public static String getShackStory(int storyID, Context context) throws JSONException {

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

		}
		catch (Exception e) {

			result = "";
		}

		if (result.length() > 0) {
			JSONObject json = new JSONObject(result);
			final String body = json.getString("body");
			final String name = json.getString("name");

			return "<h3>" + name + "</h3>" + body;
		}
		else
			return "Story could not be loaded at this time.";

	}

	public static int GetTimeLeftDrawable(String postDate) {
		final DateFormat dfm = new SimpleDateFormat("MMM d, y hh:mmaa z");
		try {
			final Date conDate = dfm.parse(postDate);
			Date date = new Date();

			long timeStampPostDate = conDate.getTime() / 1000;
			final long currentTimeStamp = date.getTime() / 1000;

			final long timeDifference = currentTimeStamp - timeStampPostDate;
			if (timeDifference < 57600) // 8 hours
				return R.drawable.hourblank;
			else if (timeDifference > 57600 && timeDifference < 79200) // two to eight
				return R.drawable.hourfull;
			else if (timeDifference > 79200 && timeDifference < 82800) // one hour to two
				return R.drawable.hourmid;
			else if (timeDifference > 82800) // less than an hour
				return R.drawable.hourlow;

		}
		catch (Exception ex) {
			return R.drawable.hourblank;
		}

		return R.drawable.hourblank;
	}

	public static String GetTimeLeftString(String postDate) {
		final DateFormat dfm = new SimpleDateFormat("MMM d, y hh:mmaa z");
		try {
			final Date conDate = dfm.parse(postDate);
			Date date = new Date();
			long timeStampPostDate = conDate.getTime();
			final long currentTimeStamp = date.getTime();
			final long timeDifference = 86400000 - (currentTimeStamp - timeStampPostDate);

			if (timeDifference < 0)
				return "Thread expired.";
			else {
				long x = timeDifference / 1000;
				x /= 60;
				final int minutes = (int) (x % 60);
				x /= 60;
				final int hours = (int) (x % 24);

				String hourString = " hours ";
				String minuteString = " minutes";

				if (hours == 1)
					hourString = " hour ";

				if (minutes == 1)
					minuteString = " minute";

				if (hours > 0)
					return hours + hourString + minutes + minuteString;
				else
					return minutes + minuteString;

			}

		}
		catch (Exception ex) {
			return ex.getMessage();
		}
	}

	public static void ShowToastMessage(Context context, String message, int duration) {
		CharSequence text = message;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

	public static String ParseShackText(String text, boolean addSpoilerMarkers) {

		// make a youtoobs thumbnail
		// http://img.youtube.com/vi/{videoid}/default.jpg

		// Pattern p = Pattern.compile("href=.*>(.*www.youtube.com.*)</a>");
		// Matcher m = p.matcher(text);
		// if (m.find())
		// {
		// String link = m.group();
		// link = text.replaceAll("http://www.youtube.com/watch\\?v=(.*?)\\W", "<img src='http://img.youtube.com/vi/$1/default.jpg'>");
		// }

		// fix for links from main site page
		text = text.replaceAll("Comment on <a href=\"/article/", "Comment on <a href=\"http://www.shacknews.com/article/");

		// String regex = "Comment on <a href=\"(/article/[0-9]*/(.*?)\")>.*?</a>";
		// Pattern pattern = Pattern.compile(regex,Pattern.DOTALL|Pattern.MULTILINE);
		// Matcher matcher= pattern.matcher(text);
		//
		// if (matcher.find())
		// {
		// text = matcher.replaceAll("http://www.shacknews.com" + matcher.group(1));
		// }

		// Convert the shack spans into HTML fonts since our TextView can convert stuff to HTML
		// not sure if this is the best or most efficient, but works.
		text = text.replaceAll("<span class=\"jt_red\">(.*?)</span>", "<font color=\"#ff0000\">$1</font>");
		text = text.replaceAll("<span class=\"jt_green\">(.*?)</span>", "<font color=\"#8dc63f\">$1</font>");
		text = text.replaceAll("<span class=\"jt_pink\">(.*?)</span>", "<font color=\"#f49ac1\">$1</font>");
		text = text.replaceAll("<span class=\"jt_olive\">(.*?)</span>", "<font color=\"#808000\">$1</font>");
		text = text.replaceAll("<span class=\"jt_fuchsia\">(.*?)</span>", "<font color=\"#c0ffc0\">$1</font>");
		text = text.replaceAll("<span class=\"jt_yellow\">(.*?)</span>", "<font color=\"#ffde00\">$1</font>");
		text = text.replaceAll("<span class=\"jt_blue\">(.*?)</span>", "<font color=\"#44aedf\">$1</font>");
		text = text.replaceAll("<span class=\"jt_lime\">(.*?)</span>", "<font color=\"#c0ffc0\">$1</font>");
		text = text.replaceAll("<span class=\"jt_orange\">(.*?)</span>", "<font color=\"#f7941c\">$1</font>");
		text = text.replaceAll("<span class=\"jt_bold\">(.*?)</span>", "<b>$1</b>");
		text = text.replaceAll("<span class=\"jt_italic\">(.*?)</span>", "<i>$1</i>");
		text = text.replaceAll("<span class=\"jt_underline\">(.*?)</span>", "<u>$1</u>");
		text = text.replaceAll("<span class=\"jt_strike\">(.*?)</span>", "<s>$1</s>");

		// You can only do "highlights" on the actual TextView itself, so we mark up spoilers
		// !!-text-!! like so, and then handle it on the appling text to the TextView
		if (addSpoilerMarkers == true) {
			text = text.replaceAll("<span class=\"jt_spoiler\"(.*?)>(.*?)</span>", "!!-$2-!!");
		}
		return text;
	}

	public static class SortByPostIDComparator implements Comparator<ShackPost> {
		@Override
		public int compare(ShackPost object1, ShackPost object2) {
			return Integer.valueOf(object2.getPostID()) - Integer.valueOf(object1.getPostID());
		}

		/**
		 * Used to sort the post array based on the OrderID
		 */
	}

	public static class SortByOrderIDComparator implements Comparator<ShackPost> {
		@Override
		public int compare(ShackPost object1, ShackPost object2) {
			return object1.getOrderID() - object2.getOrderID();
		}

	}

	public static class ShackURLMatchFilter implements MatchFilter {
		@Override
		public boolean acceptMatch(CharSequence s, int start, int end) {
			return true;
		}
	}

	public static class ShackURLTransform implements TransformFilter {

		@Override
		public String transformUrl(Matcher match, String url) {

			// String test = url;

			return null;
		}
	}

}
