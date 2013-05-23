package com.applams.dstl.service;

import java.util.ArrayList;
import java.util.List;

import com.applams.dstl.R;
import com.applams.dstl.data.SpeedCopDbAdapter;
import com.applams.dstl.receiver.SCAlarmBrodcastReceiver;
import com.applams.dstl.ui.SpeedCopActivity;
import com.applams.dstl.util.SpeedCopConstants;
import com.applams.dstl.util.SpeedCopUtil;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
/**
 * @author Hari Raghupathy  Jan 2 2012
 */
public class SCCheckMovementService extends Service {
	LocationManager locationManager ;
	SpeedCopDbAdapter dbAdapter = null;
	boolean isNotified=false;
	String providerUsed;

	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		int delayType;
		if(intent==null) //this is case when android kills and restart service. rather checking lots of other things..we treat it as manual start
			delayType	= SpeedCopConstants.DEFAULT_VALUE;
		else
			delayType = intent.getIntExtra(SpeedCopConstants.INTENT_EXTRA_MOV_LOC_DELAY_TYPE,SpeedCopConstants.DEFAULT_VALUE);
		
		Log.d(SpeedCopConstants.TAG,"LOCATION SERVICE IS GETTING CALLED.......delayType:"+delayType);
		
		if(delayType==SpeedCopConstants.INTENT_EXTRA_WIFI_SCAN_DELAY_YES_FLAG){
			WifiManager wManager = (WifiManager)SCCheckMovementService.this.getSystemService(Context.WIFI_SERVICE);
			dbAdapter=SpeedCopUtil.initDbAdapter(SCCheckMovementService.this,dbAdapter);
			if(wManager.isWifiEnabled()){
			  if(SpeedCopUtil.isWifiAreaChangedorEmptyAndSave(wManager.getScanResults(),dbAdapter))
				movementCheck();
			  else{
	    		Log.d(SpeedCopConstants.TAG,"NO WIFI CHANGE NOW...AFTER X MINUTES..SO STOP SERVICE");
	    		stopSelf();
			   }
		    }else{
				movementCheck();
		    }
		}else if(delayType==SpeedCopConstants.INTENT_EXTRA_SMS_DELAY_YES_FLAG){
			dbAdapter=SpeedCopUtil.initDbAdapter(SCCheckMovementService.this,dbAdapter);
			movementCheck();  
		}else if(delayType==SpeedCopConstants.INTENT_EXTRA_JUST_ONLY_START_SERVICE_FLAG){
			Log.d(SpeedCopConstants.TAG,"JUST START ONLY SERVICE CALLED");
			//here we do nothing, but just start service. This is useful is we want to start service and post action later. So other
			//calls know service is already started. like say calling from wifi and then receive sms. //Note got to do this work around as handlers 
			//does not get called when phone is sleeping. so got to use alarm.
		}
		else if(delayType==SpeedCopConstants.DEFAULT_VALUE){ //this is if user started it manually using the start button
			createNotification(SCCheckMovementService.this);
			dbAdapter=SpeedCopUtil.initDbAdapter(SCCheckMovementService.this,dbAdapter);
			dbAdapter.updateAppDriveModeStatus(SpeedCopConstants.DRIVE_MODE_STATUS_ON);
			isNotified=true;
			movementCheck();
		}
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		isNotified=false;
		Log.d(SpeedCopConstants.TAG,"LOCATION SERVICE IS GETTING DESTROYED.......");
		if(locationManager!=null)//what if service is force stopped or connected back to wifi...before even loc manager is created (say in case of wifi delay stat loc man)
			locationManager.removeUpdates(locationListener);
		dbAdapter=SpeedCopUtil.initDbAdapter(SCCheckMovementService.this,dbAdapter);
		dbAdapter.updateAppDriveModeStatus(SpeedCopConstants.DRIVE_MODE_STATUS_OFF);
		NotificationManager nManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
		nManager.cancel(SpeedCopConstants.NOTIFICATION_UNNIQUE_ID);
		SpeedCopUtil.closeDbAdapter(dbAdapter);
		Log.d(SpeedCopConstants.TAG,"LOCATION SERVICE IS DESTROYED all done.......");
	}

	  private void movementCheck(){
		 Log.d(SpeedCopConstants.TAG,"LOCATION SERVICE ...REGISTER LOCATION SERVICE.......");
		 locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		 WifiManager wManager = (WifiManager)SCCheckMovementService.this.getSystemService(Context.WIFI_SERVICE);
		 if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			 locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, SpeedCopConstants.GPS_LOCATION_LISTEN_MIN_TIME, 
                     SpeedCopConstants.GPS_LOCATION_LISTEN_MIN_DISTANCE, locationListener);
			 providerUsed = LocationManager.GPS_PROVIDER;
		 }else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){	 
			 locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, SpeedCopConstants.NW_LOCATION_LISTEN_MIN_TIME, 
			                                                       SpeedCopConstants.NW_LOCATION_LISTEN_MIN_DISTANCE, locationListener);
			 providerUsed = LocationManager.NETWORK_PROVIDER;
		 }else {
			 Log.d(SpeedCopConstants.TAG,"****NO PROVIDER AVAILABLE to calculate movement...NO NETWORK OR GPS PROVIDER..APP CLOSE.");
			 stopSelf();
			 return;
		 }
		Intent delayedCallIntent = new Intent(this, SCAlarmBrodcastReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, delayedCallIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, (System.currentTimeMillis()+SpeedCopConstants.FIRST_TRIGGER_LAST_LOCATION_CHECK_ALARM_TIME),
															SpeedCopConstants.LAST_LOCATION_CHECK_INTERVAL, pi);
	  }  
	
	// Define a listener that responds to location updates
	LocationListener locationListener = new LocationListener() {
		int loopCnt  = 1;
		long time1 ;
		double locLat1,locLon1;
		long eTStart=0; //elapsed  start time
		List<Float> accuracyList = new ArrayList<Float>(5);
		boolean isHighWay=false;
	    public void onLocationChanged(Location location) {
	    	Log.d(SpeedCopConstants.TAG,"LOC_CALL1 LISTENER BY ANDROID ...");
	    	accuracyList.add(location.getAccuracy());
	    	if(eTStart==0) //see loopcnt-- //This is before Jelly Bean...since the minTime parameter (on requestlocation method) was only a hint, and some location provider implementations ignored it
	    		eTStart = System.currentTimeMillis();	
	        dbAdapter.updateLastLocReceivedTimeStamp(System.currentTimeMillis()); //Incase there is not location update for a long time...since no movement. used in SCAlarmBrodcastReceiver
	    	if(loopCnt%2==0){
	    		Log.d(SpeedCopConstants.TAG,"CHANGE IN LOCATION ROUND 2,,4..6..");
	    		if((System.currentTimeMillis()-eTStart)>SpeedCopConstants.ELAPSED_TIME_TO_FIND_MOVEMENT){//if ok, else ignore location.
	    			float avgAccu = findAverageAccuracy(accuracyList);
	    			eTStart=0; //reset
		    		float results[] = new float[3];
		    		Location.distanceBetween(locLat1, locLon1, location.getLatitude(), location.getLongitude() ,results);
		    		if(results[0]>0){  //if distance traveled results[0] is greater than 0 meters.
		    			Log.d(SpeedCopConstants.TAG,"CHANGE IN LOCATION IN METERS: "+results[0]);
		    			double time = (((System.currentTimeMillis()-time1)/1000)/60); //time in minutes
		    			Log.d(SpeedCopConstants.TAG,"TIME BETWEEN CALLS.....--->IN MINUTES-->"+time);
		    			double velocity = (results[0]/time);  // Meters/Minute
		    			Log.d(SpeedCopConstants.TAG,"VELOCITY --- velocity: "+velocity);
		    			boolean nowHighWay=false;
		    			if(velocity<SpeedCopConstants.MINUMUM_DRIVER_VELOCITY){
		    				Log.d(SpeedCopConstants.TAG,"NO LOCATION CHANGE --- velocity is only: "+velocity);
		    				stopSelf();
		    			}else if(velocity>SpeedCopConstants.HIGHWAY_DRIVER_VELOCITY){
		    				Log.d(SpeedCopConstants.TAG,"HIGHWAY_DRIVER_VELOCITY--- velocity in meters/minute: "+velocity);
		    				nowHighWay = true;
		    			}
		    			if(avgAccu>SpeedCopConstants.MAX_ACCURACY_DEVIATION){//very less accuracy..
		    				Log.d(SpeedCopConstants.TAG,"MAX_ACCURACY_DEVIATION..provider Used:"+location.getProvider());
		    				if(LocationManager.NETWORK_PROVIDER.equals(providerUsed) 
		    													&& locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			    				 locationManager.removeUpdates(locationListener);
			    				 if(nowHighWay)
			   	    			    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, SpeedCopConstants.HIGHWAY_LOC_LISTEN_MIN_TIME, 
			   	                         SpeedCopConstants.HIGHWAY_LOC_LISTEN_MIN_DISTANCE, locationListener);
			    				 else
			    					 locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, SpeedCopConstants.GPS_LOCATION_LISTEN_MIN_TIME, 
				   	                         SpeedCopConstants.GPS_LOCATION_LISTEN_MIN_DISTANCE, locationListener);
			    				 
			   	    			 providerUsed = LocationManager.GPS_PROVIDER;    			
		    				}else if(LocationManager.GPS_PROVIDER.equals(providerUsed)//May be: in city/downtown and no gps ..like chicago we had..this can occur 
												&& locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			    				 locationManager.removeUpdates(locationListener);
			    				 if(nowHighWay)
			    					 locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, SpeedCopConstants.HIGHWAY_LOC_LISTEN_MIN_TIME, 
			                             SpeedCopConstants.HIGHWAY_LOC_LISTEN_MIN_DISTANCE, locationListener);
			    				 else
			    					 locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, SpeedCopConstants.NW_LOCATION_LISTEN_MIN_TIME, 
				                             SpeedCopConstants.NW_LOCATION_LISTEN_MIN_DISTANCE, locationListener);
			    					 
			   	    			 providerUsed = LocationManager.NETWORK_PROVIDER;    			
		    				}
		    				Log.d(SpeedCopConstants.TAG,"NOW..provider Used:"+providerUsed);
		    				isHighWay=nowHighWay;
		    			}else if(nowHighWay!=isHighWay){
		    				 Log.d(SpeedCopConstants.TAG,"nowHighWay!=isHighWay:-->nowHighWay"+nowHighWay);
		    				 locationManager.removeUpdates(locationListener);
		    				 if(nowHighWay)
		    					 locationManager.requestLocationUpdates(providerUsed, SpeedCopConstants.HIGHWAY_LOC_LISTEN_MIN_TIME, 
		    							 SpeedCopConstants.HIGHWAY_LOC_LISTEN_MIN_DISTANCE, locationListener);
		    				 else{
		    					 if(LocationManager.NETWORK_PROVIDER.equals(providerUsed)){
		    		    			 locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, SpeedCopConstants.NW_LOCATION_LISTEN_MIN_TIME, 
		    	                             SpeedCopConstants.NW_LOCATION_LISTEN_MIN_DISTANCE, locationListener);
		    		    		}else if(LocationManager.GPS_PROVIDER.equals(providerUsed)){
		    		    			 locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, SpeedCopConstants.GPS_LOCATION_LISTEN_MIN_TIME, 
		    		                         SpeedCopConstants.GPS_LOCATION_LISTEN_MIN_DISTANCE, locationListener);
		    		    		}		    					 
		    				 }
		    				 isHighWay=nowHighWay;
		    			}
		    			
		    			if(!isNotified){ //If velocity > than minimum and loopCnt is 2 then display notification. so donot have to redraw again and again.
		    				createNotification(SCCheckMovementService.this);
		    				dbAdapter=SpeedCopUtil.initDbAdapter(SCCheckMovementService.this,dbAdapter);
		    				dbAdapter.updateAppDriveModeStatus(SpeedCopConstants.DRIVE_MODE_STATUS_ON);
		    				isNotified=true;
		    			}
		    		}else{
		    			Log.d(SpeedCopConstants.TAG,"NO LOCATION CHANGE --- LOCATION IN METERS: "+results[0]);
		    			stopSelf();
		    		}
		    		accuracyList.clear();
	    		}else{
	    			loopCnt--; //since this callbacks are erratic, i control when to take new location.//This is before Jelly Bean...since the minTime parameter (on requestlocation method) was only a hint, and some location provider implementations ignored it
	    		}
	    	}else{
	    		Log.d(SpeedCopConstants.TAG,"CHANGE IN LOCATION ROUND 1 ,,3..5..");
	    		time1 = System.currentTimeMillis();
	    		locLat1= location.getLatitude();
	    		locLon1 = location.getLongitude();
	    	}
	    	loopCnt++;
	    }
	    
	    public void onStatusChanged(String provider, int status, Bundle extras) {
	    }

	    public void onProviderEnabled(String provider) {
	    }

	    public void onProviderDisabled(String provider) {
	    	Log.d(SpeedCopConstants.TAG,"onProviderDisabled:"+provider);
	    	if(provider.equals(providerUsed)){
	    		if(LocationManager.GPS_PROVIDER.equals(provider) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
	    			 locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, SpeedCopConstants.NW_LOCATION_LISTEN_MIN_TIME, 
                             SpeedCopConstants.NW_LOCATION_LISTEN_MIN_DISTANCE, locationListener);
	    			 providerUsed = LocationManager.NETWORK_PROVIDER;	    			
	    		}else if(LocationManager.NETWORK_PROVIDER.equals(provider) && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
	    			 locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, SpeedCopConstants.GPS_LOCATION_LISTEN_MIN_TIME, 
	                         SpeedCopConstants.GPS_LOCATION_LISTEN_MIN_DISTANCE, locationListener);
	    			 providerUsed = LocationManager.GPS_PROVIDER;    			
	    		}else{
	    			Log.d(SpeedCopConstants.TAG,"NO PROVIDER to use..except passive..so..stopself");
	    			SCCheckMovementService.this.stopSelf();
	    		}
	    	}
	    }
	  };

	public void createNotification(Context context){
		NotificationManager nManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.sc_note_icon;
		Notification notification = new Notification(icon, getText(R.string.app_name), System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, SpeedCopActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, getText(R.string.notification_title), getText(R.string.notification_text), contentIntent);
		nManager.notify(SpeedCopConstants.NOTIFICATION_UNNIQUE_ID,notification);
	}
	
	public float findAverageAccuracy(List<Float> aList){
		float tot=0;
		for(int i=0;i<aList.size();i++)
			tot = tot + (Float)aList.get(i);
		return tot/aList.size(); //average accuracy
	}

}
