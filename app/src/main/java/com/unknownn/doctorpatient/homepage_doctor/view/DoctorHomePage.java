package com.unknownn.doctorpatient.homepage_doctor.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.unknownn.doctorpatient.CreateAccount;
import com.unknownn.doctorpatient.DoctorProfile;
import com.unknownn.doctorpatient.PatientProfile;
import com.unknownn.doctorpatient.R;
import com.unknownn.doctorpatient.VideoActivity;
import com.unknownn.doctorpatient.adapter.AvAdapter;
import com.unknownn.doctorpatient.databinding.ActivityHomepageBinding;
import com.unknownn.doctorpatient.others.AvDoctor;
import com.unknownn.doctorpatient.others.Doctor;
import com.unknownn.doctorpatient.others.Patient;
import com.unknownn.doctorpatient.others.SharedPref;
import com.unknownn.doctorpatient.others.SnapListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DoctorHomePage extends AppCompatActivity implements SnapListener {

    private static final int UPDATE_TIME_INTERVAL = 10000;
    public static final int UPDATE_TIME_INTERVAL_MAX = 12000;
//    private ProgressBar progressBar;
//    private TextView tvMessage;
//    private RecyclerView recyclerView;
//    private Button buttonExit;
    private AvAdapter adapter;

    private boolean amIDoctor = false, forceExit = false, hasDoublePressed = false;
    private SharedPref sp;
    private final Handler mHandler = new Handler();
    private Runnable runnable = null;
    private Dialog mainDialog, callingDialog;
    private TextView tvProgress;
    private boolean isCallingShowing = false, inPauseState = false, hasClickedOne = false;
    private Toast mToast = null;
    private String appId = null, token = null, cName = null;

    private ActivityHomepageBinding binding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomepageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //User user = getSp().getMyProfile();
        amIDoctor = getSp().amIDoctor();
        forceExit = getIntent().getBooleanExtra("force_exit",false);

        downloadToken();
        startAdapter();
        setClickListener();

        if(amIDoctor){
            showInTv(getString(R.string.making_you_available));
            updateMyStatus();
        }
        else {
            showInTv(getString(R.string.getting_active_doc_list_dot));
            downloadOnline();
        }
    }

    private void downloadToken(){
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("token/0");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appId = String.valueOf(snapshot.child("appId").getValue());
                token = String.valueOf(snapshot.child("token").getValue());
                cName = String.valueOf(snapshot.child("cName").getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setClickListener(){
        binding.buttonExit.setOnClickListener(v -> new AlertDialog.Builder(DoctorHomePage.this)
                .setTitle("Exit?")
                .setMessage("Are you sure you want to exit?")
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel,null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> exitUser())
                .show());
    }

    private void exitUser(){
        if(runnable != null){
            mHandler.removeCallbacks(runnable);
        }

        if(amIDoctor) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String uid = user.getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                        .child("available/doctor").child(uid).child("lastOnline");
                ref.setValue(0);
            }
        }
        finishAffinity();
    }

    private void startAdapter(){
        adapter = new AvAdapter(this,doctor -> {
            if(!hasClickedOne){
                hasClickedOne = true;
                //handleRest(doctor);
            }
        });

        final boolean isLarge = getResources().getBoolean(R.bool.isLargeDevice);
        final int count = (isLarge) ? 3 : 2;
        binding.recyclerView.setLayoutManager( new GridLayoutManager(this, count, RecyclerView.VERTICAL, false));
        binding.recyclerView.setAdapter(adapter);
    }

    private SharedPref getSp(){
        if(sp == null) {
            sp = new SharedPref(this);
        }
        return sp;
    }

    private void showInTv(String message){
        binding.tvMessage.setText(message);
    }

    private void handleRest(AvDoctor doctor){
        if(doctor == null) {
            hasClickedOne = false;
            return;
        }

        if(doctor.isInCall()){
            showSnackBar("Busy in another call");
            hasClickedOne = false;
            return;
        }

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            showSnackBar(getString(R.string.you_are_not_signed_in));
            hasClickedOne = false;
            return;
        }

        if(doctor.isTimeInvalid()){
            hasClickedOne = false;
            showSnackBar(getString(R.string.not_available));
            return;
        }

        final String uid = user.getUid();
        showProgress(null,doctor.getUid(),uid);
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("request").child(doctor.getUid()).child(uid);

        final Patient mine = (Patient)getSp().getMyProfile();

        ref.setValue(mine).addOnCompleteListener(task -> {
            hasClickedOne = false;
            if(!task.isSuccessful()){
                dismissMainDialog();
                showSnackBar(getString(R.string.something_went_wrong));
            }
            else{
                changeProgressMessage("Waiting for accepting...");
                addCallUpdateListener(doctor.getUid(),uid);
            }
        });
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    private void addCallUpdateListener(String docUid, String myUid){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("request")
                .child(docUid).child(myUid);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String val = String.valueOf(snapshot.child("status").getValue());
                    if(val.equalsIgnoreCase("accepted")){//accepted
                        acceptOrRejectStatus(ref,this,true,docUid);
                    }
                }
                else{ // rejected
                    acceptOrRejectStatus(ref,this,false,docUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        ref.addValueEventListener(listener);
    }



    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.bar_profile){
            Intent intent = new Intent(DoctorHomePage.this, (amIDoctor ? DoctorProfile.class : PatientProfile.class) );
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        else if(id == R.id.log_out){
            try {
                new AlertDialog.Builder(DoctorHomePage.this)
                        .setTitle("LogOut?")
                        .setMessage("Are you sure you want to log-out")
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> logOutUser())
                        .show();
            }catch (Exception ignored){}
        }
        return super.onOptionsItemSelected(item);
    }

    private void logOutUser(){
        getSp().saveIsSignedIn(false);
        getSp().clearAll();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(this,gso);
        client.signOut();
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(DoctorHomePage.this, CreateAccount.class);
        intent.putExtra("force_exit",true);
        startActivity(intent);
    }

    //patient
    private void acceptOrRejectStatus(DatabaseReference ref, ValueEventListener listener,boolean isAccepted, String docId){
        if(ref != null && listener != null) {
            ref.removeEventListener(listener);
        }

        dismissMainDialog();
        if(isAccepted){
            String patId = getMyUid();

            if(patId != null) {
                Intent intent = new Intent(DoctorHomePage.this, VideoActivity.class);
                intent.putExtra("doctor_uid", docId);
                intent.putExtra("patient_uid", patId);
                intent.putExtra("token", token);
                intent.putExtra("channel_name", cName);
                intent.putExtra("app_id", appId);
                startActivity(intent);
            }
        }
        else{
            showSnackBar("Dismissed");
        }
    }

    private String getMyUid(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) return null;
        return user.getUid();
    }

    private void dismissMainDialog(){
        try {
            if(mainDialog != null) {
                mainDialog.dismiss();
            }
        }catch (Exception ignored){}
    }

    private void dismissCallingDialog(){
        isCallingShowing = false;
        try {
            if(callingDialog != null) {
                callingDialog.dismiss();
            }
        }catch (Exception ignored){}
    }

    private void changeProgressMessage(String message){
        if(mainDialog != null && tvProgress != null){
            tvProgress.setText(message);
        }
    }

    //patient
    public void showProgress(String message, String docUid, String myUid) {
        mainDialog = new Dialog(this);
        mainDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mainDialog.setContentView(R.layout.progress_dialog_layout);

        tvProgress = mainDialog.findViewById(R.id.textViewAboutUpload);
        Button buttonExit = mainDialog.findViewById(R.id.button_exit);

        if(message != null) {
            tvProgress.setText(message);
        }

        buttonExit.setOnClickListener(v -> {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                    .child("request").child(docUid).child(myUid);
            ref.removeValue();
            dismissMainDialog();
        });

        Window window = mainDialog.getWindow();
        if(window!=null){
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        mainDialog.setCanceledOnTouchOutside(false);
        mainDialog.setCancelable(false);
        if(!isFinishing()) mainDialog.show();

    }


    private void updateMyStatus(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            showSnackBar(getString(R.string.something_went_wrong));
            binding.tvMessage.setText(R.string.restart_app);
            binding.pbHomepage.setVisibility(View.GONE);
            return;
        }

        final String uid = user.getUid();
        downloadMyData(uid, mine -> {
            if(mine == null){
                binding.tvMessage.setText(R.string.failed_to_fetch_data);
                binding.pbHomepage.setVisibility(View.GONE);
            }
            else{
                Map<String,Object> map = mine.getMap();
                map.put("lastOnline", ServerValue.TIMESTAMP);
                map.put("inCall", false);

                final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("available/doctor").child(uid);
                ref.updateChildren(map).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        binding.pbHomepage.setVisibility(View.GONE);
                        binding.tvMessage.setText(R.string.notify_message);
                        addRequestListener(uid);
                        startRunningLoop(uid);
                    }
                    else{
                        binding.pbHomepage.setVisibility(View.GONE);
                        binding.tvMessage.setVisibility(View.GONE);
                        showSnackBar(getString(R.string.something_went_wrong));
                    }
                });
            }
        });
    }

    //doctor
    private void addRequestListener(String docUid){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("request").child(docUid);
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                onSnapFound(snapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                onSnapFound(null);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                goNext(null);
            }
        });

    }


    @Override
    public void onSnapFound(DataSnapshot ds) {
        if(ds == null){//deleted
            dismissCallingDialog();
        }
        else {
            goNext(ds);
        }
    }

    private void goNext(DataSnapshot snapshot){
        if(snapshot == null){
            showSnackBar(getString(R.string.something_went_wrong));
            return;
        }

        final Patient patient = snapshot.getValue(Patient.class);
        if(patient == null) {
            showSafeToast("Failed to read");
            return;
        }

        if(isCallingShowing) return;
        showCallingDialog(patient,snapshot);
    }

    private void showCallingDialog(final Patient patientInfo, DataSnapshot snapshot){
        isCallingShowing = true;

        callingDialog = new Dialog(DoctorHomePage.this);
        callingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        callingDialog.setContentView(R.layout.calling_dialog_layout);

        TextView tvPatInfo = callingDialog.findViewById(R.id.tv_pat_info);
        TextView tvPatDesc = callingDialog.findViewById(R.id.tv_pat_desc);

        ImageView ivAccept = callingDialog.findViewById(R.id.iv_accept);
        ImageView ivReject = callingDialog.findViewById(R.id.iv_reject);

        tvPatInfo.setText(patientInfo.getFormattedString());
        tvPatDesc.setText(patientInfo.getDesc());

        ivAccept.setOnClickListener(v -> {
            isCallingShowing = false;
            showSafeToast("Accepting call...");
            dismissCallingDialog();
            acceptCall(snapshot,patientInfo.getUid());
        });

        ivReject.setOnClickListener(v -> {
            isCallingShowing = false;
            snapshot.getRef().removeValue();
            dismissCallingDialog();
        });

        Window window = callingDialog.getWindow();
        if(window!=null){
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        callingDialog.setCanceledOnTouchOutside(false);
        callingDialog.setCancelable(false);
        if(!isFinishing()) callingDialog.show();

    }

    private void showSafeToast(String message){
        try {
            if(mToast != null) mToast.cancel();
            mToast = Toast.makeText(this,message,Toast.LENGTH_LONG);
            mToast.show();
        }catch (Exception ignored){}
    }

    private void acceptCall(DataSnapshot ds, String patUid){
        ds.getRef().child("status").setValue("accepted").addOnCompleteListener(task -> {
            if(!task.isSuccessful()){
                showSnackBar(getString(R.string.something_went_wrong));
            }
            else{
                String docId = getMyUid();
                if(docId != null){

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                            .child("available/doctor").child(docId).child("inCall");
                    ref.setValue(true);
                    Intent intent = new Intent(DoctorHomePage.this, VideoActivity.class);
                    intent.putExtra("doctor_uid", docId);
                    intent.putExtra("patient_uid", patUid);
                    intent.putExtra("token",token);
                    intent.putExtra("channel_name",cName);
                    intent.putExtra("app_id",appId);
                    startActivity(intent);

                }
            }
        });
    }

    private void startRunningLoop(@NonNull String uid){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("available/doctor").child(uid);

        final int[] color = new int[]{
                getColor(R.color.text_color_1),
                getColor(R.color.text_color_2),
                getColor(R.color.text_color_3),
                getColor(R.color.text_color_4),
                getColor(R.color.text_color_5),
                getColor(R.color.text_color_6),
                getColor(R.color.text_color_7)
        };
        binding.tvMessage.setTextColor(color[0]);
        AtomicInteger ind = new AtomicInteger(1);

        runnable = new Runnable() {
            @Override
            public void run() {
                updateTime(ref, isSuccessful -> {
                    binding.tvMessage.setTextColor(color[ind.get() %7]);
                    mHandler.postDelayed(this,UPDATE_TIME_INTERVAL);
                    ind.set((ind.get() + 1) % 7);
                });
            }
        };

        mHandler.postDelayed(runnable,UPDATE_TIME_INTERVAL);
    }

    private void updateTime(DatabaseReference ref, DataListener listener){
        if(inPauseState) { // won't update but loop will run
            listener.onDataSaved(true);
        }
        else{
            Map<String,Object> map = new HashMap<>();
            map.put("lastOnlineTime",ServerValue.TIMESTAMP);
            map.put("inCall",false);

            Doctor doctor = (Doctor)getSp().getMyProfile();
            map.put("speciality",doctor.getSpeciality());
            map.put("imageUrl",doctor.getImageUrl());

            ref.updateChildren(map).addOnCompleteListener(task -> listener.onDataSaved(task.isSuccessful()));
        }
    }

    private interface DataListener{
        void onDataSaved(boolean isSuccessful);
    }

    private interface DataReadListener{
        void onDataRead(Doctor mine);
    }

    private void downloadMyData(@NonNull String uid, DataReadListener listener){
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ds) {
                final Doctor doctor = ds.getValue(Doctor.class);
                listener.onDataRead(doctor);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onDataRead(null);
            }
        });
    }

    private void downloadOnline(){
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("available/doctor");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<AvDoctor> doctors = new ArrayList<>();

                for(DataSnapshot ds : snapshot.getChildren()){

                    final AvDoctor avDoctor = ds.getValue(AvDoctor.class);
                    if(avDoctor == null) continue;

                    final long curTime = System.currentTimeMillis();
                    if(Math.abs(curTime - avDoctor.getLastOnlineTime()) > UPDATE_TIME_INTERVAL_MAX) continue;

                    doctors.add(avDoctor);
                }
                // add some dummy
                for(int i=0; i<0; i++){
                    AvDoctor doctor = new AvDoctor(
                            "dElGdKzqUpYNJF5amDxAMgkBSZ93",
                            2816248,
                            "buu",
                            "Male",
                            "https://firebasestorage.googleapis.com/v0/b/doctorpatient-aebc9.appspot.com/o/profile%2FdElGdKzqUpYNJF5amDxAMgkBSZ93%2Fprofile_pic.jpg?alt=media&token=db421a5c-7b75-4c22-8c0e-afb7d59935d5",
                            "none",
                            3,
                            System.currentTimeMillis(),
                            false
                    );
                    doctors.add(doctor);
                }
                updateAdapter(doctors);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateAdapter(List<AvDoctor> doctors){
        binding.pbHomepage.setVisibility(View.GONE);
        if(doctors.isEmpty()){
            binding.tvMessage.setText(R.string.no_doctor_av);
            binding.tvMessage.setVisibility(View.VISIBLE);
        }
        else{
            binding.tvMessage.setVisibility(View.GONE);
        }

        //adapter.submitList(doctors);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mHandler != null && runnable != null) mHandler.removeCallbacks(runnable);
    }

    @Override
    public void onBackPressed() {
        if(hasDoublePressed) {

            if(mHandler != null && runnable != null) mHandler.removeCallbacks(runnable);

            hasDoublePressed = false;
            if(forceExit) {
                finishAffinity();
            }
            else {
                super.onBackPressed();
            }
        }
        else{
            showSnackBar("Press again to exit");
            hasDoublePressed = true;
            new Handler(Looper.getMainLooper()).postDelayed(() -> hasDoublePressed = false,2000);
        }
    }

    private void showSnackBar(String message){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.cl_homepage),message,Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        inPauseState = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        inPauseState = false;
    }

}
