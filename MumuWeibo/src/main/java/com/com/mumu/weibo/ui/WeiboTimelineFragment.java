package com.com.mumu.weibo.ui;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mumuWeibo2.AccessTokenKeeper;
import com.mumuWeibo2.AsyncBitmapLoader;
import com.mumuWeibo2.MumuWeiboUtility;
import com.mumuWeibo2.MyListView;
import com.mumuWeibo2.R;
import com.mumuWeibo2.ShareOperation;
import com.mumuWeibo2.WeiboDetail;
import com.mumuWeibo2.WeiboErrorHelper;
import com.mumuWeibo2.WeiboInfo;
import com.mumuWeibo2.WeiboListAdapter;
import com.mumuWeibo2.WeiboListParser;
import com.mumuWeibo2.WeiboUserInfo;
import com.mumuWeibo2.WeiboUserParser;
import com.mumuWeibo2.WriteWeibo;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.AccountAPI;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.api.UsersAPI;
import com.weibo.sdk.android.api.WeiboAPI;
import com.weibo.sdk.android.net.RequestListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by luliang on 5/29/15.
 */
public class WeiboTimelineFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();
    ImageButton sendWeibo;
    ImageView flushWeibo;

    SimpleDraweeView logo;
    TextView title;

    ProgressDialog progressDialog;
    Animation anim;
    private MyListView myListView;
    private Handler mHandler = new Handler();

    public void showToast(final String s) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getActivity(), s,
                        Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View fragView = inflater.inflate(R.layout.timeline_fragment_layout, container, false);
        initView(fragView);
        return fragView;
    }

    private void initView(View view) {
        logo = (SimpleDraweeView) view.findViewById(R.id.iv_log);
        sendWeibo = (ImageButton) view.findViewById(R.id.sendweibo);
        flushWeibo = (ImageView) view.findViewById(R.id.flushweibo);
        logo.setOnClickListener(btn_listener);
        sendWeibo.setOnClickListener(btn_listener);
        flushWeibo.setOnClickListener(btn_listener);
        myListView = (MyListView) view.findViewById(R.id.listview_weibo);
        myListView.setOnItemClickListener(listItemClickListener);
        myListView.setOnItemLongClickListener(mOnItemLongClickListener);
        title = (TextView) view.findViewById(R.id.tv_title_in_mainpage);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initListViewAndAdapter();
        if (AccessTokenKeeper.readAccessToken(MumuWeiboUtility.context).isSessionValid())
            if (MumuWeiboUtility.isFlushingWeibo == false) flushWeibo();
    }

    public void onResume() {
        super.onResume();
        Log.i(TAG, "OnResume");
        if (AccessTokenKeeper.readAccessToken(MumuWeiboUtility.context).isSessionValid())
            if (MumuWeiboUtility.isSeized) {
                MumuWeiboUtility.isSeized = false;
            } else if (!MumuWeiboUtility.isFlushingWeibo) flushWeibo();

        setUserInfo();
    }

    private void initListViewAndAdapter() {
        myListView.setOnRefreshListener(new MyListView.IOnRefreshListener() {

            @Override
            public void OnRefresh() {
                flushWeibo();
            }
        });
        myListView.setOnLoadMoreListener(new MyListView.IOnLoadMoreListener() {

            @Override
            public void OnLoadMore() {
                getMoreWeibo();
            }
        });

        WeiboListAdapter adapter = new WeiboListAdapter(getActivity(), MumuWeiboUtility
                .WeiboInfoList);
        myListView.setAdapter(adapter);
    }

    public void getMoreWeibo() {
        String maxId = "1";
        int length = MumuWeiboUtility.WeiboInfoList.size();
        if (length > 0)
            maxId = MumuWeiboUtility.WeiboInfoList.get(length - 1).getWeiboId();

        long max = Long.parseLong(maxId) - 1;

        StatusesAPI api = new StatusesAPI(AccessTokenKeeper.readAccessToken(getActivity()));
        api.friendsTimeline(0l, max, 30, 1, false, WeiboAPI.FEATURE.ALL, false, new
                GetMoreWeiboListener());

    }

    class GetMoreWeiboListener implements RequestListener {

        @Override
        public void onComplete(String result) {
            // TODO Auto-generated method stub
            if (progressDialog != null) progressDialog.dismiss();

            int count;
            try {
                count = WeiboListParser.parse(result, MumuWeiboUtility.WeiboInfoList, 1);

                if (count == 0) {
                    showToast("没有更多微博了。");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            myListView.onLoadMoreComplete(true);
                        }
                    });


                } else {
                    showToast("收到" + count + "条微博");

                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            WeiboListAdapter adapter = new WeiboListAdapter(getActivity(),
                                    MumuWeiboUtility.WeiboInfoList);
                            adapter.notifyDataSetChanged();
                            myListView.onLoadMoreComplete(false);
                            MumuWeiboUtility.saveWeiboList(getActivity(), MumuWeiboUtility
                                    .LIST_FLAG.PUBLIC);
                        }
                    });

                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                myListView.onLoadMoreComplete(false);
            }

            if (anim != null) anim.cancel();
        }

        @Override
        public void onError(WeiboException arg0) {
            if (progressDialog != null) progressDialog.dismiss();
            if (anim != null) anim.cancel();
            showToast(WeiboErrorHelper.WeiboError(arg0));
            mHandler.post(new Runnable() {
                public void run() {
                    myListView.onLoadMoreComplete(false);
                }
            });
        }

        @Override
        public void onIOException(IOException arg0) {
            if (progressDialog != null) progressDialog.dismiss();
            if (anim != null) anim.cancel();
            showToast("刷新微博失败");
            mHandler.post(new Runnable() {
                public void run() {
                    myListView.onLoadMoreComplete(false);
                }
            });
        }
    }

    public void setUserInfo() {

        if (MumuWeiboUtility.LoginUser != null) {
            WeiboUserInfo user = MumuWeiboUtility.LoginUser;
            title.setText(user.getName());

            logo.setImageURI(Uri.parse(user.getProfile()));
            return;
        }

        Oauth2AccessToken token = AccessTokenKeeper.readAccessToken(MumuWeiboUtility.context);
        if (!token.isSessionValid()) return;

        AccountAPI api0 = new AccountAPI(token);

        api0.getUid(new getLoginerListener());
    }

    public void onPause() {
        super.onPause();
        Log.i(TAG, "OnPause");
        MumuWeiboUtility.isSeized = true;
    }


    public void onStop() {
        super.onStop();
        MumuWeiboUtility.isSeized = true;
        Log.i(TAG, "OnStop");
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

            UsersAPI userAPI = new UsersAPI(AccessTokenKeeper.readAccessToken(MumuWeiboUtility
                    .context));
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

            mHandler.post(new Runnable() {
                public void run() {
                    try {
                        WeiboUserInfo user = WeiboUserParser.parse(arg0);
                        MumuWeiboUtility.LoginUser = user;

                        if (user != null) {
                            AsyncBitmapLoader async = new AsyncBitmapLoader();
                            async.loadBitmap(logo, MumuWeiboUtility.IMAGE_TYPE.PROFILE, user
                                    .getProfile());
                            title.setText(user.getName());
                        }

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

    public void flushWeibo() {
        MumuWeiboUtility.isFlushingWeibo = true;

        if (MumuWeiboUtility.WeiboInfoList.size() == 0) {
            progressDialog = ProgressDialog.show(getActivity(), null, "正在刷新微博，请稍等。。。");
            progressDialog.setCancelable(true);
        }

        anim = AnimationUtils.loadAnimation(getActivity(), R.anim.loading);
        flushWeibo.setAnimation(anim);
        anim.startNow();

        int num = 20;//一次刷新的围脖数量
        long since = 0l;
        if (MumuWeiboUtility.WeiboInfoList.size() > 1) {
            num += 1;
            since = MumuWeiboUtility.WeiboInfoList.get(1).getId();
        }

        StatusesAPI api = new StatusesAPI(AccessTokenKeeper.readAccessToken(getActivity()));


        api.friendsTimeline(since, 0l, num, 1, false, WeiboAPI.FEATURE.ALL, false, new
                GetNewWeiboListener());

    }

    class GetNewWeiboListener implements RequestListener {

        @Override
        public void onComplete(String newWeibo) {
            try {
                final int count = WeiboListParser.parse(newWeibo, MumuWeiboUtility.WeiboInfoList,
                        0);
                if (progressDialog != null) progressDialog.dismiss();
                if (anim != null) anim.cancel();

                if (count == 0) {
                    showToast("已经是最新微博，请稍后刷新。");
                } else {
                    showToast("收到" + count + "条新微博");
                }

                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        WeiboListAdapter adapter = new WeiboListAdapter(getActivity(),
                                MumuWeiboUtility.WeiboInfoList);
                        myListView.setAdapter(adapter);

                        myListView.onRefreshComplete();
                        MumuWeiboUtility.isFlushingWeibo = false;
                        MumuWeiboUtility.saveWeiboList(getActivity(), MumuWeiboUtility.LIST_FLAG
                                .PUBLIC);
                    }
                });
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                mHandler.post(new Runnable() {
                    public void run() {
                        myListView.onRefreshComplete();
                        MumuWeiboUtility.isFlushingWeibo = false;
                    }
                });

            }
        }

        @Override
        public void onError(WeiboException arg0) {
            // TODO Auto-generated method stub
            if (progressDialog != null) progressDialog.dismiss();
            if (anim != null) anim.cancel();

            showToast(WeiboErrorHelper.WeiboError(arg0));
            mHandler.post(new Runnable() {
                public void run() {
                    myListView.onRefreshComplete();
                    MumuWeiboUtility.isFlushingWeibo = false;
                }
            });


        }

        @Override
        public void onIOException(IOException arg0) {
            // TODO Auto-generated method stub
            if (progressDialog != null) progressDialog.dismiss();
            if (anim != null) anim.cancel();
            //	if(anim2!=null)anim2.cancel();
            showToast("刷新微博失败");
            mHandler.post(new Runnable() {
                public void run() {
                    myListView.onRefreshComplete();
                    MumuWeiboUtility.isFlushingWeibo = false;
                }
            });

        }

    }


    private AdapterView.OnItemClickListener listItemClickListener = new AdapterView
            .OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            // TODO Auto-generated method stub
            Intent i = new Intent();
            i.putExtra("position", arg2 - 1);
            i.putExtra("weibo", MumuWeiboUtility.WeiboInfoList.get(arg2 - 1));
            i.setClass(getActivity(), WeiboDetail.class);
            getActivity().startActivity(i);
        }
    };


    View.OnClickListener btn_listener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // 发送微博按钮
            if (v == sendWeibo) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), WriteWeibo.class);
                startActivity(intent);
            }
            //刷新微博
            else if (v == flushWeibo) {
                flushWeibo();
            }
        }
    };

    //长按listview弹出菜单
    AdapterView.OnItemLongClickListener mOnItemLongClickListener = new AdapterView
            .OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
            // TODO Auto-generated method stub

            final WeiboInfo weiboInfo0 = MumuWeiboUtility.WeiboInfoList.get(arg2 - 1);

            ShareOperation.showWeiboOperation(getActivity(), weiboInfo0);
            return true;
        }

    };
}
