package com.applams.dstl.receiver;

import com.applams.dstl.data.SpeedCopDbAdapter;
import com.applams.dstl.service.SCCheckMovementService;
import com.applams.dstl.util.SpeedCopConstants;
import com.applams.dstl.util.SpeedCopUtil;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
/**
 * @author Hari Raghupathy 
 * Jan 21 2011
 */
public class SCAlarmBrodcastReceiver extends BroadcastReceiver {
	SpeedCopDbAdapter dbAdapter = null;
	@Override 
	public void onReceive(Context context, Intent intent) {
	  try{
		if(SpeedCopUtil.isServiceRunning(context, SpeedCopConstants.CHECK_MOVEMENT_SERVICE_CLASS_NAME)){
			dbAdapter=SpeedCopUtil.initDbAdapter(context, dbAdapter);
			if((System.currentTimeMillis()-dbAdapter.getLastLocTS())>SpeedCopConstants.LAST_LOCATION_CHECK_INTERVAL){
				stopServiceAndAlarm(context,true);
			}
		}else{
			stopServiceAndAlarm(context,false);
		}
	  }finally{
		  SpeedCopUtil.closeDbAdapter(dbAdapter);
	  }
	}
	
	
	public void stopServiceAndAlarm(Context context,boolean stopService){
		if(stopService)
			context.stopService(new Intent(context, SCCheckMovementService.class));
		Intent delayedCallIntent = new Intent(context, SCAlarmBrodcastReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, delayedCallIntent, PendingIntent.FLAG_NO_CREATE);
		if(pi!=null){
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(pi);
			pi.cancel();
			Log.d(SpeedCopConstants.TAG,"NO MOVEMENT .NO LOCATION UPDATE RECEIVED SO CANCEL ALARM AND SERVICE...........");
		}else{
			Log.d(SpeedCopConstants.TAG,"NO MOVEMENT.NO LOCATION UPDATE RECEIVED SO CANCEL SERVIVE...PendingIntent is null!!!........");
		}
	}
}


