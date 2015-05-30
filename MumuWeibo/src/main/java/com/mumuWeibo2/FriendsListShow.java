package com.mumuWeibo2;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.FriendshipsAPI;
import com.weibo.sdk.android.net.RequestListener;

public class FriendsListShow extends ListActivity{
	
	private TextView tvTitle;
	String uid;
	ProgressDialog pd=null;
	ArrayList<WeiboUserInfo> list;
	int nextCursor;
	Animation anim;
	private View loadMore;
	ImageButton loadMoreButton;
	String request; //记录激活该activitiy的activity的请求是查看关注，还是粉丝
	String username;
	FriendshipsAPI api=new FriendshipsAPI(AccessTokenKeeper.readAccessToken(MumuWeiboUtility.context));
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
	public void onCreate(Bundle bb)
	{
		super.onCreate(bb);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.friends_list_layout);
		tvTitle=(TextView)findViewById(R.id.tv_title_text_in_friends_list);
		 anim = AnimationUtils.loadAnimation(this, R.anim.loading); 
		 loadMore=getLayoutInflater().inflate(R.layout.load_more, null);	       
	     loadMoreButton=(ImageButton)loadMore.findViewById(R.id.bt_loading_more);
	     
	       loadMore.setOnClickListener(btn_listener);       
	       getListView().addFooterView(loadMore);
				
		list=new ArrayList<WeiboUserInfo>();
		//getData
		Intent in=getIntent();
		
		uid=in.getExtras().getString("uid");	
		 username=in.getExtras().getString("screen_name");
		request=in.getExtras().getString("request");
		if(request.equals("following"))
			{
				tvTitle.setText("@"+username+"的关注");
				getFriendsList();					
			}
		else if(request.equals("follower"))
			{
			tvTitle.setText("@"+username+"的粉丝");
				getFollowerList();
			}
	}
	
	private void getFollowerList(){
		pd=ProgressDialog.show(FriendsListShow.this, null, "正在获取用户的粉丝列表");
		pd.setCancelable(true);
		
		api.followers(username, 50, 0, false, new GetFollowerListener());		
	}
	
	class GetFollowerListener implements RequestListener{

		@Override
		public void onComplete(final String arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			
			 handler.post(new Runnable() {

				   @Override
				   public void run() {
					   try {
							nextCursor=FriendsListParser.parse(arg0,list);
							//Toast.makeText(FriendsListShow.this, "收到"+count+"名关注用户信息", Toast.LENGTH_SHORT).show();
							FriendsListAdapter adapter=new FriendsListAdapter(FriendsListShow.this,list);
							
							setListAdapter(adapter);
						} 
						catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				   }
				  });
			 
			
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			showToast("获取关注失败！\n"+WeiboErrorHelper.WeiboError(arg0));
			finish();
		
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			Toast.makeText(FriendsListShow.this, "获取用户粉丝信息失败!", Toast.LENGTH_SHORT).show();
			finish();
		}
		
	}
	
	
	private void getFriendsList(){
		pd=ProgressDialog.show(FriendsListShow.this, null, "正在获取用户的关注列表");
		pd.setCancelable(true);
		Log.i("```~~~~~~~~~~~~~~~~~","I am Here");
		api.friends(username, 50, 0, true, new GetFriendsListener());
				
	}
	
	class GetFriendsListener implements RequestListener{

		@Override
		public void onComplete(final String arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			
			 handler.post(new Runnable() {

				   @Override
				   public void run() {
					   try {
							nextCursor=FriendsListParser.parse(arg0,list);
							//Toast.makeText(FriendsListShow.this, "收到"+count+"名关注用户信息", Toast.LENGTH_SHORT).show();
							FriendsListAdapter adapter=new FriendsListAdapter(FriendsListShow.this,list);
							if(pd!=null)pd.dismiss();
							setListAdapter(adapter);
						} 
						catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				   }
				  });
			
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			showToast("获取关注列表失败！\n"+WeiboErrorHelper.WeiboError(arg0));
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			showToast( "获取粉丝失败!\r\n"+arg0.getMessage());
		}		
	}
	
	
	private OnClickListener btn_listener=new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v==loadMore){
				loadMoreButton.setAnimation(anim);
				anim.startNow();
				if(nextCursor==0)
					{
					if(pd!=null)pd.dismiss();
					Toast.makeText(FriendsListShow.this, "没有更多用户信息了！", Toast.LENGTH_SHORT).show();
					loadMore.setVisibility(ViewGroup.GONE);
					return;
					}
				
				if(request.equals("following"))
					api.friends(username, 50, nextCursor, false, new GetMoreFriendsListener());
				else if(request.equals("follower"))
				{
					api.followers(username, 50, nextCursor, false, new GetMoreFollowersListener());
				}
				
				
							
			}
		}
		
	};
	
	class GetMoreFriendsListener implements RequestListener{

		@Override
		public void onComplete(final String arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			
			 handler.post(new Runnable() {

				   @Override
				   public void run() {
					   try {
							int preCursor=list.size();
							nextCursor=FriendsListParser.parse(arg0,list);
							//Toast.makeText(FriendsListShow.this, "收到"+count+"名关注用户信息", Toast.LENGTH_SHORT).show();
							FriendsListAdapter adapter=new FriendsListAdapter(FriendsListShow.this,list);
							adapter.notifyDataSetChanged();
							getListView().setSelection(preCursor);
							//setListAdapter(adapter);
						} 
						catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				   }
				  });
			 if(anim!=null)anim.cancel();
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			if(anim!=null)anim.cancel();
			showToast(WeiboErrorHelper.WeiboError(arg0));	}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			if(anim!=null)anim.cancel();
			showToast("获取关注失败!\r\n"+arg0.getMessage());
			
		}
		
	}
	
	class GetMoreFollowersListener implements RequestListener{

		@Override
		public void onComplete(final String arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			
			 handler.post(new Runnable() {

				   @Override
				   public void run() {
					   try {
							int preCursor=list.size();
							nextCursor=FriendsListParser.parse(arg0,list);
							//Toast.makeText(FriendsListShow.this, "收到"+count+"名关注用户信息", Toast.LENGTH_SHORT).show();
							FriendsListAdapter adapter=new FriendsListAdapter(FriendsListShow.this,list);
							adapter.notifyDataSetChanged();
							getListView().setSelection(preCursor);
							//setListAdapter(adapter);
						} 
						catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				   }
				  });
			 if(anim!=null)anim.cancel();
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			if(anim!=null)anim.cancel();
			showToast(WeiboErrorHelper.WeiboError(arg0));
			finish();
			
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			if(anim!=null)anim.cancel();
			showToast("获取粉丝失败!\r\n"+arg0.getMessage());
			finish();
				
		}
		
	}
	
	public void onListItemClick(ListView arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		
		Intent in=new Intent();
		in.setClass(FriendsListShow.this, UserInfoShow.class);
		in.putExtra("screen_name", list.get(arg2).getName());		
		startActivity(in);
		
	}
}
