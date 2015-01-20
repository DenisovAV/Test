package ru.shureg.pomidoRating;

import ru.shureg.pomidoRating.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class Settings extends Activity { 
	
	  public static final String CATCH_PREFERENCES_AUTO = "auto";
	  public static final String CATCH_PREFERENCES_SERVER = "server";
	  public static final String CATCH_PREFERENCES_SAVE= "save";
	  public static final int CATCH_SETTING_REQUEST_CODE= 1;

	CheckBox settingAutoCheckBox, settingServerCheckBox, settingSaveCheckBox; 
		
	@Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        Log.v("SHUREG","3");
        setContentView(R.layout.settings);
        settingAutoCheckBox = (CheckBox) findViewById(R.id.settingAutoCheckBox);
        settingServerCheckBox = (CheckBox) findViewById(R.id.settingServerCheckBox); 
        settingSaveCheckBox = (CheckBox) findViewById(R.id.settingSaveCheckBox);
        
        settingAutoCheckBox.setOnCheckedChangeListener(setSettings);
    }
	
	CompoundButton.OnCheckedChangeListener setSettings=new CompoundButton.OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

			switch (buttonView.getId())
			{
				case R.id.settingAutoCheckBox:
					MainActivity.settingAuto=isChecked;
				case R.id.settingServerCheckBox:
					MainActivity.settingServer=isChecked;
				case R.id.settingSaveCheckBox:
					MainActivity.settingSave=isChecked;
			}
			
		}
	};
	
	@Override
    protected void onResume() 
   	{
		super.onResume();
		Bundle b=getIntent().getExtras();
		settingAutoCheckBox.setChecked(b.getBoolean(CATCH_PREFERENCES_AUTO));
		settingServerCheckBox.setChecked(b.getBoolean(CATCH_PREFERENCES_SERVER));
		settingSaveCheckBox.setChecked(b.getBoolean(CATCH_PREFERENCES_SAVE));
	    Log.v("SHUREG","3");
    }
	
	 public void onOkPressed(View view)  
	  {  
			Intent answerIntent = new Intent();
			answerIntent.putExtra(CATCH_PREFERENCES_AUTO, settingAutoCheckBox.isChecked());
			answerIntent.putExtra(CATCH_PREFERENCES_SERVER, settingServerCheckBox.isChecked());
			answerIntent.putExtra(CATCH_PREFERENCES_SAVE, settingSaveCheckBox.isChecked());
			Log.v("SHUREG", "ûûûfdfdfdfd");
			setResult(RESULT_OK, answerIntent);
			finish();
  
	  }  
	
}
