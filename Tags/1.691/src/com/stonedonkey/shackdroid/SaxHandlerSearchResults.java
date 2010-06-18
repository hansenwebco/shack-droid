package com.stonedonkey.shackdroid;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SaxHandlerSearchResults extends DefaultHandler {
	
	private ArrayList<ShackSearch> searchResults = new ArrayList<ShackSearch>();
	
	
	private String author;
	private String datePosted;
	private String storyName;
	private String id;
	private String storyID;
	private String resultText = "";
	private String totalPages = "0";
	private String totalResults = "0";
	
	private Boolean result = false;
		
	

	public ArrayList<ShackSearch> getSearchResults() {
		return searchResults;
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
		
		if("results".equalsIgnoreCase(localName))
		{
			totalPages= attributes.getValue("last_page");
			totalResults = attributes.getValue("total_results");
		}
		
		if("result".equalsIgnoreCase(localName))
		{
			author = attributes.getValue("author");
			datePosted =  attributes.getValue("date");
			storyName =  attributes.getValue("story_name");
			id =  attributes.getValue("id");
			storyID =  attributes.getValue("story_id");
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
			
			ShackSearch ss = new ShackSearch(author, datePosted, storyName, id, storyID,  resultText);
			searchResults.add(ss);
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
