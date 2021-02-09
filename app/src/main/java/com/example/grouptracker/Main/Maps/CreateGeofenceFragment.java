package com.example.grouptracker.Main.Maps;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.grouptracker.Model.Group;
import com.example.grouptracker.Model.Place;
import com.example.grouptracker.Model.UserLocation;
import com.example.grouptracker.R;
import com.example.grouptracker.Utils.GeofenceHelper;
import com.example.grouptracker.Utils.ViewWeightAnimationWrapper;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.GeoApiContext;

import java.io.IOException;
import java.util.List;

import static com.example.grouptracker.Utils.Common.MAPVIEW_BUNDLE_KEY;

public class CreateGeofenceFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener {
    private static final int MAP_LAYOUT_STATE_CONTRACTED = 0;
    private static final int MAP_LAYOUT_STATE_EXPANDED = 1;
    Toolbar toolbar;
    private MapView mMapView;
    private RelativeLayout mMapContainer;
    private GoogleMap mGoogleMap;
    private UserLocation mUserPosition;
    private LatLngBounds mapBoundary;
    private int mMapLayoutState = 0;
    private Address address;
    private GeoApiContext mGeoApiContext = null;
    private UserLocation userLocation;
    private GoogleMap gMap;
    private Place place;
    Marker geofenceMarker;
    Circle geofenceRadius;
    private boolean addingGeofence = true;
    private float GEOFENCE_RADIUS = 200;
    private Slider slider;
    private ConstraintLayout editGeofence;
    private Animation rotate_forward,rotate_backward;
    private ImageButton fullScreenBtn, myLocationBtn;
    private Group group;
    SearchView searchView;
    private FirebaseFirestore mDb;
    private TextInputLayout geofenceTitle;
    private LatLng choosenPosition;
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;
    List<Address> addressList = null;
    private boolean isMoving;
    TextView tv_radius;

    public static CreateGeofenceFragment newInstance() { return new CreateGeofenceFragment();}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_geofence, container, false);
        mMapView = view.findViewById(R.id.user_list_map);
        fullScreenBtn = view.findViewById(R.id.btn_full_screen_map);
        myLocationBtn = view.findViewById(R.id.btn_my_location);
        editGeofence = view.findViewById(R.id.editGeofence);
        slider = view.findViewById(R.id.radius_slider);
        geofenceTitle = view.findViewById(R.id.geofenceTitle);
        tv_radius = view.findViewById(R.id.tvRadius);
        searchView = view.findViewById(R.id.search_location);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                addressList = null;

                if(location != null || !location.equals("")) {
                    Geocoder geocoder = new Geocoder(getActivity());
                    try{
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(addressList.size() > 0) {
                        Address address = addressList.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        setCameraViewFromLatLng(latLng);
                        createGeofence();
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mMapContainer = view.findViewById(R.id.map_container);
        view.findViewById(R.id.closeBtn).setOnClickListener(this);
        view.findViewById(R.id.saveBtn).setOnClickListener(this);
        rotate_forward = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_backward);
        fullScreenBtn.setOnClickListener(this);
        myLocationBtn.setOnClickListener(this);
        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                GEOFENCE_RADIUS = value;
                if(gMap != null) {
                    createGeofence();
                }
            }
        });
        slider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                setCameraZoom();
                tv_radius.setText(String.valueOf(Math.round(slider.getValue()))+" m");
            }
        });
        mDb = FirebaseFirestore.getInstance();
        geofencingClient = LocationServices.getGeofencingClient(getActivity());
        geofenceHelper = new GeofenceHelper(getActivity());
        getIncomingGroup();
        initGoogleMap(savedInstanceState);
        return view;
    }

    private void setCameraZoom() {
        Log.d("MapsFragment-setCamView", "set");
        double bottomBoundary = userLocation.getGeo_point().getLatitude() - .1;
        double leftBoundary = userLocation.getGeo_point().getLongitude() - .1;
        double topBoundary = userLocation.getGeo_point().getLatitude() + .1;
        double rightBoundary = userLocation.getGeo_point().getLongitude() + .1;

        mapBoundary = new LatLngBounds(
                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );

        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getGeo_point().getLatitude(), userLocation.getGeo_point().getLongitude()), getZoomLevel(geofenceRadius)));
    }

    public int getZoomLevel(Circle circle) {
        int zoomLevel = 15;
        if (circle != null) {
            double radius = circle.getRadius() + circle.getRadius() / 2;
            double scale = radius / 500;
            zoomLevel = (int) (16 - Math.log(scale) / Math.log(2));
        }
        return zoomLevel;
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);
        if(mGeoApiContext == null){
            mGeoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_api_key)).build();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                //fragmentTransaction.replace()
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // CONSIDER CALLING PERMISSION REQUEST
        }
        gMap = googleMap;

        if(gMap != null) {
            gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            if(place != null) {
                setCameraViewFromLatLng(new LatLng(place.getPlace_latLng().getLatitude(), place.getPlace_latLng().getLongitude()));
                setCameraZoom();
                createGeofence();
            }
            else {
                if (userLocation.getGeo_point() != null) {
                    setCameraViewFromLatLng(new LatLng(userLocation.getGeo_point().getLatitude(), userLocation.getGeo_point().getLongitude()));
                    setCameraZoom();
                    createGeofence();
                }
            }
        }

        try {
            moveCompas();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        gMap.getUiSettings().setMapToolbarEnabled(false);
        gMap.getUiSettings().setTiltGesturesEnabled(false);
        gMap.setOnCameraMoveStartedListener(this);
        gMap.setOnCameraIdleListener(this);
        gMap.setOnCameraMoveListener(this);
        gMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                Geocoder geocoder = new Geocoder(getActivity());
                addressList = null;
                try{
                    addressList = geocoder.getFromLocation(userLocation.getGeo_point().getLatitude(), userLocation.getGeo_point().getLongitude(), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(addressList.size() > 0) {
                    address = addressList.get(0);
                    searchView.setQuery(address.getAddressLine(0).split(",")[0], false);
                    searchView.clearFocus();
                }
            }
        });
    }

    @Override
    public void onCameraMove() {
        CameraPosition cameraPosition = gMap.getCameraPosition();
        if(cameraPosition.zoom >= 14.0) {
            if(gMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
                gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
        } else {
            if(gMap.getMapType() == GoogleMap.MAP_TYPE_HYBRID) {
                gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        }
        createGeofence();
    }

    private void setCameraView() {
        Log.d("MapsFragment-setCamView", "set");
        double bottomBoundary = userLocation.getGeo_point().getLatitude() - .1;
        double leftBoundary = userLocation.getGeo_point().getLongitude() - .1;
        double topBoundary = userLocation.getGeo_point().getLatitude() + .1;
        double rightBoundary = userLocation.getGeo_point().getLongitude() + .1;

        mapBoundary = new LatLngBounds(
                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );

        if(gMap != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(userLocation.getGeo_point().getLatitude(), userLocation.getGeo_point().getLongitude()))
                    .zoom(15)
                    .bearing(0)
                    .tilt(0)
                    .build();
            gMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    private void setCameraViewFromLatLng(LatLng latLng) {
        Log.d("MapsFragment-setCamView", "set");
        double bottomBoundary = latLng.latitude - .1;
        double leftBoundary = latLng.longitude - .1;
        double topBoundary = latLng.latitude + .1;
        double rightBoundary = latLng.longitude + .1;

        mapBoundary = new LatLngBounds(
                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );

        if(gMap != null) {
            int width = getActivity().getResources().getDisplayMetrics().widthPixels;
            int height = getActivity().getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(latLng.latitude, latLng.longitude))
                    .zoom(15)
                    .bearing(0)
                    .tilt(0)
                    .build();
            gMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    private void getIncomingGroup() {
        if (getArguments() != null) {
            userLocation = (UserLocation) getArguments().getSerializable(getString(R.string.intent_user_locations));
            group = (Group) getArguments().getSerializable(getString(R.string.intent_group));
            if (getArguments().getSerializable(getString(R.string.intent_group_place)) != null) {
                place = (Place) getArguments().getSerializable(getString(R.string.intent_group_place));
                slider.setValue(place.getPlace_radius());
                geofenceTitle.getEditText().setText(place.getPlace_title());
            } else {
                choosenPosition = new LatLng(userLocation.getGeo_point().getLatitude(), userLocation.getGeo_point().getLongitude());
                DocumentReference geoRef = mDb
                        .collection(getString(R.string.collection_groups))
                        .document(group.getGroup_id())
                        .collection(getString(R.string.collection_group_places))
                        .document();
                place = new Place("Place Title", geoRef.getId(), new com.example.grouptracker.Model.LatLng(userLocation.getGeo_point().getLatitude(), userLocation.getGeo_point().getLongitude()), 50, userLocation.getUser().getUserid(), "", false);
                slider.setValue(GEOFENCE_RADIUS);
                geofenceTitle.getEditText().setText("Place Title");
            }
        }
    }


    private void addGeofence(LatLng latLng, float radius, String geofence_id) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Geofence geofence = geofenceHelper.getGeofence(geofence_id, latLng, radius,
                Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_DWELL |
                        Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("addGeofence", "onSuccess: Geofence Added...");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = geofenceHelper.getErrorString(e);
                        Log.d("addGeofence", "onFailure: " + errorMessage);
                    }
                });
    }

    private void createGeofence() {
        if(geofenceMarker != null && geofenceRadius != null) {
            LatLng latLng = gMap.getProjection().getVisibleRegion().latLngBounds.getCenter();
            choosenPosition = latLng;
            geofenceMarker.setPosition(latLng);
            geofenceRadius.setCenter(latLng);
            geofenceRadius.setRadius(GEOFENCE_RADIUS);
        } else {
            LatLng latLng = gMap.getProjection().getVisibleRegion().latLngBounds.getCenter();
            choosenPosition = latLng;
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(place.getPlace_title());
            geofenceMarker = gMap.addMarker(markerOptions);
            addCircle(latLng, GEOFENCE_RADIUS);
        }
    }

    private void addCircle(LatLng latLng, float radius) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 100, 200, 100));
        circleOptions.fillColor(Color.argb(64, 100, 200, 100));
        circleOptions.strokeWidth(4);
        geofenceRadius = gMap.addCircle(circleOptions);
    }

    private void resetMap(){
        if(gMap != null) {
            gMap.clear();
        }
    }

    private void moveCompas() {
        final ViewGroup parent = (ViewGroup) mMapView.findViewWithTag("GoogleMapMyLocationButton").getParent();
        parent.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Resources r = getResources();
                    //convert our dp margin into pixels
                    int marginPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, r.getDisplayMetrics());
                    // Get the map compass view
                    View mapCompass = parent.getChildAt(4);

                    // create layoutParams, giving it our wanted width and height(important, by default the width is "match parent")
                    RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(mapCompass.getHeight(),mapCompass.getHeight());
                    // position on top right
                    rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
                    rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
                    //give compass margin
                    rlp.setMargins(30, 300, marginPixels, marginPixels);
                    mapCompass.setLayoutParams(rlp);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mMapView != null) {
            mMapView.onDestroy();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_full_screen_map:{
                if(mMapLayoutState == MAP_LAYOUT_STATE_CONTRACTED){
                    mMapLayoutState = MAP_LAYOUT_STATE_EXPANDED;
                    fullScreenBtn.startAnimation(rotate_forward);
                    expandMapAnimation();
                }
                else if(mMapLayoutState == MAP_LAYOUT_STATE_EXPANDED){
                    mMapLayoutState = MAP_LAYOUT_STATE_CONTRACTED;
                    fullScreenBtn.startAnimation(rotate_backward);
                    contractMapAnimation();
                }
                break;
            }
            case R.id.btn_my_location:{
                setCameraView();
                createGeofence();
                break;
            }
            case R.id.closeBtn:{
                goToPlaces();
                break;
            }

            case R.id.saveBtn:{
                saveGeofence();
                break;
            }

        }
    }

    private void saveGeofence() {
        DocumentReference geoRef = mDb
                .collection(getString(R.string.collection_groups))
                .document(group.getGroup_id())
                .collection(getString(R.string.collection_group_places))
                .document(place.getPlace_id());

        DocumentReference userPlaceRef = mDb
                .collection(getString(R.string.collection_users))
                .document(userLocation.getUser().getUserid())
                .collection(getString(R.string.collection_group_places))
                .document(place.getPlace_id());

        if(geofenceTitle.getEditText().getText().length() > 1) {
            place.setPlace_title(String.valueOf(geofenceTitle.getEditText().getText()));
        } else {
            Toast.makeText(getActivity(), "Geofence title should be atleast 2 letters", Toast.LENGTH_SHORT).show();
        }
        place.setPlace_radius(slider.getValue());
        place.setNotifications(true);
        place.setPlace_latLng(new com.example.grouptracker.Model.LatLng(choosenPosition.latitude, choosenPosition.longitude));
        place.setAddress(address.getAddressLine(0));
        userPlaceRef.set(place);
        geoRef.set(place).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    goToPlaces();
                    addGeofence(new LatLng(place.getPlace_latLng().getLatitude(), place.getPlace_latLng().getLongitude()), place.getPlace_radius(), place.getPlace_id());
                }
            }
        });
    }

    private void goToPlaces() {
        Log.d("MapsActivity-PlaceFrag", "replacing fragment with Places Fragment");
        PlacesFragment placesFragment = PlacesFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putSerializable(getString(R.string.intent_group), group);
        bundle.putSerializable(getString(R.string.intent_user_locations), userLocation);
        placesFragment.setArguments(bundle);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_fragment, placesFragment, "PlacesFragment");
        transaction.addToBackStack("PlacesFragment");
        transaction.commit();
    }

    private void expandMapAnimation(){
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                50,
                100);
        mapAnimation.setDuration(400);

        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(editGeofence);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                50,
                0);
        recyclerAnimation.setDuration(400);

        recyclerAnimation.start();
        mapAnimation.start();
    }

    private void contractMapAnimation(){
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                100,
                50);
        mapAnimation.setDuration(400);

        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(editGeofence);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                0,
                50);
        recyclerAnimation.setDuration(400);

        recyclerAnimation.start();
        mapAnimation.start();
    }

    @Override
    public void onCameraMoveStarted(int i) {
        isMoving = true;
    }

    @Override
    public void onCameraIdle() {
        if(isMoving) {
            isMoving = false;
            Geocoder geocoder = new Geocoder(getActivity());
            addressList = null;
            try{
                addressList = geocoder.getFromLocation(choosenPosition.latitude, choosenPosition.longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(addressList.size() > 0) {
                address = addressList.get(0);
                searchView.setQuery(address.getAddressLine(0).split(",")[0], false);
                searchView.clearFocus();
            }
        }
    }
}
