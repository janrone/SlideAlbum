package com.janrone.app.slidealbum.activity;

import com.janrone.app.slidealbum.R;
import com.janrone.app.slidealbum.R.string;
import com.janrone.app.slidealbum.R.xml;
import com.janrone.app.slidealbum.util.SystemBarTintManager;
import com.janrone.app.slidealbum.util.Utils;
import com.janrone.app.slidealbum.util.SystemBarTintManager.SystemBarConfig;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.View;

public class AboutActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.pref_app_about);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		initSystemBar();
		
		Preference preVersion = findPreference("app_details");
		try {
			preVersion.setTitle(getString(R.string.pref_version_title) + Utils.getVersionName(this));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		 
       findPreference("app_details").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference paramPreference) {
				Intent localIntent = new Intent("android.intent.action.SENDTO", Uri.fromParts("mailto", "janronehoo@gmail.com", null));
	            localIntent.putExtra("android.intent.extra.SUBJECT", "Contact Us");
	            AboutActivity.this.startActivity(Intent.createChooser(localIntent, "Send email..."));
				return true;
			}
		});
		
		findPreference("pref_about_android_slideshow_widget").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference paramPreference) {
				Intent localIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://github.com/marvinlabs/android-slideshow-widget"));
				AboutActivity.this.startActivity(localIntent);
				return true;
			}
		});
		
		findPreference("pref_about_system_bar_tint").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference paramPreference) {
				Intent localIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://github.com/jgilfelt/SystemBarTint"));
				AboutActivity.this.startActivity(localIntent);
				return true;
			}
		});
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

}
