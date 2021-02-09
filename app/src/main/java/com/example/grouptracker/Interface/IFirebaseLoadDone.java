package com.example.grouptracker.Interface;

import java.util.List;

public interface IFirebaseLoadDone {
    void onFirebaseLoadUserNameDone(List<String> listEmail);
    void onFirebaseLoadFailed(String message);
}
