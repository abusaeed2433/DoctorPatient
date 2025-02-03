package com.unknownn.doctorpatient.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Speciality {
    ALL("All", "https://i.postimg.cc/MKCQtqGB/cardiologist.png"),
    DENTIST("Dentist","https://i.postimg.cc/MKCQtqGB/cardiologist.png"),
    MEDICINE("Medicine","https://i.postimg.cc/ZYD3BrtV/dentist.png"),
    CARDIOLOGIST("Cardiologist","https://i.postimg.cc/YqGF1htk/medicine.png"),
    HUDAI("Hudai", "https://i.postimg.cc/JzjB2vy2/sarcastic.png")
    ;

    public static List<Speciality> getAll(){
        return new ArrayList<>(Arrays.asList(Speciality.values()));
    }

    public final String category;
    public final String imageUrl;

    Speciality(String category, String imageUrl) {
        this.category = category;
        this.imageUrl = imageUrl;
    }
}
