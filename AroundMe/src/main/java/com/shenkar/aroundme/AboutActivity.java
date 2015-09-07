package com.shenkar.aroundme;

import android.app.Activity;
import android.os.Bundle;

/**
 * about activity with the developer name and app logo
 */
public class AboutActivity extends Activity {

    /**
     * Called when the activity is first created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }
}
