package com.unknownn.doctorpatient.fragments.patient_home.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unknownn.doctorpatient.R;
import com.unknownn.doctorpatient.adapter.AvAdapter;
import com.unknownn.doctorpatient.databinding.FragmentHomeBinding;
import com.unknownn.doctorpatient.fragments.patient_home.model.Speciality;
import com.unknownn.doctorpatient.others.AvDoctor;
import com.unknownn.doctorpatient.others.Doctor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class FragmentPatientHome extends Fragment {

    private final Set<String> selectedSpecialities = new TreeSet<>();

    private FragmentHomeBinding binding = null;

    private final List<Doctor> doctorList = new ArrayList<>();
    private AvAdapter doctorAdapter;

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

    private void startAdapter(){
        doctorAdapter = new AvAdapter(getActivity(), doctor -> {
            //openDoctorDetailsPage(doctor);
        });

        final boolean isLarge = getResources().getBoolean(R.bool.isLargeDevice);
        final int count = (isLarge) ? 3 : 2;
        binding.rvDoctors.setLayoutManager( new GridLayoutManager(getActivity(), count, RecyclerView.VERTICAL, false));
        binding.rvDoctors.setAdapter(doctorAdapter);
    }

    private void updateAdapter(List<Doctor> doctors){
        binding.progressBar.setVisibility(View.GONE);
        if(doctors.isEmpty()){
            binding.tvNotFound.setVisibility(View.VISIBLE);
        }
        else{
            binding.tvNotFound.setVisibility(View.INVISIBLE);
        }

        this.doctorList.clear();
        this.doctorList.addAll(doctors);
        doctorAdapter.submitList(this.doctorList);
    }
    private void showSpecialityList(){
        String[] arr = new String[]{"All","Dentist", "Medicine", "Cardiologist", "Mental", "Central"};
        String[] urls = new String[]{
                "https://i.postimg.cc/MKCQtqGB/cardiologist.png",
                "https://i.postimg.cc/MKCQtqGB/cardiologist.png",
                "https://i.postimg.cc/ZYD3BrtV/dentist.png",
                "https://i.postimg.cc/YqGF1htk/medicine.png",
                "https://i.postimg.cc/JzjB2vy2/sarcastic.png",
                "https://i.postimg.cc/JzjB2vy2/sarcastic.png"
        };
        final List<Speciality> specialities = new ArrayList<>();
        for(int i=0; i<arr.length; i++){
            specialities.add(new Speciality(arr[i], urls[i]));
        }

        final SpecialityAdapter adapter = new SpecialityAdapter(getActivity(), (speciality, removed) -> {
            if(removed){
                selectedSpecialities.remove(speciality.getName());
            }
            else{
                selectedSpecialities.add(speciality.getName());
            }
        });

        selectedSpecialities.add(specialities.get(0).getName());
        binding.rvSpeciality.setAdapter(adapter);
        adapter.submitList(specialities);
    }

}
