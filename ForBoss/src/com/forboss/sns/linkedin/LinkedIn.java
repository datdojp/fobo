package com.forboss.sns.linkedin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.LinkedInApiClientFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInAccessToken;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthService;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthServiceFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInRequestToken;
import com.google.code.linkedinapi.schema.Person;

//http://stackoverflow.com/questions/5804257/posting-linkedin-message-from-android-application/5804353#5804353
public class LinkedIn /*extends Activity*/ {
	public static final String CONSUMER_KEY = "ohb7y72eyttg";
	public static final String CONSUMER_SECRET = "bFe77OlhGUNmIVQx";
	public static final String APP_NAME = "ForBoss";
	public static final String OAUTH_CALLBACK_SCHEME = "x-oauthflow-linkedin";
	public static final String OAUTH_CALLBACK_HOST = "litestcalback";
	public static final String OAUTH_CALLBACK_URL = OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;

	public final LinkedInOAuthService oAuthService = LinkedInOAuthServiceFactory
			.getInstance().createLinkedInOAuthService(CONSUMER_KEY, CONSUMER_SECRET);
	public final LinkedInApiClientFactory factory = LinkedInApiClientFactory
			.newInstance(CONSUMER_KEY, CONSUMER_SECRET);

	public LinkedInRequestToken liToken;
	public LinkedInAccessToken accessToken;
	public LinkedInApiClient client;

	public static final String LINKEDIN_PREF_KEY = "linkedin";
	public static final String ACCESS_TOKEN = "accessToken";
	public static final String ACCESS_TOKEN_SECRET = "accessTokenSecret";
	public void save(Context context) {
		Editor editor = context.getSharedPreferences(LINKEDIN_PREF_KEY, Context.MODE_PRIVATE).edit();
		editor.putString(ACCESS_TOKEN, accessToken.getToken());
		editor.putString(ACCESS_TOKEN_SECRET, accessToken.getTokenSecret());
		editor.commit();
	}
	public void restore(Context context) {
		SharedPreferences savedSession = context.getSharedPreferences(LINKEDIN_PREF_KEY, Context.MODE_PRIVATE);
		String token = savedSession.getString(ACCESS_TOKEN, null);
		String tokenSecret = savedSession.getString(ACCESS_TOKEN_SECRET, null);
		if (token != null && tokenSecret != null) {
			LinkedInAccessToken accessToken = new LinkedInAccessToken(token, tokenSecret);
			client = factory.createLinkedInApiClient(accessToken);
		}
	}

	//	TextView tv = null;

	//	@Override
	//	public void onCreate(Bundle savedInstanceState) {
	//		super.onCreate(savedInstanceState);
	//		setContentView(R.layout.main);
	//		tv = (TextView) findViewById(R.id.tv);
	//		liToken = oAuthService.getOAuthRequestToken(OAUTH_CALLBACK_URL);
	//		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(liToken
	//				.getAuthorizationUrl()));
	//		startActivity(i);
	//	}

	//	@Override
	//	protected void onNewIntent(Intent intent) {
	//		String verifier = intent.getData().getQueryParameter("oauth_verifier");
	//
	//		LinkedInAccessToken accessToken = oAuthService.getOAuthAccessToken(
	//				liToken, verifier);
	//		client = factory.createLinkedInApiClient(accessToken);
	//		client.postNetworkUpdate("LinkedIn Android app test");
	//		Person p = client.getProfileForCurrentUser();
	//		tv.setText(p.getLastName() + ", " + p.getFirstName());
	//	}
}