package com.shenkar.aroundme;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.shenkar.aroundme.bl.AroundMeApplication;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * handle the setting screen to present a GUI
 */
public class SettingsActivity extends ActionBarActivity {

    @Bind(R.id.settingsListView)
    ListView listView;

    /**
     * Called when the activity is first created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        //handle action bar colors and text
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#4daf4e"));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#ffffff'> Settings </font>"));
        SettingsAdapter adapter = new SettingsAdapter(this, R.layout.settings_list_item, addItems());
        listView.setAdapter(adapter);
        //listener on setting object
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    //sent to the about activity
                    case 0:
                        Intent aboutIntent = new Intent(SettingsActivity.this,AboutActivity.class);
                        startActivity(aboutIntent);
                        break;
                    //send to the bg picker activity
                    case 1:
                        Intent pickerIntent = new Intent(SettingsActivity.this,BgPickerActivity.class);
                        startActivity(pickerIntent);
                        break;
                    //send to the logout function
                    case 2:
                        new MaterialDialog.Builder(SettingsActivity.this)
                                .title("Logout?")
                                .content("All conversations will be deleted...")
                                .negativeText("no")
                                .positiveText("yes")
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        super.onPositive(dialog);
                                        AroundMeApplication application = (AroundMeApplication)getApplication();
                                        application.getController().logoutUser();
                                        Intent i = new Intent(SettingsActivity.this,MainActivity.class);
                                        startActivity(i);
                                    }
                                }).show();

                        break;
                }
            }
        });

    }

    //all item in the setting activity
    private ArrayList<SettingItem> addItems() {
        ArrayList<SettingItem> arrayList = new ArrayList<>();
        arrayList.add(new SettingItem("About",R.drawable.ic_action_about));
        arrayList.add(new SettingItem("Chat wallpaper",R.drawable.ic_action_new_picture));
        arrayList.add(new SettingItem("Logout",R.drawable.ic_action_cancel));
        return arrayList;
    }


}
