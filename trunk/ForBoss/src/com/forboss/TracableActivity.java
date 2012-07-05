package com.forboss;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;


public abstract class TracableActivity extends Activity {
	private static List<Activity> activityList = new ArrayList<Activity>();
	
	private GoogleAnalyticsTracker tracker;
	
	public  static void finishAll() {
		if (activityList != null) {
			for(Activity activity : activityList) {
				activity.finish();
			}
			activityList.clear();
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activityList.add(this);
		
		// Init Google Analytics
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.startNewSession("UA-3013465-32", this);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		activityList.remove(this);
		
		if (tracker != null) {
			tracker.stopSession();
		}
	}
	
	
}
