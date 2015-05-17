package com.mumuWeibo2;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

public class OneWeiboView extends LinearLayout{
	
	TextView weiboText;
	SimpleDraweeView weiboPic;
	
	View retweetHolder;
	
	TextView retWeiboText;
	SimpleDraweeView retWeiboPic;

	TextView sourceFrom;
	TextView weiboCounts;
	
	TextView createTimeOfSourceWeibo;
	TextView weiboCountsOfSourceWeibo;
	
//	String weiboSmallPicUrl;
	String weiboMiddlePicUrl;
//	String retWeiboSmallPicUrl;
	String retWeiboMiddlePicUrl;
		
	String weiboOriginalPicUrl;
	String retWeiboOriginalPicUrl;
	private Context mContext;
	
	AsyncBitmapLoader async=new AsyncBitmapLoader();

	public OneWeiboView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
		// TODO Auto-generated constructor stub
	}
	
	public OneWeiboView(Context context) {
		super(context);
		mContext = context;
		init();
		// TODO Auto-generated constructor stub
	}
	
	private void  init()
	{
		LayoutInflater.from(getContext()).inflate(R.layout.one_weibo_layout, this);
		
		//GET VIEW
		weiboText=(TextView)findViewById(R.id.weibo_text_in_one);
		weiboPic=(SimpleDraweeView)findViewById(R.id.weibo_picture_in_one);
		retweetHolder=(View)findViewById(R.id.weibo_msg_dialog_holder_in_one);
		retWeiboText=(TextView)findViewById(R.id.retweet_weibo_text_in_one);
		retWeiboPic=(SimpleDraweeView)findViewById(R.id.retweet_weibo_picture_in_one);
		sourceFrom=(TextView)findViewById(R.id.source_from);
		weiboCounts=(TextView)findViewById(R.id.weibo_counts);
		createTimeOfSourceWeibo=(TextView)findViewById(R.id.create_time_of_source_weibo);
		weiboCountsOfSourceWeibo=(TextView)findViewById(R.id.weibo_counts_of_source_weibo);
				
//		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
//		Display display = wm.getDefaultDisplay();
//		Point size = new Point();
//		display.getSize(size);
//		int picSize = size.x/3;
//		weiboPic.getLayoutParams().height = picSize;
//		weiboPic.getLayoutParams().width = picSize;
		weiboPic.setOnClickListener(lis);
		retWeiboPic.setOnClickListener(lis);
	}	
	public void setView(WeiboInfo weiboInfo)
	{
		
		boolean isAuto=MumuWeiboUtility.autoShowImage;
		boolean isShowImage=true;
		
		if(isAuto){
		ConnectivityManager con=(ConnectivityManager)MumuWeiboUtility.context.getSystemService(Context.CONNECTIVITY_SERVICE);
		State isWifi=con.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		if(isWifi==State.CONNECTED)
			isShowImage=true;
		else isShowImage=false;
		}
		
		MumuWeiboUtility.FormatWeibo(getContext(),weiboText, weiboInfo.getWeiboText(), false);
		
		
		if(weiboInfo.isDeleted().equals("1"))
		{		
			sourceFrom.setVisibility(View.GONE);
			weiboCounts.setVisibility(View.GONE);
			weiboPic.setVisibility(View.GONE);
			return;
		}
		
		
//		weiboSmallPicUrl=weiboInfo.getWeiboPicSmall();
		weiboMiddlePicUrl = weiboInfo.getWeiboPicMiddle();
		
		if(weiboMiddlePicUrl==null || weiboMiddlePicUrl.equals("") || isShowImage==false)
		{
			weiboPic.setVisibility(View.GONE);
		}
		else
		{		
			weiboPic.setVisibility(View.VISIBLE);
			weiboOriginalPicUrl=weiboInfo.getWeiboPicOriginal();
			weiboPic.setImageURI(Uri.parse(weiboMiddlePicUrl));
			//if(async.mGifTask!=null)async.mGifTask.stop();
//			async.loadBitmap(weiboPic, MumuWeiboUtility.IMAGE_TYPE.PIC,weiboMiddlePicUrl);
		}

		
		sourceFrom.setText("来自"+weiboInfo.getSourceName());
		sourceFrom.setVisibility(View.VISIBLE);
		weiboCounts.setText("转发("+weiboInfo.getRepostCount()+")  评论("+weiboInfo.getCommentCount()+")");
		weiboCounts.setVisibility(View.VISIBLE);
		
		WeiboInfo retWeibo=weiboInfo.getRetweetWeiboInfo();
		if(retWeibo==null)
		{
			retweetHolder.setVisibility(ViewGroup.GONE);			
		}
		else{
			
			retweetHolder.setVisibility(ViewGroup.VISIBLE);
			if(retWeibo.isDeleted().equals("1"))
			{
				createTimeOfSourceWeibo.setVisibility(View.GONE);
				weiboCountsOfSourceWeibo.setVisibility(View.GONE);
				MumuWeiboUtility.FormatWeibo(getContext(),retWeiboText,retWeibo.getWeiboText(), false);
				
				retWeiboPic.setVisibility(View.GONE);
				return;				
			}
			
			createTimeOfSourceWeibo.setText(MumuWeiboUtility.parseWeiboTime(retWeibo.getCreateTime()));
			weiboCountsOfSourceWeibo.setText("转发("+retWeibo.getRepostCount()+")  评论("+retWeibo.getCommentCount()+")");
			createTimeOfSourceWeibo.setVisibility(View.VISIBLE);
			weiboCountsOfSourceWeibo.setVisibility(View.VISIBLE);
			
		WeiboUserInfo user=retWeibo.getWeiboUser();
		if(user==null)return;
		
		MumuWeiboUtility.FormatWeibo(getContext(),retWeiboText, "@"+user.getName()+":"+retWeibo.getWeiboText(), false);
		
//		retWeiboSmallPicUrl=retWeibo.getWeiboPicSmall();
		retWeiboMiddlePicUrl = retWeibo.getWeiboPicMiddle();
		
		if(retWeiboMiddlePicUrl==null || retWeiboMiddlePicUrl.equals("")|| isShowImage==false)
		{
			retWeiboPic.setVisibility(View.GONE);
		}
		else
		{
			retWeiboPic.setVisibility(View.VISIBLE);
//			retWeiboPic.setImageURI(Uri.parse(retWeiboMiddlePicUrl));
			Uri uri = Uri.parse(retWeiboMiddlePicUrl);
			ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
//					. // other setters
			.build();

			DraweeController controller = Fresco.newDraweeControllerBuilder()
					.setImageRequest(request)
					.setAutoPlayAnimations(true)
//					. // other setters
			.build();
			retWeiboPic.setController(controller);
			retWeiboOriginalPicUrl=retWeibo.getWeiboPicOriginal();
		}
		}
		
		
		
		
	}//end of setView
	
	private OnClickListener lis=new OnClickListener()
	{
		public void onClick(View v)
		{
			if(v==weiboPic)
			{
				Intent i=new Intent();
				i.setClass(getContext(),WeiboImageShow.class);
				i.putExtra("IMAGE_URL_SMALL",weiboMiddlePicUrl);
				i.putExtra("IMAGE_URL_ORIGINAL", weiboOriginalPicUrl);
				getContext().startActivity(i);
			}
			else if(v==retWeiboPic){
				
				Intent i=new Intent();
				i.setClass(getContext(),WeiboImageShow.class);
				i.putExtra("IMAGE_URL_SMALL",retWeiboMiddlePicUrl);
				i.putExtra("IMAGE_URL_ORIGINAL", retWeiboOriginalPicUrl);
				getContext().startActivity(i);
			}
		}
	};

}
