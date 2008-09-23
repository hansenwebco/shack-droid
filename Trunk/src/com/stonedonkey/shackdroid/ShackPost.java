package com.stonedonkey.shackdroid;

public class ShackPost {

	private String posterName = null;
	private String postDate = null;
	private String postPreview = null;
	private String postID = null;
	private String postText = null;
	private String replyCount =null;
	private Integer indent = 0;
	private String postCategory;
	private Integer postIndex = null;
	private Integer orderID = 0;
	

	public ShackPost(String posterName, String postDate, String postPreview,
			String postID, String postText,String replyCount,Integer indent,String postCategory, Integer postIndex,Integer orderID) {
		
		this.posterName = posterName;
		this.postDate = postDate;
		this.postPreview = postPreview;
		this.postID = postID;
		this.postText = postText;
		this.replyCount = replyCount; 
		this.indent = indent;
		this.postCategory = postCategory;
		this.postIndex = postIndex;
		this.orderID = orderID; 
	}

	public Integer getOrderID()
	{
		return orderID;
	}
	public void setPostIndex(Integer postIndex)
	{
		this.postIndex = postIndex;
	}
	public Integer getPostIndex()
	{
		return postIndex;
	}

	public String getPostCategory()
	{
		return postCategory;
	}
	
	public Integer getIndent()
	{
		return indent;
	}
	public String getReplyCount()
	{
		return replyCount;
	}
	
	public String getPostText() {
		return postText;
	}

	public String getPosterName() {
		return posterName;
	}

	public String getPostDate() {
		return postDate;
	}

	public String getPostPreview() {
		return postPreview;
	}

	public String getPostID() {
		return postID;
	}

}
