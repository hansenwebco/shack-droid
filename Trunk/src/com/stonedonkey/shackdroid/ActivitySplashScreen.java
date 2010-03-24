package com.stonedonkey.shackdroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivitySplashScreen extends Activity {

		private final int SPLASH_LENGTH = 5000;
		Boolean skip = false;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		Helper.SetWindowState(getWindow(),this);
		
		setContentView(R.layout.splash);
		ImageView iv = (ImageView)findViewById(R.id.ImageViewSplash);
		iv.setImageResource(R.drawable.shackdroid_splash);
		
		TextView auth= (TextView)findViewById(R.id.TextViewAuthor);
		auth.setTextSize(12);
		auth.setText("written by: stonedonkey ~ chazums");
		
		TextView version= (TextView)findViewById(R.id.TextViewVersion);
		version.setTextSize(12);
		version.setText("Version " + getResources().getString(R.string.version_id));
		
		TextView web = (TextView)findViewById(R.id.TextViewWebSite);
		web.setTextSize(10);
		web.setText("http://www.stonedonkey.com");
		
		// start the service that checks for new shackmessages
		startService(new Intent(ActivitySplashScreen.this, ActivityShackDroidServices.class));
 
		// ad listener to skip screen
		iv.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
    			intent.setClass(getBaseContext(), ActivityMainMenu.class);
    			startActivity(intent);
    			
    			// TODO: Find a way to kill the Runnable, this is hacky
    			skip = true;
    			
    			ActivitySplashScreen.this.finish();
			}
		});
		

		new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
    		
            	if (skip==false) {
            	Intent intent = new Intent();
    			intent.setClass(getBaseContext(), ActivityMainMenu.class);
    			startActivity(intent);
    			ActivitySplashScreen.this.finish();
            	}

            }
       }, SPLASH_LENGTH); 
		
	}

}
