package com.shenkar.aroundme.data;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.Message;
import java.sql.SQLException;
import java.util.List;

/**
 * interface of the messages DAO
 */
public interface MessagesIDAO {
    void open() throws SQLException;
    void close();
    List<Message> getMessage(String friendMail);
    long addMessage(Message message);
    void removeConversationMessages(String userMail);
    Message getOneMessage(long id);
    void clearAllMessages();
}
