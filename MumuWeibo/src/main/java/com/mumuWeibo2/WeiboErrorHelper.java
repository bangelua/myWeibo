package com.mumuWeibo2;

import org.json.JSONException;
import org.json.JSONObject;

import com.weibo.sdk.android.WeiboException;

//处理微博异常错误的类
public class WeiboErrorHelper {
	
	//返回导致微博异常错误的原因
	public static String WeiboError(WeiboException e){
		
		JSONObject jo;
		String response=e.getMessage();
		try {
			jo = new JSONObject(response);
		} 
		catch (JSONException e1) {
			// TODO Auto-generated catch block
			
			return "操作失败，请检查网络连接。";
		}
		int code=jo.optInt("error_code");
		String reason=jo.optString("error");
		
		switch(code){
		case 10001: return "系统错误"; 
		case 10002: return "服务端资源不可用"; 
	   
		case 10026 : return "该接口已经废弃"; 

		case 20003 : return "用户不存在"; 
		case 20005 : return "不支持的图片类型,仅仅支持JPG,GIF,PNG";
		case 20006 : return "图片太大";		
	   
		case 20008 : return "内容为空";
	   
		case 20012 : return "输入文字太长，请确认不超过140个字符";
		case 20013 : return "输入文字太长，请确认不超过300个字符";
	    
		case 20016 : return "发微博太多啦，休息一会儿吧";
		case 20017 : return "你刚刚已经发送过相似内容了哦，先休息一会吧";
		case 20019 : return "不要太贪心哦，发一次就够啦";
		case 20023 : return "很抱歉，此功能暂时无法使用，如需帮助请联系@微博客服 或者致电客服电话400 690 0000";
		
	   
		case 20032 : return "微博发布成功。目前服务器数据同步可能会有延迟，请耐心等待1-2分钟。谢谢";
		
		case 20033 : return "登陆状态异常";
		case 20038 : return "您刚才已经发过相似的内容啦，建议您第二天再尝试！";
	  
		case 20101 : return "不存在的微博";
	   
		case 20103 : return "不能转发自己的微博";
	 
		case 20111 : return "不能发布相同的微博";
		case 20112 : return "由于作者隐私设置，你没有权限查看此微博";
	    
		case 20120 : return "由于作者设置了可见性，你没有权限转发此微博";
	    
		case 20130 : return "由于作者隐私设置，你没有权限评论此微博";
		case 20132 : return "抱歉，该内容暂时无法查看。如需帮助，请联系客服";
	   
		case 20134 : return "分组不存在";
		case 20135 : return "源微博已被删除";
	   
	   
		case 20206 : return "作者只允许关注用户评论";
		case 20207 : return "作者只允许可信用户评论"; 

		case 20704 : return "您已经收藏了此微博";
		case 20705 : return "此微博不是您的收藏";
		case 20706 : return "操作失败"; 
	   
		case 21301 : return "需要重新认证";
	   
		case 21315 : return "Token已经过期";
	   
	   
		case 21332 : return "access_token 无效";
		case 21333 : return "禁止使用此认证方式";
		case 21334 : return "帐号状态不正常";  
		case 21321 : 
			AccessTokenKeeper.clear(MumuWeiboUtility.context);
			return "该应用尚未通过审核，仅限测试账户登录！";
		
		default:
			return "错误码："+code+"\n"+reason;
	   
		}
		
	}
}
