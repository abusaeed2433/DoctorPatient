package com.unknownn.doctorpatient.fragments.patient_home.model;

public class Speciality {

    private String name;
    private String imageUrl;

    public Speciality() {
    }

    public Speciality(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
