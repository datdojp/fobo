package com.forboss.sns.facebook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

public class BaseRequestListener implements RequestListener {

	@Override
	public void onComplete(String response, Object state) {
		
	}

	@Override
	public void onIOException(IOException e, Object state) {
		
	}

	@Override
	public void onFileNotFoundException(FileNotFoundException e, Object state) {
		
	}

	@Override
	public void onMalformedURLException(MalformedURLException e, Object state) {
		
	}

	@Override
	public void onFacebookError(FacebookError e, Object state) {
		
	}

}
