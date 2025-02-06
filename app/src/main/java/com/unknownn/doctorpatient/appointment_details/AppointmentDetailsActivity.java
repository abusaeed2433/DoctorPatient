package com.unknownn.doctorpatient.appointment_details;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unknownn.doctorpatient.R;
import com.unknownn.doctorpatient.databinding.ActivityAppointmentDetailsBinding;
import com.unknownn.doctorpatient.homepage_doctor.model.Appointment;
import com.unknownn.doctorpatient.homepage_doctor.view.DoctorHomePage;
import com.unknownn.doctorpatient.others.Doctor;
import com.unknownn.doctorpatient.others.Patient;
import com.unknownn.doctorpatient.others.SharedPref;
import com.unknownn.doctorpatient.others.User;

import java.util.Timer;
import java.util.TimerTask;

public class AppointmentDetailsActivity extends AppCompatActivity {

    private long lastTimeFromDatabase = 0L;
    private ActivityAppointmentDetailsBinding binding = null;
    private final Timer timer = new Timer();
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppointmentDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final Appointment appointment = (Appointment) getIntent().getSerializableExtra("appointment");
        final boolean amIDoctor = new SharedPref(this).getMyProfile().isAmIDoctor();
        downloadData(appointment, amIDoctor);
        setClickListener(appointment, amIDoctor);

        if(appointment != null) {
            downloadDoctorStatus(amIDoctor, appointment.getDoctorUid());
        }
    }


    private void downloadDoctorStatus(boolean amIDoctor, String doctorUid){
        if(amIDoctor){
            binding.llOnlineStatus.setVisibility(View.GONE);
            return;
        }

        // patient seeing doctor details
        binding.llOnlineStatus.setVisibility(View.VISIBLE);
        repeatAtInterval();

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("available/doctor").child(doctorUid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()) return;

                Long temp = snapshot.child("lastOnlineTime").getValue(Long.class);
                if(temp == null) return;

                lastTimeFromDatabase = temp;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void repeatAtInterval(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                final long curTime = System.currentTimeMillis();

                mHandler.post(() -> {
                    boolean isActive = (curTime - lastTimeFromDatabase) < DoctorHomePage.UPDATE_TIME_INTERVAL;

                    binding.tvAvailableStatus.setText(
                            isActive ? getString(R.string.online) : getString(R.string.offline)
                    );
                    binding.tvAvailableStatus.setTextColor(
                            getResources().getColor(
                                    isActive ? R.color.blue : R.color.red,
                                    null
                            )
                    );

                });

            }
        };

        timer.schedule(timerTask, 0, DoctorHomePage.UPDATE_TIME_INTERVAL);
    }

    private void downloadData(Appointment appointment, boolean amIDoctor){
        final String uid = amIDoctor ? appointment.getPatientUid() : appointment.getDoctorUid();
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    showData(null);
                    return;
                }

                if(amIDoctor){
                    showData( snapshot.getValue(Patient.class) );
                }
                else{
                    showData( snapshot.getValue(Doctor.class) );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showData(null);
            }
        });
    }

    private void reloadButton(Appointment appointment, boolean amIDoctor){
        if(appointment.isConfirmed() || !amIDoctor){
            binding.buttonAccept.setVisibility(View.GONE);
        }
        binding.llButtonHolder.setVisibility(View.VISIBLE);
    }

    private void setClickListener(Appointment appointment, boolean amIDoctor){
        reloadButton(appointment, amIDoctor);
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("appointment").child(appointment.getAppointmentId());

        binding.buttonAccept.setOnClickListener(v ->
                ref.child("confirmed").setValue(true)
                        .addOnSuccessListener(unused -> {
                            appointment.setConfirmed(true);
                            reloadButton(appointment, amIDoctor);
                        })
        );

        binding.buttonDelete.setOnClickListener(v -> ref.removeValue().addOnSuccessListener(unused -> showSnackBar("Deleted successfully")));
    }

    private void showData(User user){
        if(user == null){
            showSnackBar(getString(R.string.something_went_wrong));
            return;
        }

        Patient patient = null;
        Doctor doctor = null;
        if(user instanceof Patient) patient = (Patient) user;
        else doctor = (Doctor) user;

        final boolean amIDoctor = (doctor != null);

        binding.tvAboutWhom.setText( getString(R.string.about_the_ph, (amIDoctor ? "doctor" : "patient")) );

        Glide.with(this)
                .load(amIDoctor ? doctor.getImageUrl() : patient.getImageUrl())
                .timeout(30*1000)
                .placeholder(R.drawable.doctor_icon)
                .into(binding.imageViewProfile);
        binding.tvDoctorName.setText( amIDoctor ? doctor.getName() : patient.getName() );
        binding.tvDoctorExperience.setText(
                amIDoctor ? doctor.getExperienceInMonth() +" years of experience" : patient.getAge()+" years");
        binding.tvDoctorSpecialities.setText( amIDoctor ? doctor.getSpecialityMessage() : patient.getBasicInfo() );

        binding.tvDoctorDetails.setText( amIDoctor ? doctor.getDescription() : patient.getDesc() );
    }

    private void showSnackBar(String message){
        Snackbar snackbar = Snackbar.make(binding.main,message,Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @SuppressWarnings("deprecation")
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    // todo add button for join call in appointment details page


    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }
}
