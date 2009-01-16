package com.stonedonkey.shackdroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

public class ActivitySplashScreen extends Activity {

		private final int SPLASH_LENGTH = 1000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.splash);
		ImageView iv = (ImageView)findViewById(R.id.ImageViewSplash);
		iv.setImageResource(R.drawable.shack_droid_about);

		new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
    			
            	Intent intent = new Intent();
    			intent.setClass(getBaseContext(), ActivityTopicView.class);
    			startActivity(intent);
    			ActivitySplashScreen.this.finish();

            }
       }, SPLASH_LENGTH); 
		
	}

}
