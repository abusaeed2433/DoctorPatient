package com.unknownn.doctorpatient.doctor_chat_list.view;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.unknownn.doctorpatient.databinding.ActivityDoctorChatListBinding;
import com.unknownn.doctorpatient.fragments.chat_list.view.FragmentChatList;

public class DoctorChatListActivity extends AppCompatActivity {

    private ActivityDoctorChatListBinding binding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoctorChatListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setFragment();
    }

    private void setFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(binding.fragmentContainerView.getId(), new FragmentChatList());
        transaction.commit();
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
    }

}
