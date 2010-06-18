package com.stonedonkey.shackdroid;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import android.content.Context;

public class ShackDroidStats  implements Serializable {

	private static final long serialVersionUID = -6234264258840979021L;
	private int postsViewed = 0;			// *
	private int postsMade = 0;				// *
	private int lolsMade = 0; 				// *
	private int infsMade = 0;  				// *
	private int unfsMade = 0; 				// * 
	private int tagsMade = 0; 				// *
	private int vanitySearches = 0;			// *
	private int shackSearches = 0;			// *
	private int viewedShackMessages = 0;	//
	private int sentShackMessage = 0;		//
	private int checkedForNewVersion = 0;	// *
	private int viewedRSSFeed = 0;			// *
	private int viewedShackLOLs = 0;		// *
	private int viewedChatty = 0;			// *
	private int viewedStats = 0;			// *
	
	
	public static ShackDroidStats GetUserStats(Context context) throws StreamCorruptedException, IOException
	{
		if (context.getFileStreamPath("stats.cache").exists()) {
			
			ShackDroidStats stats = null;
			
			final FileInputStream fileIn = context.openFileInput("stats.cache");
			final ObjectInputStream in = new ObjectInputStream(fileIn);
			try {
				stats = (ShackDroidStats) in.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			in.close();
			fileIn.close();
			
			return stats;
		}
		else
			return new ShackDroidStats();
	}
	
	public static void UpdateUserStats(ShackDroidStats stats, Context context) throws IOException
	{
		if (stats == null)
			stats = new  ShackDroidStats();
				
		final FileOutputStream fos = context.openFileOutput("stats.cache",Context.MODE_PRIVATE);
		final ObjectOutputStream os = new ObjectOutputStream(fos);
		os.writeObject(stats);
		os.close();
		fos.close();	
		
	}
	public static void AddPostsViewed(Context context)
	{
		try {
			final ShackDroidStats stats = GetUserStats(context);
			stats.setPostsViewed(stats.getPostsViewed()+1);
			UpdateUserStats(stats,context);
		}
		catch (Exception ex){}
	}
	public static void AddPostsMade(Context context)
	{
		try {
			final ShackDroidStats stats = GetUserStats(context);
			stats.setPostsMade(stats.getPostsMade()+1);
			UpdateUserStats(stats,context);
		}
		catch (Exception ex){}
	}	
	public static void AddLOLsMade(Context context)
	{
		try {
			final ShackDroidStats stats = GetUserStats(context);
			stats.setLolsMade(stats.getLolsMade()+1);
			UpdateUserStats(stats,context);
		}
		catch (Exception ex){}
	}	
	public static void AddINFsMade(Context context)
	{
		try {
			final ShackDroidStats stats = GetUserStats(context);
			stats.setInfsMade(stats.getInfsMade()+1);
			UpdateUserStats(stats,context);
		}
		catch (Exception ex){}
	}	
	public static void AddUNFsMade(Context context)
	{
		try {
			final ShackDroidStats stats = GetUserStats(context);
			stats.setUnfsMade(stats.getUnfsMade()+1);
			UpdateUserStats(stats,context);
		}
		catch (Exception ex){}
	}	
	public static void AddTAGsMade(Context context)
	{
		try {
			final ShackDroidStats stats = GetUserStats(context);
			stats.setTagsMade(stats.getTagsMade()+1);
			UpdateUserStats(stats,context);
		}
		catch (Exception ex){}
	}	
	public static void AddVanitySearch(Context context)
	{
		try {
			final ShackDroidStats stats = GetUserStats(context);
			stats.setVanitySearches(stats.getVanitySearches()+1);
			UpdateUserStats(stats,context);
		}
		catch (Exception ex){}
	}	
	public static void AddShackSearch(Context context)
	{
		try {
			final ShackDroidStats stats = GetUserStats(context);
			stats.setShackSearches(stats.getShackSearches()+1);
			UpdateUserStats(stats,context);
		}
		catch (Exception ex){}
	}	
	public static void AddViewShackMessage(Context context)
	{
		try {
			final ShackDroidStats stats = GetUserStats(context);
			stats.setViewedShackMessages(stats.getViewedShackMessages()+1);
			UpdateUserStats(stats,context);
		}
		catch (Exception ex){}
	}	
	public static void AddSentShackMessage(Context context)
	{
		try {
			final ShackDroidStats stats = GetUserStats(context);
			stats.setSentShackMessage(stats.getSentShackMessage()+1);
			UpdateUserStats(stats,context);
		}
		catch (Exception ex){}
	}	
	public static void AddCheckedForNewVersion(Context context)
	{
		try {
			final ShackDroidStats stats = GetUserStats(context);
			stats.setCheckedForNewVersion(stats.getCheckedForNewVersion()+1);
			UpdateUserStats(stats,context);
		}
		catch (Exception ex){}
	}	
	public static void AddViewedRssFeed(Context context)
	{
		try {
			final ShackDroidStats stats = GetUserStats(context);
			stats.setViewedRSSFeed(stats.getViewedRSSFeed()+1);
			UpdateUserStats(stats,context);
		}
		catch (Exception ex){}
	}
	public static void AddViewedShackLOLs(Context context)
	{
		try {
			final ShackDroidStats stats = GetUserStats(context);
			stats.setViewedShackLOLs(stats.getViewedShackLOLs()+1);
			UpdateUserStats(stats,context);
		}
		catch (Exception ex){}
	}	
	public static void AddViewedChatty(Context context)
	{
		try {
			final ShackDroidStats stats = GetUserStats(context);
			stats.setViewedChatty(stats.getViewedChatty()+1);
			UpdateUserStats(stats,context);
		}
		catch (Exception ex){}
	}		
	public static void AddViewedStats(Context context)
	{
		try {
			final ShackDroidStats stats = GetUserStats(context);
			stats.setViewedStats(stats.getViewedStats()+1);
			UpdateUserStats(stats,context);
		}
		catch (Exception ex){}
	}	
	public void setPostsViewed(int postsViewed) {
		this.postsViewed = postsViewed;
	}
	public int getPostsViewed() {
		return postsViewed;
	}
	public void setPostsMade(int postsMade) {
		this.postsMade = postsMade;
	}
	public int getPostsMade() {
		return postsMade;
	}
	public int getLolsMade() {
		return lolsMade;
	}
	public void setLolsMade(int lolsMade) {
		this.lolsMade = lolsMade;
	}
	public int getInfsMade() {
		return infsMade;
	}
	public void setInfsMade(int infsMade) {
		this.infsMade = infsMade;
	}
	public int getUnfsMade() {
		return unfsMade;
	}
	public void setUnfsMade(int unfsMade) {
		this.unfsMade = unfsMade;
	}
	public int getTagsMade() {
		return tagsMade;
	}
	public void setTagsMade(int tagsMade) {
		this.tagsMade = tagsMade;
	}
	public int getVanitySearches() {
		return vanitySearches;
	}
	public void setVanitySearches(int vanitySearches) {
		this.vanitySearches = vanitySearches;
	}
	public int getShackSearches() {
		return shackSearches;
	}
	public void setShackSearches(int shackSearches) {
		this.shackSearches = shackSearches;
	}
	public int getViewedShackMessages() {
		return viewedShackMessages;
	}
	public void setViewedShackMessages(int viewedShackMessages) {
		this.viewedShackMessages = viewedShackMessages;
	}
	public int getSentShackMessage() {
		return sentShackMessage;
	}
	public void setSentShackMessage(int sentShackMessage) {
		this.sentShackMessage = sentShackMessage;
	}
	public int getCheckedForNewVersion() {
		return checkedForNewVersion;
	}
	public void setCheckedForNewVersion(int checkedForNewVersion) {
		this.checkedForNewVersion = checkedForNewVersion;
	}
	public int getViewedRSSFeed() {
		return viewedRSSFeed;
	}
	public void setViewedRSSFeed(int viewedRSSFeed) {
		this.viewedRSSFeed = viewedRSSFeed;
	}
	public int getViewedShackLOLs() {
		return viewedShackLOLs;
	}
	public void setViewedShackLOLs(int viewedShackLOLs) {
		this.viewedShackLOLs = viewedShackLOLs;
	}

	public void setViewedChatty(int viewedChatty) {
		this.viewedChatty = viewedChatty;
	}

	public int getViewedChatty() {
		return viewedChatty;
	}

	public void setViewedStats(int viewedStats) {
		this.viewedStats = viewedStats;
	}

	public int getViewedStats() {
		return viewedStats;
	}




	
}
