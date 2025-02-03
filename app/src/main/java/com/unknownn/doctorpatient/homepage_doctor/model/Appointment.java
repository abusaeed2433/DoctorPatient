package com.unknownn.doctorpatient.homepage_doctor.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Appointment {
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

    public Appointment() {
    }

    public Appointment(String appointmentId, String date, String time, String doctorUid, String doctorName, String doctorSpeciality, String doctorImage, String patientUid, String patientName, String patientDescription, String patientImage) {
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

    private String dateDD = null;
    private String dateMmDayName = null;
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

        }catch (Exception ignored){}
        return dateDD;
    }

    @Exclude
    public String getDateMmDayName(){
        getDayDD();
        return dateMmDayName;
    }

    @Exclude
    public boolean conditionalEqual(Appointment item, boolean showDoctorData){
        if( !time.equals(item.time) || !date.equals(item.date)) return false;

        if(showDoctorData){
            return doctorName.equals(item.doctorName) && doctorSpeciality.equals(item.doctorSpeciality) && doctorImage.equals(item.doctorImage);
        }
        else {
            return patientName.equals(item.patientName) && patientDescription.equals(item.patientDescription) && patientImage.equals(item.patientImage);
        }
    }

}
