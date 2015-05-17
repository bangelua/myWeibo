package com.mumuWeibo2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MumuWeiboUtility {		
	public static Context context;
	public static final String fileCacheDir=Environment.getExternalStorageDirectory()+"/.mumuWeiboCache/";
	public static String imageSaveDir=Environment.getExternalStorageDirectory()+"/MumuWeibo/";
	public static String emotionSaveDir=Environment.getExternalStorageDirectory()+"/.mumuWeiboCache/emotions/";
		
	//public static String imageSaveDir;
	//public static  String emotionSaveDir;
	//public static  String emotionSaveDir;
	
	public static boolean autoShowImage;//微博是否自适应显示缩略图
	public static boolean isFlushingWeibo=false;
	
	public static final String SETTING_INFO="SETTING_INFOS";
	
	public static enum IMAGE_TYPE{PROFILE, PIC};
	public static enum LIST_FLAG{PUBLIC,COMMENTS,ATMSG,MYWEIBOS};
	
	public static boolean isSeized=false;
	
	//程序在启动运行后后保存的公共微博列表，下次启动后会消失
	public static ArrayList<WeiboInfo> WeiboInfoList=new ArrayList<WeiboInfo>();
	//收到的评论微博列表
	public static ArrayList<WeiboInfo> CommentsList=new ArrayList<WeiboInfo>();
	//收到的@微博列表
	public static ArrayList<WeiboInfo> AtMsgList=new ArrayList<WeiboInfo>();
	
	//登录用户的微博保存列表
	public static ArrayList<WeiboInfo> MyWeibosList=new ArrayList<WeiboInfo>();
	
	
	//微博屏蔽关键字
	public static ArrayList<String> BlockWordsList=new ArrayList<String>();
	
	//表情名及其网络地址键值对。如，{[笑哈哈]，http://...}
	public static HashMap<String,String> emotionMapList=new HashMap<String,String>();
	
	//必须要初始化
	private  static HashMap<String,SoftReference<Drawable>> imageCache=new HashMap<String,SoftReference<Drawable>>();
	
	//用户信息缓存
	public static HashMap<String,WeiboUserInfo> userInfoCache=new HashMap<String,WeiboUserInfo>();
	
	//当前用户
	public static WeiboUserInfo LoginUser=null;
	
	

	
	static HashMap<String,SoftReference<Drawable>> getCache()
	{
		return imageCache;
	}
	
	public static void ImportBlockWords()
	{
		try {
			FileInputStream fis=context.openFileInput("BlockWordsList.txt");
			
			try {
				ObjectInputStream ois=new ObjectInputStream(fis);
				try {
					MumuWeiboUtility.BlockWordsList=(ArrayList<String>)ois.readObject();
					
				} 
				catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(ois!=null)ois.close();
				if(fis!=null)fis.close();
			} catch (StreamCorruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e1){
			//e1.printStackTrace();
			}
	}
	
	public static void saveBlockWordsList()
	{
		try {
			FileOutputStream os=context.openFileOutput("BlockWordsList.txt", Context.MODE_PRIVATE);
			try {
				ObjectOutputStream oos=new ObjectOutputStream(os);
				oos.writeObject((ArrayList<String>)MumuWeiboUtility.BlockWordsList);
				if(oos!=null)oos.close();
				if(os!=null)os.close();
			
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
			
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//检查文本内是否含有指定的屏蔽关键字，如果有，则返回true，否则返回false
	
	public static boolean isContainBlockWords(String text)
	{
		for(int i=0;i<MumuWeiboUtility.BlockWordsList.size();i++)
		{
			if(text.contains(MumuWeiboUtility.BlockWordsList.get(i)))
				return true;
		}
		return false;
	}
	
	//检查文本内是否含有指定的屏蔽关键字，如果有，则返回true，否则返回false
	public static boolean isContainBlockWords(WeiboInfo weiboInfo)
	{
		boolean isBlock=true;
		if(weiboInfo==null)return true;
		if(weiboInfo.getWeiboUser()!=null && MumuWeiboUtility.LoginUser!=null &&
				weiboInfo.getWeiboUser().getName().equals(MumuWeiboUtility.LoginUser.getName()))
		isBlock=false;
		
		//屏蔽关键字
		if(isBlock){
		if(MumuWeiboUtility.isContainBlockWords(weiboInfo.getWeiboUser().getName()+weiboInfo.getWeiboText()))
			return true;
		
		WeiboInfo retWeibo=weiboInfo.getRetweetWeiboInfo();
		if(retWeibo!=null && retWeibo.getWeiboUser()!=null && MumuWeiboUtility.isContainBlockWords(retWeibo.getWeiboUser().getName()+retWeibo.getWeiboText()))
				return true;
		}		
		return false;
	}
	
	//导入保存的微博列表,info可能为"public","comments","atmsg";
	public static void  ImportWeibosList(Context context,LIST_FLAG info)
	{
		String fileName=null;
		switch(info){
		case PUBLIC: fileName="PublicWeibosList.txt"; break;
		case COMMENTS: fileName="CommentsList.txt"; break;
		case ATMSG: fileName="AtMsgList.txt"; break;
		case MYWEIBOS:	fileName="MyWeibosList.txt"; break;
		default: return;
		}
		
		ArrayList<WeiboInfo> list=new ArrayList<WeiboInfo>();
		try {
			FileInputStream fis=context.openFileInput(fileName);
			
			try {
				ObjectInputStream ois=new ObjectInputStream(fis);
				try {
					list=(ArrayList<WeiboInfo>)ois.readObject();
					
				} 
				catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(ois!=null)ois.close();
				if(fis!=null)fis.close();
			} catch (StreamCorruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e1){
			//e1.printStackTrace();
			}
		
		switch(info){		
		case PUBLIC: MumuWeiboUtility.WeiboInfoList=list; break;
		case COMMENTS: MumuWeiboUtility.CommentsList=list; break;
		case ATMSG: MumuWeiboUtility.AtMsgList=list; break;
		case MYWEIBOS: MumuWeiboUtility.MyWeibosList=list; break;
		default: return;
		}		
	}
	public static void importUserInfo(Context context)
	{
		try {
			FileInputStream fis=context.openFileInput("UserInfo.dat");
			ObjectInputStream ois=new ObjectInputStream(fis);		
			LoginUser=(WeiboUserInfo)ois.readObject();		
			if(ois!=null)ois.close();
			if(fis!=null)fis.close();
			
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	//保存用户信息
	public static void saveUserInfo(Context context)
	{
		try {
			FileOutputStream fos=context.openFileOutput("UserInfo.dat", Context.MODE_PRIVATE);
			
				ObjectOutputStream oos=new ObjectOutputStream(fos);
				oos.writeObject(LoginUser);
				if(oos!=null)oos.close();
				if(fos!=null)fos.close();
			
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	//保存weibolist到本地，方便下次打开。
	public static void saveWeiboList(Context context,LIST_FLAG info)
	{
		String fileName=null;
		switch(info){
		case PUBLIC: fileName="PublicWeibosList.txt"; break;
		case COMMENTS: fileName="CommentsList.txt"; break;
		case ATMSG: fileName="AtMsgList.txt"; break;
		case MYWEIBOS: fileName="MyWeibosList.txt"; break;
		default: return;
		}
		
		try {
			FileOutputStream os=context.openFileOutput(fileName, Context.MODE_PRIVATE);
			try {
				ObjectOutputStream oos=new ObjectOutputStream(os);
				switch(info){
				case PUBLIC: oos.writeObject(MumuWeiboUtility.WeiboInfoList); break;
				case COMMENTS: oos.writeObject(MumuWeiboUtility.CommentsList);; break;
				case ATMSG: oos.writeObject(MumuWeiboUtility.AtMsgList);; break;
				case MYWEIBOS: oos.writeObject(MumuWeiboUtility.MyWeibosList);; break;
				
				default: return;
				}
				
				if(oos!=null)oos.close();
				if(os!=null)os.close();
				
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
			
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	//清除缓存中过期的图片文件(3天过期时间）
		public static void  clearCache(){		
			if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				return;	
			Log.i("---------","clear cache is begin!");
			final File cacheDir=new File(MumuWeiboUtility.fileCacheDir);
			if(!cacheDir.exists())
				cacheDir.mkdirs();
			new Thread(){
			public void run(){			
				File[] files=cacheDir.listFiles();
				for(int i=0;i<files.length;i++){
					File f=files[i];
					if(f.isDirectory())continue;
					if(System.currentTimeMillis()-f.lastModified()>3*24*60*60*1000)
						{f.delete();
						Log.i("file deleted",f.getName());
						}				
				}
				Log.i("---------","clear cache is over!");
			}
			}.start();
			
		}
		
	//save file to sdcard
	static void saveImage2Sdcard( final String url,final String savePath)
	{			
		final String filename=url.replace('/', '%').replace(':', '%');
				
		new Thread(){
			public void run()
			{
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				{			
					File dir=new File(savePath);
					if(!dir.exists())dir.mkdirs();
				
				File file=new File(savePath+'/'+filename);	
				BufferedOutputStream bos;
				try {
					bos = new BufferedOutputStream(new FileOutputStream(file));
				
				
				File fileCache=new File(fileCacheDir+'/'+filename);
				if(fileCache!=null && fileCache.exists())
				{
					BufferedInputStream bis=new BufferedInputStream(new FileInputStream(fileCache));
					byte[] buf=new byte[1024];
					int len;
					while((len=bis.read(buf))!=-1)
							bos.write(buf,0,len);
					if(bis!=null)bis.close();						
					if(bos!=null)bos.close();
					return;
				}	
				else
				{
					URL imageUrl=new URL(url);
					InputStream is=imageUrl.openStream();
					byte[] buf=new byte[1024];
					int len;
					while((len=is.read(buf))!=-1)
							bos.write(buf,0,len);
					if(is!=null)is.close();						
					if(bos!=null)bos.close();
				}				
				} 
				catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					
			}
			}
		}.start();				
   }
	 //从网络上下载图片
	 public static Drawable getImageFromUrl(String url,String savePath)
	 {
			//another get method
			URL imageUrl = null;
			Drawable draw=null;
			String filename=url.replace('/', '%').replace(':', '%');
			
			boolean flag=true;
		
			try {
				imageUrl = new URL(url);
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				{			
					File dir=new File(savePath);
					if(!dir.exists())dir.mkdirs();
				
					File file=new File(savePath+'/'+filename);	
					if(file.exists())return Drawable.createFromPath(file.getAbsolutePath());
					int i=3;
					while(i>0){
						i--;					
					try {					
						BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(file));
						InputStream is=imageUrl.openStream();
						//int len=is.available();
						//Log.i("len----len---len is:","`````"+len);
						byte[] buffer=new byte[4096];
						int len;
					while((len=is.read(buffer))!=-1)bos.write(buffer, 0, len);													
							
							bos.flush();
							if(is!=null)is.close();
							if(bos!=null)bos.close();	
							
							Drawable d=Drawable.createFromPath(file.getAbsolutePath());
							if(d!=null)return d;
							else
								file.delete();
					} 
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					}//end of while
					return null;
				}
				else//如果sdcard不可用
				{
					try {
						return Drawable.createFromStream(imageUrl.openStream(),null);
					} 
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}							
			} 
			catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.i("getImageByURl", "malformedURL");
			}
			return null;
	 }
			
	 //微博用户名蓝色显示
	public static void  formatWeiboUsername(SpannableStringBuilder sp,final Context context,final TextView tv,String s,boolean clickable)
	 {
		//SpannableStringBuilder sp=new SpannableStringBuilder(s);
		int start,end;
		 for(int i=0;i<s.length();){
			 start=s.indexOf('@',i);
			 if(start==-1)break;
			 end=indexOf(s, start+1);	
			 if(end==start+1)
				 {
				 	i=end;
				 	continue;
				 }
			// end++;
			 if(end<start)break;
			 String name=s.substring(start, end);
			 if(name.equals("@我"))
				 {
				 	i=end;
				 	continue;
				 }
			
			 if(clickable==false)
				 sp.setSpan(new ForegroundColorSpan(Color.BLUE), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			 else
			 {			 					
				 sp.setSpan(new URLSpan(name){
					@Override
					public void onClick(View widget) {
						// TODO Auto-generated method stub
						
						Intent in=new Intent();
						//getURL();
						in.setClass(context, UserInfoShow.class);
						in.putExtra("screen_name", getURL().substring(1));							
						context.startActivity(in);						
					}
					public void updateDrawState(TextPaint ds)
					{
						ds.setColor(Color.BLUE);
						ds.setUnderlineText(true);
					}					 
				 }, 
				 start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			 }
			 
			 i=end;
		 }
		// tv.setText(sp);
	 }
	
	//设置微博内容网页链接颜色及点击事件
	public static void formatWeiboUrl(SpannableStringBuilder sp,final Context context,final TextView tv,String s,boolean clickable)
	 {
		int start,end;
		 for(int i=0;i<s.length();i++)
		 {
			 start=s.indexOf("http://",i);
			 if(start==-1)break;
			 end=start+7;
			 if(end>=s.length())break;
			 char ch=s.charAt(end);	
			 
			 while(end<s.length())
			 {
				 ch=s.charAt(end);			
				 if( (ch>='a'&& ch<='z' || ch>='A' && ch<='Z' || ch>='0' && ch<='9'
						 || ch=='.' || ch=='/' || ch=='?' || ch=='&' || ch=='='
						 || ch=='_'))
					 end++;
				 else
				 break; 				 
			 }					
			 String url=s.substring(start, end);				 
			
			 if(clickable)
				 sp.setSpan(
					 //第一个参数
					 new URLSpan(url){
					 public void updateDrawState(TextPaint ds){
						 ds.setColor(Color.BLUE);
						 ds.setUnderlineText(true);
					 }
				},
				//其他参数
			   start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		 else
			   sp.setSpan(new ForegroundColorSpan(Color.BLUE), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			 i=end;
		 }
		// tv.setText(sp);
	 }
	
	 
	 //格式化微博，如特殊颜色显示“话题”，用户名，链接等
	 public static void  FormatWeibo(final Context context,final TextView tv,String s,boolean clickable)
	 {
		 SpannableStringBuilder sp=new SpannableStringBuilder(s);
		 int start,end;
		 
		 //微博用户名蓝色显示
		formatWeiboUsername(sp,context,tv,s,clickable);
		 
		 //网页链接
		formatWeiboUrl(sp,context,tv,s,clickable);
		 
		 //话题黄色显示
		 for(int i=0;i<s.length();){
			 start=s.indexOf('#',i);
			 if(start==-1)break;
			 end=s.indexOf('#',start+1);
			 end++;
			 if(end<=start)break;
			 sp.setSpan(new ForegroundColorSpan(Color.MAGENTA), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			 i=end;
		 }
		 
		 tv.setText(sp);
		 
		 //表情设置
		 formatWeiboEmotion2(sp,context,tv,s,clickable);		
	}
	 
	 //设置微博表情显示方法一(推荐)：如果表情图片不存在，可以根据表情名下载表情，完成后立即显示表情。
	 public static void formatWeiboEmotion2(SpannableStringBuilder sp,final Context context,final TextView tv,String s,boolean clickable)
	 {
		int start;
		int end;
		 for(int i=0;i<s.length();)
		 {
			 end=s.indexOf(']', i);
			 if(end==-1)break;
			 start=s.substring(0, end).lastIndexOf('[');
			 if(start==-1){i=end+1; continue;}
			 
			 end++;
			 String emotionName=s.substring(start,end);
			 final AsyncBitmapLoader async=new AsyncBitmapLoader();
			 async.setEmotion(tv, sp, emotionName, start, end);
			 				 
			 i=end;			 
		 }//表情设置结束		 	 
		
	 }
	 
	 //设置微博表情显示方法一：如果表情图片不存在，可以根据表情名下载表情，完成后再下一次会显示该表情。
	 public static void formatWeiboEmotion(SpannableStringBuilder sp,final Context context,final TextView tv,String s,boolean clickable)
	 {
		 int start,end;
		 for(int i=0;i<s.length();)
		 {			
			 end=s.indexOf(']', i);
			 if(end==-1)break;
			 start=s.substring(0, end).lastIndexOf('[');
			 if(start==-1){i=end+1; continue;}
			 
			 end++;
			 String emotionName=s.substring(start,end);
			 AsyncBitmapLoader async=new AsyncBitmapLoader();
			 Drawable bp=async.loadEmotion(emotionName);			
			 
			 if(bp==null)
			 {
				 i=end; continue;
			 }
			
			 String imageURL=MumuWeiboUtility.emotionMapList.get(emotionName);
			 if(imageURL==null)continue;
			 String filePath=MumuWeiboUtility.emotionSaveDir+imageURL.replace('/', '%').replace(':', '%');  	        
		     File file=new File(filePath); 
			 if(file.exists()){
				 try{
				 InputStream is=new FileInputStream(file);
							       
			        AnimatedGifDrawable dd=new AnimatedGifDrawable(is, new AnimatedGifDrawable.UpdateListener() {   
			            @Override
			            public void update() {
			                tv.postInvalidate();
			            }
			        });
			       
			        AnimatedImageSpan as=new AnimatedImageSpan(dd);
			        
			        sp.setSpan(as, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			       tv.setText(sp);
			        if(is!=null)	
							is.close();
				 }
				 catch(FileNotFoundException e){}
				 catch(IOException e){}
						
		        }
			 else continue;
			 				 
			 i=end;			 
		 }//表情设置结束		 	 
		
		 tv.setText(sp);
	 }
	 
	 public static Bitmap zoomBitmap(Bitmap bitmap,int w,int h){
		 int width = bitmap.getWidth();
		 int height = bitmap.getHeight();
		 Matrix matrix = new Matrix();
		 float scaleWidht = ((float)w / width);
		 float scaleHeight = ((float)h / height);
		 matrix.postScale(scaleWidht, scaleHeight);
		 Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,
		 true);
		 return newbmp;
		 }
	 
	 //返回从start开始的包含在数组字符chars的最小索引
	 private static int indexOf(String s,int start){
		 
		 //分隔符数组
		 char[] chars={'@',' ',' ','　',',','.','。','，',':','：','?','？',
				 		'[','(','（',')','）','~','~','、','!','！',
				 		'\'','\"','‘','“','\\','/','#','$',
				 		'&','=','%','^','*','】','【',';','；','》','《'};
		 
		 for(int i=start;i<s.length();i++)
		 {
			 for(int j=0;j<chars.length;j++)
				 if(s.charAt(i)==chars[j])return i;			 
		 }
		 return s.length();
	 }
	 
	 //转换时间为09-11  16:13格式
	 public static String parseWeiboTime(String gmtDatetime){
		 //注意参数H代表0-23，h代表0-11
         SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzzz yyyy", Locale.ENGLISH);   
         
        java.util.Date dd;
	        try {
             dd = df.parse(gmtDatetime);
            
             
             //如果时间短于半小时则显示多少分钟前
             long create=dd.getTime();
             long interval=System.currentTimeMillis()-create;
             
             long minutes=interval/1000/60l;
            if(minutes<30)
            	 return minutes+1+"分钟前";             
             
             //more待补
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String todayStr = format.format(date);
            //得到今天零时零分零秒这一时刻
            Date today = format.parse(todayStr);
            
            long ins=create-today.getTime();
            if(ins>0 && ins<86400000){
            	 SimpleDateFormat sdf1 = new SimpleDateFormat("今天 HH:mm");
            	 sdf1.setTimeZone(TimeZone.getDefault());
            	 return sdf1.format(dd);
            }
            
	                 SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日  HH:mm");    
	                 
	                 sdf.setTimeZone(TimeZone.getDefault());
	                 return sdf.format(dd);
	        } catch (ParseException e) {
	                e.printStackTrace();	               
	        }	
	        return gmtDatetime;
	}
}
