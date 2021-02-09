package com.example.grouptracker.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class User implements Serializable {

    public String name;
    public String surrname;
    public String family;
    public String family_name;
    public String email;
    public String gender;
    public String isSharing;
    public String imageUri;
    public String userid;
    public String date;
    public int locationUpdateInterval;
    public List<String> groupList = new ArrayList<>();

    public List<String> getPlaceList() {
        return placeList;
    }

    public void setPlaceList(List<String> placeList) {
        this.placeList = placeList;
    }

    public List<String> placeList = new ArrayList<>();

    public User(String name, String surrname, String email, String gender, String isSharing, String imageUri, String userid, String date, int locationUpdateInterval) {
        this.name = name;
        this.surrname = surrname;
        this.family_name = surrname + " Family";
        this.email = email;
        this.gender = gender;
        this.isSharing = isSharing;
        this.imageUri = imageUri;
        this.userid = userid;
        this.date = date;
        this.locationUpdateInterval = locationUpdateInterval;
    }

    public String getFamily_name() {
        return family_name;
    }

    public void setFamily_name(String family_name) {
        this.family_name = family_name;
    }

    public int getLocationUpdateInterval() {
        return locationUpdateInterval;
    }

    public void setLocationUpdateInterval(int locationUpdateInterval) {
        this.locationUpdateInterval = locationUpdateInterval;
    }

    public User()
    {}

    public void addGroup(String group) {
        groupList.add(group);
    }

    public void addPlace(String place) {
        placeList.add(place);
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public List<String> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<String> groupList) {
        this.groupList = groupList;
    }

    public String getSurrname() {
        return surrname;
    }

    public void setSurrname(String surrname) {
        this.surrname = surrname;
    }

    protected User(Parcel in) {
        name = in.readString();
        surrname = in.readString();
        email = in.readString();
        gender = in.readString();
        isSharing = in.readString();
        imageUri = in.readString();
        userid = in.readString();
        date = in.readString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getIsSharing() {
        return isSharing;
    }

    public void setIsSharing(String isSharing) {
        this.isSharing = isSharing;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
