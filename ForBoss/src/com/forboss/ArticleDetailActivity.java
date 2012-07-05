package com.forboss;

import java.io.FileNotFoundException;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings.LayoutAlgorithm;
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
import com.forboss.sns.linkedin.LinkedIn;
import com.forboss.utils.ForBossUtils;
import com.forboss.utils.URL;
import com.google.code.linkedinapi.client.oauth.LinkedInAccessToken;
import com.j256.ormlite.dao.Dao;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class ArticleDetailActivity extends Activity {
	private Article article;
	private Dao<Article, String> articleDao; 
	private ArticleDetailActivity instance;
	private static LinkedIn linkedIn = new LinkedIn();
	
	private GoogleAnalyticsTracker tracker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.article_detail);
		
		// Init Google Analytics
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.startNewSession("UA-3013465-32", this);
		
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

		// Hide Time, Title, Thumbnail if this article is event
		if (article.getCategory().equals(ForBossUtils.getEventCategory())) {
			findViewById(R.id.imageClock).setVisibility(View.GONE);
			findViewById(R.id.time).setVisibility(View.GONE);
			findViewById(R.id.titleText).setVisibility(View.GONE);
			findViewById(R.id.thumbnailImage).setVisibility(View.GONE);
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

		TextView time = (TextView) findViewById(R.id.time);
		time.setText(ForBossUtils.getLastUpdateInfo(article.getCreatedTimeInDate()));

		setViewText();
		setLikeText();

		if (article.getPictureLocation() != null) {
			ImageView thumbnailImage = (ImageView) findViewById(R.id.thumbnailImage);
			try {
				Bitmap bm = ForBossUtils.loadBitmapFromInternalStorage(
						article.getPictureLocation(), this);
				thumbnailImage.setImageBitmap(bm);
				thumbnailImage.setTag(bm);
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

		// init like button
		ImageButton likeButton = (ImageButton) findViewById(R.id.likeButton);
		
		if (article.isLike()) {
			likeButton.setImageResource(R.drawable.icon_heart);
		}
		
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
					
					((ImageButton) v).setImageResource(R.drawable.icon_heart);

					// send like to server
					ForBossUtils.get(URL.LIKE_ARTICLE_URL + "/" + article.getId(), null);
				} else {
					article.setLike(false);
					article.setLikes(article.getLikes() - 1);
					try {
						articleDao.update(article);
					} catch (SQLException e) {
						e.printStackTrace();
					}
					setLikeText();
				}
			}
		});

		// init bottom menu
		View bottomMenuWrapper = findViewById(R.id.bottomMenuWrapper);
		ImageButton favArticleListButton = (ImageButton) bottomMenuWrapper.findViewById(R.id.favArticleListButton);
		favArticleListButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((Activity) v.getContext()).finish();
				ForBossViewPagerFragmentActivity.getInstance().navigateToFavArticleList();
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
		ImageButton linkedinButton = (ImageButton) bottomMenuWrapper.findViewById(R.id.linkedinButton);
		linkedinButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ForBossUtils.alertProgress(instance, "Chờ xử lý...");
				(new Thread(new Runnable() {
					@Override
					public void run() {
						linkedIn.restore(instance);
						if (linkedIn.client == null) {
							linkedIn = new LinkedIn();
							linkedIn.liToken = linkedIn.oAuthService.getOAuthRequestToken(LinkedIn.OAUTH_CALLBACK_URL);
							Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(linkedIn.liToken.getAuthorizationUrl()));
							startActivity(i);
							ForBossUtils.dismissProgress(instance);
						} else {
							postToLinkedIn();
							ForBossUtils.dismissProgress(instance);
						}
						//						Intent intent = new Intent(instance, LinkedIn.class);
						//						startActivity(intent);
					}
				})).start();
			}
		});
	}

	@Override
	protected void onNewIntent(Intent intent) {
		String verifier = intent.getData().getQueryParameter("oauth_verifier");

		linkedIn.accessToken = linkedIn.oAuthService.getOAuthAccessToken(
				linkedIn.liToken, verifier);
		linkedIn.save(instance);
		Log.d(this.getClass().getName(), "Token=" + linkedIn.accessToken.getToken() + ", Token Secret=" + linkedIn.accessToken.getTokenSecret());
		linkedIn.client = linkedIn.factory.createLinkedInApiClient(linkedIn.accessToken);
		postToLinkedIn();
	}

	private void postToLinkedIn() {
		linkedIn.client.postNetworkUpdate(article.getTitle());
	}

	private void setViewText() {
		TextView nViewText = (TextView) findViewById(R.id.nViewText);
		nViewText.setText(Integer.toString(article.getViews()) + " người đã đọc bài viết");	
	}

	private void setLikeText() {
		TextView nLikeText = (TextView) findViewById(R.id.nLikeText);
		if (article.isLike()) {
			if (article.getLikes() == 1) { // only you like the article
				nLikeText.setText("Bạn thích bài viết");
			} else if (article.getLikes() > 1) { // you and someone like the article
				nLikeText.setText("Bạn và " + (article.getLikes()-1) + " người khác thích bài viết");
			}
		} else {
			nLikeText.setText(article.getLikes() + " người thích bài viết");
		}
	}

	private void setArticleContent() {
		WebView htmlContent = (WebView) findViewById(R.id.htmlContent);
		htmlContent.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		htmlContent.getSettings().setLoadWithOverviewMode(true);
		htmlContent.getSettings().setUseWideViewPort(true);
		htmlContent.getSettings().setDefaultFontSize(32);
		htmlContent.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
		htmlContent.loadDataWithBaseURL(null, 
				"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" +
						"<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
						"<head>" +
						"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" +
						"</head>" +
						"<body style='background-color:black; color: white;'>" + 
						article.getHtmlContent() + 
						"</body>" +
						"</html>", 

						"text/html", "utf-8", null);
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		ImageView thumbnailImage = (ImageView) findViewById(R.id.thumbnailImage);
		if (thumbnailImage != null) {
			ForBossUtils.recycleBitmapOfImage(thumbnailImage, "article detail");
		}
		
		if (tracker != null) {
			tracker.stopSession();
		}
	}
}
