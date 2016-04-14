package com.janrone.app.slidealbum.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.google.gson.Gson;
import com.janrone.app.slidealbum.R;
import com.janrone.app.slidealbum.slideshow.SlideShowAdapter;
import com.janrone.app.slidealbum.slideshow.SlideShowView;
import com.janrone.app.slidealbum.slideshow.adapter.GenericBitmapAdapter;
import com.janrone.app.slidealbum.slideshow.adapter.RemoteBitmapAdapter;
import com.janrone.app.slidealbum.slideshow.picasso.GenericPicassoBitmapAdapter;
import com.janrone.app.slidealbum.util.SystemBarTintManager;
import com.janrone.app.slidealbum.util.Utils;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class SlideShowActivity extends Activity {

	private DropboxAPI<AndroidAuthSession> mApi;

	private SlideShowView slideShowView;
	private SlideShowAdapter adapter;

	private SystemBarTintManager mTintManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mTintManager = new SystemBarTintManager(this);
		mTintManager.setStatusBarTintEnabled(true);

		// Set screen always on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Activity layout
		setContentView(R.layout.activity_slideshow);

		slideShowView = (SlideShowView) findViewById(R.id.slideshow);
		slideShowView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				initSystemBar();
			}
		});

		AndroidAuthSession session = Utils.buildSession(SlideShowActivity.this);
		mApi = new DropboxAPI<AndroidAuthSession>(session);
		CloudCast.mApi = mApi;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onResume() {
		super.onResume();
		
		initSystemBar();

		AndroidAuthSession session = mApi.getSession();

		// The next part must be inserted in the onResume() method of the
		// activity from which session.startAuthentication() was called, so
		// that Dropbox authentication completes properly.
		if (session.authenticationSuccessful()) {
			try {
				// Mandatory call to complete the auth
				session.finishAuthentication();

				// Store it locally in our app for later use
				Utils.storeAuth(SlideShowActivity.this, session);
				Utils.setLoggedIn(SlideShowActivity.this, true);
			} catch (IllegalStateException e) {
				// showToast("Couldn't authenticate with Dropbox:"
				// + e.getLocalizedMessage());
				Log.i(Utils.TAG, "Error authenticating", e);
			}
		}

		// String castImageUrl = prefs.getString(Utils.CAST_IMAGE_URL, "");
		// if (castImageUrl.equals("")) {
		// new GetCastImage().execute(folderPath, null, null);
		// }

		// ArrayList<String> list = getCastImageUrlsFromPrefs();
		// if(list != null) startSlideShow(list);

		if (!Utils.isExistsImageUrlFile(getContext())
				|| getImageUrlListFromFile() == null
				|| getImageUrlListFromFile().size() == 0) {
			new GetCastImage().execute(getImageFolder(), null, null);
		} else {
			startSlideShow(getImageUrlListFromFile());
		}
	}
	
	private void initSystemBar() {
		View view = getWindow().getDecorView();

		int status = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

		boolean isFull = view.getSystemUiVisibility() == status;
		if (isFull) {
			showSystemUI(view);
			int actionBarColor = Color.parseColor("#DDDDDD");
			mTintManager.setStatusBarTintColor(actionBarColor);
		} else {
			hideSystemUI(view);
			int actionBarColor = Color.parseColor("#00000000");
			mTintManager.setStatusBarTintColor(actionBarColor);
		}
	}

	private void setActionBar() {
		ActionBar actionBar = getActionBar();
		if (actionBar.isShowing()) {
			actionBar.hide();
			int actionBarColor = Color.parseColor("#00000000");
			mTintManager.setStatusBarTintColor(actionBarColor);
		} else {
			actionBar.show();
			int actionBarColor = Color.parseColor("#DDDDDD");
			mTintManager.setStatusBarTintColor(actionBarColor);
		}
	}

	private void setTranslucentStatus(boolean on) {
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		if (on) {
			winParams.flags |= bits;
		} else {
			winParams.flags &= ~bits;
		}
		win.setAttributes(winParams);
	}

	public static int getStatusBarHeight(Context context) {
		int result = 0;
		int resourceId = context.getResources().getIdentifier(
				"status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<String> getImageUrlListFromFile() {
		try {
			Gson gson = new Gson();
			return gson.fromJson(Utils.readAssetsFileString(getContext()),
					java.util.ArrayList.class);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.slide_show, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onPrepareOptionsMenu(menu);
		menu.clear();
		getMenuInflater().inflate(R.menu.slide_show, menu);
		//if (Utils.mLoggedIn) {
		//	menu.getItem(0).setTitle(R.string.unlink_dropbox);
		//} else {
		//	menu.getItem(0).setTitle(R.string.link_dropbox);
		//}
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long 
		// as you specify a parent activity in AndroidManifest.xml.

		int id = item.getItemId();
		if (id == R.id.action_dropbox) {
			if (Utils.mLoggedIn == true) {
				logOut();
			} else {
				mApi.getSession().startOAuth2Authentication(
						SlideShowActivity.this);
			}
		} else if (id == R.id.select_folder) {
			if (Utils.mLoggedIn == true) {
				Intent intent = new Intent(SlideShowActivity.this,
						FolderActivity.class);
				startActivityForResult(intent, Utils.REQUEST_FOLDER_ACTIVITY);
			} else {
				mApi.getSession().startOAuth2Authentication(
						SlideShowActivity.this);
			}
		} else if (id == R.id.action_about) {
			Intent intent = new Intent(SlideShowActivity.this,
					AboutActivity.class);
			startActivity(intent);
		} else if (id == R.id.action_settings) {
			Intent intent = new Intent(SlideShowActivity.this,
					SettingsActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			new GetCastImage().execute(getImageFolder(), null, null);
		}

	}

	public String getImageFolder() {
		SharedPreferences prefs = getSharedPreferences(
				Utils.ACCOUNT_PREFS_NAME, 0);
		return prefs.getString(Utils.SELECT_FOLDER_URL, "");
	}

	private void logOut() {
		// Remove credentials from the session
		mApi.getSession().unlink();

		// Clear our stored keys
		Utils.clearKeys(SlideShowActivity.this);
		// Change UI state to display logged out version
		Utils.setLoggedIn(SlideShowActivity.this, false);
	}

	private ArrayList<String> getCastImageUrlsFromPrefs() {

		// 从ObjectInputStream中读取Entry对象
		try {
			SharedPreferences prefs = getSharedPreferences(
					Utils.ACCOUNT_PREFS_NAME, 0);
			String castImageUrl = prefs.getString(Utils.CAST_IMAGE_URL, "");

			byte[] base64Bytes = Base64.decode(castImageUrl.getBytes(),
					Base64.DEFAULT);
			ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (ArrayList<String>) ois.readObject();
		} catch (Exception e) {
			// TODO: handle exception
			Log.d(Utils.TAG, "getCastImageUrlsFromPrefs====>" + e.toString());
		}
		return null;
	}

	private SlideShowAdapter createRemoteAdapter(ArrayList<String> thumbs) {
		String[] slideUrls = new String[] {
				"http://lorempixel.com/1280/720/sports",
				"http://lorempixel.com/1280/720/nature",
				"http://lorempixel.com/1280/720/people",
				"http://lorempixel.com/1280/720/city", };
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = 2;
		adapter = new RemoteBitmapAdapter(this, thumbs, opts, CloudCast.mApi);
		return adapter; 
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void onStop() {
		Log.d(Utils.TAG, " slide stop ...");
		if (adapter instanceof GenericBitmapAdapter) {
			((GenericBitmapAdapter) adapter).shutdown();
		} else if (adapter instanceof GenericPicassoBitmapAdapter) {
			((GenericPicassoBitmapAdapter) adapter).shutdown();
		}
		super.onStop();
	}

	private void startSlideShow(ArrayList<String> thumbs) {
		// Create an adapter
		// slideShowView.setAdapter(createResourceAdapter());
		slideShowView.setAdapter(createRemoteAdapter(thumbs));
		// slideShowView.setAdapter(createPicassoAdapter(thumbs));

		// Optional customisation follows
		// slideShowView.setTransitionFactory(new RandomTransitionFactory());
		// slideShowView.setPlaylist(new RandomPlayList());

		// Some listeners if needed
		slideShowView.setOnSlideShowEventListener(slideShowListener);
		// slideShowView.setOnSlideClickListener(slideClickListener);
		// Then attach the adapter
		slideShowView.play();
	}

	private SlideShowView.OnSlideClickListener slideClickListener = new SlideShowView.OnSlideClickListener() {
		@Override
		public void onItemClick(SlideShowView parent, int position) {
			toggleHideyBar();
		}
	};

	private SlideShowView.OnSlideShowEventListener slideShowListener = new SlideShowView.OnSlideShowEventListener() {
		@Override
		public void beforeSlideShown(SlideShowView parent, int position) {
			Log.d("SlideShowDemo",
					"OnSlideShowEventListener.beforeSlideShown: " + position);
		}

		@Override
		public void onSlideShown(SlideShowView parent, int position) {
			Log.d("SlideShowDemo", "OnSlideShowEventListener.onSlideShown: "
					+ position);
		}

		@Override
		public void beforeSlideHidden(SlideShowView parent, int position) {
			Log.d("SlideShowDemo",
					"OnSlideShowEventListener.beforeSlideHidden: " + position);
		}

		@Override
		public void onSlideHidden(SlideShowView parent, int position) {
			Log.d("SlideShowDemo", "OnSlideShowEventListener.onSlideHidden: "
					+ position);
		}
	};

	class GetCastImage extends AsyncTask<String, Void, Void> {

		private static final int ArrayList = 0;
		private static final int String = 0;
		private List<Entry> listEntry = null;
		ArrayList<String> thumbs = null;

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {

				String[] folders = params[0].split(";");

				for (int i = 0; i < folders.length; i++) {
					listEntry = new ArrayList<Entry>();
					Entry entry = CloudCast.mApi.metadata(folders[i],
							Utils.FILE_LIMIT, null, true, null);
					if (entry.isDir && entry.contents != null) {
						listEntry.add(entry);
					}
				}

				thumbs = new ArrayList<String>();
				if (listEntry != null && listEntry.size() != 0) {
					for (Entry le : listEntry) {
						for (Entry e : le.contents) {
							if (e.thumbExists) {
								// Add it to the list of thumbs we can choose
								// from
								thumbs.add(e.path);
								System.err.println("e.path" + e.path);
							}
						}
					}
				}

			} catch (DropboxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO: handle exception
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			if (thumbs != null) {
				try {
					Gson gson = new Gson();
					Utils.writeToFile(SlideShowActivity.this,
							gson.toJson(thumbs, java.util.ArrayList.class));
					startSlideShow(getImageUrlListFromFile());
					// getCacheDir();
					// SharedPreferences prefs = getSharedPreferences(
					// Utils.ACCOUNT_PREFS_NAME, 0);
					// ByteArrayOutputStream baos = new ByteArrayOutputStream();
					// ObjectOutputStream oos = new ObjectOutputStream(baos);
					// // 将Product对象放到OutputStream中
					// oos.writeObject(thumbs);
					// // 将编码后的字符串写到base64.xml文件中
					// Editor edit = prefs.edit();
					// edit.putString(Utils.CAST_IMAGE_URL,
					// Base64.encodeToString(
					// baos.toByteArray(), Base64.DEFAULT));
					// edit.commit();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	Context getContext() {
		return SlideShowActivity.this;
	}

	public void toggleHideyBar() {

	}

	public void hideSystemUI(View view) {
		view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
	}

	public void showSystemUI(View view) {
		view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
	}

}
