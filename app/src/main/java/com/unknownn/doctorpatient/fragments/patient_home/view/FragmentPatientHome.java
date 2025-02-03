package com.unknownn.doctorpatient.fragments.patient_home.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unknownn.doctorpatient.R;
import com.unknownn.doctorpatient.adapter.AvAdapter;
import com.unknownn.doctorpatient.databinding.FragmentHomeBinding;
import com.unknownn.doctorpatient.enums.Speciality;
import com.unknownn.doctorpatient.homepage_doctor.model.Appointment;
import com.unknownn.doctorpatient.homepage_patient.view.PatientHomePage;
import com.unknownn.doctorpatient.others.Doctor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class FragmentPatientHome extends Fragment {

    private final Set<Speciality> selectedSpecialities = new TreeSet<>();
    private String searchKey = "";

    private FragmentHomeBinding binding = null;
    private final List<Doctor> doctorList = new ArrayList<>();
    private AvAdapter doctorAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity activity = getActivity();
        if(activity instanceof PatientHomePage){
            ((PatientHomePage) activity).setAppointmentListener(new AppointmentListener() {
                @Override
                public void sendCurrentAppointment(@Nullable Appointment curItem) {
                    if(curItem == null){
                        binding.tvNoAppointment.setVisibility(View.VISIBLE);
                        binding.clCurrentAppointmentMain.setVisibility(View.GONE);
                    }
                    else{
                        binding.tvNoAppointment.setVisibility(View.GONE);
                        binding.clCurrentAppointmentMain.setVisibility(View.VISIBLE);

                        binding.tvDayDD.setText( curItem.getDayDD() );
                        binding.tvMonthDayName.setText( curItem.getDateMmDayName() );
                        Glide.with(activity)
                                .load(curItem.getDoctorImage())
                                .timeout(30*1000)
                                .placeholder(R.drawable.doctor_icon)
                                .into(binding.ivProfile);
                        binding.tvName.setText( curItem.getDoctorName() );
                        binding.tvTime.setText( curItem.getTime() );
                        binding.tvInfo.setText( curItem.getDoctorSpeciality() );
                    }
                }
            });
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        startAdapter();
        setListener();
        showSpecialityList();
        downloadAllDoctors();
    }

    private void downloadAllDoctors(){
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final List<Doctor> doctors = new ArrayList<>();

                for(DataSnapshot ds : snapshot.getChildren()){

                    final Doctor doctor = ds.getValue(Doctor.class);
                    if(doctor == null || !doctor.isAmIDoctor()) continue;

                    doctors.add(doctor);
                }

                updateAdapter(doctors);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateAdapter(new ArrayList<>());
            }
        });
    }

    private void setListener(){
        binding.editTextSearch.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                searchKey = String.valueOf(binding.editTextSearch.getText()).toLowerCase().trim();
                filterAndShow(this.doctorList);
                return true;
            }
            return false;
        });
    }

    private void startAdapter(){
        doctorAdapter = new AvAdapter(getActivity(), this::openDoctorDetailsPage);

        final boolean isLarge = getResources().getBoolean(R.bool.isLargeDevice);
        final int count = (isLarge) ? 3 : 2;
        binding.rvDoctors.setLayoutManager( new GridLayoutManager(getActivity(), count, RecyclerView.VERTICAL, false));
        binding.rvDoctors.setAdapter(doctorAdapter);
    }

    private void openDoctorDetailsPage(Doctor doctor){

    }

    private List<Doctor> filterList(List<Doctor> doctors){
        final List<Doctor> tempList = new ArrayList<>();
        mainLoop:
        for(Doctor doctor : doctors){
            if(!doctor.getName().contains(searchKey)) continue;

            final List<Speciality> docSpecialities = doctor.getAllSpecialities();
            for(Speciality sp : docSpecialities){
                if(selectedSpecialities.contains(Speciality.ALL) || selectedSpecialities.contains(sp)){
                    tempList.add(doctor);
                    continue mainLoop;
                }
            }
        }

        return tempList;
    }

    private void filterAndShow(List<Doctor> doctorList){
        List<Doctor> doctors = filterList(doctorList);

        binding.progressBar.setVisibility(View.GONE);
        if(doctors.isEmpty()){
            binding.tvNotFound.setVisibility(View.VISIBLE);
        }
        else{
            binding.tvNotFound.setVisibility(View.INVISIBLE);
        }

        doctorAdapter.submitList(doctors);
    }

    private void updateAdapter(List<Doctor> doctors){
        this.doctorList.clear();
        this.doctorList.addAll(doctors);
        filterAndShow(this.doctorList);
    }

    private void showSpecialityList(){
        final SpecialityAdapter adapter = new SpecialityAdapter(getActivity(), (speciality, removed) -> {
            if(removed){
                selectedSpecialities.remove(speciality);
            }
            else{
                selectedSpecialities.add(speciality);
            }
            filterAndShow(doctorList);
        });

        selectedSpecialities.add(Speciality.ALL);
        binding.rvSpeciality.setAdapter(adapter);
        adapter.submitList(Speciality.getAll());
    }

    public interface AppointmentListener{
        void sendCurrentAppointment(@Nullable Appointment appointment);
    }

}
