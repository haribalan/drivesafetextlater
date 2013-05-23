package com.applams.dstl.util;

import java.util.ArrayList;
import java.util.List;

import com.applams.dstl.data.SpeedCopDbAdapter;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.telephony.SmsManager;
import android.util.Log;
/**
 * @author Hari Raghupathy  12/31/2011
 */
public class SpeedCopUtil {
	
	private static  SpeedCopUtil objectInstance;
	
	private SpeedCopUtil(){
	}
	
	public static SpeedCopUtil getInstance(){
		if(objectInstance==null){
			objectInstance = new SpeedCopUtil();
			return objectInstance;
		}
		return objectInstance;
	}
	
	// TODO replace this method with SharedPreferences
	public static boolean isServiceRunning(Context context,String serviceName) {
	    ActivityManager manager = (ActivityManager) context.getSystemService(Service.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceName.equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	/**
	 * Important.Read return rules.  
	 * This return true or false and ALSO saves the given scan list if its any different from the database. 
	 * @return TRUE if the NowScanResult and DBScanResult does not have  single BSSID match..TRUE if the NowScanResult is empty. 
	 * False if the NowScanResult and DBScanResult at-least have  single BSSID match.
	 */
	public static boolean isWifiAreaChangedorEmptyAndSave(List<ScanResult> scanList ,SpeedCopDbAdapter dbAdapter){
		Log.d(SpeedCopConstants.TAG,"WIFI CHANGE CHECK CALLED...");
		if(scanList.size()<1)
			return true; //since the NOW area does not have any Wifiscanresult (say forest) we assume area changed.
		List<String> currScanList = new ArrayList<String>(scanList.size());
		List<String> toDbScanList = new ArrayList<String>(scanList.size());
		for(int i=0;i<scanList.size();i++){
			currScanList.add(((ScanResult)scanList.get(i)).BSSID);
			toDbScanList.add(((ScanResult)scanList.get(i)).BSSID);
			Log.d(SpeedCopConstants.TAG,"CURRENT WIFI SCANSET:"+((ScanResult)scanList.get(i)).BSSID);
		}
		List<String> fromDbList =dbAdapter.getLastWifiScanResult();
		for(int i=0;i<fromDbList.size();i++)
			Log.d(SpeedCopConstants.TAG,"DB WIFI SCANSET:"+fromDbList.get(i));
		
		currScanList.retainAll(fromDbList);
		if(currScanList.size()!=toDbScanList.size()) //currScan has changed during the compare (retainAll call) with Db list, then save it. Means currList had some extra bssids.
			dbAdapter.saveNewWifiScanSet(toDbScanList);
		if(currScanList.size()>0){ //even if 1 item is left, mean one Bssid matched, which means user in same place as before.
			Log.d(SpeedCopConstants.TAG,"WIFI CHANGE  **FALSE**...");
			return false;
		}
		Log.d(SpeedCopConstants.TAG,"WIFI CHANGE  **TRUE**...");
		return true;
	}
	
	/**
	 * Save the BSSIDs to the database.
	 */
	public static void saveWifiScanList(List<ScanResult> scanList ,SpeedCopDbAdapter dbAdapter){
		Log.d(SpeedCopConstants.TAG,"ENTER SAVE WIFI LIST DB..");
		if(scanList==null || scanList.size()<1)
			return; 
		Log.d(SpeedCopConstants.TAG,"TO SAVE: "+scanList.size());
		List<String> toDbScanList = new ArrayList<String>(scanList.size());
		for(int i=0;i<scanList.size();i++){
			toDbScanList.add(((ScanResult)scanList.get(i)).BSSID);
			Log.d(SpeedCopConstants.TAG,"SAVEing WIFI SCANSET:"+((ScanResult)scanList.get(i)).BSSID);
		}
		dbAdapter.saveNewWifiScanSet(toDbScanList);
		Log.d(SpeedCopConstants.TAG,"EXIT WIFI SAVE DONE SUCCESS");
	}
	
	public static SpeedCopDbAdapter initDbAdapter(Context context,SpeedCopDbAdapter dbAdapter){
		if(dbAdapter!=null)
			return dbAdapter;
		dbAdapter = new  SpeedCopDbAdapter(context);
		dbAdapter.open();
		return dbAdapter;
	}
	public static void closeDbAdapter(SpeedCopDbAdapter dbAdapter){
		if(dbAdapter==null)
			return;
		dbAdapter.close();
	}
	
	
	public static void sendSMS(String phoneNumber, String message){
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null); 
	}
}
