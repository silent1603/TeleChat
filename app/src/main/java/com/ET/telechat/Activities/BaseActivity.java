package com.ET.telechat.Activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ET.telechat.Utilities.Constants;
import com.ET.telechat.Utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseActivity extends AppCompatActivity {

    private DocumentReference documentReference;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init()
    {
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
    }

    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update(Constants.KEY_AVAILABILITY,0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update(Constants.KEY_AVAILABILITY,1);

    }
    
}
