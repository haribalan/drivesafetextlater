package com.applams.dstl.ui;

import com.applams.dstl.R;
import com.applams.dstl.data.SpeedCopDbAdapter;
import com.applams.dstl.service.SCCheckMovementService;
import com.applams.dstl.util.SpeedCopConstants;
import com.applams.dstl.util.SpeedCopUtil;

import android.R.drawable;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @author Hari Raghupathy  dec 12-09-2011
 */
public class SpeedCopActivity extends Activity implements View.OnClickListener {
    private SpeedCopDbAdapter dbAdapter;
    private boolean IS_APP_ENABLED;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button startButton = (Button)findViewById(R.id.startDrvMode);
        startButton.setOnClickListener(this);
        
        Button stopButton = (Button)findViewById(R.id.stopDrvMode);
        stopButton.setOnClickListener(this);
       
        if(SpeedCopUtil.isServiceRunning(this, SpeedCopConstants.CHECK_MOVEMENT_SERVICE_CLASS_NAME)){
        	startButton.setClickable(false);
        	startButton.setBackgroundResource(drawable.button_onoff_indicator_off);
        	stopButton.setBackgroundResource(drawable.button_onoff_indicator_on);
        }else{
        	stopButton.setClickable(false);
        	startButton.setBackgroundResource(drawable.button_onoff_indicator_on);
        	stopButton.setBackgroundResource(drawable.button_onoff_indicator_off);
        }
        
        EditText editText = (EditText)findViewById(R.id.editText1);
        dbAdapter=SpeedCopUtil.initDbAdapter(this,dbAdapter);
       	editText.setText(dbAdapter.getAutoText());
        
       	Button saveButton = (Button)findViewById(R.id.button1);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	saveAutoText(v);
            	Toast.makeText(SpeedCopActivity.this,SpeedCopConstants.AUTO_RESPOND_MESSAGE_SAVED, Toast.LENGTH_SHORT).show();
            	setResult(RESULT_OK);
            	finish();
            }
        });
        
        IS_APP_ENABLED = dbAdapter.isAppEnabled();
        Button enableAppBtn = (Button)findViewById(R.id.appEnableBtn);
        if(IS_APP_ENABLED)
        	enableAppBtn.setBackgroundResource(R.drawable.button_on);
        else
        	enableAppBtn.setBackgroundResource(R.drawable.button_off);
        
        enableAppBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(IS_APP_ENABLED){
					dbAdapter.updateAppStatus(SpeedCopConstants.APP_STATUS_OFF);
					v.setBackgroundResource(R.drawable.button_off);
					IS_APP_ENABLED = false;
	        		Toast.makeText(SpeedCopActivity.this,SpeedCopConstants.APP_DISABLED, Toast.LENGTH_LONG).show();
	        	}else{
	        		dbAdapter.updateAppStatus(SpeedCopConstants.APP_STATUS_ON);
	        		v.setBackgroundResource(R.drawable.button_on);
	        		IS_APP_ENABLED = true;
	        		Toast.makeText(SpeedCopActivity.this,SpeedCopConstants.APP_ENABLED, Toast.LENGTH_LONG).show();
	        	}
            	setResult(RESULT_OK);
			}
		});
        
      	Button doneButton = (Button)findViewById(R.id.doneBtn);
      	doneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	setResult(RESULT_OK);
            	finish();
            }
        });
    }
    
    public void onClick(View src) {
        switch (src.getId()) {
        case R.id.startDrvMode:
          src.setClickable(false);
          ((Button)this.findViewById(R.id.stopDrvMode)).setClickable(true);
          ((Button)this.findViewById(R.id.stopDrvMode)).setBackgroundResource(drawable.button_onoff_indicator_on);
          src.setBackgroundResource(drawable.button_onoff_indicator_off); 
          if(!SpeedCopUtil.isServiceRunning(this, SpeedCopConstants.CHECK_MOVEMENT_SERVICE_CLASS_NAME))
        	  startService(new Intent(this, SCCheckMovementService.class));
          Toast.makeText(this,SpeedCopConstants.STARTED_DRIVEMODE, Toast.LENGTH_SHORT).show();
          setResult(RESULT_OK);
          finish();
          break;
        case R.id.stopDrvMode:
          src.setClickable(false);
          ((Button)this.findViewById(R.id.startDrvMode)).setClickable(true);
          ((Button)this.findViewById(R.id.startDrvMode)).setBackgroundResource(drawable.button_onoff_indicator_on);
          src.setBackgroundResource(drawable.button_onoff_indicator_off);          
          stopService(new Intent(this, SCCheckMovementService.class));
          Toast.makeText(this,SpeedCopConstants.STOP_DRIVEMODE, Toast.LENGTH_SHORT).show();
          setResult(RESULT_OK);
          break;
        }
   }
  
    private void saveAutoText(View v){
    	updateAutoText();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	updateAutoText();
    }
    
    private void updateAutoText(){
      dbAdapter=SpeedCopUtil.initDbAdapter(this,dbAdapter);
	  EditText editText = (EditText)findViewById(R.id.editText1);
	  dbAdapter.updateAutoText(editText.getText().toString());
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if(dbAdapter==null)
    		return;
    	SpeedCopUtil.closeDbAdapter(dbAdapter);
    }
   
}