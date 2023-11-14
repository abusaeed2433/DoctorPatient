package com.unknownn.doctorpatient;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unknownn.doctorpatient.others.Patient;
import com.unknownn.doctorpatient.others.SharedPref;

import java.util.Map;

public class PatientProfile extends AppCompatActivity {

    private ProgressBar pbProfile;
    private TextInputEditText editTextName, editTextAge, editTextWeight,
            editTextHeightFt, editTextHeightIn, editTextDescription;

    private Button buttonSave;
    private RadioButton rbMale, rbFemale;

    private boolean isMale = true, isProcessing = false;
    private Toast mToast = null;
    private SharedPref sp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile);

        findAllViews();
        setClickListener();
        loadFromDb();
    }

    private void findAllViews(){
        pbProfile = findViewById(R.id.pb_profile);
        editTextName = findViewById(R.id.edit_text_name);
        editTextAge = findViewById(R.id.edit_text_age);
        editTextWeight = findViewById(R.id.edit_text_weight);
        editTextHeightFt = findViewById(R.id.edit_text_height_ft);
        editTextHeightIn = findViewById(R.id.edit_text_height_in);
        editTextDescription = findViewById(R.id.editTextDescription);

        rbMale = findViewById(R.id.radio_button_male);
        rbFemale = findViewById(R.id.radio_button_female);
        buttonSave = findViewById(R.id.button_save);
    }

    private void setClickListener(){
        buttonSave.setOnClickListener(v -> {
            if(isProcessing) return;

            isProcessing = true;
            String name = String.valueOf(editTextName.getText());
            String age = String.valueOf(editTextAge.getText());
            String weight = String.valueOf(editTextWeight.getText());
            String hFt = String.valueOf(editTextHeightFt.getText());
            String hIn = String.valueOf(editTextHeightIn.getText());
            String desc = String.valueOf(editTextDescription.getText());
            String gender = isMale ? "Male" : "Female";
            Patient patient = new Patient(null,name,age,weight,gender,hFt,hIn,desc);
            validateAndSave(patient);
        });
    }

    private void validateAndSave(final Patient patient){
        if(patient == null){
            showSafeToast(getString(R.string.something_went_wrong));
            isProcessing = false;
            return;
        }

        if( isTextInValid(patient.getName()) ){ showSafeToast("Enter your name"); isProcessing = false; return; }

        if( isTextInValid(patient.getAge()) ){ showSafeToast("Enter your age"); isProcessing = false; return; }
        if( isTextInValid(patient.getWeight()) ){ showSafeToast("Enter your weight"); isProcessing = false; return; }
        if( isTextInValid(patient.getHeightFt()) ){ showSafeToast("Enter your height(ft)"); isProcessing = false; return; }
        if( isTextInValid(patient.getHeightIn()) ){ showSafeToast("Enter your height(in)"); isProcessing = false; return; }
        if( isTextInValid(patient.getDesc()) ){ showSafeToast("Enter your disease details"); isProcessing = false; return; }

        saveToDatabase(patient);

    }

    private void saveToDatabase(final Patient patient){
        showOrHide(false);
        isProcessing = false;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            showSnackBar(getString(R.string.you_are_not_signed_in));
            showOrHide(true);
            return;
        }

        String uid = user.getUid();
        patient.setUid(uid);


        Map<String, Object> map = patient.getSavableMap();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        ref.updateChildren(map).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                showSafeToast("Successfully updated");
                try{
                    getSp().saveWasMyInfoAdded(true);
                    getSp().saveMyProfile(patient);
                }catch (Exception ignored){}
            }
            else{
                showSafeToast(getString(R.string.something_went_wrong));
            }
            showOrHide(true);
        });

    }

    private void loadFromDb(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            showSnackBar(getString(R.string.you_are_not_signed_in));
            showOrHide(true);
            return;
        }

        String uid = user.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    String uid = String.valueOf(snapshot.getKey());
                    String name = String.valueOf(snapshot.child("name").getValue());
                    String age = String.valueOf(snapshot.child("age").getValue());
                    String weight = String.valueOf(snapshot.child("weight").getValue());
                    String hFt = String.valueOf(snapshot.child("heightFt").getValue());
                    String hIn = String.valueOf(snapshot.child("heightIn").getValue());
                    String desc = String.valueOf(snapshot.child("desc").getValue());
                    String gender = String.valueOf(snapshot.child("gender").getValue());
                    Patient patient = new Patient(uid,name,age,weight,gender,hFt,hIn,desc);
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

    private SharedPref getSp(){
        if(sp == null) sp = new SharedPref(PatientProfile.this);
        return sp;
    }

    private void processData(final Patient patient){
        if(patient == null){
            showOrHide(true);
            return;
        }

        getSp().saveMyProfile(patient);
        editTextName.setText(patient.getName());
        editTextAge.setText(patient.getAge());
        editTextWeight.setText(patient.getWeight());

        editTextHeightFt.setText(patient.getHeightFt());
        editTextHeightIn.setText(patient.getHeightIn());
        editTextDescription.setText(patient.getDesc());

        if(patient.getGender().equalsIgnoreCase("female")){
            rbFemale.setChecked(true);
        }
        else{
            rbMale.setChecked(true);
        }

        showOrHide(true);

    }

    private void showOrHide(boolean enableButton){
        if(enableButton){
            pbProfile.setVisibility(View.INVISIBLE);
            buttonSave.setEnabled(true);
        }
        else{
            pbProfile.setVisibility(View.VISIBLE);
            buttonSave.setEnabled(false);
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

}