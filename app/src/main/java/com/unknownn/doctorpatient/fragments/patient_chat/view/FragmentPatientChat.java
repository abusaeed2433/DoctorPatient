package com.unknownn.doctorpatient.fragments.patient_chat.view;

import android.app.Activity;
import android.content.Intent;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.unknownn.doctorpatient.R;
import com.unknownn.doctorpatient.chat_page.view.ChatActivity;
import com.unknownn.doctorpatient.databinding.FragmentPatientChatBinding;
import com.unknownn.doctorpatient.fragments.patient_chat.model.EachChat;
import com.unknownn.doctorpatient.others.SharedPref;

import java.util.ArrayList;
import java.util.List;

public class FragmentPatientChat extends Fragment {


    private ChatAdapter chatAdapter = null;
    private FragmentPatientChatBinding binding = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPatientChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        startAdapter();
        downloadChatList();
    }

    private void startAdapter(){
        Activity activity = getActivity();
        if(activity == null) return;

        chatAdapter = new ChatAdapter(activity, false, item -> {
            Intent intent = new Intent(activity, ChatActivity.class);
            intent.putExtra("each_chat", item);
            startActivity(intent);
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        boolean isLargeDevice = getResources().getBoolean(R.bool.isLargeDevice);
        binding.rvChat.setLayoutManager(
                new GridLayoutManager(
                        activity,
                        (isLargeDevice ? 2 : 1),
                        RecyclerView.VERTICAL,
                        false
                )
        );

        binding.rvChat.setAdapter(chatAdapter);
    }

    private void downloadChatList(){
        final Activity activity = getActivity();
        if(activity == null) return;

        final String patUid = new SharedPref(activity).getMyProfile().getUid();
        final Query query = FirebaseDatabase.getInstance().getReference("chat_heads")
                .orderByKey()
                .endAt(patUid);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final List<EachChat> list = new ArrayList<>();

                for(DataSnapshot ds : snapshot.getChildren()){
                    EachChat chat = ds.getValue(EachChat.class);
                    if(chat == null) continue;

                    list.add(chat);
                }
                updateAdapter(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateAdapter(new ArrayList<>());
            }
        });
    }

    private void updateAdapter(List<EachChat> chatList){
        chatList.sort((o1, o2) -> Long.compare(o2.getLastMessageTime(), o1.getLastMessageTime()));
        binding.progressBar.setVisibility(View.GONE);

        if(chatList.isEmpty()){
            binding.tvNotFound.setVisibility(View.VISIBLE);
        }
        else{
            binding.tvNotFound.setVisibility(View.GONE);
        }

        chatAdapter.submitList(chatList);
    }

}
