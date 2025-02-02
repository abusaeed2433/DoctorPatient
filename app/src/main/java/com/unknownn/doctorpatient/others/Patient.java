package com.unknownn.doctorpatient.others;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

public class Patient extends User{
    @PropertyName("age")
    private int age;

    @PropertyName("weight")
    private int weight;

    @PropertyName("height_ft")
    private int heightFt;

    @PropertyName("height_in")
    private int heightIn;

    @PropertyName("disease_desc")
    private String desc;

    public Patient() {
        super();
        this.age = 0;
        this.weight = 0;
        this.heightFt = 0;
        this.heightIn = 0;
        this.desc = "";
    }

    public Patient(String uid, int intId, String name, int age, int weight, String gender, int heightFt, int heightIn, String desc, String imageUrl) {
        super(uid,intId,name,imageUrl,false,gender);

        this.age = age;
        this.weight = weight;
        this.heightFt = heightFt;
        this.heightIn = heightIn;
        this.desc = desc;
    }

    public int getHeightFt() {
        return heightFt;
    }

    public void setHeightFt(int heightFt) {
        this.heightFt = heightFt;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getHeightIn() {
        return heightIn;
    }

    public void setHeightIn(int heightIn) {
        this.heightIn = heightIn;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Exclude
    public SpannableStringBuilder getFormattedString(){

        try {
            SpannableStringBuilder ans = new SpannableStringBuilder("A patient named " + super.getName());

            int prev = 15;
            MyPair[] pairs = new MyPair[6];

            pairs[0] = new MyPair(prev, ans.length());

            prev = ans.length();
            ans.append(", age: ").append(age+"");
            pairs[1] = new MyPair(prev + 6, ans.length());


            prev = ans.length();
            ans.append(", weight: ").append(weight+"kg");
            pairs[2] = new MyPair(prev + 9, ans.length());


            prev = ans.length();
            ans.append(", height: ").append(heightFt+"ft");
            pairs[3] = new MyPair(prev + 9, ans.length());

            prev = ans.length();
            ans.append(" ").append(heightIn+"in");
            pairs[4] = new MyPair(prev, ans.length());

            ans.append(" is calling you. ");
            prev = ans.length();

            if (super.getGender().equalsIgnoreCase("female")) ans.append("Her ");
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
            ans.append("A patient named ").append(super.getName());
            ans.append(", age: "+age);
            ans.append(", weight: "+weight).append("kg");
            ans.append(", height: "+heightFt).append("ft");
            ans.append(" "+heightIn).append("in");
            ans.append(" is calling you. ");

            if (super.getGender().equalsIgnoreCase("female")) ans.append("Her ");
            else ans.append("His ");
            ans.append("disease info is given below:\n");

            return ans;
        }
    }

}
