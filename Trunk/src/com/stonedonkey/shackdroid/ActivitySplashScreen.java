package com.stonedonkey.shackdroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivitySplashScreen extends Activity {

		private final int SPLASH_LENGTH = 1000;
		Boolean skip = false;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		Helper.SetWindowState(getWindow(),this,1);
		
		setContentView(R.layout.splash);
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final String feedURL = prefs.getString("shackFeedURL",getString(R.string.default_api));
		if (feedURL.contains("shackchatty"))
		{
			final SharedPreferences settings= PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("shackFeedURL","http://shackapi.stonedonkey.com");
			editor.commit(); 
			
		}
		
		ImageView iv = (ImageView)findViewById(R.id.ImageViewSplashText);
		iv.setImageResource(R.drawable.shackdroid_splash2);
		
		iv = (ImageView)findViewById(R.id.ImageViewSplash);
		iv.setImageResource(R.drawable.shackdroid_splash1);
		
//		Animation anim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.spin);
//		iv.setAnimation(anim);
//		anim = null;
		
		
		
		TextView auth= (TextView)findViewById(R.id.TextViewAuthor);
		auth.setTextSize(12);
		auth.setText("written by:\n stonedonkey ~ chazums ~ DrWaffles");
		
		TextView version= (TextView)findViewById(R.id.TextViewVersion);
		version.setTextSize(12);
		version.setText("Version " + getResources().getString(R.string.version_id));
		
		TextView web = (TextView)findViewById(R.id.TextViewWebSite);
		web.setTextSize(10);
		web.setText("http://www.stonedonkey.com");
		
		// try and start sm service
		StartSMService();
		
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
	protected void StartSMService()
	{
		// start the service that checks for new shack messages
		if (Helper.CheckAllowSMService(this)) {
			Log.d("ShackDroid", "Starting SM Alarm");
			Helper.setSMAlarm(getApplicationContext());
			//startService(new Intent(ActivitySplashScreen.this, ActivityShackDroidServices.class));
		}
		else {
			Log.d("ShackDroid", "Stopping SM Alarm");
			Helper.clearSMAlarm(getApplicationContext());
			//stopService(new Intent(ActivitySplashScreen.this, ActivityShackDroidServices.class));
		}
	}
}
