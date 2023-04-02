package com.ET.telechat.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ET.telechat.Adapters.UsersAdapter;
import com.ET.telechat.Listeners.UserListener;
import com.ET.telechat.Models.Users;
import com.ET.telechat.Utilities.Constants;
import com.ET.telechat.Utilities.PreferenceManager;
import com.ET.telechat.databinding.ActivityUserBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends BaseActivity implements UserListener
{

    private ActivityUserBinding binding;

    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        init();
        setListeners();
        config();
        setContentView(binding.getRoot());

    }

    private void init()
    {
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
    }

    private void setListeners()
    {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }


    private void config()
    {
        getUsers();
    }

    private void getUsers()
    {
        loading(true);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener( task ->
                {
                   loading(false);
                   String currentUserID = preferenceManager.getString(Constants.KEY_USER_ID);
                   if(task.isSuccessful() && task.getResult() != null)
                   {
                       List<Users> users = new ArrayList<>();
                       for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult())
                       {
                           if(currentUserID.equals(queryDocumentSnapshot.getId()))
                           {
                               continue;
                           }
                           Users user = new Users(
                                   queryDocumentSnapshot.getString(Constants.KEY_NAME),
                                   queryDocumentSnapshot.getString(Constants.KEY_IMAGE),
                                   queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN),
                                   queryDocumentSnapshot.getString(Constants.KEY_EMAIL),
                                   queryDocumentSnapshot.getId()
                           );
                           users.add(user);

                       }
                       if(users.size() > 0)
                       {
                           UsersAdapter usersAdapter = new UsersAdapter(users,this);
                           binding.usersRecyclerView.setAdapter(usersAdapter);
                           binding.usersRecyclerView.setVisibility(View.VISIBLE);
                       }
                       else
                       {
                            showErrorMessage();
                       }
                   }
                   else
                   {
                       showErrorMessage();
                   }
                });

    }

    private void showErrorMessage()
    {
        binding.textErrorMessage.setText(String.format("%s","No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading)
    {
        if(isLoading)
        {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else
        {
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onUserClicked(Users user)
    {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}