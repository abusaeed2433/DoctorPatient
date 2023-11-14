package com.unknownn.doctorpatient.others;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import java.util.HashMap;
import java.util.Map;

public class Patient {
    private String uid, name;
    private final String age, weight, gender, heightFt, heightIn, desc;

    public Patient(String uid,String name, String age, String weight, String gender, String heightFt, String heightIn, String desc) {
        this.uid = uid;
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.gender = gender;
        this.heightFt = heightFt;
        this.heightIn = heightIn;
        this.desc = desc;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public Map<String, Object> getSavableMap(){
        Map<String, Object> map = new HashMap<>();
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
