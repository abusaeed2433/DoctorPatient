package com.unknownn.doctorpatient.others;

import com.google.firebase.database.PropertyName;

import java.util.Random;

public class Doctor extends User{
    @PropertyName("speciality")
    private String speciality;
    @PropertyName("experience_in_month")
    private int experienceInMonth;

    public Doctor() {
        super();
    }

    public Doctor(String uid, int intId, String name, String imageUrl, String speciality, int experienceInMonth) {
        super(uid,intId,name,imageUrl,true);
        this.speciality = speciality;
        this.experienceInMonth = experienceInMonth;
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

    public boolean fullySame(final Doctor item){
        return speciality.equals(item.speciality) && experienceInMonth == item.experienceInMonth;
    }

}
