package com.example.grouptracker.Main.Maps;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grouptracker.Adapters.GroupRecyclerAdapter;
import com.example.grouptracker.Adapters.MessageViewHolder;
import com.example.grouptracker.Adapters.PlacesAdapter;
import com.example.grouptracker.Model.Group;
import com.example.grouptracker.Model.Message;
import com.example.grouptracker.Model.Place;
import com.example.grouptracker.Model.UserLocation;
import com.example.grouptracker.R;
import com.example.grouptracker.Utils.ButtonObserver;
import com.example.grouptracker.Utils.Common;
import com.example.grouptracker.Utils.ScrollToBottomObserver;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

public class ChatFragment extends Fragment {

    private FirebaseDatabase mDatabase;
    private FirebaseRecyclerAdapter<Message, MessageViewHolder> mFirebaseAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    ProgressBar progressBar;
    RecyclerView messageRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private ImageView addMessageImageView;
    private static final int REQUEST_INVITE = 1;
    private static final int REQUEST_IMAGE = 2;
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";

    public ChatFragment(){

    }



    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        messageRecyclerView = view.findViewById(R.id.messageRecyclerView);
        messageEditText = view.findViewById(R.id.messageEditText);
        sendButton = view.findViewById(R.id.sendButton);
        addMessageImageView = view.findViewById(R.id.addMessageImageView);
        // Initialize Realtime Database
        mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference messagesRef = mDatabase.getReference().child(getString(R.string.collection_chat_messages));

        FirebaseRecyclerOptions<Message> options =
                new FirebaseRecyclerOptions.Builder<Message>()
                        .setQuery(messagesRef, Message.class)
                        .build();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(options) {
            @Override
            public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new MessageViewHolder(inflater.inflate(R.layout.user_message, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(MessageViewHolder vh, int position, Message message) {
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                vh.bindMessage(message);
            }
        };

        mLinearLayoutManager = new LinearLayoutManager(view.getContext());
        mLinearLayoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(mLinearLayoutManager);
        messageRecyclerView.setAdapter(mFirebaseAdapter);

// Scroll down when a new message arrives
// See MyScrollToBottomObserver.java for details
        mFirebaseAdapter.registerAdapterDataObserver(
                new ScrollToBottomObserver(messageRecyclerView, mFirebaseAdapter, mLinearLayoutManager));

        // Disable the send button when there's no text in the input field
        // See MyButtonObserver.java for details
        messageEditText.addTextChangedListener(new ButtonObserver(sendButton));

        // When the send button is clicked, send a text message
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message friendlyMessage = new Message(messageEditText.getText().toString(), getUserName(), getUserPhotoUrl(), null);

                mDatabase.getReference().child(getString(R.string.collection_chat_messages)).push().setValue(friendlyMessage);
                messageEditText.setText("");
            }
        });

        // When the image button is clicked, launch the image picker
        addMessageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK && data != null) {
                final Uri uri = data.getData();
                Log.d(TAG, "Uri: " + uri.toString());

                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Message tempMessage = new Message(null, getUserName(), getUserPhotoUrl(), LOADING_IMAGE_URL);

                mDatabase.getReference().child(getString(R.string.collection_chat_messages)).push()
                        .setValue(tempMessage, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError,
                                                   DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Log.w(TAG, "Unable to write message to database.",
                                            databaseError.toException());
                                    return;
                                }

                                // Build a StorageReference and then upload the file
                                String key = databaseReference.getKey();
                                StorageReference storageReference =
                                        FirebaseStorage.getInstance()
                                                .getReference(user.getUid())
                                                .child(key)
                                                .child(uri.getLastPathSegment());

                                putImageInStorage(storageReference, uri, key);
                            }
                        });
            }
        }
    }

    private void putImageInStorage(StorageReference storageReference, Uri uri, final String key) {
        // First upload the image to Cloud Storage
        storageReference.putFile(uri)
                .addOnSuccessListener(getActivity(), new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // After the image loads, get a public downloadUrl for the image
                        // and add it to the message.
                        taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Message friendlyMessage = new Message(null, getUserName(), getUserPhotoUrl(), uri.toString());
                                        mDatabase.getReference()
                                                .child(getString(R.string.collection_chat_messages))
                                                .child(key)
                                                .setValue(friendlyMessage);
                                    }
                                });
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Image upload task was not successful.", e);
                    }
                });
    }


    @Override
    public void onPause() {
        mFirebaseAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAdapter.startListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    private String getUserPhotoUrl() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getPhotoUrl() != null) {
            return user.getPhotoUrl().toString();
        }

        return null;
    }

    private String getUserName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getDisplayName();
        }

        return "Anonymous";
    }
}
