package com.shenkar.aroundme.geolocation;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.Conversation;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.GeoPt;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.Message;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.UserAroundMe;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.appConst;
import com.shenkar.aroundme.MainActivity;
import com.shenkar.aroundme.R;
import com.shenkar.aroundme.bl.AroundMeApplication;
import com.google.api.client.util.DateTime;

/**
 * handle the intent service of the receiving end
 */
public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG = "GeofenceTransitions";
    private NotificationManager mNotificationManager;
    public static final int NOTIFICATION_ID = 1;
    private static AroundMeApplication aroundMeApplication;

    public GeofenceTransitionsIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        aroundMeApplication = (AroundMeApplication)getApplication();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String from = extras.getString(appConst.MESSAGE_FROM);
            String content = extras.getString(appConst.MESSAGE_CONTENT);
            String to = extras.getString(appConst.MY_MAIL);
            float lat = extras.getFloat(appConst.MY_LAT);
            float lon = extras.getFloat(appConst.MY_LONG);
            GeoPt geoPt = new GeoPt();
            geoPt.setLongitude(lon);
            geoPt.setLatitude(lat);
            Message m = new Message();
            m.setLocation(geoPt);
            m.setFrom(from);
            m.setContnet(content);
            m.setTo(to);
            m.setDownloaded(true);
            m.setTimestamp(new DateTime(System.currentTimeMillis()));
            String messageDetailes = m.getContnet() + ", from:"
                    + m.getFrom();
            sendNotification(messageDetailes);
            Conversation conversation = new Conversation();
            UserAroundMe newUser = new UserAroundMe();
            newUser.setMail(m.getFrom());
            AroundMeApplication aroundMeApplication = (AroundMeApplication)getApplication();
            newUser.setImageUrl(aroundMeApplication.getController().getUserImageUrl(newUser.getMail(),0));
            DateTime lastSeen = new DateTime(System.currentTimeMillis());
            newUser.setLastSeen(lastSeen);
            conversation.setDisplayName(aroundMeApplication.getController().getUserImageUrl(newUser.getMail(),1));
            conversation.setUser1(aroundMeApplication.getController().getMyUser());
            conversation.setUser2(newUser);
            conversation.setImageUrl(newUser.getImageUrl());
            conversation.setLastModified(System.currentTimeMillis());
            conversation.setUnreadMessages(1);
            long cid = -1;
            cid = aroundMeApplication.getController().checkIfConversationExists(newUser.getMail());
            Log.i(TAG, "cid is:" + cid);
            if (cid == -1) {
                cid = aroundMeApplication.getController().addConversation(conversation);
                Log.i(TAG, "cid is:"+cid);
            }
            else { //just update the database
                aroundMeApplication.getController().updateUnreadMessages(conversation.getUser2().getMail(),true);
                aroundMeApplication.getController().updateLastModified(conversation.getUser2().getMail());
            }
            Intent cIntent = new Intent();
            cIntent.putExtra(appConst.C_ID,cid);
            cIntent.putExtra(appConst.MESSAGE_FROM,m.getFrom());
            cIntent.setAction(appConst.NEW_CONVERSATION);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(cIntent);
            long mid = aroundMeApplication.getController().addMassage(m);
            Intent i = new Intent();
            i.putExtra(appConst.M_ID,mid);
            i.putExtra(appConst.USER_MAIL_FOR_INTENT,newUser.getMail());
            i.setAction(appConst.NEW_MESSAGE);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
        }
    }


    /*
     * on receving this message send notification to the user
     */
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("GeoFence notification")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }


}
