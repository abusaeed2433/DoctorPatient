package com.unknownn.doctorpatient.fragments.patient_appointment.view;

import android.app.Activity;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.unknownn.doctorpatient.R;
import com.unknownn.doctorpatient.databinding.ActivityDoctorProfileBinding;
import com.unknownn.doctorpatient.databinding.FragmentAppointmentBinding;
import com.unknownn.doctorpatient.homepage_doctor.model.Appointment;
import com.unknownn.doctorpatient.homepage_doctor.view.AppointmentAdapter;
import com.unknownn.doctorpatient.others.ItemClickListener;
import com.unknownn.doctorpatient.others.SharedPref;

import java.util.ArrayList;
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
        readAppointment();
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
        
    }

    private void updateAdapter(List<Appointment> list){
        if(list.isEmpty()){
            binding.tvNotFound.setVisibility(View.VISIBLE);
            return;
        }

        binding.tvNotFound.setVisibility(View.INVISIBLE);

        appointmentAdapter.submitList(list);
    }

    private void readAppointment(){
        final Activity activity = getActivity();
        if(activity == null) return;

        final String myUserId = new SharedPref(activity).getMyProfile().getUid();

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

}