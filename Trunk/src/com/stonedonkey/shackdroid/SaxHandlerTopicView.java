package com.stonedonkey.shackdroid;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SaxHandlerTopicView extends DefaultHandler
{
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

	
	public SaxHandlerTopicView(Context context, String v)
	{
		//this.context = context;
		
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
			
		
		}   
				
		if("comment".equalsIgnoreCase(localName))
		{
			
			posterName = attributes.getValue("author");
			postDate = attributes.getValue("date");
			preview = attributes.getValue("preview");
			postID = attributes.getValue("id");
			replyCount = attributes.getValue("reply_count");
			postCategory = attributes.getValue("category");
		}
		
		if ("participant".equalsIgnoreCase(localName))
			participant = true;
		
		if ("body".equalsIgnoreCase(localName))
			body = true;
		
	}
	
	@Override
	public void endElement(final String nsURI, final String localName,
            final String rawName)
			throws SAXException {
		 
		if ("participant".equalsIgnoreCase(localName))
		{
			participant = false;
		}
		
		if (("body".equalsIgnoreCase(localName) && view.equalsIgnoreCase("threaded")) || ("comment".equalsIgnoreCase(localName) && view.equalsIgnoreCase("topic"))) 
		{
			body = false;
			
		
			// this handles determining how far in a reply is indented
			int currentIndent;
			int indentSize = indent.size();
			
			
			if (indentSize > 0)
			 currentIndent = indentSize;
			else
			 currentIndent = 0;
			
			for (int i = 0 ; i < indentSize ; i++)
			{
				indent.set(i, indent.get(i) - 1 );
				if (indent.get(i) == 0)
				{
					indent.remove(i);
					indentSize--;
					i--;
				}
			}
			
			if (Integer.parseInt(replyCount) > 0)
				indent.add(Integer.parseInt(replyCount));
			// end indention code
				
		
			// check to see if it passes the persons filters, if it does not
			// then we don't add it
			if ( ((postCategory.equals("nws") && allowNWS == true) ||
				 (postCategory.equals("offtopic") && allowOffTopic == true) ||
				 (postCategory.equals("political") && allowPolitical == true) ||
				 (postCategory.equals("stupid") && allowStupid == true) ||
				 (postCategory.equals("informative") && allowInteresting == true)) ||
				 (postCategory.equals("ontopic")))
			{
				// add new post to collection
				ShackPost currentPost = new ShackPost( posterName, postDate, preview ,postID, bodyText, replyCount, currentIndent,postCategory,0,posts.size(),author,null);
				posts.add(currentPost);
				bodyText= "";
				author = false;
			}
			
		}
					
	} 
		
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {

		if (body)
			bodyText = bodyText + new String(ch,start,length);
		
		if (participant)
		{
			String part = new String(ch,start,length);
			if (part.equalsIgnoreCase(username))
			{
				author = true;
			}
		}
		
		
	}
}
