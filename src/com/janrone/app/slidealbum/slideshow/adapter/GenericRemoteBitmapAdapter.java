package com.janrone.app.slidealbum.slideshow.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.InputStream;
import java.util.Collection;

import com.baidu.pcs.BaiduPCSActionInfo;
import com.baidu.pcs.BaiduPCSClient;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.ThumbFormat;
import com.dropbox.client2.DropboxAPI.ThumbSize;
import com.janrone.app.slidealbum.util.AccountType;
import com.janrone.app.slidealbum.util.Utils;

/**
 * A GenericBitmapAdapter that loads images from the Internet.
 * <p/>
 * Created by Vincent Mimoun-Prat @ MarvinLabs on 29/05/2014.
 */
public abstract class GenericRemoteBitmapAdapter<T> extends GenericBitmapAdapter<T> {

    // Options for the BitmapFactory to decode the bitmap
    private BitmapFactory.Options bitmapFactoryOptions;
    
    private Object mApi;

    //==============================================================================================
    // GENERAL METHODS
    //==

    /**
     * Constructor
     *
     * @param context The context in which the adapter is created (activity)
     * @param items   The items for which we have images to load
     */
    public GenericRemoteBitmapAdapter(Context context, Collection<T> items) {
        super(context, items);
    }

    /**
     * Constructor
     *
     * @param context              The context in which the adapter is created (activity)
     * @param items                The items for which we have images to load
     * @param bitmapFactoryOptions The options to pass to the BitmapFactory used to decode the
     *                             bitmaps
     */
    public GenericRemoteBitmapAdapter(Context context, Collection<T> items, BitmapFactory.Options bitmapFactoryOptions) {
        super(context, items);
        this.bitmapFactoryOptions = bitmapFactoryOptions;
    }
    
    public GenericRemoteBitmapAdapter(Context context, Collection<T> items, BitmapFactory.Options bitmapFactoryOptions, Object api) {
        super(context, items);
        this.bitmapFactoryOptions = bitmapFactoryOptions;
        this.mApi = api;
    }


    //==============================================================================================
    // ASYNC MANAGEMENT METHODS
    //==

    protected abstract String getItemImageUrl(T item, int position);
    
    //protected abstract String getRandomItemImageUrl();

    @Override
    protected Bitmap asyncLoadBitmap(T item, int position) {
        InputStream in = null;
        AccountType accountType = Utils.mAccountType;
        try {
        	switch (accountType) {
			case DROPBOX:{
				in = ((DropboxAPI<?>)mApi).getThumbnailStream(getItemImageUrl(item, position), ThumbSize.BESTFIT_1024x768, ThumbFormat.JPEG);
	            Bitmap bm = BitmapFactory.decodeStream(in, null, bitmapFactoryOptions);
	            return bm;
	            }

			case BAIDU:{
				BaiduPCSClient api = new BaiduPCSClient();
				api.setAccessToken(Utils.mAccessToken);
				final BaiduPCSActionInfo.PCSThumbnailResponse ret = api.thumbnail(getItemImageUrl(item, position), 100, 1024, 768);
				if(null != ret && null != ret.bitmap){
					return ret.bitmap;
				}
				return null;
				}
        	}
        } catch (Exception e) {
            Log.e("RemoteImageSlide", "Error while downloading image slide", e);
            e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
            } catch (Exception e) { /* Ignored */ }
        }
        return null;
    }

}
