package com.unknownn.doctorpatient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
            intent = new Intent(StartingPage.this, HomePage.class);
            intent.putExtra("is_doctor",sp.amIDoctor());
        }
        else{
            intent = new Intent(StartingPage.this,CreateAccount.class);
        }
        startActivity(intent);

    }

    private void hideNavigationAndStatusBar() {
        View decorView = getWindow().getDecorView();
        if(decorView!=null){
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY );
        }
    }
}