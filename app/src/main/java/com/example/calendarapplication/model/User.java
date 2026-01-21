package com.example.calendarapplication.model;

public class User {
    private long id;
    private String phone;
    private String password;
    private String salt;
    private String nickname;
    private String avatar;
    private String gender;
    private int age;

    public User() {
    }

    public User(String phone, String password) {
        this.phone = phone;
        this.password = password;
        this.salt = "";
        this.nickname = "我是一个用户昵称";
        this.avatar = "";
        this.gender = "";
        this.age = 0;
    }

    public User(String phone, String password, String nickname, String avatar, String gender, int age) {
        this.phone = phone;
        this.password = password;
        this.salt = "";
        this.nickname = nickname;
        this.avatar = avatar;
        this.gender = gender;
        this.age = age;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}