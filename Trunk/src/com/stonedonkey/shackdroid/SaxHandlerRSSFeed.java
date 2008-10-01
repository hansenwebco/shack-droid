package com.stonedonkey.shackdroid;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;

public class SaxHandlerRSSFeed extends DefaultHandler {

	@SuppressWarnings("unused")
	private Context context = null;
		
	private ArrayList<ShackRSS> rssItems = new ArrayList<ShackRSS>();
	
	private Boolean item = false;
	private Boolean title = false;
	private Boolean description = false;
	private Boolean link = false;
	private Boolean datePosted = false;

	private String titleText = "";
	private String descriptionText ="";
	private String linkText = "";
	private String datePostedText = "";

	
	public SaxHandlerRSSFeed(Context context) {
		this.context =context;
	}

	public ArrayList<ShackRSS> getRssItems() {
		return rssItems;
	}

	@Override
	public void startElement(final String nsURL, final String localName, final String rawName,final Attributes attributes) throws SAXException
	{
		if ("item".equalsIgnoreCase(localName))
			this.item = true;
		
		if ("title".equalsIgnoreCase(localName))
			this.title = true;
		
		if ("description".equalsIgnoreCase(localName))
			this.description = true;

		if ("link".equalsIgnoreCase(localName))
			this.link = true;
		
		if ("date".equalsIgnoreCase(localName))
			this.datePosted = true;
				
		
	}
	@Override
	public void endElement(final String nsURI, final String localName,final String rawName)throws SAXException 
	{
		
		if ("title".equalsIgnoreCase(localName))
			this.title = false;
		
		if ("description".equalsIgnoreCase(localName))
			this.description = false;

		if ("link".equalsIgnoreCase(localName))
			this.link = false;
		
		if ("date".equalsIgnoreCase(localName))
			this.datePosted = false;
		
		if ("item".equalsIgnoreCase(localName))
		{
			this.item = false;
			
			ShackRSS rssItem = new ShackRSS(titleText, descriptionText, linkText, datePostedText);
			rssItems.add(rssItem);
			
			titleText="";
			descriptionText="";
			linkText="";
			datePostedText="";
		}
		
		
	}
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException 
	{
		if (title && item)
			titleText = titleText +  new String(ch,start,length);
		
		if (description && item)
			descriptionText = descriptionText +  new String(ch,start,length);
		
		if (link && item)
			 linkText = linkText + new String(ch,start,length);
		
		if (datePosted && item)
			datePostedText = datePostedText +  new String(ch,start,length);
		
	}
}
