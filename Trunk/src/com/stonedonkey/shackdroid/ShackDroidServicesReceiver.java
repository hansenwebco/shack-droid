package com.stonedonkey.shackdroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ShackDroidServicesReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent arg1) {
		if (Helper.CheckAllowSMService(context))
			context.startService(new Intent(context, ActivityShackDroidServices.class));
		else
			context.stopService(new Intent(context, ActivityShackDroidServices.class));
	}
}