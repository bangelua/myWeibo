package com.mumuWeibo2;

import java.io.Serializable;


//记录微博用户的信息类
public class WeiboUserInfo implements Serializable{
	
	private long userId=0;
	private String usermid="";
	private String name="";
	private String loc="";
	private String description="";
	private String profile="";
	private String gender="";
	private int followerCount=-1;
	private int friendsCount=-1;
	private int weiboCount=-1;
	private int favorCount=-1;
	private boolean isFollowing; //你是否在关注ta
	private boolean isFollowMe;  //ta是否在关注你
	private int isOnline=0;
	
	private String url="";
	
	private WeiboInfo lastWeibo=null;
	
	
	public void setFavorCount(int i){
		favorCount=i;
	}
	public int getFavorCount(){
		return favorCount;
	}
	
	public void setMid(String id){
		usermid=id;
	}
	
	public void setLastWeibo(WeiboInfo weibo)
	{
		lastWeibo=weibo;
	}
	
	public WeiboInfo getLastWeibo(){
		return lastWeibo;
	}
	
	public void setUrl(String u){
		url=u;
	}
	public String getUrl(){
		return url;
	}
	
	public void setIsOnline(int i){
		isOnline=i;
	}
	public int getIsOnline(){
		return isOnline;
	}
	
	public void setIsFollowMe(boolean b){
		isFollowMe=b;
	}
	public boolean getIsFollowME(){
		return isFollowMe;
	}
	
	public void setIsFollowing(boolean b){
		isFollowing=b;
	}
	public boolean isFollowing(){
		return isFollowing;
	}
	
	public void setWeiboCount(int i){
		weiboCount=i;
	}
	public int getWeiboCount(){
		return weiboCount;
	}
	
	public void setFriendsCount(int i){
		friendsCount=i;
	}
	public int getFriendsCount(){
		return friendsCount;
	}
	
	public void setFollowerCount(int c){
		followerCount=c;
	}
	public int getFollowerCount(){
		return followerCount;
	}
	
	public void setGender(String g){
		gender=g;
	}
	public String getGender(){
		return gender;
	}
	
	
	public void setId(long id)
	{
		this.userId=id;
	}
	public long getId(){
		return userId;
	}
	
	public void setName(String s){
		name=s;
	}
	public String getName(){
		return name;
	}

	public void setLocation(String lo){
		loc=lo;
	}
	public String getLocation(){
		return loc;
	}
	
	public void setDescription(String d){
		description=d;
	}
	public String getDescription(){
		return description;
	}
	
	public void setProfile(String s){
		profile=s;
	}
	public String getProfile(){
		return profile;
	}

}
