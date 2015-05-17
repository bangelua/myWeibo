package com.mumuWeibo2;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SourceWeiboView extends LinearLayout{
	
	private Context context;
	private ImageView profile;
	private TextView username;
	private View v;
	private TextView weiboText;
	private ImageView weiboPic;
	private TextView sourceFrom;
	private TextView weiboCounts;
	private TextView createTime;
	private WeiboInfo 	weiboInfo;
	
	String bigPicUrl;
	String smallPicUrl;
	
	WeiboUserInfo user;
	
	//String name;
	
	String mid;
	String username11;
	private AsyncBitmapLoader async=new AsyncBitmapLoader();
	
	public SourceWeiboView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context=context;
		init();
	}

	public SourceWeiboView(Context context) {
		super(context);
		this.context=context;
		init();
	}
	private void init()
	{
		LayoutInflater.from(getContext()).inflate(R.layout.source_weibo_layout, this);
		profile=(ImageView)findViewById(R.id.iv_user_profile_in_source);
		username=(TextView)findViewById(R.id.tv_user_name_in_source);
		v=(View)findViewById(R.id.weibo_msg_dialog_holder_in_source);
		weiboText=(TextView)findViewById(R.id.retweet_weibo_text_in_source);
		weiboPic=(ImageView)findViewById(R.id.retweet_weibo_picture_in_source);
		
		sourceFrom=(TextView)findViewById(R.id.source_from_in_sourceweibo);
		weiboCounts=(TextView)findViewById(R.id.weibo_counts_in_souceweibo);
		createTime=(TextView)findViewById(R.id.create_time_insourceweibo);
		
		profile.setOnClickListener(lis);
		username.setOnClickListener(lis);
		weiboPic.setOnClickListener(lis);
		weiboCounts.setOnClickListener(lis);
	}
	
	public void setView(WeiboInfo weibo)
	{
		 weiboInfo=weibo;
		if(weibo.getRetweetWeiboInfo()!=null)	
			weiboInfo=weibo.getRetweetWeiboInfo();	
		
		mid=weiboInfo.getWeiboId();
		if(weiboInfo.isDeleted().equals("1"))
		{
			createTime.setText(MumuWeiboUtility.parseWeiboTime(weiboInfo.getCreateTime()));
			sourceFrom.setVisibility(View.GONE);
			weiboCounts.setVisibility(View.GONE);
			username.setText("");
			MumuWeiboUtility.FormatWeibo(getContext(),weiboText,weiboInfo.getWeiboText(),true);
			
			weiboText.setMovementMethod(LinkMovementMethod.getInstance());
			profile.setImageResource(R.drawable.defalut_profile_image);
			weiboPic.setVisibility(View.GONE);	
			return;
		}		
		
		smallPicUrl=weiboInfo.getWeiboPicSmall();
		bigPicUrl=weiboInfo.getWeiboPicOriginal();		
		
		createTime.setText(MumuWeiboUtility.parseWeiboTime(weiboInfo.getCreateTime()));
		sourceFrom.setText("来自"+weiboInfo.getSourceName());
		
		
		 user=weiboInfo.getWeiboUser();
		if(user!=null){
		async.loadBitmap(profile, MumuWeiboUtility.IMAGE_TYPE.PROFILE,user.getProfile());
		String s=user.getName();
		if(s.getBytes().length>24)
			s=user.getName().substring(0,7)+"...";
		username.setText(s);
		username11=user.getName();
		}
		else
			username.setText("未知用户");
			
		MumuWeiboUtility.FormatWeibo(getContext(),weiboText, weiboInfo.getWeiboText(), true);
				
		
		weiboText.setMovementMethod(LinkMovementMethod.getInstance());
		
		String pic=weiboInfo.getWeiboPicSmall();
		if(pic==null || pic.equals(""))
			weiboPic.setVisibility(View.GONE);
		else
			async.loadBitmap(weiboPic,MumuWeiboUtility.IMAGE_TYPE.PIC, pic);		
		
String weiboCount="转发("+weiboInfo.getRepostCount()+")  评论("+weiboInfo.getCommentCount()+")";
		
		weiboCounts.setText(weiboCount);
		
		int start=weiboCount.indexOf("评论");
		int end=weiboCount.length();
		String coments=weiboCount.substring(start, end);
		
		//weiboCounts.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		SpannableStringBuilder sp=new SpannableStringBuilder(weiboCount);
		 sp.setSpan(new URLSpan(coments){
				@Override
				public void onClick(View widget) {
					// TODO Auto-generated method stub
						
				}
				public void updateDrawState(TextPaint ds)
				{
					//ds.setColor(Color.BLUE);
					ds.setUnderlineText(true);
				}					 
			 }, 
			 start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		 weiboCounts.setText(sp);
	}
	
	private OnClickListener lis=new OnClickListener()
	{
		
		public void onClick(View v)
		{
			Intent in=new Intent();
			
			if(v==profile || v==username){		
				if(user==null)return;
			in.setClass(getContext(), UserInfoShow.class);
			//in.putExtra("screen_name",username11);
			in.putExtra("userinfo", user);
			getContext().startActivity(in);
			}
			else if(v==weiboPic)
			{
				in.setClass(getContext(),WeiboImageShow.class);
				in.putExtra("IMAGE_URL_SMALL",smallPicUrl);
				in.putExtra("IMAGE_URL_ORIGINAL", bigPicUrl);
				getContext().startActivity(in);
			}
			else if(v==weiboCounts)
			{
				
				Intent i=new Intent();
				i.setClass(context, CommentListShow.class);
				i.putExtra("id", mid);	
				i.putExtra("weibo_author", username11);
				context.startActivity(i);	
				
			}
			
		}
	};
	
}
