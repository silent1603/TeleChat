package com.ET.telechat.Models;

import java.util.Date;

public class ChatMessage {
    private String senderId ;
    private String receiverId;
    private String message;
    private String dateTime;

    private Date dataObject;

    public ChatMessage()
    {

    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId)
    {
        this.senderId = senderId;
    }

    public String getReceiverId()
    {
        return receiverId;
    }

    public void setReceiverId(String receiverId)
    {
        this.receiverId = receiverId;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getDateTime()
    {
        return dateTime;
    }

    public void setDateTime(String dateTime)
    {
        this.dateTime = dateTime;
    }

    public Date getDataObject()
    {
        return dataObject;
    }

    public void setDataObject(Date dataObject)
    {
        this.dataObject = dataObject;
    }
}
