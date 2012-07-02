package com.forboss.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.widget.Toast;

import com.forboss.ForBossViewPagerFragmentActivity;
import com.forboss.api.exception.ServiceException;
import com.forboss.data.model.Article;
import com.forboss.data.utils.DatabaseHelper;
import com.forboss.utils.ForBossUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.j256.ormlite.dao.Dao;

public class ArticleService extends IntentService {

	public ArticleService() {
		super("ArticleService");
	}

	private static final String ARTICLE_LIST_URL = ForBossUtils.getConfig("API_URL") + "/" + "api/mobile/posts";
	private HttpClient mHttpClient;
	private String category;

	@Override
	public void onCreate() {
		super.onCreate();
		mHttpClient = getHttpClient(this);
	}

	public void doSync() throws ServiceException {
		String url = ARTICLE_LIST_URL + "/" + category;
		long updateTime = ForBossUtils.getUpdateTimeOfCate(category, this);
		if ( updateTime != 0) {
			url = url + "/" + updateTime;
		}
		Log.d(this.getClass().getName(), "Sync with URL: " + url);
		final HttpUriRequest request = new HttpGet(url);
		execute(request, new ExecutorFunction() {
			@Override
			public void executeJob(InputStream input) throws SQLException,
			ParseException {
				parseAndInsertContent(input);
			}
		});
	}

	public void parseAndInsertContent(InputStream jsonStream) throws SQLException {
		Dao<Article, String> articleDao = DatabaseHelper.getHelper(this).getArticleDao();

		// parse json string into entity objects
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gsonParser = gsonBuilder.create();
		Type collectionType = new TypeToken<Collection<Article>>(){}.getType();
		Collection<Article> articles = gsonParser.fromJson(new JsonReader(new InputStreamReader(jsonStream)), collectionType);

		// check if the news item exists in the database --> insert into database if not
		for (Article anArticle : articles) {
			Article articleFromDb = articleDao.queryForId(anArticle.getId());
			if(articleFromDb == null) {
				Log.d(this.getClass().getName(), "Inserting article with title " + anArticle.getTitle());
				articleDao.create(anArticle);
			} else {
				Article.copyContent(anArticle, articleFromDb);
				articleDao.update(articleFromDb);
			}
		}
	}

	public void execute(HttpUriRequest request, ExecutorFunction func) throws ServiceException {
		try {
			final HttpResponse resp = mHttpClient.execute(request);
			final int status = resp.getStatusLine().getStatusCode();
			if (status != HttpStatus.SC_OK) {
				throw new ServiceException("Unexpected server response " + resp.getStatusLine()
						+ " for " + request.getRequestLine());
			}

			final InputStream input = resp.getEntity().getContent();
			try {
				func.executeJob(input);
			} catch (SQLException e) {
				throw new ServiceException("Unexpected error while persisting content for "+ request.getRequestLine() +"\ncaused by " +e );
			} catch (ParseException e) {
				throw new ServiceException("Unexpected error while persisting content for "+ request.getRequestLine() +"\ncaused by " +e );
			} finally {
				if (input != null) input.close();
			}
		} catch (ServiceException e) {
			throw e;
		} catch (IOException e) {
			throw new ServiceException("Problem reading remote response for "
					+ request.getRequestLine(), e);
		}
	}

	interface ExecutorFunction {
		void executeJob(InputStream input) throws SQLException, ParseException;
	}

	/**
	 * Generate and return a {@link HttpClient} configured for general use,
	 * including setting an application-specific user-agent string.
	 */
	public static HttpClient getHttpClient(Context context) {
		final HttpParams params = new BasicHttpParams();

		// Use generous timeouts for slow mobile networks
		HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
		HttpConnectionParams.setSoTimeout(params, 20 * 1000);

		HttpConnectionParams.setSocketBufferSize(params, 8192);
		HttpProtocolParams.setUserAgent(params, buildUserAgent(context));

		final DefaultHttpClient client = new DefaultHttpClient(params);

		return client;
	}

	/**
	 * Build and return a user-agent string that can identify this application
	 * to remote servers. Contains the package name and version code.
	 */
	private static String buildUserAgent(Context context) {
		try {
			final PackageManager manager = context.getPackageManager();
			final PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);

			// Some APIs require "(gzip)" in the user-agent string.
			return info.packageName + "/" + info.versionName
					+ " (" + info.versionCode + ") (gzip)";
		} catch (NameNotFoundException e) {
			return null;
		}
	}

	private static boolean isSyncing = false;

	@Override
	protected void onHandleIntent(Intent intent) {
		// prevent 2 sync run at the same time
		if (isSyncing == true) {
			return;
		}

		if (!ForBossUtils.isNetworkAvailable()) {
			Toast.makeText(ForBossViewPagerFragmentActivity.getInstance(), "Không có kết nối mạng. Không thể lấy dữ liệu từ server", Toast.LENGTH_SHORT).show();
//			ForBossUtils.alert(ForBossViewPagerFragmentActivity.getInstance(), "Không có kết nối mạng. Không thể lấy dữ liệu từ server");
		} else {
			isSyncing = true;
			try {
				List<String> categoryList = ForBossUtils.getCategoryList();
				for(String aCate : categoryList) {
					category = aCate;
					doSync();
				}
			} catch (Exception e) {
//				ForBossUtils.alert(this, "Xảy ra lỗi trong quá trình lấy dữ liệu từ server");
				Log.e(this.getClass().getName(), "Problem with sync");
				e.printStackTrace();
			}
			isSyncing = false;
		}

		ForBossViewPagerFragmentActivity.getInstance().refreshArticleList();
	}
}
