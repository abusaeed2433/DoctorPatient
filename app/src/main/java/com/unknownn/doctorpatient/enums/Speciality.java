package com.unknownn.doctorpatient.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Speciality {
    ALL("All", "https://i.postimg.cc/L5q2LZ2J/medicine.png"), // medicine
    DERMATOLOGY("Dermatology","https://i.postimg.cc/x1Lj6HHH/dermatology.png"),
    NEUROSURGERY("Neurosurgery","https://i.postimg.cc/FRChYx5t/neurosurgery.png"),
    OPHTHALMOLOGY("Ophthalmology","https://i.postimg.cc/fLRwWrPj/ophtalmology.png"),
    PEDIATRICS("Pediatrics", "https://i.postimg.cc/W4nNvv51/pediatrics.png"),
    VIROLOGY("Virology", "https://i.postimg.cc/0NxkFQd4/virology.png")
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
