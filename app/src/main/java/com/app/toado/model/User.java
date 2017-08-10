package com.app.toado.model;

import com.google.firebase.database.DataSnapshot;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by RajK on 13-06-2017.
 */

public class User {

    @PrimaryKey
    private String key;

    private String name,phone,gender,dob,profpicurl,userLikes;

    public String getProfpicurl() {
        return profpicurl;
    }

    public void setProfpicurl(String profpicurl) {
        this.profpicurl = profpicurl;
    }

    public User(String name, String phone, String gender, String dob, String profpicurl,String userLikes) {
        this.name = name;
        this.phone = phone;
        this.gender = gender;
        this.dob = dob;
        this.profpicurl = profpicurl;
        this.userLikes=userLikes;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getUserLikes() {
        return userLikes;
    }

    public void setUserLikes(String userLikes) {
        this.userLikes = userLikes;
    }

    public User() {

    }

    public static User parse(DataSnapshot dataSnapshot) throws NullPointerException {
        User usr = new User();
        usr.setDob(dataSnapshot.child("dob").getValue().toString());
        usr.setGender(dataSnapshot.child("gender").getValue().toString());
        usr.setPhone(dataSnapshot.child("phone").getValue().toString());
        usr.setName(dataSnapshot.child("name").getValue().toString());
        usr.setProfpicurl(dataSnapshot.child("profpicurl").getValue().toString());
        usr.setUserLikes(dataSnapshot.child("userLikes").getValue().toString());

//        System.out.println("from user parse function"+usr.getName()+usr.getDob()+usr.getUserLikes());
        return usr;
    }

}
