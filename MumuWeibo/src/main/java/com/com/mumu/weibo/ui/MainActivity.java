package com.com.mumu.weibo.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.mumuWeibo2.AccessTokenKeeper;
import com.mumuWeibo2.BlockWords;
import com.mumuWeibo2.EmotionParser;
import com.mumuWeibo2.FaceAdapter;
import com.mumuWeibo2.MumuWeiboUtility;
import com.mumuWeibo2.R;
import com.mumuWeibo2.WeiboErrorHelper;
import com.mumuWeibo2.WeiboInfo;
import com.mumuWeibo2.WeiboUserInfo;
import com.mumuWeibo2.WeiboUserParser;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.AccountAPI;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.api.UsersAPI;
import com.weibo.sdk.android.api.WeiboAPI;
import com.weibo.sdk.android.net.RequestListener;
import com.weibo.sdk.android.sso.SsoHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by luliang on 5/29/15.
 */
public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    ImageView mainPage;
    ImageView message;
    ImageView userInfo;

    SsoHandler mSsoHandler;

    Handler handler = new Handler();
    String mainId = "1";
    String userId = "2";
    String msgId = "3";

    static String app_key = "2606413906";
    static String app_secret = "e8e84282519e5a785d62482ce441f2be";
    static String redirectUrl = "http://api.weibo.com/oauth2/default.html";

    private final int SHOW_PICTURE = Menu.FIRST + 1;
    private final int BLOCK_WORDS = Menu.FIRST + 2;
    private final int CHANGE_USER = Menu.FIRST + 3;

    private Fragment mTimelineFragement;
    private Fragment mUserInfoFragment;

    private static final int ID_FRAGMENT_TIMELINE = 1;
    private static final int ID_FRAGMENT_USERINFO = 2;
    private static final int ID_FRAGMENT_MESSAGE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_tab);

        loadLocalEmotions();
        initConfig();
        importLocalWeiboData();
        initView();


        showFragment(ID_FRAGMENT_TIMELINE);

        checkAccountValid();

    }

    private void hideAllFragments(FragmentTransaction ft) {
        if (mUserInfoFragment != null) {
            ft.hide(mUserInfoFragment);
        }

        if (mTimelineFragement != null) {
            ft.hide(mTimelineFragement);
        }

    }

    private void showFragment(int id) {
        setSelection(id);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        hideAllFragments(ft);
        switch (id) {
            case ID_FRAGMENT_TIMELINE:
                if (mTimelineFragement == null) {
                    mTimelineFragement = new WeiboTimelineFragment();
                    ft.replace(R.id.fragment_content,
                            mTimelineFragement);
                } else {
                    ft.show(mTimelineFragement);
                }
                break;

            case ID_FRAGMENT_USERINFO:
                if (mUserInfoFragment == null) {
                    mUserInfoFragment = new UserInfoFragment();
                    ft.add(R.id.fragment_content,
                            mUserInfoFragment);
                } else {
                    ft.show(mUserInfoFragment);
                }
                break;
        }

        ft.commit();


    }


    public void setSelection(int id) {
        mainPage.setImageResource(R.drawable.tab_home_normal);
        message.setImageResource(R.drawable.tab_message_normal);
        userInfo.setImageResource(R.drawable.tab_profile_focused);

        switch (id) {
            case ID_FRAGMENT_TIMELINE:
                mainPage.setImageResource(R.drawable.tab_home_pressed);
                break;
            case ID_FRAGMENT_USERINFO:
                userInfo.setImageResource(R.drawable.tab_profile_disabled);
                break;
            case ID_FRAGMENT_MESSAGE:
                message.setImageResource(R.drawable.tab_message_pressed);
                break;
        }

    }


//    public void showView(int i) {
//        if (i == 1) {
//            bodyView.removeAllViews();
//            View v = getLocalActivityManager().startActivity(mainId,
//                    this.mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)).getDecorView();
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                    ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
//            v.setLayoutParams(params);
//            bodyView.addView(v);
//
//            mainPage.setImageResource(R.drawable.tab_home_pressed);
//            message.setImageResource(R.drawable.tab_message_normal);
//            userInfo.setImageResource(R.drawable.tab_profile_focused);
//        } else if (i == 2) {
//            if (MumuWeiboUtility.LoginUser == null) {
//                Toast.makeText(getApplicationContext(), "暂无用户信息，请稍等。。。", Toast.LENGTH_LONG)
// .show();
//                setUserInfo();
//                return;
//            }
//            bodyView.removeAllViews();
//            this.userIntent.putExtra("userinfo", MumuWeiboUtility.LoginUser);
//            View v = getLocalActivityManager().startActivity(userId,
//                    this.userIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                    ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
//            v.setLayoutParams(params);
//            bodyView.addView(v);
//
//            mainPage.setImageResource(R.drawable.tab_home_normal);
//            message.setImageResource(R.drawable.tab_message_normal);
//            userInfo.setImageResource(R.drawable.tab_profile_disabled);
//        } else if (i == 3) {
//
//            bodyView.removeAllViews();
//            View v = getLocalActivityManager().startActivity(msgId,
//                    this.msgIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                    ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
//            v.setLayoutParams(params);
//            bodyView.addView(v);
//
//            mainPage.setImageResource(R.drawable.tab_home_normal);
//            message.setImageResource(R.drawable.tab_message_pressed);
//            userInfo.setImageResource(R.drawable.tab_profile_focused);
//        }
//    }


    private void initConfig() {
        MumuWeiboUtility.context = getApplicationContext();

        //初始化设置信息
        SharedPreferences settings = getSharedPreferences(MumuWeiboUtility.SETTING_INFO, 0);
        MumuWeiboUtility.autoShowImage = settings.getBoolean("isShowPic", false);
    }

    private void checkAccountValid() {
        Oauth2AccessToken token = AccessTokenKeeper.readAccessToken(getApplicationContext());
        if (token.getToken().equals("")) {
            showLoginDialog("第一次使用请先认证。");
        } else if (!token.isSessionValid())
            showLoginDialog("认证已过期，请重新登录");
        else
            MumuWeiboUtility.isSeized = true;
    }

    private void initView() {
        mainPage = (ImageView) findViewById(R.id.iv_mainpage);
        message = (ImageView) findViewById(R.id.iv_message);
        userInfo = (ImageView) findViewById(R.id.iv_userinfo);

        mainPage.setOnClickListener(lis);
        message.setOnClickListener(lis);
        userInfo.setOnClickListener(lis);

//        this.mainIntent = new Intent(this, MumuWeibo.class);
//        this.userIntent = new Intent(this, UserInfoShow.class);
//        this.msgIntent = new Intent(this, MsgHolder.class);

//        showView(1);
    }


    private void importLocalWeiboData() {
        //从本地导入保存的公共微博列表
        MumuWeiboUtility.ImportWeibosList(getApplicationContext(), MumuWeiboUtility.LIST_FLAG
                .PUBLIC);
        MumuWeiboUtility.ImportWeibosList(getApplicationContext(), MumuWeiboUtility.LIST_FLAG
                .COMMENTS);
        MumuWeiboUtility.ImportWeibosList(getApplicationContext(), MumuWeiboUtility.LIST_FLAG
                .ATMSG);
        MumuWeiboUtility.ImportWeibosList(getApplicationContext(), MumuWeiboUtility.LIST_FLAG
                .MYWEIBOS);

        MumuWeiboUtility.ImportBlockWords();

        MumuWeiboUtility.importUserInfo(getApplicationContext());
    }

    private void loadLocalEmotions() {
        InputStream emotionInputStream = getResources().openRawResource(R.raw.emotion);

        try {
            ObjectInputStream ois = new ObjectInputStream(emotionInputStream);
            MumuWeiboUtility.emotionMapList = (HashMap<String, String>) ois.readObject();
            Log.i(TAG, "get emotionList form app RAW resource");
            if (ois != null) ois.close();
            if (emotionInputStream != null) emotionInputStream.close();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void showLoginDialog(String hint) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setMessage(hint);
        dialog.setTitle(null);
        dialog.setPositiveButton("去登录", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO Auto-generated method stub
                changeUser();
            }
        });

        dialog.create().show();

    }

    public void onDestroy() {
        super.onDestroy();
        //退出清除缓存

        MumuWeiboUtility.saveWeiboList(getApplicationContext(), MumuWeiboUtility.LIST_FLAG.PUBLIC);
        MumuWeiboUtility.saveWeiboList(getApplicationContext(), MumuWeiboUtility.LIST_FLAG
                .COMMENTS);
        MumuWeiboUtility.saveWeiboList(getApplicationContext(), MumuWeiboUtility.LIST_FLAG.ATMSG);
        MumuWeiboUtility.saveWeiboList(getApplicationContext(), MumuWeiboUtility.LIST_FLAG
                .MYWEIBOS);
        MumuWeiboUtility.saveUserInfo(getApplicationContext());

        //MumuWeiboUtility.WeiboInfoList.removeAll(MumuWeiboUtility.WeiboInfoList.subList(5,
        // MumuWeiboUtility.WeiboInfoList.size()));
        int i = MumuWeiboUtility.WeiboInfoList.size();

        int MAX_PUBLIC_WEIBO_RECORD = 150;//保留公共微博的最大记录数为MAX_RECORD;

        while (i > MAX_PUBLIC_WEIBO_RECORD)
            MumuWeiboUtility.WeiboInfoList.remove(--i);
        MumuWeiboUtility.saveWeiboList(getApplicationContext(), MumuWeiboUtility.LIST_FLAG.PUBLIC);


        int MAX_COMMENTS_WEIBO_RECORD = 100;//保留评论的最大记录数为MAX_RECORD;
        i = MumuWeiboUtility.CommentsList.size();
        while (i > MAX_COMMENTS_WEIBO_RECORD)
            MumuWeiboUtility.CommentsList.remove(--i);
        MumuWeiboUtility.saveWeiboList(getApplicationContext(), MumuWeiboUtility.LIST_FLAG
                .COMMENTS);

        int MAX_ATMSG_WEIBO_RECORD = 100;//保留@的最大记录数为MAX_RECORD;
        i = MumuWeiboUtility.AtMsgList.size();
        while (i > MAX_ATMSG_WEIBO_RECORD)
            MumuWeiboUtility.AtMsgList.remove(--i);
        MumuWeiboUtility.saveWeiboList(getApplicationContext(), MumuWeiboUtility.LIST_FLAG.ATMSG);

        int MAX_MY_WEIBOS_RECORD = 100;//保留我的微博最大记录数为MAX_RECORD;
        i = MumuWeiboUtility.MyWeibosList.size();
        while (i > MAX_MY_WEIBOS_RECORD)
            MumuWeiboUtility.MyWeibosList.remove(--i);
        MumuWeiboUtility.saveWeiboList(getApplicationContext(), MumuWeiboUtility.LIST_FLAG
                .MYWEIBOS);


        Log.i("----------------------", "on destroy");
        MumuWeiboUtility.userInfoCache.clear();
        MumuWeiboUtility.clearCache();
    }

    private View.OnClickListener lis = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_mainpage:
                    showFragment(ID_FRAGMENT_TIMELINE);
                    break;
                case R.id.iv_userinfo:
                    showFragment(ID_FRAGMENT_USERINFO);
                    break;
            }


//            if (v.getId() == R.id.iv_mainpage)
//                showView(1);
//            else if (v.getId() == R.id.iv_userinfo)
//                showView(2);
//            else if (v.getId() == R.id.iv_message)
//                showView(3);

        }

    };

    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent
                .ACTION_DOWN) {
            AlertDialog.Builder sureFinish = new AlertDialog.Builder(MainActivity.this);
            sureFinish.setMessage("确定要退出沐牧微博客户端？");
            sureFinish.setPositiveButton("退出", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub

                    finish();
                }
            });
            sureFinish.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                }
            });
            sureFinish.create().show();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    //设置菜单项
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, BLOCK_WORDS, 1, "屏蔽关键字");

        Oauth2AccessToken token = AccessTokenKeeper.readAccessToken(getApplicationContext());
        String login;
        if (token.getToken().equals("")) {
            login = "用户登录";
        } else
            login = "更换账户";
        menu.add(0, CHANGE_USER, 2, login);

        if (MumuWeiboUtility.autoShowImage)
            menu.add(0, SHOW_PICTURE, 3, "一直显示缩略图");
        else
            menu.add(0, SHOW_PICTURE, 3, "仅wifi下显示缩略图");
        return true;
    }

    //菜单项被选中
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case CHANGE_USER:
                //	AccessTokenKeeper.
                changeUser();
                break;

            case BLOCK_WORDS:
                Intent i = new Intent();
                i.setClass(MainActivity.this, BlockWords.class);
                startActivity(i);
                break;
            case SHOW_PICTURE:
                //DO SOMETHING!
                //Log.i("-----------------", "the isShow is "+MumuWeiboUtility.isShowImage);
                if (MumuWeiboUtility.autoShowImage)
                    MumuWeiboUtility.autoShowImage = false;
                else
                    MumuWeiboUtility.autoShowImage = true;

                SharedPreferences settings = getSharedPreferences(MumuWeiboUtility.SETTING_INFO, 0);

                settings.edit().putBoolean("isShowPic", MumuWeiboUtility.autoShowImage).commit();

                //Log.i("-----------------", "the isShow is "+MumuWeiboUtility.isShowImage);

                break;

            default:
                ;
        }
        return true;
    }

    public void onOptionsMenuClosed(Menu menu) {
        MenuItem item = menu.findItem(SHOW_PICTURE);
        if (MumuWeiboUtility.autoShowImage) item.setTitle("一直显示缩略图");
        else
            item.setTitle("仅在wifi下显示缩略图");
    }


    public void changeUser() {

        Weibo weibo = Weibo.getInstance(app_key, redirectUrl);
        // weibo.startAuthDialog(context, listener)
        mSsoHandler = new SsoHandler(MainActivity.this, weibo);
        mSsoHandler.authorize(new AuthListener());


    }

    class AuthListener implements WeiboAuthListener {

        @Override
        public void onCancel() {
            // TODO Auto-generated method stub
            Log.i(TAG, "Auth onCancel");
            MumuWeiboUtility.isSeized = true;
        }

        @Override
        public void onComplete(Bundle values) {
            // TODO Auto-generated method stub
            Log.i(TAG, "Auth onComplete");
            //showToast("认证成功！");
            //Toast.makeText(MumuWeibo.this, , Toast.LENGTH_SHORT).show();
            AccessTokenKeeper.clear(getApplicationContext());
            MumuWeiboUtility.LoginUser = null;

            String token = values.getString("access_token");
            String expires_in = values.getString("expires_in");
            String refresh_token = values.getString("refresh_token");

            Oauth2AccessToken token2 = new Oauth2AccessToken(token, expires_in);

            token2.setRefreshToken(refresh_token);

            AccessTokenKeeper.keepAccessToken(MainActivity.this, token2);
            //setUserInfo();

            //**********
            //放在这合适否?带修正待补
            //更换用户或重新认证时，清空现有saved的微博
            MumuWeiboUtility.WeiboInfoList.clear();
            MumuWeiboUtility.WeiboInfoList = new ArrayList<WeiboInfo>();
            MumuWeiboUtility.saveWeiboList(getApplicationContext(), MumuWeiboUtility.LIST_FLAG
                    .PUBLIC);

            MumuWeiboUtility.CommentsList.clear();
            MumuWeiboUtility.CommentsList = new ArrayList<WeiboInfo>();
            MumuWeiboUtility.saveWeiboList(getApplicationContext(), MumuWeiboUtility.LIST_FLAG
                    .COMMENTS);

            MumuWeiboUtility.AtMsgList.clear();
            MumuWeiboUtility.AtMsgList = new ArrayList<WeiboInfo>();
            MumuWeiboUtility.saveWeiboList(getApplicationContext(), MumuWeiboUtility.LIST_FLAG
                    .ATMSG);

            MumuWeiboUtility.MyWeibosList.clear();
            MumuWeiboUtility.MyWeibosList = new ArrayList<WeiboInfo>();
            MumuWeiboUtility.saveWeiboList(getApplicationContext(), MumuWeiboUtility.LIST_FLAG
                    .MYWEIBOS);

            MumuWeiboUtility.userInfoCache.clear();
            MumuWeiboUtility.LoginUser = null;
            MumuWeiboUtility.isSeized = false;
//            showView(1);
            showFragment(ID_FRAGMENT_TIMELINE);
            getEmotion();
            downloadEmotion();
            setUserInfo();
        }

        @Override
        public void onError(WeiboDialogError arg0) {
            // TODO Auto-generated method stub
            Log.i(TAG, "Auth Error");
            MumuWeiboUtility.isSeized = true;
        }

        @Override
        public void onWeiboException(WeiboException arg0) {
            // TODO Auto-generated method stub
            Log.i(TAG, "Auth Exception");
            MumuWeiboUtility.isSeized = true;
        }

    }//end of authlistener

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    //获取表情
    private void getEmotion() {
        try {
            FileInputStream fis = MainActivity.this.openFileInput("EmotionHashMap.dat");
            try {
                ObjectInputStream ois = new ObjectInputStream(fis);
                try {
                    MumuWeiboUtility.emotionMapList = (HashMap<String, String>) ois.readObject();
                    Log.i(TAG, "get emotionList form app store dir");
                    if (MumuWeiboUtility.emotionMapList.size() == 0) {
                        throw new FileNotFoundException();
                    }
                    return;
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (ois != null) ois.close();
                if (fis != null) fis.close();
            } catch (StreamCorruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            //e1.printStackTrace();
            //get emotion form internet
            Log.i(TAG, "Start to get emotion from internet!!!!");

            StatusesAPI api6 = new StatusesAPI(AccessTokenKeeper.readAccessToken(MainActivity
                    .this));

            api6.emotions(WeiboAPI.EMOTION_TYPE.FACE, WeiboAPI.LANGUAGE.cnname, new
                    EmotionListener());

            api6.emotions(WeiboAPI.EMOTION_TYPE.ANI, WeiboAPI.LANGUAGE.cnname, new
                    EmotionListener());

            api6.emotions(WeiboAPI.EMOTION_TYPE.CARTOON, WeiboAPI.LANGUAGE.cnname, new
                    EmotionListener());

            //end get emotion form internet
        }
    }


    class EmotionListener implements RequestListener {

        @Override
        public void onComplete(String arg0) {
            // TODO Auto-generated method stub
            EmotionParser.parse(arg0);
            saveEmotion();
            downloadEmotion();
        }


        @Override
        public void onError(WeiboException arg0) {
            // TODO Auto-generated method stub
            showToast(WeiboErrorHelper.WeiboError(arg0));
        }

        @Override
        public void onIOException(IOException arg0) {
            // TODO Auto-generated method stub
            showToast("获取表情包失败！");
        }
    }

    public void showToast(final String s) {
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), s,
                        Toast.LENGTH_SHORT).show();

            }
        });
    }

    //下载些微博的常用表情
    private void downloadEmotion() {
        // TODO Auto-generated method stub

        new Thread() {
            public void run() {
                for (int i = 0; i < FaceAdapter.faceArray.length; i++) {
                    final String imageURL = MumuWeiboUtility.emotionMapList.get(FaceAdapter
                            .faceArray[i]);
                    if (imageURL == null) continue;
                    MumuWeiboUtility.getImageFromUrl(imageURL, MumuWeiboUtility.emotionSaveDir);
                }
                Log.i(TAG, "常用表情下载完毕！");
            }
        }.start();
    }

    //保存表情hashmap到本地
    public void saveEmotion() {
        //保存表情列表
        try {
            FileOutputStream os = MainActivity.this.openFileOutput("EmotionHashMap.dat",
                    MODE_PRIVATE);

            try {
                ObjectOutputStream oos = new ObjectOutputStream(os);
                oos.writeObject(MumuWeiboUtility.emotionMapList);
                if (oos != null) oos.close();
                if (os != null) os.close();
                Log.i("---------------info", "emotion has been downloaded,and saved at local");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                //Toast.makeText(MumuWeibo.this, "表情列表保存失败！", Toast.LENGTH_SHORT).show();
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            //Toast.makeText(MumuWeibo.this, "表情列表本地不存在！", Toast.LENGTH_SHORT).show();
        }
    }

    public void setUserInfo() {

        AccountAPI api0 = new AccountAPI(AccessTokenKeeper.readAccessToken(MainActivity.this));

        api0.getUid(new getLoginerListener());
    }

    class getLoginerListener implements RequestListener {

        @Override
        public void onComplete(String arg0) {
            // TODO Auto-generated method stub
            Long uids = null;
            try {
                JSONObject jo = new JSONObject(arg0);
                uids = jo.getLong("uid");
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            UsersAPI userAPI = new UsersAPI(AccessTokenKeeper.readAccessToken(MainActivity.this));
            userAPI.show(uids, new GetLoginerInfoListener());
        }

        @Override
        public void onError(WeiboException arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onIOException(IOException arg0) {
            // TODO Auto-generated method stub

        }

    }

    class GetLoginerInfoListener implements RequestListener {

        @Override
        public void onComplete(final String arg0) {
            // TODO Auto-generated method stub

            handler.post(new Runnable() {
                public void run() {
                    try {
                        WeiboUserInfo user = WeiboUserParser.parse(arg0);
                        MumuWeiboUtility.LoginUser = user;
                        MumuWeiboUtility.saveUserInfo(getApplicationContext());

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onError(WeiboException arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onIOException(IOException arg0) {
            // TODO Auto-generated method stub

        }

    }


    class LoginOutListener implements RequestListener {

        @Override
        public void onComplete(String arg0) {
            // TODO Auto-generated method stub
            handler.post(new Runnable() {
                public void run() {
                    //changeUser2();
                }
            });

            Log.i(TAG, "user has login out!");
        }

        @Override
        public void onError(final WeiboException arg0) {
            // TODO Auto-generated method stub
            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "LOGIN OUT FAILED!!" + WeiboErrorHelper
                            .WeiboError(arg0), Toast.LENGTH_LONG).show();

                }
            });
            //Toast.makeText(MainActivity.this, WeiboErrorHelper.WeiboError(arg0), Toast
            // .LENGTH_LONG)
            // .show();
        }

        @Override
        public void onIOException(IOException arg0) {
            // TODO Auto-generated method stub

        }

    }

}
