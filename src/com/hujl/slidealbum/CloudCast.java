package com.hujl.slidealbum;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;

import android.app.Application;

public class CloudCast extends Application {

	public static DropboxAPI<AndroidAuthSession> mApi;
}
