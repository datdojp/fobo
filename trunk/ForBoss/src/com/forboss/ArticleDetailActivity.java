package com.forboss;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.j256.ormlite.dao.Dao;

public class ArticleDetailActivity extends Activity {
	private Article article;
	private Dao<Article, String> articleDao;
	private ArticleDetailActivity instance;
	private static LinkedIn linkedIn = new LinkedIn();
	private ViewGroup relatedArticles;

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
		// if (article.getCategory().equals(ForBossUtils.getEventCategory())) {
		if (ForBossUtils.belongsToGroup(article.getCategory(),
				ForBossUtils.GROUP_EVENT)) {
			findViewById(R.id.imageClock).setVisibility(View.GONE);
			findViewById(R.id.time).setVisibility(View.GONE);
			findViewById(R.id.titleText).setVisibility(View.GONE);
			findViewById(R.id.thumbnailImage).setVisibility(View.GONE);
		}

		// load article body if HTML content is not loaded yet
		if (article.getHtmlContent() == null) {
			Log.d(this.getClass().getName(), "Get HTML content for article:"
					+ article.getId());
			ForBossUtils.get(URL.GET_ARTICLE_URL + "/" + article.getId(),
					new Handler() {
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
		time.setText(ForBossUtils.getLastUpdateInfo(article
				.getCreatedTimeInDate()));

		setViewText();
		setLikeText();

		if (article.getPictureLocation() != null) {
			ImageView thumbnailImage = (ImageView) findViewById(R.id.thumbnailImage);
			Bitmap bm = ForBossUtils.loadBitmapFromInternalStorage(
					article.getPictureLocation(), this);
			thumbnailImage.setImageBitmap(bm);
			thumbnailImage.setTag(bm);
		}

		// init the top menu
		View topMenuWrapper = findViewById(R.id.topMenuWrapper);
		ImageButton backButton = (ImageButton) topMenuWrapper
				.findViewById(R.id.backButton);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		TextView categoryText = (TextView) findViewById(R.id.categoryText);
		categoryText.setText(ForBossUtils.getConfig(article.getCategory()));

		ImageButton shareButton = (ImageButton) findViewById(R.id.shareButton);
		shareButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent sharingIntent = new Intent(
						android.content.Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
						"ForBoss - " + article.getTitle());
				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
						article.getBody() + "\n\nLink: " + article.getLink());
				startActivity(Intent.createChooser(sharingIntent,
						"Chia sẻ thông qua"));
			}
		});

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
					ForBossUtils.get(
							URL.LIKE_ARTICLE_URL + "/" + article.getId(), null);
				} else {
					article.setLike(false);
					article.setLikes(article.getLikes() - 1);

					((ImageButton) v).setImageResource(R.drawable.icon_heart_);

					try {
						articleDao.update(article);
					} catch (SQLException e) {
						e.printStackTrace();
					}
					setLikeText();
				}
			}
		});

		// Listen scroll event
//		View contentView = findViewById(R.id.contentView);
//		contentView.setOnTouchListener(new View.OnTouchListener() {
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				
//				//if (event.getAction() == MotionEvent.ACTION_MOVE) {
//					View relatedItems = findViewById(R.id.relatedArticles);
//					Log.d("Height", String.valueOf(v.getHeight()));
//					Log.d("Scroll Y", String.valueOf(v.getScrollY()));
//					if (v.getScrollY() >= v.getHeight() - 100) {
//						relatedItems.setVisibility(View.VISIBLE);
//					} else {
//						relatedItems.setVisibility(View.GONE);
//					}
//				//}
//				return false;
//			}
//		});

		// init bottom menu
		View bottomMenuWrapper = findViewById(R.id.bottomMenuWrapper);
		ImageButton favArticleListButton = (ImageButton) bottomMenuWrapper
				.findViewById(R.id.favArticleListButton);
		favArticleListButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((Activity) v.getContext()).finish();
				MainActivity.getInstance().navigateToFavorite();
			}
		});
		ImageButton facebookButton = (ImageButton) bottomMenuWrapper
				.findViewById(R.id.facebookButton);
		facebookButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle params = new Bundle();
				params.putString("name", article.getTitle());
				params.putString("caption", article.getBody());
				params.putString("description", article.getLink());
				params.putString("picture", article.getThumbnail());
				params.putString("link", article.getLink());

				Utility.mFacebook.dialog(instance, "feed", params,
						new BaseDialogListener() {

							@Override
							public void onComplete(Bundle values) {
								ForBossUtils.alert(instance,
										"Đăng Facebook thành công.");
							}

							@Override
							public void onFacebookError(FacebookError e) {
								super.onFacebookError(e);
								ForBossUtils
										.alert(instance,
												"Đăng Facebook thất bại. Hãy thử lại sau.");
							}

							@Override
							public void onError(DialogError e) {
								super.onError(e);
								ForBossUtils
										.alert(instance,
												"Đăng Facebook thất bại. Hãy thử lại sau.");
							}
						});
			}
		});
		ImageButton linkedinButton = (ImageButton) bottomMenuWrapper
				.findViewById(R.id.linkedinButton);
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
							linkedIn.liToken = linkedIn.oAuthService
									.getOAuthRequestToken(LinkedIn.OAUTH_CALLBACK_URL);
							Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(linkedIn.liToken.getAuthorizationUrl()));
							startActivity(i);
							ForBossUtils.dismissProgress(instance);
						} else {
							postToLinkedIn();
							ForBossUtils.dismissProgress(instance);
						}
						// Intent intent = new Intent(instance, LinkedIn.class);
						// startActivity(intent);
					}
				})).start();
			}
		});

		// init related articles
		relatedArticles = (ViewGroup) findViewById(R.id.relatedArticles);
		int max = ForBossUtils.getMaxRelatedArticles();
		int count = 0;
		for (Article other : MainActivity.cateDataMapping.get(article.getCategory())) {
			if (other.getId() != article.getId() && other.getPictureLocation() != null) {
				
				Bitmap bm = ForBossUtils.loadBitmapFromInternalStorage(other.getPictureLocation(), this);
				if (bm != null) {
					RelativeLayout itemLayout = (RelativeLayout) this.getLayoutInflater().inflate(R.layout.related_article_item, relatedArticles, false);
					ImageView item = (ImageView) itemLayout.findViewById(R.id.relatedItemImage);
					// ImageView item = (ImageView)
					// this.getLayoutInflater().inflate(R.layout.related_article_item,
					// relatedArticles, false);
					item.setImageBitmap(bm);
					item.setTag(bm);
					
					TextView title = (TextView) itemLayout.findViewById(R.id.title);
					title.setText(other.getTitle());

					Map<String, Object> tag = new HashMap<String, Object>();
					tag.put("article", other);
					tag.put("bm", bm);
					item.setTag(tag);
					item.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							finish();
							Article article = (Article) ((Map) v.getTag()).get("article");
							ForBossUtils.putBundleData("article", article);
							Intent intent = new Intent(instance, ArticleDetailActivity.class);
							startActivity(intent);
						}
					});

					relatedArticles.addView(itemLayout);

					count++;
					if (count >= max) {
						break;
					}
				}
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		String verifier = intent.getData().getQueryParameter("oauth_verifier");

		linkedIn.accessToken = linkedIn.oAuthService.getOAuthAccessToken(
				linkedIn.liToken, verifier);
		linkedIn.save(instance);
		Log.d(this.getClass().getName(),
				"Token=" + linkedIn.accessToken.getToken() + ", Token Secret="
						+ linkedIn.accessToken.getTokenSecret());
		linkedIn.client = linkedIn.factory
				.createLinkedInApiClient(linkedIn.accessToken);
		postToLinkedIn();
	}

	private void postToLinkedIn() {
		linkedIn.client.postNetworkUpdate(article.getTitle() + "\n"
				+ article.getBody() + "\nLink: " + article.getLink());
	}

	private void setViewText() {
		TextView nViewText = (TextView) findViewById(R.id.nViewText);
		nViewText.setText(Integer.toString(article.getViews())
				+ " người đã đọc bài viết");
	}

	private void setLikeText() {
		TextView nLikeText = (TextView) findViewById(R.id.nLikeText);
		if (article.isLike()) {
			if (article.getLikes() == 1) { // only you like the article
				nLikeText.setText("Bạn thích bài viết");
			} else if (article.getLikes() > 1) { // you and someone like the
													// article
				nLikeText.setText("Bạn và " + (article.getLikes() - 1)
						+ " người khác thích bài viết");
			}
		} else {
			nLikeText.setText(article.getLikes() + " người thích bài viết");
		}
	}

	private void setArticleContent() {
		WebView htmlContent = (WebView) findViewById(R.id.htmlContent);
		htmlContent.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		htmlContent.getSettings().setLoadWithOverviewMode(true);
//		htmlContent.getSettings().setUseWideViewPort(true);
		htmlContent.getSettings().setDefaultFontSize(14);
		htmlContent.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
		htmlContent.getSettings().setBuiltInZoomControls(true);
		htmlContent.getSettings().setSupportZoom(true);
		htmlContent
				.loadDataWithBaseURL(
						null,
						"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
								+ "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
								+ "<head>"
								+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"
								+ "</head>"
								+ "<body style='background-color:black; color: white;'>"
								+ article.getHtmlContent()
								+ "</body>"
								+ "</html>",

						"text/html", "utf-8", null);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ImageView thumbnailImage = (ImageView) findViewById(R.id.thumbnailImage);
		if (thumbnailImage != null) {
			ForBossUtils.recycleBitmapOfImage(thumbnailImage, "article detail");
		}
		if (relatedArticles != null) {
			for (int i = 0; i < relatedArticles.getChildCount(); i++) {
				View view = relatedArticles.getChildAt(i);
				if (view != null) {
					ImageView item = (ImageView) view.findViewById(R.id.relatedItemImage);
					if (item != null) {
						ForBossUtils.recycleBitmapOfImage(item, "item of related article");
					}
				}
			}
		}

		if (tracker != null) {
			tracker.stopSession();
		}
	}
}
