package com.example.grouptracker.Main.Maps;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grouptracker.Adapters.GroupRecyclerAdapter;
import com.example.grouptracker.Model.Group;
import com.example.grouptracker.R;
import com.example.grouptracker.Utils.Common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static android.app.Activity.RESULT_OK;

public class GroupsFragment extends Fragment implements
        GroupRecyclerAdapter.GroupRecyclerClickListener{
    //Firebase
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference user_information;
    private GroupRecyclerAdapter groupRecyclerAdapter;
    private RecyclerView groupRecyclerView;
    private FirebaseFirestore mDb;
    private ListenerRegistration groupEventListener;
    private ArrayList<Group> groups = new ArrayList<>();
    private Set<String> groupIds = new HashSet<>();

    public GroupsFragment(){

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initGroupRecyclerView();
        getGroups();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        // Init firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        user_information = firebaseDatabase.getReference(Common.USER_INFORMATION);

        groupRecyclerView = view.findViewById(R.id.groups_recycler_view);

        FloatingActionButton floatingActionButton = view.findViewById(R.id.fab);
        
        // FLoating Action Button click
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //newChatroomDialog();
            }
        });
        mDb = FirebaseFirestore.getInstance();

        return view;
    }

    private void getGroups() {
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        mDb.setFirestoreSettings(settings);

        CollectionReference groupsCollection = mDb
                .collection(getString(R.string.collection_groups));

        groupEventListener = groupsCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                Log.d("TAG", "onEvent: called.");

                if (e != null) {
                    Log.e("TAG", "onEvent: Listen failed.", e);
                    return;
                }

                if(queryDocumentSnapshots != null){
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        Group group = doc.toObject(Group.class);
                        if(!groupIds.contains(group.getGroup_id())){
                            groupIds.add(group.getGroup_id());
                            groups.add(group);
                        }
                    }
                    Log.d("TAG", "onEvent: number of chatrooms: " + groups.size());
                    groupRecyclerAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void initGroupRecyclerView(){
        groupRecyclerAdapter = new GroupRecyclerAdapter(groups, this);
        groupRecyclerView.setAdapter(groupRecyclerAdapter);
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
    }

    @Override
    public void onGroupSelected(int position) {
        navGroupFragment(groups.get(position));
    }

    private void navGroupFragment(Group group) {
        Bundle bundle = new Bundle();
        bundle.putString("group_id", group.getGroup_id());
        // can send other data like bundle.putBoolean("key", true)...
        // can send array list
        UsersFragment fragment = new UsersFragment();
        fragment.setArguments(bundle);
        getParentFragmentManager().beginTransaction().replace(R.id.container_fragment, fragment).commit();
        //FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        //fragmentTransaction.add(R.id.container_fragment, new UsersFragment()).commit();
        //Intent intent = new Intent(MainActivity.this, ChatroomActivity.class);
        //intent.putExtra(getString(R.string.intent_group), group);
        //startActivity(intent);
    }
/*
    private void newChatroomDialog(){

        androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter a chatroom name");

        final EditText input = new EditText(getActivity().getApplicationContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!input.getText().toString().equals("")){
                    buildNewChatroom(input.getText().toString());
                }
                else {
                    Toast.makeText(getActivity().getApplicationContext(), "Enter a chatroom name", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void buildNewGroup(String groupName){

        final Group group = new Group();
        group.setTitle(groupName);

        DatabaseReference newGroupRef = user_information.child()

        group.setGroup_id(newGroupRef.getId);

        newGroupRef.setValue(group).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hideDialog();

                if(task.isSuccessful()){
                    navGroupActivity(group);
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

 */
}
