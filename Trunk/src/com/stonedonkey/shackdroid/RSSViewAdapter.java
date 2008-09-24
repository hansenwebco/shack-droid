package com.stonedonkey.shackdroid;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RSSViewAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<ShackRSS> rssItems;
	private int rowResouceID;
	static Typeface face;
	
	public RSSViewAdapter(Context context, ArrayList<ShackRSS> rssItems, int rowResouceID)
	{
		this.context = context;
		this.rssItems = rssItems;
		this.rowResouceID = rowResouceID;
		
		face = Typeface.createFromAsset(context.getAssets(), "fonts/arial.ttf");
	}
	
	@Override
	public int getCount() {
		return rssItems.size();
	}

	@Override
	public Object getItem(int position) {
		return rssItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		//TODO: return the article ID from the link
		//ShackRSS rss = rssItems.get(position);
		//return rss.getLink());
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ShackRSS rss = rssItems.get(position);
		LayoutInflater inflate = LayoutInflater.from(context);
		
		View v = inflate.inflate(rowResouceID,parent,false);
		
		TextView title = (TextView)v.findViewById(R.id.TextViewRssTitle);
		title.setText(rss.getTitle());
		title.setTypeface(face);
		
		TextView date = (TextView)v.findViewById(R.id.TextViewRssDatePosted);
		date.setText(rss.getDatePosted());
		date.setTypeface(face);		
		
		TextView desc = (TextView)v.findViewById(R.id.TextViewRssDescription);
		desc.setText(Html.fromHtml(rss.getDescription().substring(0, 100)));
		desc.setTypeface(face);
		
		return v;
		
	}

}

