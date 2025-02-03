package com.unknownn.doctorpatient.others;

import android.util.Log;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;
import com.unknownn.doctorpatient.enums.Speciality;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Doctor extends User implements Serializable {
    @PropertyName("description")
    private String description;
    @PropertyName("experience_in_month")
    private int experienceInMonth;
    @PropertyName("specialities")
    private String specialities;

    public Doctor() {
        super();
    }

    public Doctor(String uid, int intId, String name, String gender, String imageUrl, String description, int experienceInMonth, String specialities) {
        super(uid,intId,name,imageUrl,true, gender);
        this.description = description;
        this.specialities = specialities;
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
        map.put("description",description);
        map.put("speciality", specialities);
        map.put("experienceInMonth", getExperienceInMonth());

        return map;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSpecialities() {
        return specialities;
    }

    public void setSpecialities(String specialities) {
        this.specialities = specialities;
    }

    public int getExperienceInMonth() {
        return experienceInMonth;
    }

    public void setExperienceInMonth(int experienceInMonth) {
        this.experienceInMonth = experienceInMonth;
    }

    @Exclude
    public String getSpecialityMessage(){
        return specialities;
    }

    @Exclude
    public List<Speciality> getAllSpecialities(){
        String[] words = specialities.split("[\\s,]+");
        final List<Speciality> list = new ArrayList<>();

        for(String word : words){
            try{
                list.add( Speciality.valueOf(word.toUpperCase()) );
            }catch (Exception e){
                Log.d("Error", "getAllSpecialities: "+e.getMessage());
            }
        }
        return list;
    }

}
