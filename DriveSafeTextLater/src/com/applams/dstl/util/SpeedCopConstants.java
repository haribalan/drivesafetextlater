package com.applams.dstl.util;
/**
 * @author Hari Raghupathy  12/31/2011
 */
public class SpeedCopConstants {
 public static final String TAG = "com.applams.dstl";
 
 public static final int DEFAULT_VALUE = -10; //this can be any value...but nagative is safe...just used threw our the app to know , default results.
 public static final int NOTIFICATION_UNNIQUE_ID = 55; //an arbitrary  number to to find  notification (with in our app only)
 
 //if the broadcast is for wifi disconnect then we wait for the movement service to begin for some time ...so delay-rescan-and start movement test
 public static final String INTENT_EXTRA_MOV_LOC_DELAY_TYPE = "com.applams.dstl.wifi.scandelay";
 public static final int INTENT_EXTRA_WIFI_SCAN_DELAY_YES_FLAG = 1; 
 public static final int INTENT_EXTRA_SMS_DELAY_YES_FLAG = 2; 
 public static final int INTENT_EXTRA_JUST_ONLY_START_SERVICE_FLAG = 3;
 
 public static final String INTENT_EXTRA_TEXT_TO_READ_OUT ="TEXT_TO_READ_OUT";
 
 public static final long WIFI_DISCONTED_START_LOC_MOV_INTERVAL =  150000; //2.5 minutes
 //SMS RECEIVED TIME GAP...
 public static final long SMS_RECEIVED_ELAPSED_MILLI_SECONDS = 300000; //5 minutes

 public static final long PASSIVE_LOCATION_LISTENER_MIN_TIME_INTERVAL = 180000; //3 minutes
 public static final long PASSIVE_LOCATION_LISTENER_MIN_DISTANCE = 1000; //METERS
 
//Movement calculation parameters.
public static final long NW_LOCATION_LISTEN_MIN_TIME =  180000;// 120000; //2 minutes // RATE AT WHICH THE LOCATION MUST BE CHECKED
public static final float NW_LOCATION_LISTEN_MIN_DISTANCE =   1000; // METERS  1.25miles
public static final long GPS_LOCATION_LISTEN_MIN_TIME =  180000;// 120000; //2 minutes // RATE AT WHICH THE LOCATION MUST BE CHECKED
public static final float GPS_LOCATION_LISTEN_MIN_DISTANCE =   2500; // METERS  1.25miles
public static final long HIGHWAY_LOC_LISTEN_MIN_TIME =  240000;// 120000; //2 minutes // RATE AT WHICH THE LOCATION MUST BE CHECKED
public static final float HIGHWAY_LOC_LISTEN_MIN_DISTANCE =   6400; // METERS  4miles
public static final long ELAPSED_TIME_TO_FIND_MOVEMENT = 300000; 
public static final double MINUMUM_DRIVER_VELOCITY =  135; //      275 METERS/MINUTE  (I.E) ~ 10miles/hr ,   213m/m==8mi/hr
public static final double HIGHWAY_DRIVER_VELOCITY =  1200; // METERS/MINUTE  (I.E) ~ 45miles/hr 
public static final float MAX_ACCURACY_DEVIATION =  1000; // METERS
public static final long LAST_LOCATION_CHECK_INTERVAL = 315000;//5mts 15sec SHOULD  LINK IT WITH WIFI_DISCONTED_START_LOC_MOV_INTERVAL and ELAPSED_TIME_TO_FIND_MOVEMENT??
//below just call first time with more delay..since we ignore the first location hit 
public static final long FIRST_TRIGGER_LAST_LOCATION_CHECK_ALARM_TIME = LAST_LOCATION_CHECK_INTERVAL+15000;
  
 //DB Related
 public static final int CAR_DOCK_STATUS_ON = 1;
 public static final int CAR_DOCK_STATUS_OFF = 0;
 public static final int DRIVE_MODE_STATUS_ON = 1;
 public static final int DRIVE_MODE_STATUS_OFF = 0;
 public static final int APP_STATUS_ON = 1;  //IF USER DISABLES OR ENABLES IT IN THE GUI...
 public static final int APP_STATUS_OFF = 0; 
 
 
 //Service Names.
 public static final String CHECK_MOVEMENT_SERVICE_CLASS_NAME = "com.applams.dstl.service.SCCheckMovementService";
 public static final String INTENT_SMS_RECEIVED ="android.provider.Telephony.SMS_RECEIVED"; 
 
 //User Messages
 public static final String  STARTED_DRIVEMODE = "DriveMode Started! Drive Safe.";
 public static final String  STOP_DRIVEMODE = "Exited DriveMode.";
 public static final String  APP_DISABLED = "Application disabled. Please remember to enable it again.";
 public static final String  APP_ENABLED = "Application enabled. We recommend Manual Start the Drive Mode before you start driving.";
 public static final String  AUTO_RESPOND_MESSAGE_SAVED = "Auto response message saved!";
 public static final String DEFAULT_TEXT_AUTO_RESPOND_MSG =  "I am driving. Sent by DriveSafeTextLater.";//remember String.xml
 
 //Send and Receive SMS
 public static final String SMS_MESSAGE_BUNDLE_KEY = "pdus";
 
}
