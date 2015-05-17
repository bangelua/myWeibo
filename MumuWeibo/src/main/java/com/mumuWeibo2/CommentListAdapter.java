package com.mumuWeibo2;

import java.util.ArrayList;

import android.content.Context;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


//评论列表适配器
public class CommentListAdapter extends BaseAdapter{
	
	private Context context;
	private ArrayList<WeiboInfo> list;
	private String username;
	
	public CommentListAdapter(Context context,ArrayList list1,String name)
	{
		this.context=context;
		list=list1;
		username=name;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		ViewHolder holder=null;
		
		if(convertView==null)
		{
			holder=new ViewHolder();
			convertView=LayoutInflater.from(context).inflate(R.layout.comment_list_item,null);
			
			holder.userName=(TextView)convertView.findViewById(R.id.username_in_comment_list_item);
			holder.createTime=(TextView)convertView.findViewById(R.id.createtime_in_comment_list_item);
			holder.commentText=(TextView)convertView.findViewById(R.id.comment_text_in_list_item);			
			convertView.setTag(holder);
		}
		else
			holder=(ViewHolder) convertView.getTag();
		
		WeiboInfo weiboInfo=new WeiboInfo();
		weiboInfo=list.get(pos);	
		
		String name=weiboInfo.getWeiboUser().getName();
		String s=name;
		if(name.getBytes().length>36){
			s=name.substring(0, 12)+"...";
		}
		
		holder.userName.setText("@"+s);
		holder.createTime.setText(MumuWeiboUtility.parseWeiboTime(weiboInfo.getCreateTime()));
		
		String comment=weiboInfo.getWeiboText();
		int index=comment.indexOf("//@"+username);
		if(index==0)comment="转发微博";
		else if(index>0)comment=comment.substring(0, index);
		
		MumuWeiboUtility.FormatWeibo(context,holder.commentText,comment, true);
		
			
		
		return convertView;
	}
	
	private static class ViewHolder{
		TextView userName;
		TextView createTime;
		TextView commentText;
		public ViewHolder(){
			
		}		
	}	
}
