package com.stonedonkey.shackdroid;

public class ShackSearch {

	private String author;
	private String datePosted;
	private String storyName;
	private String id;
	private String storyID;
	private String resultText;
	
	public ShackSearch(String author, String datePosted, String storyName, String id, String storyID, String resultText)
	{
		this.author = author;
		this.datePosted = datePosted;
		this.storyName = storyName;
		this.id = id;
		this.storyID = storyID;
		this.resultText = resultText;
	}


	public String getResultText() {
		return resultText;
	}
	public String getStoryID() {
		return storyID;
	}
	public String getId() {
		return id;
	}
	public String getStoryName() {
		return storyName;
	}
	public String getDatePosted() {
		return datePosted;
	}
	public String getAuthor() {
		return author;
	}
	
	
}

