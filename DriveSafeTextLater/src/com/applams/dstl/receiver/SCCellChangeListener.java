package com.applams.dstl.receiver;

import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
/**
 * @author Hari Raghupathy  12/31/2011
 */
public class SCCellChangeListener extends PhoneStateListener {

public static SCCellChangeListener objectCallBack=null; //Just a way to get this Listener from any where to deregister it when not used.

public SCCellChangeListener() {
	super();
}

@Override
public void onCellLocationChanged(CellLocation location) {
	super.onCellLocationChanged(location);
}
  
}
