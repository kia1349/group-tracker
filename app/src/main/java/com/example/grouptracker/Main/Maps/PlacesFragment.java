package com.example.grouptracker.Main.Maps;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grouptracker.Adapters.GroupRecyclerAdapter;
import com.example.grouptracker.Adapters.PlacesAdapter;
import com.example.grouptracker.Model.Group;
import com.example.grouptracker.Model.Place;
import com.example.grouptracker.Model.UserLocation;
import com.example.grouptracker.R;
import com.example.grouptracker.Utils.Common;
import com.example.grouptracker.Utils.GeofenceEvent;
import com.example.grouptracker.Utils.LocationEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PlacesFragment extends Fragment {
    //Firebase
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference user_information;
    private GroupRecyclerAdapter groupRecyclerAdapter;
    private RecyclerView placesRecyclerView;
    private FirebaseFirestore mDb;
    private PlacesAdapter placesAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ListenerRegistration placesEventListener;
    private ArrayList<Place> places = new ArrayList<>();
    private Set<String> placeIds = new HashSet<>();
    private Group group;
    private UserLocation userLocation;
    private ExtendedFloatingActionButton fab;

    public PlacesFragment(){

    }

    private void getIncomingGroup() {
        if (getArguments() != null) {
            group = (Group) getArguments().getSerializable(getString(R.string.intent_group));
            userLocation = (UserLocation) getArguments().getSerializable(getString(R.string.intent_user_locations));
            getPlaces();
            Log.d("getIncomingUser", "Got arguments!");
        }
    }

    public static PlacesFragment newInstance() {
        return new PlacesFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_places, container, false);

        // Init firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        user_information = firebaseDatabase.getReference(Common.USER_INFORMATION);

        fab = view.findViewById(R.id.fab);
        placesRecyclerView = view.findViewById(R.id.user_list_recycler_view);
        placesRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createGeofence();
            }
        });
        mDb = FirebaseFirestore.getInstance();
        getIncomingGroup();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(placesEventListener != null) {
            placesEventListener.remove();
        }
    }

    private void getPlaces() {
        CollectionReference placeRef = mDb
                .collection(getString(R.string.collection_groups))
                .document(group.getGroup_id())
                .collection(getString(R.string.collection_group_places));

        placesEventListener = placeRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                Log.d("TAG", "onEvent: called.");

                if (e != null) {
                    Log.e("TAG", "onEvent: Listen failed.", e);
                    return;
                }
                if(queryDocumentSnapshots != null){
                    places.clear();
                    places = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Place place = doc.toObject(Place.class);
                        if(!placeIds.contains(place.getPlace_id())){
                            placeIds.add(place.getPlace_id());
                            places.add(place);
                        }
                    }
                    initPlaceListRecyclerView();
                    placesAdapter.notifyDataSetChanged();
                }
            }
        });
    }


    private void initPlaceListRecyclerView() {
        placesAdapter = new PlacesAdapter(places);
        placesRecyclerView.setLayoutManager(layoutManager);
        placesRecyclerView.setAdapter(placesAdapter);

        placesAdapter.setOnItemClickListener(new PlacesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                updateGeofence(position);
            }

            @Override
            public void onDeleteClick(int position) {
                DocumentReference geoRef = mDb
                        .collection(getString(R.string.collection_groups))
                        .document(group.getGroup_id())
                        .collection(getString(R.string.collection_group_places))
                        .document(places.get(position).getPlace_id());

                DocumentReference placeRef = mDb
                        .collection(getString(R.string.collection_users))
                        .document(user.getUid())
                        .collection(getString(R.string.collection_group_places))
                        .document(places.get(position).getPlace_id());

                geoRef.delete();
                placeRef.delete();
                Log.d("initRecycler", "deleted ref");
                places.remove(position);
                placesAdapter.notifyItemRemoved(position);
            }

            @Override
            public void onNotificationClick(final int position) {
                Place p = places.get(position);

                if(p.isNotifications()) {
                    //Toast.makeText(getActivity(), "was on, going off", Toast.LENGTH_SHORT).show();
                    p.setNotifications(false);
                    uploadPlace(p);
                    places.set(position, p);
                    placesAdapter.notifyItemChanged(position);
                } else {
                    //Toast.makeText(getActivity(), "was off, going on", Toast.LENGTH_SHORT).show();
                    p.setNotifications(true);
                    uploadPlace(p);
                    places.set(position, p);
                    placesAdapter.notifyItemChanged(position);
                }
                goToPlaces();
            }
        });
    }

    private void uploadPlace(Place p) {
        DocumentReference groupRef = mDb
                .collection(getString(R.string.collection_groups))
                .document(group.getGroup_id())
                .collection(getString(R.string.collection_group_places))
                .document(p.getPlace_id());

        DocumentReference userRef = mDb
                .collection(getString(R.string.collection_users))
                .document(user.getUid())
                .collection(getString(R.string.collection_group_places))
                .document(p.getPlace_id());

        groupRef.set(p);
        userRef.set(p);
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

    private void createGeofence() {
        CreateGeofenceFragment createGeofenceFragment = CreateGeofenceFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putSerializable(getString(R.string.intent_group), group);
        bundle.putSerializable(getString(R.string.intent_user_locations), userLocation);
        createGeofenceFragment.setArguments(bundle);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_fragment, createGeofenceFragment, "CreateGeofenceFragment");
        transaction.addToBackStack("CreateGeofenceFragment");
        transaction.commit();
    }

    private void updateGeofence(int position) {
        Place place = places.get(position);
        CreateGeofenceFragment createGeofenceFragment = CreateGeofenceFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putSerializable(getString(R.string.intent_user_locations), userLocation);
        bundle.putSerializable(getString(R.string.intent_group), group);
        bundle.putSerializable(getString(R.string.intent_group_place), place);
        createGeofenceFragment.setArguments(bundle);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_fragment, createGeofenceFragment, "CreateGeofenceFragment");
        transaction.addToBackStack("CreateGeofenceFragment");
        transaction.commit();
    }
}
