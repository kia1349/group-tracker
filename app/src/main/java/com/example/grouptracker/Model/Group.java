package com.example.grouptracker.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Group implements Serializable {

    private String group_title;
    private String group_id;
    private String group_admin;
    private String group_code;
    private List<String> placeList = new ArrayList<>();
    private List<String> memberList = new ArrayList<>();

    public Group(String group_title, String group_id, String group_admin, String group_code) {
        this.group_title = group_title;
        this.group_id = group_id;
        this.group_admin = group_admin;
        this.group_code = group_code;
    }

    public List<String> getMemberList() {
        return memberList;
    }

    public void setMemberList(List<String> memberList) {
        this.memberList = memberList;
    }

    public List<String> getPlaceList() {
        return placeList;
    }

    public void setPlaceList(List<String> placeList) {
        this.placeList = placeList;
    }

    public String getGroup_code() {
        return group_code;
    }

    public void setGroup_code(String group_code) {
        this.group_code = group_code;
    }

    @Override
    public String toString() {
        return "Group{" +
                "group_title='" + group_title + '\'' +
                ", group_id='" + group_id + '\'' +
                ", group_admin='" + group_admin + '\'' +
                ", group_code='" + group_code + '\'' +
                '}';
    }

    public String getGroup_title() {
        return group_title;
    }

    public void setGroup_title(String group_title) {
        this.group_title = group_title;
    }

    public String getGroup_admin() {
        return group_admin;
    }

    public void setGroup_admin(String group_admin) {
        this.group_admin = group_admin;
    }

    public Group(String group_title, String group_id, String group_admin) {
        this.group_title = group_title;
        this.group_id = group_id;
        this.group_admin = group_admin;
    }

    public Group() {

    }

    protected Group(Parcel in) {
        group_title = in.readString();
        group_id = in.readString();
        group_admin = in.readString();
    }

    public void addPlace(String place) {
        placeList.add(place);
    }

    public void addMember(String user) {
        memberList.add(user);
    }

    public String getTitle() {
        return group_title;
    }

    public void setTitle(String title) {
        this.group_title = title;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String chatroom_id) {
        this.group_id = chatroom_id;
    }
}
