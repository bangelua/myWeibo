package com.mumuWeibo2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

public class SplashScreen extends Activity {

    public void onCreate(Bundle bb) {
        super.onCreate(bb);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);


        final Intent i = new Intent();
        i.setClass(SplashScreen.this, AppMain.class);

        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                SplashScreen.this.startActivity(i);
                finish();
            }

        });

    }

}
