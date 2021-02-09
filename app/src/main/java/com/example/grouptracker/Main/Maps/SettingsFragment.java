package com.example.grouptracker.Main.Maps;

import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.grouptracker.Model.Group;
import com.example.grouptracker.Model.User;
import com.example.grouptracker.Model.UserLocation;
import com.example.grouptracker.R;
import com.example.grouptracker.Utils.UserClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class SettingsFragment extends Fragment {
    //Firebase
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    User user;
    Group group;
    FirebaseDatabase firebaseDatabase;
    private static final int LOCATION_UPDATE_INTERVAL = 5000;
    DatabaseReference user_information;
    DatabaseReference user_location;
    private Handler handler = new Handler();
    private Runnable runnable;
    UserLocation userLocation;
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_address, tv_waypointCounts, tv_updateInterval;
    Switch sw_locationUpdates, sw_gps;
    MaterialButton btn_newWaypoint, btn_showWayPoints, btn_Maps;
    LocationRequest locationRequest;
    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FAST_UPDATE_INTERVAL = 5;
    boolean updateOn = false;
    Location currentLocation;
    List<Location> savedLocations;
    LocationCallback locationCallback;
    private GoogleMap mMap;
    Geocoder geocoder;
    UserClient userClient;
    Toolbar toolbar;
    Slider slider;
    FloatingActionButton locationUpdateCancel, locationUpdateSave;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initViews(view);
        getIncomingIntent();
        startUserLocationsRunnable();
        return view;
    }

    private void initViews(View view){
        tv_lat = view.findViewById(R.id.tv_lat);
        tv_lon = view.findViewById(R.id.tv_lon);
        tv_altitude = view.findViewById(R.id.tv_altitude);
        tv_accuracy = view.findViewById(R.id.tv_accuracy);
        tv_speed = view.findViewById(R.id.tv_speed);
        tv_address = view.findViewById(R.id.tv_address);
        tv_updateInterval = view.findViewById(R.id.tvUpdateInterval);
        locationUpdateSave = view.findViewById(R.id.saveLocationUpdateButton);
        locationUpdateSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationUpdateSave.setVisibility(View.INVISIBLE);
                locationUpdateCancel.setVisibility(View.INVISIBLE);
                tv_updateInterval.setText(String.valueOf(slider.getValue()));
            }
        });
        locationUpdateCancel = view.findViewById(R.id.cancelLocationUpdateBtn);
        locationUpdateCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationUpdateSave.setVisibility(View.INVISIBLE);
                locationUpdateCancel.setVisibility(View.INVISIBLE);
            }
        });
        slider = view.findViewById(R.id.location_interval_slider);
        slider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                tv_updateInterval.setText(String.valueOf(Math.round(slider.getValue()))+" ms");
                locationUpdateSave.setVisibility(View.VISIBLE);
                locationUpdateCancel.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getIncomingIntent() {
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(getString(R.string.intent_user));
            group = (Group) getArguments().getSerializable(getString(R.string.intent_group));
            userLocation = (UserLocation) getArguments().getSerializable(getString(R.string.intent_user_locations));
            updateUIValues(userLocation);
        } else {

        }
    }

    private void startUserLocationsRunnable() {
        Log.d("startRunnable", "MapsFragment-startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                retrieveUserLocations();
                handler.postDelayed(runnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void retrieveUserLocations() {
        try {
                DocumentReference userLocationRef = FirebaseFirestore.getInstance()
                        .collection(getString(R.string.collection_user_locations))
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

                userLocationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            final UserLocation updatedUserLocation = task.getResult().toObject(UserLocation.class);
                            updateUIValues(updatedUserLocation);
                        }
                    }
                });
            } catch (IllegalStateException e) {
            Log.e("retrieveLocation", "retrieveUserLocations: Fragment was destroyed during Firestore query. Ending query." + e.getMessage());
        }
    }

    private void updateUIValues(UserLocation location) {
        tv_lat.setText(String.valueOf(location.getGeo_point().getLatitude()));
        tv_lon.setText(String.valueOf(location.getGeo_point().getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));
        tv_altitude.setText(String.valueOf(location.getAltitude()));
        tv_speed.setText(String.valueOf(location.getSpeed()));
        tv_address.setText(location.getAddress());
    }
}
