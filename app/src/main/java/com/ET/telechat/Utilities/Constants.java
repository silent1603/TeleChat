package com.ET.telechat.Utilities;

import java.util.HashMap;

public class Constants
{
    public static final  String KEY_COLLECTION_USERS="users";
    public static final  String KEY_NAME="name";
    public static final  String KEY_EMAIL="email";

    public static final  String KEY_PASSWORD="password";
    public static final  String KEY_PREFERENCE_NAME="chapAppPreferemce";
    public static final  String KEY_IS_SIGNED_IN="isSignedIn";
    public static final  String KEY_USER_ID="userID";
    public static final  String KEY_IMAGE="profilePic";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";

    public static final String KEY_COLLECTION_CHAT = "chat";

    public static final String KEY_SENDER_ID = "senderID";

    public static final String KEY_RECEIVER_ID = "receiverId";

    public static final String KEY_MESSAGE = "message";

    public static final String KEY_TIMESTAMP = "timestamp";

    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";

    public static final String KEY_SENDER_NAME = "senderName";

    public static final String KEY_RECEIVER_NAME = "receiverName" ;

    public static final String KEY_SENDER_IMAGE = "senderImage";

    public static  final String KEY_RECEIVER_IMAGE = "receiverImage";

    public static final String KEY_LAST_MESSAGGE = "lastMessage";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";


    public static HashMap<String,String> remoteMsgHeaders = null;
    public static HashMap<String,String> getRemoteMsgHeaders()
    {
        if(remoteMsgHeaders ==null)
        {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(REMOTE_MSG_AUTHORIZATION,"key=AAAAUUjdhbA:APA91bF2LHFBoVPn7qOg5si_GTjIMCc04g-GwXWHtbqd3limo2oEJ2SSIYa3HLNSl8jBsQYEN3dJlnADljSiO0mH_QavaBIpU1h1A-KlfUgfNyvfzQFu6Kb1-GSEnvvjksb66gFpIxT3");
            remoteMsgHeaders.put(REMOTE_MSG_CONTENT_TYPE,"application/json");
        }
        return remoteMsgHeaders;
    }
}
