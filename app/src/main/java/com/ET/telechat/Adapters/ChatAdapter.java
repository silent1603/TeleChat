package com.ET.telechat.Adapters;

import android.graphics.Bitmap;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ET.telechat.Models.ChatMessage;
import com.ET.telechat.R;
import com.ET.telechat.Utilities.Constants;
import com.ET.telechat.Utilities.Reactions;
import com.ET.telechat.databinding.ItemContainerReceivedMessageBinding;
import com.ET.telechat.databinding.ItemContainerSendMessageBinding;
import com.ET.telechat.databinding.ItemContainerUserBinding;
import com.bumptech.glide.Glide;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.Date;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private ReactionsConfig config;
    private Context context;

    private FirebaseFirestore database;
    private final List<ChatMessage> chatMessages;
    private Bitmap receiverProfileImage;
    private final String senderId;

    private ReactionPopup popup;
    private FirebaseRemoteConfig remoteConfig;
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    private boolean isShowReaction = false;

    public void setReceiverProfileImage(Bitmap bitmap)
    {
        receiverProfileImage = bitmap;
    }


    public ChatAdapter(Context context,FirebaseFirestore database,List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderId)
    {
        remoteConfig = FirebaseRemoteConfig.getInstance();
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
        this.context = context;
        this.database = database;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENT)
        {
            return new SendMessageViewHolder(ItemContainerSendMessageBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
        else
        {
            return new ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        ChatMessage chatMessage = chatMessages.get(position);
        int reactions[] = new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        popup = new ReactionPopup(context, config, (pos) -> {

            if (pos < 0)
                return false;

            if (holder.getClass() == SendMessageViewHolder.class) {
                SendMessageViewHolder viewHolder = (SendMessageViewHolder) holder;
                viewHolder.binding.feeling.setImageResource(reactions[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            } else {
                ReceivedMessageViewHolder viewHolder = (ReceivedMessageViewHolder) holder;
                viewHolder.binding.feeling.setImageResource(reactions[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);


            }
            chatMessage.setFeeling(pos);

            DocumentReference documentReference = database
                    .collection(Constants.KEY_COLLECTION_CHAT)
                    .document(chatMessage.getMessageID());
            documentReference.update(Constants.KEY_MESSAGE_FEELING,chatMessage,Constants.KEY_MESSAGE_FEELING,chatMessage.getFeeling());

            return true;
        });

        if(getItemViewType(position) == VIEW_TYPE_SENT)
        {
            ((SendMessageViewHolder)holder).setData(chatMessages.get(position));
            if(chatMessage.getMessage().equals("photo"))
            {
                ((SendMessageViewHolder) holder).binding.image.setVisibility(View.VISIBLE);
                ((SendMessageViewHolder) holder).binding.textMessage.setVisibility(View.GONE);
                Glide.with(context).load(chatMessage.getImageUrl()).placeholder(R.drawable.placeholder_image).into(((SendMessageViewHolder) holder).binding.image);
            }
            if(chatMessage.getFeeling() >= 0)
            {
                ((SendMessageViewHolder) holder).binding.feeling.setImageResource(reactions[chatMessage.getFeeling()]);
                ((SendMessageViewHolder) holder).binding.feeling.setVisibility(View.VISIBLE);
            } else {
                ((SendMessageViewHolder) holder).binding.feeling.setVisibility(View.GONE);
            }
            ((SendMessageViewHolder) holder).binding.textMessage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    boolean isFeelingsEnabled = remoteConfig.getBoolean("isFeelingsEnabled");
                    if(isFeelingsEnabled)
                        if(isShowReaction)
                        {
                            popup.dismiss();
                            isShowReaction = false;
                        }else {
                            popup.onTouch(v, event);
                            isShowReaction = true;
                        }
                    else
                        Toast.makeText(context, "This feature is disabled temporarily.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

            ((SendMessageViewHolder) holder).binding.image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(isShowReaction)
                    {
                        popup.dismiss();
                        isShowReaction = false;
                    }else {
                        popup.onTouch(v, event);
                        isShowReaction =true;
                    }
                    return false;
                }
            });
        }
        else
        {
            ((ReceivedMessageViewHolder)holder).setData(chatMessages.get(position),receiverProfileImage);
            if(chatMessage.getMessage().equals("photo"))
            {
                ((ReceivedMessageViewHolder) holder).binding.image.setVisibility(View.VISIBLE);
                ((ReceivedMessageViewHolder) holder).binding.textMessage.setVisibility(View.GONE);
                Glide.with(context).load(chatMessage.getImageUrl()).placeholder(R.drawable.placeholder_image).into(((ReceivedMessageViewHolder)holder).binding.image);
            }
            if(chatMessage.getFeeling() >= 0)
            {
                ((ReceivedMessageViewHolder) holder).binding.feeling.setImageResource(reactions[chatMessage.getFeeling()]);
                ((ReceivedMessageViewHolder) holder).binding.feeling.setVisibility(View.VISIBLE);
            } else {
                ((ReceivedMessageViewHolder) holder).binding.feeling.setVisibility(View.GONE);
            }
            ((ReceivedMessageViewHolder) holder).binding.textMessage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if(isShowReaction)
                    {
                        popup.dismiss();
                        isShowReaction = false;
                    }else {
                        popup.onTouch(view, motionEvent);
                        isShowReaction = true;
                    }
                    return false;
                }
            });

            ((ReceivedMessageViewHolder) holder).binding.image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(isShowReaction)
                    {
                        popup.dismiss();
                        isShowReaction = false;
                    }else {
                        popup.onTouch(v, event);
                        isShowReaction = true;
                    }

                    return false;
                }
            });
        }

    }

    @Override
    public int getItemCount()
    {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position)
    {
        if(chatMessages.get(position).getSenderId().equals(senderId))
        {
            return  VIEW_TYPE_SENT;
        }
        else
        {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SendMessageViewHolder extends RecyclerView.ViewHolder
    {
        private final ItemContainerSendMessageBinding binding;

        SendMessageViewHolder(ItemContainerSendMessageBinding itemContainerSendMessageBinding )
        {
            super(itemContainerSendMessageBinding.getRoot());
            binding = itemContainerSendMessageBinding;

        }

        void setData(ChatMessage chatMessage)
        {
            binding.textMessage.setText(chatMessage.getMessage());
            binding.textDateTime.setText(chatMessage.getDateTime());
        }

    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder
    {
        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding)
        {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage,Bitmap receiverProfileImage)
        {
            binding.textMessage.setText(chatMessage.getMessage());
            binding.textDateTime.setText(chatMessage.getDateTime());
            if(receiverProfileImage != null)
            {
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }

        }


    }

    public boolean IsShowReaction() {
        return isShowReaction;
    }

    public void SetShowReaction(boolean isShowReaction) {
        if(!isShowReaction)
        {
            if(popup != null && popup.isShowing())
            {
                popup.dismiss();
            }
        }
        this.isShowReaction = isShowReaction;
    }
}
