package com.unknownn.doctorpatient.homepage_patient.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unknownn.doctorpatient.CreateAccount;
import com.unknownn.doctorpatient.PatientProfile;
import com.unknownn.doctorpatient.R;
import com.unknownn.doctorpatient.VideoActivity;
import com.unknownn.doctorpatient.adapter.PagerAdapter;
import com.unknownn.doctorpatient.databinding.ActivityPatientHomepageBinding;
import com.unknownn.doctorpatient.fragments.patient_appointment.view.FragmentPatientAppointment;
import com.unknownn.doctorpatient.fragments.chat_list.view.FragmentChatList;
import com.unknownn.doctorpatient.fragments.patient_home.view.FragmentPatientHome;
import com.unknownn.doctorpatient.homepage_doctor.model.Appointment;
import com.unknownn.doctorpatient.others.SharedPref;

import java.util.ArrayList;
import java.util.List;

public class PatientHomePage extends AppCompatActivity {

    private boolean forceExit = false, hasDoublePressed = false;
    private SharedPref sp;
    private Dialog mainDialog;
    private TextView tvProgress;
    private String appId = null, token = null, cName = null;
    private int selectedPage = 0;
    private FragmentPatientHome.AppointmentListener listener = null;
    private ActivityPatientHomepageBinding binding = null;
    private Appointment lastAppointment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPatientHomepageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        forceExit = getIntent().getBooleanExtra("force_exit",false);

        startFsAdapter();
        setBnvListener();
        downloadToken();
    }

    private void startFsAdapter(){
        List<Fragment> list = new ArrayList<>();
        list.add(new FragmentPatientHome());
        list.add(new FragmentPatientAppointment());
        list.add(new FragmentChatList());

        FragmentStateAdapter fSAdapter = new PagerAdapter(this,list);
        binding.viewPager.setOffscreenPageLimit(list.size());
        binding.viewPager.setAdapter(fSAdapter);
        binding.viewPager.setUserInputEnabled(false);
    }

    private void setBnvListener(){
        binding.bnv.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if(id == R.id.navHome){
                binding.viewPager.setCurrentItem(0);
                selectedPage = 0;
                return true;
            }
            if(id == R.id.navAppointment){
                binding.viewPager.setCurrentItem(1);
                selectedPage = 1;
                return true;
            }
            if(id == R.id.navChat){
                binding.viewPager.setCurrentItem(2);
                selectedPage = 2;
                return true;
            }

            return false;
        });

    }


    private void downloadToken(){
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("token/0");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appId = String.valueOf(snapshot.child("appId").getValue());
                token = String.valueOf(snapshot.child("token").getValue());
                cName = String.valueOf(snapshot.child("cName").getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private SharedPref getSp(){
        if(sp == null) {
            sp = new SharedPref(this);
        }
        return sp;
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.bar_profile){
            Intent intent = new Intent(PatientHomePage.this, PatientProfile.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        else if(id == R.id.log_out){
            try {
                new AlertDialog.Builder(PatientHomePage.this)
                        .setTitle("LogOut?")
                        .setMessage("Are you sure you want to log-out")
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> logOutUser())
                        .show();
            }catch (Exception ignored){}
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("deprecation")
    private void logOutUser(){
        getSp().saveIsSignedIn(false);
        getSp().clearAll();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(this,gso);
        client.signOut();
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(PatientHomePage.this, CreateAccount.class);
        intent.putExtra("force_exit",true);
        startActivity(intent);
    }

    //patient
    private void acceptOrRejectStatus(DatabaseReference ref, ValueEventListener listener,boolean isAccepted, String docId){
        if(ref != null && listener != null) {
            ref.removeEventListener(listener);
        }

        dismissMainDialog();
        if(isAccepted){
            String patId = getMyUid();

            if(patId != null) {
                Intent intent = new Intent(PatientHomePage.this, VideoActivity.class);
                intent.putExtra("doctor_uid", docId);
                intent.putExtra("patient_uid", patId);
                intent.putExtra("token", token);
                intent.putExtra("channel_name", cName);
                intent.putExtra("app_id", appId);
                startActivity(intent);
            }
        }
        else{
            showSnackBar("Dismissed");
        }
    }

    private String getMyUid(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) return null;
        return user.getUid();
    }

    private void dismissMainDialog(){
        try {
            if(mainDialog != null) {
                mainDialog.dismiss();
            }
        }catch (Exception ignored){}
    }

    private void changeProgressMessage(String message){
        if(mainDialog != null && tvProgress != null){
            tvProgress.setText(message);
        }
    }

    //patient above
    public void showProgress(String message, String docUid, String myUid) {
        mainDialog = new Dialog(this);
        mainDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mainDialog.setContentView(R.layout.progress_dialog_layout);

        tvProgress = mainDialog.findViewById(R.id.textViewAboutUpload);
        Button buttonExit = mainDialog.findViewById(R.id.button_exit);

        if(message != null) {
            tvProgress.setText(message);
        }

        buttonExit.setOnClickListener(v -> {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                    .child("request").child(docUid).child(myUid);
            ref.removeValue();
            dismissMainDialog();
        });

        Window window = mainDialog.getWindow();
        if(window!=null){
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        mainDialog.setCanceledOnTouchOutside(false);
        mainDialog.setCancelable(false);
        if(!isFinishing()) mainDialog.show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        if(hasDoublePressed) {
            hasDoublePressed = false;
            if(forceExit) {
                finishAffinity();
            }
            else {
                super.onBackPressed();
            }
        }
        else{
            if(selectedPage != 0){
                binding.viewPager.setCurrentItem(0);
                binding.bnv.setSelectedItemId(R.id.navHome);
                selectedPage = 0;
            }
            else {
                showSnackBar("Press again to exit");
                hasDoublePressed = true;
                new Handler(Looper.getMainLooper()).postDelayed(() -> hasDoublePressed = false, 2000);
            }
        }
    }

    private void showSnackBar(String message){
        Snackbar snackbar = Snackbar.make(binding.relativeLayoutHomepage,message,Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public void setAppointmentListener(FragmentPatientHome.AppointmentListener listener){
        this.listener = listener;
        if(lastAppointment != null){
            this.listener.sendCurrentAppointment(lastAppointment);
        }
    }

    public void showAllAppointment(){
        binding.viewPager.setCurrentItem(1);
        binding.bnv.setSelectedItemId(R.id.navAppointment);
        selectedPage = 1;
    }

    public void sendCurrentAppointment(Appointment appointment){
//        appointment = new Appointment(
//                "1",
//                "03/02/2025",
//                "08:45PM",
//                "doc_uid",
//                "Kutta",
//                "Dentist",
//                null,
//                "pid",
//                "cat",
//                "none",
//                null,
//                true
//        );

        if(this.listener == null){
            lastAppointment = appointment;
        }
        else{
            this.listener.sendCurrentAppointment(appointment);
        }
    }

}
