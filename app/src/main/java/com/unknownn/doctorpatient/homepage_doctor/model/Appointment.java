package com.unknownn.doctorpatient.homepage_doctor.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Appointment implements Comparable<Appointment>, Serializable {
    // common
    @PropertyName("appointment_id") // doctorId_patientId_date_time
    private String appointmentId;
    @PropertyName("date")
    private String date; // dd/MM/yyyy
    @PropertyName("time")
    private String time; // hh:mma

    // doctor
    @PropertyName("doctor_uid")
    private String doctorUid;
    @PropertyName("doctor_name")
    private String doctorName;
    @PropertyName("doctor_speciality")
    private String doctorSpeciality;

    @PropertyName("doctor_image")
    private String doctorImage;

    // patient
    @PropertyName("patient_uid")
    private String patientUid;
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

    public Appointment(String appointmentId, String date, String time, String doctorUid, String doctorName, String doctorSpeciality, String doctorImage, String patientUid, String patientName, String patientDescription, String patientImage, boolean isConfirmed) {
        this.appointmentId = appointmentId;
        this.date = date;
        this.time = time;
        this.doctorUid = doctorUid;
        this.doctorName = doctorName;
        this.doctorSpeciality = doctorSpeciality;
        this.doctorImage = doctorImage;
        this.patientUid = patientUid;
        this.patientName = patientName;
        this.patientDescription = patientDescription;
        this.patientImage = patientImage;
        this.isConfirmed = isConfirmed;
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
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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

    private transient String dateDD = null;
    private transient String dateMmDayName = null;

    private transient long timestamp = 0L;
    @Exclude
    public String getDayDD(){
        if(dateDD != null) return dateDD;

        try{
            final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate localDate = LocalDate.from( dateTimeFormatter.parse(date) );

            {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd");
                dateDD = formatter.format(localDate);
            }
            {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM, E");
                dateMmDayName = formatter.format(localDate);
            }
            {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mma");
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
        if( !time.equals(item.time) || !date.equals(item.date)) return false;

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
