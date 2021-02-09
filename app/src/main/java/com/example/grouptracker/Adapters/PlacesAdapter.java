package com.example.grouptracker.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grouptracker.Main.Maps.MapsActivity;
import com.example.grouptracker.Model.Place;
import com.example.grouptracker.Model.User;
import com.example.grouptracker.R;
import com.example.grouptracker.Utils.UserClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder>{

    private Context context;
    private List<Place> mPlaceList;
    private FirebaseFirestore mDb;
    private OnItemClickListener mListener;

    // constructor
    public PlacesAdapter(Context context, List<Place> placeList) {
        this.context = context;
        this.mPlaceList = placeList;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onDeleteClick(int position);
        void onNotificationClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class PlaceViewHolder extends RecyclerView.ViewHolder {
        CircleImageView placeImageView;
        TextView titleTextView, addressTextView;
        ImageButton setLocationButton, deleteGeofenceBtn;
        SwitchCompat setNotificationsButton;
        public PlaceViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            placeImageView = itemView.findViewById(R.id.placeImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            addressTextView = itemView.findViewById(R.id.addressTextView);
            deleteGeofenceBtn = itemView.findViewById(R.id.deleteBtn);
            setNotificationsButton = itemView.findViewById(R.id.setNotificationButton);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });

            setNotificationsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onNotificationClick(position);
                        }
                    }
                }
            });

            deleteGeofenceBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null) {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });
        }
    }

    public PlacesAdapter(ArrayList<Place> placeList) {
        mPlaceList = placeList;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_places, parent, false);
        PlaceViewHolder place = new PlaceViewHolder(v, mListener);
        return place;
    }

    @Override
    public void onBindViewHolder(@NonNull final PlaceViewHolder holder, int position) {
        // get data
        Place place = mPlaceList.get(position);
        if(place.isNotifications()) {
            holder.setNotificationsButton.setChecked(true);
        } else {
            holder.setNotificationsButton.setChecked(false);
        }
        // set data
        holder.titleTextView.setText(place.getPlace_title());
        if(place.getAddress() != null) {
            holder.addressTextView.setText(place.getAddress());
        }
        if(place.getPlace_title().equals("Home")) {
            try {
                Picasso.get().load(R.mipmap.home_place).placeholder(R.mipmap.home_place_foreground).into(holder.placeImageView);
            } catch (Exception e){

            }
        } else if (place.getPlace_title().equals("Work")) {
            try {
                Picasso.get().load(R.mipmap.workplace_geofence).placeholder(R.mipmap.workplace_geofence_foreground).into(holder.placeImageView);
            } catch (Exception e){

            }
        } else {
            try {
                Picasso.get().load(place.getImageUri()).placeholder(R.mipmap.default_place_photo).into(holder.placeImageView);
            } catch (Exception e){

            }
        }
    }

    @Override
    public int getItemCount() {
        return mPlaceList.size();
    }
}
