package com.unknownn.doctorpatient;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.unknownn.doctorpatient.adapter.MessageAdapter;
import com.unknownn.doctorpatient.others.EachMessage;
import com.unknownn.doctorpatient.others.FileListener;
import com.unknownn.doctorpatient.others.FileUploader;
import com.unknownn.doctorpatient.others.ImageUploader;
import com.unknownn.doctorpatient.others.SharedPref;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;


public class VideoActivity extends AppCompatActivity {

    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = { Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA };

    //NEED TO BE CHANGED IF NEW AGORA PROJECT IS USED
    public String appId = "10224527143a457c8efe9a784593e350";

    //user "channel1" as channel name while creating temporary token
    public String channelName = "channel1"; // no need to change

    //GENERATED TOKEN ID, WHICH EXPIRES IN 18HOURS. BE Careful
    public String token = "007eJxTYBC/s+nUEeHrtmKLbjSsPZp0edV0jz6dR6+lX09l2yDQI9mkwGCYaJqUbJZomWKYYmpinGyYZGyRaJaYamyQlpKammRsunhrTHJDICPDUqadTIwMEAjiczAkZyTm5aXmGDIwAABc/SKN";

    private int uid;
    private boolean isJoined = false;

    private RtcEngine agoraEngine;
    private SurfaceView localSurfaceView, remoteSurfaceView;
    private FrameLayout localContainer, remoteContainer;

    private ConstraintLayout clChat;
    private SharedPref sp = null;
    private boolean amIDoctor = false, isMuted = false, isChatVisible = false, isProgressShowing = false,hasDoublePressed = false;
    private ImageView ivMic;

    private TextInputEditText editTextSend;
    private ProgressBar progressBarSend;
    private ImageView ivSend, ivPickFile, ivPickImage, ivChat;
    private RecyclerView rvMessage;

    private String docUid = null, patUid = null;
    private DatabaseReference ref = null;
    private ChildEventListener listener = null;
    private MessageAdapter adapter;

    private ActivityResultLauncher<String> mGetContent;
    private ActivityResultLauncher<String> mGetContentFile;
    private FileUploader uploader = null;
    private String myStrUid = null;
    private final List<EachMessage> messages = new ArrayList<>();
    private LinearProgressIndicator progressIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        localContainer = findViewById(R.id.local_video_view_container);
        remoteContainer = findViewById(R.id.remote_video_view_container);
        ivMic = findViewById(R.id.iv_mic);
        clChat = findViewById(R.id.cl_chat);

        rvMessage = findViewById(R.id.rv_message);
        ivPickFile = findViewById(R.id.iv_pick_file);
        ivPickImage = findViewById(R.id.iv_pick_image);
        ivChat = findViewById(R.id.iv_chat);
        editTextSend = findViewById(R.id.edit_text_send);
        progressBarSend = findViewById(R.id.progress_bar_send);
        ivSend = findViewById(R.id.iv_send);
        progressIndicator = findViewById(R.id.progress_indicator);

        docUid = getIntent().getStringExtra("doctor_uid");
        patUid = getIntent().getStringExtra("patient_uid");
        String appId = getIntent().getStringExtra("app_id");
        String cName = getIntent().getStringExtra("channel_name");
        String token = getIntent().getStringExtra("token");

        if(appId != null && token != null && cName != null){
            this.appId = appId;
            this.token = token;
            this.channelName = cName;
        }

        hideChatFirstTime();

        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }

        uid = getSp().getMyIntegerId();
        amIDoctor = getSp().amIDoctor();

        setupVideoSDKEngine();
        joinChannel();

        initializePicker();
        setClickListener();

        startAdapter();
        startUploader();
        loadAllMessages();

    }

    private SharedPref getSp(){
        if(sp == null){
            sp = new SharedPref(VideoActivity.this);
        }
        return sp;
    }

    private void setClickListener(){
        ivPickImage.setOnClickListener(v -> {
            if(isProgressShowing){
                showMessage("wait...");
            }
            else {
                mGetContent.launch("image/jpeg");
            }
        });

        ivPickFile.setOnClickListener(v -> {
            if(isProgressShowing){
                showMessage("wait...");
            }
            else {
                mGetContentFile.launch("*/*");
            }
        });

        ivSend.setOnClickListener(v -> {
            if(isProgressShowing) return;
            String message = String.valueOf( editTextSend.getText() ).trim();
            if(message.isEmpty()) return;

            changeSendStatus(false); // show progress
            saveToDatabase(message,true,false,null);
        });
    }

    private void initializePicker(){
        mGetContentFile = registerForActivityResult(new ActivityResultContracts.GetContent(), this::uploadFileToStorage);
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), this::uploadToStorage);
    }

    private String getMessageKey(){
        return docUid+"_"+patUid;
    }

    private String getMyStrUid(){
        if(myStrUid != null) return myStrUid;


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            myStrUid = user.getUid();
        }
        return myStrUid;
    }

    private void uploadFileToStorage(Uri uri){
        if(uri == null){
            showMessage("No file selected");
            return;
        }

        String uid = getMyStrUid();
        if(uid == null){
            showMessage("Something went wrong");
            return;
        }
        changeSendStatus(false);
        uploader.uploadFile(uri, uid); // listener is used while creating object
    }

    private void uploadToStorage(Uri uri){
        if(uri == null){
            showMessage("No file selected");
            return;
        }

        String messageKey = getMessageKey();
        String uid = getMyStrUid();
        if(uid == null){
            showMessage("Something went wrong");
            return;
        }
        String time = String.valueOf(System.currentTimeMillis());
        changeSendStatus(false);
        ImageUploader uploader = new ImageUploader(VideoActivity.this, uri, messageKey + "/" + uid+"/"+time, url -> {
            if(url == null){
                showMessage("Failed to send image");
            }
            else{
                saveToDatabase(url,false,false,null);
            }
        });
        uploader.startUploading();
    }

    /**
     * will hide or show progress bar based on hidePB
     * @param hidePB true for hiding progressBar else false
     */
    private void changeSendStatus(boolean hidePB){
        if(hidePB){
            progressBarSend.setVisibility(View.GONE);
            ivSend.setVisibility(View.VISIBLE);
            isProgressShowing = false;
        }
        else{
            isProgressShowing = true;
            progressBarSend.setVisibility(View.VISIBLE);
            ivSend.setVisibility(View.INVISIBLE);
        }

    }

    private void saveToDatabase(@NonNull String messageOrUrl, boolean isText, boolean isFile,String fName){
        HashMap<String,Object> map = new HashMap<>();

        map.put("isDoctor",amIDoctor);

        if(isFile){
            map.put("text",fName);
            map.put("url",messageOrUrl);
        }
        else{
            if(isText) map.put("text",messageOrUrl);
            else map.put("url",messageOrUrl);
        }

        map.put("isFile",isFile);

        String messageKey = getMessageKey();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("message").child(messageKey);

        ref.push().setValue(map).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                changeSendStatus(true); // pushId is null here
                editTextSend.setText(null); // this is triggering textWatcher
            }
            else{
                changeSendStatus(true);
            }
        });
    }

    private boolean checkSelfPermission() {
        return ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED;
    }

    private void showMessage(String message) {
        runOnUiThread(() -> {
            try{
                Toast.makeText(VideoActivity.this, message, Toast.LENGTH_SHORT).show();
            }catch (Exception ignored){}
        });
    }

    private void setupVideoSDKEngine() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = appId;
            config.mEventHandler = mRtcEventHandler;
            agoraEngine = RtcEngine.create(config);
            // By default, the video module is disabled, call enableVideo to enable it.
            agoraEngine.enableVideo();
        } catch (Exception e) {
            showMessage(e.toString());
        }
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // Listen for the remote host joining the channel to get the uid of the host.
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(() -> setupRemoteVideo(uid));
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            isJoined = true;
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            runOnUiThread(() -> {
                remoteSurfaceView.setVisibility(View.GONE);
                String text = amIDoctor ? " patient" : " doctor";
                showMessage("Call ended by"+text);
            });
        }
    };

    private void setupRemoteVideo(int uid) {
        remoteSurfaceView = new SurfaceView(getBaseContext());
        remoteSurfaceView.setZOrderMediaOverlay(false);
        remoteContainer.addView(remoteSurfaceView);
        agoraEngine.setupRemoteVideo(new VideoCanvas(remoteSurfaceView, VideoCanvas.RENDER_MODE_FIT, uid));
        // Display RemoteSurfaceView.
        remoteSurfaceView.setVisibility(View.VISIBLE);
    }

    private void setupLocalVideo() {
        localSurfaceView = new SurfaceView(getBaseContext());
        localContainer.addView(localSurfaceView);
        localSurfaceView.setZOrderMediaOverlay(true);
        // Pass the SurfaceView object to Agora so that it renders the local video.
        agoraEngine.setupLocalVideo(new VideoCanvas(localSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    public void joinChannel() {
        if (checkSelfPermission()) {
            ChannelMediaOptions options = new ChannelMediaOptions();

//            // For a Video call, set the channel profile as COMMUNICATION.
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;
//            // Set the client role as BROADCASTER or AUDIENCE according to the scenario.
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;

            // Display LocalSurfaceView.
            setupLocalVideo();
            localSurfaceView.setVisibility(View.VISIBLE);
            // Start local preview.
            agoraEngine.startPreview();
            // Join the channel with a temp token.
            // You need to specify the user ID yourself, and ensure that it is unique in the channel.
            agoraEngine.joinChannel(token, channelName, uid, options);
            removeAllNodesIf();
        }
        else {
            Toast.makeText(getApplicationContext(), "Permissions denied. Restart app", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeAllNodesIf(){ // ifDoctor
        if(!amIDoctor) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) return;

        String uid = user.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("request").child(uid);
        ref.removeValue();
    }

    protected void onDestroy() {
        super.onDestroy();
        agoraEngine.stopPreview();
        agoraEngine.leaveChannel();

        // Destroy the engine in a sub-thread to avoid congestion
        new Thread(() -> {
            RtcEngine.destroy();
            agoraEngine = null;
        }).start();
    }

    private void hideChatFirstTime(){
        clChat.post(() -> {
            int width = clChat.getWidth();
            ObjectAnimator animator = ObjectAnimator.ofFloat(clChat,"translationX",0,width);
            animator.setDuration(200);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    clChat.setVisibility(View.VISIBLE);
                }
            });
            animator.start();
        });

        progressIndicator.post(new Runnable() {
            @Override
            public void run() {
                int width = progressIndicator.getWidth();
                ObjectAnimator animator = ObjectAnimator.ofFloat(progressIndicator,View.X,0,width);
                animator.setDuration(200);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        progressIndicator.setVisibility(View.VISIBLE);
                    }
                });
                animator.start();
            }
        });
    }

    private void showOrHideChat(boolean showChat){
        int width = clChat.getWidth();
        ObjectAnimator animator;

        if(showChat){
            animator = ObjectAnimator.ofFloat(clChat,"translationX",width,0);
        }
        else{
            animator = ObjectAnimator.ofFloat(clChat,"translationX",0,width);
        }
        animator.setDuration(450);
        if(showChat){
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ivChat.setImageResource(R.drawable.ic_baseline_chat_24);
                }
            });
        }
        animator.start();
    }

    private void showOrHideProgress(boolean showProgress){
        int width = progressIndicator.getWidth();
        ObjectAnimator animator;

        if(showProgress){
            progressIndicator.setProgress(0); // resetting to default
            animator = ObjectAnimator.ofFloat(progressIndicator,View.X,width,0);
        }
        else{
            animator = ObjectAnimator.ofFloat(progressIndicator,View.X,0,width);
        }
        animator.setDuration(150);
        animator.start();
    }

    private void startUploader(){
        String pathToFolder = getMessageKey();

        uploader = new FileUploader(VideoActivity.this,pathToFolder, new FileListener() {
            @Override
            public void onUploadCompleted(String url,String fileName) {
                saveToDatabase(url,false,true,fileName);
            }

            @Override
            public void onProgressUpdated(int progress) {
                progressIndicator.setProgress(progress);
            }

            @Override
            public void onErrorOccurred(String error) {
                showMessage(error);
                changeSendStatus(true);
            }

            @Override
            public void showIndicator() {
                showOrHideProgress(true);
            }

            @Override
            public void hideIndicator() {
                showOrHideProgress(false);
            }
        });
    }

    private void loadAllMessages(){
        String messageKey = getMessageKey();

        ref = FirebaseDatabase.getInstance().getReference()
                .child("message").child(messageKey);
        listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                String pushId, text = null, url = null;

                pushId = String.valueOf(snapshot.getKey());
                if(snapshot.child("text").exists()){ text = String.valueOf(snapshot.child("text").getValue()); }
                if(snapshot.child("url").exists()){ url = String.valueOf(snapshot.child("url").getValue()); }

                String val = String.valueOf(snapshot.child("isDoctor").getValue());
                String file = String.valueOf(snapshot.child("isFile").getValue());

                boolean isDoctor = false, isFile = false;
                try{
                    isDoctor = Boolean.parseBoolean(val);
                    isFile = Boolean.parseBoolean(file);
                }catch (Exception ignored){}

                EachMessage message = new EachMessage(pushId,text,url,isDoctor,isFile);
                addToAdapter(message);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        ref.addChildEventListener(listener);
    }

    private void startAdapter(){
        adapter = new MessageAdapter(this, amIDoctor, message -> {
            // only process if chat is a file
            if(message.isFile()){
                if(message.isUrlValid()){
                    startDownloadingFile(message.getUrl());
                }
                else{
                    showMessage("Invalid file");
                }
            }
            else{
                if(message.isUrlValid()){//show image in big screen
                    showFullImage(message.getUrl());
                }
            }
        });
        adapter.submitList(messages);
        rvMessage.setAdapter(adapter);

    }

    private void showFullImage(String url){
        if(url == null) return;

        try{
            Dialog dialog = new Dialog(VideoActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.photo_viewer_layout);

            Window window = dialog.getWindow();
            if(window != null){
                window.setBackgroundDrawableResource(android.R.color.transparent);
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(true);

            ImageView ivClosePhoto = dialog.findViewById(R.id.iv_close_photo);
            ImageView ivDownloadPhoto = dialog.findViewById(R.id.iv_download_photo);
            PhotoView photoView = dialog.findViewById(R.id.photo_view);

            try {
                Glide.with(VideoActivity.this)
                        .load(url)
                        .placeholder(R.drawable.ic_baseline_image_24)
                        .dontTransform()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(photoView);
            }catch (Exception ignored){}

            ivClosePhoto.setOnClickListener(view -> dialog.dismiss());
            ivDownloadPhoto.setOnClickListener(view->{
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                }catch (Exception e) {
                    Toast.makeText(VideoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();

        }catch (Exception ignored){}
    }

    private void startDownloadingFile(@NonNull  String url){
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        }catch (Exception e) {
            Toast.makeText(VideoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void addToAdapter(EachMessage message){
        messages.add(0,message);
        adapter.notifyItemInserted(0);
        rvMessage.smoothScrollToPosition(0);
        if(!isChatVisible){
            ivChat.setImageResource(R.drawable.ic_baseline_mark_unread_chat_alt_24);
        }
    }

    public void onCallEndClicked(View view) {
        try{
            if (!isJoined) {
                showMessage("You are not in a call");
            }
            else {
                agoraEngine.leaveChannel();
                // Stop remote video rendering.
                if (remoteSurfaceView != null) remoteSurfaceView.setVisibility(View.GONE);
                // Stop local video rendering.
                if (localSurfaceView != null) localSurfaceView.setVisibility(View.GONE);
                isJoined = false;

                if(amIDoctor) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        String uid = user.getUid();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                                .child("available/doctor").child(uid).child("inCall");
                        ref.setValue(false);
                    }
                }

                if(ref != null && listener != null) ref.removeEventListener(listener);
                showMessage("Ending call...");
                new Handler(Looper.getMainLooper()).postDelayed(VideoActivity.super::onBackPressed,2000);
            }

        }catch (Exception ignored){}
    }


    public void onMicClicked(View view) {
        if(agoraEngine == null){
            showMessage(getString(R.string.please_wait));
            return;
        }

        if(isMuted){
            ivMic.setImageResource(R.drawable.ic_baseline_mic_24);
        }
        else{
            ivMic.setImageResource(R.drawable.ic_baseline_mic_off_24);
        }

        isMuted = !isMuted;
        agoraEngine.muteLocalAudioStream(isMuted);
    }

    public void onChatClicked(View view) {
        showOrHideChat(!isChatVisible);
        isChatVisible = !isChatVisible;
    }

    @Override
    public void onBackPressed() {
        try {
            if (isJoined) {
                showMessage("Disconnect call first...");
                return;
            }

            if (hasDoublePressed) {
                if (ref != null && listener != null) ref.removeEventListener(listener);
                hasDoublePressed = false;
                super.onBackPressed();
            } else {
                showMessage("Press again to exit");
                hasDoublePressed = true;
                new Handler(Looper.getMainLooper()).postDelayed(() -> hasDoublePressed = false, 2000);
            }
        }catch (Exception ignored){}
    }

}
