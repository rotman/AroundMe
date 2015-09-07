package com.shenkar.aroundme.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ConversationsDBHelper extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "conversations.db";
    private static final String TAG ="ConversationsDBHelper";

    public ConversationsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE "
                + ConversationsDBContract.ConversationEntry.TABLE_NAME + "("
                + ConversationsDBContract.ConversationEntry._ID + " INTEGER PRIMARY KEY, "
                + ConversationsDBContract.ConversationEntry.COLUMN_USER + " TEXT NOT NULL, "
                + ConversationsDBContract.ConversationEntry.COLUMN_FRIEND + " TEXT NOT NULL, "
                + ConversationsDBContract.ConversationEntry.COLUMN_UNREAD + " INTEGER, "
                + ConversationsDBContract.ConversationEntry.COLUMN_LAST_MODIFIED + " REAL NOT NULL, "
                + ConversationsDBContract.ConversationEntry.COLUMN_IMAGE + " TEXT, "
                + ConversationsDBContract.ConversationEntry.COLUMN_DISPLAY_NAME + " TEXT NOT NULL, "
                + "unique("+ConversationsDBContract.ConversationEntry.COLUMN_USER+","
                + ConversationsDBContract.ConversationEntry.COLUMN_FRIEND+"))";
        db.execSQL(SQL_CREATE_LOCATION_TABLE);
        Log.i(TAG, SQL_CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ConversationsDBContract.ConversationEntry.TABLE_NAME);
        onCreate(db);
    }
}
