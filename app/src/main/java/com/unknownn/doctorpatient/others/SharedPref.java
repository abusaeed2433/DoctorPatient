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
        User user = getMyProfile();
        return user.isAmIDoctor();
    }

    public User getMyProfile(){
        if(activity == null) return null;
        SharedPreferences spProfile = activity.getSharedPreferences("sp_profile",MODE_PRIVATE);

        final String uid = spProfile.getString("uid",null);
        final int intId = spProfile.getInt("int_id",-1);
        final String name = spProfile.getString("name",null);
        final String imageUrl = spProfile.getString("image_url", null);
        final String gender = spProfile.getString("gender", null);
        final boolean isDoctor = spProfile.getBoolean("is_doctor", false);

        if( !isDoctor ) {
            final int age = spProfile.getInt("age",-1);
            final int weight = spProfile.getInt("weight",-1);
            final int heightFt = spProfile.getInt("height_ft", -1);
            final int heightIn = spProfile.getInt("height_in", -1);
            final String desc = spProfile.getString("desc", null);


            return new Patient(uid, intId, name, age, weight, gender, heightFt, heightIn, desc,imageUrl);
        }
        else{
            final String speciality = spProfile.getString("speciality", null);
            final int experienceInMonth = spProfile.getInt("experience_in_month",-1);

            return new Doctor(uid,intId,name, gender, imageUrl,speciality, experienceInMonth);
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
        editor.putBoolean("is_doctor", user.isAmIDoctor());
        editor.putString("gender", user.getGender());

        if( user instanceof Patient ) {
            final Patient patient = (Patient)user;
            editor.putInt("age", patient.getAge());
            editor.putInt("weight", patient.getWeight());
            editor.putInt("height_ft", patient.getHeightFt());
            editor.putInt("height_in", patient.getHeightIn());
            editor.putString("desc", patient.getDesc());
        }
        else{
            final Doctor doctor = (Doctor) user;
            editor.putString("speciality", doctor.getSpeciality());
            editor.putInt("experience_in_month", doctor.getExperienceInMonth());
        }

        editor.apply();
    }

    public int getMyIntegerId(){
        return getMyProfile().getIntId();
    }
    public void clearAll(){
        sp = activity.getSharedPreferences("sp",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();

        SharedPreferences spProfile = activity.getSharedPreferences("sp_profile",MODE_PRIVATE);
        SharedPreferences.Editor editor1 = spProfile.edit();
        editor1.clear();
        editor1.apply();
    }
    public void saveIsSignedIn(boolean val){
        if(activity == null) return;

        sp = activity.getSharedPreferences("sp",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("is_signed_in",val);
        editor.apply();

    }
}
