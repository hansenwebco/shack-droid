package com.stonedonkey.shackdroid;

import java.util.ArrayList;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;

public class ShackPopup {
	
	//PopupWindow w;
	public static final int REPLY = 0;
	public static final int WATCH = 1;
	public static final int REFRESH = 2;
	public static final int MESSAGE = 3;
	
	
	private ArrayList<ShackPopupEvent> mListeners = new ArrayList<ShackPopupEvent>();
	public ShackPopup(){

	}
	
	public PopupWindow Init(Activity ctx, PopupWindow w){
		View v = ctx.getLayoutInflater().inflate(R.layout.popup, null, false);
		w = new PopupWindow(v, 300, 75);
		w.setBackgroundDrawable(ctx.getResources().getDrawable(R.drawable.popup));

		setupButton(v.findViewById(R.id.ivPopupReply), REPLY);
		setupButton(v.findViewById(R.id.ivPopupMessage), MESSAGE);
		setupButton(v.findViewById(R.id.ivPopupWatch), WATCH);
		setupButton(v.findViewById(R.id.ivPopupRefresh), REFRESH);
		
		return w;
	}
	
	public void addListener(ShackPopupEvent l){
		mListeners.add(l);
	}
	public void removeListener(ShackPopupEvent l){
		mListeners.remove(l);
	}
	
	private void setupButton(View v, final int event){
		v.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				raiseEvent(event);
			}});		
	}
	
	private void raiseEvent(int event){
		for(ShackPopupEvent e : mListeners){
			e.PopupEventRaised(event);
		}
	}
	public interface ShackPopupEvent{
		public void PopupEventRaised(int eventType);
	}
}
