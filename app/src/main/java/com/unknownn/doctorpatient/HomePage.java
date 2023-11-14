package com.unknownn.doctorpatient;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import com.unknownn.doctorpatient.adapter.AvAdapter;
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

public class HomePage extends AppCompatActivity implements SnapListener {

    private static final int UPDATE_TIME_INTERVAL = 10000;
    public static final int UPDATE_TIME_INTERVAL_MAX = 12000;
    private ProgressBar progressBar;
    private TextView tvMessage;
    private RecyclerView recyclerView;
    private Button buttonExit;
    private AvAdapter adapter;

    private boolean amIDoctor = false, forceExit = false, hasDoublePressed = false;
    private SharedPref sp;
    private final Handler mHandler = new Handler();
    private Runnable runnable = null;
    private Dialog mainDialog, callingDialog;
    private TextView tvProgress;
    private boolean isCallingShowing = false, inPauseState = false, hasClickedOne = false, wasInfoAdded = true;
    private Toast mToast = null;
    private String appId = null, token = null, cName = null;
    private final String appNo = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        amIDoctor = getSp().amIDoctor();
        forceExit = getIntent().getBooleanExtra("force_exit",false);

        progressBar = findViewById(R.id.pb_homepage);
        tvMessage = findViewById(R.id.tv_message);
        recyclerView = findViewById(R.id.recycler_view);
        buttonExit = findViewById(R.id.button_exit);

        retrieveBasicInfo();
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

    private void retrieveBasicInfo(){
        wasInfoAdded = getSp().wasMyInfoAdded(amIDoctor);

        if(!wasInfoAdded){
            requestToAddInfo();
        }

    }

    private void downloadToken(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("token").child(appNo);
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
        buttonExit.setOnClickListener(v -> new AlertDialog.Builder(HomePage.this)
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
        adapter = new AvAdapter(doctor -> {
            if(!hasClickedOne){
                hasClickedOne = true;
                handleRest(doctor);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private SharedPref getSp(){
        if(sp == null) {
            sp = new SharedPref(this);
        }
        return sp;
    }

    private void showInTv(String message){
        tvMessage.setText(message);
    }

    private void handleRest(AvDoctor doctor){

        if(!wasInfoAdded){
            requestToAddInfo();
            return;
        }

        if(doctor == null) {
            hasClickedOne = false;
            return;
        }

        if(doctor.isInCall()){
            showSnackBar("Busy in another call");
            hasClickedOne = false;
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
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

        String uid = user.getUid();
        showProgress(null,doctor.getUid(),uid);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("request").child(doctor.getUid()).child(uid);



        Patient mine = getSp().getMyProfile();
        if(mine == null){
            showSafeToast("Your info isn't found");
            hasClickedOne = false;
            showSnackBar(getString(R.string.not_available));
            return;
        }

        Map<String, Object> map = mine.getSavableMap();
        int myId = getSp().getMyId();
        map.put("id",myId);

        ref.setValue(map).addOnCompleteListener(task -> {
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

    private void requestToAddInfo(){
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(HomePage.this);
            builder.setTitle("Missing necessary info");
            builder.setMessage("You need to add more details about you before calling a doctor");
            builder.setNegativeButton(R.string.later,null);
            builder.setPositiveButton(R.string.add_now, (dialog, which) -> {
                Intent intent = new Intent(HomePage.this,PatientProfile.class);
                startActivity(intent);
            });
            builder.setCancelable(false);
            builder.show();
        }
        catch (Exception ignored){}
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        try{
            wasInfoAdded = getSp().wasMyInfoAdded(amIDoctor);
        }catch (Exception ignored){}
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
        if(amIDoctor){
            menu.getItem(0).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(amIDoctor){
            menu.getItem(0).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.bar_profile){
            if(amIDoctor){
                showSafeToast("Profile is only for patient");
            }
            else{
                Intent intent = new Intent(HomePage.this,PatientProfile.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }
        else if(id == R.id.log_out){
            try {
                new AlertDialog.Builder(HomePage.this)
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

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(this,gso);
        client.signOut();
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(HomePage.this,CreateAccount.class);
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
                Intent intent = new Intent(HomePage.this, VideoActivity.class);
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            showSnackBar(getString(R.string.something_went_wrong));
            tvMessage.setText(R.string.restart_app);
            progressBar.setVisibility(View.GONE);
            return;
        }

        String uid = user.getUid();

        downloadMyData(uid, mine -> {
            if(mine == null){
                tvMessage.setText(R.string.failed_to_fetch_data);
                progressBar.setVisibility(View.GONE);
            }
            else{
                Map<String,Object> map = new HashMap<>();
                map.put("name",mine.getName());
                map.put("id",mine.getId());
                map.put("lastOnline", ServerValue.TIMESTAMP);
                map.put("inCall", false);

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("available/doctor").child(uid);

                ref.updateChildren(map).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        progressBar.setVisibility(View.GONE);
                        tvMessage.setText(R.string.notify_message);
                        addRequestListener(uid);
                        startRunningLoop(uid);
                    }
                    else{
                        progressBar.setVisibility(View.GONE);
                        tvMessage.setVisibility(View.GONE);
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

        int id = -1;
        String sid = String.valueOf(snapshot.child("id").getValue());
        try{ id = Integer.parseInt(sid); }catch (Exception ignored){}

        String uid = String.valueOf(snapshot.getKey());
        String name = String.valueOf(snapshot.child("name").getValue());
        String age = String.valueOf(snapshot.child("age").getValue());
        String weight = String.valueOf(snapshot.child("weight").getValue());
        String hFt = String.valueOf(snapshot.child("heightFt").getValue());
        String hIn = String.valueOf(snapshot.child("heightIn").getValue());
        String desc = String.valueOf(snapshot.child("desc").getValue());
        String gender = String.valueOf(snapshot.child("gender").getValue());
        Patient patient = new Patient(uid,name,age,weight,gender,hFt,hIn,desc);

        if(id == -1 || isCallingShowing){
            return;
        }

        showCallingDialog(patient,snapshot);
    }

    private void showCallingDialog(final Patient patientInfo, DataSnapshot snapshot){
        isCallingShowing = true;

        callingDialog = new Dialog(HomePage.this);
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
                    Intent intent = new Intent(HomePage.this, VideoActivity.class);
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
        tvMessage.setTextColor(color[0]);
        AtomicInteger ind = new AtomicInteger(1);

        runnable = new Runnable() {
            @Override
            public void run() {
                updateTime(ref, isSuccessful -> {
                    tvMessage.setTextColor(color[ind.get() %7]);
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
            map.put("lastOnline",ServerValue.TIMESTAMP);
            map.put("inCall",false);

            ref.updateChildren(map).addOnCompleteListener(task -> listener.onDataSaved(task.isSuccessful()));
        }
    }

    private interface DataListener{
        void onDataSaved(boolean isSuccessful);
    }

    private interface DataReadListener{
        void onDataRead(Doctor mine);
    }

    private void downloadMyData(@NonNull String uid,DataReadListener listener){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ds) {
                String name = String.valueOf(ds.child("name").getValue());
                String sid = String.valueOf(ds.child("id").getValue());
                String uid = String.valueOf(ds.getKey());

                int id = -1;
                try{
                    id = Integer.parseInt(sid);
                }catch (Exception ignored){}

                Doctor doctor = new Doctor(name,uid,id);
                listener.onDataRead(doctor);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onDataRead(null);
            }
        });
    }

    private void downloadOnline(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("available/doctor");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<AvDoctor> doctors = new ArrayList<>();

                for(DataSnapshot ds : snapshot.getChildren()){
                    String name = String.valueOf(ds.child("name").getValue());
                    String sid = String.valueOf(ds.child("id").getValue());
                    String lot = String.valueOf(ds.child("lastOnline").getValue());
                    String call = String.valueOf(ds.child("inCall").getValue());
                    String uid = String.valueOf(ds.getKey());

                    int id = -1;
                    long timestamp = 0;
                    boolean inCall = false;
                    try{
                        id = Integer.parseInt(sid);
                        timestamp = Long.parseLong(lot);
                        inCall = Boolean.parseBoolean(call);
                    }catch (Exception ignored){}

                    long curTime = System.currentTimeMillis();
                    System.out.println("printing_time ->  " +curTime+" - "+timestamp + " = " + (curTime-timestamp));
                    if(id == -1 || Math.abs(curTime-timestamp) > UPDATE_TIME_INTERVAL_MAX) continue;

                    AvDoctor doctor = new AvDoctor(name,uid,id,timestamp,inCall);

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
        progressBar.setVisibility(View.GONE);
        if(doctors.isEmpty()){
            tvMessage.setText(R.string.no_doctor_av);
            tvMessage.setVisibility(View.VISIBLE);
        }
        else{
            tvMessage.setVisibility(View.GONE);
        }

        adapter.submitList(doctors);
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
