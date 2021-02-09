package com.example.grouptracker.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grouptracker.Model.Group;
import com.example.grouptracker.R;

import java.util.ArrayList;

public class GroupRecyclerAdapter extends RecyclerView.Adapter<GroupRecyclerAdapter.ViewHolder>{

    private ArrayList<Group> mGroups = new ArrayList<>();
    private GroupRecyclerClickListener mGroupRecyclerClickListener;

    public GroupRecyclerAdapter(ArrayList<Group> chatrooms, GroupRecyclerClickListener chatroomRecyclerClickListener) {
        this.mGroups = chatrooms;
        mGroupRecyclerClickListener = chatroomRecyclerClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_group_list_item, parent, false);
        final ViewHolder holder = new ViewHolder(view, mGroupRecyclerClickListener);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.chatroomTitle.setText(mGroups.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return mGroups.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener
    {
        TextView chatroomTitle;
        GroupRecyclerClickListener clickListener;

        public ViewHolder(View itemView, GroupRecyclerClickListener clickListener) {
            super(itemView);
            chatroomTitle = itemView.findViewById(R.id.group_title);
            this.clickListener = clickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onGroupSelected(getAdapterPosition());
        }
    }

    public interface GroupRecyclerClickListener {
        void onGroupSelected(int position);
    }
}
















