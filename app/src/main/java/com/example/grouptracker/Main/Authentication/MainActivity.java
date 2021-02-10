package com.example.grouptracker.Main.Authentication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grouptracker.Main.Maps.MapsActivity;
import com.example.grouptracker.Model.Group;
import com.example.grouptracker.Model.Place;
import com.example.grouptracker.Model.User;
import com.example.grouptracker.R;
import com.example.grouptracker.Utils.UserClient;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.errors.ApiException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.grouptracker.Utils.Common.PERMISSIONS_REQUEST_ENABLE_GPS;

public class MainActivity extends AppCompatActivity{

    public static final String TAG ="MainActivity";
    private static final String TAG_FB = "FacebookAuthentication";

    // FIREBASE
    private FirebaseAuth auth; // For authenticationg with Firebase
    private FirebaseUser firebaseUser; // For getting user details from firebase
    private FirebaseFirestore mDb;
    private FirebaseAuth.AuthStateListener authListener;
    private Group group;
    private Place place;
    private CollectionReference userReference;

    // LOGIN BUTTONS
    private MaterialButton useGoogleButton;
    private MaterialButton useFacebookButton;
    private MaterialButton useEmailButton;
    private MaterialButton requestPermissionsButton;
    private MaterialButton checkPermissionsButton;
    private LoginButton originalFacebookButton;

    // PERMISSIONS
    private boolean locationPermission = false;
    private boolean storagePermission = false;
    private boolean backgroundPermission = false;
    private boolean allPermissions = false;
    private final int CODE_STORAGE_PERMISSION = 11;
    private final int CODE_LOCATION_PERMISSION = 12;
    private final int CODE_ALL_PERMISSIONS = 22;

    // DIALOG
    private Dialog permissionsDialog;
    private ImageView closePopupBtn;
    private Button acceptBtn;
    private TextView popupTitle;
    private LinearLayout popupMsg;
    private boolean isPopup = false;

    // GOOGLE
    private GoogleSignInClient googleSignInClient;
    private int RC_SIGN_IN = 1; // activity for result 1

    // FACEBOOK
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

    // ELEMENTS
    private ImageView logo;
    CircleImageView mLogoImage;
    ProgressDialog pd;
    DatabaseReference groupReference;
    ProgressBar progressBar;

    // user
    String name;
    String email;
    String surrname;
    String family;
    String family_name;
    String imageUri;
    String userId;
    User user;
    String gender;
    String isSharing;
    int locationUpdateInterval;

    // group
    String code;
    String date;
    String title;
    String groupId;
    String admin;
    Place home;
    Place workplace;
    String homeId;
    String workplaceId;

    // place
    private List<Group> groupsList = new ArrayList<>();
    private Set<String> groupIds = new HashSet<>();
    private ArrayList<String> groupNames = new ArrayList<>();
    List<User> usersList = new ArrayList<>();
    List<Place> placeList = new ArrayList<>();
    List<String> memberList = new ArrayList<>();
    List<String> groupList = new ArrayList<>();

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(authListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(authListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(authListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionsDialog = new Dialog(this);
        Log.d("MainActivity", "onCreate");

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        mDb = FirebaseFirestore.getInstance();
        userReference = mDb.collection(getString(R.string.collection_users));

        // authentication buttons
        useEmailButton = findViewById(R.id.use_email_button);
        useGoogleButton = findViewById(R.id.use_google_button);
        useFacebookButton = findViewById(R.id.use_facebook_button);

        // permission buttons
        requestPermissionsButton = findViewById(R.id.grant_permissions_btn);
        checkPermissionsButton = findViewById(R.id.check_permissions_btn);

        // progress animation
        progressBar = findViewById(R.id.updateProgressBar);

        setupWindow();
        setupLogoAnimation();
        if(areAllPermissionsGranted()) {
            showAuthenticationButtons();
            checkBatteryOptimization();
        } else if(isLocationPermissionGranted() && isStoragePermissionGranted() && !isBackgroundPermissionGranted()) {
            showAuthenticationButtons();
            ShowBackgroundLocationAlert();
        } else if(!isStoragePermissionGranted()) {
            showPermissionButtons();
            ShowPermissionsPopup();
        } else if(isStoragePermissionGranted() && !isLocationPermissionGranted()) {
            showPermissionButtons();
            ShowPermissionsPopup();
        } else {
            showPermissionButtons();
            ShowPermissionsPopup();
        }

        setupFirebaseAuth();
    }

    ////////////////////////////////////////// SETUP //////////////////////////////////////////

    private void setupWindow() {
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorSecondaryLight));
    }

    private void setupLogoAnimation() {
        logo = findViewById(R.id.logo);
        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logo.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotation_cw));
            }
        });
    }

    private void startProgressAnimation() {
        Animation loading = AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotation_cw);
        loading.setRepeatCount(Animation.INFINITE);
        loading.setInterpolator(new AccelerateDecelerateInterpolator());
        progressBar.setVisibility(View.VISIBLE);
        progressBar.startAnimation(loading);
    }

    private void stopProgressAnimation() {
        progressBar.clearAnimation();
        progressBar.setVisibility(View.GONE);
    }

    private void checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getApplicationContext().getPackageName();
            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                ShowBatteryOptimizationAlert();
            }
        }
    }

    public void showAuthenticationButtons() {
        useEmailButton.setVisibility(View.VISIBLE);
        useEmailButton.setEnabled(true);
        useGoogleButton.setVisibility(View.VISIBLE);
        useGoogleButton.setEnabled(true);
        useFacebookButton.setVisibility(View.VISIBLE);
        useFacebookButton.setEnabled(true);
        requestPermissionsButton.setVisibility(View.GONE);
        requestPermissionsButton.setEnabled(false);
        checkPermissionsButton.setVisibility(View.GONE);
        checkPermissionsButton.setEnabled(false);

        // LOGIN WITH EMAIL
        useEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginWithEmail = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(loginWithEmail);
                finish();
            }
        });

        // LOGIN WITH GOOGLE
        useGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProgressAnimation();
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                googleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);

                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        // LOGIN WITH FACEBOOK
        callbackManager = CallbackManager.Factory.create();

        useFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProgressAnimation();
                useFacebookButton.setEnabled(false);

                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("email", "public_profile"));
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG_FB, "onSuccess" + loginResult);
                        handleFacebookToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG_FB, "onCancel");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(TAG_FB, "onError" + error);
                    }
                });
            }
        });
    }

    public void showPermissionButtons() {
        useEmailButton.setVisibility(View.GONE);
        useEmailButton.setEnabled(false);
        useGoogleButton.setVisibility(View.GONE);
        useGoogleButton.setEnabled(false);
        useFacebookButton.setVisibility(View.GONE);
        useFacebookButton.setEnabled(false);
        requestPermissionsButton.setVisibility(View.VISIBLE);
        requestPermissionsButton.setEnabled(true);
        checkPermissionsButton.setVisibility(View.VISIBLE);
        checkPermissionsButton.setEnabled(true);

        requestPermissionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowPermissionsPopup();
            }
        });

        checkPermissionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions();
            }
        });
    }

    ////////////////////////////////////////// AUTHENTICATION //////////////////////////////////////////

    private void setupFirebaseAuth(){
        Log.d(TAG, "MainActivity-setupAuth: started.");
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                auth = firebaseAuth;
                firebaseUser = auth.getCurrentUser();
                if (firebaseUser != null) {
                    if(!isPopup) getUser();
                } else {
                    showAuthenticationButtons();
                }
            }
        };
    }

    public String generateCode() {
        Random r = new Random();
        int n = 100000 + r.nextInt(900000);
        return String.valueOf(n);
    }

    public String generateDate() {
        Date myDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd.MM.yyyy hh:mm:ss", Locale.getDefault());
        return dateFormat.format(myDate);
    }

    private void getUserDetails() {
        name = user.getName();
        surrname = user.getSurrname();
        email = user.getEmail();
        gender = user.getGender();
        isSharing = user.getIsSharing();
        imageUri = user.getImageUri();
        userId = user.getUserid();
        date = user.getDate();
        family = user.getFamily();
        family_name = user.getFamily_name();
        locationUpdateInterval = user.getLocationUpdateInterval();
    }

    private void getGroupDetails() {
        final DocumentReference groupDocumentReference = mDb
                .collection(getString(R.string.collection_groups))
                .document(user.getFamily());

        groupDocumentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    group = documentSnapshot.toObject(Group.class);
                    getGroup();
                    //getGroups();
                }
            }
        });
    }

    private void getGroup() {
        admin = group.getGroup_admin();
        title = group.getGroup_title();
        code = group.getGroup_code();
        groupId = group.getGroup_id();
    }

    private void getGroups() {
        CollectionReference userGroupsReference = mDb
                .collection(getString(R.string.collection_users))
                .document(firebaseUser.getUid())
                .collection(getString(R.string.collection_group_list));

        userGroupsReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
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
                }
            }
        });
    }

    private void getUser() {
        final DocumentReference userDocumentReference = mDb
                .collection(getString(R.string.collection_users))
                .document(firebaseUser.getUid());

        userDocumentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    user = documentSnapshot.toObject(User.class);
                    getUserDetails();
                    getGroupDetails();
                    ((UserClient) getApplicationContext()).setUser(user);
                    goToMapsActivity();
                } else if (!documentSnapshot.exists()) {
                    createUser();
                }
            }
        });
    }

    private void createUser() {
        createUserDetails();
        createGroupDetails();
        //createPlaceDetails();

        ((UserClient) getApplicationContext()).setUser(user);

        uploadUser();
        uploadGroup();
        //uploadPlaces();

        goToMapsActivity();
    }

    private void createUserDetails() {
        name = firebaseUser.getDisplayName();
        surrname = firebaseUser.getDisplayName();
        if (surrname.contains(" ")) {
            surrname = name.split(" ")[1];
        }
        email = firebaseUser.getEmail();
        gender = "Male";
        isSharing = "true";
        imageUri = firebaseUser.getPhotoUrl().toString();
        userId = firebaseUser.getUid();
        date = generateDate();
        user = new User(name, surrname, email, gender, isSharing, imageUri, userId, date, 4000);
    }

    private void uploadUser() {
        DocumentReference userDocumentReference = mDb
                .collection(getString(R.string.collection_users))
                .document(firebaseUser.getUid());

        userDocumentReference.set(user);
                DocumentReference userGroupsDocumentReference = mDb
                        .collection(getString(R.string.collection_users))
                        .document(firebaseUser.getUid())
                        .collection(getString(R.string.collection_group_list))
                        .document(groupId);

        userGroupsDocumentReference.set(group);

        /*
        DocumentReference userPlacesDocumentReference = mDb
                .collection(getString(R.string.collection_users))
                .document(firebaseUser.getUid())
                .collection(getString(R.string.collection_group_places))
                .document(homeId);

        userPlacesDocumentReference.set(home);

        userPlacesDocumentReference = mDb
                .collection(getString(R.string.collection_users))
                .document(firebaseUser.getUid())
                .collection(getString(R.string.collection_group_places))
                .document(workplaceId);

        userPlacesDocumentReference.set(workplace);
         */
    }

    private void createGroupDetails() {
        DocumentReference groupDocumentReference = mDb
                .collection(getString(R.string.collection_groups))
                .document();

        admin = user.getUserid();
        code = generateCode();
        groupId = groupDocumentReference.getId();
        title = surrname + " Family";
        group = new Group(title, groupId, admin, code);
        user.setFamily(groupId);
    }

    private void uploadGroup() {
        DocumentReference groupDocumentReference = mDb
                .collection(getString(R.string.collection_groups))
                .document(groupId);

        groupDocumentReference.set(group);

        DocumentReference groupMembersReference = mDb
                .collection(getString(R.string.collection_groups))
                .document(groupId)
                .collection(getString(R.string.collection_group_user_list))
                .document(firebaseUser.getUid());

        groupMembersReference.set(user);
    }

    private void createPlaceDetails() {
        DocumentReference homeDocumentReference = mDb
                .collection(getString(R.string.collection_groups))
                .document(group.getGroup_id())
                .collection(getString(R.string.collection_group_places)).document();

        homeId = homeDocumentReference.getId();

        home = new Place( user.getFamily_name()+"'s Place", homeId, null, 50, user.getUserid(), "", false);
        place = home;
        placeList.add(home);

        usersList.add(user);
        groupsList.add(group);
        memberList.add(admin);
        groupList.add(groupId);

        group.addPlace(home.getPlace_id());
        user.addPlace(place.getPlace_id());
    }

    private void uploadPlaces() {
        DocumentReference homeDocumentReference = mDb
                .collection(getString(R.string.collection_groups))
                .document(group.getGroup_id())
                .collection(getString(R.string.collection_group_places)).document(homeId);

        homeDocumentReference.set(home);
    }

    private void FirebaseGoogleAuth(GoogleSignInAccount acc) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(acc.getIdToken(), null);
        FirebaseAuth.getInstance().signInWithCredential(authCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            firebaseUser = task.getResult().getUser();
                            getUser();
                            Log.d("MainActivity-GoogleAuth", "SUCCESSFUL LOGIN WITH GOOGLE");
                        }
                        else {
                            Log.d("MainActivity-GoogleAuth", "FAILED FACEBOOK GOOGLE");
                        }
                    }
                });
    }

    private void handleFacebookToken(AccessToken accessToken) {
        Log.d(TAG_FB, "handleFacebookToken" + accessToken);

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            firebaseUser = task.getResult().getUser();
                            getUser();
                            Log.d("MainActivity-FbAuth", "SUCCESSFUL LOGIN WITH FACEBOOK");
                        }
                        else {
                            Log.d("MainActivity-FbAuth", "FAILED FACEBOOK LOGIN");
                        }
                    }
                });
    }

    public void goToMapsActivity() {
        Log.d("MainActivity-goToMaps", "Authenticated user as "+ firebaseUser.getEmail());
        Toast.makeText(MainActivity.this, "Authenticated as: " + firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", (Serializable) user);
        bundle.putSerializable("group", (Serializable) group);
        //bundle.putSerializable("place", (Serializable) home);
        //bundle.putSerializable("groupsList", (Serializable) groupsList);
        //bundle.putSerializable("userPlaces", (Serializable) placeList);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN) {
            if(resultCode == Activity.RESULT_OK) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try{
                    GoogleSignInAccount acc = task.getResult(ApiException.class);
                    FirebaseGoogleAuth(acc);
                } catch (ApiException e) {
                    stopProgressAnimation();
                    Toast.makeText(this, "Sign In Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                stopProgressAnimation();
                Toast.makeText(this, "Sign In Dismissed", Toast.LENGTH_SHORT).show();
            }
        } else if(requestCode == 0) {
            permissionsDialog.dismiss();
        }
    }

    ////////////////////////////////////////// PERMISSIONS //////////////////////////////////////////

    public void HidePermissionsPopup() {
        permissionsDialog.hide();
    }

    public void ShowPermissionsPopup() {
        isPopup = true;
        permissionsDialog.setContentView(R.layout.dialog_important_permissions);
        closePopupBtn = permissionsDialog.findViewById(R.id.closePopupBtn);
        acceptBtn = permissionsDialog.findViewById(R.id.popupBtn);
        popupTitle = permissionsDialog.findViewById(R.id.popupTitle);
        popupMsg = permissionsDialog.findViewById(R.id.popupMsg);

        closePopupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissionsDialog.dismiss();
            }
        });

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestAllPermission();
            }
        });

        permissionsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        permissionsDialog.show();
    }

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

    public void ShowBackgroundLocationAlert() {
        isPopup = true;
        permissionsDialog.setContentView(R.layout.dialog_background_location_permission);
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
                requestLocationPermission();
            }
        });

        permissionsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        permissionsDialog.show();
    }

    public void checkPermissions() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void ShowBatteryOptimizationAlert() {
        isPopup = true;
        permissionsDialog.setContentView(R.layout.dialog_battery_permission);
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
                openPowerSettings();
            }
        });

        permissionsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        permissionsDialog.show();
    }

    private void openPowerSettings() {
        startActivityForResult(new Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS), 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                HidePermissionsPopup();
                showAuthenticationButtons();
            }
            case CODE_ALL_PERMISSIONS: {
                if (Build.VERSION.SDK_INT >= 29) {
                    if ((grantResults.length > 0) && areAllPermissionsGranted()) {
                        HidePermissionsPopup();
                        showAuthenticationButtons();
                    } else if ((grantResults.length > 0) && !isLocationPermissionGranted()) {
                        showPermissionButtons();
                        ShowPermissionsPopup();
                    } else if ((grantResults.length > 0) && !isBackgroundPermissionGranted()){
                        showAuthenticationButtons();
                        ShowBackgroundLocationAlert();
                    }
                } else {
                    if ((grantResults.length > 0) && areAllPermissionsGranted()) {
                        HidePermissionsPopup();
                        showAuthenticationButtons();
                    } else if((grantResults.length > 0) && !isLocationPermissionGranted()){
                        showPermissionButtons();
                        ShowPermissionsPopup();
                    } else {
                        HidePermissionsPopup();
                        showAuthenticationButtons();
                    }
                }
            }
        }
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            return false;
        }
        return true;
    }

    private boolean isStoragePermissionGranted() {
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) +
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
           return false;
        }
        return true;
    }

    private boolean isLocationPermissionGranted() {
            if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) +
                    ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            return true;
    }

    private boolean isBackgroundPermissionGranted() {
        if(Build.VERSION.SDK_INT >= 29) {
                    if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean areAllPermissionsGranted() {
        if(isStoragePermissionGranted() && isLocationPermissionGranted() && isBackgroundPermissionGranted()) {
            return true;
        }
        return false;
    }

    private void requestAllPermission() {
        if(Build.VERSION.SDK_INT >= 29) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION))
            {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        },
                        CODE_ALL_PERMISSIONS
                );
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        },
                        CODE_ALL_PERMISSIONS
                );
            }
        } else {
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION))
            {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        CODE_ALL_PERMISSIONS
                );
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        CODE_ALL_PERMISSIONS
                );
            }
        }
    }

    private void requestLocationPermission() {
        if(Build.VERSION.SDK_INT >= 29) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION))
            {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        },
                        CODE_LOCATION_PERMISSION
                );
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        },
                        CODE_ALL_PERMISSIONS
                );
            }
        } else {
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION))
            {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        CODE_LOCATION_PERMISSION
                );
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        CODE_ALL_PERMISSIONS
                );
            }
        }
    }

    ////////////////////////////////////////// DOWNLOAD //////////////////////////////////////////

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(Environment.getExternalStorageDirectory());

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }
    }
}