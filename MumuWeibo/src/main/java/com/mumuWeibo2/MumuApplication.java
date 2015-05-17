package com.mumuWeibo2;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

public class MumuApplication extends Application{

	@Override
	public void onCreate() {
		super.onCreate();
		Fresco.initialize(this);
	}
}
