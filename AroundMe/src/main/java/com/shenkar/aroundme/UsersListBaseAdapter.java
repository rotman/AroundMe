package com.shenkar.aroundme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.UserAroundMe;
import java.util.List;

public class UsersListBaseAdapter extends BaseAdapter {

    private Context context;
    private List<UserAroundMe> items;
    private static final String TAG = "UsersListBaseAdapter";


    public UsersListBaseAdapter(Context context, List<UserAroundMe> items) {
        this.items = items;
        this.context = context;
    }

    static class ViewHolder {
        TextView tv_mail;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        if (this.items != null && items.size() > position)
            return this.items.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.user_list_item, null);
            holder = new ViewHolder();
            holder.tv_mail = (TextView) convertView
                    .findViewById(R.id.userTextView);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tv_mail.setText(items.get(position).getMail());
        return convertView;
    }

}
