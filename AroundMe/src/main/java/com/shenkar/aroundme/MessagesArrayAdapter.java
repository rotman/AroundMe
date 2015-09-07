package com.shenkar.aroundme;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.Message;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.appConst;
import com.google.api.client.util.DateTime;

/**
 * adapter for presenting the messages and putting the in an array
 * also present the GUI of bubble chat
 */
public class MessagesArrayAdapter extends ArrayAdapter<Message> {

    private ImageView imageViewLocation;
    private List<Message> messages = new ArrayList<Message>();
    private static final String TAG = "MessagesArrayAdapter";



    @Override
    public void add(Message object) {
        messages.add(object);
        super.add(object);
    }

    public MessagesArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public int getCount() {
        return this.messages.size();
    }

    public Message getItem(int index) {
        return this.messages.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.checkbox_list_item, parent, false);
        }

        LinearLayout wrapper = (LinearLayout) row.findViewById(R.id.wrapper);
        Message msg = getItem(position);
        TextView messageBubble = (TextView) row.findViewById(R.id.comment);
        DateTime dateTime = msg.getTimestamp();
        Date date = new Date(dateTime.getValue());
        Format format = new SimpleDateFormat("HH:mm");
        messageBubble.setText(Html.fromHtml(msg.getContnet() + "<br>" + format.format(date)));
        final SharedPreferences prefs = getContext().getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
        String myMail = prefs.getString(appConst.MY_MAIL, "");
        messageBubble.setBackgroundResource(msg.getFrom().equals(myMail) ? R.drawable.bubble_yellow : R.drawable.bubble_green);
        wrapper.setGravity(msg.getFrom().equals(myMail) ? Gravity.LEFT : Gravity.RIGHT);

        return row;
    }
}
