package com.forboss.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.forboss.ForBossApplication;

public class ForBossUtils {
	private static Map<String, Object> bundleData = new HashMap<String, Object>();
	public static Object getBundleData(String name) {
		return bundleData.get(name);
	}
	public static void putBundleData(String name, Object data) {
		bundleData.put(name, data);
	}

	/**
	 * This method downloads a file from given <code>url</code> and save it as <code>fileName</code> to internal storage
	 * @param url
	 * @param fileName
	 * @param contextWrapper the activity where this method is called
	 * @throws IOException
	 */
	public static void downloadAndSaveToInternalStorage(String url, String fileName, ContextWrapper contextWrapper) throws IOException {
		URL urlObj;
		HttpURLConnection conn = null;
		try {
			Log.d("Util", "Start downloading image "+url+" as file:"+fileName);
			//create connection
			urlObj = new URL(url);
			conn = (HttpURLConnection) urlObj.openConnection();
			conn.setDoInput(true);
			conn.setUseCaches(true);
			conn.setConnectTimeout(20000);
			conn.connect();
			InputStream is = conn.getInputStream();

			//save stream to local
			FileOutputStream fos = contextWrapper.openFileOutput(fileName, Context.MODE_PRIVATE);
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = is.read(bytes)) != -1) {
				fos.write(bytes, 0, read);
			}
			is.close();
			fos.flush();
			fos.close();
		} catch (IOException e) {
			throw e;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	public static Bitmap loadBitmapFromUrl(String url) {
		if (url == null || url == "") {
			return null;
		}
		URL urlObj;
		HttpURLConnection conn = null;
		Bitmap bm = null;
		try {
			//create connection
			urlObj = new URL(url);
			conn = (HttpURLConnection) urlObj.openConnection();
			conn.setDoInput(true);
			conn.setUseCaches(true);
			conn.setConnectTimeout(20000);
			conn.connect();
			InputStream is = conn.getInputStream();
			bm = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			Log.e("CelesteUtils", "Unable to load bitmap from url=#" + url + "#", e);
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

		return bm;
	}

	/**
	 * This method loads a bitmap from internal storage 
	 * @param fileName
	 * @param contextWrapper the activity where this method is called
	 * @return the bitmap
	 * @throws FileNotFoundException
	 */
	public static Bitmap loadBitmapFromInternalStorage(String fileName, ContextWrapper contextWrapper) throws FileNotFoundException {
		FileInputStream fis = contextWrapper.openFileInput(fileName);
		Bitmap bm = BitmapFactory.decodeStream(fis);
		return bm;
	}

	public static Bitmap makeSquare(Bitmap rectangle) {
		if (rectangle == null) {
			return null;
		}
		Bitmap square = rectangle;
		if (rectangle.getHeight() > rectangle.getWidth()) {
			square = Bitmap.createBitmap(rectangle, 0, (rectangle.getHeight() - rectangle.getWidth()) / 2, rectangle.getWidth(), rectangle.getWidth());
		} else if (rectangle.getWidth() > rectangle.getHeight()){
			square = Bitmap.createBitmap(rectangle, (rectangle.getWidth() - rectangle.getHeight()) / 2, 0, rectangle.getHeight(), rectangle.getHeight());
		}
		return square;
	}

	//the code come from: 
	//	http://ruibm.com/?p=184
	public static Bitmap makeRounded(Bitmap bitmap, int pixels) {
		if (bitmap == null) {
			return bitmap;
		}

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	public static void setImageByStatus(ImageView img, boolean status, int onStatusResId, int offStatusResId) {
		if (img == null) {
			return;
		}
		if (status) {
			img.setImageResource(onStatusResId);
		} else {
			img.setImageResource(offStatusResId);
		}
	}

	private static final String EMAIL_EXPRESSION="^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
	public static boolean isValidEmailAddress(String emailAddress){  
		CharSequence inputStr = emailAddress;  
		Pattern pattern = Pattern.compile(EMAIL_EXPRESSION,Pattern.CASE_INSENSITIVE);  
		Matcher matcher = pattern.matcher(inputStr);  
		return matcher.matches();  
	}

	/**
	 * Show an alert dialog with message and "Close" button
	 * @param context
	 * @param message
	 */
	public static void alert(Context context, String message) {
		AlertDialog.Builder	builder = new AlertDialog.Builder(context);
		builder.setMessage(message)
		.setPositiveButton("Close", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private static ProgressDialog progressDialog = null;
	public static void alertProgress(Context context, String message) {
		dismissProgress(context);

		progressDialog = new ProgressDialog(context) {
			@Override
			public void onBackPressed() {};
		};
		progressDialog.setMessage(message);
		progressDialog.show();
	}

	public static void dismissProgress(Context context) {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	private static final SimpleDateFormat YYYYMMDD = new SimpleDateFormat("yyyyMMdd");
	public static final boolean sameDay(Date aDate, Date otherDate) {
		if (aDate == null && otherDate == null) {
			return true;
		}
		if (aDate == null || otherDate == null) {
			return false;
		}

		return YYYYMMDD.format(aDate).equals( YYYYMMDD.format(otherDate) );
	}

	public static final void addView(ViewGroup parent, View child) {
		if (parent == null || child == null) {
			return;
		}

		if (child.getParent() == null) {
			parent.addView(child);
		}
	}

	public static final void removeView(ViewGroup parent, View child) {
		if (parent == null || child == null) {
			return;
		}

		if (child.getParent() == parent) {
			parent.removeView(child);
		}
	}

	public static int convertDpToPixel(int dp, Context context) {
		return Math.round( dp * (context.getResources().getDisplayMetrics().densityDpi / 160f) );
	}


	public static boolean isNetworkAvailable() {
		//TODO need to improve the network detection
		ConnectivityManager connectivityManager 
		= (ConnectivityManager)ForBossApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}

	private static ResourceBundle config = ResourceBundle.getBundle("configuration");

	public static String getConfig(String key) {
		return config.getString(key);
	}

	public static boolean getProductionalEnvironment() {
		return true;
	}

	public static String getAPIBaseUrl() {
		if (!getProductionalEnvironment()) return config.getString("API_STAGING_URL");
		else return config.getString("API_PRODUCTIONAL_URL");
	}

	private static final SimpleDateFormat sessionDateFormat = new SimpleDateFormat("MMM d$$$, yyyy");
	public static String formatSessionDate(int nthDay, Date sessionDate) {
		if (sessionDate == null) {
			return "";
		}
		String result = sessionDateFormat.format(sessionDate);
		result = StringUtils.replace(result, "$$$", getDayOfMonthSuffix(sessionDate));

		return "Day " + nthDay + " - " + result;
	}
	private static String getDayOfMonthSuffix(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int n = calendar.get(Calendar.DAY_OF_MONTH);
		if (n < 1 || n > 31) {
			throw new IllegalArgumentException("illegal day of month: " + n);
		}
		if (n >= 11 && n <= 13) {
			return "th";
		}
		switch (n % 10) {
		case 1:  return "st";
		case 2:  return "nd";
		case 3:  return "rd";
		default: return "th";
		}
	}

	public static void initNavHeader(Activity activity) {
		//		TODO
		//		//add listener for controls
		//		ImageButton btnBack = (ImageButton) activity.findViewById(R.id.navBar_BackBtn);
		//		btnBack.setOnClickListener(new View.OnClickListener() {
		//			@Override
		//			public void onClick(View v) {
		//				((Activity)v.getContext()).finish();
		//			}
		//		});
		//
		//		//bring nav header to front
		//		activity.findViewById(R.id.navHeaderContainer).bringToFront();
	}

	public static boolean isFileExisting(String filename, Context context) {
		File file = context.getFileStreamPath(filename);
		return file.exists();
	}

	public static void copyAssetFile(String src, String dst) throws IOException {
		InputStream in = null;
		OutputStream out = null;
		AssetManager assets = ForBossApplication.getAppContext().getAssets();
		try {
			in = assets.open(src);
			out = new FileOutputStream(dst);
			copyFile(in, out);
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
		} catch(Exception e) {
			Log.e("CopyFile", "Copy file failed because of ", e);
		}       

	}

	private static void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
	}

	public static String[] getAssetListFromConfig(String configKey) {
		String configString = getConfig(configKey);
		String[] splitted = StringUtils.split(configString, ",");
		return splitted;
	}

	private static List<String> articleCategoryList;
	public static List<String> getArticleCategoryList() {
		if (articleCategoryList == null) {
			articleCategoryList = new ArrayList<String>();
			String categoryConfig = getConfig("categories");
			String[] splitted = categoryConfig.trim().split(",");
			for(int i = 0; i < splitted.length; i++) {
				articleCategoryList.add(splitted[i].trim());
			}
		}

		return articleCategoryList;
	}
	private static List<String> categoryList;
	public static List<String> getCategoryList() {
		if (categoryList == null) {
			categoryList = new ArrayList<String>();
			categoryList.addAll(getArticleCategoryList());
			categoryList.add(getEventCategory());
		}
		return categoryList;
	}
	private static String eventCategory;
	public static String getEventCategory() {
		if (eventCategory == null) {
			eventCategory = getConfig("event");
		}
		return eventCategory;
	}

	public static void get(String url, Handler taskFinishedHandler) {
		GetUrlRunnable getUrlRunnable = new GetUrlRunnable();
		getUrlRunnable.setUrl(url);
		getUrlRunnable.setTaskFinishedHandler(taskFinishedHandler);
		(new Thread(getUrlRunnable)).start();
	}

	private static class GetUrlRunnable implements Runnable {
		private String url;
		private Handler taskFinishedHandler;
		
		
		public String getUrl() {
			return url;
		}


		public void setUrl(String url) {
			this.url = url;
		}


		public Handler getTaskFinishedHandler() {
			return taskFinishedHandler;
		}


		public void setTaskFinishedHandler(Handler taskFinishedHandler) {
			this.taskFinishedHandler = taskFinishedHandler;
		}


		@Override
		public void run() {
			// reference: http://www.androidsnippets.com/executing-a-http-post-request-with-httpclient
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(url);

			try {
				HttpResponse response = httpclient.execute(httpget);
				InputStream input = response.getEntity().getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
				String json = reader.readLine();
				JSONTokener tokener = new JSONTokener(json);
				try {
					JSONObject finalResult = new JSONObject(tokener);
					// boolean success = (Boolean)finalResult.get("success");

					if (taskFinishedHandler != null)  {
						Message message = taskFinishedHandler.obtainMessage();
						message.obj = finalResult;
						taskFinishedHandler.sendMessage(message);
					}

				} catch (JSONException e) {
					Log.e("ForBossUtils", e.toString());
				}
				finally {
					if (input != null) input.close();
				}

			} catch (ClientProtocolException e) {
				Log.e("ForBossUtils", e.toString());
				e.printStackTrace();
			} catch (IOException e) {
				Log.e("ForBossUtils", e.toString());
				e.printStackTrace();
			}		
		}
	}
	
	public static String getLastUpdateInfo(Date lastUpdate) {
		if (lastUpdate == null) {
			return "Chưa bao giờ";
		}
		
		Date now = new Date();
		long timeDiff = now.getTime() - lastUpdate.getTime();
		long deltaSecond = timeDiff / 1000;
		if (deltaSecond < 5) {
			return "Mới tức thi";
		}
		if (deltaSecond < 60) {
			return deltaSecond + " giây trước";
		}
		if (deltaSecond < 120) {
			return "Một phút trước";
		}
		long deltaMinute = timeDiff / (60 * 1000);
		if (deltaMinute < 60) {
			return deltaMinute + " phút trước";
		}
		if (deltaMinute < 120) {
			return "Một giờ trước";
		}
		long deltaHour = timeDiff / (60 * 60 * 1000);
		if (deltaHour < 24) {
			return deltaHour + " giờ trước";
		}
		long deltaDay = timeDiff / (24 * 60 * 60 * 1000);
		if (deltaDay < 2) {
			return "Hôm qua";
		}
		if (deltaDay < 7) {
			return deltaDay + " ngày trước";
		}
		long deltaWeek = timeDiff / (7 * 24 * 60 * 60 * 1000);
		if (deltaWeek < 2) {
			return "Tuần trước";
		}
		if (deltaDay < 31) {
			return deltaWeek + " tuần trước";
		}
		if (deltaDay < 61) {
			return "Tháng trước";
		}
		long deltaMonth = timeDiff / (30 * 24 * 60 * 60 * 1000);
		if (deltaDay < 365.25) {
			return deltaMonth + " tháng trước";
		}
		if (deltaDay < 731) {
			return "Năm ngoái";
		}
		long deltaYear = timeDiff / Math.round(365.25 * 24 * 60 * 60 * 1000);
		return deltaYear + " năm trước";
	}
}
