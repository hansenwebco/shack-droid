package com.stonedonkey.shackdroid;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AdapterMainMenu extends BaseAdapter {

	private Context context;
	private ArrayList<ShackMenuItem> menuList;
	private int rowResouceID;
	static Typeface face;
	
	public AdapterMainMenu(Context context,int rowResouceID, ArrayList<ShackMenuItem> noteList)
	{
		this.context = context;
		this.menuList = noteList;
		this.rowResouceID = rowResouceID;
		
		face = Typeface.createFromAsset(context.getAssets(), "fonts/arial.ttf");
		
	}
	@Override
	public int getCount() {
		return menuList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return menuList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflate = LayoutInflater.from(context);
		

		View v = inflate.inflate(rowResouceID,parent,false);
		
		// bind the TextViews to the items in our datasource
		TextView menuTitle = (TextView)v.findViewById(R.id.MainMenuTitle);
		TextView menuSubTitle = (TextView)v.findViewById(R.id.MainMenuSubTitle);
		ImageView menuIcon = (ImageView)v.findViewById(R.id.MainMenuIcon);
		
		menuTitle.setText(menuList.get(position).getMenuTitle().toString());
		menuSubTitle.setText(menuList.get(position).getMenuSubTitle().toString());
		menuIcon.setImageResource(menuList.get(position).getMenuIcon());
		
		menuTitle.setTypeface(face);
		menuSubTitle.setTypeface(face);
		
	
		
		return v;
	}

}
