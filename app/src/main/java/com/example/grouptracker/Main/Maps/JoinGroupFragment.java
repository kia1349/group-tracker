package com.example.grouptracker.Main.Maps;

import android.app.Activity;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.grouptracker.Model.Group;
import com.example.grouptracker.Model.Place;
import com.example.grouptracker.Model.User;
import com.example.grouptracker.Model.UserLocation;
import com.example.grouptracker.R;
import com.example.grouptracker.Utils.Common;
import com.example.grouptracker.Utils.UserClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mukesh.OnOtpCompletionListener;
import com.mukesh.OtpView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class JoinGroupFragment extends Fragment {

    DatabaseReference reference, currentReference;
    FirebaseUser firebaseUser;
    FirebaseAuth auth;
    User user;
    String current_user_id, join_user_id;
    DatabaseReference groupReference, joinedReference, user_information;
    private OtpView otpView;
    Group joinedGroup;
    private ListenerRegistration groupEventListener;
    private Set<String> groupIds = new HashSet<>();
    private ArrayList<String> groupNames = new ArrayList<>();
    String code;
    Group group;
    private ArrayList<Group> groups = new ArrayList<>();
    User currUser;
    private FirebaseFirestore mDb;
    UserLocation userLocation;
    private List<Group> groupsList = new ArrayList<>();
    ExtendedFloatingActionButton joinButton;
    boolean alreadyJoined = false;
    List<String> places;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_join_group, container, false);
        joinedGroup = null;
        joinButton = view.findViewById(R.id.joinBtn);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinGroup(code);
            }
        });

        mDb = FirebaseFirestore.getInstance();

        otpView = view.findViewById(R.id.otp_view);
        otpView.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                code = otp;
                joinGroup(code);
            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (groupEventListener != null) {
            groupEventListener.remove();
        }
    }

    private void getIncomingGroup() {
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(getString(R.string.intent_user));
            group = (Group) getArguments().getSerializable(getString(R.string.intent_group));
            userLocation = (UserLocation) getArguments().getSerializable(getString(R.string.intent_user_locations));
            groupsList = (List<Group>) getArguments().getSerializable(getString(R.string.collection_group_list));
        }
    }

    public static JoinGroupFragment newInstance() {
        return new JoinGroupFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        currUser = ((UserClient)(getActivity().getApplicationContext())).getUser();
        getIncomingGroup();
    }

    public void joinGroup(final String pin) {
        final CollectionReference groupsRef = mDb
                .collection(getString(R.string.collection_groups));

        groupEventListener = groupsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null) {
                    if (e != null) {
                        Log.e("TAG", "onEvent: Listen failed.", e);
                        return;
                    }
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Group g = doc.toObject(Group.class);
                        if (g.getGroup_code().equals(pin)) {
                            if(!user.getGroupList().contains(g.getGroup_id()) && !g.getMemberList().contains(user.getUserid())) {
                                joinedGroup = g;
                                group = joinedGroup;
                                joinedGroup.addMember(user.getUserid());
                                user.addGroup(joinedGroup.getGroup_id());
                                // get user list from joined group and add user to joined group
                                // set joined group to user

                                final CollectionReference userGroupList = mDb
                                        .collection(getString(R.string.collection_users))
                                        .document(user.getUserid())
                                        .collection(getString(R.string.collection_group_list));

                                userGroupList.add(joinedGroup);

                                final DocumentReference groupMemberList = mDb
                                        .collection(getString(R.string.collection_groups))
                                        .document(joinedGroup.getGroup_id())
                                        .collection(getString(R.string.collection_group_user_list))
                                        .document(user.getUserid());

                                groupMemberList.set(user);

                                final DocumentReference userRef = mDb
                                        .collection(getString(R.string.collection_users))
                                        .document(user.getUserid());

                                final CollectionReference userPlacesRef = userRef
                                        .collection(getString(R.string.collection_group_places));

                                userRef.set(user);

                                final DocumentReference groupRef = mDb
                                        .collection(getString(R.string.collection_groups))
                                        .document(joinedGroup.getGroup_id());

                                groupRef.set(joinedGroup);

                                final CollectionReference groupPlacesRef = groupRef
                                        .collection(getString(R.string.collection_group_places));

                                groupPlacesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                        if (queryDocumentSnapshots != null) {
                                            if (e != null) {
                                                Log.e("TAG", "onEvent: Listen failed.", e);
                                                return;
                                            }
                                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                                Place p = doc.toObject(Place.class);
                                                userPlacesRef.add(p);
                                            }
                                        }
                                    }
                                });

                                final CollectionReference userPlaces = mDb
                                        .collection(getString(R.string.collection_users))
                                        .document(user.getUserid())
                                        .collection(getString(R.string.collection_group_places));



                                hideKeyboard(getActivity());
                                Toast.makeText(getActivity(), "Successfully joined group: " + joinedGroup.getGroup_title(), Toast.LENGTH_SHORT).show();
                                replaceMapsFragment();
                                groupEventListener.remove();
                            } else {
                                alreadyJoined = true;
                                otpView.setText("");
                                Toast.makeText(getActivity(), "You are already in this group", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    if(!alreadyJoined && joinedGroup == null) {
                        Toast.makeText(getActivity(), "Please enter a valid code", Toast.LENGTH_SHORT).show();
                        otpView.setText("");
                    }
                }
            }
        });


    }

    private void replaceMapsFragment() {
        Log.d("MapsActivity-mapsFrag", "replacing fragment with maps");
        //getActivity().getSupportFragmentManager().popBackStack();
        MapsFragment mapsFragment = MapsFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putSerializable(getString(R.string.intent_user), user);
        bundle.putSerializable(getString(R.string.intent_group), group);
        bundle.putSerializable("joinedGroup", joinedGroup);
        bundle.putSerializable(getString(R.string.intent_user_locations), userLocation);
        mapsFragment.setArguments(bundle);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_fragment, mapsFragment, "mapsFragment");
        transaction.addToBackStack("mapsFragment");
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

}
