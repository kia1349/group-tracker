package com.example.grouptracker.Main.Maps;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.grouptracker.Model.Group;
import com.example.grouptracker.R;
import com.example.grouptracker.Utils.Common;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mukesh.OtpView;

public class CodeFragment extends Fragment {
    //Firebase
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference user_information;
    String code;
    OtpView codeView;
    ExtendedFloatingActionButton copyBtn, shareBtn;
    Group group;


    public CodeFragment(){

    }

    private void getIncomingGroup() {
        if (getArguments() != null) {
            group = (Group) getArguments().getSerializable(getString(R.string.intent_group));
            setGroupCode();
            Log.d("getIncomingGroup", "Got arguments!");
        }
    }

    private void setGroupCode() {
        code = group.getGroup_code();
        codeView.setText(code);
    }

    public static CodeFragment newInstance() {
        return new CodeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_code, container, false);

        // Init firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        user_information = firebaseDatabase.getReference(Common.USER_INFORMATION);

        copyBtn = view.findViewById(R.id.copyBtn);
        codeView = view.findViewById(R.id.persCode);
        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String clip_text = codeView.getText().toString();
                setClipboard(clip_text);
            }
        });

        shareBtn = view.findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, "My family code is: "+code);
                startActivity(i.createChooser(i, "share using: "));
            }
        });


        user_information = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION);
        getIncomingGroup();

        return view;
    }
    // Makes clipboard for copying the group code
    private void setClipboard(String text) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
            Toast.makeText(getActivity().getApplicationContext(), "Code copied to clipboard!", Toast.LENGTH_SHORT).show();
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getActivity().getApplicationContext(), "Code copied to clipboard!", Toast.LENGTH_SHORT).show();
        }
    }
}
