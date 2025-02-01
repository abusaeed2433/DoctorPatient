package com.unknownn.doctorpatient.others;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.google.firebase.database.PropertyName;

import java.util.HashMap;
import java.util.Map;

public class Patient {
    @PropertyName("uid")
    private String uid;

    @PropertyName("int_id")
    private int intId;

    @PropertyName("name")
    private String name;

    @PropertyName("age")
    private String age;

    @PropertyName("weight")
    private String weight;

    @PropertyName("gender")
    private String gender;

    @PropertyName("height_ft")
    private String heightFt;

    @PropertyName("height_in")
    private String heightIn;

    @PropertyName("disease_desc")
    private String desc;

    @PropertyName("image_url")
    private String imageUrl;

    public Patient() {
        this.uid = "";
        this.intId = -1;
        this.name = "";
        this.age = "";
        this.weight = "";
        this.gender = "";
        this.heightFt = "";
        this.heightIn = "";
        this.desc = "";
        this.imageUrl = "";
    }

    public Patient(String uid, int intId, String name, String age, String weight, String gender, String heightFt, String heightIn, String desc, String imageUrl) {
        this.uid = uid;
        this.intId = intId;
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.gender = gender;
        this.heightFt = heightFt;
        this.heightIn = heightIn;
        this.desc = desc;
        this.imageUrl = imageUrl;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setHeightFt(String heightFt) {
        this.heightFt = heightFt;
    }

    public void setHeightIn(String heightIn) {
        this.heightIn = heightIn;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getIntId() {
        return intId;
    }

    public void setIntId(int intId) {
        this.intId = intId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public Map<String, Object> getSavableMap(){
        final Map<String, Object> map = new HashMap<>();
        map.put("name",name);
        map.put("age",age);
        map.put("weight",weight);
        map.put("heightFt",heightFt);
        map.put("heightIn",heightIn);
        map.put("desc",desc);
        map.put("gender",gender);

        return map;
    }

    public SpannableStringBuilder getFormattedString(){

        try {
            SpannableStringBuilder ans = new SpannableStringBuilder("A patient named " + name);

            int prev = 15;
            MyPair[] pairs = new MyPair[6];

            pairs[0] = new MyPair(prev, ans.length());

            prev = ans.length();
            ans.append(", age: ").append(age);
            pairs[1] = new MyPair(prev + 6, ans.length());


            prev = ans.length();
            ans.append(", weight: ").append(weight).append("kg");
            pairs[2] = new MyPair(prev + 9, ans.length());


            prev = ans.length();
            ans.append(", height: ").append(heightFt).append("ft");
            pairs[3] = new MyPair(prev + 9, ans.length());

            prev = ans.length();
            ans.append(" ").append(heightIn).append("in");
            pairs[4] = new MyPair(prev, ans.length());

            ans.append(" is calling you. ");
            prev = ans.length();

            if (gender.equalsIgnoreCase("female")) ans.append("Her ");
            else ans.append("His ");

            pairs[5] = new MyPair(prev, ans.length());
            ans.append("disease info is given below:\n");

            for (MyPair pair : pairs) {
                final ForegroundColorSpan fcs = new ForegroundColorSpan(Color.MAGENTA);
                final StyleSpan iss = new StyleSpan(android.graphics.Typeface.ITALIC); //Span to make text italic

                ans.setSpan(fcs, pair.a, pair.b, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                ans.setSpan(iss, pair.a, pair.b, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }

            return ans;

        }catch (Exception ignored){
            SpannableStringBuilder ans = new SpannableStringBuilder();
            ans.append("A patient named ").append(name);
            ans.append(", age: ").append(age);
            ans.append(", weight: ").append(weight).append("kg");
            ans.append(", height: ").append(heightFt).append("ft");
            ans.append(" ").append(heightIn).append("in");
            ans.append(" is calling you. ");

            if (gender.equalsIgnoreCase("female")) ans.append("Her ");
            else ans.append("His ");
            ans.append("disease info is given below:\n");

            return ans;
        }
    }

    public String getName() {
        if(name.equalsIgnoreCase("null")) return "";
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        if(age.equalsIgnoreCase("null")) return "";
        return age;
    }

    public String getWeight() {
        if(weight.equalsIgnoreCase("null")) return "";
        return weight;
    }

    public String getGender() {
        if(gender.equalsIgnoreCase("null")) return "";
        return gender;
    }

    public String getHeightFt() {
        if(heightFt.equalsIgnoreCase("null")) return "";
        return heightFt;
    }

    public String getHeightIn() {
        if(heightIn.equalsIgnoreCase("null")) return "0";
        return heightIn;
    }

    public String getDesc() {
        if(desc.equalsIgnoreCase("null")) return "";
        return desc;
    }

}
