package com.mumuWeibo2;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class ReceviedCommentParser {	
	public static int parse(String s,int loc)
	{
		ArrayList<WeiboInfo> commentList=new ArrayList<WeiboInfo>();
		int length=0;
		try {
			JSONObject jo=new JSONObject(s);
			JSONArray ja=jo.getJSONArray("comments");
			length=ja.length();	
					
			long lastWeiboId = 0l; //收到的微博时间最晚的微博id
			
			//解析收到的评论
			for(int i=0;i<length;i++)
			{				
				JSONObject comment=ja.getJSONObject(i);
				WeiboInfo weiboInfo=new WeiboInfo();				
				
				weiboInfo=OneCommentParser.parse(comment);
				
				if(MumuWeiboUtility.isContainBlockWords(weiboInfo))continue;
				commentList.add(weiboInfo);
								
			}//end for
			
			int chang=commentList.size();
				if(chang>0)lastWeiboId=commentList.get(chang-1).getId();
				
			int len=MumuWeiboUtility.CommentsList.size();
			if(loc==0)
				{
				if(len>1)
				{
					if(lastWeiboId>MumuWeiboUtility.CommentsList.get(0).getId())
					{
						commentList.remove(commentList.size()-1);
						MumuWeiboUtility.CommentsList.clear();
					}
					else if(lastWeiboId==MumuWeiboUtility.CommentsList.get(0).getId())
						commentList.remove(commentList.size()-1);
				}
				else if(len==1)
				{
					MumuWeiboUtility.CommentsList.clear();
				}
					MumuWeiboUtility.CommentsList.addAll(0, commentList);
				}
			else if(loc==1)MumuWeiboUtility.CommentsList.addAll(MumuWeiboUtility.CommentsList.size(),commentList);
			
		} 
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.i("ReceiverdCommentParser","评论列表解析失败！！！");
			
		}
		return commentList.size();
	}	

}
