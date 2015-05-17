package com.mumuWeibo2;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MsgHolder extends ActivityGroup{
	
	final String TAG="TAG------->MsgHolder";
	
	ImageView tvAtMe;
	ImageView tvComment;
	LinearLayout bodyView1;
	
	String atId="4";
	String comId="5";
	
	private Intent atIntent;
	private Intent comIntent;
	
	public void onCreate(Bundle bb){
		super.onCreate(bb);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.msg_layout);
		
		Log.i(TAG,"onCreate");
		
		MumuWeiboUtility.isSeized=true;
		
		bodyView1=(LinearLayout)findViewById(R.id.msg_body_view);
		tvAtMe=(ImageView)findViewById(R.id.atme);
		tvComment=(ImageView)findViewById(R.id.rec_comment);
		
		atIntent=new Intent().setClass(this, UserWeibosShow.class);
		
		comIntent=new Intent().setClass(this, UserWeibosShow.class);
		
		tvAtMe.setOnClickListener(lis);
		tvComment.setOnClickListener(lis);		
		
		show(2);
	}
	
	private OnClickListener lis=new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v==tvAtMe){
				show(1);
			}
			else if(v==tvComment){
				show(2);
			}
		}
		
	};
	
	public void show(int i){
		if(i==1){
			  bodyView1.removeAllViews();  
			  atIntent.putExtra("action", "@me");
	          View v = getLocalActivityManager().startActivity(atId,  
	                 this.atIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)).getDecorView();        
	          LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(
						 LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
	       v.setLayoutParams(params);
	          bodyView1.addView(v); 	
	          tvAtMe.setImageResource(R.drawable.atme_pressed);
	          tvComment.setImageResource(R.drawable.com_bd_normal);
	         // tvAtMe.setBackgroundResource(R.drawable.left_mark_pressed_bd);
	         // tvComment.setBackgroundResource(R.drawable.right_mark_bd);
	         
			}
		if(i==2){
			  bodyView1.removeAllViews();  
			  comIntent.putExtra("action", "comment");
	          View v = getLocalActivityManager().startActivity(comId,  
	                 this.comIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)).getDecorView();        
	          LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(
						 LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
	       v.setLayoutParams(params);
	          bodyView1.addView(v); 
	          tvComment.setImageResource(R.drawable.com_pressed_bd);
	         // tvComment.setBackgroundResource(R.drawable.right_mark_pressed_bd);
	          tvAtMe.setImageResource(R.drawable.atmee);
	          //tvAtMe.setBackgroundResource(R.drawable.left_mark_bd);
		}
	}
	
	public void onResume()
	{
		super.onResume();
		Log.i(TAG,"OnResume");
		
	}
	
	public void onPause()
	{
		super.onPause();
		Log.i(TAG,"OnPause");
	}
	public void onRestart()
	{
		super.onRestart();
		Log.i(TAG,"OnRestart");
	}
	public void onStop()
	{
		super.onStop();
		Log.i(TAG,"OnStop");
	}
}
