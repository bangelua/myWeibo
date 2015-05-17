package com.mumuWeibo2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class SplashScreen extends Activity{

		public void onCreate(Bundle bb)
		{
			super.onCreate(bb);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		    setContentView(R.layout.splash); 
		    
		    
		    final Intent i=new Intent();
		    i.setClass(SplashScreen.this, AppMain.class);
		    
		    new Handler().postDelayed(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					SplashScreen.this.startActivityForResult(i,0);
				}
		    	
		    }, 1500);
		    
		    
		}
		
		protected void  onActivityResult(int requestCode,int resultCode,Intent data){
			 finish();
		}



}
