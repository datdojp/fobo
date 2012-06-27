package com.forboss;

import android.app.Application;
import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.forboss.sns.facebook.SessionEvents;
import com.forboss.sns.facebook.SessionEvents.AuthListener;
import com.forboss.sns.facebook.SessionStore;
import com.forboss.sns.facebook.Utility;
import com.forboss.utils.ForBossUtils;

public class ForBossApplication extends Application {
	private static ForBossApplication instance;
	private static final String APP_ID = "299767643453542";

	public ForBossApplication() {
		instance = this;
	}

	public static Context getAppContext() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// Create the Facebook Object using the app id.
		Utility.mFacebook = new Facebook(APP_ID);
		// Instantiate the asynrunner object for asynchronous api calls.
		Utility.mAsyncRunner = new AsyncFacebookRunner(Utility.mFacebook);

		// restore session if one exists
		SessionStore.restore(Utility.mFacebook, this);
		SessionEvents.addAuthListener(new FbAPIsAuthListener());

		WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		display = wm.getDefaultDisplay();
	}

	/*
	 * The Callback for notifying the application when authorization succeeds or
	 * fails.
	 */

	public class FbAPIsAuthListener implements AuthListener {

		@Override
		public void onAuthSucceed() {
			SessionStore.save(Utility.mFacebook, instance);
		}

		@Override
		public void onAuthFail(String error) {
			ForBossUtils.alert(instance, "Đăng nhập facebook thất bại.");
		}
	}

	private static Display display;
	public static Display getWindowDisplay() {
		return display;
	}
}
