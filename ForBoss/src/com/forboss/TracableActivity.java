package com.forboss;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;

public abstract class TracableActivity extends Activity {
	private static List<Activity> activityList = new ArrayList<Activity>();
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
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		activityList.remove(this);
	}
	
	
}
