package com.stonedonkey.shackdroid;

import java.io.Serializable;

public class ShackLOL implements Serializable{

	private static final long serialVersionUID = 1L;
	private String commentDate;
	private String tag;
	private String tagCount;
	private String author;
	private String id;
	private String category;
	private String body;
	private String taggers;
	
	public ShackLOL(String commentDate, String tag, String tagCount,String author, String id, String category,String body, String taggers)
	{
		this.setCommentDate(commentDate);
		this.setTag(tag);
		this.setTagCount(tagCount);
		this.setAuthor(author);
		this.setId(id);
		this.setCategory(category);
		this.setBody(body);
		this.setTaggers(taggers);
		
	}
	public ShackLOL()
	{
		
	}

	public void setCommentDate(String commentDate) {
		this.commentDate = commentDate;
	}

	public String getCommentDate() {
		return commentDate;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getTag() {
		return tag;
	}

	public void setTagCount(String tagCount) {
		this.tagCount = tagCount;
	}

	public String getTagCount() {
		return tagCount;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAuthor() {
		return author;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCategory() {
		return category;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getBody() {
		return body;
	}

	public void setTaggers(String taggers) {
		this.taggers = taggers;
	}

	public String getTaggers() {
		return taggers;
	}
	
}