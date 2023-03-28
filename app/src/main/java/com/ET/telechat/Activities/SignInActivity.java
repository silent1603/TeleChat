package com.ET.telechat.Activities;

import static com.ET.telechat.Utilities.UIHelpers.showToast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;


import com.ET.telechat.Utilities.Constants;
import com.ET.telechat.Utilities.PreferenceManager;
import com.ET.telechat.databinding.ActivitySignInBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


public class SignInActivity extends AppCompatActivity
{

    ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        init();
        setListensers();
        config();
        setContentView(binding.getRoot());
    }

    private void init()
    {

        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            Map<String, String> data = new HashMap<String, String>();
            data.put(Constants.KEY_EMAIL, user.getEmail());
            Log.d("TELECHAT",user.getEmail().toString());
            signInWithUserData(data);

        }


        binding = ActivitySignInBinding.inflate(getLayoutInflater());
    }

    private void setListensers()
    {
        binding.textCreateNewAccount.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
        });
        binding.buttonSignIn.setOnClickListener(v -> {
            if (isValidSignInDetails()) {
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
        auth.signInWithEmailAndPassword(binding.inputEmail.getText().toString(), binding.inputPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                loading(false);
                if (task.isSuccessful())
                {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
                    signInWithUserData(data);

                }
                else
                {
                    showToast(getApplicationContext(), task.getException().getMessage());
                }


            }
        });

    }

    private void signInWithUserData(Map<String, String> fields)
    {
        Query query = database.collection(Constants.KEY_COLLECTION_USERS);
        Iterator<Map.Entry<String, String>> iterator = fields.entrySet().iterator();

        Map.Entry<String, String> entry = null;

        while(iterator.hasNext())
        {
            entry = iterator.next();
            query =  query.whereEqualTo(entry.getKey(),entry.getValue());
            Log.d("TELECHAT",entry.getKey()+"_"+entry.getValue());
        }
        Log.d("TELECHAT",query.toString());

        if (query != null) {
                query.get().addOnCompleteListener(taskFireStore ->
                {
                    Log.d("TELECHAT", taskFireStore.isSuccessful() + "_" + (taskFireStore.getResult() != null) + "_" + (taskFireStore.getResult().getDocuments().size() > 0));
                    if (taskFireStore.isSuccessful() && taskFireStore.getResult() != null
                            && taskFireStore.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = taskFireStore.getResult().getDocuments().get(0);
                        Map<String, Object> userData = documentSnapshot.getData();
                        setPreferenceData(documentSnapshot,userData);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    } else {
                        showToast(getApplicationContext(), "Unable to sign in");


                    }
                }).addOnFailureListener( taskFailed -> {
                    Log.d("TELECHAT", taskFailed.getMessage().toString());
                });
        } else
        {


        }

}

    private void setPreferenceData(DocumentSnapshot document, Map<String, Object> userData)

    {
        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
        preferenceManager.putString(Constants.KEY_USER_ID,document.getId());
        Log.d("ERROR",document.getId());
        preferenceManager.putString(Constants.KEY_NAME,userData.get("name").toString());
        preferenceManager.putString(Constants.KEY_EMAIL,userData.get("email").toString());
        preferenceManager.putString(Constants.KEY_IMAGE,userData.get("profilePic").toString());
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
    public void onBackPressed()
    {
        if( binding.progressBar.getVisibility() != View.VISIBLE)
        {
            super.onBackPressed();
        }
    }
}