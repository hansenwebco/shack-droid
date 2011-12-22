package com.stonedonkey.shackdroid;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AdapterRSSView extends BaseAdapter {
	private Context context;
	private ArrayList<ShackRSS> rssItems;
	private int rowResouceID;
	static Typeface face;
	
	
	public AdapterRSSView(Context context, ArrayList<ShackRSS> rssItems, int rowResouceID)
	{
		this.context = context;
		this.rssItems = rssItems;
		this.rowResouceID = rowResouceID;
		
		face = Typeface.createFromAsset(context.getAssets(), "fonts/arial.ttf");
	}
	
	@Override
	public int getCount() {
		if (rssItems == null)
			return 0;
		else
			return rssItems.size();
	}

	@Override
	public Object getItem(int position) {
		return rssItems.get(position);
	}

	@Override
	public long getItemId(int position) {

		ShackRSS rss = rssItems.get(position);
		String id = rss.getID();
		
		// TODO: lets think about this more later
		//String[] story = link.split("/");
		long storyID = Long.parseLong(id);
		
		return storyID;
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		int fontSize = Integer.parseInt(prefs.getString("fontSize", "12"));
			
		ShackRSS rss = rssItems.get(position);
		LayoutInflater inflate = LayoutInflater.from(context);
		
		View v = inflate.inflate(rowResouceID,parent,false);
		
		TextView title = (TextView)v.findViewById(R.id.TextViewRssTitle);
		title.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
		title.setText(rss.getTitle());
		title.setTypeface(face);
		
		TextView date = (TextView)v.findViewById(R.id.TextViewRssDatePosted);
		
		//String formattedDate = Helper.FormShackRSSDate(rss.getDatePosted());
		String formattedDate = rss.getDatePosted();
		date.setText(formattedDate);
		date.setTypeface(face);		
		
		String descText = rss.getDescription();
		
		
		descText= descText.replaceAll("</?\\w++[^>]*+>", ""); // remove HTML tags
		descText = descText.replaceAll("(\r\n|\r|\n|\n\r)", "");
		
		if (descText.length() > 150)
			descText = descText.substring(0, 150);
			
		TextView desc = (TextView)v.findViewById(R.id.TextViewRssDescription);
		desc.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
		desc.setText(descText);
		desc.setTypeface(face);
		
		return v;
		
	}

}

