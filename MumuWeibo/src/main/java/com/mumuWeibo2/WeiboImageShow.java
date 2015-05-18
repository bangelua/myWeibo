package com.mumuWeibo2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.mumuWeibo2.GifHelper.GifFrame;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class WeiboImageShow extends Activity{
	
	private ImageView iv;
	private Bitmap bp;
	private Button btDownload;
	private Button btReturn;
	private AsyncBitmapLoader asyncBitmapLoader=new AsyncBitmapLoader();
	private int IMAGE_RECEIVED=1000;
	private CircleProgressBar pd;
	
	
	String imageUrlOriginal=null;
	
	private Handler handler=new Handler(){
		public void handleMessage(Message msg)
		{

//			pd.setVisibility(View.GONE);
			if(imageUrlOriginal!=null && !imageUrlOriginal.endsWith(".gif"))
			{
				iv.setImageDrawable((Drawable) msg.obj);
				return;
			}
			
		String filePath=MumuWeiboUtility.fileCacheDir+imageUrlOriginal.replace('/', '%').replace(':', '%');  	        
	        final File f=new File(filePath);			       
	    if(f.exists()){	   
		 InputStream is=null;
		try {
			is = new FileInputStream(f);
			 final GifFrame[] frames = CommonUtil.getGif(is);
		        
		        mGifTask = new PlayGifTask(iv, frames);
		        mGifTask.start();
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
	   }
			
			
			
			
		}
	};
	
	  private PlayGifTask mGifTask=null;

	    private static class PlayGifTask implements Runnable {
	        int i = 0;
	        ImageView iv;
	        GifFrame[] frames;

	        public PlayGifTask(ImageView iv, GifFrame[] frames) {
	            this.iv = iv;
	            this.frames = frames;
	        }

	        @Override
	        public void run() {
	        	if(frames==null || i>=frames.length)return;
	        	
	            if (!frames[i].image.isRecycled()) {
	                iv.setImageBitmap(frames[i].image);
	            }
	            iv.postDelayed(this, frames[i++].delay);
	            i %= frames.length;
	        }
	        
	        public void start() {
	            iv.post(this);
	        }
	        
	        public void stop() {
	            if(null != iv) iv.removeCallbacks(this);
	            iv = null;
	            if(null != frames) {
	                for(GifFrame frame : frames) {
	                    if(frame.image != null && !frame.image.isRecycled()) {
	                        frame.image.recycle();
	                        frame.image = null;
	                    }
	                }
	                frames = null;
	            }
	        }
	    }


	@Override
	public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
		super.onCreate(savedInstanceState, persistentState);
	}

	public void onCreate(Bundle bb)
	{
		super.onCreate(bb);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weibo_image_show);
		pd = (CircleProgressBar)findViewById(R.id.progressBar);
		
		MumuWeiboUtility.isSeized=true;
		
		iv=(ImageView)this.findViewById(R.id.weibo_image_show);
		//iv.setScaleType(ImageView.ScaleType.MATRIX);
		
		//iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
		iv.setOnTouchListener(new MulitPointTouchListener());
		
		btDownload=(Button)findViewById(R.id.bt_download_image);
		btDownload.setOnClickListener(lis);
		
		btReturn=(Button)findViewById(R.id.bt_return_in_imageshow);
		btReturn.setOnClickListener(lis);
				
		Intent i=this.getIntent();
		Bundle info=i.getExtras();
		
		String imageUrlSmall="";
		if(info!=null){
			imageUrlSmall=info.getString("IMAGE_URL_SMALL");			
			imageUrlOriginal=info.getString("IMAGE_URL_ORIGINAL");
			
			if(imageUrlOriginal==null)return;
		        String filePath=MumuWeiboUtility.fileCacheDir+imageUrlOriginal.replace('/', '%').replace(':', '%');  	        
		        final File f=new File(filePath);
		       
		       
		        if(f.exists()){	    
		        		f.setLastModified(System.currentTimeMillis());
		        		
		        			Drawable bp=Drawable.createFromPath(f.getAbsolutePath());
		        			if(null!=bp){
		        			Message msg=handler.obtainMessage(0, bp);
		        			handler.sendMessage(msg);
		        			return;
		        			}		        		
		        }
			
			pd.setVisibility(View.VISIBLE);
			new Thread(){
				  public void run(){
					  Drawable d=MumuWeiboUtility.getImageFromUrl(imageUrlOriginal, MumuWeiboUtility.fileCacheDir);
					  Message msg=handler.obtainMessage(0,d);
					  handler.sendMessage(msg);
				  }
			  }.start();			
			//asyncBitmapLoader.loadBitmap(iv, imageUrlOriginal,pd);
		
		}		
	}
	
	private OnClickListener lis=new OnClickListener(){
		public void onClick(View v){
			if(v==btDownload)
			{
				 //先查看sdcard是否挂载
			      if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) 
			      {
			    	  Toast.makeText(getApplicationContext(), "sdcard不可用", Toast.LENGTH_LONG).show();
			    	  return;
			      }
			      
			      //查看文件是否已保存
			      String filePath=MumuWeiboUtility.fileCacheDir+imageUrlOriginal.replace('/', '%').replace(':', '%');  	        
			      File savePath=new File(MumuWeiboUtility.imageSaveDir);
				  if(!savePath.exists())savePath.mkdirs();
				  String imageSavePath=MumuWeiboUtility.imageSaveDir+filePath.substring(
						  filePath.lastIndexOf('%')+1);					  
				  File saveImage=new File(imageSavePath);
				  if(saveImage.exists())
				  {
					  Toast.makeText(getApplicationContext(), "图片已保存", Toast.LENGTH_LONG).show();
					  return;
				  }
				  
				  
			      //查找缓存
			      File imageSource=new File(filePath);	
				  if(!imageSource.exists())
				  {
//					  pd.setVisibility(View.GONE);
					  //asyncBitmapLoader.loadBitmap(iv, imageUrlOriginal,pd);
					  //图片下载
					  new Thread(){
						  public void run(){
							  Drawable d=MumuWeiboUtility.getImageFromUrl(imageUrlOriginal, MumuWeiboUtility.imageSaveDir);
							  Message msg=handler.obtainMessage(0,d);
							  handler.sendMessage(msg);
						  }
					  }.start();
				  }
				  else
				  {
					  //先从缓存中查找					 
					  
						try {
							// if(!saveImage.exists())saveImage.createNewFile();
							BufferedInputStream bis=
									new BufferedInputStream(new FileInputStream(imageSource));
							  BufferedOutputStream bos=
									  new BufferedOutputStream(new FileOutputStream(imageSavePath));
							  int length=bis.available();
							  byte[] content=new byte[length];
							  bis.read(content, 0, length);
							  bos.write(content, 0, length);
							  
							  if(bis!=null)bis.close();
							  if(bos!=null)bos.close();
							  Log.i("image show","image has saved to mumu");
							  Toast.makeText(getApplicationContext(), "文件已保存到目录："+
							 MumuWeiboUtility.imageSaveDir,
									  Toast.LENGTH_SHORT).show();
						} 
						catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						catch(IOException e){
							e.printStackTrace();
						}				 
				    
			      }//endif
				  
			}//endif btDownload
			else if(v==btReturn)
			{
				finish();
			}
			
		}
	};
	
	protected void onDestroy() {
        super.onDestroy();
        if(null != mGifTask) mGifTask.stop();
    }

}
	
