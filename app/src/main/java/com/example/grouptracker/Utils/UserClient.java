package com.example.grouptracker.Utils;

import android.app.Application;
import android.location.Location;

import com.example.grouptracker.Model.User;

import java.util.ArrayList;
import java.util.List;

//SINGLETON USER-JA DA SO LAHKO VSI NAENKAT GOR IN ZRAVEN SPREMLJAM USERDETAILS
public class UserClient extends Application {

    private User user = null;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
