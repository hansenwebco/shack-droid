package com.stonedonkey.shackdroid;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

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
		timer.scheduleAtFixedRate( new TimerTask() {
			public void run() {
				//code goes here
				int a = 1;
				a++;
			}
		}, 0, 10000);
	;}
	
	private void stopService() {
		 if (timer != null)
			 timer.cancel();
	}	

}
