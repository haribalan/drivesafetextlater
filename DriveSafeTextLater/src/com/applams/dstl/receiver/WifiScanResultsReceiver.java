package com.applams.dstl.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.applams.dstl.data.SpeedCopDbAdapter;
import com.applams.dstl.util.SpeedCopConstants;
import com.applams.dstl.util.SpeedCopUtil;

public class WifiScanResultsReceiver extends BroadcastReceiver {
	SpeedCopDbAdapter dbAdapter;
	@Override
	public void onReceive(Context context, Intent intent) {
//		Log.d(SpeedCopConstants.TAG,"WifiScanResultsReceiver called.");
//		dbAdapter=SpeedCopUtil.initDbAdapter(context, dbAdapter);
//		WifiManager wManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
//		SpeedCopUtil.saveWifiScanList(wManager.getScanResults(), dbAdapter);
	}
}
