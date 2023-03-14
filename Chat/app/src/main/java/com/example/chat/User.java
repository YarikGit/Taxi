package com.example.chat;

public class User {

    private String email;
    private String name;
    private String id;
    private String avatar;
    private String key;

    public User() {
    }

    public User(String email, String name, String id, String avatar, String key) {
        this.email = email;
        this.name = name;
        this.id = id;
        this.avatar = avatar;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
