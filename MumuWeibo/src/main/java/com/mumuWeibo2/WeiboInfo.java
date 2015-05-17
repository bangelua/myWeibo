package com.mumuWeibo2;

import java.io.Serializable;

import android.text.TextUtils;

public class WeiboInfo implements Serializable{
	
	//一条微博包含的信息
	
	private String text=""; //weibo text
		
	private String weiboPicSmall="";  //用户原创微博的小图片地址
	private String weiboPicOriginal="";	
	private String mWeiboMiddlePicUrl = "";
	
	private String weiboId="";
	
	private long id;
	
	private WeiboInfo retweetWeibo=null;
	
	private WeiboUserInfo user=null;	
	private boolean isFavorated=false;
	
	private String createTime="";
	private int repostCount=0;
	private int commentCount=0;
	private String sourceName="";//来自应用的名称
	
	private String isDeleted="0";
	
	
	public WeiboInfo()
	{
		
	}
	
	public void setId(long id)
	{
		this.id=id;
	}
	
	public long getId()
	{
		return this.id;
	}
	
	public void setIsDeleted(String is)
	{
		if(!is.equals(""))isDeleted=is;
	}
	public String isDeleted(){
		return isDeleted;
	}
	public void setSourceName(String s)
	{
		int start=s.indexOf('>')+1;
		if(start==-1 || start>=s.length())
		{
			sourceName="";
			return;
		}
		
		int end=s.indexOf('<', start);
		if(end==-1)
		{
			sourceName="";
			return;
		}
		sourceName=s.substring(start, end);
	}
	public String getSourceName(){
		return sourceName;
	}
	
	public void setCommentCount(int i)
	{
		commentCount=i;
	}
	public int getCommentCount(){
		return commentCount;
	}
	
	public void setRepostCount(int c){
		repostCount=c;
	}
	public int getRepostCount(){
		return repostCount;
	}
	
	public void setCreateTime(String create)
	{
		createTime=create;
	}
	
	public String getCreateTime(){
		return createTime;
	}
	
	public void setIsFavorate(boolean favor)
	{
		isFavorated=favor;
	}
	
	public boolean isFavorated(){
		return isFavorated;
	}
	
	public void setRetweetWeiboInfo(WeiboInfo wi){
		 retweetWeibo=wi;
	}
	public WeiboInfo getRetweetWeiboInfo(){
		 return retweetWeibo;
	}
	
	public void setWeiboUser(WeiboUserInfo w){
		user=w;
	}
	public WeiboUserInfo getWeiboUser(){
		return user;
	}
	
	public void setWeiboText(String text)
	{
		this.text=text;
	}
	public String getWeiboText()
	{
		return text;
	}
	
	
	
	
	public void setWeiboPicSmall(String pic)
	{
		weiboPicSmall=pic;
	}
	
	public void setWeiboMiddlePicUrl(String midPicUrl){
		mWeiboMiddlePicUrl = midPicUrl;
		
	}
	
	public String getWeiboPicMiddle() {
		return mWeiboMiddlePicUrl;
	}
	
	public String getWeiboPicSmall()
 {
		if (TextUtils.isEmpty(mWeiboMiddlePicUrl)) {
			return weiboPicSmall;
		} else {
			return getWeiboPicMiddle();// fake, be care!
		}

	}
	
	public void setWeiboPicOriginal(String pic)
	{
		weiboPicOriginal=pic;
	}
	
	public String getWeiboPicOriginal()
	{
		return weiboPicOriginal;
	}
	
	
	public void setWeiboId(String mid)
	{
		weiboId=mid;
	}
	
	public String getWeiboId(){
		return weiboId;
	}
	
	public boolean hasPic()
	{
		if(!weiboPicSmall.equals(""))return true;
		
		if(retweetWeibo!=null && !retweetWeibo.weiboPicSmall.equals(""))
			return true;
		return false;		
	}	
}
