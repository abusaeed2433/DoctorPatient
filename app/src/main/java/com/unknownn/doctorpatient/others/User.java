package com.unknownn.doctorpatient.others;

import com.google.firebase.database.PropertyName;

public class User {
    @PropertyName("uid")
    private String uid;
    @PropertyName("int_id")
    private int intId;
    @PropertyName("name")
    private String name;
    @PropertyName("image_url")
    private String imageUrl;
    @PropertyName("is_doctor")
    private boolean isDoctor;

    public User() {
    }

    public User(String uid, int intId, String name, String imageUrl, boolean isDoctor) {
        this.uid = uid;
        this.intId = intId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.isDoctor = isDoctor;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getIntId() {
        return intId;
    }

    public void setIntId(int intId) {
        this.intId = intId;
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

    public boolean isDoctor() {
        return isDoctor;
    }

    public void setDoctor(boolean doctor) {
        isDoctor = doctor;
    }
}
