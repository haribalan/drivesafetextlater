package com.applams.dstl.receiver;

import com.applams.dstl.data.SpeedCopDbAdapter;
import com.applams.dstl.service.SCCheckMovementService;
import com.applams.dstl.service.TTSService;
import com.applams.dstl.util.SpeedCopConstants;
import com.applams.dstl.util.SpeedCopUtil;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsMessage;
import android.util.Log;
/**
 * @author Hari Raghupathy 
 * dec 12-10-2011
 */
public class SpeedCopBrodcastReceiver extends BroadcastReceiver {
	SpeedCopDbAdapter dbAdapter = null;
	@Override
	public void onReceive(Context context, Intent intent) {
	  try{
		dbAdapter=SpeedCopUtil.initDbAdapter(context, dbAdapter);
		if(!dbAdapter.isAppEnabled()) //cannot find the SMS_RECEIVED intent string to register broadcast dynamically. so that unregister on app disable. 
			return;                   //so we are using DB to work around, since we register statically in androidmanifest.
		
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
			if(locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)){
				Intent passiveIntent = new Intent(context,PassiveLocationChangedReceiver.class);
				PendingIntent passivePendingIntent = PendingIntent.getBroadcast(context, 0, passiveIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, SpeedCopConstants.PASSIVE_LOCATION_LISTENER_MIN_TIME_INTERVAL,
									SpeedCopConstants.PASSIVE_LOCATION_LISTENER_MIN_DISTANCE, passivePendingIntent);
			}
		}else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
		    NetworkInfo info = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
		    Log.d(SpeedCopConstants.TAG,"Entering the SC BROADCAST RECEIVER:"+info.getDetailedState());
		    if (info.getDetailedState() == DetailedState.DISCONNECTED) {
		    	if(!SpeedCopUtil.isServiceRunning(context, SpeedCopConstants.CHECK_MOVEMENT_SERVICE_CLASS_NAME)){//sometimes we get double disconnect calls. so check.
			    	Log.d(SpeedCopConstants.TAG,"WIFI DISCONNETED ACTION");
					WifiManager wManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
					SpeedCopUtil.saveWifiScanList(wManager.getScanResults(), dbAdapter);
					
					Intent sIntent = new Intent(context, SCCheckMovementService.class);
					sIntent.putExtra(SpeedCopConstants.INTENT_EXTRA_MOV_LOC_DELAY_TYPE, SpeedCopConstants.INTENT_EXTRA_JUST_ONLY_START_SERVICE_FLAG);
					context.startService(sIntent);
					
					Intent delayedCallIntent = new Intent(context, SCCheckMovementService.class);
					delayedCallIntent.putExtra(SpeedCopConstants.INTENT_EXTRA_MOV_LOC_DELAY_TYPE, SpeedCopConstants.INTENT_EXTRA_WIFI_SCAN_DELAY_YES_FLAG);				
					PendingIntent pi = PendingIntent.getService(context, 0, delayedCallIntent, PendingIntent.FLAG_CANCEL_CURRENT);
					AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
					alarmManager.set(AlarmManager.RTC_WAKEUP, (System.currentTimeMillis()+SpeedCopConstants.WIFI_DISCONTED_START_LOC_MOV_INTERVAL), pi);
		    	}
		    }
		    else if (info.getDetailedState() == DetailedState.CONNECTED) {
		    	if(dbAdapter.isAppDriveModeOn() || SpeedCopUtil.isServiceRunning(context, SpeedCopConstants.CHECK_MOVEMENT_SERVICE_CLASS_NAME)){
		    		Log.d(SpeedCopConstants.TAG,"WIFI CONNECTED ACTION..so stop service etc");
					Intent delayedCallIntent = new Intent(context, SCCheckMovementService.class);
					delayedCallIntent.putExtra(SpeedCopConstants.INTENT_EXTRA_MOV_LOC_DELAY_TYPE, SpeedCopConstants.INTENT_EXTRA_WIFI_SCAN_DELAY_YES_FLAG);				
		    		PendingIntent pi = PendingIntent.getService(context, 0, delayedCallIntent, PendingIntent.FLAG_NO_CREATE);
		    		if(pi!=null){
		    			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		    			alarmManager.cancel(pi);
		    			pi.cancel(); //this is case if we post an alarm, but user comes back to wifi before that executes.
		    			Log.d(SpeedCopConstants.TAG,"PI exists so cancel it first..............");
		    		}
		    		context.stopService(new Intent(context, SCCheckMovementService.class));
		    	}
		    }
		}
		else if(Intent.ACTION_DOCK_EVENT.equals(intent.getAction())){
			if(Intent.EXTRA_DOCK_STATE_CAR==intent.getIntExtra(Intent.EXTRA_DOCK_STATE,SpeedCopConstants.DEFAULT_VALUE)){
				Log.d(SpeedCopConstants.TAG,"CAR DOCK ENTER CONNCTED");
				if(!SpeedCopUtil.isServiceRunning(context, SpeedCopConstants.CHECK_MOVEMENT_SERVICE_CLASS_NAME)){
					//dbAdapter.updateCarDockStatus(SpeedCopConstants.CAR_DOCK_STATUS_ON); Right now no use	
					 context.startService(new Intent(context, SCCheckMovementService.class)); //Just do like MANUAL start (from activity)
				}
			} //On undock not handled , maybe user just took mobile off dock & talk or something while driving...let Drive mode stop on own or manually. 
//			else if(Intent.EXTRA_DOCK_STATE_UNDOCKED==intent.getIntExtra(Intent.EXTRA_DOCK_STATE,SpeedCopConstants.DEFAULT_VALUE)){
//				if(dbAdapter.isCarDockOn()){
//					Log.d(SpeedCopConstants.TAG,"CAR DOCK EXIT ");
//					//dbAdapter.updateCarDockStatus(SpeedCopConstants.CAR_DOCK_STATUS_OFF);
//				}
//			}
		}
		else if(SpeedCopConstants.INTENT_SMS_RECEIVED.equals(intent.getAction())){
			Log.d(SpeedCopConstants.TAG,"SMS RECEIVED NOW...");
			if(dbAdapter.isAppDriveModeOn()){
				Log.d(SpeedCopConstants.TAG,"APP running so auto respond...");
		        
				//TODO right now we just autorespond and read the text out loud. todo is: ask if the user wants to listen to that TEXT right now...
				//then give option to auto respond or user can add speech recognition to respond.
				
				Bundle bundle = intent.getExtras();        
		        if (bundle != null)
		        {
		            Object[] pdus = (Object[]) bundle.get(SpeedCopConstants.SMS_MESSAGE_BUNDLE_KEY);
		            SmsMessage[] msgs = new SmsMessage[1];   
	                msgs[0] = SmsMessage.createFromPdu((byte[])pdus[0]); //just get the first one, assume only on messgae at a time
		            SpeedCopUtil.sendSMS(msgs[0].getOriginatingAddress(),dbAdapter.getAutoText());
		        }
		        ///////////////////////////READ out teXT ///////////////////////////////////
		        SmsMessage[] msgs = null;
		        StringBuffer buffer = new StringBuffer();            
		        if (bundle != null)
		        {
		            Object[] pdus = (Object[]) bundle.get("pdus");
		            msgs = new SmsMessage[pdus.length];            
		            for (int i=0; i<msgs.length; i++){
		                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);                
		                buffer.append(".  Text From "); 
		                String from=msgs[i].getOriginatingAddress();
		                String contactName=null;
		                if(from!=null){
			                ContentResolver cr = context.getContentResolver();
			                Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(from));
			                Cursor cursor = cr.query(uri, new String[]{PhoneLookup.DISPLAY_NAME},null,null,null);
			                if (cursor.moveToFirst()) 
			                	contactName =   cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
			                cursor.close();
		                }
		                if(contactName!=null)
		                	buffer.append(contactName);
		                else
		                	buffer.append(from!=null?from:"Unknown");
		                buffer.append(" . "  + msgs[i].getMessageBody().toString() + " . ");
		            }
		        }
				Intent sIntent = new Intent(context, TTSService.class);
				sIntent.putExtra(SpeedCopConstants.INTENT_EXTRA_TEXT_TO_READ_OUT, buffer.toString());	
				context.startService(sIntent);		        
		        /////////////////////////////////////////////////////////////
		        Log.d(SpeedCopConstants.TAG,"DONE SMS auto respond......");
			}
			else{
				Log.d(SpeedCopConstants.TAG,"SMS RECEIVED APP NOT RUNNING...");
				ConnectivityManager cManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo nInfo = cManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if(!nInfo.isConnected() && !SpeedCopUtil.isServiceRunning(context, SpeedCopConstants.CHECK_MOVEMENT_SERVICE_CLASS_NAME)){
					Log.d(SpeedCopConstants.TAG,"SMS RECEIVED..OK WIFI AND SERVICE NOT RUNNING...");
					if((System.currentTimeMillis()-dbAdapter.getLastSmsTS())>SpeedCopConstants.SMS_RECEIVED_ELAPSED_MILLI_SECONDS){
						Log.d(SpeedCopConstants.TAG,"SMS RECEIVED..OK LAST SMS  MORE THAN 8 MINUTES...");
						dbAdapter.updateSmsReceivedTimeStamp(System.currentTimeMillis()); //Save it only if we are processing it.
						WifiManager wManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
						Intent sIntent = new Intent(context, SCCheckMovementService.class);
						sIntent.putExtra(SpeedCopConstants.INTENT_EXTRA_MOV_LOC_DELAY_TYPE, SpeedCopConstants.INTENT_EXTRA_SMS_DELAY_YES_FLAG);
						if(wManager.isWifiEnabled()){
							if(SpeedCopUtil.isWifiAreaChangedorEmptyAndSave(wManager.getScanResults(),dbAdapter)){ 
								context.startService(sIntent);
							}
						}else{
							context.startService(sIntent);
						}
					}
					Log.d(SpeedCopConstants.TAG,"SMS RECEIVED EXITING...");
				}
			}
		}
//		else if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
//			TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
//			SCCellChangeListener.objectCallBack = new SCCellChangeListener();
//			telephonyManager.listen(SCCellChangeListener.objectCallBack, PhoneStateListener.LISTEN_CELL_LOCATION);
//		}
	  }finally{
		  SpeedCopUtil.closeDbAdapter(dbAdapter);
	  }
	}
}
