package com.janrone.app.slidealbum.activity;

import java.util.ArrayList;
import java.util.List;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.janrone.app.slidealbum.R;
import com.janrone.app.slidealbum.R.id;
import com.janrone.app.slidealbum.R.layout;
import com.janrone.app.slidealbum.R.menu;
import com.janrone.app.slidealbum.util.SystemBarTintManager;
import com.janrone.app.slidealbum.util.Utils;
import com.janrone.app.slidealbum.util.SystemBarTintManager.SystemBarConfig;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class FolderActivity extends Activity {
	
	private ListView mList;
	private ProgressBar mProgressBar;
	private DropboxAPI<AndroidAuthSession> mApi; 
	private FolderAdapter folderAdapter ;
	private List<Entry> listEntry = null;
	
	private boolean isLoadFolder;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_folder);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		 
		mList = (ListView) findViewById(R.id.list);
		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
		
		mApi = CloudCast.mApi;
		
		initSystemBar();
		loadFolders();
		
	}
	
	private void initSystemBar() {
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
	        //setTranslucentStatus(true);
	        SystemBarTintManager tintManager = new SystemBarTintManager(this);
	        tintManager.setStatusBarTintEnabled(true);
	        
	        int actionBarColor = Color.parseColor("#DDDDDD");
	        tintManager.setStatusBarTintColor(actionBarColor);
	        //tintManager.setStatusBarTintResource(android.R.drawable.ic_notification_overlay);
	        SystemBarConfig config = tintManager.getConfig();
	        mList.setPadding(0, config.getPixelInsetTop(true), 0, config.getPixelInsetBottom());
	    }
	}
	
	private void loadFolders() {
		// TODO Auto-generated method stub
		new GetFolders().execute();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_folder_activity, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		int id = item.getItemId();
		if (id == R.id.action_done && isLoadFolder) {
			folderAdapter.saveFolderUrl();
			setResult(RESULT_OK);
			finish();
		}
		return false;
	}

	
	class GetFolders extends AsyncTask<Void, Void, Void>{

		private Entry entry = null;
	
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			try {
				entry = mApi.metadata(Utils.ROOT_PATH, Utils.FILE_LIMIT, null, true, null);
				listEntry = new ArrayList<Entry>();
				for (Entry e : entry.contents) {
					if (entry.isDir) {
						listEntry.add(e);
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
			super.onPostExecute(result);
			mProgressBar.setVisibility(View.GONE);
			if (listEntry == null){
				isLoadFolder = false;
				Toast.makeText(FolderActivity.this, "get folder error !", Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
			isLoadFolder = true;
			folderAdapter = new FolderAdapter(FolderActivity.this, listEntry);
			mList.setAdapter(folderAdapter);
		}
	}
}
