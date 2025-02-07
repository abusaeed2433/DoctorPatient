package com.unknownn.doctorpatient.chat_page.view;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.unknownn.doctorpatient.R;
import com.unknownn.doctorpatient.adapter.MessageAdapter;
import com.unknownn.doctorpatient.databinding.ActivityChatBinding;
import com.unknownn.doctorpatient.fragments.chat_list.model.EachChat;
import com.unknownn.doctorpatient.others.EachMessage;
import com.unknownn.doctorpatient.others.FileListener;
import com.unknownn.doctorpatient.others.FileUploader;
import com.unknownn.doctorpatient.others.ImageUploader;
import com.unknownn.doctorpatient.others.SharedPref;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private String docUid = null, patUid = null;
    private DatabaseReference ref = null;
    private ChildEventListener listener = null;
    private MessageAdapter adapter;

    private ActivityResultLauncher<String> mGetContent;
    private ActivityResultLauncher<String> mGetContentFile;
    private FileUploader uploader = null;
    private String myStrUid = null;
    private final List<EachMessage> messages = new ArrayList<>();
    private ActivityChatBinding binding = null;

    private boolean isProgressShowing = false, amIDoctor = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        EachChat chat = (EachChat) getIntent().getSerializableExtra("each_chat");
        if(chat == null){
            finish();
            return;
        }

        docUid = chat.getDoctorUid();
        patUid = chat.getPatientUid();
        amIDoctor = new SharedPref(this).getMyProfile().isAmIDoctor();

        initializePicker();
        setClickListener();
        startAdapter();
        startUploader();
        loadAllMessages();
    }

    private void initializePicker(){
        mGetContentFile = registerForActivityResult(new ActivityResultContracts.GetContent(), this::uploadFileToStorage);
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), this::uploadToStorage);
    }

    private void setClickListener(){
        binding.ivPickImage.setOnClickListener(v -> {
            if(isProgressShowing){
                showMessage("wait...");
            }
            else {
                mGetContent.launch("image/jpeg");
            }
        });

        binding.ivPickFile.setOnClickListener(v -> {
            if(isProgressShowing){
                showMessage("wait...");
            }
            else {
                mGetContentFile.launch("*/*");
            }
        });

        binding.ivSend.setOnClickListener(v -> {
            if(isProgressShowing) return;
            String message = String.valueOf( binding.editTextSend.getText() ).trim();
            if(message.isEmpty()) return;

            changeSendStatus(false); // show progress
            saveToDatabase(message,true,false,null);
        });
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
        binding.rvMessage.setAdapter(adapter);

    }

    private void startUploader(){
        String pathToFolder = getMessageKey();

        uploader = new FileUploader(this,pathToFolder, new FileListener() {
            @Override
            public void onUploadCompleted(String url,String fileName) {
                saveToDatabase(url,false,true,fileName);
            }

            @Override
            public void onProgressUpdated(int progress) {
                binding.progressIndicator.setProgress(progress);
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

    private void showOrHideProgress(boolean showProgress){
        int width = binding.progressIndicator.getWidth();
        ObjectAnimator animator;

        if(showProgress){
            binding.progressIndicator.setProgress(0); // resetting to default
            animator = ObjectAnimator.ofFloat(binding.progressIndicator,View.X,width,0);
        }
        else{
            animator = ObjectAnimator.ofFloat(binding.progressIndicator,View.X,0,width);
        }
        animator.setDuration(150);
        animator.start();
    }

    private void showFullImage(String url){
        if(url == null) return;

        try{
            Dialog dialog = new Dialog(this);
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
                Glide.with(this)
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
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();

        }catch (Exception ignored){}
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

    private void addToAdapter(EachMessage message){
        messages.add(0,message);
        adapter.notifyItemInserted(0);
        binding.rvMessage.smoothScrollToPosition(0);
    }

    private void startDownloadingFile(@NonNull  String url){
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        }catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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

    private String getMyStrUid(){
        if(myStrUid != null) return myStrUid;


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            myStrUid = user.getUid();
        }
        return myStrUid;
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
        ImageUploader uploader = new ImageUploader(this, uri, messageKey + "/" + uid+"/"+time, url -> {
            if(url == null){
                showMessage("Failed to send image");
            }
            else{
                saveToDatabase(url,false,false,null);
            }
        });
        uploader.startUploading();
    }

    private String getMessageKey(){ // don't change it bro. Same format is used in chat page
        return docUid+"_"+patUid;
    }

    private void saveToDatabase(@NonNull String messageOrUrl, boolean isText, boolean isFile, String fName){
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
                binding.editTextSend.setText(null); // this is triggering textWatcher
            }
            else{
                changeSendStatus(true);
            }
        });
    }

    /**
     * will hide or show progress bar based on hidePB
     * @param hidePB true for hiding progressBar else false
     */
    private void changeSendStatus(boolean hidePB){
        if(hidePB){
            binding.progressBarSend.setVisibility(View.GONE);
            binding.ivSend.setVisibility(View.VISIBLE);
            isProgressShowing = false;
        }
        else{
            isProgressShowing = true;
            binding.progressBarSend.setVisibility(View.VISIBLE);
            binding.ivSend.setVisibility(View.INVISIBLE);
        }

    }

    private void showMessage(String message) {
        runOnUiThread(() -> {
            try{
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }catch (Exception ignored){}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(ref != null && listener != null) ref.removeEventListener(listener);
    }

    @SuppressWarnings("deprecation")
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        if(ref != null && listener != null) ref.removeEventListener(listener);
    }

}
