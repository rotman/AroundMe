package com.shenkar.aroundme;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * image adapter for the background picking screen
 * by displaying a grid of photo to choose for
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;

    public ImageAdapter(Context c) {
        mContext = c;
    }

    /**
     * get count in image adapter
     * @return length
     */
    public int getCount() {
        return mThumbIds.length;
    }

    /**
     * get item
     * @param position
     * @return null
     */
    public Object getItem(int position) {
        return null;
    }

    /**
     *
     * @param position
     * @return 0
     */
    public long getItemId(int position) {
        return 0;
    }

    /**
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     * create a new ImageView for each item referenced by the Adapter
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(250, 250));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.bg0, R.drawable.bg1,
            R.drawable.bg2, R.drawable.bg3,
            R.drawable.bgd
    };
}