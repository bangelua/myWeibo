package com.mumuWeibo2;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

//微博列表的解析
public class WeiboListParser {
	
	
	public static int parse(String json,ArrayList list,int loc) throws JSONException
	{
		JSONObject jo=new JSONObject(json);
		return parse(jo,list,loc);
	}
	
	//第二个参数代表插入的位置，0代表解析数据插入到原来list头部，1代表插入到后面,2代表啥事不干。
	public static int parse(JSONObject jo,ArrayList<WeiboInfo> list,int location) throws JSONException
	{					
			ArrayList<WeiboInfo> weiboList=new ArrayList<WeiboInfo>();
			
			JSONArray ja=jo.optJSONArray("statuses");	
		
			if(ja==null)return 0;
			
			int weibo_count=ja.length();
			long lastWeiboId = 0l; //收到的微博时间最晚的微博id
			
			for(int i=0;i<weibo_count;i++)
			{
				//保存单条微博
				WeiboInfo weiboInfo=new WeiboInfo();
												
				weiboInfo=WeiboParser.parse(ja.getString(i));	
				
				if(MumuWeiboUtility.isContainBlockWords(weiboInfo))continue;
				
				//------------
				weiboList.add(weiboInfo);		
				
			}//end for
			
			
			//不接受置顶微博
			if(weiboList.size()>1  && weiboList.get(0).getId()<weiboList.get(1).getId())
			{
				weiboList.remove(0);
			}
			
			
			if(weiboList.size()!=0)
				lastWeiboId=weiboList.get(weiboList.size()-1).getId();
			
			int length=list.size();
			if(location==0)
				{
					//if(length==0)list.addAll(0, weiboList);
					if(length>1)
					{
						if(lastWeiboId>list.get(0).getId())
						{
							weiboList.remove(weiboList.size()-1);
							list.clear();
						}
						else if(lastWeiboId==list.get(0).getId())
							weiboList.remove(weiboList.size()-1);
					}
					else if(length==1)
					{
						list.clear();
					}					
					
					list.addAll(0, weiboList);
					
				
				}
			else if(location==1)
				list.addAll(length,weiboList);	
			
			return weiboList.size();
		
	}
	
	public static int parseFavor(String jo,ArrayList<WeiboInfo> list,int location) throws JSONException
	{		
			
			ArrayList<WeiboInfo> weiboList=new ArrayList<WeiboInfo>();
			JSONObject joo=new JSONObject(jo);
			
			JSONArray ja=joo.optJSONArray("favorites");	
		
			if(ja==null)return 0;
			
			int weibo_count=ja.length();
			
			for(int i=0;i<weibo_count;i++)
			{
				JSONObject one=ja.getJSONObject(i);					
				
				//保存单条微博
				WeiboInfo weiboInfo=new WeiboInfo();
				
				weiboInfo=WeiboParser.parse(one.getString("status"));	
				if(weiboInfo.isDeleted().equals("1"))continue;
				
				if(MumuWeiboUtility.isContainBlockWords(weiboInfo.getWeiboText()))
					continue;			
								
				WeiboInfo retWeibo=weiboInfo.getRetweetWeiboInfo();
				
				if(retWeibo!=null)
				{
					String retWeiboText=retWeibo.getWeiboText();
					
					if(MumuWeiboUtility.isContainBlockWords(retWeiboText))
						continue;					
				}
				
				//------------
				weiboList.add(weiboInfo);		
				
			}//end for
			int length=list.size();
			if(location==0)list.addAll(0, weiboList);	
			else if(location==1)
				list.addAll(length,weiboList);			
			return weibo_count;
		
	}
	
	
}
