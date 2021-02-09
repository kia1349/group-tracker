package com.example.grouptracker.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grouptracker.Interface.IRecyclerItemClickListener;
import com.example.grouptracker.Main.Authentication.MainActivity;
import com.example.grouptracker.Main.Maps.MapsActivity;
import com.example.grouptracker.Model.ClusterMarker;
import com.example.grouptracker.Model.User;
import com.example.grouptracker.R;
import com.example.grouptracker.Utils.UserClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MyHolder>{

    Context context;
    List<User> mUserList;
    UserListRecyclerClickListener mClickListener;
    FirebaseFirestore mDb;
    String userIsSharing;

    // constructor
    public UsersAdapter(Context context, List<User> userList, UserListRecyclerClickListener clickListener) {
        this.context = context;
        this.mUserList = userList;
        this.mClickListener = clickListener;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout(row_users.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, parent, false);
        final MyHolder holder = new MyHolder(view, mClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {
        // get data
        String userImage = mUserList.get(position).getImageUri();
        String userName = mUserList.get(position).getName();
        final String userEmail = mUserList.get(position).getEmail();
        userIsSharing = mUserList.get(position).getIsSharing();

        // set data
        if(userEmail.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
            holder.nameTextView.setText(userName+"   (Me)");
        } else {
            holder.nameTextView.setText(userName);
        }
        holder.emailTextView.setText(userEmail);
        try {
            Picasso.get().load(userImage).placeholder(R.mipmap.default_male_photo).into(holder.avatarImageView);
        } catch (Exception e){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, ""+userEmail, Toast.LENGTH_SHORT).show();
                }
            });
        }
        /*
        mDb = FirebaseFirestore.getInstance();
        DocumentReference userRef = mDb
                .collection(String.valueOf(R.string.collection_users))
                .document(FirebaseAuth.getInstance().getUid());

        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null) {
                    Log.w("statusListener", "Listen failed.", e);
                    return;
                }
                if(documentSnapshot != null && documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    userIsSharing = user.isSharing;
                    if(userIsSharing.equals("true")) {
                        holder.isSharingView.setImageResource(R.drawable.ic_online);
                    } else if (userIsSharing.equals("false")) {
                        holder.isSharingView.setImageResource(R.drawable.ic_offline);
                    }
                }
            }
        });
         */
        if(userIsSharing.equals("true")) {
            holder.isSharingView.setImageResource(R.drawable.ic_online);
        } else if (userIsSharing.equals("false")) {
            holder.isSharingView.setImageResource(R.drawable.ic_offline);
        }
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    // view holder class
    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        CircleImageView avatarImageView;
        TextView nameTextView, emailTextView;
        ImageView isSharingView;

        public MyHolder(@NonNull final View itemView, UserListRecyclerClickListener clickListener) {
            super(itemView);

            // init views
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            isSharingView = itemView.findViewById(R.id.isSharing);

            mClickListener = clickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mClickListener.onUserClicked(getAdapterPosition());
        }
    }
    public interface UserListRecyclerClickListener {
        void onUserClicked(int position);
    }
}
