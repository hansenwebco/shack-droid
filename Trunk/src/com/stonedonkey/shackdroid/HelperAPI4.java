package com.stonedonkey.shackdroid;

import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.hardware.Camera;
import android.hardware.Camera.Size;
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
	
	
	public static Camera setCameraParams(Camera _cam)
	{
		
		Camera.Parameters parameters= _cam.getParameters();
		
		List<Size> sizes = parameters.getSupportedPreviewSizes();
		if (sizes != null && sizes.size() >=0)
		{
			int w = 0;
			int h = 0;
			// lets pick the largest preview size for this phone
			for (int counter = 0;counter < sizes.size();counter++)
			{
				final Size s = sizes.get(counter);
				if (s.width > w)
				{
					w = s.width;
					h = s.height;
				}
			}

			parameters.setPreviewSize(w, h);
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			
			
			
		}
		return _cam;
	}
	
	
}
