package com.unknownn.doctorpatient.book_appointment.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.MenuItem;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unknownn.doctorpatient.R;
import com.unknownn.doctorpatient.book_appointment.model.SpanItem;
import com.unknownn.doctorpatient.databinding.ActivityBookAppointmentBinding;
import com.unknownn.doctorpatient.homepage_doctor.model.Appointment;
import com.unknownn.doctorpatient.others.Doctor;
import com.unknownn.doctorpatient.others.ItemClickListener;
import com.unknownn.doctorpatient.others.MyPopUp;
import com.unknownn.doctorpatient.others.Patient;
import com.unknownn.doctorpatient.others.SharedPref;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookAppointmentActivity extends AppCompatActivity {

    private BookAdapter dateAdapter;
    private BookAdapter timeAdapter;
    private Dialog mainDialog = null;
    private Doctor doctor = null;
    private ActivityBookAppointmentBinding binding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookAppointmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        doctor = (Doctor) getIntent().getSerializableExtra("doctor");
        showData(doctor);
        startAdapter();
        setClickListener();
    }

    private void startAdapter(){

        final LinearLayoutManager dateLayoutManager = (LinearLayoutManager) binding.rvDate.getLayoutManager();
        final LinearLayoutManager timeLayoutManager = (LinearLayoutManager) binding.rvTime.getLayoutManager();

        dateAdapter = new BookAdapter();
        timeAdapter = new BookAdapter();

        binding.rvDate.setAdapter(dateAdapter);
        binding.rvTime.setAdapter(timeAdapter);

        // Date adapter items. From curDate - 2 to curDate+15+1+2
        LocalDate localDate = LocalDate.now().minusDays(2);
        DateTimeFormatter formatterBase = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatterUser = DateTimeFormatter.ofPattern("dd\nE");

        final List<SpanItem> dateList = new ArrayList<>();
        for(int i=0; i<20; i++){
            LocalDate ld = localDate.plusDays(i);
            String base = formatterBase.format(ld);
            String dateDayName = formatterUser.format(ld);

            SpannableString spannable = new SpannableString(dateDayName);
            spannable.setSpan(new RelativeSizeSpan(1.5f), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            dateList.add( new SpanItem(i, spannable, base, (i == 2)) );
        }

        dateAdapter.submitList(dateList);
        dateAdapter.highlightItem(2);

        // time adapter items. From 8:00AM to 11:30PM.
        final List<SpanItem> timeList = new ArrayList<>();
        LocalTime startTime = LocalTime.of(8,0);
        LocalTime lastTime = LocalTime.of(23,30);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mma");
        int index = 0;
        while (startTime.isBefore(lastTime)){
            LocalTime endTime = startTime.plusMinutes(30);

            String start = formatter.format(startTime);
            String end = formatter.format(endTime);

            timeList.add( new SpanItem( index, new SpannableString(start+"\n"+end), start, (index == 2) ) );
            startTime = endTime;
            index++;
        }

        timeAdapter.submitList(timeList);
        timeAdapter.highlightItem(2);
    }


    private void setClickListener(){
        binding.buttonBookNow.setOnClickListener(v -> bookAppointment());
    }

    private void bookAppointment(){
        final SpanItem dateItem = dateAdapter.getHighlightedItem();
        final SpanItem timeItem = timeAdapter.getHighlightedItem();
        final Patient patient = (Patient) new SharedPref(this).getMyProfile();

        final String appointmentId = doctor.getUid()+"_"+patient.getUid()+"_"+(dateItem.getDateOrTime().replaceAll("/","-"))+"_"+timeItem.getDateOrTime();

        final Appointment appointment = new Appointment(
                appointmentId,
                dateItem.getDateOrTime(),
                timeItem.getDateOrTime(),
                doctor.getUid(),
                doctor.getName(),
                doctor.getSpecialities(),
                doctor.getImageUrl(),
                patient.getUid(),
                patient.getName(),
                patient.getDesc(),
                patient.getImageUrl(),
                false
        );

        showProgress();
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("appointment").child(appointmentId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    showAlertDialog("Exists", "Appointment exists for this doctor at this date and time");
                    dismissMainDialog();
                }
                else{
                    ref.setValue(appointment).addOnSuccessListener(unused -> {
                        dismissMainDialog();
                        showAlertDialog("Successful", "Appointment booked successfully");
                    }).addOnFailureListener(e -> {
                        dismissMainDialog();
                        showAlertDialog("Error occurred", e.getMessage());
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showAlertDialog("Error occurred", error.getMessage());
                dismissMainDialog();
            }
        });
    }



    @SuppressLint("SetTextI18n")
    private void showData(Doctor doctor){
        try {
            Glide.with(this)
                    .load(doctor.getImageUrl())
                    .timeout(30 * 1000)
                    .placeholder(R.drawable.doctor_icon)
                    .into(binding.imageViewProfile);
        }catch (Exception ignored){}
        binding.tvDoctorName.setText( doctor.getName() );
        binding.tvDoctorExperience.setText(doctor.getExperienceInMonth() +" years of experience");
        binding.tvDoctorSpecialities.setText(doctor.getSpecialityMessage());
        binding.tvDoctorDetails.setText( doctor.getDescription() );
    }

    private void showAlertDialog(String title, String message) {
        MyPopUp myPopUp = new MyPopUp(this, title, message);
        myPopUp.setCancelable(false);
        myPopUp.setClickListener("Dismiss",null);
        myPopUp.show();
    }

    public void showProgress() {
        mainDialog = new Dialog(this);
        mainDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mainDialog.setContentView(R.layout.progress_bar_2);
        Window window = mainDialog.getWindow();
        if(window!=null) window.setBackgroundDrawableResource(android.R.color.transparent);
        mainDialog.setCanceledOnTouchOutside(false);
        mainDialog.setCancelable(false);
        mainDialog.show();
    }
    private void dismissMainDialog(){
        try { mainDialog.dismiss(); }catch (Exception ignored){}
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
}
