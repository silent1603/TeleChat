package com.ET.telechat.Activities;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.ET.telechat.R;
import com.ET.telechat.databinding.ActivityMainBinding;
import com.ET.telechat.databinding.ActivitySignUpBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setContentView(binding.getRoot());
        setListenser();
        config();
    }

    private void init()
    {

        auth = FirebaseAuth.getInstance();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
    }

    private void setListenser()
    {

    }

    private void config()
    {

    }



    @Override
    public void onBackPressed() {
        if(auth.getCurrentUser() != null)
        {

        }
    }
}