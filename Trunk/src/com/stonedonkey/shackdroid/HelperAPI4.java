package com.stonedonkey.shackdroid;

import android.app.Activity;
import android.content.SharedPreferences;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;

public class HelperAPI4 {
	public static ShackGestureListener setGestureEnabledContentView(int resourceId, Activity activity){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		
		// Add a gesture listener if we're 1.6 or greater.
		if (Integer.parseInt(android.os.Build.VERSION.SDK) > 3 && prefs.getBoolean("useGestures", false)){
			GestureOverlayView v = new GestureOverlayView(activity);
			
			// Wrapper class to parse between gesture events and some consts.
			ShackGestureListener l = new ShackGestureListener(activity);
			v.setOrientation(GestureOverlayView.ORIENTATION_VERTICAL);
			v.setEventsInterceptionEnabled(true);
			v.setGestureVisible(prefs.getBoolean("gestureVisible", false)); // set this to true to see what you draw;
			v.addOnGesturePerformedListener((OnGesturePerformedListener) l.get());
			
			//Add the original view as child (this gesture view sits over the top)
			v.addView(LayoutInflater.from(activity).inflate(resourceId, null));
			activity.setContentView(v);
			return l;
		}
		else{
			activity.setContentView(resourceId);
			return null;
		}		
	}
}
