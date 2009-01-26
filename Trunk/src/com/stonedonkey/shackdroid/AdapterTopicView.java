package com.stonedonkey.shackdroid;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AdapterTopicView extends BaseAdapter {

	private Context context;
	private List<ShackPost> topicList;
	private int rowResouceID;
	private String shackLogin;
	static Typeface face;
	private int fontSize = 12;
	
	
	public AdapterTopicView(Context context,int rowResouceID, List<ShackPost> topicList, String shackLogin,int fontSize ){
		this.context = context;
		this.topicList = topicList;
		this.rowResouceID = rowResouceID;
		this.shackLogin = shackLogin;
		this.fontSize = fontSize;
				
	    face = Typeface.createFromAsset(context.getAssets(), "fonts/arial.ttf");
	}
	
	@Override
	public int getCount() {
		return topicList.size();
	}
	@Override
	public Object getItem(int position) {
		return topicList.get(position);
	}
	@Override
	public long getItemId(int position) {
		//return position;
		ShackPost post = topicList.get(position);
		return Long.parseLong(post.getPostID());
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ShackPost post = topicList.get(position);
		LayoutInflater inflate = LayoutInflater.from(context);
		
		
		
		View v = inflate.inflate(rowResouceID,parent,false);
	
		
		// bind the TextViews to the items in our datasource
		TextView posterName = (TextView)v.findViewById(R.id.TextViewPosterName);
		posterName.setTypeface(face);
		
		if (posterName != null)
			posterName.setText(post.getPosterName());
		
		if (shackLogin.equalsIgnoreCase(post.getPosterName()))
			posterName.setTextColor(Color.parseColor("#00BFF3"));
		
		TextView postDate = (TextView)v.findViewById(R.id.TextViewDatePosted);
		postDate.setTypeface(face);
		if (postDate != null)
			postDate.setText(post.getPostDate());
		
	
		// Sep 14, 2008 2:31pm CST  
		//DateFormat dfm = new SimpleDateFormat("MMM d, y hh:mmaa z");
		//Date conDate = null;
		//try {  
		//	conDate = dfm.parse(post.getPostDate());
		//} catch (ParseException e) {
		//	// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}   
	
		
		TextView postReplyCount = (TextView)v.findViewById(R.id.TextViewReplyCount);
		postReplyCount.setTypeface(face);
		postReplyCount.setText(post.getReplyCount());	
			
		
		TextView postText = (TextView)v.findViewById(R.id.TextViewPostText);
		postText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
		postText.setTypeface(face);
		if (postText != null)
			postText.setText(Html.fromHtml(ParseShackText(post.getPostPreview(), false)));
				
		ImageView img = (ImageView)v.findViewById(R.id.ImageViewCat);
				
		// TODO: clean this up a little / also replicated in ShackDroidThread ick
		String postCat = post.getPostCategory();
		if (postCat.equals("offtopic"))  {
			img.setImageResource(R.drawable.offtopic);
			//tr.setBackgroundColor(Color.parseColor("#081407"));
		}
		else if (postCat.equals("nws"))
			img.setImageResource(R.drawable.nws);
		else if (postCat.equals("political")) {
			img.setImageResource(R.drawable.political);
			//tr.setBackgroundColor(Color.parseColor("#211D1A"));
		}
		else if (postCat.equals("stupid")) {
			img.setImageResource(R.drawable.stupid);
			//tr.setBackgroundColor(Color.GREEN);
		}
		else if (postCat.equals("informative"))
			img.setImageResource(R.drawable.interesting);		
				
		return v;
	}
	// TODO: Quick fix, duplicated function in ThreadedView, need to move to own class
	private String ParseShackText(String text,boolean addSpoilerMarkers) {

		//Convert the shack spans into HTML fonts since our TextView can convert stuff to HTML
		// not sure if this is the best or most efficent, but works.e
		text = text.replaceAll("<span class=\"jt_red\">(.*?)</span>", "<font color=\"#ff0000\">$1</font>");	
		text = text.replaceAll("<span class=\"jt_green\">(.*?)</span>",	"<font color=\"#8dc63f\">$1</font>");
		text = text.replaceAll("<span class=\"jt_pink\">(.*?)</span>", "<font color=\"#f49ac1\">$1</font>");
		text = text.replaceAll("<span class=\"jt_olive\">(.*?)</span>",	"<font color=\"#808000\">$1</font>");
		text = text.replaceAll("<span class=\"jt_fuchsia\">(.*?)</span>", "<font color=\"#c0ffc0\">$1</font>");
		text = text.replaceAll("<span class=\"jt_yellow\">(.*?)</span>", "<font color=\"#ffde00\">$1</font>");
		text = text.replaceAll("<span class=\"jt_blue\">(.*?)</span>", "<font color=\"#44aedf\">$1</font>");
		text = text.replaceAll("<span class=\"jt_lime\">(.*?)</span>",	"<font color=\"#c0ffc0\">$1</font>");
		text = text.replaceAll("<span class=\"jt_orange\">(.*?)</span>", "<font color=\"#f7941c\">$1</font>");
		text = text.replaceAll("<span class=\"jt_bold\">(.*?)</span>", "<b>$1</b>");
		text = text.replaceAll("<span class=\"jt_italic\">(.*?)</span>", "<i>$1</i>");
		
		// You can only do "highlights" on the actual TextView itself, so we mark up spoilers 
		// !!-text-!! like so, and then handle it on the appling text to the TextView
		if (addSpoilerMarkers == true) {
		text = text.replaceAll("<span class=\"jt_spoiler\"(.*?)>(.*?)</span>",
		"<font color=\"#383838\">!!-$2-!!</font>");
		}
		return text;
	}
}
