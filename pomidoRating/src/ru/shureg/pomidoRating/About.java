package ru.shureg.pomidoRating;


import ru.shureg.pomidoRating.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class About extends Activity { 
	
	 
		
	@Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
    
        
    }
	
	
	 public void onOkPressed(View view)  
	  {  
			finish();
      }  
	
}
