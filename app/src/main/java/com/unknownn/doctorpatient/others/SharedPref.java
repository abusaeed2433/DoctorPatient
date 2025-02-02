package com.unknownn.doctorpatient.others;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.SharedPreferences;

public class SharedPref {
    private final Activity activity;
    private SharedPreferences sp;

    public SharedPref(Activity activity) {
        this.activity = activity;
        sp = activity.getSharedPreferences("sp",MODE_PRIVATE);
    }

    public boolean isSignedIn(){
        if(sp == null){ sp = activity.getSharedPreferences("sp",MODE_PRIVATE); }
        return sp.getBoolean("is_signed_in",false);
    }

    public boolean amIDoctor(){
        if(sp == null){ sp = activity.getSharedPreferences("sp",MODE_PRIVATE); }
        return sp.getBoolean("am_i_doctor",false);
    }

    /**
     * will check only if you are patient
     * @param amIDoctor to be checked
     * @return true if info needs to be added else false
     */
    public boolean wasMyInfoAdded(boolean amIDoctor){
        if(amIDoctor) return true;

        if(activity == null) return true;

        return sp.getBoolean("was_my_info_added",true);
    }

    public User getMyProfile(){
        if(activity == null) return null;
        SharedPreferences spProfile = activity.getSharedPreferences("sp_profile",MODE_PRIVATE);

        final String uid = spProfile.getString("uid",null);
        final int intId = spProfile.getInt("int_id",-1);
        final String name = spProfile.getString("name",null);
        final String imageUrl = spProfile.getString("image_url", null);
        final boolean isDoctor = spProfile.getBoolean("is_doctor", false);

        if( isDoctor ) {
            final int age = spProfile.getInt("age",-1);
            final int weight = spProfile.getInt("weight",-1);
            final int heightFt = spProfile.getInt("height_ft", -1);
            final int heightIn = spProfile.getInt("height_in", -1);
            final String desc = spProfile.getString("desc", null);
            final String gender = spProfile.getString("gender", null);

            return new Patient(uid, intId, name, age, weight, gender, heightFt, heightIn, desc,imageUrl);
        }
        else{
            final String speciality = spProfile.getString("speciality", null);
            final int experienceInMonth = spProfile.getInt("experience_in_month",-1);

            return new Doctor(uid,intId,name,imageUrl,speciality, experienceInMonth);
        }
    }

    public void saveMyProfile(User user){
        if(activity == null) return;
        SharedPreferences spProfile = activity.getSharedPreferences("sp_profile",MODE_PRIVATE);
        SharedPreferences.Editor editor = spProfile.edit();

        editor.putString("uid",user.getUid());
        editor.putInt("int_id",user.getIntId());
        editor.putString("name",user.getName());
        editor.putString("image_url", user.getImageUrl());
        editor.putBoolean("is_doctor", user.isDoctor());

        if( user instanceof Patient ) {
            final Patient patient = (Patient)user;
            editor.putInt("age", patient.getAge());
            editor.putInt("weight", patient.getWeight());
            editor.putInt("height_ft", patient.getHeightFt());
            editor.putInt("height_in", patient.getHeightIn());
            editor.putString("desc", patient.getDesc());
            editor.putString("gender", patient.getGender());
        }
        else{
            final Doctor doctor = (Doctor) user;
            editor.putString("speciality", doctor.getSpeciality());
            editor.putInt("experience_in_month", doctor.getExperienceInMonth());
        }

        editor.apply();
    }

    public int getMyIntegerId(){
        SharedPreferences spProfile = activity.getSharedPreferences("sp_profile",MODE_PRIVATE);
        return spProfile.getInt("int_id",-1);
    }

    public void saveWasMyInfoAdded(boolean hasInfoAdded){
        if(activity == null) return;
        if(sp == null) sp = activity.getSharedPreferences("sp", MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("was_my_info_added",hasInfoAdded);
        editor.apply();

    }

    public void saveAmIDoctor(boolean val){
        if(activity == null) return;

        sp = activity.getSharedPreferences("sp",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("am_i_doctor",val);
        editor.apply();
    }

    public String getMyUid(){
        if(activity == null) return "not_found";

        return sp.getString("my_id","not_found");
    }

    public void saveMyUid(String uid){
        if(activity == null) return;

        sp = activity.getSharedPreferences("sp",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("my_id",uid);
        editor.apply();
    }

    public void saveIsSignedIn(boolean val){
        if(activity == null) return;

        sp = activity.getSharedPreferences("sp",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("is_signed_in",val);
        editor.apply();

    }
}
