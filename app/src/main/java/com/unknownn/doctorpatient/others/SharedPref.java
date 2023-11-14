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

    public Patient getMyProfile(){
        if(activity == null) return null;
        SharedPreferences spProfile = activity.getSharedPreferences("sp_profile",MODE_PRIVATE);

        String uid = spProfile.getString("uid",""),
                name = spProfile.getString("name",""),
                age = spProfile.getString("age",""),
                weight = spProfile.getString("weight",""),
                heightFt = spProfile.getString("heightFt",""),
                heightIn = spProfile.getString("heightIn",""),
                desc = spProfile.getString("desc",""),
                gender = spProfile.getString("gender","");

        return new Patient(uid,name,age,weight,gender,heightFt,heightIn,desc);
    }

    public void saveMyProfile(final Patient mine){
        if(activity == null) return;
        SharedPreferences spProfile = activity.getSharedPreferences("sp_profile",MODE_PRIVATE);
        SharedPreferences.Editor editor = spProfile.edit();

        editor.putString("uid",mine.getUid());
        editor.putString("name",mine.getName());
        editor.putString("age", mine.getAge());
        editor.putString("weight", mine.getWeight());
        editor.putString("heightFt", mine.getHeightFt());
        editor.putString("heightIn", mine.getHeightIn());
        editor.putString("desc", mine.getDesc());
        editor.putString("gender", mine.getGender());

        editor.apply();
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

    public int getMyId(){
        if(activity == null) return 0;

        return sp.getInt("my_id",0);
    }

    public void saveMyId(int id){
        if(activity == null) return;

        sp = activity.getSharedPreferences("sp",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("my_id",id);
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
