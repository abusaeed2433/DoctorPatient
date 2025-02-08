package com.unknownn.doctorpatient.homepage_doctor.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;
import com.unknownn.doctorpatient.R;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

public class Appointment implements Comparable<Appointment>, Serializable {
    // common
    @PropertyName("appointment_id") // doctorId_patientId_date_time
    private String appointmentId;
    @PropertyName("date")
    private String date; // dd/MM/yyyy
    @PropertyName("time")
    private String time; // hh:mma

    @PropertyName("date_time")
    private String dateTime;// dd/MM/yyyy_hh:mma

    // doctor
    @PropertyName("doctor_uid")
    private String doctorUid;
    @PropertyName("doctor_int_id")
    private int doctorIntId;
    @PropertyName("doctor_name")
    private String doctorName;
    @PropertyName("doctor_speciality")
    private String doctorSpeciality;

    @PropertyName("doctor_image")
    private String doctorImage;

    // patient
    @PropertyName("patient_uid")
    private String patientUid;
    @PropertyName("patient_int_id")
    private int patientIntId;
    @PropertyName("patient_name")
    private String patientName;
    @PropertyName("patient_description")
    private String patientDescription;
    @PropertyName("patient_image")
    private String patientImage;

    @PropertyName("is_confirmed")
    private boolean isConfirmed;

    public Appointment() {
    }

    public Appointment(String appointmentId, String date, String time, String doctorUid, int doctorIntId, String doctorName, String doctorSpeciality, String doctorImage,
                       String patientUid, int patientIntId, String patientName, String patientDescription, String patientImage, boolean isConfirmed) {
        this.appointmentId = appointmentId;
        this.date = date;
        this.time = time;
        this.dateTime = date + "_" + time;
        this.doctorUid = doctorUid;
        this.doctorIntId = doctorIntId;
        this.doctorName = doctorName;
        this.doctorSpeciality = doctorSpeciality;
        this.doctorImage = doctorImage;
        this.patientUid = patientUid;
        this.patientIntId = patientIntId;
        this.patientName = patientName;
        this.patientDescription = patientDescription;
        this.patientImage = patientImage;
        this.isConfirmed = isConfirmed;
    }

    public int getDoctorIntId() {
        return doctorIntId;
    }

    public void setDoctorIntId(int doctorIntId) {
        this.doctorIntId = doctorIntId;
    }

    public int getPatientIntId() {
        return patientIntId;
    }

    public void setPatientIntId(int patientIntId) {
        this.patientIntId = patientIntId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void setConfirmed(boolean confirmed) {
        isConfirmed = confirmed;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
        this.dateTime = date + "_" + time;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
        this.dateTime = date + "_" + time;
    }

    public String getDoctorUid() {
        return doctorUid;
    }

    public void setDoctorUid(String doctorUid) {
        this.doctorUid = doctorUid;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorSpeciality() {
        return doctorSpeciality;
    }

    public void setDoctorSpeciality(String doctorSpeciality) {
        this.doctorSpeciality = doctorSpeciality;
    }

    public String getDoctorImage() {
        return doctorImage;
    }

    public void setDoctorImage(String doctorImage) {
        this.doctorImage = doctorImage;
    }

    public String getPatientUid() {
        return patientUid;
    }

    public void setPatientUid(String patientUid) {
        this.patientUid = patientUid;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientDescription() {
        return patientDescription;
    }

    public void setPatientDescription(String patientDescription) {
        this.patientDescription = patientDescription;
    }

    public String getPatientImage() {
        return patientImage;
    }

    public void setPatientImage(String patientImage) {
        this.patientImage = patientImage;
    }

    @Exclude
    public String getTypeString(){
        return isConfirmed ? "Active" : "Pending";
    }

    @Exclude
    public int getTypeColorResId(){
        return isConfirmed ? R.color.blue : R.color.red;
    }

    private transient String dateDD = null;
    private transient String dateMmDayName = null;

    private transient long timestamp = 0L;
    @Exclude
    public String getDayDD(){
        if(dateDD != null) return dateDD;

        try{
            final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy",Locale.US);
            LocalDate localDate = LocalDate.from( dateTimeFormatter.parse(date) );

            {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd",Locale.US);
                dateDD = formatter.format(localDate);
            }
            {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM, E",Locale.US);
                dateMmDayName = formatter.format(localDate);
            }
            {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mma", Locale.US);
                LocalDateTime ldt = LocalDateTime.from( formatter.parse( date+" "+time ) );
                timestamp = ldt.toEpochSecond(ZoneOffset.UTC);
            }

        }catch (Exception ignored){}
        return dateDD;
    }


    @Exclude
    public String getDateMmDayName(){
        getDayDD();
        return dateMmDayName;
    }

    @Exclude
    public long getTimestamp(){
        if(timestamp == 0) getDayDD();

        return timestamp;
    }

    @Exclude
    public boolean conditionalEqual(Appointment item, boolean showDoctorData){
        if( !time.equals(item.time) || !date.equals(item.date) || (isConfirmed != item.isConfirmed)) return false;

        if(showDoctorData){
            return Objects.equals(doctorName,item.doctorName) &&
                    Objects.equals(doctorSpeciality,item.doctorSpeciality) &&
                    Objects.equals(doctorImage,item.doctorImage);
        }
        else {
            return Objects.equals(patientName,item.patientName) &&
                    Objects.equals(patientDescription,item.patientDescription) &&
                    Objects.equals(patientImage,item.patientImage);
        }
    }

    @Override
    public int compareTo(Appointment o) {
        return Long.compare(getTimestamp(), o.getTimestamp());
    }
}
