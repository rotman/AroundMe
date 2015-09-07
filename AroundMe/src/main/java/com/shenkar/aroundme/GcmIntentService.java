package com.shenkar.aroundme;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.appspot.enhanced_cable_88320.aroundmeapi.Aroundmeapi;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.Conversation;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.Message;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.UserAroundMe;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.appConst;
import com.shenkar.aroundme.bl.AroundMeApplication;
import com.shenkar.aroundme.geolocation.GeoFenceActivity;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.util.DateTime;


public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private static final String TAG = "GcmIntentService";
    private NotificationManager mNotificationManager;
    private static AroundMeApplication aroundMeApplication;
    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);
        String messageId = intent.getStringExtra("newMessage");
        //Intent msgIntent = new Intent(this,MessageActivity.class);
        if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
                    .equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
                    .equals(messageType)) {
                sendNotification("Deleted messages on server: "
                        + extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
                    .equals(messageType)) {
                String newMatch = intent.getStringExtra("message");
                if (newMatch != null) {
                    sendNotification("Received New message : " + newMatch);
                }
                if (messageId != null) {
                    try {
                        Aroundmeapi api = EndpointApiCreator
                                .getApi(Aroundmeapi.class);
                        Message m = api.getMessage(Long.parseLong(messageId)).execute();
                        if (m.getLocation() != null) {
                            Intent geoFenceIntent = new Intent(getBaseContext(), GeoFenceActivity.class);
                            geoFenceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            geoFenceIntent.putExtra(appConst.MY_LAT, m.getLocation().getLatitude());
                            geoFenceIntent.putExtra(appConst.MY_LONG,m.getLocation().getLongitude());
                            geoFenceIntent.putExtra(appConst.M_ID,m.getId());
                            geoFenceIntent.putExtra(appConst.GEOFENCE_RADIUS_IN_METERS,m.getReadRadius());
                            geoFenceIntent.putExtra(appConst.MESSAGE_CONTENT,m.getContnet());
                            geoFenceIntent.putExtra(appConst.MESSAGE_FROM,m.getFrom());
                            geoFenceIntent.putExtra(appConst.MY_MAIL,m.getTo());
                            getApplication().startActivity(geoFenceIntent);
                        }
                        else {
                            String messageDetailes = m.getContnet() + ", from:"
                                    + m.getFrom();
                            sendNotification(messageDetailes);
                            Conversation conversation = new Conversation();
                            UserAroundMe newUser = new UserAroundMe();
                            newUser.setMail(m.getFrom());
                            aroundMeApplication = (AroundMeApplication)getApplication();
                            newUser.setImageUrl(aroundMeApplication.getController().getUserImageUrl(newUser.getMail(), 0));
                            DateTime lastSeen = new DateTime(System.currentTimeMillis());
                            newUser.setLastSeen(lastSeen);
                            conversation.setUser1(aroundMeApplication.getController().getMyUser());
                            conversation.setDisplayName(aroundMeApplication.getController().getUserImageUrl(newUser.getMail(), 1));
                            conversation.setUser2(newUser);
                            conversation.setImageUrl(newUser.getImageUrl());
                            conversation.setLastModified(System.currentTimeMillis());
                            conversation.setUnreadMessages(1);
                            long cid = -1;
                            cid = aroundMeApplication.getController().checkIfConversationExists(newUser.getMail());
                            Log.i(TAG, "cid is:"+cid);
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

                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
                // Post notification of received message.
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("GCM Notification")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }


}