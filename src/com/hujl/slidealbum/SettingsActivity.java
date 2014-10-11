package com.hujl.slidealbum;

import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.readystatesoftware.systembartint.SystemBarTintManager.SystemBarConfig;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;

public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	addPreferencesFromResource(R.xml.pref_app_setting);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		initSystemBar();
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