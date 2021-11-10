package com.tejma.sched.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tejma.sched.POJO.Lecture;
import com.tejma.sched.POJO.NotificationSaved;
import com.tejma.sched.R;
import com.tejma.sched.adapters.NotificationAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.ContentValues.TAG;

public class NotificationsActivity extends AppCompatActivity implements NotificationAdapter.OnClickListener{

    private NotificationAdapter notificationAdapter;
    private ArrayList<NotificationSaved> savedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        SharedPreferences sharedPreferences = getSharedPreferences("Classes", Context.MODE_PRIVATE);

        savedList = new ArrayList<>();

        ((Toolbar)findViewById(R.id.toolbar)).setNavigationOnClickListener(v->finish());

        String jsonIn = sharedPreferences.getString("savedNoti", null);
        if(jsonIn!=null)
            savedList = new Gson().fromJson(jsonIn,
                new TypeToken<ArrayList<NotificationSaved>>(){}.getType());

        if(savedList == null || savedList.size()==0)
            findViewById(R.id.msg).setVisibility(View.VISIBLE);

        Collections.reverse(savedList);

        notificationAdapter = new NotificationAdapter(this, savedList, this);

        ((RecyclerView)findViewById(R.id.notiRV)).setAdapter(notificationAdapter);
    }

    @Override
    public void onClick(int position, View v) {

    }

    @Override
    public void onLongClick(int position) {

    }
}