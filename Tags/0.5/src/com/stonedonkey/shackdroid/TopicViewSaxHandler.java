package com.stonedonkey.shackdroid;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TopicViewSaxHandler extends DefaultHandler
{
	private ArrayList<ShackPost> posts = new ArrayList<ShackPost>();	
	private ArrayList<Integer> indent = new ArrayList<Integer>();
	
	private boolean body = false;

	private String bodyText = "";
	private String posterName = "";
	private String postID = "";
	private String postDate = "";
	private String preview = "";
	private String replyCount ="";
	private String storyID = "";
	private String storyName = "";
	private String postCategory = "";
	@SuppressWarnings("unused")
	private boolean comments = false;
	@SuppressWarnings("unused")
	private boolean comment = false;
	
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
	
	@Override
	public void startElement(final String nsURL, final String localName,
	final String rawName,final Attributes attributes) throws SAXException
	{
		if("comments".equalsIgnoreCase(rawName))
		{
			if (storyID.length() == 0 && attributes.getValue("story_id") != null ) // only the first node has it
				storyID = attributes.getValue("story_id");
			 
			if (storyName.length() == 0 && attributes.getValue("story_name") != null ) // only the first node has it
				storyName = attributes.getValue("story_name");
			
			comments = true; 
		}
				
		if("comment".equalsIgnoreCase(rawName))
		{
			comment= true;
			posterName = attributes.getValue("author");
			postDate = attributes.getValue("date");
			preview = attributes.getValue("preview");
			postID = attributes.getValue("id");
			replyCount = attributes.getValue("reply_count");
			postCategory = attributes.getValue("category");
		}
		
		if ("body".equalsIgnoreCase(rawName))
			body = true;
		
	}
	
	@Override
	public void endElement(final String nsURI, final String localName,
            final String rawName)
			throws SAXException {
	
		if ("comments".equalsIgnoreCase(rawName))
		{
			comments = false;
		}
		if ("comment".equalsIgnoreCase(rawName))
		{
			comment = false;
		}			
	
		if ("body".equalsIgnoreCase(rawName)) 
		{
			body = false;
		
			
			// this handles determining how far in a reply is indented
			// TODO: find a way to disable this logic during the main view
			// the threaded view only needs it
			Integer currentIndent;
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
				
			// add new post to collection
			ShackPost currentPost = new ShackPost( posterName, postDate, preview,postID, bodyText, replyCount, currentIndent,postCategory);

			posts.add(currentPost);
		}
					
	}
		
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {

		if (body)
		{
		// do stuff here
			bodyText = new String(ch,start,length);
		}
	}
}
