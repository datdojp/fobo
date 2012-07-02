package com.forboss.api;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.forboss.ForBossViewPagerFragmentActivity;
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
		Dao<Article, String> articleDao;
		try {
			articleDao = DatabaseHelper.getHelper(this).getArticleDao();
		} catch (SQLException e1) {
			ForBossUtils.alert(this, "Cơ sở dữ liệu bị lỗi. Hãy khởi động lại ứng dụng.");
			e1.printStackTrace();
			return;
		}
		for(String aCate : ForBossUtils.getCategoryList()) {
			List<Article> data = ForBossViewPagerFragmentActivity.cateDataMapping.get(aCate);
			if (data != null) {
				for(Article anArticle : data) {
					if (anArticle.getThumbnail() != null && anArticle.getPictureLocation() == null) {
						try {
							String pictureLocation = "article_" + anArticle.getId();
							ForBossUtils.downloadAndSaveToInternalStorage(
									StringUtils.replace(anArticle.getThumbnail(), " ", "%20"), 
									pictureLocation, this);
							anArticle.setPictureLocation(pictureLocation);
							articleDao.update(anArticle);
						} catch (IOException e) {
							Log.e(this.getClass().getName(), e.getMessage());
							e.printStackTrace();
						} catch (SQLException e) {
							Log.e(this.getClass().getName(), e.getMessage());
							e.printStackTrace();
						}
					}
				}
			}
			ArticleListBuilder builder = ForBossViewPagerFragmentActivity.cateBuilderMapping.get(aCate);
			if (builder != null) {
				builder.refresh();
			}
		}
	}

}
