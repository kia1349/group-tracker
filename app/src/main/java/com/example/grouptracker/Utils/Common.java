package com.example.grouptracker.Utils;

import com.example.grouptracker.Model.User;
import com.example.grouptracker.Model.UserLocation;

public class Common {
    public static final String USER_INFORMATION = "UserInformation";
    public static final String USER_UID_SAVE_KEY = "SaveUid";
    public static final String TOKENS = "Tokens";
    public static final String FROM_NAME = "FromName";
    public static final String ACCEPT_LIST = "acceptList";
    public static final String FROM_UID = "FromUid";
    public static final String TO_UID = "ToUid";
    public static final String TO_NAME = "ToName";
    public static final String GROUPS = "Groups";
    public static final String USER_LIST = "UserList";
    public static final String USER_LOCATIONS = "UserLocations";
    public static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;
    public static final int LOCATION_SERVICE_ID = 175;
    public static final String ACTION_START_LOCATION_SERVICE = "startLocationService";
    public static final String ACTION_STOP_LOCATION_SERVICE = "stopLocationService";
    public static final String ACTION_START_GEOFENCING_SERVICE = "startGeofencingService";
    public static final String ACTION_STOP_GEOFENCING_SERVICE = "stopGeofencingService";
    public static final String RECEIVER = "locationReceiver";
    public static boolean isFullscreenViewActive = true;
    public static User loggedUser;
    public static UserLocation userLocation;
}
