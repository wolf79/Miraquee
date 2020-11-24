package com.example.chatterboi.afterauthenticated;

import android.net.Uri;

public class PostModel {
    String displayName, postText;
    String profileUri, postUri;
    String time, uid;

    public PostModel(String displayName, String postText, String time, String profileUri, String postUri, String uid) {
        this.displayName = displayName;
        this.postText = postText;
        this.time = time;
        this.profileUri = profileUri;
        this.postUri = postUri;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPostText() {
        return postText;
    }

    public void setPostText(String postText) {
        this.postText = postText;
    }

    public String getProfileUri() {
        return profileUri;
    }

    public void setProfileUri(String profileUri) {
        this.profileUri = profileUri;
    }

    public String getPostUri() {
        return postUri;
    }

    public void setPostUri(String postUri) {
        this.postUri = postUri;
    }
}
