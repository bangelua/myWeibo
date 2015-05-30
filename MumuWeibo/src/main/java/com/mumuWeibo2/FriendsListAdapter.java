package com.mumuWeibo2;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FriendsListAdapter extends BaseAdapter{
	
	private Context context;
	ArrayList<WeiboUserInfo> userList;
	private AsyncBitmapLoader asyncBitmapLoader=new AsyncBitmapLoader();
	
	
	
	
	
	public FriendsListAdapter(Context context,ArrayList<WeiboUserInfo> list)
	{
		this.context=context;
		userList=list;		
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return userList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int arg0, View convertView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		ViewHolder holder=null;
		if(convertView==null)
		{
			holder=new ViewHolder();
			convertView=LayoutInflater.from(context).inflate(R.layout.friends_list_item,null);
			holder.userProfileImage =(ImageView)convertView.findViewById(R.id.user_profile_image_in_friends_list);
			holder.userName=(TextView)convertView.findViewById(R.id.user_screen_name_in_friends_list);
			holder.detail=(TextView)convertView.findViewById(R.id.user_gender_location_in_friends_list);
			//holder.btFollow=(Button)convertView.findViewById(R.id.follow_status_in_friends_list);		
			convertView.setTag(holder);
		}
		else
			holder=(ViewHolder) convertView.getTag();
		
		
		
		
		WeiboUserInfo user=new WeiboUserInfo();
		user=userList.get(arg0);
		asyncBitmapLoader.loadBitmap(holder.userProfileImage,MumuWeiboUtility.IMAGE_TYPE.PROFILE,user.getProfile());
		final String username=user.getName();
		holder.userName.setText(username);
		
		String gender="保密";
		if(user.getGender().equals("f"))
			gender="女";
		if(user.getGender().equals("m"))
			gender="男";
		holder.detail.setText(gender+", "+user.getLocation());
		
		/*
		boolean isFollowing=user.isFollowing();
		if(isFollowing)
		{
			holder.btFollow.setText("取消关注");			
		}
		else
			holder.btFollow.setText("关注Ta");
		*/
		holder.userProfileImage.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				Intent in=new Intent();
				in.setClass(context, UserInfoShow.class);
				in.putExtra("screen_name", username);
				context.startActivity(in);
			}
		});
		
		return convertView;
	}
	
	private class ViewHolder{		
		public ImageView userProfileImage ;
		public TextView userName;	
		public TextView detail;
		//public Button btFollow;	
		public ViewHolder(){
			
		}
	}

}
