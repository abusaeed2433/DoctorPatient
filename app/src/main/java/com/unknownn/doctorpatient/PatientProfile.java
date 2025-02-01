package com.unknownn.doctorpatient;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unknownn.doctorpatient.databinding.ActivityPatientProfileBinding;
import com.unknownn.doctorpatient.others.MyPopUp;
import com.unknownn.doctorpatient.others.Patient;
import com.unknownn.doctorpatient.others.SharedPref;

import java.time.Duration;
import java.time.LocalDateTime;

public class PatientProfile extends AppCompatActivity {

    private boolean isMale = true, isProcessing = false;
    private Toast mToast = null;
    private SharedPref sp = null;

    private ActivityPatientProfileBinding binding = null;
    private boolean isFromLoginPage = false;
    private Dialog mainDialog = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPatientProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isFromLoginPage = getIntent().getBooleanExtra("from_login_page",false);

        setClickListener();
        loadFromDb();
    }

    private void setClickListener(){
        binding.buttonSave.setOnClickListener(v -> {
            if(isProcessing) return;

            isProcessing = true;
            final String name = String.valueOf(binding.editTextName.getText());
            final String age = String.valueOf(binding.editTextAge.getText());
            final String weight = String.valueOf(binding.editTextWeight.getText());
            final String hFt = String.valueOf(binding.editTextHeightFt.getText());
            final String hIn = String.valueOf(binding.editTextHeightIn.getText());
            final String desc = String.valueOf(binding.editTextDescription.getText());
            final String gender = isMale ? "Male" : "Female";

            final String uid = new SharedPref(PatientProfile.this).getMyUid();
            final Patient patient = new Patient(uid, getUniqueID(), name,age,weight,gender,hFt,hIn,desc,null);
            validateAndSave(patient);
        });
    }

    private int getUniqueID(){
        final LocalDateTime curLDT = LocalDateTime.now();
        final LocalDateTime startLDT = LocalDateTime.of(2025,1,1,0,0,0);
        final Duration duration = Duration.between(startLDT, curLDT);
        return (int)duration.toSeconds();
    }

    private void validateAndSave(final Patient patient){
        if( isTextInValid(patient.getName()) ){ showSafeToast("Enter your name"); isProcessing = false; return; }
        if( isTextInValid(patient.getAge()) ){ showSafeToast("Enter your age"); isProcessing = false; return; }
        if( isTextInValid(patient.getWeight()) ){ showSafeToast("Enter your weight"); isProcessing = false; return; }
        if( isTextInValid(patient.getHeightFt()) ){ showSafeToast("Enter your height(ft)"); isProcessing = false; return; }
        if( isTextInValid(patient.getHeightIn()) ){ showSafeToast("Enter your height(in)"); isProcessing = false; return; }
        if( isTextInValid(patient.getDesc()) ){ showSafeToast("Enter your disease details"); isProcessing = false; return; }

        saveToDatabase(patient);
    }

    private void saveToDatabase(final Patient patient){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            isProcessing = false;
            showAlertDialog("Error occurred", getString(R.string.you_are_not_signed_in));
            return;
        }

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(patient.getUid());
        ref.setValue(patient).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                dismissMainDialog();
                showSafeToast("Successfully saved");
                getSp().saveWasMyInfoAdded(true);
                getSp().saveMyProfile(patient);

                if(isFromLoginPage){
                    final Intent intent = new Intent(PatientProfile.this, HomePage.class);
                    intent.putExtra("force_exit",true);
                    startActivity(intent);
                }
            }
            else{
                dismissMainDialog();
                Exception exception = task.getException();
                final String message = (exception == null) ? getString(R.string.something_went_wrong) : exception.getMessage();
                showAlertDialog("Error occurred", message);
            }
        });

    }

    private void loadFromDb(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            showAlertDialog("Error occurred", getString(R.string.you_are_not_signed_in));
            return;
        }

        final String uid = user.getUid();
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    final Patient patient = snapshot.getValue(Patient.class);
                    processData(patient);
                }
                else{
                    processData(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                processData(null);
            }
        });
    }

    private void showAlertDialog(String title, String message) {
        MyPopUp myPopUp = new MyPopUp(this, title, message);
        myPopUp.setCancelable(false);
        myPopUp.setClickListener("Dismiss",null);
        myPopUp.show();
    }

    private SharedPref getSp(){
        if(sp == null) sp = new SharedPref(PatientProfile.this);
        return sp;
    }

    private void processData(final Patient patient){
        if(patient == null) return;

        getSp().saveMyProfile(patient);
        binding.editTextName.setText(patient.getName());
        binding.editTextAge.setText(patient.getAge());
        binding.editTextWeight.setText(patient.getWeight());

        binding.editTextHeightFt.setText(patient.getHeightFt());
        binding.editTextHeightIn.setText(patient.getHeightIn());
        binding.editTextDescription.setText(patient.getDesc());

        if(patient.getGender().equalsIgnoreCase("female")){
            binding.radioButtonFemale.setChecked(true);
        }
        else{
            binding.radioButtonMale.setChecked(true);
        }
    }

    private boolean isTextInValid(String text){
        return text == null || text.isEmpty();
    }

    public void onMaleClicked(View view) {
        isMale = true;
    }

    public void onFemaleClicked(View view) {
        isMale = false;
    }

    private void showSafeToast(String message){
        try{
            if(mToast != null) mToast.cancel();

            mToast = Toast.makeText(PatientProfile.this,message,Toast.LENGTH_LONG);
            mToast.show();

        }catch (Exception ignored){}
    }

    private void showSnackBar(String message){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.cl_profile_root),message,Snackbar.LENGTH_LONG);
        snackbar.setAction(android.R.string.ok, v -> snackbar.dismiss());
        snackbar.show();
    }

    public void showProgress2() {
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

}
