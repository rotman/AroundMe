package com.shenkar.aroundme;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.appConst;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * bg picker is in the setting portion of the app and allows you to pick
 * a background by using a multiple number flag in the shared preference
 */
public class BgPickerActivity extends Activity {

    // Find all the views on the page
    @Bind(R.id.gridview)
    GridView gridview;


    /**
     * Called when the activity is first created
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bg_picker);
        ButterKnife.bind(this);
        //creates the adapter and the adapter listener to choose the background
        gridview.setAdapter(new ImageAdapter(this));
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                SharedPreferences prefs = BgPickerActivity.this.getSharedPreferences(BgPickerActivity.class.getSimpleName(),
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                //check if background chosen is the default background
                if(position == 4){
                    position = -1;
                }
                editor.putInt(appConst.BG_NUM, position);
                editor.apply();
                finish();
                Toast.makeText(BgPickerActivity.this, "Background Changed",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}
