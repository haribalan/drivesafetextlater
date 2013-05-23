package com.applams.dstl.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.applams.dstl.util.SpeedCopConstants;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class TTSService extends Service implements TextToSpeech.OnInitListener {
	private TextToSpeech textToSpeech;
	private List<String> textQ = Collections.synchronizedList(new ArrayList<String>()); //Just  in  two Service (two sms ) arrives too fast
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	@Override
	public void onCreate() {
		super.onCreate();
	}

	 @Override
	    public void onDestroy() {
	        if (textToSpeech != null) {
	            textToSpeech.stop();
	            textToSpeech.shutdown();
	        }
	        super.onDestroy();
	    }
	 
	 @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e(SpeedCopConstants.TAG,"INSIDE ONSTART OF TEH SERVUCE");
        String textToRead = intent!=null?intent.getStringExtra(SpeedCopConstants.INTENT_EXTRA_TEXT_TO_READ_OUT):null;
        if(textToRead==null)
        	textToRead="Empty Message";
        textQ.add(textToRead);
        textToSpeech = new TextToSpeech(this,this);
        Log.e(SpeedCopConstants.TAG,"END FROM SERVICE");
        return super.onStartCommand(intent, flags, startId);
	}
	 
	@Override
	public void onInit(int status) {
			Log.e(SpeedCopConstants.TAG,"INSIDE THE ONINIT");
		  if (status != TextToSpeech.SUCCESS) {
	            Log.e(SpeedCopConstants.TAG, "Could not initialize TextToSpeech.");
	        }else{
	        	synchronized (textQ){
	        		for(int i=0;i<textQ.size();i++)
	        			readText(textQ.get(i));
	        		Log.e(SpeedCopConstants.TAG,"TEXT Q SIZE-->"+textQ.size());
	        		textQ.clear();
	        	}
	        }
		  Log.e(SpeedCopConstants.TAG,"DONE THE ONINIT");
	}
	
    private void readText(String text) {
    	Log.e(SpeedCopConstants.TAG,"called and now inside the readTEXT...==>"+text);
        textToSpeech.speak(text,TextToSpeech.QUEUE_ADD, null);
        Log.e(SpeedCopConstants.TAG,"DONE the readTEXT...");
    }
}
