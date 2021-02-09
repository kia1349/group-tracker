package com.example.grouptracker.Main.Maps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grouptracker.Interface.APIService;
import com.example.grouptracker.Main.Authentication.MainActivity;
import com.example.grouptracker.Main.Authentication.SignInActivity;
import com.example.grouptracker.Model.Client;
import com.example.grouptracker.Model.Data;
import com.example.grouptracker.Model.Group;
import com.example.grouptracker.Model.LatLng;
import com.example.grouptracker.Model.Place;
import com.example.grouptracker.Model.Token;
import com.example.grouptracker.Model.User;
import com.example.grouptracker.Model.UserLocation;
import com.example.grouptracker.R;
import com.example.grouptracker.Services.LocationService;
import com.example.grouptracker.Utils.Common;
import com.example.grouptracker.Utils.GeofenceEvent;
import com.example.grouptracker.Utils.GeofenceHelper;
import com.example.grouptracker.Utils.LocationEvent;
import com.example.grouptracker.Utils.MyResponse;
import com.example.grouptracker.Utils.NotificationSender;
import com.example.grouptracker.Utils.UserClient;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.common.collect.Maps;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.w3c.dom.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.grouptracker.Utils.Common.ERROR_DIALOG_REQUEST;
import static com.example.grouptracker.Utils.Common.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.grouptracker.Utils.Common.PERMISSIONS_REQUEST_ENABLE_GPS;
import static com.example.grouptracker.Utils.Common.isFullscreenViewActive;

public class MapsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // CODES
    public static final int RESULT_LOCATION = 11;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 12;
    String TAG = "MapsActivity";

    // LAYOUTS
    private Toolbar toolbar;
    private LinearLayout toolbar_container;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawerLayoutLeft;
    private NavigationView navigationViewLeft;
    private TextView curr_name, curr_email;
    private ImageView pic;
    private Spinner spinner;
    private ImageButton fullscreenButton;
    private ImageButton updatesButton;
    private FrameLayout container;
    private ProgressBar progressBar;
    private Dialog permissionsDialog;
    private ImageView closePopupBtn;
    private Button acceptBtn;
    private TextView popupTitle;
    private LinearLayout popupMsg;

    // USER & MAPS VARIABLES
    private FusedLocationProviderClient fusedLocationProviderClient;
    private List<Place> placesList = new ArrayList<>();
    private List<Group> groupsList = new ArrayList<>();
    private Set<String> placeIds = new HashSet<>();
    private Set<String> groupIds = new HashSet<>();
    private ArrayList<String> groupNames = new ArrayList<>();
    private List<User> userList = new ArrayList<User>();
    private ArrayList<UserLocation> userLocations = new ArrayList<UserLocation>();
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;
    private Group group;
    private Group family_group;
    private User currUser;
    private String imageUri;
    private ArrayList<Group> groups = new ArrayList<>();
    private String choosenGroup;
    private boolean isLocationTrackingOn;
    private boolean isGeofenceTrackingOn;
    private UserLocation userLocation = new UserLocation();
    private ArrayAdapter<String> groupNamesAdapter;
    private ListenerRegistration groupEventListener;
    private ListenerRegistration placeEventListener;
    private boolean locationPermissionGranted = false;
    private SupportMapFragment supportMapFragment;
    private ListenerRegistration userListEventListener;
    private UserLocation userPosition;
    private Place place;

    // FIREBASE VARIABLES
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    User user;
    private FirebaseFirestore mDb;
    private DatabaseReference user_information;
    private DatabaseReference user_locations;
    private DatabaseReference group_reference;
    private APIService apiService;

    // FRAGMENTS
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    MapsFragment mapsFragment;


    /////////////////////////////////////////////////////////// LOCATION ///////////////////////////////////////////////////////////

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
            @Override
            public void onComplete(@NonNull Task<android.location.Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    if (location != null) {
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        userLocation.setGeo_point(geoPoint);
                        userLocation.setTimestamp(null);
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
                        Log.d("getLastKnownLocation", userLocation.toString());
                        saveUserLocation();
                    }
                    startLocationService();
                }
            }
        });
    }

    private void saveUserLocation(){
        if(userLocation != null) {
            DocumentReference locationRef = mDb
                    .collection(getString(R.string.collection_user_locations))
                    .document(FirebaseAuth.getInstance().getUid());

            locationRef.set(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d("saveUserLocation", "Uploaded users location: "+FirebaseAuth.getInstance().getUid());
                    }
                }
            });
        }
    }

    private void updateToken(final FirebaseUser firebaseUser) {
        final DocumentReference tokens = mDb
                .collection(getString(R.string.collection_tokens))
                .document(firebaseUser.getUid());

        // Get token
        firebaseUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if(task.isSuccessful()) {
                    String idToken = task.getResult().getToken();
                    Token token = new Token(idToken);
                    tokens.set(token);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error updating token: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
            serviceIntent.setAction(Common.ACTION_START_LOCATION_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                MapsActivity.this.startForegroundService(serviceIntent);
            }else{
                startService(serviceIntent);
            }
        }
    }

    private void stopLocationService() {
        if(isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(getApplicationContext(), LocationService.class);
            serviceIntent.setAction(Common.ACTION_STOP_LOCATION_SERVICE);
            startService(serviceIntent);
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if(activityManager != null) {
            for (ActivityManager.RunningServiceInfo service:
            activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if(LocationService.class.getName().equals(service.service.getClassName())) {
                    if(service.foreground){
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    //////////////////////////////////////////////// NAVIGATION ///////////////////////////////////////////////////////

    public void dropdown(View v) {
        if(spinner.getSelectedItem() == null) {
            spinner.performClick();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawerLeft = findViewById(R.id.drawer_layout_left);
        // if its opened/started/positioned from the start or the left
        if (drawerLeft.isDrawerOpen(GravityCompat.START)) {
            // close it by pushing it to the start or left. Push it back to where it came from
            drawerLeft.closeDrawer(GravityCompat.START);
        } else {

        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                if (drawerLayoutLeft.isDrawerOpen(GravityCompat.START)) {
                    drawerLayoutLeft.closeDrawer(GravityCompat.START);
                }
                else {
                    drawerLayoutLeft.openDrawer(GravityCompat.START);
                }
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void replaceChatFragment() {
        Log.d("MapsActivity-codeFrag", "replacing fragment with Code Fragment");
        CodeFragment codeFragment = CodeFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putSerializable(getString(R.string.intent_group), group);
        codeFragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_fragment, codeFragment, "CodeFragment");
        transaction.addToBackStack("CodeFragment");
        transaction.commit();
    }

    private void replaceCodeFragment() {
        Log.d("MapsActivity-codeFrag", "replacing fragment with Code Fragment");
        CodeFragment codeFragment = CodeFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putSerializable(getString(R.string.intent_group), group);
        codeFragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_fragment, codeFragment, "CodeFragment");
        transaction.addToBackStack("CodeFragment");
        transaction.commit();
    }

    private void replaceJoinGroupFragment() {
        Log.d("MapsActivity-joinFrag", "replacing fragment with Join Group Fragment");
        JoinGroupFragment joinGroupFragment = JoinGroupFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putSerializable(getString(R.string.intent_user), user);
        bundle.putSerializable(getString(R.string.intent_group), group);
        bundle.putSerializable(getString(R.string.intent_user_locations), userLocation);
        bundle.putSerializable(getString(R.string.collection_group_list), (Serializable) groupsList);
        joinGroupFragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_fragment, joinGroupFragment, "JoinGroupFragment");
        transaction.addToBackStack("JoinGroupFragment");
        transaction.commit();
    }

    private void replaceMapsFragment() {
        Log.d("MapsActivity-mapsFrag", "replacing fragment with Maps Fragment");
        mapsFragment = MapsFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putSerializable(getString(R.string.intent_user), user);
        bundle.putSerializable(getString(R.string.intent_group), group);
        bundle.putSerializable(getString(R.string.intent_user_locations), userLocation);
        bundle.putSerializable("place", place);
        mapsFragment.setArguments(bundle);

        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container_fragment, mapsFragment, "MapsFragment");
        fragmentTransaction.addToBackStack("MapsFragment");
        fragmentTransaction.commit();
    }

    private void replaceProfileFragment() {
        Log.d("MapsActivity-userFrag", "replacing fragment with Profile Fragment");
        DocumentReference userRef = mDb.collection(getString(R.string.collection_users))
                .document(FirebaseAuth.getInstance().getUid());
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    User currUser = task.getResult().toObject(User.class);
                    ProfileFragment profileFragment = ProfileFragment.newInstance();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(getString(R.string.intent_user), user);
                    bundle.putSerializable(getString(R.string.intent_group), group);
                    bundle.putSerializable(getString(R.string.intent_user_locations), userLocation);
                    profileFragment.setArguments(bundle);

                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container_fragment, profileFragment, "ProfileFragment");
                    transaction.addToBackStack("ProfileFragment");
                    transaction.commit();
                }
            }
        });
    }

    private void replacePlacesFragment() {
        Log.d("MapsActivity-PlaceFrag", "replacing fragment with Places Fragment");
        PlacesFragment placesFragment = PlacesFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putSerializable(getString(R.string.intent_group), group);
        bundle.putSerializable(getString(R.string.intent_user_locations), userLocation);
        placesFragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_fragment, placesFragment, "PlacesFragment");
        transaction.addToBackStack("PlacesFragment");
        transaction.commit();
    }

    private void replaceSettingsFragment() {
        Log.d("MapsActivity-SettFrag", "replacing fragment with Settings Fragment");
        SettingsFragment settingsFragment = SettingsFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putSerializable(getString(R.string.intent_user), user);
        bundle.putSerializable(getString(R.string.intent_group), group);
        bundle.putSerializable(getString(R.string.intent_user_locations), userLocation);
        settingsFragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_fragment, settingsFragment, "SettingsFragment");
        transaction.addToBackStack("SettingsFragment");
        transaction.commit();
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

    View.OnClickListener onButtonClickListener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onClick(View v) {
            if (v.getId() == fullscreenButton.getId()) { // Fullcreen button
                if (!isFullscreenViewActive) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        LinearLayout.LayoutParams paramsToolbar = (LinearLayout.LayoutParams) toolbar.getLayoutParams();
                        MapsActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                        MapsActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                        MapsActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                        MapsActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        MapsActivity.this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                        isFullscreenViewActive = true;
                        toolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
                        paramsToolbar.setMargins(0, 110, 0, 0);
                        fullscreenButton.setBackground(getDrawable(R.drawable.group_dropdown_background));
                        updatesButton.setBackground(getDrawable(R.drawable.group_dropdown_background));
                        fullscreenButton.setImageTintList(getColorStateList(R.color.colorSecondary));
                        actionBarDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.spinner_background_black));
                        toolbar.setLayoutParams(paramsToolbar);
                    }
                } else {
                    LinearLayout.LayoutParams paramsToolbar = (LinearLayout.LayoutParams) toolbar.getLayoutParams();
                    MapsActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    MapsActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    MapsActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                    MapsActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                    MapsActivity.this.getWindow().setStatusBarColor(getResources().getColor(R.color.colorSecondary));
                    isFullscreenViewActive = false;
                    toolbar.setBackgroundColor(getResources().getColor(R.color.colorSecondary));
                    paramsToolbar.setMargins(0, 0, 0, 0);
                    fullscreenButton.setBackground(getDrawable(R.drawable.rounded_shape_transparent_black));
                    updatesButton.setBackground(getDrawable(R.drawable.rounded_shape_transparent_black));
                    fullscreenButton.setImageTintList(getColorStateList(R.color.colorPrimary));
                    actionBarDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.colorPrimary));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        MapsActivity.this.getWindow().getDecorView().setSystemUiVisibility(0);
                    }
                    toolbar.setLayoutParams(paramsToolbar);
                }
            }
            if (v.getId() == updatesButton.getId()) { // Location button
                if(isLocationServiceRunning()) {
                    stopLocationService();
                    final DocumentReference userRef = FirebaseFirestore.getInstance()
                            .collection(getString(R.string.collection_users))
                            .document(FirebaseAuth.getInstance().getUid());

                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            User user = task.getResult().toObject(User.class);
                            user.setIsSharing("false");
                            userRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                    }
                                }
                            });
                        }
                    });
                    updatesButton.setImageDrawable(getDrawable(R.drawable.ic_location_off_black_24dp));
                } else if(!isLocationServiceRunning()) {
                    startLocationService();
                    final DocumentReference userRef = FirebaseFirestore.getInstance()
                            .collection(getString(R.string.collection_users))
                            .document(FirebaseAuth.getInstance().getUid());

                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            User user = task.getResult().toObject(User.class);
                            user.setIsSharing("true");
                            userRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                    }
                                }
                            });
                        }
                    });
                    updatesButton.setImageDrawable(getDrawable(R.drawable.ic_location_on_black_24dp));
                }
            }
        }
    };

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_map: {
                hideKeyboard(MapsActivity.this);
                getGroup(choosenGroup);
                replaceMapsFragment();
                break;
            }
            case R.id.nav_profile: {
                replaceProfileFragment();
                break;
            }
            case R.id.nav_places: {
                replacePlacesFragment();
                break;
            }
            case R.id.nav_join_group: {
                replaceJoinGroupFragment();
                break;
            }
            case R.id.nav_code: {
                replaceCodeFragment();
                break;
            }
            case R.id.nav_settings: {
                replaceSettingsFragment();
                break;
            }
            case R.id.nav_chat: {
                replaceChatFragment();
                break;
            }
            case R.id.nav_sign_out: {
                stopLocationService();
                signOut();
                Toast.makeText(this, "You're logged out!", Toast.LENGTH_SHORT).show();
                break;
            }
        }
        item.setChecked(true);
        drawerLayoutLeft.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut() {
        DocumentReference ref = mDb
                .collection(getString(R.string.collection_tokens))
                .document(firebaseUser.getUid());

        ref.delete();

        for(UserInfo userInfo : FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
            if(userInfo.getProviderId().equals("facebook.com")) {
                LoginManager.getInstance().logOut();
                FirebaseAuth.getInstance().signOut();
            } else if (userInfo.getProviderId().equals("google.com")) {
                GoogleSignIn.getClient(
                        this,
                        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                ).signOut();
                FirebaseAuth.getInstance().signOut();
            } else {
                FirebaseAuth.getInstance().signOut();
            }
        }
        stopLocationService();
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.remove(mapsFragment);
        fragmentTransaction.commit();
        Intent accountIntent = new Intent(MapsActivity.this, MainActivity.class);
        startActivity(accountIntent);
        finish();
    }

    /////////////////////////////////////////// CHECK MAP SERVICES & PERMISSIONS /////////////////////////////////////////////////

    private void checkMapServices(){
        if(isServicesOK()){
            isGpsEnabled();
        }
    }

    public boolean isGpsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            return false;
        }
        return true;
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapsActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapsActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
    }

    public void HidePermissionsPopup() {
        permissionsDialog.hide();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(resultCode) {
            case RESULT_OK: {
                switch (requestCode) {
                    case PERMISSIONS_REQUEST_ENABLE_GPS: {
                        HidePermissionsPopup();
                    }
                }
            }
        }
    }

    ////////////////////////////////////////////////////// SETUP //////////////////////////////////////////////////////////

    public void ShowGPSAlert() {
        permissionsDialog.setContentView(R.layout.dialog_gps_permission);
        closePopupBtn = permissionsDialog.findViewById(R.id.closePopupBtn);
        acceptBtn = permissionsDialog.findViewById(R.id.popupBtn);
        popupTitle = permissionsDialog.findViewById(R.id.popupTitle);

        closePopupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissionsDialog.dismiss();
            }
        });

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
            }
        });

        permissionsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        permissionsDialog.show();
    }

    private void initMenuDrawer() {
        // View linking
        drawerLayoutLeft = findViewById(R.id.drawer_layout_left);
        navigationViewLeft = findViewById(R.id.nav_view_left);
        View header = navigationViewLeft.getHeaderView(0); // User info widget
        curr_name = header.findViewById(R.id.name_text);
        curr_email = header.findViewById(R.id.email_text);
        pic = header.findViewById(R.id.profile_pic);

        // Init drawer
        navigationViewLeft.setNavigationItemSelectedListener(this);
        navigationViewLeft.setCheckedItem(R.id.nav_map);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayoutLeft, R.string.open, R.string.close); // Set menu toggle button (Burger)
        drawerLayoutLeft.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        // Fill with user info
        //fillDrawerWithInfo();
    }

    private void initToolbar() {
        // View linking
        toolbar = findViewById(R.id.toolbar); // Toolbar
        toolbar_container = findViewById(R.id.toolbar_container);
        spinner = toolbar.findViewById(R.id.group_spinner); // Dropwdown (Group Spinner)
        fullscreenButton = toolbar.findViewById(R.id.btn_full_screen_map);
        updatesButton = toolbar.findViewById(R.id.btn_updates);

        // Init toolbar
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        toolbar.setClipToOutline(true);

        setFullscreenAndStatusBar(); // Make Android window.mode fullscreen and color status bar

        fullscreenButton.setOnClickListener(onButtonClickListener);
        updatesButton.setOnClickListener(onButtonClickListener);
    }

    public void setFullscreenAndStatusBar() {
        // SET WINDOW FULLSCREEN + STATUS & TOOLBAR TRANSPARENCY
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            isFullscreenViewActive = true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorSecondaryLight));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
    private void getInfoFromPreviousIntent() {
        Intent intent = getIntent();
        if(intent.getExtras() != null) {
            user = (User) intent.getExtras().getSerializable("user");
            group = (Group) intent.getExtras().getSerializable("group");
            //place = (Place) intent.getExtras().getSerializable("place");
            //groupsList = (List<Group>) intent.getSerializableExtra("groupsList");
            //placesList = (List<Place>) intent.getExtras().getSerializable("userPlaces");
            userLocation.setUser(user);
            ((UserClient)(getApplicationContext())).setUser(user);
            fillDrawerWithInfo(user);
            updateToken(firebaseUser);
            getLastKnownLocation();
            getGroups();
            getPlaces();
            Log.d("MapsActivity", "onComplete: successfully set the user client.");
        } else {
            Toast.makeText(MapsActivity.this, "Could not get user details!", Toast.LENGTH_SHORT).show();
        }
    }

    private void fillDrawerWithInfo(final User u) {
        // Data sent from MainActivity to Maps Activity
        curr_name.setText(u.getName());
        curr_email.setText(u.getEmail());
        imageUri = u.getImageUri();
        try {
            Picasso.get().load(Uri.parse(imageUri)).placeholder(R.mipmap.default_male_photo).into(pic);
        } catch (Exception e){
            Picasso.get().load(R.mipmap.default_male_photo).into(pic);
        }
        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceProfileFragment();
                MenuItem profile = navigationViewLeft.getMenu().getItem(1);
                profile.setChecked(true);
                drawerLayoutLeft.closeDrawer(GravityCompat.START);
            }
        });
    }

    private void getPlaces() {
        CollectionReference placesCollection = mDb
                .collection(getString(R.string.collection_users))
                .document(user.getUserid())
                .collection(getString(R.string.collection_group_places));

        placeEventListener = placesCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                Log.d("TAG", "onEvent: called.");

                if (e != null) {
                    Log.e("TAG", "onEvent: Listen failed.", e);
                    return;
                }

                if(queryDocumentSnapshots != null) {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Place p = doc.toObject(Place.class);

                        if(!placeIds.contains(p.getPlace_id())) {
                            placeIds.add(p.getPlace_id());
                            if(p.getPlace_latLng() == null) {
                                place = p;
                            }
                            if(p.isNotifications() && p.getPlace_latLng() != null) {
                                addGeofence(new com.google.android.gms.maps.model.LatLng(p.getPlace_latLng().getLatitude(), p.getPlace_latLng().getLongitude()), p.getPlace_radius(), p.getPlace_id());
                            }
                            placesList.add(p);
                        }
                    }
                }
            }
        });
    }

    private void addGeofence(com.google.android.gms.maps.model.LatLng latLng, float radius, String geofence_id) {
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    private void getGroups() {
        CollectionReference groupsCollection = mDb
                .collection(getString(R.string.collection_users))
                .document(FirebaseAuth.getInstance().getUid())
                .collection(getString(R.string.collection_group_list));

        groupEventListener = groupsCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                Log.d("TAG", "onEvent: called.");

                if (e != null) {
                    Log.e("TAG", "onEvent: Listen failed.", e);
                    return;
                }

                if(queryDocumentSnapshots != null) {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Group g = doc.toObject(Group.class);

                        if(!groupIds.contains(g.getGroup_id())) {
                            groupIds.add(g.getGroup_id());
                            groupNames.add(g.getGroup_title());
                            groupsList.add(g);
                        }
                    }
                    for(Group f : groupsList) {
                        if(f.getGroup_admin().equals(FirebaseAuth.getInstance().getUid()))
                        {
                            family_group = f;
                            group = family_group;
                            //Toast.makeText(MapsActivity.this, "Family group: "+group.getGroup_title(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    groupNamesAdapter.notifyDataSetChanged();
                    //Toast.makeText(MapsActivity.this, "Number of groups: "+groupNames.size(), Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void getGroup(String group_name) {
        Log.d("MapsActivity-getGroup", "started");
        if(groupsList.size() != 0) {
            for(Group g : groupsList) {
                if(g.getGroup_title().equals(group_name)) {
                    group = g;
                    choosenGroup = group.getGroup_title();
                }
            }
        }
    }

    private void initGroupsAdapter() {
        groupNamesAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, groupNames);
        groupNamesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(groupNamesAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected_group = (String) parent.getItemAtPosition(position);
                choosenGroup = selected_group;
                getGroup(choosenGroup);
                replaceMapsFragment();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkMapServices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(groupEventListener != null) {
            groupEventListener.remove();
        }
        if(placeEventListener != null) {
            placeEventListener.remove();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        signOut();
        super.onStop();
    }

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationEvent(LocationEvent event) {
        userLocation = event.userLocation;
        /*
        if(place.getGeoAdmin().equals(user.getUserid()) && place.getAddress() == null && userLocation.getAddress() != null) {
            setFirstGeofence();
        }
         */
        //Toast.makeText(getApplicationContext(), userLocation.getAddress(), Toast.LENGTH_SHORT).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGeofenceEvent(GeofenceEvent event) {
        Place p = event.place;

        DocumentReference groupRef = mDb
                .collection(getString(R.string.collection_groups))
                .document(group.getGroup_id())
                .collection(getString(R.string.collection_group_places))
                .document(p.getPlace_id());

        DocumentReference userRef = mDb
                .collection(getString(R.string.collection_users))
                .document(user.getUserid())
                .collection(getString(R.string.collection_group_places))
                .document(p.getPlace_id());

        groupRef.set(p);
        userRef.set(p);


        /*
        if(place.getGeoAdmin().equals(user.getUserid()) && place.getAddress() == null && userLocation.getAddress() != null) {
            setFirstGeofence();
        }
         */
        //Toast.makeText(getApplicationContext(), userLocation.getAddress(), Toast.LENGTH_SHORT).show();
    }


/*
    private void setFirstGeofence() {
        DocumentReference placeRef = mDb
                .collection(getString(R.string.collection_groups))
                .document(group.getGroup_id())
                .collection(getString(R.string.collection_group_places))
                .document(place.getPlace_id());

        place.setAddress(userLocation.getAddress());
        place.setNotifications(false);
        place.setPlace_latLng(new com.example.grouptracker.Model.LatLng(userLocation.getGeo_point().getLongitude(), userLocation.getGeo_point().getLatitude()));

        placeRef.set(place);
    }
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        permissionsDialog = new Dialog(this);
        Log.d("MapsActivity", "onCreate()");

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);

        auth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser(); // User instanced
        mDb = FirebaseFirestore.getInstance();
        user_information = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION); // Database referenced
        user_locations = FirebaseDatabase.getInstance().getReference(Common.USER_LOCATIONS);
        group_reference = FirebaseDatabase.getInstance().getReference(Common.GROUPS);
        container = findViewById(R.id.container_fragment); // Fragment container

        // treba še preverjat na začetku ali je lokacija oz. geofence vklopljene
        initToolbar();
        initMenuDrawer();
        getInfoFromPreviousIntent();
        initGroupsAdapter();
    }
}
