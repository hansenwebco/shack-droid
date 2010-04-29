package com.stonedonkey.shackdroid;

import java.net.URL;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class JSONHandlerTopicView {

	//private ArrayList<ShackPost> posts = new ArrayList<ShackPost>();
	
	public JSONHandlerTopicView(Context context)
	{
		
		try {
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		final String feedURL = prefs.getString("shackFeedURL",context.getString(R.string.default_api));
		final URL url = new URL(feedURL + "/index.json");
 
		JsonFactory jsonFactory = new JsonFactory();
		JsonParser jp = jsonFactory.createJsonParser(url);
		
		jp.nextToken();
		while (jp.nextToken() != JsonToken.END_OBJECT)
		{
			final String fieldName = jp.getCurrentName();
			if (fieldName.equalsIgnoreCase("comments"))
			{
				
			}
					
			
			
		}
		 
		
		
		jp.close();
		
		
//		// lets get the URL
//		HttpClient httpclient = new DefaultHttpClient();
//		HttpGet request = new HttpGet(url);
//		ResponseHandler handler = new BasicResponseHandler();
//		//you result will be String :
//		String result = (String) httpclient.execute(request, handler);
//		
//		//JSONObject json = (JSONObject) JSONSerializer.toJSON( jsonTxt );
//		final JSONObject json = new  JSONObject(result);
//		
//		JSONArray comments = json.getJSONArray("comments");
//		JSONObject comment = comments.getJSONObject(0);
//		
//		final String id = comment.getString("id");
//		final String category = comment.getString("category");
		
		
		
		}
		catch (Exception ex)
		{
			
		}
		
		
	}
	
}
