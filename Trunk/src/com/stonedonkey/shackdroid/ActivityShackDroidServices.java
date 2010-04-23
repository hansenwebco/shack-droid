package com.stonedonkey.shackdroid;

import java.util.Timer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class ActivityShackDroidServices extends Service  {

	private Timer timer = new Timer(); 
	
	private static PowerManager.WakeLock _wakeLock = null;

	synchronized public static PowerManager.WakeLock getLock(Context context) {
		if (_wakeLock == null) {
			Log.d("ShackDroid", "Creating wakeLock");
			PowerManager mgr=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
			
			_wakeLock=mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Shackdroid ShackMessge lock aquired");
			_wakeLock.setReferenceCounted(false);
		}
		
		return(_wakeLock);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override 
	public void onStart(Intent intent, int startId){
		super.onStart(intent, startId);
		startService();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		stopService();
		
	}


	private void startService()
	{
		Context context = getApplicationContext();
		if (CheckConnectivity()){
			Log.d("ShackDroid", "Checking Shack Messages");
			try {
				 Helper.CheckForNewShackMessages(context);
				} 
			catch (Exception e) {
				Log.d("ShackDroid", e.getMessage());	
			}
		}
		else{
			Log.d("ShackDroid", "Network too old and busted for SM check.");
		}
		
		
		getLock(context).release();
		
		
		/*
		final Context context = getApplicationContext();;
		
		timer.scheduleAtFixedRate( new TimerTask() {
			public void run() {
				Log.d("ShackDroid", "Checking Shack Messages");
				try {
					 Helper.CheckForNewShackMessages(context);
					} 
				catch (Exception e) {
					Log.d("ShackDroid", e.getMessage());
			}
				
				
			}
		}, 0, 300000);*/
	}
	
	
	private boolean CheckConnectivity(){
		TelephonyManager t = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		ConnectivityManager c = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo i = c.getActiveNetworkInfo();
		
		// getActiveNetworkInfo() can return null so check before nullPointerExecptions 
		if (i == null ) 
			return false;
		
		// If the user has selected "don't do shit in the background" in the phone settings
		if (!c.getBackgroundDataSetting()){
			return false;
		}
		
		// If we're on wi-fi awesome.
		if ( i.getType() == ConnectivityManager.TYPE_WIFI &&
				i.isConnected()){
			return true;
		}

		// If we're on a mobile network better than GPRS -> EDGE, UMTS(3G)
		// Connected to it and not going to incur roaming charges.
		if (i.getType() == ConnectivityManager.TYPE_MOBILE &&
				i.getSubtype() > TelephonyManager.NETWORK_TYPE_GPRS &&
				i.isConnected() &&
				!t.isNetworkRoaming()){
			return true;
		}
		
		
		//TODO: We can be fancy here and get the phone to try and connect to wi-fi if it's not connected.
		return false;
	}
	
	
	private void stopService() {
		 if (timer != null)
			 timer.cancel();
	 
	}	
}
