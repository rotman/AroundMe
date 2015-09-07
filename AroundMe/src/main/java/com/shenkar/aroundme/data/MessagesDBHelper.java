package com.shenkar.aroundme.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MessagesDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "messages.db";
    private static final String TAG ="MessagesDBHelper";

    public MessagesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE "
                + MessagesDBContract.MessagesEntry.TABLE_NAME + "("
                + MessagesDBContract.MessagesEntry._ID + " INTEGER PRIMARY KEY, "
                + MessagesDBContract.MessagesEntry.COLUMN_CONTENT + " TEXT NOT NULL, "
                + MessagesDBContract.MessagesEntry.COLUMN_DOWNLOADED + " INTEGER, "
                + MessagesDBContract.MessagesEntry.COLUMN_FROM+ " TEXT NOT NULL, "
                + MessagesDBContract.MessagesEntry.COLUMN_TO+ " TEXT NOT NULL, "
                + MessagesDBContract.MessagesEntry.COLUMN_LAT+ " FLOAT, "
                + MessagesDBContract.MessagesEntry.COLUMN_LONG+ " FLOAT, "
                + MessagesDBContract.MessagesEntry.COLUMN_TIME_STAMP + " REAL NOT NULL, "
                + MessagesDBContract.MessagesEntry.COLUMN_READ_RADIUS+ " INTEGER"
                + ")";
        db.execSQL(SQL_CREATE_LOCATION_TABLE);
        Log.i(TAG, SQL_CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ConversationsDBContract.ConversationEntry.TABLE_NAME);
        onCreate(db);
    }
}
