package com.hujl.slidealbum;

import com.dropbox.client2.android.AndroidAuthSession;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.readystatesoftware.systembartint.SystemBarTintManager.SystemBarConfig;

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

		findPreference("pre_dropbox").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference paramPreference) {
				// Intent localIntent = new Intent("android.intent.action.VIEW",
				// Uri.parse("https://github.com/marvinlabs/android-slideshow-widget"));
				// AboutActivity.this.startActivity(localIntent);
				if (Utils.mLoggedIn == true) {
					logOut();
					Preference preDropbox  = findPreference("pre_dropbox");
					
					if (Utils.mLoggedIn == true) {
						preDropbox.setSummary(R.string.unlink_dropbox);
					}else {
						preDropbox.setSummary(R.string.link_dropbox);;
					}
					
				} else {
					CloudCast.mApi.getSession().startOAuth2Authentication(SettingsActivity.this);
				}
				return true;
			}
		});
		
		findPreference("pre_cloud").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference paramPreference) {
				createOption();
				return true;
			}
		});
	}
	
	private void createOption(){
		new AlertDialog.Builder(this).setTitle("Options").setItems(new String[] { "Dropbox", "Local" }, new IitemClick()).setNegativeButton("Cancel", null).show();
	}
	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
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
				Utils.setLoggedIn(SettingsActivity.this, true);
			} catch (IllegalStateException e) {
				// showToast("Couldn't authenticate with Dropbox:"
				// + e.getLocalizedMessage());
				Log.i(Utils.TAG, "Error authenticating", e);
			}
		}
		
		Preference preDropbox  = findPreference("pre_dropbox");
		if (Utils.mLoggedIn == true) {
			preDropbox.setSummary(R.string.unlink_dropbox);
		}else {
			preDropbox.setSummary(R.string.link_dropbox);;
		}
	}

	private void logOut() {
		// Remove credentials from the session
		CloudCast.mApi.getSession().unlink();

		// Clear our stored keys
		Utils.clearKeys(SettingsActivity.this);
		// Change UI state to display logged out version
		Utils.setLoggedIn(SettingsActivity.this, false);
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
	
	class IitemClick implements OnClickListener{

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			switch (which) {
			case 0:
				if (Utils.mLoggedIn == true) {
					Intent intent = new Intent(SettingsActivity.this, FolderActivity.class);
					startActivityForResult(intent, Utils.REQUEST_FOLDER_ACTIVITY);
				}else{
					CloudCast.mApi.getSession().startOAuth2Authentication(
							SettingsActivity.this);
				}
				break;
			case 1:

				break;
			}
		}
		
	}

}