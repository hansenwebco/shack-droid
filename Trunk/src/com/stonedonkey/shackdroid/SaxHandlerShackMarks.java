package com.stonedonkey.shackdroid;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SaxHandlerShackMarks extends DefaultHandler {
	
	private ArrayList<ShackPost> marks = new ArrayList<ShackPost>();	

	private boolean body = false;
	private String bodyText = "";
	private String posterName = "";
	private String postID = "";
	private String postDate = "";
	private String preview = "";

	public ArrayList<ShackPost> GetParsedPosts()
	{
		return this.marks;
	}
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {

		if("comment".equalsIgnoreCase(localName))
		{
			
			posterName = attributes.getValue("author");
			postDate = attributes.getValue("date");
			preview = attributes.getValue("preview");
			postID = attributes.getValue("id");
		}
		
		if ("body".equalsIgnoreCase(localName))
			body = true;
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {

		if ("body".equalsIgnoreCase(localName)) 
		{
			body = false;
			
			ShackPost currentPost = new ShackPost( posterName, postDate, preview ,postID, bodyText, "0", 0,"ontopic",0,marks.size());
			marks.add(currentPost);
			
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {

		
		if (body)
			bodyText = bodyText + new String(ch,start,length);
		
	}

}
