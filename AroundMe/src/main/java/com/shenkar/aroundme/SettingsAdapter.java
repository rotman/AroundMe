package com.shenkar.aroundme;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.shenkar.aroundme.bl.AroundMeApplication;
import java.util.ArrayList;
import java.util.List;

/**
 * the adapter that handle all of the setting screen
 */
public class SettingsAdapter extends ArrayAdapter<SettingItem> {

    private List<SettingItem> settingItems = new ArrayList<>();
    protected LayoutInflater inflater;
    protected int layout;
    private static AroundMeApplication application;
    private static final String TAG = "CustomArrayAdapter";

    public SettingsAdapter(Activity activity, int resourceId, List<SettingItem> objects){
        super(activity, resourceId, objects);
        layout = resourceId;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        settingItems = objects;
        application = (AroundMeApplication)activity.getApplication();
    }

    /*
     * add object to setting
     */
    @Override
    public void add(SettingItem object) {
        settingItems.add(object);
        super.add(object);
    }

    static class ViewHolder {
        ImageView iv_image;
        TextView tv_name;
    }

    public int getCount() {
        return this.settingItems.size();
    }

    public SettingItem getItem(int index) {
        return this.settingItems.get(index);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.settings_list_item, null);
            holder = new ViewHolder();
            holder.tv_name = (TextView) convertView
                    .findViewById(R.id.settingsNameTextView);
            holder.iv_image = (ImageView)convertView
                    .findViewById(R.id.settingsImageView);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();

        }
        holder.tv_name.setText(settingItems.get(position).getName());
        holder.iv_image.setImageResource(settingItems.get(position).getImage());
        return convertView;
    }
}
