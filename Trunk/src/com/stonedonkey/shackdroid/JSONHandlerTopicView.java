package com.stonedonkey.shackdroid;

import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class JSONHandlerTopicView {

	private ArrayList<ShackPost> posts = new ArrayList<ShackPost>();
	
	public JSONHandlerTopicView(Context context)
	{
		
		try {
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		final String feedURL = prefs.getString("shackFeedURL",context.getString(R.string.default_api));
		final String url =feedURL + "/index.json";
 
		

		
		// lets get the URL
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		ResponseHandler handler = new BasicResponseHandler();
		//you result will be String :
		String result = (String) httpclient.execute(request, handler);
		
		//JSONObject json = (JSONObject) JSONSerializer.toJSON( jsonTxt );
		final JSONObject json = new  JSONObject(result);
		
		JSONArray comments = json.getJSONArray("comments");
		JSONObject comment = comments.getJSONObject(0);
		
		final String id = comment.getString("id");
		final String category = comment.getString("category");
		
		int i= 1;
		
		}
		catch (Exception ex)
		{
			String fail;
			fail = ex.getMessage();
		}
		
		
	}
	
}
