package com.forboss;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.forboss.api.ArticlePictureLoadAsyncTask;
import com.forboss.api.ArticleService;
import com.forboss.data.model.Article;
import com.forboss.data.utils.DatabaseHelper;
import com.forboss.utils.ArticleListBuilder;
import com.forboss.utils.ForBossUtils;
import com.forboss.utils.URL;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.j256.ormlite.dao.Dao;

public class MainActivity extends Activity {
	private static MainActivity instance;
	public static MainActivity getInstance() {
		return instance;
	}

	private static final String APP_PREF = "forboss";
	private static final String USER_EMAIL = "user.email";

	private ViewGroup introLayoutWrapper;
	private ViewGroup loginLayoutWrapper;

	public static Map<String, ArticleListBuilder> cateBuilderMapping = new HashMap<String, ArticleListBuilder>();
	public static Map<String, List<Article>> cateDataMapping = new HashMap<String, List<Article>>();

	private Handler afterSyncAtLoadingAppHandler;
	private Handler afterManualSyncHandler;

	private GoogleAnalyticsTracker tracker;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.main);
		instance = this;
		
		// Init Google Analytics
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.startNewSession("UA-3013465-32", this);

		introLayoutWrapper = (ViewGroup) findViewById(R.id.introLayoutWrapper);
		initLoginLayout();

		afterSyncAtLoadingAppHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// get all article from db
				try {
					Dao<Article, String> articleDao = DatabaseHelper.getHelper(
							instance).getArticleDao();
					for (String aCate : ForBossUtils.getAllCategories()) {
						cateDataMapping.put(aCate, ForBossUtils.getArticleOfCategoryFromDb(aCate, articleDao, instance));
					}
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
				introLayoutWrapper.setVisibility(View.GONE);
//				try {
//					initArticleList();
				if (isLoggin()) {
					navigateToPost();
				}
					syncArticlePicture();
//				} catch (SQLException e) {
//					Log.e(this.getClass().getName(), e.getMessage());
//					e.printStackTrace();
//				}

			}
		};

		afterManualSyncHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// update content
				try {
					Dao<Article, String> articleDao = DatabaseHelper.getHelper(
							instance).getArticleDao();
					for (String cate : cateDataMapping.keySet()) {
						List<Article> data = cateDataMapping.get(cate);
						data.clear();
						List<Article> newData = ForBossUtils
								.getArticleOfCategoryFromDb(cate, articleDao,
										instance);
						data.addAll(newData);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

				// close all pull-to-refresh + notify all
				for (String cate : cateBuilderMapping.keySet()) {
					ArticleListBuilder builder = cateBuilderMapping.get(cate);
					if (builder != null && builder.getPtrArticleList() != null) {
						builder.getPtrArticleList().onRefreshComplete();
						builder.refresh();
					}
				}

				// sync pictures
				syncArticlePicture();
			}
		};
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (tracker != null) {
			tracker.stopSession();
		}
	}

	private void initLoginLayout() {
		loginLayoutWrapper = (ViewGroup) findViewById(R.id.loginLayoutWrapper);
		if (isLoggin()) {
			loginLayoutWrapper.setVisibility(View.GONE);
			return;
		}
		ImageButton submitButton = (ImageButton) loginLayoutWrapper
				.findViewById(R.id.submitButton);
		submitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText userEmailEdit = (EditText) loginLayoutWrapper
						.findViewById(R.id.userEmailEdit);
				String email = userEmailEdit.getText().toString();

				// validate email
				if (email == null || "".equals(email.trim())) {
					ForBossUtils.alert(instance, "Hãy nhập email của bạn");
					return;
				}
				if (!ForBossUtils.isValidEmailAddress(email.trim())) {
					ForBossUtils.alert(instance, "Email không đúng chuẩn.");
					return;
				}

				// handler to handle when the server response
				Handler sendEmailFinishedHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						JSONObject finalResult = (JSONObject) msg.obj;
						boolean success = false;
						try {
							success = (Boolean) finalResult.get("success");
						} catch (JSONException e) {
							e.printStackTrace();
						}
						if (success) {
							loginLayoutWrapper.setVisibility(View.GONE);

							SharedPreferences settings = getSharedPreferences(
									APP_PREF, Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = settings.edit();
							editor.putString(USER_EMAIL, getUserEmail());
							editor.commit();
							
							navigateToPost();
						} else {
							ForBossUtils
							.alert(getApplicationContext(),
									"Xác nhận email thất bại. Xin hãy thử lại lần nữa.");
						}
					}
				};

				// send email to server
				ForBossUtils.get(URL.LOGIN_URL + "/" + getUserEmail(),
						sendEmailFinishedHandler);
			}
		});
	}

	private String getUserEmail() {
		return ((EditText) loginLayoutWrapper.findViewById(R.id.userEmailEdit))
				.getText().toString().trim();
	}

	private boolean isLoggin() {
		SharedPreferences settings = getSharedPreferences(APP_PREF,
				Context.MODE_PRIVATE);
		String userEmail = settings.getString(USER_EMAIL, null);
		return userEmail != null;
	}

	public void syncArticleContent() {
		final Intent intent = new Intent(Intent.ACTION_SYNC, null, this,
				ArticleService.class);
		this.startService(intent);
	}

	public void syncArticlePicture() {
		final Intent intent = new Intent(Intent.ACTION_SYNC, null, this,
				ArticlePictureLoadAsyncTask.class);
		this.startService(intent);
	}

	@Override
	protected void onStart() {
		super.onStart();
		syncArticleContent();
	}

	private boolean isSyncAtLoadingApp = true;

	public void refreshArticleList() {
		if (isSyncAtLoadingApp) {
			Message message = afterSyncAtLoadingAppHandler.obtainMessage();
			afterSyncAtLoadingAppHandler.sendMessage(message);
			isSyncAtLoadingApp = false;
		} else {
			Message message = afterManualSyncHandler.obtainMessage();
			afterManualSyncHandler.sendMessage(message);
		}
	}

	public void navigateToPost() {
		Intent intent = new Intent(this, PostActivity.class);
		ForBossUtils.putBundleData("group", "post");
		startActivity(intent);
	}

	public void navigateToEvent() {
		Intent intent = new Intent(this, EventActivity.class);
		ForBossUtils.putBundleData("group", "event");
		startActivity(intent);
	}

	public void navigateToC360() {
		Intent intent = new Intent(this, C360Activity.class);
		ForBossUtils.putBundleData("group", "c360");
		startActivity(intent);
	}

	public void navigateToProduct() {
		Intent intent = new Intent(this, ProductListActivity.class);
		startActivity(intent);
	}

	public void navigateToFavorite() {
		Intent intent = new Intent(this, FavArticleListActivity.class);
		startActivity(intent);
	}
}
