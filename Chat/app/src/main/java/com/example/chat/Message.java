package com.example.chat;

public class Message {
    private String text;
    private String name;
    private String sender;
    private String recipient;
    private String imageUrl;
    private boolean isMine;
    private String recipientAvatar;
    private String key;
    private boolean isMessageRead;

    public Message() {
    }

    public Message(String text, String name, String imageUrl, String sender, String recipient, boolean isMine, String recipientAvatar, String key, boolean isMessageRead) {
        this.text = text;
        this.name = name;
        this.imageUrl = imageUrl;
        this.sender = sender;
        this.recipient = recipient;
        this.isMine = isMine;
        this.recipientAvatar = recipientAvatar;
        this.key = key;
        this.isMessageRead = isMessageRead;
    }

    public boolean isMessageRead() {
        return isMessageRead;
    }

    public void setMessageRead(boolean messageRead) {
        isMessageRead = messageRead;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getRecipientAvatar() {
        return recipientAvatar;
    }

    public void setRecipientAvatar(String recipientAvatar) {
        this.recipientAvatar = recipientAvatar;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
