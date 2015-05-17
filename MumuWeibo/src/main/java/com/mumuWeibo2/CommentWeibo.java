package com.mumuWeibo2;

import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.CommentsAPI;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.api.WeiboAPI;
import com.weibo.sdk.android.net.RequestListener;

public class CommentWeibo extends Activity{
	
	private EditText etComment;
	private Button btSend;
	private ImageButton ivFace;
	private TextView title;	
	private CheckBox cbIsRepostAndComment;
	private CheckBox cbIsRepostAndCommentOriginal;
	private String action="repost";
	private WeiboInfo weiboInfo=null;
	private String retCont="";//转发微博的非原始微博的内容
	
	private ProgressDialog pd=null;
	String mid;
	String originalId;
	String from;
	
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
	
	
	private OnClickListener lis=new OnClickListener()
	{
		public void onClick(View v)
		{
			if(v==btSend)
				{
					if(action.equals("repost"))repostIt();
					else if(action.equals("comment"))
						commentIt();	
					else if(action.equals("reply"))
						replyIt();
				}	
			else if(v==ivFace){
				new FaceDialog(CommentWeibo.this,etComment);
			}
		}
	};
	
	public void onCreate(Bundle bb)
	{
		super.onCreate(bb);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		MumuWeiboUtility.isSeized=true;
		//getWindow().setSoftInputMode(   
       //         WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		setContentView(R.layout.comment_layout);
		
		//setView
		title=(TextView)findViewById(R.id.tv_title_text_in_comment);
		etComment=(EditText)findViewById(R.id.edit_comment_content);
		btSend=(Button)findViewById(R.id.bt_send_comment);
		btSend.setOnClickListener(lis);
		ivFace=(ImageButton)findViewById(R.id.iv_face_show);
		ivFace.setOnClickListener(lis);
		
		cbIsRepostAndComment=(CheckBox)findViewById(R.id.cb_repost_and_comment_it);
		cbIsRepostAndCommentOriginal=(CheckBox)findViewById(R.id.cb_repost_and_comment_original);
				
		//get info
		Intent in=getIntent();			
		
		weiboInfo=(WeiboInfo) in.getExtras().getSerializable("weibo");
		 action=in.getStringExtra("action");
		if(weiboInfo==null)
			{
				Toast.makeText(getApplicationContext(), "微博不存在！", Toast.LENGTH_LONG).show();
				finish();
				return;		
			}
		
		mid=weiboInfo.getWeiboId();
		boolean isRetweet;
		if(weiboInfo.getRetweetWeiboInfo()==null)
			isRetweet=false;
		else
			isRetweet=true;
				
		//view自适应
		if(action.equals("comment"))
		{
			title.setText("评论微博");
			etComment.setHint("评论一下吧~");
			cbIsRepostAndComment.setText("同时评论给原微博作者");
			cbIsRepostAndCommentOriginal.setText("同时转发到我的微博");			
		}		
		
		if(isRetweet)
			{
				originalId=weiboInfo.getRetweetWeiboInfo().getWeiboId();
				retCont="//@"+weiboInfo.getWeiboUser().getName()+":"+weiboInfo.getWeiboText();
				if(action.equals("repost"))
					etComment.setText(retCont);	
				cbIsRepostAndComment.setVisibility(View.VISIBLE);
			}
		else
			{
				if(action.equals("comment"))
					cbIsRepostAndComment.setVisibility(View.GONE);
				else if(action.equals("repost"))
				{
					cbIsRepostAndComment.setVisibility(View.VISIBLE);							
					cbIsRepostAndCommentOriginal.setVisibility(View.GONE);
									
				}					
			}
		if(action.equals("reply")){
			title.setText("回复微博");
			etComment.setHint("回复");
			cbIsRepostAndComment.setVisibility(View.GONE);
			cbIsRepostAndCommentOriginal.setText("同时转发到我的微博");
		}
		
	}	
	
	//转发微博
	public void repostIt()
	{
		pd=ProgressDialog.show(CommentWeibo.this, null, "微博发送中...");
							
					StatusesAPI.COMMENTS_TYPE isComment2=StatusesAPI.COMMENTS_TYPE.NONE;
					if(cbIsRepostAndComment.isChecked() && !cbIsRepostAndCommentOriginal.isChecked())
						 isComment2=StatusesAPI.COMMENTS_TYPE.CUR_STATUSES;
					if(!cbIsRepostAndComment.isChecked() && cbIsRepostAndCommentOriginal.isChecked())
						 isComment2=StatusesAPI.COMMENTS_TYPE.ORIGAL_STATUSES;
					if(cbIsRepostAndComment.isChecked() && cbIsRepostAndCommentOriginal.isChecked())
						 isComment2=StatusesAPI.COMMENTS_TYPE.BOTH;
					
					String edit=etComment.getText().toString();
					if(edit.equals(""))edit="转发微博";
					int cc=edit.indexOf("//@");
					String commentCont="";
					if(cc==-1)cc=edit.length();
					commentCont=edit.substring(0,cc);
					
					StatusesAPI api=new StatusesAPI(AccessTokenKeeper.readAccessToken(CommentWeibo.this));
					
					api.repost(Long.parseLong(mid), edit, isComment2, new RepostListener());
				
}
	
	class RepostListener implements RequestListener{

		@Override
		public void onComplete(String arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			showToast("转发成功。");
			finish();
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			showToast(WeiboErrorHelper.WeiboError(arg0));
			finish();
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			Toast.makeText(CommentWeibo.this, "转发失败。\r\n"+arg0.getMessage(), Toast.LENGTH_SHORT).show();
			finish();
		}		
	}
	
				
	//评论微博
	public void commentIt()	
	{
		if(etComment.getText().toString().trim().equals(""))
			{
			Toast.makeText(getApplicationContext(), "评论内容不能为空！", Toast.LENGTH_SHORT).show();
			return;
		}
		pd=ProgressDialog.show(CommentWeibo.this, null, "微博发送中...");
				
				boolean isComment2Ori=true;
				if(cbIsRepostAndComment.isChecked())isComment2Ori=false;
				
				StatusesAPI.COMMENTS_TYPE commentType=StatusesAPI.COMMENTS_TYPE.CUR_STATUSES;
				
				//是否评论后转发
				if(cbIsRepostAndCommentOriginal.isChecked())
				{		
					if(cbIsRepostAndComment.isChecked())
						commentType=StatusesAPI.COMMENTS_TYPE.BOTH;
					
					StatusesAPI api=new StatusesAPI(AccessTokenKeeper.readAccessToken(getApplicationContext()));
					api.repost(Long.parseLong(mid), etComment.getText().toString(), commentType, new CommentWeiboListener());
					
				}
				else
				{
					CommentsAPI api=new CommentsAPI(AccessTokenKeeper.readAccessToken(CommentWeibo.this));
					api.create(etComment.getText().toString(), Long.parseLong(mid), isComment2Ori, new CommentWeiboListener());
				}					
	}
	
	class CommentWeiboListener implements RequestListener{

		@Override
		public void onComplete(String arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			showToast("微博评论成功。");
			finish();
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			showToast(WeiboErrorHelper.WeiboError(arg0));
			finish();
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			Toast.makeText(CommentWeibo.this, "微博评论失败。", Toast.LENGTH_SHORT).show();
			finish();
		}		
	}
	//回复
	public void replyIt(){
		
		final String s=etComment.getText().toString();
		
		pd=ProgressDialog.show(CommentWeibo.this, null, "发送回复中...");
		
		CommentsAPI api=new CommentsAPI(AccessTokenKeeper.readAccessToken(getApplicationContext()));
		long originalWeiboId=Long.parseLong(weiboInfo.getRetweetWeiboInfo().getWeiboId());
		api.reply(Long.parseLong(mid), originalWeiboId, s, true, false, new ReplyListener());
		
		//同时转发
		if(cbIsRepostAndCommentOriginal.isChecked())
		{
			String name=weiboInfo.getWeiboUser().getName();
			String pre="回复@"+name+":";
			String back="//@"+name+":"+weiboInfo.getWeiboText();
			
			String id=weiboInfo.getRetweetWeiboInfo().getWeiboId();
			if(weiboInfo.getRetweetWeiboInfo().getRetweetWeiboInfo()!=null)
			{
				id=weiboInfo.getRetweetWeiboInfo().getRetweetWeiboInfo().getWeiboId();
				back+="//@"+weiboInfo.getRetweetWeiboInfo().getWeiboUser().getName()+":"+weiboInfo.getRetweetWeiboInfo().getWeiboText();
			}
			String cont=pre+etComment.getText().toString()+back;
			
			StatusesAPI api2=new StatusesAPI(AccessTokenKeeper.readAccessToken(getApplicationContext()));
			api2.repost(Long.parseLong(id), cont, WeiboAPI.COMMENTS_TYPE.NONE, new RepostListener());
			
		}			
	}
	
	class ReplyListener implements RequestListener{

		@Override
		public void onComplete(String arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			showToast("回复成功。");
			finish();
			
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			showToast(WeiboErrorHelper.WeiboError(arg0));
			finish();
		}

		@Override
		public void onIOException(IOException arg0){
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			Toast.makeText(getApplicationContext(), "回复失败！/r/n"+arg0.getMessage(), Toast.LENGTH_SHORT).show();
			finish();
		}		
	}
}

