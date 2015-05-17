package com.mumuWeibo2;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class WeiboUserParser {
	
	public static WeiboUserInfo parse(String s) throws JSONException
	{
		JSONObject jo=new JSONObject(s);
		return parse(jo);
	}
	
	public static WeiboUserInfo parse(JSONObject jo){
		
		
		WeiboUserInfo userInfo=new WeiboUserInfo();	
		if(jo==null)return null;
		
		userInfo.setDescription(jo.optString("description"));
		userInfo.setFollowerCount(jo.optInt("followers_count"));
		userInfo.setFriendsCount(jo.optInt("friends_count"));
		userInfo.setFavorCount(jo.optInt("favourites_count"));
		userInfo.setGender(jo.optString("gender"));
		userInfo.setId(jo.optLong("id"));
		userInfo.setIsFollowing(jo.optBoolean("following"));
		userInfo.setIsFollowMe(jo.optBoolean("follow_me"));
		userInfo.setIsOnline(jo.optInt("online_status"));
		
		JSONObject m1=jo.optJSONObject("status");
		if(m1!=null)
			userInfo.setLastWeibo(WeiboParser.parse(m1));
		else
			userInfo.setLastWeibo(null);
		userInfo.setLocation(jo.optString("location"));
		userInfo.setMid(jo.optString("idstr"));
		userInfo.setName(jo.optString("screen_name"));
		userInfo.setProfile(jo.optString("profile_image_url"));
		userInfo.setUrl(jo.optString("url"));
		userInfo.setWeiboCount(jo.optInt("statuses_count"));
		
		return userInfo;
	}	
}
