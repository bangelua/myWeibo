package com.mumuWeibo2;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.CommentsAPI;
import com.weibo.sdk.android.api.FavoritesAPI;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.api.WeiboAPI.AUTHOR_FILTER;
import com.weibo.sdk.android.api.WeiboAPI.FEATURE;
import com.weibo.sdk.android.api.WeiboAPI.SRC_FILTER;
import com.weibo.sdk.android.api.WeiboAPI.TYPE_FILTER;
import com.weibo.sdk.android.net.RequestListener;


//显示某个用户的最新微博列表
public class UserWeibosShow extends Activity{
	
	final String TAG="TAG----->UserWeibosShow.class";
	MyListView lv;
	TextView title;
	String userName;
	//String mid;
	long uid;
	String action=null;
	int prePos;
	int listpos;
	int page=1;
	
	boolean isMe=false;

	private ArrayList<WeiboInfo>  list;
	ProgressDialog pd=null;
		
	StatusesAPI api;
	
private Handler handler=new Handler();
	
	public void showToast(final String s)
	{
		handler.post(new Runnable() {

			   @Override
			   public void run() {
			    Toast.makeText(UserWeibosShow.this, s,
			      Toast.LENGTH_SHORT).show();
			   }
			  });
	}
		
	public void onCreate(Bundle bb){		
		super.onCreate(bb);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_weibos_layout);
		lv=(MyListView)findViewById(R.id.user_weibos_lv);
	
		title=(TextView)findViewById(R.id.tv_title_in_user_weibos);
		
	    
	      lv.setOnItemClickListener(listItemClickListener);     
	       lv.setOnItemLongClickListener(lislong);
		
	       lv.setOnRefreshListener(new MyListView.IOnRefreshListener() {
	   		
	   		@Override
	   		public void OnRefresh() {
	   			// TODO Auto-generated method stub
	   			//flushWeibo();	  
	   			if(action.equals("favor"))
	   			{
	   				//getFavorWeibos();
	   				lv.onRefreshComplete();
	   			}
	   			if(action.equals("comment"))
	   			{
	   				getComment();
	   			}
	   			else if(action.equals("@me"))
	   			{
	   				getAtMe();
	   			}
	   			else if(action.equals("weibos")){
	   				getUserWeibos();
	   				//lv.onRefreshComplete();
	   				
	   			}
	   			
	   		}
	   	});
	       
	     lv.setOnLoadMoreListener(new MyListView.IOnLoadMoreListener() {
	   		
	   		@Override
	   		public void OnLoadMore() {
	   			// TODO Auto-generated method stub
	   			//getMoreWeibo();
	   			if(action.equals("favor"))
	   			{
	   				getMoreFavors();
	   			}
	   			else if(action.equals("comment"))
	   			{
	   				//
	   				getMoreComment();
	   			}
	   			else if(action.equals("@me")){
	   				getMoreAtMe();
	   			}
	   			else if(action.equals("weibos")){
	   				getMoreWeibos();
	   			}
	   		}
	   	});
	       
	       api=new StatusesAPI(AccessTokenKeeper.readAccessToken(UserWeibosShow.this));
	       
		Intent in=getIntent();
		userName=in.getExtras().getString("screen_name");
		uid=in.getExtras().getLong("id");
		action=in.getExtras().getString("action");
		
		if(MumuWeiboUtility.LoginUser!=null && MumuWeiboUtility.LoginUser.getId()==uid)
			isMe=true;
		
		list=new ArrayList<WeiboInfo>();
		if(action!=null && action.equals("favor"))
		{
			title.setText("收藏列表");
			getFavorWeibos();
		}
		else if(action.equals("weibos")){
		title.setText("@"+userName);
		//mid=String.valueOf(id);
		getUserWeibos();
		}
		else if(action.equals("@me")){
			title.setText("@我");
			WeiboListAdapter adapter=new WeiboListAdapter(UserWeibosShow.this,MumuWeiboUtility.AtMsgList);
			lv.setAdapter(adapter);
			 getAtMe();	
			 title.setVisibility(View.GONE);
		}
		else if(action.equals("comment")){
			title.setVisibility(View.GONE);
			//title.setText("收到的评论");			
			WeiboListAdapter adapter=new WeiboListAdapter(UserWeibosShow.this,MumuWeiboUtility.CommentsList);
			lv.setAdapter(adapter);
			getComment();			
		}
	}
	


	
	private OnClickListener btn_listener=new OnClickListener()
	{
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if(action!=null && action.equals("favor"))getMoreFavors();
			else if(action.equals("weibos"))
				getMoreWeibos();
			else if(action.equals("@me"))			
				getMoreAtMe();
			else if(action.equals("comment"))
				getMoreComment();
			
		}				
	};
	
	private void getUserWeibos()
	{
				
		if(isMe){
			list=MumuWeiboUtility.MyWeibosList;
			Log.i(TAG,"MyWeiboList's num is"+list.size());
			WeiboListAdapter adapter=new WeiboListAdapter(UserWeibosShow.this,list);
			lv.setAdapter(adapter);
		}
		if(list.size()==0){
		pd=ProgressDialog.show(UserWeibosShow.this, null, "正在获取用户微博。。。");
		pd.setCancelable(true);			
		}
						
		int num=20 ;//一次刷新的围脖数量		
		long since=0l;
		int leng=list.size();
		if(leng>1)		
			{
				num+=1;
				since=list.get(1).getId();
			}		
		else if(leng==1)since=MumuWeiboUtility.AtMsgList.get(0).getId();
		
		api.userTimeline(uid, since, 0, num, 1, false, FEATURE.ALL, false, new GetUserWeibosListener());
				
	}	
	
	
	class GetUserWeibosListener implements RequestListener{

		@Override
		public void onComplete(final String arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();			
			 handler.post(new Runnable() {
				   @Override
				   public void run() {
					   try {
							int weiboCount=WeiboListParser.parse(arg0, list,0);
							Log.i(TAG,"after parsed, list's num is"+list.size());
							if(weiboCount>0)Toast.makeText(UserWeibosShow.this,"收到"+weiboCount+"条新微博", Toast.LENGTH_SHORT).show();
							WeiboListAdapter adapter=new WeiboListAdapter(UserWeibosShow.this,list);
							lv.setAdapter(adapter);
							lv.onRefreshComplete();
							if(isMe){
						//	MumuWeiboUtility.MyWeibosList=list;
								Log.i(TAG,"MyWeiboList's num is"+list.size());
								Log.i(TAG,"list's num is"+list.size());
							MumuWeiboUtility.saveWeiboList(UserWeibosShow.this, MumuWeiboUtility.LIST_FLAG.MYWEIBOS);
						} 
					   }
						catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							lv.onRefreshComplete();
						}
				   }
				  });
			
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();	
			showToast(WeiboErrorHelper.WeiboError(arg0));
			
			handler.post(new Runnable(){
				public void run(){					
					lv.onRefreshComplete();
				}
			});			
			
			}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
	
			showToast("获取用户微博失败!\r\n"+arg0.getMessage());
			handler.post(new Runnable(){
				public void run(){					
					lv.onRefreshComplete();
				}
			});		
			
		}
		
	}
	private void getMoreWeibos()
	{
		//pd=ProgressDialog.show(UserWeibosShow.this, null, "正在获取用户更多微博。。。");
		//loadMoreButton.setAnimation(anim);
		//anim.startNow();		
		prePos=list.size();
		final long maxId=list.get(prePos-1).getId();		
		api.userTimeline(uid, 0, maxId-1, 20, 1, false, FEATURE.ALL, false, new GetMoreWeibosListener());
		
	}
	
	class GetMoreWeibosListener implements RequestListener{

		@Override
		public void onComplete(final String arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();						
			 handler.post(new Runnable() {

				   @Override
				   public void run() {
					   try {				
							int weiboCount=WeiboListParser.parse(arg0, list,1);
							if(weiboCount==0)
							{
								Toast.makeText(UserWeibosShow.this,"没有更多微博了！", Toast.LENGTH_SHORT).show();
								//loadMore.setVisibility(ViewGroup.GONE);
								lv.onLoadMoreComplete(true);
								return;
							}
							Toast.makeText(UserWeibosShow.this,"收到"+weiboCount+"条微博", Toast.LENGTH_SHORT).show();
							
							WeiboListAdapter adapter=new WeiboListAdapter(UserWeibosShow.this,list);
							adapter.notifyDataSetChanged();							
			   				//lv.setSelection(prePos); 
							lv.onLoadMoreComplete(false);
							
							if(isMe){
							MumuWeiboUtility.MyWeibosList=list;
							MumuWeiboUtility.saveWeiboList(UserWeibosShow.this, MumuWeiboUtility.LIST_FLAG.MYWEIBOS);
							}
						} 
						catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							lv.onLoadMoreComplete(false);
						}
				   }
				  });
			
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
	
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
			if(pd!=null)pd.dismiss();
	
			showToast("获取用户微博失败!\r\n"+arg0.getMessage());
			handler.post(new Runnable(){
				public void run(){					
					lv.onLoadMoreComplete(false);
				}
			});		
			
		}
		
	}
	private void getFavorWeibos()
	{
		
		if(list.size()==0){
		pd=ProgressDialog.show(UserWeibosShow.this, null, "正在获取收藏列表。。。");
		pd.setCancelable(true);
		}
		
		FavoritesAPI api2=new FavoritesAPI(AccessTokenKeeper.readAccessToken(UserWeibosShow.this));
		
		api2.favorites(50, page, new getFavorWeibosListener());
		
	}	
	
	class getFavorWeibosListener implements RequestListener{

		@Override
		public void onComplete(final String arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
					
			 handler.post(new Runnable() {

				   @Override
				   public void run() {
					   try {
							page++;
							int weiboCount=WeiboListParser.parseFavor(arg0, list,0);
							Log.i("--------Favor ","has"+list.size());
							Toast.makeText(UserWeibosShow.this,"收到"+weiboCount+"微博", Toast.LENGTH_SHORT).show();
							WeiboListAdapter adapter=new WeiboListAdapter(UserWeibosShow.this,list);
							lv.setAdapter(adapter);
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
		
			showToast(WeiboErrorHelper.WeiboError(arg0));
			handler.post(new Runnable(){
				public void run(){
					lv.onRefreshComplete();
				}
			});		
			
			
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
	
			showToast( "获取收藏失败!\r\n"+arg0.getMessage());
			handler.post(new Runnable(){
				public void run(){
					lv.onRefreshComplete();
				}
			});		
			
			
		}		
	}
	
	private void getMoreFavors()
	{
		//pd=ProgressDialog.show(UserWeibosShow.this, null, "正在获取用户更多微博。。。");
	
	
		prePos=list.size();
		final String maxId=list.get(prePos-1).getWeiboId();	
		
		FavoritesAPI api2=new FavoritesAPI(AccessTokenKeeper.readAccessToken(UserWeibosShow.this));
		
		api2.favorites(50, page, new getMoreFavorWeibosListener());			
	}
	
	class getMoreFavorWeibosListener implements RequestListener{

		@Override
		public void onComplete(final String arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
						
			 handler.post(new Runnable() {

				   @Override
				   public void run() {
					   try {
							page++;
							int weiboCount=WeiboListParser.parseFavor(arg0, list,1);
							if(weiboCount==0)
							{
								Toast.makeText(UserWeibosShow.this,"没有更多微博了！", Toast.LENGTH_SHORT).show();
								//loadMore.setVisibility(ViewGroup.GONE);
								lv.onLoadMoreComplete(true);
								return;
							}
							Toast.makeText(UserWeibosShow.this,"收到"+weiboCount+"条收藏", Toast.LENGTH_SHORT).show();
							
							WeiboListAdapter adapter=new WeiboListAdapter(UserWeibosShow.this,list);
							adapter.notifyDataSetChanged();					
			   				//lv.setSelection(prePos); 
			   				lv.onLoadMoreComplete(false);
							
						} 
						catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							lv.onLoadMoreComplete(false);
						}
				   }
				  });
			// if(anim!=null)anim.cancel();
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
	
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
			if(pd!=null)pd.dismiss();
	
			showToast( "获取收藏失败!\r\n"+arg0.getMessage());
			handler.post(new Runnable(){
				public void run(){
					lv.onLoadMoreComplete(false);
				}
			});		
			
		}
		
	}
	
	private void getAtMe(){
		if(MumuWeiboUtility.AtMsgList.size()==0){
		pd=ProgressDialog.show(getParent(), null, "正在获取@列表。。。");
		pd.setCancelable(true);
		}
		
		int num=20 ;//一次刷新的围脖数量		
		long since=0l;
		int leng=MumuWeiboUtility.AtMsgList.size();
		if(leng>1)		
			{
				num+=1;
				since=MumuWeiboUtility.AtMsgList.get(1).getId();
			}		
		else if(leng==1)since=MumuWeiboUtility.AtMsgList.get(0).getId();
			
		api.mentions(since, 0, num, 1, AUTHOR_FILTER.ALL, SRC_FILTER.ALL, TYPE_FILTER.ALL, false, new GetAtListener());
			
	}
	
	class GetAtListener implements RequestListener{

		@Override
		public void onComplete(final String arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
					
			 handler.post(new Runnable() {

				   @Override
				   public void run() {
					   try {
							page++;
							int weiboCount=WeiboListParser.parse(arg0, MumuWeiboUtility.AtMsgList,0);
							
							if(weiboCount!=0)Toast.makeText(UserWeibosShow.this,"收到"+weiboCount+"条@微博", Toast.LENGTH_SHORT).show();
							WeiboListAdapter adapter=new WeiboListAdapter(UserWeibosShow.this,MumuWeiboUtility.AtMsgList);
							lv.setAdapter(adapter);
							lv.onRefreshComplete();
							MumuWeiboUtility.saveWeiboList(UserWeibosShow.this, MumuWeiboUtility.LIST_FLAG.ATMSG);
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
	
			showToast(WeiboErrorHelper.WeiboError(arg0));
			handler.post(new Runnable(){
				public void run(){
					lv.onRefreshComplete();
				}
			});		
			
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
		
			showToast("获取@失败!\r\n"+arg0.getMessage());
			handler.post(new Runnable(){
				public void run(){
					lv.onRefreshComplete();
				}
			});		
						
		}
		
	}
	
	private void getMoreAtMe(){
			
		prePos=MumuWeiboUtility.AtMsgList.size();
		long maxId= Long.MAX_VALUE;
		if (prePos > 0) {
			maxId = MumuWeiboUtility.AtMsgList.get(prePos - 1).getId() - 1;
		}
		
		api.mentions(0, maxId, 20, 1, AUTHOR_FILTER.ALL, SRC_FILTER.ALL, TYPE_FILTER.ALL, false, new GetMoreAtListener());
				
	}
	
	class GetMoreAtListener implements RequestListener{

		@Override
		public void onComplete(final String arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			
			 handler.post(new Runnable() {

				   @Override
				   public void run() {

						try {
							page++;
							int weiboCount=WeiboListParser.parse(arg0, MumuWeiboUtility.AtMsgList,1);
							if(weiboCount==0)
							{
								Toast.makeText(UserWeibosShow.this,"没有更多微博了！", Toast.LENGTH_SHORT).show();
								lv.onLoadMoreComplete(true);
								return;
							}
							Toast.makeText(UserWeibosShow.this,"收到"+weiboCount+"条微博", Toast.LENGTH_SHORT).show();
							
							WeiboListAdapter adapter=new WeiboListAdapter(UserWeibosShow.this,MumuWeiboUtility.AtMsgList);
							adapter.notifyDataSetChanged();					
			   			//	lv.setSelection(prePos); 
			   				lv.onLoadMoreComplete(false);
			   				MumuWeiboUtility.saveWeiboList(UserWeibosShow.this, MumuWeiboUtility.LIST_FLAG.ATMSG);
			   				
							
						} 
						catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							lv.onLoadMoreComplete(false);
						}
				   }
				  });
			
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
	
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
			if(pd!=null)pd.dismiss();
		
			showToast("获取@失败!\r\n"+arg0.getMessage());
			handler.post(new Runnable(){
				public void run(){
					lv.onLoadMoreComplete(false);
				}
			});		
			
		}
		
	}
	private void getComment(){
		
		//先显示保存过的评论列表		
		
		if(MumuWeiboUtility.CommentsList.size()==0){
		pd=ProgressDialog.show(getParent(), null, "正在获取收到的评论。。。");
		pd.setCancelable(true);
		}
		int num=20;//一次刷新的围脖数量		
		long since=0l;
		if(MumuWeiboUtility.CommentsList.size()>1)		
			{
				num+=1;
				since=MumuWeiboUtility.CommentsList.get(1).getId();
			}		
		
		
		CommentsAPI api3=new CommentsAPI(AccessTokenKeeper.readAccessToken(UserWeibosShow.this));
		api3.toME(since, 0, num, 1, AUTHOR_FILTER.ALL, SRC_FILTER.ALL, new GetCommentsListener());
				
	}
	
	class GetCommentsListener implements RequestListener{

		@Override
		public void onComplete(final String arg0) {
			// TODO Auto-generated method stub
						
			if(pd!=null)pd.dismiss();
					
			 handler.post(new Runnable() {

				   @Override
				   public void run() {					  
						int weiboCount=ReceviedCommentParser.parse(arg0,0);				
						if(weiboCount!=0)Toast.makeText(UserWeibosShow.this,"收到"+weiboCount+"新的评论", Toast.LENGTH_SHORT).show();
						 WeiboListAdapter adapter=new WeiboListAdapter(UserWeibosShow.this,MumuWeiboUtility.CommentsList);
						 lv.setAdapter(adapter);
						 lv.onRefreshComplete();
						MumuWeiboUtility.saveWeiboList(UserWeibosShow.this,MumuWeiboUtility.LIST_FLAG.COMMENTS);
						
				   }
				  });
			
		
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
		
			showToast(WeiboErrorHelper.WeiboError(arg0));
			handler.post(new Runnable(){
				public void run(){
					lv.onRefreshComplete();
				}
			});		
			
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
		
			showToast("获取评论失败!\r\n"+arg0.getMessage());
			handler.post(new Runnable(){
				public void run(){
					lv.onRefreshComplete();
				}
			});		
			
			
		}
		
	}
	
	private void getMoreComment(){
		
	
		prePos=MumuWeiboUtility.CommentsList.size();
		final String maxId=MumuWeiboUtility.CommentsList.get(prePos-1).getWeiboId();		
		
		CommentsAPI api3=new CommentsAPI(AccessTokenKeeper.readAccessToken(UserWeibosShow.this));
		api3.toME(0, Long.parseLong(maxId)-1, 20, 1, AUTHOR_FILTER.ALL, SRC_FILTER.ALL, new GetMoreCommentsListener());
		
	}
	
	class GetMoreCommentsListener implements RequestListener{

		@Override
		public void onComplete(final String arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			
			
			 handler.post(new Runnable() {

				   @Override
				   public void run() {
					   int weiboCount=ReceviedCommentParser.parse(arg0,1);
						if(weiboCount==0)
						{
							Toast.makeText(UserWeibosShow.this,"没有更多评论了！", Toast.LENGTH_SHORT).show();
							//loadMore.setVisibility(ViewGroup.GONE);
							lv.onLoadMoreComplete(true);
							return;
						}
						Toast.makeText(UserWeibosShow.this,"收到"+weiboCount+"条评论", Toast.LENGTH_SHORT).show();
						
						WeiboListAdapter adapter=new WeiboListAdapter(UserWeibosShow.this,MumuWeiboUtility.CommentsList);
						adapter.notifyDataSetChanged();				
						//lv.setSelection(prePos);
						lv.onLoadMoreComplete(false);

				   }
				  });
		
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
		
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
			if(pd!=null)pd.dismiss();
		
			showToast("获取评论失败!\r\n"+arg0.getMessage());
			handler.post(new Runnable(){
				public void run(){
					lv.onLoadMoreComplete(false);
				}
			});		
			
		}		
	}
	
	private OnItemClickListener listItemClickListener=new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			int pos=arg2-1;					
			
			if(action.equals("comment")){
				if(pos>=MumuWeiboUtility.CommentsList.size())return;
				  String[] mList={"回复Ta","查看用户信息","查看原微博"};				 
					final WeiboInfo	weiboInfo=MumuWeiboUtility.CommentsList.get(pos);
				  AlertDialog.Builder listDia=new AlertDialog.Builder(getParent());
				  listDia.setTitle(null);
				  listDia.setItems(mList, new DialogInterface.OnClickListener() {
				          
				            @Override
				            public void onClick(DialogInterface dialog, int which) {
				                // TODO Auto-generated method stub
				                /*下标是从0开始的*/	            	
				               switch(which){	 	               
				               case 0:  //reply				            	 
				            	    Intent ins=new Intent();				
				   					ins.setClass(UserWeibosShow.this,CommentWeibo.class);				   					
				   					ins.putExtra("action","reply");		   					
				   					ins.putExtra("weibo", weiboInfo);
				   					startActivity(ins);					   					
				   					break;
				   				
				               case 1://get user info
				            	   Intent i=new Intent();
				            	   i.setClass(UserWeibosShow.this, UserInfoShow.class);
				            	   i.putExtra("screen_name", weiboInfo.getWeiboUser().getName());
				            	   startActivity(i);
				            	   
				            	   break;
				               case 2:////check the source weibo
				            	    Intent ins2=new Intent();				
				   					ins2.setClass(UserWeibosShow.this,WeiboDetail.class);
				   					
				   					ins2.putExtra("weibo", weiboInfo.getRetweetWeiboInfo()); 
				   					startActivity(ins2);	
				   					break;
				   					
				                   	   
				              default:
							  // 	Toast.makeText(CommentListShow.this, "操作尚未设置", Toast.LENGTH_SHORT).show();				    		
							  }
				            }
				        });
				       listDia.create().show();	
				       // return;
				
			}
			else{	
			
			Intent i=new Intent();	
			if(action.equals("@me")){
				i.putExtra("weibo",MumuWeiboUtility.AtMsgList.get(pos));
			}
			else
				i.putExtra("weibo",list.get(pos));
			i.setClass(UserWeibosShow.this, WeiboDetail.class);
			UserWeibosShow.this.startActivity(i);		
			}
		}
		
	};
	
	OnItemLongClickListener lislong=new OnItemLongClickListener()
	{
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			// TODO Auto-generated method stub
			int position=arg2-1;
			Context context=UserWeibosShow.this;
			WeiboInfo weiboInfo0=new WeiboInfo();
			
			if(action.equals("favor"))
   			{
   				//getFavorWeibos();
				 weiboInfo0=list.get(position);
   			}
   			if(action.equals("comment"))
   			{
   				return true;
   			// weiboInfo0=MumuWeiboUtility.CommentsList.get(position);
   			}
   			else if(action.equals("@me"))
   			{
   				context=getParent();
   			 weiboInfo0=MumuWeiboUtility.AtMsgList.get(position);
   			}
   			else if(action.equals("weibos")){
   			 weiboInfo0=list.get(position);   				
   			}
   			
			ShareOperation.showWeiboOperation(context, weiboInfo0);
			return true;
		}
		
	}; //end of onItemLonglistener
	
	
	
}
