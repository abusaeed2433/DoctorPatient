package com.unknownn.doctorpatient.others;

import java.util.Random;

public class Doctor {

    private final String name,uid;
    private final int id;

    public Doctor(String name, String uid,int id) {
        this.name = name;
        this.uid = uid;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public boolean fullySame(final Doctor item){
        return name.equals(item.name) && uid.equals(item.uid);
    }

    public int getIntId(){
        return new Random().nextInt(99999);
    }

}
