package com.mumuWeibo2;

import java.io.IOException;

import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.FriendshipsAPI;
import com.weibo.sdk.android.api.UsersAPI;
import com.weibo.sdk.android.net.RequestListener;

//显示用户信息
public class UserInfoShow extends Activity{
	
	private final String TAG="TAG----------->UserInfoShow.class";
	
	AsyncBitmapLoader async=new AsyncBitmapLoader();
	TextView userNameTitle;
	ImageView userProfile;
	TextView userName;
	TextView loc;
	TextView des;
	TextView follow;
	TextView fans;
	TextView weibos;
	TextView favors;
	TextView createTime;
	Button followButton;
	ProgressDialog pd=null;
	String screen_name;
	WeiboUserInfo user;
	OneWeiboView lastWeibo;
	
	
	RelativeLayout weiboLayout;
	RelativeLayout friendsLayout;
	RelativeLayout followerLayout;
	RelativeLayout favorLayout;
	LinearLayout lastWeiboLayout;
	
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
	
	public void onCreate(Bundle bb){
		super.onCreate(bb);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weibo_user_info);
		
		Log.i(TAG,"onCreate");
				
		//get view
		 userNameTitle=(TextView)findViewById(R.id.tv_user_name_in_title);
		 userProfile=(ImageView)findViewById(R.id.user_profile_info);
		 userName=(TextView)findViewById(R.id.user_name_info);
		 loc=(TextView)findViewById(R.id.user_gender_location_info);
		 des=(TextView)findViewById(R.id.tv_description);
		
		 follow=(TextView)findViewById(R.id.following_count);
		 fans=(TextView)findViewById(R.id.followed_count);
		 weibos=(TextView)findViewById(R.id.weibos_count);
		 favors=(TextView)findViewById(R.id.favors_count);
		 followButton=(Button)findViewById(R.id.follow_status);
		 followButton.setOnClickListener(lis);
		 createTime=(TextView)findViewById(R.id.create_time_in_userinfo);
		 
		 weiboLayout=(RelativeLayout)findViewById(R.id.user_weibos_in_usershow);
		 weiboLayout.setOnClickListener(lis);
		 friendsLayout=(RelativeLayout)findViewById(R.id.friends_layout);
		 friendsLayout.setOnClickListener(lis);
		 followerLayout=(RelativeLayout)findViewById(R.id.followerLayout);
		 followerLayout.setOnClickListener(lis);
		 favorLayout=(RelativeLayout)findViewById(R.id.user_favors_in_usershow);
		 favorLayout.setOnClickListener(lis);
		 lastWeibo=(OneWeiboView)findViewById(R.id.lastest_weibo_in_userinfo);
		
		 lastWeiboLayout=(LinearLayout)findViewById(R.id.last_weibo_layout);
		 
		 lastWeiboLayout.setVisibility(ViewGroup.INVISIBLE); //待补待去除该行
		//get params
		 
		user=new WeiboUserInfo();
		Intent in=getIntent();			
		user=(WeiboUserInfo)in.getExtras().getSerializable("userinfo");
		
		screen_name=in.getExtras().getString("screen_name");
		if(screen_name!=null)
		{
			user=MumuWeiboUtility.userInfoCache.get(screen_name);
		}		
		if(user!=null){
			screen_name=user.getName();
			setView(user);		
		}
		getUserInfoByNet();		
		
	}
	
	private void setView(WeiboUserInfo user)
	{
		String username=user.getName();		
		String s=username;
		//if(username.getBytes().length>21)
		//	s=username.substring(0, 6)+"...";
		
		
		if(MumuWeiboUtility.LoginUser.getName().equals(user.getName()))
		{
			favorLayout.setVisibility(ViewGroup.VISIBLE);	
			followButton.setVisibility(View.INVISIBLE);	
			
		}
			
		//待补
		//if(username=当前登录用户名)转移到个人用户信息activity		
		//set
		//userNameTitle.setText(username);		
		async.loadBitmap(userProfile, MumuWeiboUtility.IMAGE_TYPE.PROFILE,user.getProfile());
		userName.setText(s);
		String gender="保密";
		if(user.getGender().equals("f"))
			gender="女";
		if(user.getGender().equals("m"))
			gender="男";
		loc.setText(gender+", "+user.getLocation());			
		
		String desc=user.getDescription();
		if(!desc.equals(""))		
			{
				des.setText(desc);
				des.setTextColor(Color.BLACK);
			}
		
		follow.setText(String.valueOf(user.getFriendsCount()));
		fans.setText(String.valueOf(user.getFollowerCount()));
		weibos.setText(String.valueOf(user.getWeiboCount()));
		favors.setText(String.valueOf(user.getFavorCount()));
		
	
		if(user.isFollowing())
			followButton.setText("取消关注");
		else
			followButton.setText("关注Ta");
						
		WeiboInfo weibo=user.getLastWeibo();
		
		
		if(weibo==null )
		{
			lastWeiboLayout.setVisibility(ViewGroup.INVISIBLE);
			
		}
		else
		{
			lastWeiboLayout.setVisibility(ViewGroup.VISIBLE);
			 createTime.setText(MumuWeiboUtility.parseWeiboTime(weibo.getCreateTime()));
				
			 lastWeibo.setView(user.getLastWeibo());	
		}
		
	
		
		
	}//end setView
	
	private OnClickListener lis=new OnClickListener()
	{
		public void onClick(View v)
		{
			if(user==null)
			{
				Toast.makeText(getApplicationContext(), "用户为null", Toast.LENGTH_LONG).show();
				return;
			}
			if(v==followButton && MumuWeiboUtility.LoginUser!=null && user.getName().equals(MumuWeiboUtility.LoginUser.getName()))
			{
				
				Toast.makeText(UserInfoShow.this, "操作尚未设置", Toast.LENGTH_SHORT).show();
				
			}
			else if(v==followButton && user!=null)
				changeFocus();
			else if(v==weiboLayout)
			{
				Intent i=new Intent();
				i.setClass(UserInfoShow.this, UserWeibosShow.class);
				i.putExtra("screen_name", screen_name);
				i.putExtra("action", "weibos");
				i.putExtra("id",user.getId());
				
				UserInfoShow.this.startActivity(i);				
			}
			else if(v==friendsLayout)
			{
				Intent i=new Intent();
				i.setClass(UserInfoShow.this, FriendsListShow.class);
				i.putExtra("screen_name", screen_name);
				i.putExtra("uid",String.valueOf(user.getId()));
				i.putExtra("request", "following");				
				UserInfoShow.this.startActivity(i);	
			}
			else if(v==followerLayout)
			{
				Intent i=new Intent();
				i.setClass(UserInfoShow.this, FriendsListShow.class);
				i.putExtra("screen_name", screen_name);
				i.putExtra("uid",String.valueOf(user.getId()));
				i.putExtra("request", "follower");				
				UserInfoShow.this.startActivity(i);	
			}
			else if(v==favorLayout){
				Intent i=new Intent();
				i.setClass(UserInfoShow.this, UserWeibosShow.class);
				i.putExtra("action", "favor");			
				UserInfoShow.this.startActivity(i);		
			}
		}
	};
	
	public void changeFocus()
	{
		FriendshipsAPI api=new FriendshipsAPI(AccessTokenKeeper.readAccessToken(UserInfoShow.this));
				
		if(user.isFollowing())
		{
			//取消关注...
			//pd=ProgressDialog.show(UserInfoShow.this, null, "正在取消关注...");
			//pd.setCancelable(true);
			Toast.makeText(getApplicationContext(), "正在取消关注", Toast.LENGTH_LONG).show();
			
			
			api.destroy(user.getName(), new CancelFocusListener());
						
		}
		else
		{
			//关注
			//pd=ProgressDialog.show(UserInfoShow.this, null, "正在请求关注...");
			//pd.setCancelable(true);
			Toast.makeText(getApplicationContext(), "正在请求关注", Toast.LENGTH_LONG).show();
					
			api.create(user.getName(), new FocusListener());				
		}
	}
	
	class FocusListener implements RequestListener{

		@Override
		public void onComplete(String arg0) {
			// TODO Auto-generated method stub
			if(pd!=null){ pd.dismiss(); pd=null; }
			
			showToast( "已关注@"+user.getName());
			
			 handler.post(new Runnable() {

				   @Override
				   public void run() {
					   followButton.setText("取消关注");
						user.setIsFollowing(true);
						MumuWeiboUtility.userInfoCache.put(user.getName(), user);

				   }
				  });
			
		}

		@Override
		public void onError(WeiboException e) {
			// TODO Auto-generated method stub
			if(pd!=null){ pd.dismiss(); pd=null; }
			showToast(WeiboErrorHelper.WeiboError(e));
		}

		@Override
		public void onIOException(IOException e) {
			// TODO Auto-generated method stub
			if(pd!=null){ pd.dismiss(); pd=null; }
			showToast("关注失败!\n"+e.getMessage());
			
		}
		
	}
	
	class CancelFocusListener implements RequestListener{

		@Override
		public void onComplete(String arg0) {
			// TODO Auto-generated method stub
			if(pd!=null){ pd.dismiss(); pd=null; }
			showToast("已取消关注@"+user.getName());
			
			 handler.post(new Runnable() {

				   @Override
				   public void run() {
					   followButton.setText("关注Ta");
						user.setIsFollowing(false);
						MumuWeiboUtility.userInfoCache.put(user.getName(), user);

				   }
				  });
			
		}

		@Override
		public void onError(WeiboException e) {
			// TODO Auto-generated method stub
			if(pd!=null){ pd.dismiss(); pd=null; }
			showToast(WeiboErrorHelper.WeiboError(e));
		}

		@Override
		public void onIOException(IOException e) {
			// TODO Auto-generated method stub
			if(pd!=null){ pd.dismiss(); pd=null; }
			showToast( "取消关注失败!\n"+e.getMessage());
			
		}		
	}
	
	public 	void getUserInfoByNet()
	{
		if(user==null)
		{
			pd=ProgressDialog.show(UserInfoShow.this, null, "正在获取用户信息...");
			pd.setCancelable(true);
		}		
		
		UsersAPI api=new UsersAPI(AccessTokenKeeper.readAccessToken(UserInfoShow.this));
		api.show(screen_name, new GetUserInfoRequest());
		
	}
	
	class GetUserInfoRequest implements RequestListener{

		@Override
		public void onComplete(final String arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)
				{
					pd.dismiss(); pd=null;
				}
			 handler.post(new Runnable() {

				   @Override
				   public void run() {
					   try {
							user=WeiboUserParser.parse(arg0);
							MumuWeiboUtility.userInfoCache.put(screen_name, user);
							if(user.getName().equals(MumuWeiboUtility.LoginUser.getName()))
								{
								MumuWeiboUtility.LoginUser=user;
									MumuWeiboUtility.saveUserInfo(getApplicationContext());
								}
							setView(user);
						} 
						catch (JSONException e) 
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
							Toast.makeText(getApplicationContext(), "解析用户信息失败", Toast.LENGTH_LONG).show();
						}
				   }
				  });
			
		}

		@Override
		public void onError(WeiboException e) {
			// TODO Auto-generated method stub
			if(pd!=null)
			{
				pd.dismiss(); pd=null;
			}
			//showToast(WeiboErrorHelper.WeiboError(e));
			//finish();
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)
			{
				pd.dismiss(); pd=null;
			}
			
			//showToast("获取用户信息失败");
			//finish();
			
		}
		
		
	}
	
	public void Resume()
	{
		super.onResume();
		Log.i(TAG,"onResume");
	}
}
