package com.unknownn.doctorpatient.fragments.patient_chat.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.Objects;

public class EachChat implements Serializable {
    private String id;

    private String doctorUid;
    private String patientUid;
    private String doctorName;
    private String patientName;
    private String doctorPic;
    private String patientPic;
    private String lastMessage;
    private long lastMessageTime;

    public EachChat() {
    }

    public EachChat(String id, String doctorUid, String patientUid, String doctorName, String patientName, String doctorPic, String patientPic, String lastMessage, long lastMessageTime) {
        this.id = id;
        this.doctorUid = doctorUid;
        this.patientUid = patientUid;
        this.doctorName = doctorName;
        this.patientName = patientName;
        this.doctorPic = doctorPic;
        this.patientPic = patientPic;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }

    public String getDoctorUid() {
        return doctorUid;
    }

    public void setDoctorUid(String doctorUid) {
        this.doctorUid = doctorUid;
    }

    public String getPatientUid() {
        return patientUid;
    }

    public void setPatientUid(String patientUid) {
        this.patientUid = patientUid;
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

        return Objects.equals(doctorName,item.doctorName) &&
                Objects.equals(patientName,item.patientName) &&
                Objects.equals(doctorPic,item.doctorPic) &&
                Objects.equals(patientPic,item.patientPic) &&
                Objects.equals(lastMessage,item.lastMessage) &&
                Objects.equals(lastMessageTime,item.lastMessageTime);
    }

}
