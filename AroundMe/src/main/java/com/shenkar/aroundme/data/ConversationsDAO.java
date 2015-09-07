package com.shenkar.aroundme.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.Conversation;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.User;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.UserAroundMe;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * this class is the conversation DAO and it handle everything in the
 * conversation database, this database is to increase speed of the app when loading the
 * conversation page it just loads this database
 */
public class ConversationsDAO implements ConversationsIDAO {

    private static final String TAG = "ConversationsDAO";
    private static ConversationsDAO instance;
    private Context context;
    private ConversationsDBHelper dbHelper;
    private String[] conversationColumns = {ConversationsDBContract.ConversationEntry._ID,
            ConversationsDBContract.ConversationEntry.COLUMN_USER,
            ConversationsDBContract.ConversationEntry.COLUMN_FRIEND,
            ConversationsDBContract.ConversationEntry.COLUMN_UNREAD,
            ConversationsDBContract.ConversationEntry.COLUMN_LAST_MODIFIED,
            ConversationsDBContract.ConversationEntry.COLUMN_IMAGE,
            ConversationsDBContract.ConversationEntry.COLUMN_DISPLAY_NAME
    };
    private SQLiteDatabase database;

    private ConversationsDAO(Context context) {
        this.context = context;
        dbHelper = new ConversationsDBHelper(this.context);
    }

    /*
     * singleton implementation
     */
    public static ConversationsDAO getInstance(Context context) {
        if (instance==null) {
            instance = new ConversationsDAO(context);
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
     * method that open the database and makes a query to get conversations
     */
    @Override
    public List<Conversation> getConversations() {
        List<Conversation> conversations = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + ConversationsDBContract.ConversationEntry.TABLE_NAME+
        " ORDER BY "+ ConversationsDBContract.ConversationEntry.COLUMN_LAST_MODIFIED + " ASC",null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Conversation c = cursorToConversation(cursor);
            conversations.add(c);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return conversations;
    }

    /*
     * this method adds a conversation to the database after checking if exist
     */
    @Override
    public Conversation addConversation(Conversation conversation) {
        if (conversation == null)
            return null;
        //build the content values.
        ContentValues values = new ContentValues();
        values.put(ConversationsDBContract.ConversationEntry.COLUMN_USER, conversation.getUser1().getMail());
        values.put(ConversationsDBContract.ConversationEntry.COLUMN_FRIEND, conversation.getUser2().getMail());
        values.put(ConversationsDBContract.ConversationEntry.COLUMN_UNREAD,conversation.getUnreadMessages());
        values.put(ConversationsDBContract.ConversationEntry.COLUMN_IMAGE,conversation.getUser2().getImageUrl());
        values.put(ConversationsDBContract.ConversationEntry.COLUMN_LAST_MODIFIED,System.currentTimeMillis());
        values.put(ConversationsDBContract.ConversationEntry.COLUMN_DISPLAY_NAME,conversation.getDisplayName());
        //do the insert.
        long insertId = database.insertWithOnConflict(ConversationsDBContract.ConversationEntry.TABLE_NAME, null, values,SQLiteDatabase.CONFLICT_IGNORE);
        if (insertId == -1) {
            Log.e(TAG,"insertId="+insertId);
        }
        //get the entity from the data base - extra validation, entity was insert properly.
        Cursor cursor = database.query(ConversationsDBContract.ConversationEntry.TABLE_NAME, conversationColumns,
                ConversationsDBContract.ConversationEntry._ID + " = " + insertId, null, null, null, null);
        cursor.moveToFirst();
        Conversation newConversation = cursorToConversation(cursor);
        cursor.close();
        return newConversation;
    }

    private Conversation cursorToConversation(Cursor cursor) {
        Conversation c = new Conversation();
        c.setId(cursor.getInt(cursor.getColumnIndex(ConversationsDBContract.ConversationEntry._ID)));
        User user1 = new User();
        user1.setMail(cursor.getString(cursor
                .getColumnIndex(ConversationsDBContract.ConversationEntry.COLUMN_USER)));
        c.setUser1(user1);
        UserAroundMe user2 = new UserAroundMe();
        user2.setMail(cursor.getString(cursor
                .getColumnIndex(ConversationsDBContract.ConversationEntry.COLUMN_FRIEND)));
        c.setImageUrl(cursor.getString(cursor
                .getColumnIndex(ConversationsDBContract.ConversationEntry.COLUMN_IMAGE)));
        c.setUnreadMessages(cursor.getInt(cursor.getColumnIndex(ConversationsDBContract.ConversationEntry.COLUMN_UNREAD)));
        c.setUser2(user2);
        c.setLastModified(cursor.getLong(cursor.getColumnIndex(ConversationsDBContract.ConversationEntry.COLUMN_LAST_MODIFIED)));
        c.setDisplayName(cursor.getString(cursor
                .getColumnIndex(ConversationsDBContract.ConversationEntry.COLUMN_DISPLAY_NAME)));
        return c;
    }

    /*
     * receives a conversation and delete it from the database
     */
    @Override
    public void removeConversation(Conversation conversation) {
        long id = conversation.getId();
        database.delete(ConversationsDBContract.ConversationEntry.TABLE_NAME, ConversationsDBContract.ConversationEntry._ID + " = " + id,
                null);

    }

    /*
     * method that increment the counter of unread message by one and updated the database
     */
    @Override
    public int updateUnreadMessages(String friendMail, boolean isNew) {
        int counter = 0;
        if (isNew) { //increment the unread messages counter
            Cursor cursor = database.rawQuery("SELECT "+ ConversationsDBContract.ConversationEntry.COLUMN_UNREAD+
                    " FROM " + ConversationsDBContract.ConversationEntry.TABLE_NAME+
                    " WHERE "+ ConversationsDBContract.ConversationEntry.COLUMN_FRIEND+"='"+friendMail+"'",null);
            if (cursor.moveToFirst()) {
                counter = cursor.getInt(cursor.getColumnIndex(ConversationsDBContract.ConversationEntry.COLUMN_UNREAD));
                counter++;
            }
        }
        ContentValues value = new ContentValues();
        value.put(ConversationsDBContract.ConversationEntry.COLUMN_UNREAD,counter);
        int rowsAffected = database.update(ConversationsDBContract.ConversationEntry.TABLE_NAME,value,
                ConversationsDBContract.ConversationEntry.COLUMN_FRIEND+"='"+friendMail+"'",null);
        if (rowsAffected == 0) {
            Log.e(TAG,"no affect on database from updateUnreadMessages");
        }
        return counter;
    }

    /*
     * this method update the last modified column to the time of the last message
     */
    @Override
    public void updateLastModified(String friendMail) {
        ContentValues values = new ContentValues();
        values.put(ConversationsDBContract.ConversationEntry.COLUMN_LAST_MODIFIED, System.currentTimeMillis());
        int rowsAffected = database.update(ConversationsDBContract.ConversationEntry.TABLE_NAME,values,
                ConversationsDBContract.ConversationEntry.COLUMN_FRIEND+"='"+friendMail+"'",null);
        if (rowsAffected == 0) {
            Log.e(TAG,"no affect on database from updateLastModified");
        }
    }

    /*
     * this methos get a specific conversation and removes it
     */
    @Override
    public Conversation getConversation(long cId) {
        Cursor cursor = database.query(ConversationsDBContract.ConversationEntry.TABLE_NAME, conversationColumns,
                ConversationsDBContract.ConversationEntry._ID +"="+ cId , null, null, null, null);
        Conversation c = null;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            c = cursorToConversation(cursor);
            cursor.moveToNext();
        }
        cursor.close();
        return c;
    }

    /*
     * when user wants to log out we use this clear table
     */
    @Override
    public void clearTable() {
        database.execSQL("delete from "+ ConversationsDBContract.ConversationEntry.TABLE_NAME);
    }

}
