package com.forboss;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import com.forboss.data.model.Article;
import com.forboss.utils.ArticleListBuilder;
import com.forboss.utils.ForBossUtils;

public class FavArticleListActivity extends TracableActivity {
	private List<Article> favData = new ArrayList<Article>();
	private ArticleListBuilder builder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fav_article_list);

		// get list of favorite article
		favData.clear();
		for (String cate : ForBossViewPagerFragmentActivity.getInstance().cateDataMapping.keySet()) {
			List<Article> data = ForBossViewPagerFragmentActivity.getInstance().cateDataMapping.get(cate);
			if (data != null) {
				for(Article article : data) {
					if (article.isLike()) {
						favData.add(article);
					}
				}
			}
		}
		
		// init header
		ForBossUtils.initTabHeader(this);

		// init article list
		try {
			builder = initList();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (builder != null) {
			builder.destroy();
		}
	}	

	private ArticleListBuilder initList() throws SQLException {
//		Dao<Article, String> articleDao = DatabaseHelper.getHelper(this).getArticleDao();
		ViewGroup favArticleListWrapper = (ViewGroup) findViewById(R.id.favArticleListWrapper);

		ArticleListBuilder builder = new ArticleListBuilder();
		favArticleListWrapper.addView(builder.build(this, getLayoutInflater(), favArticleListWrapper, favData, null));

		// store data and builder
		//		ForBossViewPagerFragmentActivity.cateBuilderMapping.put(cate, builder);
		//		ForBossViewPagerFragmentActivity.cateDataMapping.put(cate, data);

		// set the category title
		TextView categoryText = (TextView) favArticleListWrapper.findViewById(R.id.categoryText);
		categoryText.setText( "DANH SÁCH YÊU THÍCH CỦA BẠN" );

		return builder;
	}

}
