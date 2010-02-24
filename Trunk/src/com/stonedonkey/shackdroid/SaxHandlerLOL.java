package com.stonedonkey.shackdroid;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;




public class SaxHandlerLOL extends DefaultHandler {

	
	private String bodyText = "";
	private String taggerText ="";
	private Boolean bodyResult = false;
	private Boolean taggerResult = false;
	private String commentDate;
	private String tag;
	private String tagCount;
	private String author;
	private String id;
	private String category;
	
	private ArrayList<ShackLOL> posts = new ArrayList<ShackLOL>();

	
	
	public ArrayList<ShackLOL> getMessages() {
		return posts;
	}
	
	@Override
	public void startElement(final String nsURL, final String localName,
	final String rawName,final Attributes attributes) throws SAXException
	{
		if ("comment".equalsIgnoreCase(localName))
		{
			commentDate = attributes.getValue("date");
			tag = attributes.getValue("tag");
			tagCount = attributes.getValue("tag_count");
			author = attributes.getValue("author");
			id = attributes.getValue("id");
			category = attributes.getValue("category");
		}
		
		if ("body".equalsIgnoreCase(localName)) 
		{
			bodyResult = true;
		}
		if ("taggers".equalsIgnoreCase(localName)) 
		{
			taggerResult = true;
		}
	}
	
	@Override
	public void endElement(final String nsURI, final String localName,
            final String rawName)
			throws SAXException {
		
		if ("body".equalsIgnoreCase(localName)) 
		{
			bodyResult = false;
		}
		
		if ("taggers".equalsIgnoreCase(localName)) 
		{
			taggerResult = false;
			
			ShackLOL post = new ShackLOL();
			post.setBody(bodyText);
			post.setTaggers(taggerText);
			post.setCommentDate(commentDate);
			post.setTag(tag);
			post.setTagCount(tagCount);
			post.setAuthor(author);
			post.setId(id);
			post.setCategory(category);
		
			posts.add(post);
			
			bodyResult = false;
			taggerResult = false;
			
			bodyText = "";
			taggerText = "";
		}
		
		
	}
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
			
		if (bodyResult)
			bodyText = bodyText + new String(ch,start,length);
		
		if (taggerResult)
			taggerText = taggerText + new String(ch,start,length);
		
	}
}
