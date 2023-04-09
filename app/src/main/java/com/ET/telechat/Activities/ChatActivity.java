package com.ET.telechat.Activities;

import static com.ET.telechat.Utilities.UIHelpers.showToast;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import com.ET.telechat.Adapters.ChatAdapter;
import com.ET.telechat.Models.ChatMessage;
import com.ET.telechat.Models.Users;
import com.ET.telechat.Network.ApiClient;
import com.ET.telechat.Network.ApiService;
import com.ET.telechat.Utilities.Constants;
import com.ET.telechat.Utilities.PreferenceManager;
import com.ET.telechat.Utilities.UIHelpers;
import com.ET.telechat.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    ActivityChatBinding binding;
    private Users receiverUser;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private FirebaseFirestore database;
    private String conversationId = null;
    private Boolean isReceiverAvailable = false;

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
        chatAdapter = new ChatAdapter(chatMessages, getBitmapFromEncodedString(receiverUser.getProfilePic()), preferenceManager.getString(Constants.KEY_USER_ID));
        binding.chatRecycleView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();

    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
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