package com.ET.telechat.Activities;

import static com.ET.telechat.Utilities.UIHelpers.showToast;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.ET.telechat.Adapters.ChatAdapter;
import com.ET.telechat.Models.ChatMessage;
import com.ET.telechat.Models.Users;
import com.ET.telechat.Network.ApiClient;
import com.ET.telechat.Network.ApiService;
import com.ET.telechat.R;
import com.ET.telechat.Utilities.Constants;
import com.ET.telechat.Utilities.PreferenceManager;
import com.ET.telechat.Utilities.UIHelpers;
import com.ET.telechat.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    //reference
    ActivityChatBinding binding;
    private Users receiverUser;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private FirebaseFirestore database;

    private FirebaseStorage storage;
    private String conversationId = null;

    private Boolean isReceiverAvailable = false;

    //ui
    Dialog loadingDialog = null;

    //callback
    ActivityResultLauncher<Intent> activityResultAttachmentLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult()
            , new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            int res = result.getResultCode();
            Intent data = result.getData();
            if(res == Activity.RESULT_OK) {
                if(data != null) {
                    if(data.getData() != null) {
                        Uri selectedImage = data.getData();
                        Calendar calendar = Calendar.getInstance();
                        StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis() + "");
                        loadingDialog.show();
                        reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                loadingDialog.dismiss();
                                if(task.isSuccessful()) {
                                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String filePath = uri.toString();

                                            String messageTxt = binding.inputMessage.getText().toString();

                                            Date date = new Date();
                                            ChatMessage message = new ChatMessage(messageTxt, preferenceManager.getString(Constants.KEY_USER_ID), date);
                                            message.setMessage("photo");
                                            message.setImageUrl(filePath);

                                            sendMessage(message);

                                            Toast.makeText(ChatActivity.this, filePath, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }
        }
    }) ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setListeners();
        setContentView(binding.getRoot());
        config();

    }

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if(encodedImage != null)
        {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        else
        {
            return null;
        }


    }

    private void sendMessage(ChatMessage messageContent)
    {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.getId());
        boolean isSendContentFromInputMessage = false;
        if(messageContent.getMessage() != null && !messageContent.getMessage().isEmpty())
        {
            message.put(Constants.KEY_MESSAGE, messageContent.getMessage());
        }else
        {
            isSendContentFromInputMessage = true;
            message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        }
        message.put(Constants.KEY_TIMESTAMP, new Date());
        if(messageContent.getImageUrl() != null && !messageContent.getImageUrl().isEmpty())
        {
            message.put(Constants.KEY_MESSAGE_IMAGE_URL,messageContent.getImageUrl());
        }
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if(conversationId != null)
        {
            updateConversion(binding.inputMessage.getText().toString());
        }
        else
        {
            HashMap<String,Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME,preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID,receiverUser.getId());
            conversion.put(Constants.KEY_RECEIVER_NAME,receiverUser.getName());
            conversion.put(Constants.KEY_RECEIVER_IMAGE,receiverUser.getProfilePic());
            conversion.put(Constants.KEY_LAST_MESSAGGE,binding.inputMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP,new Date());
            addConversion(conversion);
        }
        if(!isReceiverAvailable)
        {
            try{
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.getToken());

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME,preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN,preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA,data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);

                sendNotification(body.toString());
            }
            catch (Exception e)
            {
                showToast(getApplicationContext(),e.getMessage());
            }
        }
        if(isSendContentFromInputMessage)
        {
            binding.inputMessage.setText(null);
        }
    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.getId());
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if(conversationId != null)
        {
            updateConversion(binding.inputMessage.getText().toString());
        }
        else
        {
            HashMap<String,Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME,preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID,receiverUser.getId());
            conversion.put(Constants.KEY_RECEIVER_NAME,receiverUser.getName());
            conversion.put(Constants.KEY_RECEIVER_IMAGE,receiverUser.getProfilePic());
            conversion.put(Constants.KEY_LAST_MESSAGGE,binding.inputMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP,new Date());
            addConversion(conversion);
        }
        if(!isReceiverAvailable)
        {
            try{
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.getToken());

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME,preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN,preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA,data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);

                sendNotification(body.toString());
            }
            catch (Exception e)
            {
                showToast(getApplicationContext(),e.getMessage());
            }
        }
        binding.inputMessage.setText(null);
    }

    private void sendNotification(String messageBody)
    {
        ApiClient.getClient().create(ApiService.class)
                .sendMessage(Constants.getRemoteMsgHeaders(),messageBody)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if(response.isSuccessful())
                        {
                            try {
                                if(response.body() != null)
                                {
                                    JSONObject responeseJson = new JSONObject(response.body());
                                    JSONArray results = responeseJson.getJSONArray("results");
                                    if(responeseJson.getInt("failure") == 1)
                                    {
                                        JSONObject error = (JSONObject) results.get(0);
                                        showToast(getApplicationContext(),error.getString("error"));
                                        return;
                                    }

                                }
                            }catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                            showToast(getApplicationContext(),"Notification sent successfully");
                        }
                        else
                        {
                            showToast(getApplicationContext(),"Error: "+response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        showToast(getApplicationContext(),t.getMessage());
                    }
                });
    }


    private void listenAvailabilityOfReceiver()
    {
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                receiverUser.getId()
        ).addSnapshotListener(ChatActivity.this, (value , error) -> {
            if(error != null)
            {
                return;
            }
            if(value != null)
            {
                if(value.getLong(Constants.KEY_AVAILABILITY) != null)
                {
                    int availability = Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY).intValue());
                    isReceiverAvailable = (availability == 1);
                }
                receiverUser.setToken(value.getString(Constants.KEY_FCM_TOKEN));
                if(receiverUser.getProfilePic() == null)
                {
                    receiverUser.setProfilePic(value.getString(Constants.KEY_IMAGE));
                    chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiverUser.getProfilePic()));
                    chatAdapter.notifyItemRangeChanged(0,chatMessages.size());
                }
            }
            if(isReceiverAvailable)
            {
                binding.textAvailability.setVisibility(View.VISIBLE);
            }
            else
            {
                binding.textAvailability.setVisibility(View.GONE);
            }

        });
    }

    private void listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.getId())
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.getId())
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setSenderId(documentChange.getDocument().getString(Constants.KEY_SENDER_ID));
                    chatMessage.setReceiverId(documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID));
                    chatMessage.setMessage(documentChange.getDocument().getString(Constants.KEY_MESSAGE));
                    chatMessage.setDateTime(getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP)));
                    chatMessage.setDataObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.setMessageID(documentChange.getDocument().getId());
                    if(documentChange.getDocument().contains(Constants.KEY_MESSAGE_IMAGE_URL))
                    {
                        chatMessage.setImageUrl(documentChange.getDocument().getString(Constants.KEY_MESSAGE_IMAGE_URL));
                    }

                    if(documentChange.getDocument().contains(Constants.KEY_MESSAGE_FEELING))
                    {
                        Long feelingValueRaw = documentChange.getDocument().getLong(Constants.KEY_MESSAGE_FEELING);
                        chatMessage.setFeeling(feelingValueRaw.intValue());
                    }


                    chatMessages.add(chatMessage);
                }
            }

            Collections.sort(chatMessages, Comparator.comparing(ChatMessage::getDataObject));

            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecycleView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecycleView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if(conversationId == null)
        {
            checkForConversion();
        }
    };

    private void init() {
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        loadReceiverDetails();
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        chatAdapter = new ChatAdapter(getApplicationContext(),database,chatMessages, getBitmapFromEncodedString(receiverUser.getProfilePic()), preferenceManager.getString(Constants.KEY_USER_ID));
        binding.chatRecycleView.setAdapter(chatAdapter);
        loadingDialog = new Dialog(ChatActivity.this);
        loadingDialog.setContentView(R.layout.dialog_loading);
        if(loadingDialog.getWindow() != null)
        {
            loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());

        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                activityResultAttachmentLauncher.launch(intent);
            }
        });

        binding.viewBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chatAdapter.IsShowReaction())
                {
                    chatAdapter.SetShowReaction(false);
                }
            }
        });
    }

    private void config() {
        listenMessages();
    }

    private void loadReceiverDetails() {
        receiverUser = (Users) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.getName());
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private  void addConversion(HashMap<String,Object> conversion)
    {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .document(conversion.get(Constants.KEY_RECEIVER_ID)+"_"+conversion.get(Constants.KEY_SENDER_ID)).set(conversion)
                .addOnSuccessListener(documentReference -> conversationId = conversion.get(Constants.KEY_RECEIVER_ID)+"_"+conversion.get(Constants.KEY_SENDER_ID));
    }

    private void updateConversion(String message)
    {
        DocumentReference documentReference = database
                .collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .document(conversationId);
        documentReference.update(Constants.KEY_LAST_MESSAGGE,message,Constants.KEY_TIMESTAMP,new Date());
    }

    private void checkForConversion() {
            if(chatMessages.size() != 0)
            {
                checkForConversionRemotely(preferenceManager.getString(Constants.KEY_USER_ID),receiverUser.getId());
                checkForConversionRemotely(receiverUser.getId(),preferenceManager.getString(Constants.KEY_USER_ID));
            }
    }

    private void checkForConversionRemotely(String senderId,String receiverId)
    {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).
                whereEqualTo(Constants.KEY_SENDER_ID,senderId).
                whereEqualTo(Constants.KEY_RECEIVER_ID,receiverId).
                get().
                addOnCompleteListener(conversionOnCompleteListener);
    }


    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task ->
    {
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0)
        {
            DocumentSnapshot  documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}