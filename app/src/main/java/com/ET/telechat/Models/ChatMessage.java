package com.ET.telechat.Models;

import java.util.Date;

public class ChatMessage {
    private String senderId ;
    private String receiverId;
    private String message;
    private String dateTime;
    private Date dataObject;

    private String conversionId;

    private String conversionName;

    private String conversionImage;

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

    public String getConversionId() {
        return conversionId;
    }

    public void setConversionId(String conversionId) {
        this.conversionId = conversionId;
    }

    public String getConversionName() {
        return conversionName;
    }

    public void setConversionName(String conversionName) {
        this.conversionName = conversionName;
    }

    public String getConversionImage() {
        return conversionImage;
    }

    public void setConversionImage(String conversionImage) {
        this.conversionImage = conversionImage;
    }
}
