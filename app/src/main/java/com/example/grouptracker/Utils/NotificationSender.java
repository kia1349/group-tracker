package com.example.grouptracker.Utils;

import com.example.grouptracker.Model.Data;

public class NotificationSender {
    public Data data;
    public String to;

    public NotificationSender(Data data, String to) {
        this.data = data;
        this.to = to;
    }

    public NotificationSender() {

    }
}
