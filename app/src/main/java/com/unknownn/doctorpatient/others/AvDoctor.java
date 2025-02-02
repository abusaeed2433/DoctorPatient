package com.unknownn.doctorpatient.others;

import static com.unknownn.doctorpatient.homepage_doctor.view.DoctorHomePage.UPDATE_TIME_INTERVAL_MAX;

public class AvDoctor extends Doctor{

    private long lastOnlineTime;
    private boolean inCall;

    public AvDoctor() {
    }

    public AvDoctor(String uid, int intId, String name, String gender, String imageUrl, String speciality, int experienceInMonth, long lastOnlineTime, boolean inCall) {
        super(uid, intId, name,gender,imageUrl , speciality, experienceInMonth);
        this.lastOnlineTime = lastOnlineTime;
        this.inCall = inCall;
    }

    public long getLastOnlineTime() {
        return lastOnlineTime;
    }

    public void setLastOnlineTime(long lastOnlineTime) {
        this.lastOnlineTime = lastOnlineTime;
    }

    public boolean isInCall() {
        return inCall;
    }
    public boolean isTimeInvalid(){
        long curTime = System.currentTimeMillis();

        long dif = Math.abs(curTime - lastOnlineTime);
        return dif > UPDATE_TIME_INTERVAL_MAX;
    }

    public String getSpecialityMessage(){
        return "Specialist in "+getSpeciality();
    }

    public void setInCall(boolean inCall) {
        this.inCall = inCall;
    }

    public boolean fullySame(final AvDoctor item){
        return super.getSpeciality().equals(item.getSpeciality()) && getImageUrl().equals(item.getImageUrl());
    }
}
