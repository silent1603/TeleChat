package com.ET.telechat.Models;

public class Users {
    private String name;
    private String profilePic;

    private String userID;
    private String password;
    private String gmail;
    public Users(String name, String profilePic, String userID, String password, String gmail) {
        this.name = name;
        this.profilePic = profilePic;
        this.userID = userID;
        this.password = password;
        this.gmail = gmail;
    }

    public Users(String name, String profilePic, String userID, String password) {
        this.name = name;
        this.profilePic = profilePic;
        this.userID = userID;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGmail() {
        return gmail;
    }

    public void setGmail(String gmail) {
        this.gmail = gmail;
    }
}
