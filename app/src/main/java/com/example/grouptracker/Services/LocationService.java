package com.example.grouptracker.Services;


import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ResultReceiver;
import android.renderscript.RenderScript;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.grouptracker.Main.Maps.MapsActivity;
import com.example.grouptracker.Model.User;
import com.example.grouptracker.Model.UserLocation;
import com.example.grouptracker.R;
import com.example.grouptracker.Utils.Common;
import com.example.grouptracker.Utils.GeofenceHelper;
import com.example.grouptracker.Utils.LocationEvent;
import com.example.grouptracker.Utils.UserClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import static androidx.core.app.NotificationCompat.DEFAULT_SOUND;
import static androidx.core.app.NotificationCompat.DEFAULT_VIBRATE;


public class LocationService extends Service {

    private static final String TAG = "CurrentLocationService";
    private FusedLocationProviderClient fusedLocationClient;
    UserLocation userLocation;
    DatabaseReference user_locations;
    public final String serviceMessage = "ServiceMessage";
    private Context context;
    UserClient userClient;
    List<Address> addresses;
    FirebaseAuth auth;
    FirebaseUser user;
    Geocoder geocoder;
    LocationRequest locationRequest = new LocationRequest();
    User currUser;
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;
    private Thread thread;
    private FirebaseAuth.AuthStateListener authListener;
    private String CHANNEL_NAME = "High priority channel";
    private String CHANNEL_ID = "com.example.notifications" + CHANNEL_NAME;
    public ResultReceiver resultReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotification() {
        Intent result = new Intent(getApplicationContext(), MapsActivity.class);
        PendingIntent updateIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                result,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Intent stop = new Intent(this, LocationService.class);
        stop.setAction(Common.ACTION_STOP_LOCATION_SERVICE);
        PendingIntent stopIntent = PendingIntent.getService(this, 0, stop, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationChannel.setDescription("This channel is used by location service");
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                CHANNEL_ID
        );
        builder.setChannelId(CHANNEL_ID);
        builder.setSmallIcon(R.mipmap.ic_notification_foreground);
        builder.setContentTitle("Background Location Service");
        builder.setAutoCancel(true);
        builder.setContentText("Running");
        builder.setContentIntent(updateIntent);
        builder.setOngoing(true);
        builder.setSilent(true);
        builder.addAction(android.R.drawable.ic_media_pause, "Stop", stopIntent);
        Notification notification = builder.build();
        startForeground(1, notification);
    }

    private void startLocationService() {
        createNotification();
        // Create the location request to start receiving updates
        createLocationRequestBalanced();

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }

        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void createLocationRequestBalanced() {
        locationRequest
            .setInterval(80000)
            .setFastestInterval(40000)
            .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    private void createLocationRequestAccurate() {
        locationRequest
            .setInterval(10000)
            .setFastestInterval(5000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }



    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if(locationResult != null && locationResult.getLastLocation() != null)
            {
                Location location = locationResult.getLastLocation();

                if (location != null) {
                    User user = ((UserClient)(getApplicationContext())).getUser();
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    UserLocation userLocation = new UserLocation(user, geoPoint, null);
                    //SharedPreferences sharedPref = getSharedPreferences("details", MODE_PRIVATE);
                    //SharedPreferences.Editor editor = sharedPref.edit();
                    //putDouble(editor, "latitude", location.getLatitude());
                    //putDouble(editor, "longitude", location.getLongitude());
                    //editor.commit();
                    if(location.hasSpeed()) {
                        userLocation.setSpeed(location.getSpeed());
                    } else {
                        userLocation.setSpeed((float) 0);
                    }
                    if(location.hasAccuracy()) {
                        userLocation.setAccuracy(location.getAccuracy());
                    } else {
                        userLocation.setAccuracy((float) 0);
                    }
                    if(location.hasAltitude()) {
                        userLocation.setAltitude(location.getAltitude());
                    } else {
                        userLocation.setAltitude((double) 0);
                    }
                    geocoder = new Geocoder(getApplicationContext());
                    try {
                        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        userLocation.setAddress(addresses.get(0).getAddressLine(0));
                    } catch (Exception e) {
                        Log.d("ADDRESS", "Not available");
                    }
                    Log.d("CurrentLocationService",
                            "locationCallback \ngot new location and set to userLocation" +
                                    "\n latitude: " + userLocation.getGeo_point().getLatitude() +
                                    "\n longitude: " + userLocation.getGeo_point().getLongitude() +
                                    "\n speed: " + userLocation.getSpeed() +
                                    "\n altitude: " + userLocation.getAltitude() +
                                    "\n accuracy: " + userLocation.getAccuracy() +
                                    "\n address: " + userLocation.getAddress()
                    );
                    EventBus.getDefault().post(new LocationEvent(userLocation));
                    saveUserLocation(userLocation);
                }
            }
        }
    };

    private void sendDataToActivity() {
        Intent send = new Intent();
        send.setAction("GET_LOCATION");
        send.putExtra("userLocation", userLocation);
        sendBroadcast(send);
    }

    public void setContext(Context context){
        this.context = context;
    }

    private void saveUserLocation(final UserLocation userLocation){

        try{
                DocumentReference locationRef = FirebaseFirestore.getInstance()
                        .collection(getString(R.string.collection_user_locations))
                        .document(FirebaseAuth.getInstance().getUid());

                locationRef.set(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: \ninserted user location into database.");
                        }
                    }
                });
        }catch (NullPointerException e){
            Log.e(TAG, "saveUserLocation: User instance is null, stopping location service.");
            Log.e(TAG, "saveUserLocation: NullPointerException: "  + e.getMessage() );
            stopSelf();
        }

    }

    private void stopLocationService() {
        LocationServices.getFusedLocationProviderClient(getApplicationContext()).removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        auth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        userClient = (UserClient) getApplicationContext();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            String action = intent.getAction();
            if(action != null) {
                if(action.equals(Common.ACTION_START_LOCATION_SERVICE)) {
                    startLocationService();
                } else if(action.equals(Common.ACTION_STOP_LOCATION_SERVICE)) {
                    stopLocationService();
                }
            }
        }
        return START_NOT_STICKY;
    }
}