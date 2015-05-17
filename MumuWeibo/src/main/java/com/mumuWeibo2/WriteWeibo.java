package com.mumuWeibo2;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.net.RequestListener;

public class WriteWeibo extends Activity{
	
	private Button btSend;
	private ImageButton ivFace;
	private ImageButton ivPic;
	private EditText etWeiboContent;
	private ProgressDialog pd=null;
	private ImageView ivShowImage;
	GridView gv;
	public int RESULT_PIC_LOADED=1000;
	private String picPath=null;
	
private Handler handler=new Handler();
	
	public void showToast(final String s)
	{
		handler.post(new Runnable() {

			   @Override
			   public void run() {
			    Toast.makeText(getApplicationContext(), s,
			      Toast.LENGTH_SHORT).show();
			   }
			  });
	}
	
	public void onCreate(Bundle bb){		
		super.onCreate(bb);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getWindow().setSoftInputMode(   
        //        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		setContentView(R.layout.write_weibo_layout);
		
		btSend=(Button)findViewById(R.id.bt_send_weibo_in_write);	
		etWeiboContent=(EditText)findViewById(R.id.edit_write_content);
		ivFace=(ImageButton)findViewById(R.id.iv_face_list);
		ivPic=(ImageButton)findViewById(R.id.iv_get_pic);
		ivShowImage=(ImageView)findViewById(R.id.iv_showpic);
		btSend.setOnClickListener(lis);		
		ivFace.setOnClickListener(lis);	
		ivPic.setOnClickListener(lis);	
		ivShowImage.setOnClickListener(lis);
	}
	
	
	private OnClickListener lis=new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v==btSend)
			{
				sendWeibo();
			}
			else if(v==ivFace){
				new FaceDialog(WriteWeibo.this,etWeiboContent);
			}
			else if(v==ivPic)
			{
				Intent intent=new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(intent,RESULT_PIC_LOADED);				
			}
			else if(v==ivShowImage)
			{
				 String[] mList={"删除图片","更换图片"};
				 					 
				        AlertDialog.Builder listDia=new AlertDialog.Builder(WriteWeibo.this);
				        listDia.setTitle(null);
				        listDia.setItems(mList, new DialogInterface.OnClickListener() {
				            
				            @Override
				            public void onClick(DialogInterface dialog, int which) {
				                // TODO Auto-generated method stub
				                /*下标是从0开始的*/	            	
				               switch(which){	 	               
				               case 0:  //delete picture
				            	   picPath=null;
				            	   ivShowImage.setVisibility(View.GONE); 
				            	   etWeiboContent.setHint("");
				            	   break;
				   				
				               case 1://change image
				            	   Intent intent=new Intent(Intent.ACTION_PICK,
				            	android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				   				startActivityForResult(intent,RESULT_PIC_LOADED);	
				              default:
							   	//Toast.makeText(CommentListShow.this, "操作尚未设置", Toast.LENGTH_SHORT).show();				    		
							  }
				            }
				        });
				        listDia.create().show();	
			}
		}		
	};
	
	
	protected void onActivityResult(int requstCode,int resultCode,Intent data)
	{
		if(requstCode== RESULT_PIC_LOADED && resultCode == RESULT_OK && null !=data)
		{
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			 Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
			 cursor.moveToFirst();
			 int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			 picPath = cursor.getString(columnIndex);
			 cursor.close();
			etWeiboContent.setHint("分享图片");
			 
			Bitmap bp=null;
			bp=BitmapFactory.decodeFile(picPath);
			ivShowImage.setImageBitmap(bp);
			ivShowImage.setVisibility(View.VISIBLE);		
			 
		}
			
	}
	
	
	private void  sendWeibo()
	{
		String s=etWeiboContent.getText().toString();
				
		pd=ProgressDialog.show(WriteWeibo.this, null, "微博发送中...");
		pd.setCancelable(true);
		
		StatusesAPI api=new StatusesAPI(AccessTokenKeeper.readAccessToken(MumuWeiboUtility.context));
		if(null==picPath && s.trim().equals(""))
		{
			if(pd!=null)pd.dismiss();
			Toast.makeText(getApplicationContext(), "发送内容不能为空！", Toast.LENGTH_LONG).show();
			return;
		}
		
		if(picPath==null)
			api.update(s, String.valueOf(0.0), String.valueOf(0.0), new SendWeiboListener());
		else
		{
			if(s.equals(""))s="分享图片";
			api.upload(s, picPath, String.valueOf(0.0),String.valueOf(0.0), new SendWeiboListener());
		}
	}
	
	class SendWeiboListener implements RequestListener{

		@Override
		public void onComplete(String arg0) {
			// TODO Auto-generated method stub
			if(pd!=null)pd.dismiss();
			showToast("发送成功。");
			finish();
			
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
			showToast("发送失败！");						
		}
		
	}
	
}
