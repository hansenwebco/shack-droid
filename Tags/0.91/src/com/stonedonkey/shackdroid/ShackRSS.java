package com.stonedonkey.shackdroid;

public class ShackRSS {

	private String title;
	private String description;
	private String link;
	private String datePosted;
	
	public ShackRSS(String title, String description, String link, String datePosted)
	{
		this.title = title;
		this.description  = description;
		this.link = link;
		this.datePosted = datePosted;
	}

	public String getTitle() {
		return title;
	}
	public  String getDescription() {
		return description;
	}
	public  String getLink() {
		return link;
	}
	public  String getDatePosted() {
		return datePosted;
	}
	
}
