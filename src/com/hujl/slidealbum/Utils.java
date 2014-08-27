package com.hujl.slidealbum;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

public class Utils {

	static final String TAG = "CloudCast";

	final static String APP_KEY = AppInfo.APP_KEY;
	final static String APP_SECRET = AppInfo.APP_SECRET;

	public final static String ROOT_PATH = "/";
	final static int FILE_LIMIT = 10000;

	final static int REQUEST_FOLDER_ACTIVITY = 101;

	final static String ACCOUNT_PREFS_NAME = "prefs";
	final static String ACCESS_KEY_NAME = "ACCESS_KEY";
	final static String ACCESS_SECRET_NAME = "ACCESS_SECRET";
	final static String ACCOUNT_IS_LOGIN = "IS_LOGIN";

	final static String SELECT_FOLDER_URL = "FOLDER_URL";

	final static String CAST_IMAGE_URL = "CAST_IMAGE_URL";

	final static String IMAGE_URL_FILE = "image";

	static boolean mLoggedIn;

	static AndroidAuthSession buildSession(Context context) {
		AppKeyPair appKeyPair = new AppKeyPair(Utils.APP_KEY, Utils.APP_SECRET);

		AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
		loadAuth(context, session);
		return session;
	}

	static void loadAuth(Context context, AndroidAuthSession session) {
		SharedPreferences prefs = context.getSharedPreferences(
				Utils.ACCOUNT_PREFS_NAME, 0);

		String key = prefs.getString(Utils.ACCESS_KEY_NAME, null);
		String secret = prefs.getString(Utils.ACCESS_SECRET_NAME, null);

		if (key == null || secret == null || key.length() == 0
				|| secret.length() == 0)
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
		setLoggedIn(context, isLogin);
	}

	static void isLogin(Context context, boolean isLogin) {
		SharedPreferences prefIsLogin = context.getSharedPreferences(
				Utils.ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefIsLogin.edit();
		edit.putBoolean(Utils.ACCOUNT_IS_LOGIN, isLogin);
		edit.commit();
	}

	static void storeAuth(Context context, AndroidAuthSession session) {
		// Store the OAuth 2 access token, if there is one.
		String oauth2AccessToken = session.getOAuth2AccessToken();
		if (oauth2AccessToken != null) {
			SharedPreferences prefs = context.getSharedPreferences(
					Utils.ACCOUNT_PREFS_NAME, 0);
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
			SharedPreferences prefs = context.getSharedPreferences(
					Utils.ACCOUNT_PREFS_NAME, 0);
			Editor edit = prefs.edit();
			edit.putString(Utils.ACCESS_KEY_NAME, oauth1AccessToken.key);
			edit.putString(Utils.ACCESS_SECRET_NAME, oauth1AccessToken.secret);
			edit.commit();
			return;
		}
	}

	static void clearKeys(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				Utils.ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}

	static void setLoggedIn(Context context, boolean loggedIn) {
		mLoggedIn = loggedIn;
		isLogin(context, loggedIn);
	}

	static boolean isExistsImageUrlFile(Context context) {
		File imageUrlFile = new File(context.getCacheDir() + IMAGE_URL_FILE);

		return imageUrlFile.exists();
	}

	static String readAssetsFileString(Context context) {

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

	static void writeToFile(Context context, String str) {

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

}
