package com.shenkar.aroundme.data;

import android.provider.BaseColumns;

/**
 * name of all the column in the conversation table
 */
public class ConversationsDBContract {
    public static final class ConversationEntry implements BaseColumns {
        public static final String TABLE_NAME = "conversations";
        public static final String COLUMN_USER = "conversation_user";
        public static final String COLUMN_FRIEND = "conversation_friend";
        public static final String COLUMN_UNREAD = "conversation_unread";
        public static final String COLUMN_LAST_MODIFIED = "conversation_last_seen";
        public static final String COLUMN_IMAGE = "conversation_image";
        public static final String COLUMN_DISPLAY_NAME= "conversation_display_name";

    }
}
