package com.ET.telechat.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.ET.telechat.Models.Users;
import com.ET.telechat.R;
import com.ET.telechat.Utilities.Constants;
import com.ET.telechat.databinding.ActivityChatBinding;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    private Users receiverUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setListeners();
        setContentView(binding.getRoot());
        config();
        loadReceiverDetails();
    }

    private void init()
    {
        binding = ActivityChatBinding.inflate(getLayoutInflater());
    }

    private void setListeners()
    {
        binding.imageBack.setOnClickListener( v -> onBackPressed());
    }

    private void config()
    {

    }

    private void loadReceiverDetails()
    {
        receiverUser = (Users)  getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.getName());
    }


}