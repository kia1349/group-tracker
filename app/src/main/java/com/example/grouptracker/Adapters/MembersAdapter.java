package com.example.grouptracker.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grouptracker.Model.User;
import com.example.grouptracker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MembersViewHolder>
{
    ArrayList<User> nameList;
    Context c;
    MembersAdapter(ArrayList<User> nameList, Context c)
    {
        this.nameList = nameList;
        this.c = c;
    }

    @Override
    public int getItemCount() {
        return nameList.size();
    }

    @NonNull
    @Override
    public MembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_users, parent, false);
        MembersViewHolder membersViewHolder = new MembersViewHolder(v, c, nameList);

        return membersViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MembersViewHolder holder, int position) {
        User currentUserObject = nameList.get(position);
        holder.name_txt.setText(currentUserObject.name);
        Picasso.get().load(currentUserObject.imageUri).placeholder(R.mipmap.default_male_photo).into(holder.circleImageView);

        if(currentUserObject.isSharing.equals("false"))
        {
            holder.i1.setImageResource(R.drawable.ic_offline);
        }
        else if(currentUserObject.isSharing.equals("true"))
        {
            holder.i1.setImageResource(R.drawable.ic_online);
        }
    }

    public static class MembersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView name_txt;
        CircleImageView circleImageView;
        ImageView i1;
        Context c;
        ArrayList<User> nameArrayList;
        FirebaseAuth auth;
        FirebaseUser user;

        public MembersViewHolder(@NonNull View itemView, Context c, ArrayList<User> nameArrayList) {
            super(itemView);
            this.c = c;
            this.nameArrayList = nameArrayList;

            itemView.setOnClickListener(this);
            auth = FirebaseAuth.getInstance();
            user = auth.getCurrentUser();

            //name_txt = itemView.findViewById(R.id.item_title);
            //circleImageView = itemView.findViewById(R.id.personImageView);
            i1 = itemView.findViewById(R.id.isSharing);
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(c, "You have clicked this user", Toast.LENGTH_LONG).show();
        }
    }
}
