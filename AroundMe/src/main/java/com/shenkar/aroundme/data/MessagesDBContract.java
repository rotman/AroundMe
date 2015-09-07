package com.shenkar.aroundme.data;

import android.provider.BaseColumns;

/**
 * all the columns of the messages database
 */
public class MessagesDBContract {
    public static final class MessagesEntry implements BaseColumns {
        public static final String TABLE_NAME = "messages";
        public static final String COLUMN_CONTENT= "message_content";
        public static final String COLUMN_DOWNLOADED = "message_downloaded";
        public static final String COLUMN_FROM= "message_from";
        public static final String COLUMN_TO= "message_to";
        public static final String COLUMN_LAT= "message_lat";
        public static final String COLUMN_LONG= "message_long";
        public static final String COLUMN_TIME_STAMP= "message_time_stamp";
        public static final String COLUMN_READ_RADIUS= "message_read_radius";


    }
}
