package com.forboss;

import java.sql.SQLException;

import android.os.Bundle;
import android.view.ViewGroup;

import com.forboss.utils.ArticleListBuilder;
import com.forboss.utils.ForBossUtils;

public abstract class SpecialArticleListActivity extends TracableActivity {
	private ViewGroup wrapper;
	private ArticleListBuilder builder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutId());

		wrapper = (ViewGroup) findViewById(getWrapperId());
		try {
			builder = ForBossUtils.initSpecialArticleList(this, getCategory(), wrapper);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		ForBossUtils.initTabHeader(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (builder != null) {
			builder.destroy();
		}
	}

	protected abstract int getLayoutId();
	protected abstract int getWrapperId();
	protected abstract String getCategory();
}
