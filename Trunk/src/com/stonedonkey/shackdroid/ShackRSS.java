package com.stonedonkey.shackdroid;

import java.io.Serializable;

public final class ShackRSS implements Serializable {


	private static final long serialVersionUID = 8477799628748516544L;
	private String title;
	private String description;
	private String link;
	private String datePosted;
	private String id;
	
	public ShackRSS(String title, String description, String link, String datePosted,String id)
	{
		this.title = title;
		this.description  = description;
		this.link = link;
		this.datePosted = datePosted;
		this.id = id;
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
	public String getID()
	{
		return id;
	}
}
