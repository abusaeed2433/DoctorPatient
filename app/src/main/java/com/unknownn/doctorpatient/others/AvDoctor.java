package com.unknownn.doctorpatient.others;

import static com.unknownn.doctorpatient.HomePage.UPDATE_TIME_INTERVAL_MAX;

public class AvDoctor extends Doctor{

    private long lastOnlineTime;
    private boolean inCall;

    public AvDoctor(String name, String uid, int id, long lastOnlineTime, boolean inCall) {
        super(name, uid, id);
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

    public void setInCall(boolean inCall) {
        this.inCall = inCall;
    }
}
