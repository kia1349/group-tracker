package com.example.grouptracker.Main.Maps;

import android.Manifest;
import android.animation.Animator;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grouptracker.Adapters.MyClusterManagerRenderer;
import com.example.grouptracker.Adapters.UsersAdapter;
import com.example.grouptracker.Model.ClusterMarker;
import com.example.grouptracker.Model.Group;
import com.example.grouptracker.Model.MultiDrawable;
import com.example.grouptracker.Model.Place;
import com.example.grouptracker.Model.PolylineData;
import com.example.grouptracker.Model.User;
import com.example.grouptracker.Model.UserLocation;
import com.example.grouptracker.R;
import com.example.grouptracker.Utils.CircleBubbleTransformation;
import com.example.grouptracker.Utils.GeofenceHelper;
import com.example.grouptracker.Utils.UserClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.grouptracker.Utils.Common.MAPVIEW_BUNDLE_KEY;

public class MapsFragment extends Fragment implements
        OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnPolylineClickListener, UsersAdapter.UserListRecyclerClickListener, GoogleMap.OnMapLongClickListener, ClusterManager.OnClusterClickListener<ClusterMarker>, ClusterManager.OnClusterInfoWindowClickListener<ClusterMarker>, ClusterManager.OnClusterItemClickListener<ClusterMarker>, ClusterManager.OnClusterItemInfoWindowClickListener<ClusterMarker>, GoogleMap.OnCameraMoveListener, GoogleMap.OnMapClickListener {
    private static final String TAG = "MapsFragment";
    private static final int MAP_LAYOUT_STATE_CONTRACTED = 0;
    private static final int MAP_LAYOUT_STATE_EXPANDED = 1;
    private static final int LOCATION_UPDATE_INTERVAL = 5000;
    private static int GEOFENCE_RADIUS = 200;
    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 1001;
    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 1002;
    private String GEOFENCE_ID = "SOME_GEOFENCE_ID";

    // Layout
    private CameraPosition position;
    private MapView mapView;
    private RelativeLayout mapContainer;
    private UserLocation userLocation;
    UserClient userClient;
    Marker geofenceMarker;
    Circle geofenceRadius;
    private int mapType = 0;
    private boolean addingGeofence;
    private View map_type_default_background, map_type_satellite_background, map_type_terrain_background, map_type_hybrid_background;
    private TextView map_type_default_text, map_type_satellite_text, map_type_terrain_text, map_type_hybrid_text;
    private boolean shouldShowCluster = true;

    // Variables
    private Double longitude, latitude;
    private boolean isHybridMapTypeEnabled = true;
    private boolean isExpanded = false;
    private Group group;
    private GoogleMap gMap;
    private UserLocation userPosition;
    private LatLngBounds mapBoundary;
    private GeoApiContext mGeoApiContext = null;
    private Handler handler = new Handler();
    private Runnable runnable;
    private ArrayList<PolylineData> polylinesData = new ArrayList<>();
    private Marker selectedMarker = null;
    private ArrayList<Marker> tripMarkers = new ArrayList<>();
    private int mapLayoutState = 0;
    private ClusterManager<ClusterMarker> clusterManager;
    private MyClusterManagerRenderer clusterManagerRenderer;
    private ArrayList<ClusterMarker> clusterMarkers = new ArrayList<>();
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private User user;
    DatabaseReference user_information, user_locations;
    private BottomSheetBehavior bottomSheetBehavior;
    private LinearLayout linearLayoutBSheet;
    private ImageButton toggleUpDown, refreshMap, goToMyLocation, map_type_default, map_type_satellite, map_type_terrain, map_type_hybrid;
    ExtendedFloatingActionButton addFab;
    FloatingActionButton refreshMapButton, toggleUsersButton, goToMyLocationButton, addGeofenceButton, changeMapTypeButton;
    TextView textViewRefreshMap, textViewGeofence, textViewGoToMyLocation, textViewChangeMapType;
    Boolean areAllFabsVisible;
    private List<User> userList = new ArrayList<User>();
    private ArrayList<UserLocation> userLocations = new ArrayList<UserLocation>();
    private UsersAdapter usersAdapter;
    private ImageButton fullScreenButton;
    RecyclerView recyclerView;
    private FirebaseFirestore mDb;
    private Marker locationMarker;
    private ListenerRegistration userListEventListener;
    private Animation rotate_forward, rotate_backward, all_round, all_round_round, refresh, bounce;
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;
    private ConstraintLayout map_type_selection;
    ProgressBar progressBar;
    private float currentZoom;
    private Place place;
    ListenerRegistration placeEventListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setUpClustersNoPositioning();
            }
        }, 1000);
    }

    private void getIncomingGroup() {
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(getString(R.string.intent_user));
            group = (Group) getArguments().getSerializable(getString(R.string.intent_group));
            userLocation = (UserLocation) getArguments().getSerializable(getString(R.string.intent_user_locations));
        }
    }

    public static MapsFragment newInstance() {
        return new MapsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Initialize view
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        Log.d("MapsFragment-createView", "created");
        mDb = FirebaseFirestore.getInstance();

        linkLayouts(view);
        getIncomingGroup();
        initFloatingToolbar();
        initBottomUserSheet();
        getGroupMembers();

        initGoogleMap(savedInstanceState);
        return view;
    }

    private void initBottomUserSheet() {
        linearLayoutBSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(map_type_selection.getVisibility() == View.VISIBLE) {
                    closeMapTypeSelection();
                    changeMapTypeButton.startAnimation(all_round_round);
                }
            }
        });

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        isExpanded = true;

        toggleUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });
    }

    private void initFloatingToolbar() {
        // extend/shrink animation
        areAllFabsVisible = false;
        rotate_forward = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_backward);
        all_round = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.all_round);
        refresh = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.refresh);
        bounce = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.bounce);
        all_round_round = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.all_round_round);
        all_round_round.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                changeMapTypeButton.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_map_layers));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        addFab.shrink();

        addFloatingClickListeners();
    }

    private void startProgressAnimation() {
        Animation loading = AnimationUtils.loadAnimation(getActivity(), R.anim.rotation_cw);
        loading.setRepeatCount(Animation.INFINITE);
        loading.setInterpolator(new AccelerateDecelerateInterpolator());
        progressBar.setVisibility(View.VISIBLE);
        progressBar.startAnimation(loading);
    }

    private void stopProgressAnimation() {
        progressBar.clearAnimation();
        progressBar.setVisibility(View.GONE);
    }

    private void addFloatingClickListeners() {
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(map_type_selection.getVisibility() == View.VISIBLE) {
                    closeMapTypeSelection();
                    changeMapTypeButton.startAnimation(all_round_round);
                }
                if (!areAllFabsVisible) {
                    addFab.startAnimation(rotate_forward);
                    addFab.setIconTint(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.white)));
                    addFab.setBackgroundTintList(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.colorSecondary)));
                    goToMyLocationButton.show();
                    refreshMapButton.show();
                    changeMapTypeButton.show();
                    textViewGoToMyLocation.setVisibility(View.VISIBLE);
                    textViewRefreshMap.setVisibility(View.VISIBLE);
                    textViewChangeMapType.setVisibility(View.VISIBLE);
                    addFab.extend();
                    areAllFabsVisible = true;
                } else {
                    addFab.startAnimation(rotate_backward);
                    addFab.setIconTint(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.colorSecondary)));
                    addFab.setBackgroundTintList(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.white)));
                    goToMyLocationButton.hide();
                    refreshMapButton.hide();
                    changeMapTypeButton.hide();
                    textViewGoToMyLocation.setVisibility(View.GONE);
                    textViewRefreshMap.setVisibility(View.GONE);
                    textViewChangeMapType.setVisibility(View.GONE);
                    addFab.shrink();
                    areAllFabsVisible = false;
                }
            }
        });

        refreshMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpClustersNoPositioning();
                refreshMapButton.startAnimation(refresh);
            }
        });

        goToMyLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateCameraView();
                goToMyLocationButton.startAnimation(bounce);
            }
        });

        changeMapTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (map_type_selection.getVisibility() == View.INVISIBLE) {
                    openMapTypeSelection();
                    changeMapTypeButton.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_baseline_clear_24));
                    changeMapTypeButton.startAnimation(all_round);
                } else {
                    closeMapTypeSelection();
                    changeMapTypeButton.startAnimation(all_round_round);

                }
            }
        });

        addMapTypeOptionClickListeners();
    }

    private void addMapTypeOptionClickListeners() {
        // Handle selection of the Default map type
        map_type_default.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map_type_default_background.setVisibility(View.VISIBLE);
                map_type_satellite_background.setVisibility(View.INVISIBLE);
                map_type_terrain_background.setVisibility(View.INVISIBLE);
                map_type_hybrid_background.setVisibility(View.INVISIBLE);
                map_type_default_text.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
                map_type_satellite_text.setTextColor(Color.parseColor("#808080"));
                map_type_terrain_text.setTextColor(Color.parseColor("#808080"));
                map_type_hybrid_text.setTextColor(Color.parseColor("#808080"));
                isHybridMapTypeEnabled = false;
                gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        });

        // Handle selection of the Satellite map type
        map_type_satellite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map_type_default_background.setVisibility(View.INVISIBLE);
                map_type_satellite_background.setVisibility(View.VISIBLE);
                map_type_terrain_background.setVisibility(View.INVISIBLE);
                map_type_hybrid_background.setVisibility(View.INVISIBLE);
                map_type_default_text.setTextColor(Color.parseColor("#808080"));
                map_type_satellite_text.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
                map_type_terrain_text.setTextColor(Color.parseColor("#808080"));
                map_type_hybrid_text.setTextColor(Color.parseColor("#808080"));
                isHybridMapTypeEnabled = false;
                gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
        });

        // Handle selection of the terrain map type
        map_type_terrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map_type_default_background.setVisibility(View.INVISIBLE);
                map_type_satellite_background.setVisibility(View.INVISIBLE);
                map_type_terrain_background.setVisibility(View.VISIBLE);
                map_type_hybrid_background.setVisibility(View.INVISIBLE);
                map_type_default_text.setTextColor(Color.parseColor("#808080"));
                map_type_satellite_text.setTextColor(Color.parseColor("#808080"));
                map_type_terrain_text.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
                map_type_hybrid_text.setTextColor(Color.parseColor("#808080"));
                isHybridMapTypeEnabled = false;
                gMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            }
        });

        // Handle selection of the hybrid map type
        map_type_hybrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map_type_default_background.setVisibility(View.INVISIBLE);
                map_type_satellite_background.setVisibility(View.INVISIBLE);
                map_type_terrain_background.setVisibility(View.INVISIBLE);
                map_type_hybrid_background.setVisibility(View.VISIBLE);
                map_type_default_text.setTextColor(Color.parseColor("#808080"));
                map_type_satellite_text.setTextColor(Color.parseColor("#808080"));
                map_type_terrain_text.setTextColor(Color.parseColor("#808080"));
                map_type_hybrid_text.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
                isHybridMapTypeEnabled = true;
                CameraPosition cameraPosition = gMap.getCameraPosition();
                if (cameraPosition.zoom > 15.0) {
                    gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                } else {
                    gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
            }
        });
    }

    private void linkLayouts(View view) {
        mapView = view.findViewById(R.id.google_map);
        mapContainer = view.findViewById(R.id.map_container);
        linearLayoutBSheet = view.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(linearLayoutBSheet);
        recyclerView = view.findViewById(R.id.user_list_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        addFab = view.findViewById(R.id.add_fab);
        goToMyLocationButton = view.findViewById(R.id.btn_my_location_floating);
        refreshMapButton = view.findViewById(R.id.btn_reset_map_floating);
        changeMapTypeButton = view.findViewById(R.id.btn_map_type);
        textViewGoToMyLocation = view.findViewById(R.id.textview_my_location);
        textViewRefreshMap = view.findViewById(R.id.textview_reset);
        toggleUsersButton = view.findViewById(R.id.btn_users);
        textViewChangeMapType = view.findViewById(R.id.textview_map_type);
        map_type_selection = view.findViewById(R.id.map_type_selection);
        map_type_default = view.findViewById(R.id.map_type_default);
        map_type_satellite = view.findViewById(R.id.map_type_satellite);
        map_type_terrain = view.findViewById(R.id.map_type_terrain);
        map_type_hybrid = view.findViewById(R.id.map_type_hybrid);
        map_type_default_background = view.findViewById(R.id.map_type_default_background);
        map_type_satellite_background = view.findViewById(R.id.map_type_satellite_background);
        map_type_terrain_background = view.findViewById(R.id.map_type_terrain_background);
        map_type_hybrid_background = view.findViewById(R.id.map_type_hybrid_background);
        map_type_default_text = view.findViewById(R.id.map_type_default_text);
        map_type_satellite_text = view.findViewById(R.id.map_type_satellite_text);
        map_type_terrain_text = view.findViewById(R.id.map_type_terrain_text);
        map_type_hybrid_text = view.findViewById(R.id.map_type_hybrid_text);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void closeMapTypeSelection() {
            // Start animator close and finish at the FAB position
            Animator anim = ViewAnimationUtils.createCircularReveal(
                    map_type_selection,
                    map_type_selection.getWidth() - (changeMapTypeButton.getWidth() / 2),
                    changeMapTypeButton.getHeight() / 2,
                    changeMapTypeButton.getWidth(),
                    0);
            anim.setDuration(300);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    map_type_selection.setVisibility(View.INVISIBLE);
                }
                @Override
                public void onAnimationCancel(Animator animation) {
                }
                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            anim.start();
    }

    private void openMapTypeSelection() {
            // Start animator to reveal the selection view, starting from the FAB itself
            Animator anim = ViewAnimationUtils.createCircularReveal(
                    map_type_selection,
                    map_type_selection.getWidth() - (changeMapTypeButton.getWidth() / 2),
                    changeMapTypeButton.getHeight() / 2,
                    changeMapTypeButton.getWidth() / 2f,
                    map_type_selection.getWidth());
            anim.setDuration(300);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if(isHybridMapTypeEnabled) {
                        map_type_hybrid_text.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
                        map_type_hybrid_background.setVisibility(View.VISIBLE);
                    }

                    map_type_selection.setVisibility(View.VISIBLE);
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                }
                @Override
                public void onAnimationCancel(Animator animation) {
                }
                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            anim.start();
    }

    private void getGroupMembers() {
        Log.d("MapsFragment-getMembers", "started");
        CollectionReference usersRef = mDb
                .collection(getString(R.string.collection_groups))
                .document(group.getGroup_id())
                .collection(getString(R.string.collection_group_user_list));

        userListEventListener = usersRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e(TAG, "onEvent: Listen failed.", e);
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    // Clear the list and add all the users again
                    userList.clear();
                    userList = new ArrayList<>();
                    //LOOPA ČEZ USERJE V CHATTROOMU IN DODA V CHAT LIST
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        userList.add(user);
                        getUserLocation(user);
                    }
                    initUserListRecyclerView();
                    usersAdapter.notifyDataSetChanged();
                    setUserPosition();
                }
            }
        });
    }

    private void getUserLocation(User user) {
        DocumentReference locationRef = mDb.collection(getString(R.string.collection_user_locations))
                .document(user.getUserid());

        locationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    //ČE JE DEJANSKO NJEGOV GPS KOORDINAT V BAZI (MORE BIT CE JE DOVOLIL)
                    if (task.getResult().toObject(UserLocation.class) != null) {
                        UserLocation location = task.getResult().toObject(UserLocation.class);
                        userLocations.add(location);
                        Log.d("MapsFragment-getLoc", "" + userLocations.get(0));
                    }
                }

            }
        });
    }

    private void setUserPosition() {
        if (userLocations.size() > 0) {
            for (UserLocation userLocation : userLocations) {
                if (userLocation.getUser() != null) {
                    if (userLocation.getUser().getUserid().equals(FirebaseAuth.getInstance().getUid())) {
                        userPosition = userLocation;
                        Log.d("MapsFragment-userPos", "setUserPos & addMarkers & cameraView");
                    }
                    setUpClusters();
                } else {
                    Log.d("MapsFragment-userPos", "No user set to userLocation");
                }
            }
        } else {
            Log.d("MapsFragment-userPos", "Empty userLocations");
        }
    }

    private void setUserPositionNoClusters() {
        if (userLocations.size() > 0) {
            for (UserLocation userLocation : userLocations) {
                if (userLocation.getUser() != null) {
                    if (userLocation.getUser().getUserid().equals(FirebaseAuth.getInstance().getUid())) {
                        userPosition = userLocation;
                        Log.d("MapsFragment-userPos", "setUserPos & addMarkers & cameraView");
                    }
                } else {
                    Log.d("MapsFragment-userPos", "No user set to userLocation");
                }
            }
        } else {
            Log.d("MapsFragment-userPos", "Empty userLocations");
        }
    }


    public void zoomRoute(List<LatLng> lstLatLngRoute) {

        if (gMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build();

        gMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }

    private void removeTripMarkers() {
        for (Marker marker : tripMarkers) {
            marker.remove();
        }
    }

    private void resetSelectedMarker() {
        if (selectedMarker != null) {
            selectedMarker.setVisible(true);
            selectedMarker = null;
            removeTripMarkers();
        }
    }

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

    private void startUserLocationsRunnable() {
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                retrieveUserLocations();
                handler.postDelayed(runnable, user.getLocationUpdateInterval());
            }
        }, user.getLocationUpdateInterval());
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the view
            View locationCompass = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("5"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationCompass.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 160, 30, 0); // 160 la truc y , 30 la  truc x
        }

        mapView.onCreate(mapViewBundle);

        mapView.getMapAsync(this);
        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_api_key)).build();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        startUserLocationsRunnable();
        setUpMap();
        mapView.onResume();
        if (position != null) {
            gMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
            position = null;
        }
    }

    private void setUpMap() {
        MapsInitializer.initialize(getActivity());
        addMapMarkers();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
        startUserLocationsRunnable();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
        if (userListEventListener != null) {
            userListEventListener.remove();
        }
        stopLocationUpdates();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (userListEventListener != null) {
            userListEventListener.remove();
        }
        stopLocationUpdates();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mapView != null) {
            mapView.onStop();
        }
        if (userListEventListener != null) {
            userListEventListener.remove();
        }
        stopLocationUpdates();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
        position = gMap.getCameraPosition();
        gMap = null;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void stopLocationUpdates() {
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("MapsFragment", "onMapReady: started");
        gMap = googleMap;
        gMap.getUiSettings().setMapToolbarEnabled(false);
        if(gMap != null) {
            moveItems();
            gMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    setUpClusters();
                }
            });
            Log.d("MapsFragment", "onMapReady: before setView");
            setUpClustersNoPositioning();
        }
    }

    public void setUpClusters() {
        if (gMap != null) {
            resetMap();
            initClusterManager();
            setUserPositionNoClusters();
            addMapMarkers();
            if (userPosition != null) {
                setCameraView();
            }
            gMap.setOnCameraIdleListener(clusterManager);
            gMap.setOnInfoWindowClickListener(this);
            clusterManager.setOnClusterClickListener(this);
            clusterManager.setOnClusterInfoWindowClickListener(this);
            clusterManager.setOnClusterItemClickListener(this);
            clusterManager.setOnClusterItemInfoWindowClickListener(this);
            gMap.setOnPolylineClickListener(this);
            gMap.setOnMapClickListener(this);
            gMap.setOnCameraMoveListener(this);
            clusterManager.cluster();
        }
    }

    private void initClusterManager() {
            clusterManager = new ClusterManager<>(getActivity().getApplicationContext(), gMap);
            clusterManagerRenderer = new MyClusterManagerRenderer(getActivity().getApplicationContext(), gMap, clusterManager);
            clusterManager.setRenderer(clusterManagerRenderer);
    }

    public void setUpClustersNoPositioning() {
        if (gMap != null) {
            resetMap();
            initClusterManager();
            addMapMarkers();
            gMap.setOnCameraIdleListener(clusterManager);
            gMap.setOnInfoWindowClickListener(this);
            clusterManager.setOnClusterClickListener(this);
            clusterManager.setOnClusterInfoWindowClickListener(this);
            clusterManager.setOnClusterItemClickListener(this);
            clusterManager.setOnClusterItemInfoWindowClickListener(this);
            gMap.setOnPolylineClickListener(this);
            gMap.setOnMapLongClickListener(this);
            clusterManager.cluster();
        }
    }

    private void addMapMarkers() {
        for (UserLocation userLocation : userLocations) {
            try {
                String snippet = "";
                if (userLocation.getUser().getUserid().equals(FirebaseAuth.getInstance().getUid())) {
                    snippet = "This is you";
                } else {
                    snippet = "Determine route to " + userLocation.getUser().getName() + "?";
                }
                ClusterMarker newClusterMarker = new ClusterMarker(
                        new LatLng(userLocation.getGeo_point().getLatitude(), userLocation.getGeo_point().getLongitude()),
                        userLocation.getUser().getName(),
                        snippet,
                        userLocation.getUser().getImageUri(),
                        userLocation.getUser()
                );
                clusterManager.addItem(newClusterMarker);
                clusterMarkers.add(newClusterMarker);

            } catch (NullPointerException e) {
                Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage());
            }
        }
    }

    private void moveItems() {
        final ViewGroup parent = (ViewGroup) mapView.findViewWithTag("GoogleMapMyLocationButton").getParent();
        parent.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Resources r = getActivity().getResources();
                    //convert our dp margin into pixels
                    int marginPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, r.getDisplayMetrics());
                    // Get the map compass view
                    View mapCompass = parent.getChildAt(4);
                    // create layoutParams, giving it our wanted width and height(important, by default the width is "match parent")
                    RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(mapCompass.getHeight(), mapCompass.getHeight());
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

    private void calculateDirections(Marker marker) {
        Log.d(TAG, "MapsFragment calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        userPosition.getGeo_point().getLatitude(),
                        userPosition.getGeo_point().getLongitude()
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage());

            }
        });
    }

    private void retrieveUserLocations() {
        Log.d(TAG, "retrieveUserLocations: retrieving location of all users in the group.");

        try {
            for (final ClusterMarker clusterMarker : clusterMarkers) {

                DocumentReference userLocationRef = FirebaseFirestore.getInstance()
                        .collection(getString(R.string.collection_user_locations))
                        .document(clusterMarker.getUser().getUserid());

                userLocationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            final UserLocation updatedUserLocation = task.getResult().toObject(UserLocation.class);
                            userLocation = updatedUserLocation;
                            // update the location
                            for (int i = 0; i < clusterMarkers.size(); i++) {
                                try {
                                    if (clusterMarkers.get(i).getUser().getUserid().equals(updatedUserLocation.getUser().getUserid())) {

                                        LatLng updatedLatLng = new LatLng(
                                                updatedUserLocation.getGeo_point().getLatitude(),
                                                updatedUserLocation.getGeo_point().getLongitude()
                                        );

                                        clusterMarkers.get(i).setPosition(updatedLatLng);
                                        clusterManagerRenderer.setUpdateMarker(clusterMarkers.get(i));
                                    }
                                } catch (NullPointerException e) {
                                    Log.e(TAG, "retrieveUserLocations: NullPointerException: " + e.getMessage());
                                }
                            }
                        }
                    }
                });
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "retrieveUserLocations: Fragment was destroyed during Firestore query. Ending query." + e.getMessage());
        }

    }

    private void initUserListRecyclerView() {
        usersAdapter = new UsersAdapter(getActivity(), userList, this);
        //Toast.makeText(getActivity(), "userList size: " + userList.size(), Toast.LENGTH_SHORT).show();
        recyclerView.setAdapter(usersAdapter);
    }

    private void resetMap() {
        if (gMap != null) {
            gMap.clear();

            if (clusterManager != null) {
                clusterManager.clearItems();
            }

            if (clusterMarkers.size() > 0) {
                clusterMarkers.clear();
                clusterMarkers = new ArrayList<>();
            }

            if (polylinesData.size() > 0) {
                polylinesData.clear();
                polylinesData = new ArrayList<>();
            }
        }
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        if (marker.getTitle().contains("Trip")) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Open Google Maps?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            String latitude = String.valueOf(marker.getPosition().latitude);
                            String longitude = String.valueOf(marker.getPosition().longitude);
                            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");

                            try {
                                if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                    startActivity(mapIntent);
                                }
                            } catch (NullPointerException e) {
                                Log.e(TAG, "onClick: NullPointerException: Couldn't open map." + e.getMessage());
                                Toast.makeText(getActivity(), "Couldn't open map", Toast.LENGTH_SHORT).show();
                            }

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        } else {
            if (marker.getSnippet().equals("This is you")) {
                marker.hideInfoWindow();
            } else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(marker.getSnippet())
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                resetSelectedMarker();
                                selectedMarker = marker;
                                calculateDirections(marker);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }


    @Override
    public void onUserClicked(int position) {
        //Toast.makeText(getActivity(), "selected a user: "+ userList.get(position).getUserid(), Toast.LENGTH_SHORT).show();
        if(map_type_selection.getVisibility() == View.VISIBLE) {
            closeMapTypeSelection();
            changeMapTypeButton.startAnimation(all_round_round);
        }
        String selectedUserId = userList.get(position).getUserid();

        for (ClusterMarker clusterMarker : clusterMarkers) {
            if (selectedUserId.equals(clusterMarker.getUser().getUserid())) {
                gMap.animateCamera(CameraUpdateFactory.newLatLng(
                        new LatLng(clusterMarker.getPosition().latitude, clusterMarker.getPosition().longitude)),
                        600,
                        null
                );
                break;
            }
        }
    }

    private void addCircle(LatLng latLng, float radius) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        geofenceRadius = gMap.addCircle(circleOptions);
    }

    @Override
    public void onCameraMove() {
        /*
        currentZoom = gMap.getCameraPosition().zoom;
        if(currentZoom > 19) {
            shouldShowCluster = false;
        } else {
            shouldShowCluster = true;
        }
         */
        if(isHybridMapTypeEnabled) {
            CameraPosition cameraPosition = gMap.getCameraPosition();
            if (cameraPosition.zoom > 15.0) {
                if(gMap.getMapType() != GoogleMap.MAP_TYPE_HYBRID) {
                    gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                }
            } else {
                if(gMap.getMapType() != GoogleMap.MAP_TYPE_NORMAL) {
                    gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
            }
        }
    }

    public void addMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        gMap.addMarker(markerOptions);
    }

    private void addGeofence(LatLng latLng, float radius) {
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
        Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID, latLng, radius,
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if(grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // we have the permission
            } else {
                // we do not have the permission...
            }
        }
        if(requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if(grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // we have the permission
                Toast.makeText(getActivity(), "You can add geofences", Toast.LENGTH_SHORT).show();
            } else {
                // we do not have the permission...
                Toast.makeText(getActivity(), "Background location access is neccessary for geonces to trigger...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setCameraView() {
        Log.d("MapsFragment-setCamView", "set");
        double bottomBoundary = userPosition.getGeo_point().getLatitude() - .1;
        double leftBoundary = userPosition.getGeo_point().getLongitude() - .1;
        double topBoundary = userPosition.getGeo_point().getLatitude() + .1;
        double rightBoundary = userPosition.getGeo_point().getLongitude() + .1;

        mapBoundary = new LatLngBounds(
                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );

        if (gMap == null) {
            int width = getActivity().getResources().getDisplayMetrics().widthPixels;
            int height = getActivity().getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen
            gMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBoundary, width, height, padding));
        } else {
            gMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBoundary, 0));
        }
    }

    private void animateCameraView() {
        Log.d("MapsFragment-animateCam", "animated camera to user position");
        double bottomBoundary = userPosition.getGeo_point().getLatitude() - .1;
        double leftBoundary = userPosition.getGeo_point().getLongitude() - .1;
        double topBoundary = userPosition.getGeo_point().getLatitude() + .1;
        double rightBoundary = userPosition.getGeo_point().getLongitude() + .1;

        mapBoundary = new LatLngBounds(
                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );

        if (gMap == null) {
            LatLng position = new LatLng(userPosition.getGeo_point().getLatitude(), userPosition.getGeo_point().getLongitude());
            int speed = 700;
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(position)
                    .zoom(11)
                    .bearing(0)
                    .tilt(0)
                    .build();
            gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), speed, null);
        } else {
            LatLng position = new LatLng(userPosition.getGeo_point().getLatitude(), userPosition.getGeo_point().getLongitude());
            int speed = 700;
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(position)
                    .zoom(11)
                    .bearing(0)
                    .tilt(0)
                    .build();
            gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), speed, null);
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

        if(gMap == null) {
            int width = getActivity().getResources().getDisplayMetrics().widthPixels;
            int height = getActivity().getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen
            gMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBoundary, width, height, padding));
        } else {
            gMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBoundary, 0));
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if(Build.VERSION.SDK_INT >= 29) {
            // We need background permission
            if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                setUpClustersNoPositioning();
                addMarker(latLng);
                addCircle(latLng, GEOFENCE_RADIUS);
                addGeofence(latLng, GEOFENCE_RADIUS);
                setCameraViewFromLatLng(latLng);
            } else {
                if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    // we show a dialog and ask for permission
                    ActivityCompat.requestPermissions(getActivity(), new String [] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                } else {

                }
            }
        } else {
            setUpClustersNoPositioning();
            addMarker(latLng);
            addCircle(latLng, GEOFENCE_RADIUS);
            addGeofence(latLng, GEOFENCE_RADIUS);
            setCameraViewFromLatLng(latLng);
        }
        /*
        List<Address> addressList = null;
        if(latLng != null) {
            Geocoder geocoder = new Geocoder(getActivity().getApplicationContext());
            try {
                addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(addressList.size() > 0) {
                Address address = addressList.get(0);
                final LatLng latLng1 = new LatLng(address.getLatitude(), address.getLongitude());
                if(locationMarker == null) {
                    locationMarker = gMap.addMarker(new MarkerOptions()
                            .position(latLng1)
                            .title(address.getAddressLine(0))
                            .snippet("Determine route to " + address.getAddressLine(0).split(",")[0] + "?"));
                    locationMarker.showInfoWindow();
                    gMap.animateCamera(CameraUpdateFactory.newLatLng(latLng1));
                } else {
                    locationMarker.setPosition(latLng1);
                    locationMarker.setTitle(address.getAddressLine(0));
                    locationMarker.setSnippet("Determine route to " + address.getAddressLine(0).split(",")[0] + "?");
                    locationMarker.showInfoWindow();
                    gMap.animateCamera(CameraUpdateFactory.newLatLng(latLng1));
                }
            }
        }
         */
    }

    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "MapsFragment run: result routes: " + result.routes.length);
                if(polylinesData.size() > 0){
                    for(PolylineData polylineData: polylinesData){
                        polylineData.getPolyline().remove();
                    }
                    polylinesData.clear();
                    polylinesData = new ArrayList<>();
                }
                double duration = 99999999;
                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = gMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getActivity(), R.color.colorSecondary));
                    polyline.setClickable(true);
                    polylinesData.add(new PolylineData(polyline, route.legs[0]));

                    double tempDuration = route.legs[0].duration.inSeconds;
                    if(tempDuration < duration){
                        duration = tempDuration;
                        onPolylineClick(polyline);
                        zoomRoute(polyline.getPoints());
                    }

                    selectedMarker.setVisible(false);
                }
            }
        });
    }

    @Override
    public void onPolylineClick(Polyline polyline) {

        int index = 0;
        for(PolylineData polylineData: polylinesData){
            index++;
            Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if(polyline.getId().equals(polylineData.getPolyline().getId())){
                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.blue));
                polylineData.getPolyline().setZIndex(1);

                LatLng endLocation = new LatLng(
                        polylineData.getLeg().endLocation.lat,
                        polylineData.getLeg().endLocation.lng
                );

                Marker marker = gMap.addMarker(new MarkerOptions()
                        .position(endLocation)
                        .title("Trip: #" + index)
                        .snippet("Duration: " + polylineData.getLeg().duration)
                );

                marker.showInfoWindow();

                tripMarkers.add(marker);
            }
            else{
                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.colorSecondary));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }

    @Override
    public boolean onClusterClick(Cluster<ClusterMarker> cluster) {
        String firstName = cluster.getItems().iterator().next().toString();
        shouldShowCluster = false;
        //Toast.makeText(getActivity(), cluster.getSize() +" (including " + firstName + ")", Toast.LENGTH_SHORT).show();
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for(ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        final LatLngBounds bounds = builder.build();

        try {
            gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<ClusterMarker> cluster) {

    }

    @Override
    public boolean onClusterItemClick(ClusterMarker item) {
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(final ClusterMarker marker) {
        if(marker.getTitle().contains("Trip")){
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Open Google Maps?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            String latitude = String.valueOf(marker.getPosition().latitude);
                            String longitude = String.valueOf(marker.getPosition().longitude);
                            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");

                            try{
                                if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                    startActivity(mapIntent);
                                }
                            }catch (NullPointerException e){
                                Log.e(TAG, "onClick: NullPointerException: Couldn't open map." + e.getMessage() );
                                Toast.makeText(getActivity(), "Couldn't open map", Toast.LENGTH_SHORT).show();
                            }

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
        else{
            if(marker.getSnippet().equals("This is you")){
                clusterManagerRenderer.getMarker(marker).hideInfoWindow();
            }
            else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(marker.getSnippet())
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                resetSelectedMarker();
                                selectedMarker = clusterManagerRenderer.getMarker(marker);
                                calculateDirections(clusterManagerRenderer.getMarker(marker));
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (map_type_selection.getVisibility() == View.VISIBLE) {
            closeMapTypeSelection();
            changeMapTypeButton.startAnimation(all_round_round);
        }
        if (addFab.isExtended()) {
            addFab.shrink();
        }
    }


}
