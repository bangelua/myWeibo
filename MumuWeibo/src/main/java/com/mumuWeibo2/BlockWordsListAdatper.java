package com.mumuWeibo2;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BlockWordsListAdatper extends BaseAdapter{
	
	ArrayList<String> list=new ArrayList<String>();
	Context context;
	
	public BlockWordsListAdatper(Context ctx,ArrayList<String> lst)
	{
		this.context=ctx;
		list=lst;
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
		return 0;
	}

	@Override
	public View getView(final int index, View convertView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		ViewHolder holder=null;
		
		if(convertView==null)
		{
			holder=new ViewHolder();
			convertView=LayoutInflater.from(context).inflate(R.layout.block_item_layout,null);
			holder.keywords =(TextView)convertView.findViewById(R.id.block_item);
			holder.iv_delete=(ImageView)convertView.findViewById(R.id.iv_delete);
			convertView.setTag(holder);
		}
		else
			holder=(ViewHolder) convertView.getTag();
		
		String word=list.get(index);
		holder.keywords.setText(word);
		
		holder.iv_delete.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//list.remove(index);				
			}
		});
		
		return convertView;
		
	}
	
	private class ViewHolder{		
		
		public TextView keywords;			
		public ImageView iv_delete;		
		public ViewHolder(){
			
		}
	}

}
