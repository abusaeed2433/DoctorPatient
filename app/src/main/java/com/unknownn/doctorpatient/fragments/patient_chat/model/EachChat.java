package com.unknownn.doctorpatient.fragments.patient_chat.model;

import com.google.firebase.database.Exclude;

public class EachChat {
    private String id;
    private String doctorName;
    private String patientName;
    private String doctorPic;
    private String patientPic;
    private String lastMessage;
    private long lastMessageTime;

    public EachChat() {
    }

    public EachChat(String id, String doctorName, String patientName, String doctorPic, String patientPic, String lastMessage, long lastMessageTime) {
        this.id = id;
        this.doctorName = doctorName;
        this.patientName = patientName;
        this.doctorPic = doctorPic;
        this.patientPic = patientPic;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorPic() {
        return doctorPic;
    }

    public void setDoctorPic(String doctorPic) {
        this.doctorPic = doctorPic;
    }

    public String getPatientPic() {
        return patientPic;
    }

    public void setPatientPic(String patientPic) {
        this.patientPic = patientPic;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    @Exclude
    public boolean fullyEquals(EachChat item){

        return doctorName.equals(item.doctorName) &&
                patientName.equals(item.patientName) &&
                doctorPic.equals(item.doctorPic) &&
                patientPic.equals(item.patientPic) &&
                lastMessage.equals(item.lastMessage) &&
                lastMessageTime == item.lastMessageTime;
    }

}
