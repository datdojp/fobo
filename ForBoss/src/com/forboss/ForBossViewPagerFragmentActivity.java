package com.forboss;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forboss.adapter.PagerAdapter;
import com.forboss.data.model.Article;
import com.forboss.data.utils.DatabaseHelper;
import com.forboss.fragment.ArticleListFragment;
import com.forboss.utils.ArticleListBuilder;
import com.forboss.utils.ForBossUtils;
import com.j256.ormlite.dao.Dao;


public class ForBossViewPagerFragmentActivity extends FragmentActivity {
	private static List<Activity> activityList = new ArrayList<Activity>();
	public  static void finishAll() {
		if (activityList != null) {
			for(Activity activity : activityList) {
				activity.finish();
			}
			activityList.clear();
		}
	}
	private ForBossViewPagerFragmentActivity instance;
	
	// wrappers
	//	private ViewGroup mainWrapper;
	private ViewGroup articleListWrapper;
	//	private ViewGroup eventListWrapper;
	// private ViewGroup c360ListWrapper;
	//	private ViewGroup productListWrapper;

	// article
	private ViewGroup articleViewPagerIndicator;

	// data
	private Map<String, List<Article>> articleData;
	// private List<Article> eventData;
	// private List<Article> c360Data;

	private String group;
	private List<String> categories;


	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		super.setContentView(R.layout.article_list_pager);
		activityList.add(this);
		this.instance = this;
		
		group = (String) ForBossUtils.getBundleData("group");
		categories = ForBossUtils.getCategoriesOfGroup(group);

		// get wrapper from layout
		//		mainWrapper = (ViewGroup) findViewById(R.id.mainWrapper);
		//		productListWrapper = (ViewGroup) findViewById(R.id.productListWrapper);
		//		introLayoutWrapper = (ViewGroup) findViewById(R.id.introLayoutWrapper);
		//		eventListWrapper = (ViewGroup) findViewById(R.id.eventListWrapper);
		// c360ListWrapper = (ViewGroup) findViewById(R.id.c360ListWrapper);

		// all initializations
		ForBossUtils.initTabHeader(this);

		try {
			initArticleList();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void initArticleList() throws SQLException {
		articleListWrapper = (ViewGroup) findViewById(R.id.articleListWrapper);
		articleViewPagerIndicator = (ViewGroup) articleListWrapper
				.findViewById(R.id.viewPagerIndicator);
		Dao<Article, String> articleDao = DatabaseHelper.getHelper(this)
				.getArticleDao();

		List<Fragment> fragments = new ArrayList<Fragment>();
		for (String aCate : categories) {
			List<Article> data = MainActivity.cateDataMapping.get(aCate);

			ArticleListFragment aFragment = (ArticleListFragment) Fragment
					.instantiate(this, ArticleListFragment.class.getName());
			aFragment.setCategory(aCate);
			aFragment.setContext(this);
			aFragment.setData(data);
			fragments.add(aFragment);

			// store the builder and data
			MainActivity.cateBuilderMapping.put(aCate, aFragment.getArticleListBuilder());
			MainActivity.cateDataMapping.put(aCate, data);

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
				categoryText.setText( ForBossUtils.getConfig(categories.get(position)) );
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
		categoryText.setText(ForBossUtils.getConfig(categories.get(0)));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		activityList.remove(this);
		
		for(String aCate : categories) {
			ArticleListBuilder builder = MainActivity.cateBuilderMapping.get(aCate);
			if (builder != null) {
				builder.destroy();
				MainActivity.cateBuilderMapping.put(aCate, null);
			}
		}
	}

	@Override
	public void onBackPressed() {
		finish();
		MainActivity.getInstance().finish();
	}
	
	
}
