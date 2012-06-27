package com.forboss;

import java.io.FileNotFoundException;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.android.DialogError;
import com.facebook.android.FacebookError;
import com.forboss.data.model.Article;
import com.forboss.data.utils.DatabaseHelper;
import com.forboss.sns.facebook.BaseDialogListener;
import com.forboss.sns.facebook.Utility;
import com.forboss.utils.ForBossUtils;
import com.forboss.utils.URL;
import com.j256.ormlite.dao.Dao;

public class ArticleDetailActivity extends Activity {
	private Article article;
	private Dao<Article, String> articleDao; 
	private ArticleDetailActivity instance;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.article_detail);
		this.instance = this;
		// get the data
		article = (Article) ForBossUtils.getBundleData("article");
		try {
			articleDao = DatabaseHelper.getHelper(instance).getArticleDao();
			if (!article.isView()) {
				article.setViews(article.getViews() + 1);
				article.setView(true);
				articleDao.update(article);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		// load article body if HTML content is not loaded yet
		if (article.getHtmlContent() == null) {
			Log.d(this.getClass().getName(), "Get HTML content for article:" + article.getId());
			ForBossUtils.get(URL.GET_ARTICLE_URL + "/" + article.getId(), new Handler() {
				@Override
				public void handleMessage(Message msg) {
					JSONObject result = (JSONObject) msg.obj;
					String body = "";
					try {
						body = (String) result.get("Body");
						article.setHtmlContent(body);
						articleDao.update(article);
						setArticleContent();
					} catch (SQLException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
		} else {
			setArticleContent();
		}

		TextView titleText = (TextView) findViewById(R.id.titleText);
		titleText.setText(article.getTitle());

		setViewText();
		setLikeText();

		if (article.getPictureLocation() != null) {
			ImageView thumbnailImage = (ImageView) findViewById(R.id.thumbnailImage);
			try {
				thumbnailImage.setImageBitmap(ForBossUtils.loadBitmapFromInternalStorage(
						article.getPictureLocation(), this));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		// init the top menu
		View topMenuWrapper = findViewById(R.id.topMenuWrapper);
		ImageButton backButton = (ImageButton) topMenuWrapper.findViewById(R.id.backButton);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		TextView categoryText = (TextView) findViewById(R.id.categoryText);
		categoryText.setText(ForBossUtils.getConfig(article.getCategory()));

		// init bottom menu
		View bottomMenuWrapper = findViewById(R.id.bottomMenuWrapper);
		ImageButton likeButton = (ImageButton) bottomMenuWrapper.findViewById(R.id.likeButton);
		likeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!article.isLike()) {
					article.setLike(true);
					article.setLikes(article.getLikes() + 1);
					try {
						articleDao.update(article);
					} catch (SQLException e) {
						e.printStackTrace();
					}
					setLikeText();

					// send like to server
					ForBossUtils.get(URL.LIKE_ARTICLE_URL + "/" + article.getId(), null);
				}
			}
		});
		ImageButton facebookButton =  (ImageButton) bottomMenuWrapper.findViewById(R.id.facebookButton);
		facebookButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle params = new Bundle();
				params.putString("name", article.getTitle());
				params.putString("caption", article.getBody());
				params.putString("description", article.getLink());
				params.putString("picture", article.getThumbnail());
				params.putString("link", article.getLink());

				Utility.mFacebook.dialog(instance, "feed", params, new BaseDialogListener() {
					
					@Override
					public void onComplete(Bundle values) {
						ForBossUtils.alert(instance, "Đăng Facebook thành công.");
					}

					@Override
					public void onFacebookError(FacebookError e) {
						super.onFacebookError(e);
						ForBossUtils.alert(instance, "Đăng Facebook thất bại. Hãy thử lại sau.");
					}

					@Override
					public void onError(DialogError e) {
						super.onError(e);
						ForBossUtils.alert(instance, "Đăng Facebook thất bại. Hãy thử lại sau.");
					}
				});
			}
		});

	}

	private void setViewText() {
		TextView nViewText = (TextView) findViewById(R.id.nViewText);
		nViewText.setText(Integer.toString(article.getViews()) + " người đã đọc bài viết");	
	}

	private void setLikeText() {
		TextView nLikeText = (TextView) findViewById(R.id.nLikeText);
		nLikeText.setText(Integer.toString(article.getLikes()) + " người thích bài viết");
	}

	private void setArticleContent() {
		WebView htmlContent = (WebView) findViewById(R.id.htmlContent);
		htmlContent.getSettings().setLoadWithOverviewMode(true);
		htmlContent.getSettings().setUseWideViewPort(true);
		htmlContent.getSettings().setDefaultFontSize(32);
		htmlContent.loadData(

				"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" +
						"<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
						"<head>" +
						"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" +
						"</head>" +
						"<body style='background-color:black; color: white;'>" + 
						article.getHtmlContent() + 
						"</body>" +
						"</html>", 

						"text/html", "UTF-8");
	}

}
