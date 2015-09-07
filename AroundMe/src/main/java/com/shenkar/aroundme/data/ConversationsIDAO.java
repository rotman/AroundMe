package com.shenkar.aroundme.data;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.Conversation;
import java.sql.SQLException;
import java.util.List;

/**
 * interface for the conversation DAO
 */
public interface ConversationsIDAO {
    void open() throws SQLException;
    void close();
    List<Conversation> getConversations();
    Conversation addConversation(Conversation conversation);
    void removeConversation(Conversation conversation);
    int updateUnreadMessages(String friendMail, boolean isNew);
    void updateLastModified(String friendMail);
    Conversation getConversation(long cId);
    void clearTable();
}
