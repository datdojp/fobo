package com.forboss;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.forboss.api.SendAsyncTask;
import com.forboss.data.model.Article;
import com.forboss.data.model.Event;
import com.forboss.data.utils.DatabaseHelper;
import com.forboss.fragment.ArticleListFragment;
import com.forboss.utils.ArticleListBuilder;
import com.forboss.utils.ForBossUtils;
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
	private View articleListWrapper;
	private View eventListWrapper;
	private View productListWrapper;
	private View introLayoutWrapper;
	private View loginLayoutWrapper;

	// article
	private ViewGroup articleViewPagerIndicator;
	public static Map<String, ArticleListBuilder> cateBuilderMapping = new HashMap<String, ArticleListBuilder>();
	public static Map<String, List<Article>> cateDataMapping = new HashMap<String, List<Article>>();
	
	// data
	private Map<String, List<Article>> articleData;
	private List<Event> eventData;
	
	// handlers
	private Handler afterSyncArticleHandler;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		super.setContentView(R.layout.main);
		
		// set the instance
		instance = this;

		// get wrapper from layout
		mainWrapper = (ViewGroup) findViewById(R.id.mainWrapper);
		eventListWrapper = findViewById(R.id.eventListWrapper);
		productListWrapper = findViewById(R.id.productListWrapper);
		introLayoutWrapper = findViewById(R.id.introLayoutWrapper);

		// all initializations
		initLoginLayout();
		initTabHeader();
		
		afterSyncArticleHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				introLayoutWrapper.setVisibility(View.GONE);
				try {
					initArticleList();
					syncArticlePicture();
				} catch (SQLException e) {
					Log.e(this.getClass().getName(), e.getMessage());
					e.printStackTrace();
				}

			}
		};
	}

	private void initArticleList() throws SQLException {
		articleListWrapper = findViewById(R.id.articleListWrapper);
		articleViewPagerIndicator = (ViewGroup) articleListWrapper.findViewById(R.id.viewPagerIndicator);
		Dao<Article, String> articleDao = DatabaseHelper.getHelper(this).getArticleDao();
		
		List<Fragment> fragments =  new ArrayList<Fragment>();
		for(String aCate : ForBossUtils.getCategoryList()) {
			Article sampleArticle = new Article();
			sampleArticle.setCategory(aCate);
			List<Article> data = articleDao.queryForMatching(sampleArticle);
			
			ArticleListFragment aFragment = (ArticleListFragment) Fragment.instantiate(this, ArticleListFragment.class.getName());
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
			anIndicator.setImageResource(R.drawable.dot);
			articleViewPagerIndicator.addView(anIndicator, anIndicatorLayoutParams);
		}

		ViewPager viewPager = (ViewPager) articleListWrapper.findViewById(R.id.viewpager);
		PagerAdapter mPagerAdapter  = new PagerAdapter(super.getSupportFragmentManager(), fragments);
		viewPager.setAdapter(mPagerAdapter);
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				int nChild = articleViewPagerIndicator.getChildCount();
				for(int i = 0; i < nChild; i++) {
					ImageView anIndicator = (ImageView) articleViewPagerIndicator.getChildAt(i);
					if (i == position) {
						anIndicator.setImageResource(R.drawable.dot_);
					} else {
						anIndicator.setImageResource(R.drawable.dot);
					}
				}
				
				setCategoryText(position);
			}

			@Override
			public void onPageScrollStateChanged(int arg0) { }

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) { }
		});
		
		setCategoryText(0);
	}

	private void setCategoryText(int position) {
		TextView categoryText = (TextView) mainWrapper.findViewById(R.id.categoryText);
		categoryText.setText( ForBossUtils.getConfig(ForBossUtils.getCategoryList().get(position)) );
	}
	
	private void initTabHeader() {
		View tabHeaderWrapper = findViewById(R.id.tabHeaderWrapper);
		ImageButton articleButton = (ImageButton) tabHeaderWrapper.findViewById(R.id.contentButton);
		articleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ForBossUtils.addView(mainWrapper, articleListWrapper);
				ForBossUtils.removeView(mainWrapper, eventListWrapper);
				ForBossUtils.removeView(mainWrapper, productListWrapper);
			}
		});
		ImageButton eventButton = (ImageButton) tabHeaderWrapper.findViewById(R.id.eventButton);
		eventButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ForBossUtils.removeView(mainWrapper, articleListWrapper);
				ForBossUtils.addView(mainWrapper, eventListWrapper);
				ForBossUtils.removeView(mainWrapper, productListWrapper);
			}
		});
		ImageButton moreButton = (ImageButton) tabHeaderWrapper.findViewById(R.id.moreButton);
		moreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ForBossUtils.removeView(mainWrapper, articleListWrapper);
				ForBossUtils.removeView(mainWrapper, eventListWrapper);
				ForBossUtils.addView(mainWrapper, productListWrapper);
			}
		});
		articleButton.performClick();
	}

	private void initLoginLayout() {
		loginLayoutWrapper = findViewById(R.id.loginLayoutWrapper);
		if (isLoggin()) {
			loginLayoutWrapper.setVisibility(View.GONE);
			return;
		}
		ImageButton submitButton = (ImageButton) loginLayoutWrapper.findViewById(R.id.submitButton);
		submitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO: validate email

				Handler sendEmailSuccessHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						loginLayoutWrapper.setVisibility(View.GONE);

						SharedPreferences settings = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = settings.edit();
						editor.putString(USER_EMAIL, getUserEmail());
						editor.commit();
					}
				};

				Handler sendEmailFailHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						ForBossUtils.alert(getApplicationContext(), "Xác nhận email thất bại. Xin hãy thử lại lần nữa.");
					}
				};
				SendAsyncTask thread = new SendAsyncTask();
				thread.setUrl(SendAsyncTask.LOGIN_URL + "/" + getUserEmail());
				thread.setTaskFinishedHandler(sendEmailSuccessHandler);
				thread.setTaskFailedHandler(sendEmailFailHandler);
				thread.execute("");
			}
		});
	}

	private String getUserEmail() {
		return ( (EditText)loginLayoutWrapper.findViewById(R.id.userEmailEdit) ).getText().toString();
	}

	private boolean isLoggin() {
		SharedPreferences settings = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
		String userEmail = settings.getString(USER_EMAIL, null);
		return userEmail != null;
	}

	private void syncArticleContent() {
		final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, ArticleService.class);
		this.startService(intent);
	}

	private void syncArticlePicture() {
		final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, ArticlePictureLoadAsyncTask.class);
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
		}
	}

}
