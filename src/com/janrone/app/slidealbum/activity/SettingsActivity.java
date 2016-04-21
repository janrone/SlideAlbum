package com.janrone.app.slidealbum.activity;

import com.dropbox.client2.android.AndroidAuthSession;
import com.janrone.app.slidealbum.R;
import com.janrone.app.slidealbum.R.string;
import com.janrone.app.slidealbum.R.xml;
import com.janrone.app.slidealbum.util.AccountType;
import com.janrone.app.slidealbum.util.SystemBarTintManager;
import com.janrone.app.slidealbum.util.Utils;
import com.janrone.app.slidealbum.util.SystemBarTintManager.SystemBarConfig;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class SettingsActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.pref_app_setting);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		initSystemBar();

		findPreference("pre_account_type").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference paramPreference) {
				//if(!Utils.mLoggedIn){
					accountTypeOption();
				return true;
			}
		});

		findPreference("pre_cloud").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference paramPreference) {
				Preference preAccountType = findPreference("pre_account_type");
				if (preAccountType.getTitle().equals(getString(R.string.pref_account_baidu))) {

				}
				return true;
			}
		});
	}

	private void slideFolderOption() {
		new AlertDialog.Builder(this).setTitle("Options").setItems(new String[] { "Dropbox", "Local" }, new SlideFolderIitemClick()).setNegativeButton("Cancel", null).show();
	}

	private void accountTypeOption() {
		new AlertDialog.Builder(this).setTitle("Options").setItems(new String[] { "Dropbox", "BaiDu" }, new AccountIitemClick()).setNegativeButton("Cancel", null).show();
	}
	
	private void logoutDialog(){
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		Preference preAccountType = findPreference("pre_account_type");

		if (Utils.mAccountType.equals(AccountType.DROPBOX)) {
			preAccountType.setTitle(R.string.pref_account_dropbox);
			
			AndroidAuthSession session = CloudCast.mApi.getSession();

			// The next part must be inserted in the onResume() method of the
			// activity from which session.startAuthentication() was called, so
			// that Dropbox authentication completes properly.
			if (session.authenticationSuccessful()) {
				try {
					// Mandatory call to complete the auth
					session.finishAuthentication();

					// Store it locally in our app for later use
					Utils.storeAuth(SettingsActivity.this, session);
					Utils.setLoggedIn(SettingsActivity.this, true, Utils.mAccountType);

					if (Utils.mLoggedIn == true) {
						preAccountType.setSummary(R.string.unlink_dropbox);
					} else {
						preAccountType.setSummary(R.string.link_dropbox);
					}
				} catch (IllegalStateException e) {
					// showToast("Couldn't authenticate with Dropbox:"
					// + e.getLocalizedMessage());
					Log.i(Utils.TAG, "Error authenticating", e);
				}
			}

		} else if (Utils.mAccountType.equals(AccountType.BAIDU) && !TextUtils.isEmpty(Utils.mAccessToken)) {
			preAccountType.setTitle(R.string.pref_account_baidu);
			
			Utils.storeAuth(SettingsActivity.this, Utils.mAccessToken);
			Utils.setLoggedIn(SettingsActivity.this, true, Utils.mAccountType);

			if (Utils.mLoggedIn == true) {
				preAccountType.setSummary(R.string.unlink_baidu);
			} else {
				preAccountType.setSummary(R.string.link_baidu);
			}
		}

	}

	private void logOut() {
		// Remove credentials from the session
		CloudCast.mApi.getSession().unlink();

		// Clear our stored keys
		Utils.clearKeys(SettingsActivity.this);
		// Change UI state to display logged out version
		Utils.setLoggedIn(SettingsActivity.this, false, AccountType.EMPTY);
	}

	private void initSystemBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// setTranslucentStatus(true);
			SystemBarTintManager tintManager = new SystemBarTintManager(this);
			tintManager.setStatusBarTintEnabled(true);

			int actionBarColor = Color.parseColor("#DDDDDD");
			tintManager.setStatusBarTintColor(actionBarColor);
			// tintManager.setStatusBarTintResource(android.R.drawable.ic_notification_overlay);
			SystemBarConfig config = tintManager.getConfig();
			View view = findViewById(android.R.id.list);
			view.setPadding(0, config.getPixelInsetTop(true), 0, config.getPixelInsetBottom());
		}
	}

	class SlideFolderIitemClick implements OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			switch (which) {
			case 0:
				if (Utils.mLoggedIn == true) {
					Intent intent = new Intent(SettingsActivity.this, FolderActivity.class);
					startActivityForResult(intent, Utils.REQUEST_FOLDER_ACTIVITY);
				} else {
					CloudCast.mApi.getSession().startOAuth2Authentication(SettingsActivity.this);
				}
				break;
			case 1:

				break;
			}
		}
	}

	class AccountIitemClick implements OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			Preference preAccountType = findPreference("pre_account_type");
			switch (which) {
			case 0:
				Utils.mAccountType = AccountType.DROPBOX;
				preAccountType.setTitle(R.string.pref_account_dropbox);
				if (!Utils.mLoggedIn == true) {
					CloudCast.mApi.getSession().startOAuth2Authentication(SettingsActivity.this);
				}
				break;
			case 1:
				Utils.mAccountType = AccountType.BAIDU;
				preAccountType.setTitle(R.string.pref_account_baidu);
				if (!Utils.mLoggedIn == true) {
					Utils.loginBaidu(SettingsActivity.this);
				}
				break;
			}
		}
	}
}