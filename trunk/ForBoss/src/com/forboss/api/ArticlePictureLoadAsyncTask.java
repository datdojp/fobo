package com.forboss.api;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.forboss.data.model.Article;
import com.forboss.data.utils.DatabaseHelper;
import com.forboss.utils.ArticleListBuilder;
import com.forboss.utils.ForBossUtils;
import com.j256.ormlite.dao.Dao;

public class ArticlePictureLoadAsyncTask extends IntentService {

	public ArticlePictureLoadAsyncTask() {
		super("ArticlePictureLoadAsyncTask");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// get list of articles that need to load pictures
		List<ArticleListBuilder> articleListBuilderList = (List<ArticleListBuilder>) ForBossUtils.getBundleData("articleListBuilderList");
		List<Article> articlesNeedToLoadPictures = new ArrayList<Article>();
		for (ArticleListBuilder aBuilder : articleListBuilderList) {
			for (Article anArticle : aBuilder.getPtrNewsListData()) {
				if (anArticle.getThumbnail() != null && anArticle.getPictureLocation() == null) {
					articlesNeedToLoadPictures.add(anArticle);
				}
			}
		}

		if (articlesNeedToLoadPictures != null) {
			try {
				Dao<Article, String> articleDao = DatabaseHelper.getHelper(this).getArticleDao();
				for (Article anArticle : articlesNeedToLoadPictures) {
					// double check the picture location
					if (anArticle.getThumbnail() != null && anArticle.getPictureLocation() == null) {
						String pictureLocation = "article_" + anArticle.getId();
						ForBossUtils.downloadAndSaveToInternalStorage(anArticle.getThumbnail(), pictureLocation, this);
						anArticle.setPictureLocation(pictureLocation);
						articleDao.update(anArticle);
					}
				}
			} catch (IOException e) {
				Log.e(this.getClass().getName(), e.getMessage());
				e.printStackTrace();
			} catch (SQLException e) {
				Log.e(this.getClass().getName(), e.getMessage());
				e.printStackTrace();
			}
		}
		
		// notify change to builder
		for (ArticleListBuilder aBuilder : articleListBuilderList) {
			aBuilder.refresh();
		}
	}

}
