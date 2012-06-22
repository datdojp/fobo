package com.forboss;

import android.app.Application;
import android.content.Context;

public class ForBossApplication extends Application {
	private static ForBossApplication instance;
	
	public ForBossApplication() {
		instance = this;
	}
	
	public static Context getAppContext() {
		return instance;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

}
