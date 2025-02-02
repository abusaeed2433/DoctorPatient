package com.unknownn.doctorpatient;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.unknownn.doctorpatient.databinding.ActivityPatientProfileBinding;
import com.unknownn.doctorpatient.others.MyPopUp;
import com.unknownn.doctorpatient.others.Patient;
import com.unknownn.doctorpatient.others.SharedPref;
import com.unknownn.doctorpatient.others.User;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class PatientProfile extends AppCompatActivity {

    private boolean isMale = true, isProcessing = false;
    private Toast mToast = null;
    private SharedPref sp = null;

    private ActivityPatientProfileBinding binding = null;
    private boolean isFromLoginPage = false;
    private Dialog mainDialog = null;

    private Uri photoUri = null;
    private ActivityResultLauncher<String> mGetContent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPatientProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isFromLoginPage = getIntent().getBooleanExtra("from_login_page",false);

        initializeCallBack();
        setClickListener();
        loadFromDatabase();
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

            for(String str : List.of(name, age, weight, hFt, hIn, desc, gender)){
                if( isTextInValid(str) ){
                    showSafeToast("Fill all the form");
                    return;
                }
            }

            final User oldUser = new SharedPref(PatientProfile.this).getMyProfile();
            final Patient patient = new Patient(
                    null,
                    (oldUser.getIntId() == -1) ? getUniqueID() : oldUser.getIntId(),
                    name,
                    Integer.parseInt(age),
                    Integer.parseInt(weight),
                    gender,
                    Integer.parseInt(hFt),
                    Integer.parseInt(hIn),
                    desc,
                    null
            );

            saveToStorageAndDatabase(patient);
        });

        binding.imageViewProfile.setOnClickListener(v -> mGetContent.launch("image/*"));
    }

    private void initializeCallBack(){
        mGetContent = registerForActivityResult(
                new ActivityResultContracts.GetContent(), result -> {
                    if(result == null){
                        showSafeToast("No image picked");
                        return;
                    }
                    photoUri = result;
                    showImage(photoUri);
                }
        );

    }

    private void showImage(Uri uri){
        try{
            Glide.with(this)
                    .load(uri)
                    .into(binding.imageViewProfile);
        }catch (Exception ignored){}
    }
    private void showImage(String url){
        try{
            Glide.with(this)
                    .load(url)
                    .into(binding.imageViewProfile);
        }catch (Exception ignored){}
    }

    private int getUniqueID(){
        final LocalDateTime curLDT = LocalDateTime.now();
        final LocalDateTime startLDT = LocalDateTime.of(2025,1,1,0,0,0);
        final Duration duration = Duration.between(startLDT, curLDT);
        return (int)duration.toSeconds();
    }

    private void saveToStorageAndDatabase(final Patient patient){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            isProcessing = false;
            showAlertDialog("Error occurred", getString(R.string.you_are_not_signed_in));
            return;
        }

        showProgress();
        patient.setUid(user.getUid());

        final StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child("profile").child(patient.getUid()).child("profile_pic.jpg");
        final StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

        if(photoUri == null){
            final String imageUrl = getSp().getMyProfile().getImageUrl();
            saveToDatabase(imageUrl, patient);
            return;
        }

        ref.putFile(photoUri,metadata)
                .addOnSuccessListener(taskSnapshot ->
                        taskSnapshot.getStorage().getDownloadUrl()
                                .addOnSuccessListener(uri -> saveToDatabase(uri.toString(), patient))
                                .addOnFailureListener(e -> {
                                    isProcessing = false;
                                    dismissMainDialog();
                                    showAlertDialog("Error occurred", e.getMessage());
                                }))
                .addOnFailureListener(e -> {
                    isProcessing = false;
                    dismissMainDialog();
                    showAlertDialog("Error occurred", e.getMessage());
                });
    }

    private void saveToDatabase(String imageUrl, Patient patient){
        patient.setImageUrl(imageUrl);
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(patient.getUid());
        ref.setValue(patient).addOnCompleteListener(task -> {
            isProcessing = false;
            if(task.isSuccessful()){
                dismissMainDialog();
                showSafeToast("Successfully saved");
                getSp().saveMyProfile(patient);

                if(isFromLoginPage){
                    getSp().saveIsSignedIn(true);
                    final Intent intent = new Intent(PatientProfile.this, HomePage.class);
                    intent.putExtra("force_exit",true);
                    startActivity(intent);
                }
            }
            else{
                dismissMainDialog();
                final Exception exception = task.getException();
                final String message = (exception == null) ? getString(R.string.something_went_wrong) : exception.getMessage();
                showAlertDialog("Error occurred", message);
            }
        });
    }

    private void loadFromDatabase(){
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
        if(binding == null) return;

        getSp().saveMyProfile(patient);
        binding.editTextName.setText(patient.getName());
        binding.editTextAge.setText( getString(R.string.ph_only,patient.getAge()) );
        binding.editTextWeight.setText( getString(R.string.ph_only,patient.getWeight()) );

        binding.editTextHeightFt.setText( getString(R.string.ph_only,patient.getHeightFt()) );
        binding.editTextHeightIn.setText( getString(R.string.ph_only,patient.getHeightIn()) );
        binding.editTextDescription.setText(patient.getDesc());

        if(patient.getGender().equalsIgnoreCase("female")){
            binding.radioButtonFemale.setChecked(true);
        }
        else{
            binding.radioButtonMale.setChecked(true);
        }
        showImage(patient.getImageUrl());
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

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
