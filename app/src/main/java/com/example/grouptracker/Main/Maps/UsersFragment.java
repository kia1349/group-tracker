package com.example.grouptracker.Main.Maps;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.grouptracker.Adapters.UsersAdapter;
import com.example.grouptracker.Model.User;
import com.example.grouptracker.R;
import com.example.grouptracker.Utils.Common;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    RecyclerView recyclerView;
    List<User> userList;
    List<String> membersList;
    UsersAdapter usersAdapter;
    FirebaseAuth auth;
    FirebaseUser user;
    String group_id;

    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        recyclerView = view.findViewById(R.id.users_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Bundle bundle = this.getArguments();
        group_id = bundle.getString("group_id");

        userList = new ArrayList<>();
        membersList = new ArrayList<>();

        getGroupMembers();
        getGroupUsers();

        return view;
    }

    private void getGroupMembers() {
        // get member id's
        DatabaseReference members_reference = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION).child(user.getUid()).child("MyGroups");
        members_reference.child(group_id).child("group_members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                membersList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String member = ds.getValue(String.class);

                    if(!member.equals(user.getUid())) {
                        membersList.add(member);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getGroupUsers() {
        //get users from current group
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    User group_member = ds.getValue(User.class);

                    //get all users except currently signed
                    if (membersList.contains(group_member.userid)) {
                        userList.add(group_member);
                    }

                    //usersAdapter = new UsersAdapter(getActivity(), userList);
                    recyclerView.setAdapter(usersAdapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                }
                Toast.makeText(getActivity(), ""+userList, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
