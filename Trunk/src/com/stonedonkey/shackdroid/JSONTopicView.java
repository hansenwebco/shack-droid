package com.stonedonkey.shackdroid;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class JSONTopicView {
	
	private ArrayList<ShackPost> posts = new ArrayList<ShackPost>();	
	private ArrayList<Integer> indent = new ArrayList<Integer>();
	
	private boolean body = false;
	private boolean author = false;
	private boolean participant = false;


	Boolean allowNWS = true;
	Boolean allowPolitical = true;
	Boolean allowStupid =  true;
	Boolean allowInteresting = true;
	Boolean allowOffTopic = true;
	
	private String bodyText = "";
	private String posterName = "";
	private String postID = "";
	private String postDate = "";
	private String preview = "";
	private String replyCount ="";
	private String storyID = "";
	private String storyName = "";
	private String postCategory = "";
	private int storyPageCount = 0;
	private String view = "";
	private String username = "";
	
	private Context _context;
	
	public JSONTopicView(Context context, String v, String url)
	{
		_context = context;
		
		// set our preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		allowNWS = prefs.getBoolean("allowNWS", true);
		allowPolitical = prefs.getBoolean("allowPolitical", true);
		allowStupid = prefs.getBoolean("allowStupid", true);
		allowInteresting = prefs.getBoolean("allowInteresting", true);
		allowOffTopic = prefs.getBoolean("allowOffTopic", true);
		username = prefs.getString("shackLogin", "");
		
		view = v;
	
	}
	
	public ArrayList<ShackPost> GetParsedPosts()
	{
		try {


			JsonFactory jsonFactory = new JsonFactory();


			URL url= new URL("http://shackapi.stonedonkey.com/index.json");



			JsonParser jp = jsonFactory.createJsonParser(HttpHelper.HttpRequestWithGzip(url.toString(),_context));
			if (jp.nextToken() != JsonToken.START_OBJECT) {
				throw new IOException("Expected data to start with an Object");
			}


			while (jp.nextToken() != JsonToken.END_OBJECT) {
				String fieldname = jp.getCurrentName();
				jp.nextToken(); // move to value, or START_OBJECT/START_ARRAY
			}

		}
		catch (Exception ex)
		{

		}
		return this.posts;
	}
	
	
}
