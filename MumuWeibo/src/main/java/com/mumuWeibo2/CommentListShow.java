package com.mumuWeibo2;

import java.io.IOException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.CommentsAPI;
import com.weibo.sdk.android.api.WeiboAPI.AUTHOR_FILTER;
import com.weibo.sdk.android.net.RequestListener;

public class CommentListShow extends ListActivity{	
	
	ProgressDialog pd=null;
	String weiboId;
	String weiboUserName;
	private View loadMore;
	ImageButton loadMoreButton;
	Animation anim;
	ArrayList<WeiboInfo> list;
	
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
		setContentView(R.layout.comment_list_layout);		
		//lv=(ListView)findViewById(R.id.lv_comment_list);
		  anim = AnimationUtils.loadAnimation(this, R.anim.loading);
		  loadMore=getLayoutInflater().inflate(R.layout.load_more, null);	       
	      loadMoreButton=(ImageButton)loadMore.findViewById(R.id.bt_loading_more);
	      loadMore.setOnClickListener(lis);	      
	      
	      getListView().addFooterView(loadMore);
	      list=new ArrayList<WeiboInfo>();
	       
	      Intent in=getIntent();
		 weiboId=in.getExtras().getString("id");
		 weiboUserName=in.getExtras().getString("weibo_author");
		
	
		pd=ProgressDialog.show(CommentListShow.this, null, "正在加载评论。。。");
		pd.setCancelable(true);
		
		CommentsAPI api=new CommentsAPI(AccessTokenKeeper.readAccessToken(MumuWeiboUtility.context));
		api.show(Long.parseLong(weiboId), 0, 0, 20, 1, AUTHOR_FILTER.ALL, new GetNewCommentsListener());
		
				
	}
	
	class GetNewCommentsListener implements RequestListener{

		@Override
		public void onComplete(final String arg0) {
			// TODO Auto-generated method stub
			
			handler.post(new Runnable() {

				   @Override
				   public void run() {
					   if(anim!=null)anim.cancel();
						CommentParser.parse(arg0,list,0);						
						CommentListAdapter adapter=new CommentListAdapter(CommentListShow.this,list,weiboUserName);
						if(pd!=null)pd.dismiss();
						setListAdapter(adapter);	
				   }
				  });
			
						
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			showToast(WeiboErrorHelper.WeiboError(arg0));
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			Toast.makeText(CommentListShow.this, "获取评论列表失败!", Toast.LENGTH_SHORT).show();
			
		}
		
	}
	
	public void onListItemClick(ListView arg0, View arg1, int arg2,
			long arg3) {
		 
			  String[] mList={"回复","查看用户信息"};
			 
			 
			final WeiboInfo	weiboInfo=list.get(arg2);
			 
		        AlertDialog.Builder listDia=new AlertDialog.Builder(CommentListShow.this);
		        listDia.setTitle(null);
		        listDia.setItems(mList, new DialogInterface.OnClickListener() {
		            
		            @Override
		            public void onClick(DialogInterface dialog, int which) {
		                // TODO Auto-generated method stub
		                /*下标是从0开始的*/	            	
		               switch(which){	 	               
		               case 0:  //comment
		            	 	//Toast.makeText(CommentListShow.this, "操作尚未设置", Toast.LENGTH_SHORT).show();	
		            	    Intent ins=new Intent();				
		   					ins.setClass(CommentListShow.this,CommentWeibo.class);
		   					
		   					ins.putExtra("action","reply");		   					
		   					ins.putExtra("weibo", weiboInfo);
		   					CommentListShow.this.startActivity(ins);	
		   					
		   					break;
		   				
		               case 1://repost
		            	    Intent ins2=new Intent();				
		   					ins2.setClass(CommentListShow.this,UserInfoShow.class);
		   					
		   					ins2.putExtra("screen_name", weiboInfo.getWeiboUser().getName()); 
		   					CommentListShow.this.startActivity(ins2);	
		   					break;
		   					
		                   	   
		              default:
					   	Toast.makeText(CommentListShow.this, "操作尚未设置", Toast.LENGTH_SHORT).show();				    		
					  }
		            }
		        });
		        listDia.create().show();	
	}
	
	private OnClickListener lis=new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v==loadMore)
			{
				loadMoreButton.setAnimation(anim);
				anim.startNow();
				
				CommentsAPI api=new CommentsAPI(AccessTokenKeeper.readAccessToken(MumuWeiboUtility.context));
				long max_id=Long.parseLong(list.get(list.size()-1).getWeiboId())-1;
				api.show(Long.parseLong(weiboId), 0, max_id, 20, 1, AUTHOR_FILTER.ALL, new GetMoreCommentsListener());
							
			}
		}
		
	};
	
	class GetMoreCommentsListener implements RequestListener{

		@Override
		public void onComplete(String arg0) {
			// TODO Auto-generated method stub
			if(anim!=null)anim.cancel();							
			final int positon=list.size();
			int count=CommentParser.parse(arg0,list,1);		
			if(count==0){
				
				  handler.post(new Runnable() {

					   @Override
					   public void run() {
						   loadMore.setVisibility(ViewGroup.GONE);
						   Toast.makeText(getApplicationContext(), "没有更多评论了", Toast.LENGTH_SHORT).show();
					  }
					  });		
				
				return;
			}
			
			 handler.post(new Runnable() {

				   @Override
				   public void run() {
					   CommentListAdapter adapter2=new CommentListAdapter(CommentListShow.this,list,weiboUserName);
						if(pd!=null)pd.dismiss();
						//setListAdapter(adapter2);	
						adapter2.notifyDataSetChanged();
						getListView().setSelection(positon); }
				  });		
			 
					
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			showToast(WeiboErrorHelper.WeiboError(arg0));
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
