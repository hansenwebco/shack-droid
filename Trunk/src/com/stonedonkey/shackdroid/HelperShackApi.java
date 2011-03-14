package com.stonedonkey.shackdroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import android.net.Uri;

public class HelperShackApi {
	private static final Uri API_BASE = Uri.parse("http://www.shacknews.com/api/");
	
	public static String doPost(String userAgent, String login, String password, String storyID, String postID, String postText){

		try {
			String data = URLEncoder.encode("content_type_id", "UTF-8") + "="
						+ URLEncoder.encode("17", "UTF-8") + "&"
						+ URLEncoder.encode("content_id", "UTF-8") + "="
						+ URLEncoder.encode(storyID, "UTF-8") + "&"
						+ URLEncoder.encode("body", "UTF-8") + "="
						+ URLEncoder.encode(postText, "UTF-8");

			if (postID != null && postID.length() > 0)
			{
				data = data + "&" + URLEncoder.encode("parent_id", "UTF-8") + "="
				+ URLEncoder.encode(postID, "UTF-8");
			}

			String userPassword = login + ":" + password;
			String encoding = Base64.encodeBytes(userPassword.getBytes());
			return jsonPost(userAgent, encoding, data);
		

		}catch(Exception e){}
		return "";
	}
	
	private static String jsonPost(String userAgent, String userPass, String data) throws IOException{
		// post to ShackNews
		///http://www.shacknews.com/api/chat/create/17.json
		URL url =  new URL(Uri.withAppendedPath(API_BASE, "create/17.json").toString());//  new URL("http://www.shacknews.com/api/chat/create/17.json");
		
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("Authorization", "Basic " + userPass);
		conn.setRequestProperty("User-Agent", userAgent);	
		conn.setDoOutput(true);
		
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(data);
		wr.flush();
		wr.close();
		
		// Capture reponse for handling
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		String result = "";
		while ((line = rd.readLine()) != null) {
		result = result + line;
		}
		rd.close();
		
		return result;
	}
}
