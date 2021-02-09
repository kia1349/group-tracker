package com.example.grouptracker.Interface;

import com.example.grouptracker.Utils.MyResponse;
import com.example.grouptracker.Utils.NotificationSender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAogBbIDk:APA91bEq4v2mfkZrAI1eYQJkupC8GawtEhpynckBTyJSfYh-h5REZH5OzPVdb_mqZBeFBH-Il5ZLr19HdjPTY8rGahVFYfNgkBQdvFFASKOU1ItAF5ywsIzJk2XTXQtbVVsgSYanNXKi"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body NotificationSender body);
}
