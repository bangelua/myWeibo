package com.mumuWeibo2;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class FaceAdapter extends BaseAdapter{
	
	public static String[] faceArray={"[呵呵]","[嘻嘻]","[哈哈]","[挤眼]","[爱你]","[晕]"
			,"[泪]","[馋嘴]","[抓狂]","[哼]","[抱抱]","[可爱]","[怒]","[汗]","[困]",
			"[害羞]","[睡觉]",
			"[钱]","[偷笑]","[酷]","[衰]","[吃惊]","[鄙视]","[挖鼻屎]","[花心]","[鼓掌]","[拜拜]","[失望]","[思考]"
			,"[生病]","[亲亲]","[怒骂]","[太开心]","[懒得理你]","[左哼哼]","[右哼哼]","[嘘]","[委屈]","[吐]","[可怜]","[打哈欠]"
			,"[疑问]","[握手]","[耶]","[good]","[弱]","[便便]","[ok]","[赞]","[赞啊]","[不要]","[来]"
			,"[蛋糕]","[花]","[玫瑰]","[心]","[伤心]","[钟]","[音乐盒]","[猪头]","[囧]","[话筒]","[月亮]","[下雨]","[太阳]"
			,"[蜡烛]","[国旗]","[给力]","[威武]","[织]","[围观]","[群体围观]","[神马]","[草泥马]","[奥特曼]","[浮云]","[兔子]"
			,"[熊猫]","[飞机]","[照相机]","[笑哈哈]","[得意地笑]","[带感]","[立志青年]","[泪流满面]","[江南style]","[走你]"
			,"[好激动]","[吐血]","[lt切克闹]","[非常汗]","[巨汗]","[右边亮了]","[好喜欢]","[被电]",
			"[moc顶]","[moc亲亲女]","[moc亲亲男]","[moc拍照]","[moc浮云]",};
	private AsyncBitmapLoader async=new AsyncBitmapLoader();
	Context context;	
	
	public FaceAdapter(Context ctx){
		context=ctx;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return faceArray.length;
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
	public View getView(int pos, View convertView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		 ImageView ivImageView= null;  
         if (convertView==null)  
         {  
        	 ivImageView=new ImageView(context);
        	 ivImageView.setLayoutParams(new GridView.LayoutParams(50,50));
        	 ivImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        	 ivImageView.setPadding(2, 2, 2, 2);        	         	 
         } 
         else 
        	 ivImageView=(ImageView)convertView;
         
        ivImageView.setImageDrawable(async.loadEmotion(faceArray[pos]));         
         
       return ivImageView;  
		
	
	}

}
