package com.janrone.app.slidealbum.slideshow.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;

import java.util.Collection;

import com.dropbox.client2.DropboxAPI;

/**
 * A GenericBitmapAdapter that loads images from the Internet.
 * <p/>
 * Created by Vincent Mimoun-Prat @ MarvinLabs on 29/05/2014.
 */
public class RemoteBitmapAdapter extends GenericRemoteBitmapAdapter<String> {

    /**
     * Constructor
     *
     * @param context The context in which the adapter is created (activity)
     * @param urls    The urls for which we have images to load
     */
    public RemoteBitmapAdapter(Context context, Collection<String> urls) {
        super(context, urls);
    }

    /**
     * Constructor
     *
     * @param context              The context in which the adapter is created (activity)
     * @param urls                 The urls for which we have images to load
     * @param bitmapFactoryOptions The options to pass to the BitmapFactory used to decode the
     *                             bitmaps
     */
    public RemoteBitmapAdapter(Context context, Collection<String> urls, BitmapFactory.Options bitmapFactoryOptions, Object api) {
        super(context, urls, bitmapFactoryOptions, api);
    }

    @Override
	protected String getItemImageUrl(String item, int position) {
        return item;
    }

}
