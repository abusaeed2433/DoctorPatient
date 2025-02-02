package com.unknownn.doctorpatient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.unknownn.doctorpatient.homepage_doctor.view.DoctorHomePage;
import com.unknownn.doctorpatient.homepage_patient.view.PatientHomePage;
import com.unknownn.doctorpatient.others.SharedPref;

public class StartingPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        hideNavigationAndStatusBar();


        SharedPref sp = new SharedPref(this);
        Intent intent;

        if(sp.isSignedIn()){
            intent = new Intent(
                    StartingPage.this,
                    sp.amIDoctor() ? DoctorHomePage.class : PatientHomePage.class
            );
        }
        else{
            intent = new Intent(StartingPage.this,CreateAccount.class);
        }
        startActivity(intent);

    }

    private void hideNavigationAndStatusBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
