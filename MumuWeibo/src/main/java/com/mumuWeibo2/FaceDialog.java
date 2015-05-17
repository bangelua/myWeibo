package com.mumuWeibo2;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;

public class FaceDialog {
	
	private Context context;
	private EditText et;
	
public FaceDialog(Context ctx,EditText et0){
		
		this.context=ctx;
		this.et=et0;
	
	  final Dialog dialog = new Dialog(context,R.style.dialog);
	  dialog.setContentView(R.layout.face_list_layout);
	  dialog.setCancelable(true);
	  
	  GridView gv=(GridView)dialog.findViewById(R.id.gd_face);
	  final FaceAdapter adapter=new FaceAdapter(context);
	  gv.setAdapter(adapter);
	  gv.setOnItemClickListener(new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1,
				int pos, long arg3) {
			// TODO Auto-generated method stub
									
			int start=et.getSelectionStart();
			
			et.getText().insert(start, adapter.faceArray[pos]);
			
			//et.append(adapter.faceArray[pos]);
			
			dialog.dismiss();
		}		 
	  });
	  
	  dialog.show();
}

}
