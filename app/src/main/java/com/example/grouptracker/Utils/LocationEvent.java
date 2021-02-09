package com.example.grouptracker.Utils;

import com.example.grouptracker.Model.Place;
import com.example.grouptracker.Model.UserLocation;

import java.util.ArrayList;
import java.util.List;

public class LocationEvent {
    public UserLocation userLocation;
    public List<Place> places = new ArrayList<>();

    public LocationEvent(UserLocation userLocation) {
        this.userLocation = userLocation;
    }
}
