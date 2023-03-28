package com.ET.telechat.Adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ET.telechat.Models.ChatMessage;
import com.ET.telechat.databinding.ItemContainerReceivedMessageBinding;
import com.ET.telechat.databinding.ItemContainerSendMessageBinding;
import com.ET.telechat.databinding.ItemContainerUserBinding;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private final List<ChatMessage> chatMessages;
    private final Bitmap receiverProfileImage;
    private final String senderId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderId)
    {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
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
        if(getItemViewType(position) == VIEW_TYPE_SENT)
        {
            ((SendMessageViewHolder)holder).setData(chatMessages.get(position));
        }
        else
        {
            ((ReceivedMessageViewHolder)holder).setData(chatMessages.get(position),receiverProfileImage);
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
            binding.textMessage.setText(chatMessage.getDateTime());
            binding.imageProfile.setImageBitmap(receiverProfileImage);
        }


    }

}
