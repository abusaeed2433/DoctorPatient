package com.unknownn.doctorpatient.homepage_doctor.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.unknownn.doctorpatient.CreateAccount;
import com.unknownn.doctorpatient.DoctorProfile;
import com.unknownn.doctorpatient.R;
import com.unknownn.doctorpatient.databinding.ActivityHomepageBinding;
import com.unknownn.doctorpatient.homepage_doctor.model.Appointment;
import com.unknownn.doctorpatient.others.Doctor;
import com.unknownn.doctorpatient.others.SharedPref;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DoctorHomePage extends AppCompatActivity {

    private static final int UPDATE_TIME_INTERVAL = 10000;
    public static final int UPDATE_TIME_INTERVAL_MAX = 12000;

    private boolean forceExit = false, hasDoublePressed = false;
    private SharedPref sp;
    private final Handler mHandler = new Handler();
    private Runnable runnable = null;
    private boolean inPauseState = false;
    private String appointmentType = "All";
    private ActivityHomepageBinding binding = null;
    private AppointmentAdapter appointmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomepageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        forceExit = getIntent().getBooleanExtra("force_exit",false);

        startAllAdapter();
        setClickListener();
        updateMyStatus();
        downloadAppointments();
    }

    private void setClickListener(){
        binding.buttonExit.setOnClickListener(v -> new AlertDialog.Builder(DoctorHomePage.this)
                .setTitle("Exit?")
                .setMessage("Are you sure you want to exit?")
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel,null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> exitUser())
                .show());
    }

    private void downloadAppointments(){
        final String myUserId = getSp().getMyProfile().getUid();

        Query query = FirebaseDatabase.getInstance().getReference("appointment")
                .orderByChild("patientUid").equalTo(myUserId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Appointment> list = new ArrayList<>();
                for(DataSnapshot ds : snapshot.getChildren()){
                    Appointment appointment = ds.getValue(Appointment.class);
                    if(appointment == null) continue;

                    list.add(appointment);
                }

                updateAdapter(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateAdapter(new ArrayList<>());
            }
        });
    }

    private void updateAdapter(List<Appointment> list){
        binding.pbHomepage.setVisibility(View.GONE);

        if(list.isEmpty()){
            binding.tvMessage.setText( getString(R.string.ph_string_only, "No appointment available.\nWe will let you know as soon as any patient contact") );
            binding.tvMessage.setVisibility(View.VISIBLE);
        }
        else{
            binding.tvMessage.setVisibility(View.INVISIBLE);
        }

        appointmentAdapter.submitList(list);
    }

    private void exitUser(){
        if(runnable != null){
            mHandler.removeCallbacks(runnable);
        }

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                    .child("available/doctor").child(uid).child("lastOnline");
            ref.setValue(0);
        }
        finishAffinity();
    }

    private void startAllAdapter(){

        final ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.appointment_type,
                R.layout.simple_spinner_item
        );
        binding.spinnerAppointmentType.setAdapter(arrayAdapter);
        binding.spinnerAppointmentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                appointmentType = (position == 0) ? "All" : (position == 1) ? "Active" : "Pending";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final DiffUtil.ItemCallback<Appointment> diffCallback = new DiffUtil.ItemCallback<Appointment>() {
            @Override
            public boolean areItemsTheSame(@NonNull Appointment oldItem, @NonNull Appointment newItem) {
                return oldItem.getAppointmentId().equals(newItem.getAppointmentId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Appointment oldItem, @NonNull Appointment newItem) {
                return oldItem.conditionalEqual(newItem, false);
            }
        };
        appointmentAdapter = new AppointmentAdapter(this, false, diffCallback, this::openDetailsPage);
        binding.recyclerView.setAdapter(appointmentAdapter);
    }

    private SharedPref getSp(){
        if(sp == null) {
            sp = new SharedPref(this);
        }
        return sp;
    }

    private void openDetailsPage(Appointment item){

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
            Intent intent = new Intent(DoctorHomePage.this, DoctorProfile.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        else if(id == R.id.log_out){
            try {
                new AlertDialog.Builder(DoctorHomePage.this)
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

        Intent intent = new Intent(DoctorHomePage.this, CreateAccount.class);
        intent.putExtra("force_exit",true);
        startActivity(intent);
    }

    private void updateMyStatus(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            showSnackBar(getString(R.string.something_went_wrong));
            binding.tvMessage.setText(R.string.restart_app);
            binding.pbHomepage.setVisibility(View.GONE);
            return;
        }

        final String uid = user.getUid();
        downloadMyData(uid, mine -> {
            if(mine == null){
                binding.tvMessage.setText(R.string.failed_to_fetch_data);
                binding.pbHomepage.setVisibility(View.GONE);
            }
            else{
                Map<String,Object> map = mine.getMap();
                map.put("lastOnline", ServerValue.TIMESTAMP);
                map.put("inCall", false);

                final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("available/doctor").child(uid);
                ref.updateChildren(map).addOnSuccessListener(unused -> startRunningLoop(uid));
            }
        });
    }

    private void startRunningLoop(@NonNull String uid){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("available/doctor").child(uid);

        final int[] color = new int[]{
                getColor(R.color.text_color_1),
                getColor(R.color.text_color_2),
                getColor(R.color.text_color_3),
                getColor(R.color.text_color_4),
                getColor(R.color.text_color_5),
                getColor(R.color.text_color_6),
                getColor(R.color.text_color_7)
        };
        binding.tvMessage.setTextColor(color[0]);
        AtomicInteger ind = new AtomicInteger(1);

        runnable = new Runnable() {
            @Override
            public void run() {
                updateTime(ref, isSuccessful -> {
                    binding.tvMessage.setTextColor(color[ind.get() %7]);
                    mHandler.postDelayed(this,UPDATE_TIME_INTERVAL);
                    ind.set((ind.get() + 1) % 7);
                });
            }
        };

        mHandler.postDelayed(runnable,UPDATE_TIME_INTERVAL);
    }

    private void updateTime(DatabaseReference ref, DataListener listener){
        if(inPauseState) { // won't update but loop will run
            listener.onDataSaved(true);
        }
        else{
            Map<String,Object> map = new HashMap<>();
            map.put("lastOnlineTime",ServerValue.TIMESTAMP);
            map.put("inCall",false);

            Doctor doctor = (Doctor)getSp().getMyProfile();
            map.put("speciality",doctor.getDescription());
            map.put("imageUrl",doctor.getImageUrl());

            ref.updateChildren(map).addOnCompleteListener(task -> listener.onDataSaved(task.isSuccessful()));
        }
    }

    private interface DataListener{
        void onDataSaved(boolean isSuccessful);
    }

    private interface DataReadListener{
        void onDataRead(Doctor mine);
    }

    private void downloadMyData(@NonNull String uid, DataReadListener listener){
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ds) {
                final Doctor doctor = ds.getValue(Doctor.class);
                listener.onDataRead(doctor);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onDataRead(null);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mHandler != null && runnable != null) mHandler.removeCallbacks(runnable);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        if(hasDoublePressed) {

            if(mHandler != null && runnable != null) mHandler.removeCallbacks(runnable);

            hasDoublePressed = false;
            if(forceExit) {
                finishAffinity();
            }
            else {
                super.onBackPressed();
            }
        }
        else{
            showSnackBar("Press again to exit");
            hasDoublePressed = true;
            new Handler(Looper.getMainLooper()).postDelayed(() -> hasDoublePressed = false,2000);
        }
    }

    private void showSnackBar(String message){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.cl_homepage),message,Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        inPauseState = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        inPauseState = false;
    }

}
