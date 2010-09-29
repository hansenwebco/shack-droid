package com.stonedonkey.shackdroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

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
	
	public ArrayList<ShackPost> GetParsedPostsAndroid(String json)
	{
		
		long startTime = System.currentTimeMillis();
		
		try {
			
			final URL url= new URL("http://shackapi.stonedonkey.com/index.json");
			
			// *********************************************************************************
			// * Basic Json Parse using default engine                                         * 
			// *********************************************************************************
			//final InputStream json= HttpHelper.HttpRequestWithGzip(url.toString(),_context);
			//final JSONObject jsonResponse = new JSONObject(convertStreamToString(json));
			final JSONObject jsonResponse = new JSONObject(json);
			String page = jsonResponse.getString("page");
			//JSONArray comments = jsonResponse.getJSONArray("comments");
			//JSONObject comment = comments.getJSONObject(1);
			//String preview = comment.getString("preview");

		}
		catch (Exception ex)
		{
			Log.e("ShackDroid","Error Parsing JSON");
		}
		
		long endTime = System.currentTimeMillis();
		long runTime =endTime - startTime;
		Log.i("ShackDroid", "GetParsedPostsAndroid Time: " + runTime );
		
		return this.posts;
	}
	public ArrayList<ShackPost> GetParsedPostsJson(String json)
	{
		// *********************************************************************************
		// * Jackson Tree Model Parser                                                     * 
		// *********************************************************************************		
		
		long startTime = System.currentTimeMillis();
		
		try {
			final URL url= new URL("http://shackapi.stonedonkey.com/index.json");

			ObjectMapper m = new ObjectMapper();
			//InputStream stream = HttpHelper.HttpRequestWithGzip(url.toString(), _context);
			//JsonNode rootNode = m.readTree(stream);
			JsonNode rootNode = m.readTree(json);
			//String page = rootNode.path("page").getTextValue();
			//JsonNode comments = rootNode.path("comments");
			//String preview = comments.get(0).path("preview").getTextValue();
		}
		catch (Exception ex)
		{
			Log.e("ShackDroid","Error Parsing JSON");
		}
		
		long endTime = System.currentTimeMillis();
		long runTime =endTime - startTime;
		Log.i("ShackDroid", "GetParsedPostsJSON Time: " + runTime );
		
		return this.posts;
	}
	
	private static String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

	
}
