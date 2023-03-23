package com.ET.telechat.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;


import com.ET.telechat.R;
import com.ET.telechat.databinding.ActivitySignInBinding;


public class SignInActivity extends AppCompatActivity {


    ActivitySignInBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setListensers();
        config();
        setContentView(binding.getRoot());
    }

    private void init()
    {
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
    }

    private void setListensers()
    {
        binding.textCreateNewAccount.setOnClickListener( v -> {
            startActivity(new Intent(getApplicationContext(),SignUpActivity.class));
        });
    }

    private void config()
    {

    }
}