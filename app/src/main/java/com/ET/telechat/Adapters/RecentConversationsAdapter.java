package com.ET.telechat.Adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ET.telechat.Listeners.ConversionListener;
import com.ET.telechat.Models.ChatMessage;
import com.ET.telechat.Models.Users;
import com.ET.telechat.databinding.ItemContainerRecentConversionBinding;

import java.util.List;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewholder>
{

    private final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;
    public RecentConversationsAdapter(List<ChatMessage> chatMessages,ConversionListener conversionListener) {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new ConversionViewholder(ItemContainerRecentConversionBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewholder holder, int position)
    {
            holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewholder extends RecyclerView.ViewHolder
    {

        ItemContainerRecentConversionBinding binding;
        ConversionViewholder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding)
        {
            super(itemContainerRecentConversionBinding.getRoot());
            binding = itemContainerRecentConversionBinding;
        }

        void setData(ChatMessage chatMessage)
        {
            binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.getConversionImage()));
            binding.textName.setText(chatMessage.getConversionName());
            binding.textRecentMessage.setText(chatMessage.getMessage());
            binding.getRoot().setOnClickListener( v -> {
                Users user = new Users();
                user.setId(chatMessage.getConversionId());
                user.setName(chatMessage.getConversionName());
                user.setProfilePic(chatMessage.getConversionImage());
                conversionListener.onConversionClicked(user);
            });
        }
    }

    private Bitmap getConversionImage(String encodedImage)
    {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }

}
