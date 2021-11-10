package com.tejma.sched.Utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tejma.sched.POJO.NotificationSaved;
import com.tejma.sched.R;
import com.tejma.sched.activities.Navigation;

import java.util.ArrayList;
import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public  static int NOTIFICATION_ID = 1;
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        generateNotification(Objects.requireNonNull(remoteMessage.getNotification()).getBody(), remoteMessage.getNotification().getTitle());
        
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    public void generateNotification(String body, String title) {

        Intent intent = new Intent(getApplicationContext(), Navigation.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), "Miscellaneous")
                .setSmallIcon(R.drawable.logo_trans)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.select))
                .setSound(soundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        if (NOTIFICATION_ID > 1073741824) {
           NOTIFICATION_ID = 0;
        }

        //save notifications
        SharedPreferences sharedPreferences = getSharedPreferences("Classes", Context.MODE_PRIVATE);
        String jsonIn = sharedPreferences.getString("savedNoti", null);
        ArrayList<NotificationSaved> notificationSaveds = new ArrayList<>();
        if(jsonIn!=null)
            notificationSaveds = new Gson().fromJson(jsonIn,
                    new TypeToken<ArrayList<NotificationSaved>>(){}.getType());

        notificationSaveds.add(new NotificationSaved(title, body));
        sharedPreferences.edit().putString("savedNoti", new Gson().toJson(notificationSaveds)).apply();

        notificationManager.notify(NOTIFICATION_ID++, notificationBuilder.build());

    }
}
