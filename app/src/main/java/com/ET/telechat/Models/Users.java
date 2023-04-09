package com.ET.telechat.Models;

import java.io.Serializable;

public class Users implements Serializable {
    private String name;
    private String profilePic;
    private String token;
    private String email;
    private String id;

    private String lastMessage;
    public Users()
    {

    }
    public Users(String name, String profilePic, String email)
    {
        this.name = name;
        this.profilePic = profilePic;
        this.email = email;
    }

    public Users(String name, String profilePic, String token, String email)
    {
        this.name = name;
        this.profilePic = profilePic;
        this.token = token;
        this.email = email;
    }

    public Users(String name, String profilePic, String token, String email, String id) {
        this.name = name;
        this.profilePic = profilePic;
        this.token = token;
        this.email = email;
        this.id = id;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }


    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getProfilePic()
    {
        return profilePic;
    }

    public void setProfilePic(String profilePic)
    {
        this.profilePic = profilePic;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
}
