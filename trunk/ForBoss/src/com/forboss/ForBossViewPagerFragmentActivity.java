package com.forboss;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forboss.adapter.PagerAdapter;
import com.forboss.api.ArticlePictureLoadAsyncTask;
import com.forboss.api.ArticleService;
import com.forboss.data.model.Article;
import com.forboss.data.utils.DatabaseHelper;
import com.forboss.fragment.ArticleListFragment;
import com.forboss.utils.ArticleListBuilder;
import com.forboss.utils.ForBossUtils;
import com.forboss.utils.URL;
import com.j256.ormlite.dao.Dao;

public class ForBossViewPagerFragmentActivity extends FragmentActivity {
	// class instance
	private static ForBossViewPagerFragmentActivity instance;

	public static ForBossViewPagerFragmentActivity getInstance() {
		return instance;
	}

	// constant
	private static final String APP_PREF = "forboss";
	private static final String USER_EMAIL = "user.email";

	// wrappers
	private ViewGroup mainWrapper;
	private ViewGroup articleListWrapper;
	private ViewGroup eventListWrapper;
	// private ViewGroup c360ListWrapper;
	private ViewGroup productListWrapper;
	private ViewGroup introLayoutWrapper;
	private ViewGroup loginLayoutWrapper;

	// article
	private ViewGroup articleViewPagerIndicator;
	public static Map<String, ArticleListBuilder> cateBuilderMapping = new HashMap<String, ArticleListBuilder>();
	public static Map<String, List<Article>> cateDataMapping = new HashMap<String, List<Article>>();

	// data
	private Map<String, List<Article>> articleData;
	// private List<Article> eventData;
	// private List<Article> c360Data;

	// handlers
	private Handler afterSyncArticleHandler;
	private Handler afterPTRArticleHandler;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		super.setContentView(R.layout.main);

		// set the instance
		instance = this;

		// get wrapper from layout
		mainWrapper = (ViewGroup) findViewById(R.id.mainWrapper);
		productListWrapper = (ViewGroup) findViewById(R.id.productListWrapper);
		introLayoutWrapper = (ViewGroup) findViewById(R.id.introLayoutWrapper);
		eventListWrapper = (ViewGroup) findViewById(R.id.eventListWrapper);
		// c360ListWrapper = (ViewGroup) findViewById(R.id.c360ListWrapper);

		// all initializations
		initLoginLayout();
		ForBossUtils.initTabHeader(this);

		afterSyncArticleHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				introLayoutWrapper.setVisibility(View.GONE);
				try {
					initArticleList();
					// initNotPagedList(ForBossUtils.getEventCategory(),
					// eventListWrapper);
					// initNotPagedList(ForBossUtils.getC360Category(),
					// c360ListWrapper);
					syncArticlePicture();
				} catch (SQLException e) {
					Log.e(this.getClass().getName(), e.getMessage());
					e.printStackTrace();
				}

			}
		};

		afterPTRArticleHandler = new Handler() {
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

	private void initArticleList() throws SQLException {
		articleListWrapper = (ViewGroup) findViewById(R.id.articleListWrapper);
		articleViewPagerIndicator = (ViewGroup) articleListWrapper
				.findViewById(R.id.viewPagerIndicator);
		Dao<Article, String> articleDao = DatabaseHelper.getHelper(this)
				.getArticleDao();

		List<Fragment> fragments = new ArrayList<Fragment>();
		for (String aCate : ForBossUtils.getArticleCategoryList()) {
			List<Article> data = ForBossUtils.getArticleOfCategoryFromDb(aCate,
					articleDao, instance);

			ArticleListFragment aFragment = (ArticleListFragment) Fragment
					.instantiate(this, ArticleListFragment.class.getName());
			aFragment.setCategory(aCate);
			aFragment.setContext(this);
			aFragment.setData(data);
			fragments.add(aFragment);

			// store the builder and data
			cateBuilderMapping.put(aCate, aFragment.getArticleListBuilder());
			cateDataMapping.put(aCate, data);

			// add indicator
			ImageView anIndicator = new ImageView(this);
			LinearLayout.LayoutParams anIndicatorLayoutParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			anIndicator.setPadding(0, 0, 5, 0);
			anIndicator.setImageResource(R.drawable.dot_);
			articleViewPagerIndicator.addView(anIndicator,
					anIndicatorLayoutParams);
		}
		((ImageView) articleViewPagerIndicator.getChildAt(0)).setImageResource(R.drawable.dot);

		ViewPager viewPager = (ViewPager) articleListWrapper
				.findViewById(R.id.viewpager);
		PagerAdapter mPagerAdapter = new PagerAdapter(
				super.getSupportFragmentManager(), fragments);
		viewPager.setAdapter(mPagerAdapter);
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				int nChild = articleViewPagerIndicator.getChildCount();
				for (int i = 0; i < nChild; i++) {
					ImageView anIndicator = (ImageView) articleViewPagerIndicator
							.getChildAt(i);
					anIndicator.setPadding(0, 0, 5, 0);
					if (i == position) {
						anIndicator.setImageResource(R.drawable.dot);
					} else {
						anIndicator.setImageResource(R.drawable.dot_);
					}
				}

				TextView categoryText = (TextView) articleListWrapper
						.findViewById(R.id.categoryText);
				categoryText.setText(ForBossUtils.getConfig(ForBossUtils
						.getArticleCategoryList().get(position)));
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
		});

		TextView categoryText = (TextView) articleListWrapper
				.findViewById(R.id.categoryText);
		categoryText.setText(ForBossUtils.getConfig(ForBossUtils
				.getArticleCategoryList().get(0)));
	}

	/*
	 * private void initTabHeader() { View tabHeaderWrapper =
	 * findViewById(R.id.tabHeaderWrapper); ImageButton articleButton =
	 * (ImageButton) tabHeaderWrapper.findViewById(R.id.contentButton);
	 * articleButton.setOnClickListener(new View.OnClickListener() {
	 * 
	 * @Override public void onClick(View v) { ForBossUtils.addView(mainWrapper,
	 * articleListWrapper); ForBossUtils.removeView(mainWrapper,
	 * eventListWrapper); ForBossUtils.removeView(mainWrapper,
	 * productListWrapper); // ForBossUtils.removeView(mainWrapper,
	 * c360ListWrapper); } }); ImageButton eventButton = (ImageButton)
	 * tabHeaderWrapper.findViewById(R.id.eventButton);
	 * eventButton.setOnClickListener(new View.OnClickListener() {
	 * 
	 * @Override public void onClick(View v) {
	 * ForBossUtils.removeView(mainWrapper, articleListWrapper);
	 * ForBossUtils.addView(mainWrapper, eventListWrapper);
	 * ForBossUtils.removeView(mainWrapper, productListWrapper); //
	 * ForBossUtils.removeView(mainWrapper, c360ListWrapper); } }); ImageButton
	 * c360Button = (ImageButton)
	 * tabHeaderWrapper.findViewById(R.id.c360Button);
	 * c360Button.setOnClickListener(new View.OnClickListener() {
	 * 
	 * @Override public void onClick(View v) {
	 * ForBossUtils.removeView(mainWrapper, articleListWrapper);
	 * ForBossUtils.removeView(mainWrapper, eventListWrapper);
	 * ForBossUtils.removeView(mainWrapper, productListWrapper); //
	 * ForBossUtils.addView(mainWrapper, c360ListWrapper); } }); ImageButton
	 * productListButton = (ImageButton) findViewById(R.id.productListButton);
	 * productListButton.setOnClickListener(new View.OnClickListener() {
	 * 
	 * @Override public void onClick(View v) {
	 * ForBossUtils.removeView(mainWrapper, articleListWrapper);
	 * ForBossUtils.removeView(mainWrapper, eventListWrapper);
	 * ForBossUtils.addView(mainWrapper, productListWrapper); //
	 * ForBossUtils.removeView(mainWrapper, c360ListWrapper); } });
	 * articleButton.performClick(); }
	 */

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

	/*
	 * private void initEventList() throws SQLException { eventListWrapper =
	 * (ViewGroup) findViewById(R.id.eventListWrapper);
	 * 
	 * Dao<Article, String> articleDao =
	 * DatabaseHelper.getHelper(this).getArticleDao(); eventData =
	 * ForBossUtils.getArticleOfCategoryFromDb(ForBossUtils.getEventCategory(),
	 * articleDao, instance);
	 * 
	 * LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
	 * LinearLayout.LayoutParams.FILL_PARENT,
	 * LinearLayout.LayoutParams.FILL_PARENT); ArticleListBuilder builder = new
	 * ArticleListBuilder(); eventListWrapper.addView(builder.build(this,
	 * this.getLayoutInflater(), eventListWrapper, eventData));
	 * 
	 * // store data and builder
	 * cateBuilderMapping.put(ForBossUtils.getEventCategory(), builder);
	 * cateDataMapping.put(ForBossUtils.getEventCategory(), eventData);
	 * 
	 * // set the category title TextView categoryText = (TextView)
	 * eventListWrapper.findViewById(R.id.categoryText); categoryText.setText(
	 * ForBossUtils.getConfig(ForBossUtils.getEventCategory()) ); }
	 */

	/*
	 * private void initNotPagedList(String cate, ViewGroup wrapper) throws
	 * SQLException { Dao<Article, String> articleDao =
	 * DatabaseHelper.getHelper(this).getArticleDao(); List<Article> data =
	 * ForBossUtils.getArticleOfCategoryFromDb(cate, articleDao, instance);
	 * 
	 * LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
	 * LinearLayout.LayoutParams.FILL_PARENT,
	 * LinearLayout.LayoutParams.FILL_PARENT); ArticleListBuilder builder = new
	 * ArticleListBuilder(); wrapper.addView(builder.build(this,
	 * this.getLayoutInflater(), wrapper, data));
	 * 
	 * // store data and builder cateBuilderMapping.put(cate, builder);
	 * cateDataMapping.put(cate, data);
	 * 
	 * // set the category title TextView categoryText = (TextView)
	 * wrapper.findViewById(R.id.categoryText); categoryText.setText(
	 * ForBossUtils.getConfig(cate) ); }
	 */

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

	private boolean isInitArticleList = false;

	public void refreshArticleList() {
		if (!isInitArticleList) {
			Message message = afterSyncArticleHandler.obtainMessage();
			afterSyncArticleHandler.sendMessage(message);
			isInitArticleList = true;
		} else {
			Message message = afterPTRArticleHandler.obtainMessage();
			afterPTRArticleHandler.sendMessage(message);
		}
	}

	public void navigateToEventList() {
		Intent intent = new Intent(this, EventListActivity.class);
		startActivity(intent);
	}

	public void navigateToC360List() {
		Intent intent = new Intent(this, C360ListActivity.class);
		startActivity(intent);
	}

	public void navigateToProductList() {
		Intent intent = new Intent(this, ProductListActivity.class);
		startActivity(intent);
	}

	public void navigateToFavArticleList() {
		Intent intent = new Intent(this, FavArticleListActivity.class);
		startActivity(intent);
	}
}
