package com.hujl.slidealbum;


import java.util.List;
import com.dropbox.client2.DropboxAPI.Entry;
import com.hujl.slidealbum.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class FolderAdapter extends BaseAdapter{

	private Context mContext;
	private List<Entry> mListEntry;
	
	String folderPath = "";
	SharedPreferences prefs;
	
	public FolderAdapter(Context context, List<Entry> entry) {
		// TODO Auto-generated constructor stub
		this.mContext = context;
		this.mListEntry = entry;
		
		prefs = mContext.getSharedPreferences(Utils.ACCOUNT_PREFS_NAME, 0);
		//folderPath = prefs.getString(Utils.SELECT_FOLDER_URL, "");
	}
	
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mListEntry.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mListEntry.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHodler hodler = null;
		if (convertView == null) {
			convertView = View.inflate(mContext, R.layout.folder_adapter_item, null);
			hodler = new ViewHodler();
			hodler.tvName = (TextView) convertView.findViewById(R.id.tv_name);
			hodler.cbSelect = (CheckBox) convertView.findViewById(R.id.cb_select);
			
			convertView.setTag(hodler);
		}else{
			hodler = (ViewHodler) convertView.getTag();
		}
		
		final Entry entry = (Entry) getItem(position);
		hodler.tvName.setText(entry.fileName());
		if (entry.isDir) {
			hodler.cbSelect.setVisibility(View.VISIBLE);
		}else{
			hodler.cbSelect.setVisibility(View.INVISIBLE);
		}
		
		hodler.cbSelect.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					folderPath = folderPath + entry.path + ";";
					//prefs.edit().putString(Utils.SELECT_FOLDER_URL, "");
				}else{
					folderPath.replace(entry.path+";", "");
				}
			}
		});
		
		return convertView;
		
		//[{id:1,url:"test"},{id:1,url:"test"}]
	}
	
	public void saveFolderUrl(){
		Editor edit = prefs.edit();
		edit.putString(Utils.SELECT_FOLDER_URL, folderPath);
		edit.commit();
	}
	
	static class ViewHodler{
		TextView tvName;
		CheckBox cbSelect;
	}
	
}


