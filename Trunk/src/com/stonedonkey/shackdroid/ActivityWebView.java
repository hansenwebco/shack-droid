package com.stonedonkey.shackdroid;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class ActivityWebView extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		Bundle extras = this.getIntent().getExtras();
		String storyURL = extras.getString("URL");
		String storyTitle = extras.getString("Title");
				
		setContentView(R.layout.webview);
		this.setTitle(storyTitle);
		
		WebView wv = (WebView)findViewById(R.id.WebViewShackDroid);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.getSettings().setPluginsEnabled(true);
		wv.getSettings().setSavePassword(true);
		wv.getSettings().setSupportZoom(true);

		
		wv.loadUrl(storyURL);
		
		super.onCreate(savedInstanceState);
	}

	
	
}
