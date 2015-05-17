package com.mumuWeibo2;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.FavoritesAPI;
import com.weibo.sdk.android.net.RequestListener;



//自上而下浏览微博
public class WeiboDetail extends Activity {
	
	private ArrayList<MsgItem> msgList;
	LinearLayout holder;	
	
	boolean isRetweetWeibo=false; //标记该微博是否属于"转发微博"	
	
	private ImageView favorate;
	private ImageView repost;
	private ImageView comment;
	private ImageView delete;
	
	private String mid="";
	private boolean isFavor=false;
	int pos=-1;
	String from;
	WeiboInfo weiboInfo;
	ProgressDialog pd=null;
	
	TextView tvTitle;
	Button listComment;
	
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
		setContentView(R.layout.weibo_detail);		
				
		MumuWeiboUtility.isSeized=true;
		
		Intent intent=getIntent();
		
		pos=intent.getExtras().getInt("position",-1);
		
		weiboInfo=(WeiboInfo) intent.getExtras().getSerializable("weibo");
		if(weiboInfo==null)return;
		mid=weiboInfo.getWeiboId();
		
		if(weiboInfo.getWeiboUser()!=null)
			Log.i("!!!!!!!!!!!!!!!!!!!!",""	);
		
		if(weiboInfo.getRetweetWeiboInfo()==null)
			isRetweetWeibo=false;
		else
			isRetweetWeibo=true;			
				
		//Get View
		SourceWeiboView weiboView=new SourceWeiboView(this);
		weiboView=(SourceWeiboView)findViewById(R.id.source_weibo_view);
		holder=(LinearLayout)findViewById(R.id.weibo_detail_view_holder);
				
		favorate=(ImageView)findViewById(R.id.bt_favorate);
		favorate.setOnClickListener(lis);
		
		repost=(ImageView)findViewById(R.id.bt_retweet);
		repost.setOnClickListener(lis);		
		comment=(ImageView)findViewById(R.id.bt_comment);
		comment.setOnClickListener(lis);
		
		delete=(ImageView)findViewById(R.id.delete_weibo_bt);
		delete.setOnClickListener(lis);
		
		if(MumuWeiboUtility.LoginUser!=null && weiboInfo.getWeiboUser().getName().equals(MumuWeiboUtility.LoginUser.getName()))
			delete.setVisibility(View.VISIBLE);
		
		 listComment=new Button(this);
			//listComment=(Button)findViewById(R.id.more_comment);
			 listComment.setText("评论("+weiboInfo.getCommentCount()+")");
			 
			 LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(
					 LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			
			listComment.setTextColor(Color.BLUE);
			listComment.setGravity(Gravity.CENTER);
			
		//	listComment.setTextSize(14);
			
			listComment.setOnClickListener(new OnClickListener(){
				public void onClick(View v){
					Intent i=new Intent();
					i.setClass(WeiboDetail.this, CommentListShow.class);
					i.putExtra("id", mid);		
					i.putExtra("weibo_author", weiboInfo.getWeiboUser().getName());
					WeiboDetail.this.startActivity(i);					
				}
			});		
		
		weiboView.setView(weiboInfo);	
		
		isFavor=weiboInfo.isFavorated();
		if(isFavor)
			favorate.setImageResource(R.drawable.favoriated);
		else
			favorate.setImageResource(R.drawable.unfavoriate);
				
		
		msgList=new ArrayList<MsgItem>();
		if(weiboInfo.getRetweetWeiboInfo()!=null)
		{
			String name0=weiboInfo.getWeiboUser().getName();
			String cont;
			String weiboText=weiboInfo.getWeiboText();
			int end=weiboText.indexOf("//@");
								
			if(end==-1)end=weiboText.length();
			if(weiboText.startsWith("回复@"))
			{
				int begin=weiboText.indexOf(':')+1;				
				cont=weiboText.substring(begin, end);
				
			}
			else if(weiboText.startsWith("//@"))				
			{
				cont="转发微博";
			}
			else
			{				
				cont=weiboText.substring(0, end);				
			}
			msgList.add(0, new MsgItem(name0,cont));	
			
			parseList(weiboText.substring(end));
		}
		
		
		
		//通过代码添加布局
		int length=msgList.size();
		
		
		for(int i=0;i<length;i++)
		{			
			if(i%2==1){
				LeftWeiboView view=new LeftWeiboView(this);
				view.setView(msgList.get(i));
				holder.addView(view);	
				
				
				if(i==length-1){
						params.gravity=Gravity.LEFT;
						listComment.setLayoutParams(params);
						listComment.setBackgroundResource(R.drawable.left_comment_style);
						holder.addView(listComment);
				}
			}
			else
			{
				RightWeiboView view=new RightWeiboView(this);
				view.setView(msgList.get(i));
				holder.addView(view);	
				
				
				if(i==length-1){
					params.gravity=Gravity.RIGHT;
					listComment.setLayoutParams(params);
					
					listComment.setBackgroundResource(R.drawable.comment_button_style);
					holder.addView(listComment);
				}
			}						
		}			
		//holder.addView(listComment);
		
	}
	
	
	private void  parseList(String s)
	{
		int start,end;
		String name;
		String msg;
		MsgItem item;
		for(int i=0;i<s.length();)
		{
			start=s.indexOf('@', i)+1;
			end=s.indexOf(':',start);
			if(end==-1)return;				
			name=s.substring(start,end);
			
			//if(s.substring(end+1, end+3).equals("回复"))
			//	end=s.indexOf(':',end+3);
			
			if(s.substring(end+1).trim().startsWith("回复"))
				end=s.indexOf(':',end+3);
			
			start=end+1;
			end=s.indexOf("//@",start);
			
			//if(end-5>=0 && s.substring(end-5,end).equals("http:"))
				//end=s.indexOf("//", end+1);
			if(end==-1)end=s.length();
			if(start==end)msg="转发微博";
			else
				msg=s.substring(start, end);
			if(msg.trim().equals(""))msg="转发微博";
			
			msgList.add(0, new MsgItem(name,msg));			
			i=end;
		}	
	}
	
	public static class MsgItem{
		String userName="";
		String msgText="";
		
		public MsgItem(){
			
		}
		
		public MsgItem(String name,String text){
			userName=name;
			msgText=text;
		}
		public void set(String s1,String s2)
		{
			userName=s1;
			msgText=s2;
		}
	}
	
		
	private OnClickListener lis=new OnClickListener()
	{
		@Override
		public void onClick(View v) {
		
			//点击了收藏按钮
			if(v==favorate)
			{
				//ShareOperation.favoriateOrCancel(WeiboDetail.this, weiboInfo, pos);
				favoriateOrCancel(weiboInfo);
			}//结束了收藏操作
			//转发操作
			else if(v==repost)
			{
				if(weiboInfo.isDeleted().equals("1"))
				{
					Toast.makeText(WeiboDetail.this, "原微博不存在，无法转发！", Toast.LENGTH_LONG).show();
					return;
				}
				
				WeiboInfo retWeibo=weiboInfo.getRetweetWeiboInfo();
				if(retWeibo!=null && retWeibo.isDeleted().equals("1"))
				{
					Toast.makeText(WeiboDetail.this, "原微博不存在，无法转发！", Toast.LENGTH_LONG).show();
					return;
				}				
				
				Intent ins=new Intent();				
				ins.setClass(WeiboDetail.this,CommentWeibo.class);
				
				ins.putExtra("action","repost");			
				ins.putExtra("weibo", weiboInfo);
				WeiboDetail.this.startActivity(ins);				
				
			}//结束转发操作
			
			//点击评论按钮后
			else if(v==comment)
			{
				Intent ins=new Intent();				
				ins.setClass(WeiboDetail.this,CommentWeibo.class);
				
				ins.putExtra("action","comment");			
				ins.putExtra("weibo", weiboInfo);
				WeiboDetail.this.startActivity(ins);					
			}		
			else if(v==delete)
			{				
				ShareOperation.deleteWeibo(WeiboDetail.this,weiboInfo.getId());
			}
		}
		
	};
	
	//收藏或取消收藏操作
	public void favoriateOrCancel(WeiboInfo weiboInfo)
	{
		boolean isFavor1=weiboInfo.isFavorated();
		final String mid1=weiboInfo.getWeiboId();
		
		FavoritesAPI api=new FavoritesAPI(AccessTokenKeeper.readAccessToken(WeiboDetail.this));
		
		if(isFavor1==false)
		{
			//收藏
			//pd=ProgressDialog.show(WeiboDetail.this, null, "正在收藏...");	
			Toast.makeText(getApplicationContext(), "正在收藏...", Toast.LENGTH_SHORT).show();
			api.create(Long.parseLong(mid1), new FavoriateSuccessListener());				
		}
		else
		{
			//取消收藏
			Toast.makeText(getApplicationContext(), "正在取消收藏...", Toast.LENGTH_SHORT).show();
			api.destroy(Long.parseLong(mid1), new FavoriateCancelListener());
		}
	}
	
	public class FavoriateSuccessListener implements RequestListener{

		@Override
		public void onComplete(String arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			
			 handler.post(new Runnable() {

				   @Override
				   public void run() {
					   Toast.makeText(WeiboDetail.this, "收藏成功。", Toast.LENGTH_SHORT).show();
						isFavor=true;
						favorate.setImageResource(R.drawable.favoriated);
						weiboInfo.setIsFavorate(isFavor);	
						if(pos!=-1)
							MumuWeiboUtility.WeiboInfoList.set(pos, weiboInfo);	

				   }
				  });
			
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			showToast(WeiboErrorHelper.WeiboError(arg0));
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
		}
		
	}
	
	public class FavoriateCancelListener implements RequestListener{

		@Override
		public void onComplete(String arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			
			 handler.post(new Runnable() {

				   @Override
				   public void run() {
					   Toast.makeText(WeiboDetail.this, "微博已取消收藏。", Toast.LENGTH_SHORT).show();
						isFavor=false;
						favorate.setImageResource(R.drawable.unfavoriate);
						weiboInfo.setIsFavorate(isFavor);
						if(pos!=-1)
							MumuWeiboUtility.WeiboInfoList.set(pos, weiboInfo);

				   }
				  });
			
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			showToast(WeiboErrorHelper.WeiboError(arg0));
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();			
		}		
	}

}
