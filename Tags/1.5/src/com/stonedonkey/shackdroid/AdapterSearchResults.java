package com.stonedonkey.shackdroid;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Typeface;
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
	
	public AdapterSearchResults(Context context, ArrayList<ShackSearch> rssItems, int rowResouceID)
	{
		this.context = context;
		this.searchItems = rssItems;
		this.rowResouceID = rowResouceID;
		
		face = Typeface.createFromAsset(context.getAssets(), "fonts/arial.ttf");
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
		result = result.replaceAll("(\r\n|\r|\n|\n\r)", "");
		rt.setText(result);
		rt.setTypeface(face);
		
		
		return v;
	}

}
