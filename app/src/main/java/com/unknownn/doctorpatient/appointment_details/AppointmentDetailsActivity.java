package com.unknownn.doctorpatient.appointment_details;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.unknownn.doctorpatient.R;
import com.unknownn.doctorpatient.VideoActivity;
import com.unknownn.doctorpatient.databinding.ActivityAppointmentDetailsBinding;
import com.unknownn.doctorpatient.homepage_doctor.model.Appointment;
import com.unknownn.doctorpatient.homepage_doctor.view.DoctorHomePage;
import com.unknownn.doctorpatient.others.Doctor;
import com.unknownn.doctorpatient.others.MyPopUp;
import com.unknownn.doctorpatient.others.Patient;
import com.unknownn.doctorpatient.others.SharedPref;
import com.unknownn.doctorpatient.others.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class AppointmentDetailsActivity extends AppCompatActivity {

    private long lastTimeFromDatabase = 0L;
    private ActivityAppointmentDetailsBinding binding = null;
    private final Timer timer = new Timer();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Dialog mainDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppointmentDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final Appointment appointment = (Appointment) getIntent().getSerializableExtra("appointment");
        final boolean amIDoctor = new SharedPref(this).getMyProfile().isAmIDoctor();
        downloadProfile(appointment, amIDoctor);
        setClickListener(appointment, amIDoctor);

        if(appointment != null) {
            downloadAppointment(appointment);
            downloadDoctorStatus(amIDoctor, appointment);
        }
    }

    private ValueEventListener appointmentListener;
    private DatabaseReference appointmentRef;
    private void downloadAppointment(Appointment appointment){
        appointmentRef = FirebaseDatabase.getInstance().getReference("appointment").child(appointment.getAppointmentId());
        appointmentListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()) {
                    finish(); // automatically back to homepage
                    return;
                }

                Boolean temp = snapshot.child("confirmed").getValue(Boolean.class);
                if(temp == null) return;
                boolean isAccepted = temp;

                appointment.setConfirmed(isAccepted);
                if(isAccepted){
                    binding.tvOnlineLeft.setText(getString(R.string.ph_string_only, "Doctor is currently"));
                }
                else{
                    binding.tvOnlineLeft.setText(getString(R.string.ph_string_only, "Your appointment is still"));
                    binding.tvAvailableStatus.setText(getString(R.string.ph_string_only, "Pending"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        appointmentRef.addValueEventListener(appointmentListener);

    }

    private void downloadDoctorStatus(boolean amIDoctor, Appointment appointment){
        if(amIDoctor){
            binding.llOnlineStatus.setVisibility(View.GONE);
            return;
        }

        // patient seeing doctor details
        binding.llOnlineStatus.setVisibility(View.VISIBLE);
        repeatAtInterval(appointment);

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("available/doctor").child(appointment.getDoctorUid());
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

    private void repeatAtInterval(Appointment appointment){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                final long curTime = System.currentTimeMillis();

                mHandler.post(() -> {

                    if(!appointment.isConfirmed()){
                        binding.tvOnlineLeft.setText(getString(R.string.ph_string_only, "Your appointment is still"));
                        binding.tvAvailableStatus.setText(getString(R.string.ph_string_only, "Pending"));
                        return;
                    }

                    binding.tvOnlineLeft.setText(getString(R.string.ph_string_only, "Doctor is currently"));
                    boolean isActive = (curTime - lastTimeFromDatabase) < DoctorHomePage.UPDATE_TIME_INTERVAL;

                    if(lastTimeFromDatabase == 0){
                        binding.tvAvailableStatus.setText(getString(R.string.dots));
                    }
                    else {
                        binding.tvAvailableStatus.setText(
                                isActive ? getString(R.string.online) : getString(R.string.offline)
                        );
                    }

                    binding.tvAvailableStatus.setTextColor(
                            getResources().getColor(
                                    isActive ? R.color.blue : R.color.red,
                                    null
                            )
                    );

                });

            }
        };

        timer.schedule(timerTask, 0, DoctorHomePage.UPDATE_TIME_INTERVAL/4);
    }

    private void downloadProfile(Appointment appointment, boolean amIDoctor){
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
        if(amIDoctor){
            binding.buttonAccept.setText(
                    appointment.isConfirmed() ? "Join" : "Accept"
            );
        }
        else{
            if(appointment.isConfirmed()){
                binding.buttonAccept.setVisibility(View.VISIBLE);
                binding.buttonAccept.setText( getString(R.string.ph_string_only,"Join") );
            }
            else{
                binding.buttonAccept.setVisibility(View.GONE);
            }

        }
    }

    private void setClickListener(Appointment appointment, boolean amIDoctor){
        reloadButton(appointment, amIDoctor);

        binding.buttonAccept.setOnClickListener(v -> {
            if(appointment.isConfirmed()){ // join button for both doctor and patient
                joinCall(appointment);
                return;
            }

            if(amIDoctor){ // accept
                acceptPatientAppointment(appointment, amIDoctor);
            }

        });

        binding.buttonDelete.setOnClickListener(v -> deleteAppointment(appointment.getAppointmentId()));
    }

    private void deleteAppointment(String appointmentId){
        new AlertDialog.Builder(this)
                .setTitle("Delete?")
                .setMessage("Are you sure you want to  delete this appointment?")
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel,null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("appointment").child(appointmentId);
                    ref.removeValue().addOnSuccessListener(unused -> showSnackBar("Deleted successfully"));
                })
                .show();
    }

    private void showAlertDialog(String title, String message) {
        MyPopUp myPopUp = new MyPopUp(this, title, message);
        myPopUp.setCancelable(false);
        myPopUp.setClickListener("Dismiss",null);
        myPopUp.show();
    }

    private void joinCall(Appointment appointment){

        final String dateTime = appointment.getDateTime();
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy_hh:mma", Locale.US);

        final LocalDateTime appointmentDateTime = LocalDateTime.from(dateTimeFormatter.parse(dateTime));

        final LocalDateTime curDateTime = LocalDateTime.now();

        if( curDateTime.isBefore( appointmentDateTime.minusMinutes(10) )){
            showAlertDialog("Appointment not started", "This appointment is not started yet. You can't join now.");
            return;
        }
        if( curDateTime.isAfter( appointmentDateTime.plusMinutes(40) ) ) {
            showAlertDialog("Appointment time is over", "This appointment is over. You can't join now anymore.");
            return;
        }

        final boolean amIDoctor = new SharedPref(this).getMyProfile().isAmIDoctor();

        showProgress();
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("token").child(appointment.getAppointmentId());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    finish();
                    return;
                }

                dismissMainDialog();
                final String appId = String.valueOf(snapshot.child("appId").getValue());
                final String cName = String.valueOf(snapshot.child("channelName").getValue());
                final String doctorToken = String.valueOf(snapshot.child("doctorToken").getValue());
                final String patientToken = String.valueOf(snapshot.child("patientToken").getValue());

                Intent intent = new Intent(AppointmentDetailsActivity.this, VideoActivity.class);
                intent.putExtra("doctor_uid", appointment.getDoctorUid());
                intent.putExtra("patient_uid", appointment.getPatientUid());
                intent.putExtra("token", (amIDoctor ? doctorToken : patientToken));
                intent.putExtra("channel_name", cName);
                intent.putExtra("app_id", appId);
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dismissMainDialog();
            }
        });
    }

    private void acceptPatientAppointment(Appointment appointment, boolean amIDoctor){
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("appointment").child(appointment.getAppointmentId());
        ref.child("confirmed").setValue(true).addOnSuccessListener(unused -> {
            appointment.setConfirmed(true);
            reloadButton(appointment, amIDoctor);
        });
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

    public void showProgress() {
        mainDialog = new Dialog(this);
        mainDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mainDialog.setContentView(R.layout.progress_bar_2);
        Window window = mainDialog.getWindow();
        if(window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        mainDialog.setCanceledOnTouchOutside(false);
        mainDialog.setCancelable(false);
        mainDialog.show();
    }
    private void dismissMainDialog(){
        try {
            mainDialog.dismiss();
            mainDialog = null;
        }catch (Exception ignored){}
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        appointmentRef.removeEventListener(appointmentListener);
    }
}
