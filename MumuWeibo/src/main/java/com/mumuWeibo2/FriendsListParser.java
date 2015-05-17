package com.mumuWeibo2;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FriendsListParser {
	
	public static int parse(String s,ArrayList<WeiboUserInfo> list) throws JSONException
	{
		JSONObject jo=new JSONObject(s);
		JSONArray ja=jo.getJSONArray("users");
		int nextCursor=jo.getInt("next_cursor");
		int length=ja.length();
		ArrayList<WeiboUserInfo>  userList=new ArrayList<WeiboUserInfo>();
		
		for(int i=0;i<length;i++)
		{
			JSONObject jsonUser=ja.getJSONObject(i);
			WeiboUserInfo user=new WeiboUserInfo();
			user=WeiboUserParser.parse(jsonUser);
			userList.add(user);	
			MumuWeiboUtility.userInfoCache.put(user.getName(), user);
		}
		list.addAll(list.size(), userList);
		return nextCursor;
	}
}
