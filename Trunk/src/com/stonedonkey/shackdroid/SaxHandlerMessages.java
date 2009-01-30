package com.stonedonkey.shackdroid;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SaxHandlerMessages extends DefaultHandler {
	
	private String author;
	private String datePosted;
	private String subject;
	private String id;
	private String messageStatus;
	private Boolean result = false;
	//private String storyID;
	private String resultText = "";
	private String totalPages = "0";
	private String totalResults = "0";
	
	private ArrayList<ShackMessage> messages = new ArrayList<ShackMessage>();
	
	public ArrayList<ShackMessage> getMessages() {
		return messages;
	}
	public String getTotalPages()
	{
		return totalPages;
	}
	public String getTotalResults()
	{
		return totalResults;
	}
	
	@Override
	public void startElement(final String nsURL, final String localName,
	final String rawName,final Attributes attributes) throws SAXException
	{
		
		if("messages".equalsIgnoreCase(localName))
		{
			totalPages= attributes.getValue("last_page");
			totalResults = attributes.getValue("total_results");
		}
		
		if("message".equalsIgnoreCase(localName))
		{
			author = attributes.getValue("author");
			datePosted =  attributes.getValue("date");

			String tSubject = attributes.getValue("subject");
			if (tSubject.contains("Re:")) // clean up shacks notorius RE: RE: RE: RE: RE:
				tSubject = "Re: " + tSubject.replace("Re:", "").trim();

			
			subject = tSubject;
			id =  attributes.getValue("id");
			messageStatus = attributes.getValue("status");
			
		}
		if ("body".equalsIgnoreCase(localName))
			result = true;
		
	}
	@Override
	public void endElement(final String nsURI, final String localName,
            final String rawName)
			throws SAXException {
		 
		if ("body".equalsIgnoreCase(localName)) 
		{
			result = false;
			
			ShackMessage msg = new ShackMessage(author, subject, datePosted, resultText, id, messageStatus);
			messages.add(msg);
			resultText = "";
			
		}
		
		
	}
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {

		if (result)
			resultText = resultText + new String(ch,start,length);
	}
	
}
