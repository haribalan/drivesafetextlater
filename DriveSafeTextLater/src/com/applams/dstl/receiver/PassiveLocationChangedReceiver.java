package com.applams.dstl.receiver;

import com.applams.dstl.util.SpeedCopConstants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class PassiveLocationChangedReceiver extends BroadcastReceiver {

	/**
	 * When a new location is received, extract it from the Intent and use it to start the Service used to update the
	 * list of nearby places. This is the Passive receiver, used to receive Location updates from third party apps when
	 * the Activity is not visible.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		   Location location = null;

		if (intent.hasExtra(LocationManager.KEY_LOCATION_CHANGED)) {
			location = (Location) intent.getExtras().get(LocationManager.KEY_LOCATION_CHANGED);
			// TODO implement this part! may be check movement detection, compare old localtion is displacement is there then start service!?
		} else {
		}

		if (location != null) {
			Log.e(SpeedCopConstants.TAG,"PassiveLocationChangedReceiver: "+ location.getLatitude() + "," + location.getLongitude());
//			SharedPreferences prefs = context.getSharedPreferences(SpeedCopConstants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
//			Editor editor = prefs.edit();
//			editor.putLong(PreferenceKeys.LAST_LOCATION_UPDATE_TIME, System.currentTimeMillis());
//			editor.putFloat(PreferenceKeys.LAST_LOCATION_LAT, (float) location.getLatitude());
//			editor.putFloat(PreferenceKeys.LAST_LOCATION_LON, (float) location.getLongitude());
//			editor.commit();
		}
	}
}
