package com.mumuWeibo2;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

public class BlockWords extends Activity{
	
	private ListView lv;
	private ImageView Iv_Add;
	private ArrayList<String> data=new ArrayList<String>();
	
	public void onCreate(Bundle bb)
	{
		super.onCreate(bb);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.block_words);	
		
		
		lv=(ListView)findViewById(R.id.block_lv);
		lv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				AlertDialog.Builder dialog=new AlertDialog.Builder(BlockWords.this);
				dialog.setMessage("删除该关键字?");
				dialog.setTitle(null);
				dialog.setPositiveButton("删除", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						MumuWeiboUtility.BlockWordsList.remove(arg2);
						MumuWeiboUtility.saveBlockWordsList();
						
						checkCacheIsHaveBlockWords();
						
						BlockWordsListAdatper adapter=new BlockWordsListAdatper(BlockWords.this,MumuWeiboUtility.BlockWordsList);
						lv.setAdapter(adapter);
						
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
			
		});
		
		Iv_Add=(ImageView)findViewById(R.id.add_words);
		
		
		Iv_Add.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				addWords();
			}
			
		});	
						
		BlockWordsListAdatper adapter=new BlockWordsListAdatper(BlockWords.this,MumuWeiboUtility.BlockWordsList);
				
		lv.setAdapter(adapter);
		
	}
	
	public void addWords(){
		 //显示添加关键字对话框
	  
	    	AlertDialog.Builder dialog=new AlertDialog.Builder(BlockWords.this);
	    	final View dialog_view=getLayoutInflater().from(getApplicationContext()).
	    					inflate(R.layout.add_words_dialog, null);
	    	dialog.setView(dialog_view);
	    	dialog.setTitle(null);
	    	//EditText etUser=(EditText)dialog_view.findViewById(R.id.et_search_user_name);
	    		
	    	dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
	    	
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					EditText Et_keywords=(EditText)dialog_view.findViewById(R.id.et_input_keywords);
			    	//etUser.setFocusable(true);
					String word=Et_keywords.getText().toString();
					MumuWeiboUtility.BlockWordsList.add(word);
					MumuWeiboUtility.saveBlockWordsList();
					checkCacheIsHaveBlockWords();
					BlockWordsListAdatper adapter=new BlockWordsListAdatper(BlockWords.this,MumuWeiboUtility.BlockWordsList);
					lv.setAdapter(adapter);
					
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
	
	//检查缓存中是否含有要屏蔽的关键字
	public static void checkCacheIsHaveBlockWords()
	{
		for(int i=0;i<MumuWeiboUtility.WeiboInfoList.size();i++)
		{
			if(MumuWeiboUtility.isContainBlockWords(MumuWeiboUtility.WeiboInfoList.get(i)))
				MumuWeiboUtility.WeiboInfoList.remove(i);
		}
		
		for(int i=0;i<MumuWeiboUtility.CommentsList.size();i++)
		{
			if(MumuWeiboUtility.isContainBlockWords(MumuWeiboUtility.CommentsList.get(i)))
				MumuWeiboUtility.CommentsList.remove(i);
		}
		
		for(int i=0;i<MumuWeiboUtility.AtMsgList.size();i++)
		{
			if(MumuWeiboUtility.isContainBlockWords(MumuWeiboUtility.AtMsgList.get(i)))
				MumuWeiboUtility.AtMsgList.remove(i);
		}		
		
	}
	

}
