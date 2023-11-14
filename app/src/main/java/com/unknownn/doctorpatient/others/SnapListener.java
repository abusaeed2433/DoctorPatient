package com.unknownn.doctorpatient.others;

import com.google.firebase.database.DataSnapshot;

public interface SnapListener{
    void onSnapFound(DataSnapshot ds);
}