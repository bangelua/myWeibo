package com.mumuWeibo2;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mumuWeibo2.WeiboDetail.MsgItem;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.UsersAPI;
import com.weibo.sdk.android.net.RequestListener;

import org.json.JSONException;

import java.io.IOException;

public class LeftWeiboView extends RelativeLayout {

    private TextView msg;
    private SimpleDraweeView profile;
    private TextView username;
    private AsyncBitmapLoader async = new AsyncBitmapLoader();

    String name;

    private Handler handler = new Handler();

    public void showToast(final String s) {
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getContext(), s,
                        Toast.LENGTH_SHORT).show();

            }
        });
    }

    public LeftWeiboView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init();
    }

    public LeftWeiboView(Context context) {
        super(context);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.left_weibo_view, this);
        msg = (TextView) findViewById(R.id.tv_weiboText_in_left_view);
        profile = (SimpleDraweeView) findViewById(R.id.iv_user_profile_in_left);
        username = (TextView) findViewById(R.id.tv_user_name_in_left);

        profile.setOnClickListener(lis);
        username.setOnClickListener(lis);
    }

    public void setView(final MsgItem item) {
        //....
        name = item.userName;
        username.setText(name);

        MumuWeiboUtility.FormatWeibo(getContext(), msg, item.msgText, true);

        msg.setMovementMethod(LinkMovementMethod.getInstance());

        if (MumuWeiboUtility.userInfoCache.containsKey(name)) {
            WeiboUserInfo userInfo = new WeiboUserInfo();
            userInfo = MumuWeiboUtility.userInfoCache.get(name);
            String profileUrl = userInfo.getProfile();
            profile.setImageURI(Uri.parse(profileUrl));
        } else {
            UsersAPI api = new UsersAPI(AccessTokenKeeper.readAccessToken(getContext()));
            api.show(name, new GetUserInfoListener());

        }
    }

    class GetUserInfoListener implements RequestListener {

        @Override
        public void onComplete(final String arg0) {
            // TODO Auto-generated method stub

            handler.post(new Runnable() {

                @Override
                public void run() {
                    WeiboUserInfo userInfo = new WeiboUserInfo();
                    try {
                        userInfo = WeiboUserParser.parse(arg0);
                        MumuWeiboUtility.userInfoCache.put(name, userInfo);
                        String profileUrl = userInfo.getProfile();
                        profile.setImageURI(Uri.parse(profileUrl));
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Toast.makeText(getContext(), "解析用户信息失败!", Toast.LENGTH_LONG).show();
                    }
                }
            });

        }

        @Override
        public void onError(WeiboException arg0) {
            // TODO Auto-generated method stub
            //showToast("@"+name+WeiboErrorHelper.WeiboError(arg0));
        }

        @Override
        public void onIOException(IOException arg0) {
            // TODO Auto-generated method stub
            showToast("获取用户信息失败!\n" + arg0.getMessage());

        }

    }

    private OnClickListener lis = new OnClickListener() {
        public void onClick(View v) {
            Intent in = new Intent();
            in.setClass(getContext(), UserInfoShow.class);
            in.putExtra("screen_name", username.getText().toString());
            getContext().startActivity(in);
        }
    };
}
