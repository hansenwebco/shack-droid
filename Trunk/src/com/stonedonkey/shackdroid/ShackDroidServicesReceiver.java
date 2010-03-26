package com.stonedonkey.shackdroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ShackDroidServicesReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent arg1) {
		Log.d(this.toString(), "SM Check broadcast received.");
		
		if (arg1.getAction() == Intent.ACTION_BOOT_COMPLETED){
			if (Helper.CheckAllowSMService(context)){
				Helper.setSMAlarm(context);
			}
		}
		else{
			if (Helper.CheckAllowSMService(context)){
				// Grab a wake-lock now to stop the phone from sleeping between ending this function and the service
				// actually starting.  Hold it for 3 mins max.
				ActivityShackDroidServices.getLock(context).acquire(180000); 
				context.startService(new Intent(context, ActivityShackDroidServices.class));
			}
			else
				context.stopService(new Intent(context, ActivityShackDroidServices.class));
		}
	}
}