package com.example.grouptracker.Model;

import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Place implements Serializable {

    private String place_title;
    private String place_id;
    private com.example.grouptracker.Model.LatLng place_latLng;
    private String address;
    private float place_radius;
    private String imageUri;
    private String geoAdmin;
    private boolean notifications;
    private List<String> members = new ArrayList<>();

    public Place(String place_title, String place_id, com.example.grouptracker.Model.LatLng place_latLng, float place_radius, String geoAdmin, String imageUri, boolean notifications) {
        this.place_title = place_title;
        this.place_id = place_id;
        this.place_latLng = place_latLng;
        this.place_radius = place_radius;
        this.imageUri = imageUri;
        this.notifications = notifications;
        this.geoAdmin = geoAdmin;
        this.members.add(geoAdmin);
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getGeoAdmin() {
        return geoAdmin;
    }

    public void setGeoAdmin(String geoAdmin) {
        this.geoAdmin = geoAdmin;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public void addMember(String userId) {
        members.add(userId);
    }

    public Place() {

    }

    public boolean isNotifications() {
        return notifications;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    protected Place(Parcel in) {
        place_title = in.readString();
        place_id = in.readString();
        place_latLng = in.readParcelable(LatLng.class.getClassLoader());
        place_radius = in.readFloat();
    }

    public String getPlace_title() {
        return place_title;
    }

    public void setPlace_title(String place_title) {
        this.place_title = place_title;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public com.example.grouptracker.Model.LatLng getPlace_latLng() {
        return place_latLng;
    }

    public void setPlace_latLng(com.example.grouptracker.Model.LatLng place_latLng) {
        this.place_latLng = place_latLng;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public float getPlace_radius() {
        return place_radius;
    }

    public void setPlace_radius(float place_radius) {
        this.place_radius = place_radius;
    }

}
