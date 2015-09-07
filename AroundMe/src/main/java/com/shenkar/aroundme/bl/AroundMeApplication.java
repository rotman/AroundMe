package com.shenkar.aroundme.bl;

import android.annotation.TargetApi;
import android.app.Application;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.util.LruCache;

/**
 * this class contains the controller to be created once
 */
public class  AroundMeApplication extends Application {
    private MainController controller;
    private LruCache<String, Bitmap> imagesCache;
    public MainController getController() {
        if(controller == null)
            controller = new MainController(getApplicationContext());
        return controller;
    }

    /*
     * cache for the image of users
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public LruCache<String,Bitmap> getImagesCache() {
        if (imagesCache == null) {
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            // Use 1/8th of the available memory for this memory cache.
            final int cacheSize = maxMemory / 8;
            imagesCache = new LruCache<>(cacheSize); //4MiB
        }
        return imagesCache;
    }



}
