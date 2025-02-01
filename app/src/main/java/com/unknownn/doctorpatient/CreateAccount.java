package com.unknownn.doctorpatient;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unknownn.doctorpatient.databinding.ActivityCreateAccountBinding;
import com.unknownn.doctorpatient.others.MyPopUp;
import com.unknownn.doctorpatient.others.SharedPref;

public class CreateAccount extends AppCompatActivity {

    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = { Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA };

//    private RelativeLayout rlSignIn, rlDoctor, rlPatient;
//    private ProgressBar pbSignIn;
//    private Button buttonSignIn;
//    private TextView tvSignInMessage;
//
//    private TextView tvSelected;

    private GoogleSignInClient client;
    private ActivityResultLauncher<Intent> mGetContent;
    private FirebaseAuth mAuth;
    private boolean isProgressShowing = false, forceExit = false;
    private SharedPref sp = null;
    private Boolean isDoctor = null;
    private Dialog mainDialog = null;

    private ActivityCreateAccountBinding binding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        tvSignInMessage = findViewById(R.id.tv_sign_in_message);
//        rlSignIn = findViewById(R.id.rl_sign_in);
//        pbSignIn = findViewById(R.id.pb_sign_in);
//        buttonSignIn = findViewById(R.id.button_sign_in);
//        tvSelected = findViewById(R.id.tv_selected);
//        rlDoctor = findViewById(R.id.rl_d);
//        rlPatient = findViewById(R.id.rl_p);

        forceExit = getIntent().getBooleanExtra("force_exit",false);

        startCallBack();
        createRequest();
        setClickListener();
    }

    private boolean checkSelfPermission() {
        return ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED;
    }

    private void showSnackBar(String message){
        Snackbar snackbar = Snackbar.make(binding.rlRoot,message,Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void setClickListener(){
        binding.rlP.setOnClickListener(v -> setClicked(false));
        binding.rlD.setOnClickListener(v -> setClicked(true));
        binding.llSignIn.setOnClickListener(v -> signInUser());
    }

    private void setClicked(boolean amIDoctor){
        String text = amIDoctor ? "Doctor" : "Patient";

        binding.tvSelected.setText(text);
        binding.tvSelected.setVisibility(View.VISIBLE);
        showMessageInTV("Sign in to continue");

        isDoctor = amIDoctor;
    }

    /**
     * mGetContent is initialized here for signIn result
     */
    private void startCallBack(){
        mGetContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result != null){
                Intent intent = result.getData();
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    connectToFirebase(account);
                }
                catch (ApiException e) {
                    showAlertDialog("Error occurred", getString(R.string.something_went_wrong));
                    //binding.tvSignInMessage.setText(e.getMessage());
                }
            }
        });

    }

    private void showAlertDialog(String title, String message) {
        MyPopUp myPopUp = new MyPopUp(this, title, message);
        myPopUp.setCancelable(false);
        myPopUp.setClickListener("Dismiss",null);
        myPopUp.show();
    }

    private void connectToFirebase(GoogleSignInAccount acct) {
        showProgress();

        mAuth = FirebaseAuth.getInstance();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    dismissMainDialog();
                    if (task.isSuccessful()) {
                        checkUserExistence();
                    }
                    else {
                        showOrHide(false);
                        showMessageInTV(null);
                        showAlertDialog("Error occurred", "Something went wrong. Try again later");
                    }
                });
    }

    private void showOrHide(boolean show){
        binding.pbSignIn.setVisibility(show ? View.VISIBLE : View.GONE);
        isProgressShowing = show;
    }

    private void checkUserExistence(){
        if(mAuth == null){
            showOrHide(false);
            showAlertDialog("Error occurred","Something went wrong. Retry later");
            showMessageInTV(null);
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null){
            showOrHide(false);
            showMessageInTV(null);
            showAlertDialog("Error occurred","Failed to generate id. Retry later");
            return;
        }

        final String uid = user.getUid();
        final String name = user.getDisplayName();

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    isDoctor = snapshot.child("isDoctor").getValue(Boolean.class);
                    continueToHomepage(isDoctor, uid);
                }
                else{
                    openAddInfoPage(isDoctor);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showOrHide(false);
                showAlertDialog("Error occurred",error.getMessage());
            }
        });

    }

    private void openAddInfoPage(boolean amIDoctor){
        final Intent intent = new Intent(this, (amIDoctor ? DoctorProfile.class : PatientProfile.class));
        intent.putExtra("from_login_page",true);
        startActivity(intent);

//        HashMap<String,Object> map = new HashMap<>();
//        map.put("name",name);
//        map.put("isDoctor",isDoctor);
//
//        int rand = new Random().nextInt(99999);
//        map.put("id",rand);
//
//        ref.updateChildren(map).addOnCompleteListener(task -> {
//            if(task.isSuccessful()){
//                continueToHomepage(isDoctor,rand,false);
//            }
//            else{
//                showOrHide(false);
//                showMessageInTV(null);
//                Exception exception = task.getException();
//                String message = (exception == null) ? "Something went wrong" : exception.getMessage();
//                showAlertDialog("Error occurred",message);
//            }
//        });
    }

    private void continueToHomepage(Boolean isDoctor, String uid){
        if(isDoctor == null){
            showAlertDialog("Error occurred",getString(R.string.something_went_wrong));
            return;
        }

        if(this.isDoctor != isDoctor){
            showSafeToast("Login into your existing account...");
        }

        getSp().saveIsSignedIn(true);
        getSp().saveAmIDoctor(isDoctor);
        getSp().saveMyUid(uid);
        showSafeToast("Signed in successfully");

        Intent intent = new Intent(this, HomePage.class);

        getSp().saveWasMyInfoAdded(true);

        intent.putExtra("is_doctor", sp.amIDoctor());
        intent.putExtra("force_exit",true);
        startActivity(intent);
    }

    private SharedPref getSp(){
        if(sp == null){
            sp = new SharedPref(this);
        }
        return sp;
    }

    private void createRequest(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        client = GoogleSignIn.getClient(this,gso);

    }

    private void signInUser(){
        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
            return;
        }

        if(isDoctor == null){
            showSnackBar("Select your profession");
            return;
        }

        Intent intent = client.getSignInIntent();

        //showMessageInTV("Please wait...");
        mGetContent.launch(intent);
    }

    private void showMessageInTV(String message){
        try{
            if(message==null) binding.tvSignInMessage.setText(getString(R.string.something_went_wrong));
            else binding.tvSignInMessage.setText(message);
        }catch (Exception ignored){}
    }

    private void showSafeToast(String message){
        try{
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }catch (Exception ignored){}
    }

    @Override
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
        if(isProgressShowing){
            showSafeToast("Please wait. Login in progress...");
            return;
        }
        if(forceExit){
            finishAffinity();
        }
        else {
            super.onBackPressed();
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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

}
