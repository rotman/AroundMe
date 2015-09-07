package com.shenkar.aroundme.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.GeoPt;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.Message;
import com.google.api.client.util.DateTime;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * message DAO handle all the queries being made to the conversation database
 */
public class MessagesDAO implements MessagesIDAO {
    private static final String TAG = "MessagesDAO";
    private static MessagesDAO instance;
    private Context context;
    private MessagesDBHelper dbHelper;
    private String[] messagesColumns = {MessagesDBContract.MessagesEntry._ID,
            MessagesDBContract.MessagesEntry.COLUMN_CONTENT,
            MessagesDBContract.MessagesEntry.COLUMN_FROM,
            MessagesDBContract.MessagesEntry.COLUMN_TO,
            MessagesDBContract.MessagesEntry.COLUMN_DOWNLOADED,
            MessagesDBContract.MessagesEntry.COLUMN_LAT,
            MessagesDBContract.MessagesEntry.COLUMN_LONG,
            MessagesDBContract.MessagesEntry.COLUMN_READ_RADIUS,
            MessagesDBContract.MessagesEntry.COLUMN_TIME_STAMP
    };
    private SQLiteDatabase database;

    private MessagesDAO(Context context) {
        this.context = context;
        dbHelper = new MessagesDBHelper(this.context);
    }

    /*
     * singleton implementation
     */
    public static MessagesDAO getInstance(Context context) {
        if (instance==null) {
            instance = new MessagesDAO(context);
        }
        return instance;
    }


    @Override
    public void open() throws SQLException {
        database = dbHelper.getReadableDatabase();
    }

    @Override
    public void close() {
        dbHelper.close();
    }

    /*
     * preform query to get all  messages in a conversation
     */
    @Override
    public List<Message> getMessage(String friendMail) {
        List<Message> messages = new ArrayList<>();
        Cursor cursor = database.query(MessagesDBContract.MessagesEntry.TABLE_NAME, messagesColumns,
                MessagesDBContract.MessagesEntry.COLUMN_FROM +"="+ "'" + friendMail + "'" +
                " OR " + MessagesDBContract.MessagesEntry.COLUMN_TO + "=" + "'" + friendMail +"' ORDER BY "
                + MessagesDBContract.MessagesEntry.COLUMN_TIME_STAMP, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Message m = cursorToMessages(cursor);
            messages.add(m);
            cursor.moveToNext();
        }
        cursor.close();
        return messages;
    }

    /*
     * this method add to the message table a new message
     */
    @Override
    public long addMessage(Message message) {
        if (message == null)
            return -1;
        //build the content values.
        ContentValues values = new ContentValues();
        values.put(MessagesDBContract.MessagesEntry.COLUMN_CONTENT, message.getContnet());
        values.put(MessagesDBContract.MessagesEntry.COLUMN_FROM, message.getFrom());
        values.put(MessagesDBContract.MessagesEntry.COLUMN_TO, message.getTo());
        if (message.getLocation() != null) {
            values.put(MessagesDBContract.MessagesEntry.COLUMN_LAT,message.getLocation().getLatitude());
            values.put(MessagesDBContract.MessagesEntry.COLUMN_LONG,message.getLocation().getLongitude());
        }
        boolean isDownloaded =  message.getDownloaded();
        if(isDownloaded) {
            values.put(MessagesDBContract.MessagesEntry.COLUMN_DOWNLOADED, 1);
        }else{
            values.put(MessagesDBContract.MessagesEntry.COLUMN_DOWNLOADED, 0);
        }
        values.put(MessagesDBContract.MessagesEntry.COLUMN_TIME_STAMP, message.getTimestamp().getValue());
        values.put(MessagesDBContract.MessagesEntry.COLUMN_READ_RADIUS, message.getReadRadius());

        long insertId = database.insert(MessagesDBContract.MessagesEntry.TABLE_NAME, null, values);
        if (insertId == -1) {
            Log.e(TAG, "insertId=" + insertId);
        }
        //get the entity from the data base - extra validation, entity was insert properly.
        Cursor cursor = database.query(MessagesDBContract.MessagesEntry.TABLE_NAME, messagesColumns,
                MessagesDBContract.MessagesEntry._ID + " = " + insertId, null, null, null, null);
        cursor.moveToFirst();
        Message newMessage = cursorToMessages(cursor);
        cursor.close();
        return insertId;
    }

    /*
     * when user delete conversation this method removes all related messages
     */
    @Override
    public void removeConversationMessages(String userMail) {
        database.delete(MessagesDBContract.MessagesEntry.TABLE_NAME, MessagesDBContract.MessagesEntry.COLUMN_FROM + " = " + "'" + userMail + "'"
                + " OR " + MessagesDBContract.MessagesEntry.COLUMN_TO + " = " + "'" + userMail + "'", null);
    }

    /*
     * get one specific message
     * not used in this app but for future updated
     */
    @Override
    public Message getOneMessage(long id) {
        Cursor cursor = database.query(MessagesDBContract.MessagesEntry.TABLE_NAME, messagesColumns,
                MessagesDBContract.MessagesEntry._ID +"="+ id , null, null, null, null);
        Message m=null;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
             m = cursorToMessages(cursor);
            cursor.moveToNext();
        }
        cursor.close();
        return m;
    }

    private Message cursorToMessages(Cursor cursor) {
        Message m = new Message();
        m.setId(cursor.getLong(cursor.getColumnIndex(MessagesDBContract.MessagesEntry._ID)));
        m.setContnet(cursor.getString(cursor.getColumnIndex(MessagesDBContract.MessagesEntry.COLUMN_CONTENT)));
        m.setFrom(cursor.getString(cursor.getColumnIndex(MessagesDBContract.MessagesEntry.COLUMN_FROM)));
        m.setTo(cursor.getString(cursor.getColumnIndex(MessagesDBContract.MessagesEntry.COLUMN_TO)));
        int downloaded = cursor.getInt(cursor.getColumnIndex(MessagesDBContract.MessagesEntry.COLUMN_DOWNLOADED));
        boolean isDownloaded;
        if(downloaded == 0){
            isDownloaded = false;
        }else isDownloaded = true;
        m.setDownloaded(isDownloaded);
        GeoPt geoPt = new GeoPt();
        geoPt.setLatitude(cursor.getFloat(cursor.getColumnIndex(MessagesDBContract.MessagesEntry.COLUMN_LAT)));
        geoPt.setLongitude(cursor.getFloat(cursor.getColumnIndex(MessagesDBContract.MessagesEntry.COLUMN_LONG)));
        m.setLocation(geoPt);
        m.setReadRadius(cursor.getInt(cursor.getColumnIndex(MessagesDBContract.MessagesEntry.COLUMN_READ_RADIUS)));
        long date = cursor.getLong(cursor.getColumnIndex(MessagesDBContract.MessagesEntry.COLUMN_TIME_STAMP));
        DateTime dateTime = new DateTime(date);
        m.setTimestamp(dateTime);
        return m;
    }

    /*
     * clear all messages when logging out
     */
    @Override
    public void clearAllMessages() {
        database.execSQL("delete from "+ MessagesDBContract.MessagesEntry.TABLE_NAME);
    }
}
