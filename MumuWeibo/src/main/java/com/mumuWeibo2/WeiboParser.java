package com.mumuWeibo2;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;



//单条微博的解析
public class WeiboParser {
	
	public static WeiboInfo parse(String json) throws JSONException{
		JSONObject wei=new JSONObject(json);
		
		return parse(wei);
	}
	public static WeiboInfo parse(JSONObject wei)
	{		
		//get one weibo info
		WeiboInfo weiboInfo=new WeiboInfo();			
		//get weibo info
		weiboInfo.setId(wei.optLong("id"));
		weiboInfo.setWeiboText(wei.optString("text"));
		weiboInfo.setCreateTime(wei.optString("created_at"));
		weiboInfo.setWeiboId(wei.optString("idstr"));		
		weiboInfo.setWeiboPicSmall(wei.optString("thumbnail_pic"));		
		weiboInfo.setWeiboMiddlePicUrl(wei.optString("bmiddle_pic"));
		 
		weiboInfo.setWeiboPicOriginal(wei.optString("original_pic"));
		weiboInfo.setIsFavorate(wei.optBoolean("favorited"));
		weiboInfo.setCommentCount(wei.optInt("comments_count"));
		weiboInfo.setRepostCount(wei.optInt("reposts_count"));
		weiboInfo.setSourceName(wei.optString("source"));
		weiboInfo.setIsDeleted(wei.optString("deleted"));
		
					
		//get user info
		JSONObject userInfo=wei.optJSONObject("user");
		if(userInfo!=null){
		WeiboUserInfo user=WeiboUserParser.parse(userInfo);
		weiboInfo.setWeiboUser(user);	
		}
		else
			weiboInfo.setWeiboUser(null);		
		
		//get retweet_weibo_status
		JSONObject retweetWeibo=wei.optJSONObject("retweeted_status");
		if(retweetWeibo!=null){
		WeiboInfo retweetWeiboInfo=new WeiboInfo();
		retweetWeiboInfo=parse(retweetWeibo);
		weiboInfo.setRetweetWeiboInfo(retweetWeiboInfo);
		}
		else
			weiboInfo.setRetweetWeiboInfo(null);
				
		return weiboInfo;			
	}
}
