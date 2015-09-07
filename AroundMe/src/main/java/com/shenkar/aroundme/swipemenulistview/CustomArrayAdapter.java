package com.shenkar.aroundme.swipemenulistview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.Conversation;
import com.shenkar.aroundme.ApplicationCallback;
import com.shenkar.aroundme.R;
import com.shenkar.aroundme.bl.AroundMeApplication;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * handle the conversation list adapter
 */
public class CustomArrayAdapter extends ArrayAdapter<Conversation> {
    private List<Conversation> conversations = new ArrayList<>();
    protected LayoutInflater inflater;
    protected int layout;
    private static AroundMeApplication application;
    private static final String TAG = "CustomArrayAdapter";

    /*
     * add and object to the list
     */
    @Override
    public void add(Conversation object) {
        conversations.add(object);
        super.add(object);
    }

    static class ViewHolder {
        TextView tv_mail;
        ImageView iv_image;
        TextView tv_newMessages;
        TextView tv_lastModified;
    }

    public CustomArrayAdapter(Activity activity, int resourceId, List<Conversation> objects){
        super(activity, resourceId, objects);
        layout = resourceId;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        conversations = objects;
        application = (AroundMeApplication)activity.getApplication();
    }

    public int getCount() {
        return this.conversations.size();
    }

    public Conversation getItem(int index) {
        return this.conversations.get(index);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.conversation_list_item, null);
            holder = new ViewHolder();
            holder.tv_mail = (TextView) convertView
                    .findViewById(R.id.userNameTextView);
            holder.iv_image = (ImageView)convertView
                    .findViewById(R.id.userImageView);
            holder.tv_newMessages = (TextView) convertView
                    .findViewById(R.id.messagesCounterTextView);
            holder.tv_lastModified = (TextView)convertView
                    .findViewById(R.id.lastModifiedTextView);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        String userMail = conversations.get(position).getUser2().getMail();
        String userName = conversations.get(position).getDisplayName();
        if(userName.length() > 16){
            String userNameNew = userName.substring(0,15) + "...";
            holder.tv_mail.setText(userNameNew);
        }else {
            holder.tv_mail.setText(userName);
        }
        String url = conversations.get(position).getImageUrl();
        application.getController().getImage(userMail, url, new ApplicationCallback<Bitmap>() {
            @Override
            public void done(Bitmap result, Exception e) {
                if (e == null)
                    holder.iv_image.setImageBitmap(result);
                else
                    Log.e(TAG,e.getMessage());

            }
        });
        long lastModifed = conversations.get(position).getLastModified();
        Date lastModifedDate = new Date(lastModifed);
        Format format = new SimpleDateFormat("dd/MM/yyyy");
        String sLastModified = format.format(lastModifedDate);
        long current = System.currentTimeMillis();
        Date currentDate = new Date(current);
        String sCurrent = format.format(currentDate);
        if (lastModifed >0 && sLastModified.equals(sCurrent)) {
            holder.tv_lastModified.setText(convertTimeToday(lastModifed));
        }
        else {
            holder.tv_lastModified.setText(convertTimeNotToday(lastModifed));
        }

        int counter = conversations.get(position).getUnreadMessages();
        Log.i(TAG,"userMail="+conversations.get(position).getUser2().getMail());
        Log.i(TAG,"value of unreadMessages="+counter);
        if (counter > 0) {
            holder.tv_newMessages.setText(String.valueOf(counter));
            holder.tv_newMessages.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    /*
     * if message was sent the same day
     */
    public String convertTimeToday(long time) {
        Date date = new Date(time);
        Format format = new SimpleDateFormat("HH:mm");
        return format.format(date);
    }

    /*
     * if message was sent before midnight
     */
    public String convertTimeNotToday(long time) {
        Date date = new Date(time);
        Format format = new SimpleDateFormat("dd/MM");
        return format.format(date);
    }
}
