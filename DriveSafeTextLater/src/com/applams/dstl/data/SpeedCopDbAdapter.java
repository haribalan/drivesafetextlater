package com.applams.dstl.data;

import java.util.ArrayList;
import java.util.List;

import com.applams.dstl.util.SpeedCopConstants;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Hari Raghupathy 
 * dec 12-10-2011
 */
public class SpeedCopDbAdapter {
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase liteDatabase;
    private static final String DATABASE_NAME = "scdbinstance";
    private static final int DATABASE_VERSION = 1;
    private final Context mCtx;
    
    public static final String TABLE_COL_ROWID = "_id";
    public static final String TABLE_COL_TS = "timestamp";
    
    private static final String TABLE_AUTO_TXT = "smsTable";
    public static final String AT_COL_TXT = "autotext";

    private static final String TABLE_WIFI_SCAN = "wifiScan";
    public static final String WS_COL_BSSID = "bssid";

    private static final String TABLE_SMS_RECEIVED = "smsReceived";
    
    private static final String TABLE_APP_STATUS = "appStatus"; //disable or enabled by user
    private static final String AS_COL_STATUS = "status";

    private static final String TABLE_CAR_DOCK_STATUS = "carDockStatus";
    private static final String CDS_COL_STATUS = "status";
    
    private static final String TABLE_APP_DRIVE_MODE_STATUS = "appDriveModeStatus"; //is app movement/location service running
    private static final String ADMS_COL_STATUS = "status";
    
    private static final String TABLE_LAST_LOC_UPDATE_TIME = "lastLocationUpdateTime"; 

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table "+TABLE_AUTO_TXT+" ("+TABLE_COL_ROWID+" integer primary key autoincrement,"+AT_COL_TXT+" text not null);");
            ContentValues values = new ContentValues();
            values.put(AT_COL_TXT, SpeedCopConstants.DEFAULT_TEXT_AUTO_RESPOND_MSG);
            db.insert(TABLE_AUTO_TXT, null, values);// just maintain a ..Only One row table.
            
            db.execSQL("create table "+TABLE_WIFI_SCAN+" ("+TABLE_COL_ROWID+" integer primary key autoincrement,"
            									+WS_COL_BSSID+" text not null,"+TABLE_COL_TS+" long not null);");
            
            db.execSQL("create table "+TABLE_SMS_RECEIVED+"("+TABLE_COL_TS+" long not null);");
            db.execSQL("insert into  "+TABLE_SMS_RECEIVED +" ( "+TABLE_COL_TS+" ) values(1000)"); //just for the first time mark it as 1000.
            
            db.execSQL("create table "+TABLE_APP_STATUS+" ("+AS_COL_STATUS+" integer not null);"); //ENABLE.DISABLE usually toggle between 1 and 0
            db.execSQL("insert into  "+TABLE_APP_STATUS +" ( "+AS_COL_STATUS+" ) values(1)"); //by default app is enabled
            
            db.execSQL("create table "+TABLE_CAR_DOCK_STATUS+" ("+CDS_COL_STATUS+" integer not null);"); //usually toggle between 1 and 0
            db.execSQL("insert into  "+TABLE_CAR_DOCK_STATUS +" ( "+CDS_COL_STATUS+" ) values(0)");
            
            db.execSQL("create table "+TABLE_APP_DRIVE_MODE_STATUS+" ("+ADMS_COL_STATUS+" integer not null);"); //usually toggle between 1 and 0
            db.execSQL("insert into  "+TABLE_APP_DRIVE_MODE_STATUS +" ( "+ADMS_COL_STATUS+" ) values(0)");//app drive mode running or not
            
            db.execSQL("create table "+TABLE_LAST_LOC_UPDATE_TIME+" ("+TABLE_COL_TS+" long not null);"); 
            db.execSQL("insert into  "+TABLE_LAST_LOC_UPDATE_TIME +" ( "+TABLE_COL_TS+" ) values(1000)");//just for the first time mark it as 1000
            
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//            Log.w(SpeedCopConstants.LOG_APP_NAME, "Upgrading database from version " + oldVersion + " to "
//                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+TABLE_AUTO_TXT);
            db.execSQL("DROP TABLE IF EXISTS "+TABLE_WIFI_SCAN);
            db.execSQL("DROP TABLE IF EXISTS "+TABLE_SMS_RECEIVED);
            db.execSQL("DROP TABLE IF EXISTS "+TABLE_APP_STATUS);
            db.execSQL("DROP TABLE IF EXISTS "+TABLE_CAR_DOCK_STATUS);
            db.execSQL("DROP TABLE IF EXISTS "+TABLE_APP_DRIVE_MODE_STATUS);
            db.execSQL("DROP TABLE IF EXISTS "+TABLE_LAST_LOC_UPDATE_TIME);
            onCreate(db);
        }
    }

    public SpeedCopDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public SpeedCopDbAdapter open() throws SQLException {
        databaseHelper = new DatabaseHelper(mCtx);
        liteDatabase = databaseHelper.getWritableDatabase();
        return this;
    }

    public void close() {
    	databaseHelper.close();
    }

    public void updateAutoText(String msg) {
        ContentValues values = new ContentValues();
        values.put(AT_COL_TXT, msg);
        liteDatabase.update(TABLE_AUTO_TXT, values, null, null);// just maintain a ..Only One row table.
    }

    public String getAutoText() {
    	SQLiteCursor cursor = (SQLiteCursor) liteDatabase.query(TABLE_AUTO_TXT, new String[] {TABLE_COL_ROWID, AT_COL_TXT}, null, null, null, null, null);
        try{
	        if(cursor!=null && cursor.moveToFirst())
	        	return cursor.getString(cursor.getColumnIndexOrThrow(SpeedCopDbAdapter.AT_COL_TXT));
	        else
	        	return "";
        }finally{
        	cursor.close();
        }
    }
    
    //TABLE_SMS_RECEIVED table Works
    public void updateSmsReceivedTimeStamp(long timestamp){
        ContentValues values = new ContentValues();
        values.put(TABLE_COL_TS, timestamp);
        liteDatabase.update(TABLE_SMS_RECEIVED, values, null, null);// just maintain a ..Only One row table.
    }

    public long getLastSmsTS() {
        SQLiteCursor cursor = (SQLiteCursor) liteDatabase.query(TABLE_SMS_RECEIVED, new String[] {TABLE_COL_TS}, null, null, null, null, null);
        try{
	        if(cursor!=null && cursor.moveToFirst())
	        	return cursor.getLong(cursor.getColumnIndexOrThrow(SpeedCopDbAdapter.TABLE_COL_TS));
	        else
	        	return 0;
        }finally{
        	cursor.close();
        }
    }
    
    /**
     * returns wifi bssid  list or just empty list.
     */
    public synchronized List<String> getLastWifiScanResult(){
        SQLiteCursor cursor = (SQLiteCursor) liteDatabase.query(TABLE_WIFI_SCAN, new String[] {WS_COL_BSSID}, null, null, null, null, null);
        List<String> rtnList = new ArrayList<String>(); 
        try{
	        if(cursor!=null && cursor.moveToFirst()){
	        	do{
	        		rtnList.add(cursor.getString(cursor.getColumnIndexOrThrow(SpeedCopDbAdapter.WS_COL_BSSID)));
	        	}while(cursor.moveToNext());
	        }
	        return rtnList;
        }finally{
        	cursor.close();
        }
    }
    
    public synchronized void saveNewWifiScanSet(List<String> scanList){
    	liteDatabase.beginTransaction();
    	try{
	    	liteDatabase.delete(TABLE_WIFI_SCAN, null, null); //Just delete all data from the table.
	    	long currTime = System.currentTimeMillis();
	    	ContentValues values = null;
	    	for(int i=0;i<scanList.size();i++){
		        values = new ContentValues();
		        values.put(WS_COL_BSSID, scanList.get(i));
		        values.put(TABLE_COL_TS, currTime);
		        liteDatabase.insert(TABLE_WIFI_SCAN, null, values);
	    	}
	        liteDatabase.setTransactionSuccessful();
    	}
    	finally{
    		liteDatabase.endTransaction();
    	}
    }
    
    
    public synchronized void updateCarDockStatus(int status){
        ContentValues values = new ContentValues();
        values.put(CDS_COL_STATUS, status);
        liteDatabase.update(TABLE_CAR_DOCK_STATUS, values, null, null);// just maintain a ..Only One row table.
    }
    
    
    public synchronized boolean isCarDockOn() {
        SQLiteCursor cursor = (SQLiteCursor) liteDatabase.query(TABLE_CAR_DOCK_STATUS, new String[] {CDS_COL_STATUS}, null, null, null, null, null);
        try{
        	cursor.moveToFirst();
        	if(SpeedCopConstants.CAR_DOCK_STATUS_ON==cursor.getInt(cursor.getColumnIndexOrThrow(SpeedCopDbAdapter.CDS_COL_STATUS)))
        		return true;
        	return false;
        }finally{
        	cursor.close();
        }
    }
    
    public synchronized void updateAppStatus(int status){
        ContentValues values = new ContentValues();
        values.put(AS_COL_STATUS, status);
        liteDatabase.update(TABLE_APP_STATUS, values, null, null);// just maintain a ..Only One row table.
    }
    
    public synchronized boolean isAppEnabled() {
        SQLiteCursor cursor = (SQLiteCursor) liteDatabase.query(TABLE_APP_STATUS, new String[] {AS_COL_STATUS}, null, null, null, null, null);
        try{
        	cursor.moveToFirst();
        	if(SpeedCopConstants.APP_STATUS_ON==cursor.getInt(cursor.getColumnIndexOrThrow(SpeedCopDbAdapter.AS_COL_STATUS)))
        		return true;
        	return false;
        }finally{
        	cursor.close();
        }
    }

    public synchronized void updateAppDriveModeStatus(int status){
        ContentValues values = new ContentValues();
        values.put(ADMS_COL_STATUS, status);
        liteDatabase.update(TABLE_APP_DRIVE_MODE_STATUS, values, null, null);// just maintain a ..Only One row table.
    }
    
    public synchronized boolean isAppDriveModeOn() {
        SQLiteCursor cursor = (SQLiteCursor) liteDatabase.query(TABLE_APP_DRIVE_MODE_STATUS, new String[] {ADMS_COL_STATUS}, null, null, null, null, null);
        try{
        	cursor.moveToFirst();
        	if(SpeedCopConstants.DRIVE_MODE_STATUS_ON==cursor.getInt(cursor.getColumnIndexOrThrow(SpeedCopDbAdapter.ADMS_COL_STATUS)))
        		return true;
        	return false;
        }finally{
        	cursor.close();
        }
    }

    
    // TABLE_LAST_LOC_UPDATE_TIME table Works
    public void updateLastLocReceivedTimeStamp(long timestamp){
        ContentValues values = new ContentValues();
        values.put(TABLE_COL_TS, timestamp);
        liteDatabase.update(TABLE_LAST_LOC_UPDATE_TIME, values, null, null);// just maintain a ..Only One row table.
    }

    public long getLastLocTS() {
        SQLiteCursor cursor = (SQLiteCursor) liteDatabase.query(TABLE_LAST_LOC_UPDATE_TIME, new String[] {TABLE_COL_TS}, null, null, null, null, null);
        try{
	        if(cursor!=null && cursor.moveToFirst())
	        	return cursor.getLong(cursor.getColumnIndexOrThrow(SpeedCopDbAdapter.TABLE_COL_TS));
	        else
	        	return 0;
        }finally{
        	cursor.close();
        }
    }
}
