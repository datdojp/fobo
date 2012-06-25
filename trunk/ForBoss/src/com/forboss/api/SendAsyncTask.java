package com.forboss.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.forboss.utils.ForBossUtils;

public class SendAsyncTask extends AsyncTask<String, Void, String> {
	public static final String LOGIN_URL = ForBossUtils.getConfig("API_URL") + "/" + "api/mobile/sendemail";
	
	private Handler taskFinishedHandler;
	private Handler taskFailedHandler;
	private String url;

	protected String doInBackground(String... params) {
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
				boolean success = (Boolean)finalResult.get("success");
				if (success)
				{
					if (taskFinishedHandler != null)  {
						Message message = taskFinishedHandler.obtainMessage();
						taskFinishedHandler.sendMessage(message);
					}
				} else {
					if (taskFailedHandler != null)  {
						Message message = taskFailedHandler.obtainMessage();
						taskFailedHandler.sendMessage(message);
					}
				}
			} catch (JSONException e) {
				Log.e(this.getClass().getName(), e.toString());
			}
			finally {
				if (input != null) input.close();
			}

		} catch (ClientProtocolException e) {
			Log.e(this.getClass().getName(), e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.toString());
			e.printStackTrace();
		} 

		return null;
	}

	public Handler getTaskFinishedHandler() {
		return taskFinishedHandler;
	}

	public void setTaskFinishedHandler(Handler taskFinishedHandler) {
		this.taskFinishedHandler = taskFinishedHandler;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Handler getTaskFailedHandler() {
		return taskFailedHandler;
	}

	public void setTaskFailedHandler(Handler taskFailedHandler) {
		this.taskFailedHandler = taskFailedHandler;
	}
}