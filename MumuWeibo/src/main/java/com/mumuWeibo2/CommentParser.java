package com.mumuWeibo2;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


//解析出某条微博的评论列表
public class CommentParser {
	
	public static int parse(String s,ArrayList<WeiboInfo> list,int loc)
	{
		ArrayList<WeiboInfo> commentList=new ArrayList<WeiboInfo>();
		int length=0;
		try {
			JSONObject jo=new JSONObject(s);
			JSONArray ja=jo.getJSONArray("comments");
			length=ja.length();
			
			Log.i("CommentParser",length+"comments wait to parse");
			
			//解析评论
			for(int i=0;i<length;i++)
			{
				
				JSONObject comment=ja.getJSONObject(i);
				WeiboInfo weiboInfo=new WeiboInfo();				
				//weiboInfo=WeiboParser.parse(comment);
				weiboInfo=OneCommentParser.parse(comment);
				commentList.add(weiboInfo);
			}
			if(loc==0)list.addAll(0, commentList);
			else if(loc==1)list.addAll(list.size(),commentList);
			
		} 
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.i("CommentParser","评论列表解析失败！！！");
			
		}
		return length;
	}	
}