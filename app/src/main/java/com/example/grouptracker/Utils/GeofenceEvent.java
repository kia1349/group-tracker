package com.example.grouptracker.Utils;

import com.example.grouptracker.Model.Place;
import com.example.grouptracker.Model.UserLocation;

import java.util.ArrayList;
import java.util.List;

public class GeofenceEvent {
    public Place place;

    public GeofenceEvent(Place place) {
        this.place = place;
    }
}
