package com.janrone.app.slidealbum.activity;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.janrone.app.slidealbum.R;
import com.janrone.app.slidealbum.R.id;
import com.janrone.app.slidealbum.R.layout;
import com.janrone.app.slidealbum.R.menu;
import com.janrone.app.slidealbum.R.string;
import com.janrone.app.slidealbum.util.Utils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity {

	//private Button mTvLinkDropBox;
	
	private boolean mLoggedIn;
	private DropboxAPI<AndroidAuthSession> mApi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		AndroidAuthSession session = buildSession(); 
		mApi = new DropboxAPI<AndroidAuthSession>(session);
		CloudCast.mApi = mApi;
		
		SharedPreferences prefs = getSharedPreferences(
				Utils.ACCOUNT_PREFS_NAME, 0);
		String castImageUrl = prefs.getString(Utils.CAST_IMAGE_URL, "");
		if (!castImageUrl.equals("")) {
			startCastSilde();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		AndroidAuthSession session = mApi.getSession();

		// The next part must be inserted in the onResume() method of the
		// activity from which session.startAuthentication() was called, so
		// that Dropbox authentication completes properly.
		if (session.authenticationSuccessful()) {
			try {
				// Mandatory call to complete the auth
				session.finishAuthentication();

				// Store it locally in our app for later use
				storeAuth(session);
				setLoggedIn(true);
			} catch (IllegalStateException e) {
				showToast("Couldn't authenticate with Dropbox:"
						+ e.getLocalizedMessage());
				Log.i(Utils.TAG, "Error authenticating", e);
			}
		}
	}
	
	private void startCastSilde(){
		Intent intent = new Intent(MainActivity.this, SlideShowActivity.class);
		startActivity(intent);
	}

	private void showToast(String msg) {
		Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		error.show();
	}

	private void logOut() {
		// Remove credentials from the session
		mApi.getSession().unlink();

		// Clear our stored keys
		clearKeys();
		// Change UI state to display logged out version
		setLoggedIn(false);
	}

	private AndroidAuthSession buildSession() {
		AppKeyPair appKeyPair = new AppKeyPair(Utils.APP_KEY, Utils.APP_SECRET);

		AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
		loadAuth(session);
		return session;
	}

	private void loadAuth(AndroidAuthSession session) {
		SharedPreferences prefs = getSharedPreferences(Utils.ACCOUNT_PREFS_NAME, 0);
		
		String key = prefs.getString(Utils.ACCESS_KEY_NAME, null);
		String secret = prefs.getString(Utils.ACCESS_SECRET_NAME, null);
		
		if (key == null || secret == null || key.length() == 0 || secret.length() == 0)
			return;

		if (key.equals("oauth2:")) {
			// If the key is set to "oauth2:", then we can assume the token is
			// for OAuth 2.
			session.setOAuth2AccessToken(secret);
		} else {
			// Still support using old OAuth 1 tokens.
			session.setAccessTokenPair(new AccessTokenPair(key, secret));
		}
		boolean isLogin = prefs.getBoolean(Utils.ACCOUNT_IS_LOGIN, false);
		setLoggedIn(isLogin);
	}

	private void isLogin(boolean isLogin) {
		SharedPreferences prefIsLogin = getSharedPreferences(
				Utils.ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefIsLogin.edit();
		edit.putBoolean(Utils.ACCOUNT_IS_LOGIN, isLogin);
		edit.commit();
	}

	private void storeAuth(AndroidAuthSession session) {
		// Store the OAuth 2 access token, if there is one.
		String oauth2AccessToken = session.getOAuth2AccessToken();
		if (oauth2AccessToken != null) {
			SharedPreferences prefs = getSharedPreferences(Utils.ACCOUNT_PREFS_NAME,
					0);
			Editor edit = prefs.edit();
			edit.putString(Utils.ACCESS_KEY_NAME, "oauth2:");
			edit.putString(Utils.ACCESS_SECRET_NAME, oauth2AccessToken);
			edit.commit();
			return;
		}
		// Store the OAuth 1 access token, if there is one. This is only
		// necessary if
		// you're still using OAuth 1.
		AccessTokenPair oauth1AccessToken = session.getAccessTokenPair();
		if (oauth1AccessToken != null) {
			SharedPreferences prefs = getSharedPreferences(Utils.ACCOUNT_PREFS_NAME,
					0);
			Editor edit = prefs.edit();
			edit.putString(Utils.ACCESS_KEY_NAME, oauth1AccessToken.key);
			edit.putString(Utils.ACCESS_SECRET_NAME, oauth1AccessToken.secret);
			edit.commit();
			return;
		}
	}

	private void clearKeys() {
		SharedPreferences prefs = getSharedPreferences(Utils.ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}

	private void setLoggedIn(boolean loggedIn) {
		mLoggedIn = loggedIn;
		isLogin(loggedIn);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onPrepareOptionsMenu(menu);
		menu.clear();
		getMenuInflater().inflate(R.menu.main, menu);
		if (mLoggedIn) {
			menu.getItem(0).setTitle(R.string.unlink_dropbox);
		} else {
			menu.getItem(0).setTitle(R.string.link_dropbox);
		}
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			if (mLoggedIn == true) {
				logOut();
			}else{
				mApi.getSession().startOAuth2Authentication(
						MainActivity.this);
			}
		}else{
			if (mLoggedIn == true) {
				Intent intent = new Intent(MainActivity.this, FolderActivity.class);
				startActivityForResult(intent, Utils.REQUEST_FOLDER_ACTIVITY);
			}else{
				mApi.getSession().startOAuth2Authentication(
						MainActivity.this);
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			startCastSilde();
		}
		
	}
	
	// Entry dirent = mApi.metadata(mPath, 10000, null, true, null);
	

}
