package com.ET.telechat.Activities;

import static com.ET.telechat.Utilities.UIHelpers.showToast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;


import com.ET.telechat.R;
import com.ET.telechat.Utilities.Constants;
import com.ET.telechat.Utilities.PreferenceManager;
import com.ET.telechat.databinding.ActivitySignInBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class SignInActivity extends AppCompatActivity {


    ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private FirebaseAuth auth;
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

        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN))
        {
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
    }

    private void setListensers()
    {
        binding.textCreateNewAccount.setOnClickListener( v -> {
            startActivity(new Intent(getApplicationContext(),SignUpActivity.class));
        });
        binding.buttonSignIn.setOnClickListener( v -> {
            if(isValidSignInDetails())
            {
                signIn();
            }
        });


    }

    private void config()
    {

    }



    private void signIn()
    {
        loading(true);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_NAME,binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener( task -> {
                   if(task.isSuccessful() && task.getResult() != null
                   && task.getResult().getDocuments().size() > 0)
                   {
                       DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                       preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                       preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                       preferenceManager.putString(Constants.KEY_NAME,documentSnapshot.getString(Constants.KEY_NAME));
                       preferenceManager.putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));
                       Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                       intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                       startActivity(intent);
                   }else
                   {
                       loading(false);
                       showToast(getApplicationContext(),"Unable to sign in");
                   }
                });
    }

    private void loading(Boolean isLoading)
    {
        if(isLoading)
        {
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else
        {
            binding.buttonSignIn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private Boolean isValidSignInDetails()
    {
        if(binding.inputEmail.getText().toString().trim().toString().isEmpty())
        {
            showToast(getApplicationContext(),"Enter email");
            return false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches())
        {
            showToast(getApplicationContext(),"Enter valid email");
            return false;
        }
        if(binding.inputPassword.getText().toString().trim().isEmpty())
        {
            showToast(getApplicationContext(),"Enter password");
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if( binding.progressBar.getVisibility() != View.VISIBLE)
        {
            super.onBackPressed();
        }
    }
}