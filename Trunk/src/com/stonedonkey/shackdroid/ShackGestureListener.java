package com.stonedonkey.shackdroid;

import java.util.ArrayList;

import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;

public class ShackGestureListener implements OnGesturePerformedListener {

	public static final int FORWARD = 0;
	public static final int BACKWARD = 1;
	public static final int REFRESH = 2;
	
	GestureLibrary mLibrary;
	ArrayList<ShackGestureEvent> mListeners = new ArrayList<ShackGestureEvent>();
	
	public ShackGestureListener(Context ctx){
		mLibrary = GestureLibraries.fromRawResource(ctx, R.raw.gestures);
		mLibrary.load();		
	}
	
	public void addListener(ShackGestureEvent l){
		mListeners.add(l);
	}
	public void removeListener(ShackGestureEvent l){
		mListeners.remove(l);
	}
	
	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		// TODO Auto-generated method stub
	    ArrayList<Prediction> predictions = mLibrary.recognize(gesture);

	    // We want at least one prediction
	    if (predictions.size() > 0) {
	        Prediction prediction = predictions.get(0);
	        // We want at least some confidence in the result
	        if (prediction.score > 1.0) {
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

	public interface ShackGestureEvent{
		void eventRaised(int eventType);
	}
}
