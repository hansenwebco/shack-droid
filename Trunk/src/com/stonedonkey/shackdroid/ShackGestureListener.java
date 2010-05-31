package com.stonedonkey.shackdroid;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.os.Vibrator;
import android.preference.PreferenceManager;

public class ShackGestureListener {
	public static final int FORWARD = 0;
	public static final int BACKWARD = 1;
	public static final int REFRESH = 2;
	private ArrayList<ShackGestureEvent> mListeners = new ArrayList<ShackGestureEvent>();
	
	Object o;
	public ShackGestureListener(Context ctx){
		
		// Add a gesture listener if we're 1.6 or greater.
		if (Integer.parseInt(android.os.Build.VERSION.SDK) > 3){
			o = new ShackGestureListenerInternal(ctx);
		}
	}
	
	public Object get(){
		return o;
	}
	public void addListener(ShackGestureEvent l){
		mListeners.add(l);
	}
	public void removeListener(ShackGestureEvent l){
		mListeners.remove(l);
	}
	
	public interface ShackGestureEvent{
		void eventRaised(int eventType);
	}
	
	class ShackGestureListenerInternal	implements OnGesturePerformedListener {
		
		private GestureLibrary mLibrary;
		
		private Vibrator vibe;
		
		public ShackGestureListenerInternal(Context ctx){
			mLibrary = GestureLibraries.fromRawResource(ctx, R.raw.gestures);
			mLibrary.load();
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
			
			if (prefs.getBoolean("useGestureVibrate", false)){
				vibe = (Vibrator)ctx.getSystemService(Context.VIBRATOR_SERVICE);
			}
		}
		

		
		@Override
		public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
			// TODO Auto-generated method stub
		    ArrayList<Prediction> predictions = mLibrary.recognize(gesture);

		    // We want at least one prediction
		    if (predictions.size() > 0) {
		        Prediction prediction = predictions.get(0);
		        // We want at least some confidence in the result
		        if (prediction.score > 1.9) {
		        	if (vibe != null){
		        		vibe.vibrate(50);
		        	}
		        	
		        	if (prediction.name.equals("forward")){
		        		for(ShackGestureEvent s : mListeners){
		        			s.eventRaised(FORWARD);
		        		}
		        	}
		        	else if (prediction.name.equals("backwards")){
		        		for(ShackGestureEvent s : mListeners){
		        			s.eventRaised(BACKWARD);
		        		}
		        	}
		        	if (prediction.name.equals("reload")){
		        		for(ShackGestureEvent s : mListeners){
		        			s.eventRaised(REFRESH);
		        		}
		        	}	        	
		        }
		    }		
		}


	}	
}


