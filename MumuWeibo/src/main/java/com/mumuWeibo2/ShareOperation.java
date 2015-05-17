package com.mumuWeibo2;

import java.io.IOException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.FavoritesAPI;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.net.RequestListener;

public class ShareOperation {
	
	private static Context context;
		private static WeiboInfo weiboInfo;
		static ProgressDialog pd=null;
		static long weiboId=0l;
		
		private static Handler handler=new Handler();
		
		public static void showToast(final String s)
		{
			handler.post(new Runnable() {

				   @Override
				   public void run() {
				    Toast.makeText(context, s,
				      Toast.LENGTH_SHORT).show();
				   }
				  });
		}
		
		
		//
		
	static	class DeleteWeiboListener implements RequestListener{

			@Override
			public void onComplete(String arg0) {
				// TODO Auto-generated method stub
				showToast("微博已删除。");
				for(int i=0;i<MumuWeiboUtility.MyWeibosList.size();i++)
					if(MumuWeiboUtility.MyWeibosList.get(i).getId()==weiboId)
						MumuWeiboUtility.MyWeibosList.remove(i);
				
				for(int i=0;i<MumuWeiboUtility.WeiboInfoList.size();i++)
					if(MumuWeiboUtility.WeiboInfoList.get(i).getId()==weiboId)
						MumuWeiboUtility.WeiboInfoList.remove(i);
				
				MumuWeiboUtility.saveWeiboList(context, MumuWeiboUtility.LIST_FLAG.MYWEIBOS);
				MumuWeiboUtility.saveWeiboList(context, MumuWeiboUtility.LIST_FLAG.PUBLIC);
				
						
			}

			@Override
			public void onError(WeiboException arg0) {
				// TODO Auto-generated method stub
				showToast(WeiboErrorHelper.WeiboError(arg0));
				
			}

			@Override
			public void onIOException(IOException arg0) {
				// TODO Auto-generated method stub
				showToast("微博删除失败");
				
			}
			
		}
	
	public static void showWeiboOperation(final Context ctx,final WeiboInfo weibo)
	{
		  context=ctx;
		 weiboInfo=weibo;
		  String[] mList1={"评论","转发","收藏"};
		  //,"评论原微博","转发原微博","收藏原微博","查看用户资料","更多"};		 
		  
		  
		  String[] mList2={"评论","转发","收藏","评论原微博","转发原微博","收藏原微博"};
		  if(weiboInfo.isFavorated()){
			  mList1[2]="取消收藏";
			  mList2[2]="取消收藏";
		  }
		  String[] mList3={"评论","转发","收藏","删除"};
		  String[] mList;
		 
		  final WeiboInfo retWeibo=weiboInfo.getRetweetWeiboInfo();
		  if(MumuWeiboUtility.LoginUser!=null && weiboInfo.getWeiboUser().getName().equals(MumuWeiboUtility.LoginUser.getName())){
			  mList=mList3;			  
		  }
		  else if(retWeibo!=null)
			  mList=mList2;
		  else
			  mList=mList1;
		  
		  
	        AlertDialog.Builder listDia=new AlertDialog.Builder(context);
	        listDia.setTitle("微博操作");
	        listDia.setItems(mList, new DialogInterface.OnClickListener() {
	            
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                // TODO Auto-generated method stub
	                /*下标是从0开始的*/	            	
	               switch(which){	 	               
	               case 0:  //comment
	            	    Intent ins=new Intent();				
	   					ins.setClass(context,CommentWeibo.class);
	   					//ins.putExtra("position",pos);
	   					ins.putExtra("action","comment");
	   					//ins.putExtra("from", "mainpage");
	   					ins.putExtra("weibo", weiboInfo);   				
	   					context.startActivity(ins);	
	   					break;
	   				
	               case 1://repost
	            	   if(retWeibo!=null && retWeibo.isDeleted().equals("1")){
	            		   Toast.makeText(context, "原微博不存在，无法转发！", Toast.LENGTH_LONG).show();
	            		   break;
	            	   }
	            	    Intent ins2=new Intent();				
	   					ins2.setClass(context,CommentWeibo.class);
	   					//ins2.putExtra("from", "mainpage");
	   					ins2.putExtra("action","repost");
	   					//ins2.putExtra("position",pos);
	   					ins2.putExtra("weibo", weiboInfo); 
	   					context.startActivity(ins2);	
	   					break;
	   					
	               case 2://收藏	            	              	  
	            	   favoriateOrCancel(context,weiboInfo);
	            	   break;
	               case 3: //评论原微博
	            	   	if(weiboInfo.getWeiboUser().getName().equals(MumuWeiboUtility.LoginUser.getName())){
	            		 
	            	   		deleteWeibo(context,weiboInfo.getId());	            	 
	            	   }
	            	   else{
	            	   Intent ins3=new Intent();				
	   					ins3.setClass(context,CommentWeibo.class);
	   					//ins.putExtra("position",pos);
	   					ins3.putExtra("action","comment");
	   					//ins.putExtra("from", "mainpage");
	   					ins3.putExtra("weibo", retWeibo);   				
	   					context.startActivity(ins3);
	            	   }
	   					break;
	               case 4://转发原微博
	            	  
	            	   if(retWeibo!=null && retWeibo.isDeleted().equals("1")){
	            		   Toast.makeText(context, "原微博不存在，无法转发！", Toast.LENGTH_LONG).show();
	            		   break;
	            	   }
	            	    Intent ins4=new Intent();				
	   					ins4.setClass(context,CommentWeibo.class);
	   					//ins2.putExtra("from", "mainpage");
	   					ins4.putExtra("action","repost");
	   					//ins2.putExtra("position",pos);
	   					ins4.putExtra("weibo", retWeibo); 
	   					context.startActivity(ins4);	
	   					break;
	               case 5://收藏原微博
	            	   favoriateOrCancel(context,retWeibo);	
	            	   break;	            	   
	              default:
				   	Toast.makeText(context, "操作尚未设置", Toast.LENGTH_SHORT).show();				    		
				  }
	            }

				
	        });
	        listDia.create().show();
	}
	public static void deleteWeibo(final Context ctx,final long id) {
					// TODO Auto-generated method stub
		context=ctx;
		weiboId=id;
		AlertDialog.Builder dialog=new AlertDialog.Builder(context);
		dialog.setTitle(null);
		dialog.setMessage("确定删除此条微博？");
		dialog.setPositiveButton("删除", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				StatusesAPI api=new StatusesAPI(AccessTokenKeeper.readAccessToken(context));
			 	   api.destroy(id, new DeleteWeiboListener());
			}
		});	
		
		dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		
		dialog.create().show();
	}
	
	//收藏或取消收藏
	public static void favoriateOrCancel(final Context context,WeiboInfo weiboInfo)
	{
		
		boolean isFavor1=weiboInfo.isFavorated();
		final String mid1=weiboInfo.getWeiboId();
		FavoritesAPI api=new FavoritesAPI(AccessTokenKeeper.readAccessToken(context));
		
		if(isFavor1==false)
		{
			//收藏
			 // pd=ProgressDialog.show(context, null, "正在收藏...");
			  Toast.makeText(context, "正在收藏...", Toast.LENGTH_SHORT).show();
			  //pd.setCancelable(true);
			
				api.create(Long.parseLong(mid1), new FavoriateSuccessListener());				
			
		}
		else
		{
			//取消收藏
			// pd=ProgressDialog.show(context, null, "取消收藏...");
			 Toast.makeText(context, "正在取消收藏...", Toast.LENGTH_SHORT).show();
			//pd.setCancelable(true);
			
			api.destroy(Long.parseLong(mid1), new FavoriateCancelListener());
						
		}
	}
	
	static class FavoriateCancelListener implements RequestListener{

		@Override
		public void onComplete(String arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			showToast("收藏已取消");
					
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			showToast(WeiboErrorHelper.WeiboError(arg0));
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			showToast( "取消收藏失败！\n"+arg0.getMessage());
			
		}
		
		
	}
	public static class FavoriateSuccessListener implements RequestListener{

		@Override
		public void onComplete(String arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			showToast("收藏成功。");
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			showToast(WeiboErrorHelper.WeiboError(arg0));
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			showToast("收藏失败！\r\n"+arg0.getMessage());			
		}
	}
}
