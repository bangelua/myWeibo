package com.mumuWeibo2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mumuWeibo2.GifHelper.GifFrame;

public class AsyncBitmapLoader {		
	    /** 
	     * 内存图片软引用缓冲 
	     */  
	    private HashMap<String, SoftReference<Drawable>> imageCache00 = null;  
	    private ProgressDialog pd=null;
	  
	    public AsyncBitmapLoader()  
	    {  
	        imageCache00=MumuWeiboUtility.getCache();  	        
	    }  
	    
	    //设置view中的表情
	    public synchronized void setEmotion(final TextView tv,final SpannableStringBuilder sp,final String emotionName,final int start,final int end)
	    {			
			 final String imageURL=MumuWeiboUtility.emotionMapList.get(emotionName);
			 if(imageURL==null)return;
			 final String filePath=MumuWeiboUtility.emotionSaveDir+imageURL.replace('/', '%').replace(':', '%');  	        
		     final File file=new File(filePath); 
			 if(file.exists()){
				 	setEmotionByFile(file,tv,sp,start,end);
		        }
			 else {
				 //表情不存在，网络下载，然后再设置					
				   final Handler handler0 = new Handler()  
			        {  	           
			            @Override  
			            public void handleMessage(Message msg)  
			            {  
			                // TODO Auto-generated method stub  
			            	if(msg.what==100)
			            		setEmotionByFile(file,tv,sp,start,end);
			            }  
			        };  
				
				new Thread(){
					public void run()
					{
						 Drawable bp=MumuWeiboUtility.getImageFromUrl(imageURL,MumuWeiboUtility.emotionSaveDir); 
						 if(bp!=null)
							 {							 	
							 	handler0.obtainMessage(100).sendToTarget();
							 	bp=null;
							 }
						 ///final File file1=new File(filePath);
						 //setEmotionByFile(file1,tv,sp,start,end);
					}
				}.start();				 
			 } 	    	
	    }
	    
	    
	    
	    public void setEmotionByFile(File file,final TextView tv,SpannableStringBuilder sp,int start,int end)
	    {
	    	 try{
	    		 Drawable d=Drawable.createFromPath(file.getAbsolutePath());
	    		 if(d==null)return;
	    		 
				 InputStream is=new FileInputStream(file);
			        AnimatedGifDrawable dd=new AnimatedGifDrawable(is,   tv.getLineHeight(), new AnimatedGifDrawable.UpdateListener() {
			            @Override
			            public void update() {
			                tv.postInvalidate();
			            }
			        });
			     
			        AnimatedImageSpan as=new AnimatedImageSpan(dd);
			        sp.setSpan(as, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			       tv.setText(sp);
			       as=null;
			       dd=null;
			        if(is!=null)	
							is.close();
				 }
				 catch(FileNotFoundException e){}
				 catch(IOException e){}
	    	 	 catch(Exception e){return;}
	    	 
						
	    }
	    	    
	    //*********************下载表情
	    public Drawable loadEmotion(final String emotionName)
	    {
	    	final String imageURL=MumuWeiboUtility.emotionMapList.get(emotionName);
	    	
	    	if(imageURL==null)return null;
	    	
	    	if(imageCache00.containsKey(imageURL))  
		        {  
		            SoftReference<Drawable> reference = imageCache00.get(imageURL);  
		            Drawable bitmap = reference.get();  
		            if(bitmap != null)  
		            {  	 		            	
		            	return bitmap;	               
		            }  
		        }  		    
		            
		        String filePath=MumuWeiboUtility.emotionSaveDir+imageURL.replace('/', '%').replace(':', '%');  	        
		        if(new File(filePath).exists()){
		        	
						Drawable bp=Drawable.createFromPath(filePath);
						if(bp!=null)
							{							
								imageCache00.put(imageURL, new SoftReference<Drawable>(bp));																				
								return bp;
							}	
		        }	 
		       
		        //如果不在内存缓存中，也不在本地（被jvm回收掉），则开启线程下载图片 	     	        
		        new Thread()  
		        {  		            
		            @Override  
		            public void run()  
		            {  
		                Drawable bitmap = MumuWeiboUtility.getImageFromUrl(imageURL,MumuWeiboUtility.emotionSaveDir);  	 
		                if(bitmap!=null)
		                imageCache00.put(imageURL, new SoftReference<Drawable>(bitmap)); 			               
		            }  
		        }.start();  	  
		   
	    	return null;
	    }
	      
	    //**********************************************************
	    public void loadBitmap(final ImageView imageView, final MumuWeiboUtility.IMAGE_TYPE type,final String imageURL)  
	    {  
	        //在内存缓存中，则返回Bitmap对象  	 	    	
	    	
	        if( imageCache00.containsKey(imageURL))  
	        {  
	            SoftReference<Drawable> reference = imageCache00.get(imageURL);  
	            Drawable bitmap = reference.get();  
	            if(bitmap != null)  
	            {  	            	
	            	imageView.setImageDrawable(bitmap);	            	
	            	return;	               
	            }  
	        }  
	    
	            
	        String filePath=MumuWeiboUtility.fileCacheDir+imageURL.replace('/', '%').replace(':', '%');  	        
	        final File f=new File(filePath);
	       
	        if(f.exists()){	    
	        		f.setLastModified(System.currentTimeMillis());
	        		
	        			Drawable bp=Drawable.createFromPath(f.getAbsolutePath());
	        			if(bp!=null)
						{
							imageCache00.put(imageURL, new SoftReference<Drawable>(bp));
							imageView.setImageDrawable(bp);														
							return;
						}	
	        		
	        }	        
	        
	        //在图片下载前，为了避免图片混乱，先将图片设置为默认图片
	        if(type==MumuWeiboUtility.IMAGE_TYPE.PIC) imageView.setImageResource(R.drawable.default_image_wait);
	        
	        else if(type==MumuWeiboUtility.IMAGE_TYPE.PROFILE)imageView.setImageResource(R.drawable.defalut_profile_image);
	        	          
	        final Handler handler0 = new Handler()  
	        {  	           
	            @Override  
	            public void handleMessage(Message msg)  
	            {  
	                // TODO Auto-generated method stub  
	            	Drawable bp=(Drawable)msg.obj;
	            	imageView.setImageDrawable(bp);
	            	imageCache00.put(imageURL, new SoftReference<Drawable>(bp));	            	
	            }  
	        };  
	        
	      
	          
	        //如果不在内存缓存中，也不在本地（被jvm回收掉），则开启线程下载图片 	     	        
	        new Thread()  
	        {  	            
	            @Override  
	            public void run()  
	            {  
	                // TODO Auto-generated method stub  	             
	                Drawable bitmap = MumuWeiboUtility.getImageFromUrl(imageURL,MumuWeiboUtility.fileCacheDir);  	                 
	                Message msg = handler0.obtainMessage(0, bitmap);  
	                handler0.sendMessage(msg);  
	                imageCache00.put(imageURL, new SoftReference<Drawable>(bitmap)); 
	                //MumuWeiboUtility.saveImage2Sdcard(imageURL, bitmap,MumuWeiboUtility.fileCacheDir);
	           
	            }  
	        }.start();  	      
	    }  	      	
	    
	    
	    //*******************************************
	    //该函数重载了，为了适应WeiboImageShow中的图片显示
	    public void loadBitmap(final ImageView imageView, final String imageURL, final ProgressDialog pd)  
	    {  	    		    	
	        //在内存缓存中，则返回Bitmap对象  	    
	    	
	        if(imageCache00.containsKey(imageURL))  
	        {  
	            SoftReference<Drawable> reference = imageCache00.get(imageURL);  
	            Drawable bitmap = reference.get();  
	            if(bitmap != null)  
	            {  	            	
	            	imageView.setImageDrawable(bitmap);
	            	pd.dismiss();
	            	return;	               
	            }  
	        }  
	    
	            
	        // String filePath=MumuWeiboUtility.hasImageCache(imageURL);
	        //查找图片是否在sdcard中是个耗时的过程，不查找，直接解码，解码成功则代表sdcard中包含该图片，不成功则网络下载。
	        String filePath=MumuWeiboUtility.fileCacheDir+imageURL.replace('/', '%').replace(':', '%');
	        
	        File f=new File(filePath);
	        if(f.exists()){	       
	        		f.setLastModified(System.currentTimeMillis());
	        		Drawable bp=Drawable.createFromPath(filePath);
					if(bp!=null)
						{
							imageCache00.put(imageURL, new SoftReference<Drawable>(bp));
							imageView.setImageDrawable(bp);							
							pd.dismiss();
							return;
						}	
	        }
	       
	        
				
	        final Handler handler0 = new Handler()  
	        {  	           
	            @Override  
	            public void handleMessage(Message msg)  
	            {  
	                // TODO Auto-generated method stub  
	            	imageView.setImageDrawable((Drawable)msg.obj);
	            	Drawable d;	            	
	            	pd.dismiss();
	               // imageCallBack.imageLoad(imageView, (Bitmap)msg.obj);  
	            }  
	        };  
	          
	        //如果不在内存缓存中，也不在本地（被jvm回收掉），则开启线程下载图片 
	     	        
	        new Thread()  
	        {  
	        	 @Override  
		            public void run()  
		            {  
		                // TODO Auto-generated method stub  	      
		                  
	        		 Drawable bitmap = MumuWeiboUtility.getImageFromUrl(imageURL,MumuWeiboUtility.fileCacheDir);  	                 
		                Message msg = handler0.obtainMessage(0, bitmap);  
		                handler0.sendMessage(msg);  
		                imageCache00.put(imageURL, new SoftReference<Drawable>(bitmap)); 
		              //  MumuWeiboUtility.saveImage2Sdcard(imageURL, bitmap,MumuWeiboUtility.fileCacheDir);
		           
		            }  
		        }.start();  	      
		    }  	      	
	}  

