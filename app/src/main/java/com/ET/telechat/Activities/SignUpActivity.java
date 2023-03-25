package com.ET.telechat.Activities;

import static com.ET.telechat.Utilities.UIHelpers.showToast;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ET.telechat.Models.Users;
import com.ET.telechat.Utilities.Constants;
import com.ET.telechat.Utilities.PreferenceManager;
import com.ET.telechat.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private FirebaseAuth auth;
    private String encodedImage;
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
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
    }

    private void setListensers()
    {
        binding.textSignIn.setOnClickListener( v -> {
            onBackPressed();
        });

        binding.buttonSignUp.setOnClickListener( v -> {
            if(isValidSignUpDetails())
            {
                signUp();
            }
        });

        binding.layoutImage.setOnClickListener( v ->
        {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void config()
    {

    }


    private void signUp()
    {
        loading(true);
        auth.createUserWithEmailAndPassword(binding.inputEmail.getText().toString(), binding
                .inputPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    Users user = new Users(binding.inputName.getText().toString(),encodedImage,binding.inputEmail.getText().toString());
                    database.collection(Constants.KEY_COLLECTION_USERS)
                            .add(user)
                            .addOnSuccessListener(documentReference -> {
                                loading(false);
                                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                                preferenceManager.putString(Constants.KEY_USER_ID,documentReference.getId());
                                preferenceManager.putString(Constants.KEY_NAME,binding.inputName.getText().toString());
                                preferenceManager.putString(Constants.KEY_EMAIL,binding.inputEmail.getText().toString());
                                preferenceManager.putString(Constants.KEY_IMAGE,encodedImage);
                                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }).addOnFailureListener(exception -> {
                                loading(false);
                                showToast(getApplicationContext(),exception.getMessage());
                            });

                  showToast( getApplicationContext(),"User Created Successfully");
                } else {
                    showToast(getApplicationContext(), task.getException().getMessage());
                }
            }
        });


    }

    private String encodeImage(Bitmap bitmap)
    {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth/bitmap.getWidth();
        Bitmap previewBitmap = bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK)
                {
                    if(result.getData() != null)
                    {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.imageText.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        }
                        catch (FileNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }

    );

    private Boolean isValidSignUpDetails()
    {

        if (encodedImage == null) {
            showToast(getApplicationContext(),"Select profile image");
            return false;
        }

        if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast(getApplicationContext(),"Enter name");
            return false;
        }

        if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast(getApplicationContext(),"Enter email");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast(getApplicationContext(),"Enter valid email");
            return false;
        }

        if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast(getParent().getApplicationContext(), "Enter password");
            return false;
        }

        if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast(getApplicationContext(),"Enter password");
            return false;
        }

        if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast(getApplicationContext(),"Enter Confirm password");
            return false;
        }

        if(!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString()))
        {
            showToast(getApplicationContext(),"Password & confirm password must be same");
            return  false;
        }
        return true;
    }

    private void loading(Boolean isLoading)
    {
        if(isLoading)
        {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else
        {
            binding.buttonSignUp.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onBackPressed() {
        if( binding.progressBar.getVisibility() != View.VISIBLE)
        {
            super.onBackPressed();
        }
    }
}