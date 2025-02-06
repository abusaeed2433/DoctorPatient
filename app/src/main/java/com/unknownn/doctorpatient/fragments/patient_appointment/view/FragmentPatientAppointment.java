package com.unknownn.doctorpatient.fragments.patient_appointment.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.unknownn.doctorpatient.appointment_details.AppointmentDetailsActivity;
import com.unknownn.doctorpatient.databinding.FragmentAppointmentBinding;
import com.unknownn.doctorpatient.homepage_doctor.model.Appointment;
import com.unknownn.doctorpatient.homepage_doctor.view.AppointmentAdapter;
import com.unknownn.doctorpatient.homepage_patient.view.PatientHomePage;
import com.unknownn.doctorpatient.others.SharedPref;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FragmentPatientAppointment extends Fragment {

    private FragmentAppointmentBinding binding = null;
    private AppointmentAdapter appointmentAdapter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAppointmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        startAdapter();
        downloadAppointments();
    }

    private void startAdapter(){
        final Activity activity = getActivity();
        if(activity == null) return;

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
        appointmentAdapter = new AppointmentAdapter(activity, false, diffCallback, this::openDetailsPage);

        binding.rvAppointment.setAdapter(appointmentAdapter);
    }

    private void openDetailsPage(Appointment item){
        final Activity activity = getActivity();
        if(activity == null) return;

        Intent intent = new Intent(activity, AppointmentDetailsActivity.class);
        intent.putExtra("appointment",item);
        startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void updateAdapter(List<Appointment> list){
        if(list.isEmpty()){
            binding.tvNotFound.setVisibility(View.VISIBLE);
        }
        else{
            binding.tvNotFound.setVisibility(View.INVISIBLE);
        }

        Collections.sort(list);
        appointmentAdapter.submitList(list);

        if(list.isEmpty()){
            sendAppointment(null);
            return;
        }

        final LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(5);
        if(list.get(0).getTimestamp() <= localDateTime.toEpochSecond(ZoneOffset.UTC)){
            sendAppointment(list.get(0));
        }
        else{
            sendAppointment(null);
        }
    }

    private void sendAppointment(Appointment appointment){
        final Activity activity = getActivity();
        if(activity == null) return;

        if(activity instanceof PatientHomePage){
            ((PatientHomePage)activity).sendCurrentAppointment(appointment);
        }
    }

    private void downloadAppointments(){
        final Activity activity = getActivity();
        if(activity == null) return;

        final String myUserId = new SharedPref(activity).getMyProfile().getUid();

        final Query query = FirebaseDatabase.getInstance().getReference("appointment")
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

}