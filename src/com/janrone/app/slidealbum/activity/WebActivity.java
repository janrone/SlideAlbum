package com.janrone.app.slidealbum.activity;

import com.janrone.app.slidealbum.R;
import com.janrone.app.slidealbum.util.Utils;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View.OnCreateContextMenuListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebActivity extends Activity {

	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);

		webView = (WebView) findViewById(R.id.web_view);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);// 适应屏幕，内容将自动缩放
		WebSettings webSettings = webView.getSettings();
		webSettings.setAllowFileAccess(true);
		webSettings.setBuiltInZoomControls(true);
		webView.loadUrl(getIntent().getStringExtra("url"));

		Utils.initSystemBar(this, webView);

		webView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				getAccessToken(url);
				return true;
			}

		});
	}
	
	private void getAccessToken(String url) {
		String starStr = "access_token";
		String endStr ="&session_secret";
		Log.d(Utils.TAG,url);
		Log.d(Utils.TAG,url.substring(url.indexOf(starStr)+starStr.length()+1,url.indexOf(endStr)));
		Utils.mAccessToken = url.substring(url.indexOf(starStr)+starStr.length()+1,url.indexOf(endStr));
		finish();
	}

}
