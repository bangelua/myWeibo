package com.mumuWeibo2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.AccountAPI;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.api.UsersAPI;
import com.weibo.sdk.android.api.WeiboAPI;
import com.weibo.sdk.android.net.RequestListener;
import com.weibo.sdk.android.sso.SsoHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MumuWeibo extends Activity implements OnScrollListener {
    /** Called when the activity is first created. */
		
	final String TAG="TAG------->MumuWeibo";
		
	//yibo's appkey.etc.
	//static String app_key="2849184197";
	//static String app_secret="7338acf99a00412983f255767c7643d0";
	//static String redirectUrl="http://www.yibo.me/authorize/getAccessToken.do";
	
	//static Oauth2AccessToken token=null;
	
	//static Weibo weibo;
	
	String cont=""; //weibo content.	
		
	private int firstVisibleItem;
	private int visibleCount;
	private int lastVisibleItem;
	
	int listpos;
	
	ImageButton sendWeibo;
	ImageView flushWeibo;

	SimpleDraweeView logo;
	TextView title;

	ProgressDialog progressDialog;
	
	private View loadMore;
	ImageButton loadMoreButton;
	Animation anim;
	//Animation anim2;
	private MyListView lv;
	private Button btLoadMore;
	View midView;
			
	SsoHandler mSsoHandler;

	private Handler handler=new Handler();
	
	public void showToast(final String s)
	{
		handler.post(new Runnable() {

			   @Override
			   public void run() {
			    Toast.makeText(getApplicationContext(), s,
			      Toast.LENGTH_SHORT).show();

			   }
			  });
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	    	
    	
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Log.i(TAG,"onCreate");
        setContentView(R.layout.main);
        
        //MumuWeiboUtility.isSeized=true;
           
        midView=getLayoutInflater().inflate(R.layout.midview, null);
        logo=(SimpleDraweeView)findViewById(R.id.iv_log);
        sendWeibo=(ImageButton)findViewById(R.id.sendweibo);        
        flushWeibo=(ImageView)findViewById(R.id.flushweibo);
       logo.setOnClickListener(btn_listener);
        sendWeibo.setOnClickListener(btn_listener);
        flushWeibo.setOnClickListener(btn_listener);
       lv=(MyListView)findViewById(R.id.listview_weibo);
       lv.setOnItemClickListener(listItemClickListener);     
       lv.setOnItemLongClickListener(lislong);
       //loadMore=getLayoutInflater().inflate(R.layout.load_more, null);
       title=(TextView)findViewById(R.id.tv_title_in_mainpage);
       MyListView.IOnRefreshListener refreshLis=new MyListView.IOnRefreshListener() {
   		
   		@Override
   		public void OnRefresh() {
   			// TODO Auto-generated method stub
   			flushWeibo();			
   		}
   	};
       lv.setOnRefreshListener(refreshLis);
       lv.setOnLoadMoreListener(new MyListView.IOnLoadMoreListener() {
		
		@Override
		public void OnLoadMore() {
			// TODO Auto-generated method stub
			getMoreWeibo();
		}
	});
       
       //setUserInfo();
           
       WeiboListAdapter adapter=new WeiboListAdapter(MumuWeibo.this,MumuWeiboUtility.WeiboInfoList);		
       lv.setAdapter(adapter); 
		
       if(AccessTokenKeeper.readAccessToken(MumuWeiboUtility.context).isSessionValid())
       if(MumuWeiboUtility.isFlushingWeibo==false) flushWeibo();            
      
    }//END OF ONcreate
       
    
    OnClickListener btn_listener=new OnClickListener(){

		@Override
		public void onClick(View v) {
			// 发送微博按钮
			if(v==sendWeibo){			
			
				/*
				Intent intent=new Intent();
				intent.putExtra("com.weibo.android.accesstoken", weibo.getAccessToken().getToken());
				intent.putExtra("com.weibo.android.token.secret", weibo.getAccessToken().getSecret());
				intent.setClass(MumuWeibo.this,com.weibo.net.ShareActivity.class);
				*/
				Intent intent=new Intent();
				intent.setClass(MumuWeibo.this, WriteWeibo.class);				
				startActivity(intent);				
			}
			//刷新微博
			else if(v==flushWeibo)
			{
				flushWeibo();					
			}			
		}
    };
    
    public void getMoreWeibo()
    {							
		String maxId="1";
		int length=MumuWeiboUtility.WeiboInfoList.size();
		MumuWeibo.this.lastVisibleItem=length;
		if(length>0)					
			maxId=MumuWeiboUtility.WeiboInfoList.get(length-1).getWeiboId();
						
		long max=Long.parseLong(maxId)-1;

		StatusesAPI api=new StatusesAPI(AccessTokenKeeper.readAccessToken(MumuWeibo.this));
		api.friendsTimeline(0l, max, 30, 1, false, WeiboAPI.FEATURE.ALL, false, new GetMoreWeiboListener());
	
    }
    
    class GetMoreWeiboListener implements RequestListener{

		@Override
		public void onComplete(String result) {
			// TODO Auto-generated method stub
			if(progressDialog!=null)progressDialog.dismiss();
    		
			int count;
			try {
				count = WeiboListParser.parse(result,MumuWeiboUtility.WeiboInfoList,1);
				
				 if(count==0)
					 {
					 	showToast("没有更多微博了。");
						 handler.post(new Runnable() {
							 @Override
							 public void run() {
								 lv.onLoadMoreComplete(true);
							 }
						 });

					 	
					 }
           		 else{	           
           			 showToast( "收到"+count+"条微博");
   					 
           			handler.post(new Runnable() {

           			   @Override
           			   public void run() {
           				 WeiboListAdapter adapter=new WeiboListAdapter(MumuWeibo.this,MumuWeiboUtility.WeiboInfoList);		
            				// lv.setAdapter(adapter);
            				 adapter.notifyDataSetChanged();
            				 lv.onLoadMoreComplete(false);
            				// lv.setSelection(lastVisibleItem+1); 
            				 MumuWeiboUtility.saveWeiboList(MumuWeibo.this,MumuWeiboUtility.LIST_FLAG.PUBLIC);
           			   }
           			  });
   				 	   				
           		 } 
			}
			catch(JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				lv.onLoadMoreComplete(false);
				}
			
			if(anim!=null)anim.cancel();
    		//if(anim2!=null)anim2.cancel();
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			if(progressDialog!=null)progressDialog.dismiss();
    		if(anim!=null)anim.cancel();
    		//if(anim2!=null)anim2.cancel();
			showToast(WeiboErrorHelper.WeiboError(arg0));
			handler.post(new Runnable(){
				public void run(){
					lv.onLoadMoreComplete(false);
				}
			});		
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			if(progressDialog!=null)progressDialog.dismiss();
    		if(anim!=null)anim.cancel();
    		//if(anim2!=null)anim2.cancel();
    	//	showToast(WeiboErrorHelper.WeiboError(arg0));
    		showToast("刷新微博失败");
    		handler.post(new Runnable(){
				public void run(){
					lv.onLoadMoreComplete(false);
				}
			});
		}    	
    }

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		this.visibleCount=visibleItemCount;
		this.firstVisibleItem=firstVisibleItem;		
		
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		int lastitem=MumuWeiboUtility.WeiboInfoList.size()-1;
		int lastIndex=lastitem+1;
		
		if(scrollState==OnScrollListener.SCROLL_STATE_IDLE )
		{
			Toast.makeText(MumuWeibo.this, "auto refresh", Toast.LENGTH_LONG).show();
		}
		
	}
	
	private OnItemClickListener listItemClickListener=new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			Intent i=new Intent();
			i.putExtra("position", arg2-1);
			//i.putExtra("from", "mainpage");
			i.putExtra("weibo", MumuWeiboUtility.WeiboInfoList.get(arg2-1));
			i.setClass(MumuWeibo.this, WeiboDetail.class);
			MumuWeibo.this.startActivity(i);			
		}		
	};
	
	public void flushWeibo(){
		//Log.i(TAG,"flushing weibo...");
		MumuWeiboUtility.isFlushingWeibo=true;
		
		if(MumuWeiboUtility.WeiboInfoList.size()==0){		
		progressDialog=ProgressDialog.show(MumuWeibo.this, null, "正在刷新微博，请稍等。。。");
		progressDialog.setCancelable(true);
		}
		//flushWeibo.setBackgroundResource(R.drawable.refresh_animation);
		//final AnimationDrawable ad=(AnimationDrawable)flushWeibo.getDrawable();
		
		anim = AnimationUtils.loadAnimation(this, R.anim.loading); 
		flushWeibo.setAnimation(anim);
		anim.startNow();
				
		int num=50;//一次刷新的围脖数量		
		long since=0l;
		if(MumuWeiboUtility.WeiboInfoList.size()>1)		
			{
				num+=1;
				since=MumuWeiboUtility.WeiboInfoList.get(1).getId();
			}
				
		StatusesAPI api=new StatusesAPI(AccessTokenKeeper.readAccessToken(MumuWeibo.this));
		
		
		api.friendsTimeline(since, 0l, num, 1, false, WeiboAPI.FEATURE.ALL, false, new GetNewWeiboListener());
					
	}
    
	class GetNewWeiboListener implements RequestListener{

		@Override
		public void onComplete(String newWeibo) {
			// TODO Auto-generated method stub
			int presize=MumuWeiboUtility.WeiboInfoList.size();
			try {
				final int count = WeiboListParser.parse(newWeibo,MumuWeiboUtility.WeiboInfoList,0);
				if(progressDialog!=null)progressDialog.dismiss();				
        		if(anim!=null)anim.cancel();
        	
				 if(count==0)
           			 {
					 	showToast("已经是最新微博，请稍后刷新。");
					 }
           		 else{	  
           			 	showToast("收到"+count+"条新微博");                 		
           			}
				 
				 handler.post(new Runnable() {

					   @Override
					   public void run() {						   
						   WeiboListAdapter adapter=new WeiboListAdapter(MumuWeibo.this,MumuWeiboUtility.WeiboInfoList);		
			   				 lv.setAdapter(adapter); 
			   				
			   				// lv.addView(midView, MumuWeiboUtility.WeiboInfoList.size()+count);
			   				// lv.addView(midView, 400, 30);
			   				 lv.onRefreshComplete();
			   				MumuWeiboUtility.isFlushingWeibo=false;
			   				 MumuWeiboUtility.saveWeiboList(MumuWeibo.this,MumuWeiboUtility.LIST_FLAG.PUBLIC);
					   }
					  });
			}
			catch(JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();		
				handler.post(new Runnable(){
					public void run(){
						 lv.onRefreshComplete();
						 MumuWeiboUtility.isFlushingWeibo=false;
					}
				});		
				
			//	Toast.makeText(MumuWeibo.this, "解析网络数据失败！", Toast.LENGTH_LONG).show();					
			}
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			if(progressDialog!=null)progressDialog.dismiss();
    		if(anim!=null)anim.cancel();
    	
			showToast(WeiboErrorHelper.WeiboError(arg0));
			handler.post(new Runnable(){
				public void run()
				{
					lv.onRefreshComplete();
					MumuWeiboUtility.isFlushingWeibo=false;
				}
			});
			
			
			
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			if(progressDialog!=null)progressDialog.dismiss();
    		if(anim!=null)anim.cancel();
    	//	if(anim2!=null)anim2.cancel();
			showToast("刷新微博失败");
			handler.post(new Runnable(){
				public void run(){
					 lv.onRefreshComplete();
					 MumuWeiboUtility.isFlushingWeibo=false;
				}
			});		
			
		}
		
	}
	
   	
	
	//长按listview弹出菜单
	OnItemLongClickListener lislong=new OnItemLongClickListener()
	{
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			// TODO Auto-generated method stub
			
			final WeiboInfo weiboInfo0=MumuWeiboUtility.WeiboInfoList.get(arg2-1);
				
			ShareOperation.showWeiboOperation(MumuWeibo.this, weiboInfo0);
			return true;
		}
		
	}; //end of onItemLonglistener
	
	
	//获取当前登录用户的id

	
public void setUserInfo(){
	
	if(MumuWeiboUtility.LoginUser!=null)
	{
		WeiboUserInfo user=MumuWeiboUtility.LoginUser;		
		title.setText(user.getName());

		logo.setImageURI(Uri.parse(user.getProfile()));
		return;
	}
	
	Oauth2AccessToken token=AccessTokenKeeper.readAccessToken(MumuWeiboUtility.context);
	if(!token.isSessionValid())return;
		
		AccountAPI api0=new AccountAPI(token);
		
		api0.getUid(new getLoginerListener());	
	}
	
	class getLoginerListener implements RequestListener{

		@Override
		public void onComplete(String arg0) {
			// TODO Auto-generated method stub
			Long uids=null;
			try {
				JSONObject jo=new JSONObject(arg0);
				uids=jo.getLong("uid");
			}
			catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			UsersAPI userAPI=new UsersAPI(AccessTokenKeeper.readAccessToken(MumuWeiboUtility.context));
			userAPI.show(uids, new GetLoginerInfoListener());
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	class GetLoginerInfoListener implements RequestListener{

		@Override
		public void onComplete(final String arg0) {
			// TODO Auto-generated method stub
			
			handler.post(new Runnable(){
				public void run(){
					try {
						WeiboUserInfo user=WeiboUserParser.parse(arg0);
						MumuWeiboUtility.LoginUser=user;
												
						if(user!=null){
						AsyncBitmapLoader async=new AsyncBitmapLoader();
						async.loadBitmap(logo, MumuWeiboUtility.IMAGE_TYPE.PROFILE,user.getProfile());   
						title.setText(user.getName()); 
						}
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});			
		}
		
		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}

	
	public void onResume()
	{
		super.onResume();
		Log.i(TAG,"OnResume");
		if(AccessTokenKeeper.readAccessToken(MumuWeiboUtility.context).isSessionValid())
			if (MumuWeiboUtility.isSeized) {
				MumuWeiboUtility.isSeized = false;
			} else if (!MumuWeiboUtility.isFlushingWeibo) flushWeibo();
		
		setUserInfo();
	}
	
	public void onPause()
	{
		super.onPause();
		Log.i(TAG,"OnPause");
		MumuWeiboUtility.isSeized=true;
	}
	public void onRestart()
	{
		super.onRestart();
		Log.i(TAG,"OnRestart");
	}
	public void onStop()
	{
		super.onStop();
		MumuWeiboUtility.isSeized=true;
		Log.i(TAG,"OnStop");
	}
	

}
