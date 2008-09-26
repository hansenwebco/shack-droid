package com.stonedonkey.shackdroid;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class TopicViewSaxHandler extends DefaultHandler
{
	private ArrayList<ShackPost> posts = new ArrayList<ShackPost>();	
	private ArrayList<Integer> indent = new ArrayList<Integer>();
	
	private boolean body = false;

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
	@SuppressWarnings("unused")
	private boolean comments = false;
	@SuppressWarnings("unused")
	private boolean comment = false;
	@SuppressWarnings("unused")
	private Context context = null;
	
	public TopicViewSaxHandler(Context context)
	{
		this.context = context;
		
		// set our preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		allowNWS = prefs.getBoolean("allowNWS", true);
		allowPolitical = prefs.getBoolean("allowPolitical", true);
		allowStupid = prefs.getBoolean("allowStupid", true);
		allowInteresting = prefs.getBoolean("allowInteresting", true);
		allowOffTopic = prefs.getBoolean("allowOffTopic", true);
	}


	public ArrayList<ShackPost> GetParsedPosts()
	{
		return this.posts;
	}
	public String getStoryID()
	{
		return this.storyID;
	}
	public String getStoryTitle()
	{
		return this.storyName;
	}
	public int getStoryPageCount()
	{
		return this.storyPageCount;
	}
	
	@Override
	public void startElement(final String nsURL, final String localName,
	final String rawName,final Attributes attributes) throws SAXException
	{
		if("comments".equalsIgnoreCase(localName))
		{
			if (storyID.length() == 0 && attributes.getValue("story_id") != null ) // only the first node has it
				storyID = attributes.getValue("story_id");
			 
			if (storyName.length() == 0 && attributes.getValue("story_name") != null ) // only the first node has it
				storyName = attributes.getValue("story_name");
			
			if (storyPageCount == 0 && attributes.getValue("last_page") != null  ) // only the first node has it
				if (attributes.getValue("last_page").length() > 0)
					storyPageCount = Integer.parseInt(attributes.getValue("last_page"));
			
			comments = true; 
		}
				
		if("comment".equalsIgnoreCase(localName))
		{
			comment= true;
			posterName = attributes.getValue("author");
			postDate = attributes.getValue("date");
			preview = attributes.getValue("preview");
			postID = attributes.getValue("id");
			replyCount = attributes.getValue("reply_count");
			postCategory = attributes.getValue("category");
		}
		
		if ("body".equalsIgnoreCase(localName))
			body = true;
		
	}
	
	@Override
	public void endElement(final String nsURI, final String localName,
            final String rawName)
			throws SAXException {
	
		if ("comments".equalsIgnoreCase(localName))
		{
			comments = false;
		}
		if ("comment".equalsIgnoreCase(localName))
		{
			comment = false;
		}			
	 
		if ("body".equalsIgnoreCase(localName)) 
		{
			body = false;
		
			// this handles determining how far in a reply is indented
			int currentIndent;
			if (indent.size() > 0)
			 currentIndent = indent.size();
			else
			 currentIndent = 0;
			
			for (int i = 0 ; i < indent.size() ; i++)
			{
				indent.set(i, indent.get(i) - 1 );
				if (indent.get(i) == 0)
				{
					indent.remove(i);
					i--;
				}
			}
			
			if (Integer.parseInt(replyCount) > 0)
				indent.add(Integer.parseInt(replyCount));
			// end indention code
				
		
			// check to see if it passes the persons filters, if it does not
			// then we don't add it
			if ( 
					(postCategory.equals("nws") && allowNWS != true) ||
					(postCategory.equals("offtopic") && allowOffTopic != true) ||
					(postCategory.equals("political") && allowPolitical != true) ||
					(postCategory.equals("stupid") && allowStupid != true) ||
					(postCategory.equals("informative") && allowInteresting != true) 
				)
			{
				//do nothing
			}
			else {
				// add new post to collection
				ShackPost currentPost = new ShackPost( posterName, postDate, preview,postID, bodyText, replyCount, currentIndent,postCategory,0,posts.size());
				posts.add(currentPost);
				bodyText= "";
			}
			
		}
					
	} 

		
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {

		if (body)
		{
		// do stuff here
			bodyText = bodyText + new String(ch,start,length);
		}
	}
}
