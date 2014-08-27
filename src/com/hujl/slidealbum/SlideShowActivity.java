package com.hujl.slidealbum;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.google.gson.Gson;
import com.hujl.slidealbum.R;
import com.marvinlabs.widget.slideshow.SlideShowAdapter;
import com.marvinlabs.widget.slideshow.SlideShowView;
import com.marvinlabs.widget.slideshow.adapter.GenericBitmapAdapter;
import com.marvinlabs.widget.slideshow.adapter.RemoteBitmapAdapter;
import com.marvinlabs.widget.slideshow.picasso.GenericPicassoBitmapAdapter;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class SlideShowActivity extends Activity {

	private DropboxAPI<AndroidAuthSession> mApi;

	private SlideShowView slideShowView;
	private SlideShowAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Go fullscreen
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Set screen always on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Activity layout
		setContentView(R.layout.activity_slideshow);

		slideShowView = (SlideShowView) findViewById(R.id.slideshow);

		AndroidAuthSession session = Utils.buildSession(SlideShowActivity.this);
		mApi = new DropboxAPI<AndroidAuthSession>(session);
		CloudCast.mApi = mApi;

	}

	@SuppressWarnings("unchecked")
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
				Utils.storeAuth(SlideShowActivity.this, session);
				Utils.setLoggedIn(SlideShowActivity.this, true);
			} catch (IllegalStateException e) {
				// showToast("Couldn't authenticate with Dropbox:"
				// + e.getLocalizedMessage());
				Log.i(Utils.TAG, "Error authenticating", e);
			}
		}

		//String castImageUrl = prefs.getString(Utils.CAST_IMAGE_URL, "");
		// if (castImageUrl.equals("")) {
		// new GetCastImage().execute(folderPath, null, null);
		// }

		// ArrayList<String> list = getCastImageUrlsFromPrefs();
		// if(list != null) startSlideShow(list);

		if (!Utils.isExistsImageUrlFile(getContext()) || getImageUrlListFromFile() == null || getImageUrlListFromFile().size() == 0) {
			new GetCastImage().execute(getImageFolder(), null, null);
		} else {
			startSlideShow(getImageUrlListFromFile());
		}
	}

	@SuppressWarnings("unchecked")
	public ArrayList<String> getImageUrlListFromFile() {
		try {
			Gson gson = new Gson();
			return gson.fromJson(
					Utils.readAssetsFileString(getContext()),
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
		if (Utils.mLoggedIn) {
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
		if (id == R.id.action_dropbox) {
			if (Utils.mLoggedIn == true) {
				logOut();
			} else {
				mApi.getSession().startOAuth2Authentication(
						SlideShowActivity.this);
			}
		} else {
			if (Utils.mLoggedIn == true) {
				Intent intent = new Intent(SlideShowActivity.this,
						FolderActivity.class);
				startActivityForResult(intent, Utils.REQUEST_FOLDER_ACTIVITY);
			} else {
				mApi.getSession().startOAuth2Authentication(
						SlideShowActivity.this);
			}
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
			Toast.makeText(SlideShowActivity.this,
					"Slide clicked: " + position, Toast.LENGTH_SHORT).show();
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

}
