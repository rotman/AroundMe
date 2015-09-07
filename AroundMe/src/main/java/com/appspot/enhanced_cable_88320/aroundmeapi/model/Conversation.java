package com.appspot.enhanced_cable_88320.aroundmeapi.model;

/**
 * class to define the conversation
 */
public class Conversation {
    /**
     * user1 is my value in the convesation
     */
    private User user1;
    /**
     * user2 is the other person in the conversation
     */
    private UserAroundMe user2;
    /**
     * a counter of unread messages that is incremented on each message received
     */
    private int unreadMessages;
    /**
     * image url of the conversation will be the image url of user2
     */
    private String imageUrl;
    /**
     * the current time of the last sent or received message
     */
    private long lastModified;
    /**
     * the unique id of the conversation
     */
    private long id;
    /**
     * the title of the conversation will be the display name of user2
     */
    private String displayName;


    /**
     * get from message
     * @return user1
     */
    public User getFrom() {
        return user1;
    }

    /**
     * set user 1
     * @param user1Val
     */
    public void setUser1(User user1Val) {
        this.user1 = user1Val;
    }

    /**
     * get the user1
     * @return user1
     */
    public User getUser1() {
        return user1;
    }

    /**
     * set user 2
     * @param user2Val
     */
    public void setUser2(UserAroundMe user2Val) {
        this.user2 = user2Val;
    }

    /**
     * get user2
     * @return user2
     */
    public UserAroundMe getUser2() {return user2; }

    /**
     * get the id of the conversaiton
     * @return id
     */
    public long getId() {
        return id;
    }

    /**
     * set the id of the conversation
     * @param id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * get the number of unreadMessage in a conversation
     * @return unreadMessage
     */
    public int getUnreadMessages() {
        return unreadMessages;
    }

    /**
     * set the number of unreadMessage in a conversation
     * @param unreadMessages
     */
    public void setUnreadMessages(int unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    /**
     * get the time that the conversation was last modified
     * @return lastModified
     */
    public long getLastModified() {
        return lastModified;
    }

    /**
     * set the time that the conversation was last modified
     * @param lastModified
     */
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * get the image url of the conversation
     * @return imageUrl
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * set the image url of the conversation
     * @param imageUrl
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * get the display name of the conversation
     * @return displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * set the display name of the conversation
     * @param displayName
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
