package com.tejma.sched.Service;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class NotificationBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //Log.i("TAG", "IN NOTI");
        context.sendBroadcast(new Intent("tracks")
                .putExtra("actionName", intent.getAction()));

        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String type = intent.getAction();
        String link = intent.getStringExtra("Link");
        switch (type){
            case "phone":
                    if (!link.startsWith("https://"))
                        link = "https://" + link;
                    Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent1);
                   // Log.i("ID", intent.getIntExtra(AlarmBroadcast.NOTIFICATION_ID, 0)+"");
                    notificationManager.cancel(intent.getIntExtra(AlarmBroadcast.NOTIFICATION_ID, 0));
                break;
            case "PC":
                    if (!link.startsWith("https://"))
                        link = "https://" + link;
                    String UID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(UID);
                    databaseReference.child("Link").setValue(link);
                    notificationManager.cancel(intent.getIntExtra(AlarmBroadcast.NOTIFICATION_ID, 0));
                break;
        }
    }


}
