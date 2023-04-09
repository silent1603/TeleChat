package com.ET.telechat.Activities;

import static com.ET.telechat.Utilities.UIHelpers.showToast;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.ET.telechat.Adapters.RecentConversationsAdapter;
import com.ET.telechat.Listeners.ConversionListener;
import com.ET.telechat.Models.ChatMessage;
import com.ET.telechat.Models.Users;
import com.ET.telechat.Utilities.Constants;
import com.ET.telechat.Utilities.PreferenceManager;
import com.ET.telechat.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionListener
{
    private FirebaseAuth auth;
    private FirebaseMessaging messagging;
    private FirebaseFirestore database;
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private String userName;

    private List<ChatMessage> conversations;
    private RecentConversationsAdapter conversationsAdapter;


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
        conversations =  new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversations,this);
        binding.conversationRecyclerView.setAdapter(conversationsAdapter);
    }

    private void setupUI()
    {
        userName = preferenceManager.getString(Constants.KEY_NAME);
        binding.textName.setText(userName);
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);


    }


    private void updateToken(String token)
    {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN,token);
        database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN,token)
                .addOnFailureListener(e -> {
                    showToast(getApplicationContext(), "Unable to update token" + e.getMessage());

                    });
    }

    private void listenConversations()
    {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection((Constants.KEY_COLLECTION_CONVERSATIONS))
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null)
        {
            return ;
        }
        if(value != null)
        {
            for(DocumentChange documentChange: value.getDocumentChanges())
            {
                String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);

                if(documentChange.getType() == DocumentChange.Type.ADDED)
                {

                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setSenderId(senderId);
                    chatMessage.setReceiverId(receiverId);
                    if(preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId))
                    {
                        chatMessage.setConversionImage(documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE));
                        chatMessage.setConversionName(documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME));
                        chatMessage.setConversionId(documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID));
                    }
                    else
                    {
                        chatMessage.setConversionImage(documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE));
                        chatMessage.setConversionName(documentChange.getDocument().getString(Constants.KEY_SENDER_NAME));
                        chatMessage.setConversionId(documentChange.getDocument().getString(Constants.KEY_SENDER_ID));
                    }
                    chatMessage.setMessage(documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGGE));
                    chatMessage.setDataObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    conversations.add(chatMessage);
                }
                else if(documentChange.getType() == DocumentChange.Type.MODIFIED)
                {
                    for(int i = 0 ; i < conversations.size();i++)
                    {
                        if(conversations.get(i).getSenderId().equals(senderId) && conversations.get(i).getReceiverId().equals(receiverId))
                        {
                            conversations.get(i).setMessage(documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGGE));
                            conversations.get(i).setDataObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations, (obj1,obj2)-> obj2.getDataObject().compareTo(obj1.getDataObject()));
            conversationsAdapter.notifyDataSetChanged();
            binding.conversationRecyclerView.smoothScrollToPosition(0);
            binding.conversationRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };

    private void getToken()
    {
      messagging.getToken().addOnSuccessListener(this::updateToken);
    }

    private void setListenser()
    {

        binding.imageSetting.setOnClickListener(v -> signOut());
        binding.fabNewChat.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(),UserActivity.class));
        });
    }

    private void config()
    {
        getToken();
        listenConversations();
    }



    @Override
    public void onBackPressed()
    {
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
        documentReference.update(Constants.KEY_AVAILABILITY,0);
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

    @Override
    public void onConversionClicked(Users user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
    }
}