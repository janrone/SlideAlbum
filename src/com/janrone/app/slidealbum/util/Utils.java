package com.janrone.app.slidealbum.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.janrone.app.slidealbum.activity.WebActivity;
import com.janrone.app.slidealbum.util.SystemBarTintManager.SystemBarConfig;

public class Utils {

	public static final String TAG = "CloudCast";

	public final static String APP_KEY = AppInfo.APP_KEY;
	public final static String APP_SECRET = AppInfo.APP_SECRET;

	public final static String ROOT_PATH = "/";
	public final static int FILE_LIMIT = 10000;

	public final static int REQUEST_FOLDER_ACTIVITY = 101;

	public final static String ACCOUNT_PREFS_NAME = "prefs";
	public final static String ACCESS_KEY_NAME = "ACCESS_KEY";
	public final static String ACCESS_SECRET_NAME = "ACCESS_SECRET";
	public final static String ACCOUNT_IS_LOGIN = "IS_LOGIN";
	public final static String ACCOUNT_TYPE = "ACCOUNT_TYPE";
	
	public final static String SELECT_FOLDER_URL = "FOLDER_URL";

	public final static String CAST_IMAGE_URL = "CAST_IMAGE_URL";

	public final static String IMAGE_URL_FILE = "image";

	public static boolean mLoggedIn;
	public static AccountType mAccountType = AccountType.EMPTY;
	public static String mAccessToken;
	
	public static String  mRootPath =  "/apps/wp2pcs";

	public static AndroidAuthSession buildSession(Context context) {
		AppKeyPair appKeyPair = new AppKeyPair(Utils.APP_KEY, Utils.APP_SECRET);

		AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
		loadAuth(context, session);
		return session;
	}

	public static void loadAuth(Context context, AndroidAuthSession session) {
		SharedPreferences prefs = context.getSharedPreferences(Utils.ACCOUNT_PREFS_NAME, 0);

		AccountType accountType = 
				Enum.valueOf(AccountType.class, prefs.getString(ACCOUNT_TYPE, ""));
		String key = prefs.getString(Utils.ACCESS_KEY_NAME, null);
		String secret = prefs.getString(Utils.ACCESS_SECRET_NAME, null);
		if(!TextUtils.isEmpty(accountType.toString())){
			if(accountType.equals(AccountType.DROPBOX)){
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
			}else if(accountType.equals(AccountType.BAIDU)){
				Utils.mAccessToken = key;
			}
		}
		
		boolean isLogin = prefs.getBoolean(Utils.ACCOUNT_IS_LOGIN, false);
		
		setLoggedIn(context, isLogin, accountType);
	}

	public static void isLogin(Context context, boolean isLogin, AccountType accountType) {
		SharedPreferences prefIsLogin = context.getSharedPreferences(Utils.ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefIsLogin.edit();
		edit.putBoolean(Utils.ACCOUNT_IS_LOGIN, isLogin);
		edit.putString(ACCOUNT_TYPE, accountType.toString());
		edit.commit();
	}

	public static void storeAuth(Context context, AndroidAuthSession session) {
		// Store the OAuth 2 access token, if there is one.
		String oauth2AccessToken = session.getOAuth2AccessToken();
		if (oauth2AccessToken != null) {
			SharedPreferences prefs = context.getSharedPreferences(Utils.ACCOUNT_PREFS_NAME, 0);
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
			SharedPreferences prefs = context.getSharedPreferences(Utils.ACCOUNT_PREFS_NAME, 0);
			Editor edit = prefs.edit();
			edit.putString(Utils.ACCESS_KEY_NAME, oauth1AccessToken.key);
			edit.putString(Utils.ACCESS_SECRET_NAME, oauth1AccessToken.secret);
			edit.commit();
			return;
		}
	}
	
	public static void storeAuth(Context context, String accessToken){
		if(!TextUtils.isEmpty(accessToken)){
			SharedPreferences prefs = context.getSharedPreferences(Utils.ACCOUNT_PREFS_NAME, 0);
			Editor edit = prefs.edit();
			edit.putString(Utils.ACCESS_KEY_NAME, accessToken);
			edit.commit();
		}
	}

	public static void clearKeys(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Utils.ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}

	public static void setLoggedIn(Context context, boolean loggedIn, AccountType accountType) {
		mLoggedIn = loggedIn;
		mAccountType = accountType;
		isLogin(context, loggedIn, accountType);
	}
	
	public static void setAccountType(){
		
	}

	public static boolean isExistsImageUrlFile(Context context) {
		File imageUrlFile = new File(context.getCacheDir() + IMAGE_URL_FILE);

		return imageUrlFile.exists();
	}

	public static String readAssetsFileString(Context context) {

		String str = null;
		File imageUrlFile = null;
		try {

			imageUrlFile = new File(context.getCacheDir() + IMAGE_URL_FILE);

			InputStream inputStream = new FileInputStream(imageUrlFile);

			byte[] buffer = new byte[inputStream.available()];

			inputStream.read(buffer);

			inputStream.close();

			str = new String(buffer);

		} catch (IOException e) {

			e.printStackTrace();

		}

		return str;

	}

	public static void writeToFile(Context context, String str) {

		File imageUrlFile = null;

		try {

			imageUrlFile = new File(context.getCacheDir() + IMAGE_URL_FILE);

			FileWriter fw = new FileWriter(imageUrlFile);

			BufferedWriter bw = new BufferedWriter(fw);// 使用缓冲数据流封装输出流

			bw.write(str);

			bw.newLine();

			bw.flush();

			bw.close();

		} catch (IOException e) {

			e.printStackTrace();

		}

	}

	public static String getVersionName(Context context) {
		String versionName = "";
		try {
			// ---get the package info---
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionName = pi.versionName;
			if (versionName == null || versionName.length() <= 0) {
				return "";
			}
		} catch (Exception e) {
			Log.e("VersionInfo", "Exception", e);
		}
		return versionName;
	}
	
	static String url = "http://openapi.baidu.com/oauth/2.0/authorize?response_type=token&client_id=CuOLkaVfoz1zGsqFKDgfvI0h&redirect_uri=oob&scope=netdisk";
	public static void loginBaidu(Context context){
		Intent intent = new Intent(context, WebActivity.class);
		intent.putExtra("url", url);
		context.startActivity(intent);
	}
	
	/**
	 * initSystemBar
	 * @param context Activity
	 * @param view
	 */
	public static void initSystemBar(Context context,View view) {
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
	        //setTranslucentStatus(true);
	        SystemBarTintManager tintManager = new SystemBarTintManager((Activity)context);
	        tintManager.setStatusBarTintEnabled(true);
	        
	        int actionBarColor = Color.parseColor("#DDDDDD");
	        tintManager.setStatusBarTintColor(actionBarColor);
	        //tintManager.setStatusBarTintResource(android.R.drawable.ic_notification_overlay);
	        SystemBarConfig config = tintManager.getConfig();
	        view.setPadding(0, config.getPixelInsetTop(true), 0, config.getPixelInsetBottom());
	    }
	}
	
}
