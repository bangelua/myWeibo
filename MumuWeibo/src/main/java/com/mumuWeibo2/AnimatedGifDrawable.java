package com.mumuWeibo2;

import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class AnimatedGifDrawable extends AnimationDrawable {

    private int mCurrentIndex = 0;
    private UpdateListener mListener;

    public AnimatedGifDrawable(InputStream source, UpdateListener listener) {
        mListener = listener;
      //  GifDecoder decoder = new GifDecoder();
        GifHelper decoder = new GifHelper();
        decoder.read(source);

        // Iterate through the gif frames, add each as animation frame
        for (int i = 0; i < decoder.getFrameCount(); i++) {
         // Bitmap bitmap = decoder.getFrame(i);
        	
        	
        	 Bitmap bm = decoder.getFrame(i);
        	 
        	 //璁剧疆bitmap澶у皬-------------
            int width = bm.getWidth();
            int height = bm.getHeight();
            // 璁剧疆鎯宠鐨勫ぇ灏�
            int newWidth = 35;
            int newHeight = 35;
            // 璁＄畻缂╂斁姣斾緥
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            // 鍙栧緱鎯宠缂╂斁鐨刴atrix鍙傛暟
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            // 寰楀埌鏂扮殑鍥剧墖
            Bitmap bitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
              true);
           //璁剧疆瀹屾瘯 
          
           // bitmap.get
            
           
           
            BitmapDrawable drawable = new BitmapDrawable(bitmap);
            // Explicitly set the bounds in order for the frames to display
            drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
            
          
            
            addFrame(drawable, decoder.getDelay(i));
            if (i == 0) {
                // Also set the bounds for this container drawable
                setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
            }
        }
    }
    
    /**
     * added by bangelua
     * @param source
     * @param picSize gif的宽高值，目前仅用于显示表情gif
     * @param listener
     */
    public AnimatedGifDrawable(InputStream source, int picSize, UpdateListener listener) {
        mListener = listener;
      //  GifDecoder decoder = new GifDecoder();
        GifHelper decoder = new GifHelper();
        decoder.read(source);

        // Iterate through the gif frames, add each as animation frame
        for (int i = 0; i < decoder.getFrameCount(); i++) {
         // Bitmap bitmap = decoder.getFrame(i);
        	
        	
        	 Bitmap bm = decoder.getFrame(i);
        	 
        	 //璁剧疆bitmap澶у皬-------------
            int width = bm.getWidth();
            int height = bm.getHeight();
            // 璁剧疆鎯宠鐨勫ぇ灏�
            int newWidth = 35;
            int newHeight = 35;
            // 璁＄畻缂╂斁姣斾緥
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            // 鍙栧緱鎯宠缂╂斁鐨刴atrix鍙傛暟
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            // 寰楀埌鏂扮殑鍥剧墖
            Bitmap bitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
              true);
           //璁剧疆瀹屾瘯 
          
           // bitmap.get
            
           
           
            BitmapDrawable drawable = new BitmapDrawable(bitmap);
            // Explicitly set the bounds in order for the frames to display
            drawable.setBounds(0, 0, picSize, picSize);
            
          
            
            addFrame(drawable, decoder.getDelay(i));
            if (i == 0) {
                // Also set the bounds for this container drawable
                setBounds(0, 0, picSize, picSize);
            }
        }
    }

    /**
     * Naive method to proceed to next frame. Also notifies listener.
     */
    public void nextFrame() {
        mCurrentIndex = (mCurrentIndex + 1) % getNumberOfFrames();
        if (mListener != null) mListener.update();
    }

    /**
     * Return display duration for current frame
     */
    public int getFrameDuration() {
        return getDuration(mCurrentIndex);
    }

    /**
     * Return drawable for current frame
     */
    public Drawable getDrawable() {
        return getFrame(mCurrentIndex);
    }

    /**
     * Interface to notify listener to update/redraw 
     * Can't figure out how to invalidate the drawable (or span in which it sits) itself to force redraw
     */
    public interface UpdateListener {
        void update();
    }

}
