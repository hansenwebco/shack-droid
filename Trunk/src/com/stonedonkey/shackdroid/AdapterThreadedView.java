package com.stonedonkey.shackdroid;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AdapterThreadedView extends BaseAdapter {

	private final List<ShackPost> topicList;
	private final int rowResouceID;
	private static Typeface face;
	private int selectedRow = 0;
	private final String login;
	private final Boolean highlightThread;
	private final int fontSize;
	//private TextView threadPreview, posterName;
	private final LayoutInflater inflate;
	private int postIndent;
	private String postText;
	private final String threadHighlightMode;
	private final int newThreadColor;
	
	public AdapterThreadedView(Context context,int rowResouceID, List<ShackPost> topicList,int selectedRow ){
		this.topicList = topicList;
		this.rowResouceID = rowResouceID;
		this.selectedRow = selectedRow;
		
		//face = Typeface.createFromAsset(context.getAssets(), "fonts/arial.ttf");
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		login = prefs.getString("shackLogin", "");
		
		threadHighlightMode = prefs.getString("threadHighlight", "1");
		newThreadColor =  prefs.getInt("chooseHighlightColor", Color.parseColor("#E5EF49"));
		
		
		
		highlightThread = prefs.getBoolean("highlightUserThreads", true);
		fontSize = Integer.parseInt(prefs.getString("fontSize", "12"));
		inflate = LayoutInflater.from(context);
	}
	
	public void setSelectedRow(int selectedRow) {
		this.selectedRow = selectedRow;
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
		final ShackPost post = topicList.get(position);
		return Long.parseLong(post.getPostID());
	}
	
	
	static class ViewHolder{
		TextView threadPreview, posterName;		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		final ShackPost post = topicList.get(position);
		if (convertView == null){
			convertView = inflate.inflate(rowResouceID,parent,false);
			holder = new ViewHolder();
			//holder.posterName = (TextView)convertView.findViewById(R.id.TextViewPosterNamePreview);
			holder.threadPreview = (TextView)convertView.findViewById(R.id.TextViewThreadPreview);
			
			if (holder.posterName != null){
				holder.posterName.setTypeface(face);
				holder.posterName.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
			}
			
			holder.threadPreview.setTypeface(face);
			holder.threadPreview.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
			
			convertView.setTag(holder);
		}
		else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		// fill in the poster name for hdpi landscape view.
		if (holder.posterName != null){

			holder.posterName.setText(post.getPosterName());
			
			if (post.getPosterName().toString().equalsIgnoreCase(login)){
				holder.posterName.setTextColor(Color.parseColor("#00BFF3"));
			}
			else{
				holder.posterName.setTextColor(Color.parseColor("#ffba00"));
			}
			if (position == selectedRow )
				holder.posterName.setBackgroundColor(Color.parseColor("#274FD3"));
			else
				holder.posterName.setBackgroundColor(Color.TRANSPARENT);			
		}
		
		if (holder.threadPreview != null)
		{
			// chazums
			// Now just moves the text box to the right instead of padding text.
			postIndent = 10* post.getIndent(); // avoid multiple lookups
	
			holder.threadPreview.setPadding(postIndent, 
					holder.threadPreview.getPaddingTop(), 
					holder.threadPreview.getPaddingBottom(), 
					holder.threadPreview.getPaddingRight());
			
			postText = post.getPostPreview();

			
		
			
			// TODO: clean up the thread coloring.. very messy.
			// show this users posts as blue
			 if (highlightThread == true && post.getPosterName().toString().equalsIgnoreCase(login))
				 holder.threadPreview.setTextColor(Color.parseColor("#00BFF3"));
			 else if (post.getPostIndex() > 9)
				 if (threadHighlightMode.equals("2"))
					 holder.threadPreview.setTextColor(Color.parseColor("#FFFFFF"));
				 else
					 holder.threadPreview.setTextColor(Color.parseColor("#777777"));
			 else  { // show new post
				 
				 if (threadHighlightMode.equals("2"))
				 {
					 //newThreadColor

					 int red = Color.red(newThreadColor);// 229;
					 int green = Color.green(newThreadColor);// 239;
					 int blue =  Color.blue(newThreadColor);//73;
					 
					 red = ((255 - red) / 10) * post.getPostIndex() + red;
					 green = ((255 - green) / 10) * post.getPostIndex() + green;
					 blue = ((255 - blue) / 10) * post.getPostIndex() + blue;
					 
					 holder.threadPreview.setTextColor(Color.rgb(red, green, blue));
				 }
				 else
				 {
					 holder.threadPreview.setTextColor(Color.parseColor("#FFFFFF"));
				 }
			 }
			
			if (position == selectedRow )
				holder.threadPreview.setBackgroundColor(Color.parseColor("#274FD3"));
			else
				holder.threadPreview.setBackgroundColor(Color.TRANSPARENT);		
			
			
			holder.threadPreview.setText(postText);
			
		}

		
		return convertView;
	}
}
