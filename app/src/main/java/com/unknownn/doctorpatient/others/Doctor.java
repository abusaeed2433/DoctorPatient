package com.unknownn.doctorpatient.others;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Doctor extends User{
    @PropertyName("speciality")
    private String speciality;
    @PropertyName("experience_in_month")
    private int experienceInMonth;

    public Doctor() {
        super();
    }

    public Doctor(String uid, int intId, String name, String gender, String imageUrl, String speciality, int experienceInMonth) {
        super(uid,intId,name,imageUrl,true, gender);
        this.speciality = speciality;
        this.experienceInMonth = experienceInMonth;
    }

    @Exclude
    public Map<String,Object> getMap(){
        Map<String,Object> map = new HashMap<>();
        map.put("uid",getUid());
        map.put("intId", getIntId());
        map.put("name", getName());
        map.put("gender",getGender());
        map.put("imageUrl",getImageUrl());
        map.put("speciality", getSpeciality());
        map.put("experienceInMonth", getExperienceInMonth());

        return map;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public int getExperienceInMonth() {
        return experienceInMonth;
    }

    public void setExperienceInMonth(int experienceInMonth) {
        this.experienceInMonth = experienceInMonth;
    }

}
