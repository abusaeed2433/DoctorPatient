package com.unknownn.doctorpatient;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.unknownn.doctorpatient.others.SharedPref;

import java.util.HashMap;
import java.util.Random;

public class CreateAccount extends AppCompatActivity {

    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = { Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA };

    private RelativeLayout rlSignIn, rlDoctor, rlPatient;
    private ProgressBar pbSignIn;
    private Button buttonSignIn;
    private TextView tvSignInMessage;

    private TextView tvSelected;

    private GoogleSignInClient client;
    private ActivityResultLauncher<Intent> mGetContent;
    private FirebaseAuth mAuth;
    private boolean isProgressShowing = false, forceExit = false;
    private SharedPref sp = null;
    private Boolean isDoctor = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        tvSignInMessage = findViewById(R.id.tv_sign_in_message);
        rlSignIn = findViewById(R.id.rl_sign_in);
        pbSignIn = findViewById(R.id.pb_sign_in);
        buttonSignIn = findViewById(R.id.button_sign_in);
        tvSelected = findViewById(R.id.tv_selected);
        rlDoctor = findViewById(R.id.rl_d);
        rlPatient = findViewById(R.id.rl_p);

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
        Snackbar snackbar = Snackbar.make(rlSignIn,message,Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void setClickListener(){
        rlPatient.setOnClickListener(v -> setClicked(false));
        rlDoctor.setOnClickListener(v -> setClicked(true));
        buttonSignIn.setOnClickListener(v -> signInUser());
    }

    private void setClicked(boolean amIDoctor){
        String text = getString(R.string.you_are_a);

        if(amIDoctor) text += " doctor";
        else text += " patient";

        tvSelected.setText(text);

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
                    showSnackBar("Retry again later");
                    tvSignInMessage.setText(e.getMessage());
                }
            }
        });

    }


    private void connectToFirebase(GoogleSignInAccount acct) {
        showMessageInTV("Connecting to database...");
        showOrHide(true);

        mAuth = FirebaseAuth.getInstance();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        addInfoIfNeeded();
                    }
                    else {
                        showOrHide(false);
                        showMessageInTV(null);
                        showSnackBar("Error. Try again later");
                    }
                });
    }

    private void showOrHide(boolean show){
        pbSignIn.setVisibility(show ? View.VISIBLE : View.GONE);
        isProgressShowing = show;
    }

    private void addInfoIfNeeded(){
        if(mAuth==null){
            showOrHide(false);
            showSnackBar("Something went wrong. Retry later");
            showMessageInTV(null);
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null){
            showOrHide(false);
            showMessageInTV(null);
            showSnackBar("Failed to generate id. Retry later");
            return;
        }

        String name, uid;
        uid = user.getUid();
        name = user.getDisplayName();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Boolean isDoctor = null;
                    int id = 0;
                    try{
                        String doctor = String.valueOf(snapshot.child("isDoctor").getValue());
                        isDoctor = Boolean.parseBoolean(doctor);
                        String sid = String.valueOf(snapshot.child("id").getValue());
                        id = Integer.parseInt(sid);

                    }catch (Exception ignored){}

                    boolean wasInfoAdded = snapshot.child("age").exists();

                    completeAndBack(isDoctor,id,wasInfoAdded);
                }
                else{
                    HashMap<String,Object> map = new HashMap<>();
                    map.put("name",name);
                    map.put("isDoctor",isDoctor);

                    int rand = new Random().nextInt(99999);
                    map.put("id",rand);

                    ref.updateChildren(map).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            completeAndBack(isDoctor,rand,false);
                        }
                        else{
                            showOrHide(false);
                            showMessageInTV(null);
                            showSnackBar("Something went wrong. Retry later");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showOrHide(false);
                showSnackBar(error.getMessage());
            }
        });

    }

    private void completeAndBack(Boolean isDoctor, int id, boolean wasInfoAdded){
        if(isDoctor == null){
            showSnackBar(getString(R.string.something_went_wrong));
            return;
        }

        if(this.isDoctor != isDoctor){
            showSafeToast("Login into your existing account...");
        }

        getSp().saveIsSignedIn(true);
        getSp().saveAmIDoctor(isDoctor);
        getSp().saveMyId(id);
        showSafeToast("Signed in successfully"); // signed in successfully

        Intent intent = new Intent(this, HomePage.class);

        getSp().saveWasMyInfoAdded(wasInfoAdded);

        intent.putExtra("is_doctor",sp.amIDoctor());
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

        showMessageInTV("Please wait...");
        mGetContent.launch(intent);
    }

    private void showMessageInTV(String message){
        try{
            if(message==null) tvSignInMessage.setText(getString(R.string.something_went_wrong));
            else tvSignInMessage.setText(message);
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
            showSnackBar("Please wait. Login in progress...");
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

}
