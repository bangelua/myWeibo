package com.mumuWeibo2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class EmotionParser {
	
	public static int count=0; //记录解析方法调用的次数，以此确定是否所有线程都调用该方法结束。
	
	synchronized public static  int  parse(String s) 
	{
		JSONArray ja;
		count++;
		try {
			ja = new JSONArray(s);
			int len=ja.length();
			
			for(int i=0;i<ja.length();i++)
			{
				JSONObject jo=ja.getJSONObject(i);
				String emotionName=jo.optString("phrase");
				
				String emotionUrl=jo.optString("url");	
				if(MumuWeiboUtility.emotionMapList.containsKey(emotionName))continue;
				MumuWeiboUtility.emotionMapList.put(emotionName, emotionUrl);					
			}			
			return ja.length();
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}		
	}
}
