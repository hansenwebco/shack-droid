package com.stonedonkey.shackdroid;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ActivityShackDroidServices extends Service  {

	private Timer timer = new Timer(); 
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		startService();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopService();
		
	}


	private void startService()
	{
		final Context context = getApplicationContext();;
		
		timer.scheduleAtFixedRate( new TimerTask() {
			public void run() {
				Log.d(this.toString(), "Checking Shack Messages");
				try {
					 Helper.CheckForNewShackMessages(context);
					} 
				catch (Exception e) {
					Log.d(this.toString(), e.getMessage());
			}
				
				
			}
		}, 0, 300000);
	;}
	
	private void stopService() {
		 if (timer != null)
			 timer.cancel();
	 
	}	
}
