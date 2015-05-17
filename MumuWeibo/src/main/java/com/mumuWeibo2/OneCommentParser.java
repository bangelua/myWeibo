package com.mumuWeibo2;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

//单条评论微博的解析
public class OneCommentParser {
	
	public static WeiboInfo parse(String json) throws JSONException{
		JSONObject wei=new JSONObject(json);		
		return parse(wei);
	}
	public static WeiboInfo parse(JSONObject wei)
	{		
		//get one weibo info
		WeiboInfo weiboInfo=new WeiboInfo();		
		
		//get weibo info
		weiboInfo.setWeiboText(wei.optString("text"));
		weiboInfo.setCreateTime(wei.optString("created_at"));
		weiboInfo.setWeiboId(wei.optString("idstr"));	
		weiboInfo.setId(wei.optLong("id"));
		weiboInfo.setWeiboPicSmall(wei.optString("thumbnail_pic"));		
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
		
		//获取评论的原微博（可能是转发型微博）
		JSONObject SourceWeibo=wei.optJSONObject("status");
		if(SourceWeibo!=null){
		WeiboInfo SourceWeiboInfo=new WeiboInfo();
		SourceWeiboInfo=WeiboParser.parse(SourceWeibo);
		weiboInfo.setRetweetWeiboInfo(SourceWeiboInfo);
		}
		else
			weiboInfo.setRetweetWeiboInfo(null);	
		
		//获取我发出的评论文本
		JSONObject replyme=wei.optJSONObject("reply_comment");		
		if(replyme!=null){
			String myComment=replyme.optString("text");
			JSONObject userInfo2=replyme.optJSONObject("user");
			String myname="";
			if(userInfo2!=null){
				WeiboUserInfo user1=WeiboUserParser.parse(userInfo2);
				myname=user1.getName();
			}
			//额外评论信息
			String comment="\n|---回复评论---->@"+myname+": "+myComment;
			String temp=weiboInfo.getWeiboText();
			weiboInfo.setWeiboText(temp+comment);
		}
		

		return weiboInfo;			
	}
}
