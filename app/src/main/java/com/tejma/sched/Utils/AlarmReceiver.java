package com.tejma.sched.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tejma.sched.POJO.Lecture;

import java.util.ArrayList;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "channelNotify";
    public static String NOTIFICATION = "notification";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        Lecture lecture = new Gson().fromJson(intent.getStringExtra(NOTIFICATION_ID), Lecture.class);
        int notificationId = lecture.getId();

        SharedPreferences sharedPreferences = context.getSharedPreferences("Classes", Context.MODE_PRIVATE);
        List<Lecture> lectures = new Gson().fromJson(sharedPreferences.getString("Lectures", null), new TypeToken<ArrayList<Lecture>>(){}.getType());
        int notiEnabled = sharedPreferences.getInt("NotiEnabled", 1);

//        Log.d("nneewwww", "ALARM RECEIVED "+lecture.getId());

        if(notiEnabled==1) {
            notificationManager.notify(notificationId, notification);
            Log.d("nneewwww", "ALARM NOTIFIED");
        }

    }


}
