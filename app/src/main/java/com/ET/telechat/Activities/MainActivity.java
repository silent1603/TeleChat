package com.ET.telechat.Activities;

import static com.ET.telechat.Utilities.UIHelpers.showToast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.ET.telechat.R;
import com.ET.telechat.Utilities.Constants;
import com.ET.telechat.Utilities.PreferenceManager;
import com.ET.telechat.databinding.ActivityMainBinding;
import com.ET.telechat.databinding.ActivitySignUpBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseMessaging messagging;
    private FirebaseFirestore database;
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private String userName;
    private String userImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setContentView(binding.getRoot());
        setupUI();
        setListenser();
        config();
    }

    private void init()
    {

        auth = FirebaseAuth.getInstance();
        messagging = FirebaseMessaging.getInstance();
        database = FirebaseFirestore.getInstance();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
    }

    private void setupUI()
    {
        userName = preferenceManager.getString(Constants.KEY_NAME);
        userImage = preferenceManager.getString(Constants.KEY_IMAGE);
        binding.textName.setText(userName);
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);


    }


    private void updateToken(String token)
    {
        database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN,token)
                .addOnSuccessListener(unused -> showToast(getApplicationContext(),"Token update successfully"))
                .addOnFailureListener(e -> {
                    showToast(getApplicationContext(), "Unable to update token" + e.getMessage());
                    Log.d("ERROR", e.getMessage());
                    });
    }


    private void getToken()
    {
      messagging.getToken().addOnSuccessListener(this::updateToken);
    }

    private void setListenser()
    {
        binding.imageSetting.setOnClickListener(v -> signOut());
    }

    private void config()
    {
        getToken();
    }



    @Override
    public void onBackPressed() {
        if(auth.getCurrentUser() != null)
        {

        }
    }

    private void signOut()
    {
        showToast(getApplicationContext(),"Signing out ...");
        String value = preferenceManager.getString(Constants.KEY_USER_ID);
        Log.d("USER_ID",value);
        auth.signOut();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        HashMap<String,Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates).addOnSuccessListener(
                unused -> {
                    if(auth.getCurrentUser() == null )
                    {
                        preferenceManager.clear();
                    }
                    finish();
                }).addOnFailureListener(e -> showToast(getApplicationContext(),"unable to sign out"));

        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}