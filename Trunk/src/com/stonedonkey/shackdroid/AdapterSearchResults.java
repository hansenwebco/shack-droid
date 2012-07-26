package com.stonedonkey.shackdroid;

import java.util.ArrayList;
import java.util.List;

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

public class AdapterSearchResults  extends BaseAdapter {

	private Context context;
	private ArrayList<ShackSearch> searchItems;
	private int rowResouceID;
	static Typeface face;
	static int fontSize = 12;
	
	public AdapterSearchResults(Context context, ArrayList<ShackSearch> rssItems, int rowResouceID)
	{
		this.context = context;
		this.searchItems = rssItems;
		this.rowResouceID = rowResouceID;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		fontSize = Integer.parseInt(prefs.getString("fontSize", "12"));
				
		face = Typeface.createFromAsset(context.getAssets(), "fonts/arial.ttf");
	}
	
	public void SetPosts(List<ShackSearch> searchItems)
	{
		searchItems = searchItems;
	}
	@Override
	public int getCount() {
		return searchItems.size();
	}

	@Override
	public Object getItem(int position) {
		return searchItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		ShackSearch searchItem = searchItems.get(position);
		return  Long.parseLong(searchItem.getId());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ShackSearch searchItem = searchItems.get(position);
		LayoutInflater inflate = LayoutInflater.from(context);
			
		View v = inflate.inflate(rowResouceID,parent,false);

		TextView auth = (TextView)v.findViewById(R.id.TextViewSearchAuthor);
		auth.setText(searchItem.getAuthor());
		auth.setTypeface(face);
		
		TextView dp =  (TextView)v.findViewById(R.id.TextViewSearchDatePosted);
		dp.setText(searchItem.getDatePosted());
		dp.setTypeface(face);
		
		TextView rt = (TextView)v.findViewById(R.id.TextViewSearchBody);
		String result = searchItem.getResultText();
		rt.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
		result = result.replaceAll("(\r\n|\r|\n|\n\r)", "");
		rt.setText(result);
		rt.setTypeface(face);
		
		
		return v;
	}

}
