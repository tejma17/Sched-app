package com.tejma.sched;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.tejma.sched.POJO.Lecture;
import com.tejma.sched.Service.AlarmBroadcast;
import com.tejma.sched.Service.NotificationBroadcast;

import java.util.Calendar;

public class CreateNotification {

    public static final String CHANNEL_ID = "channelNotify";
    public static  Notification notification;

    public static void createNotification(Context context, Lecture lecture, int id){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){

            PendingIntent pendingIntentphone, pendingIntentPC;

            Intent intentPhone = new Intent(context, NotificationBroadcast.class)
                    .setAction("phone");
            intentPhone.putExtra(AlarmBroadcast.NOTIFICATION_ID, id);
            intentPhone.putExtra("Link", lecture.getMeet_link());
            pendingIntentphone = PendingIntent.getBroadcast(context, id,
                    intentPhone, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent intentPC = new Intent(context, NotificationBroadcast.class)
                    .setAction("PC");
            intentPC.putExtra(AlarmBroadcast.NOTIFICATION_ID, id);
            intentPC.putExtra("Link", lecture.getMeet_link());
            pendingIntentPC = PendingIntent.getBroadcast(context, id,
                    intentPC, PendingIntent.FLAG_UPDATE_CURRENT);


            Intent resultIntent = new Intent(context, Navigation.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT);

            String titleText = lecture.getSubject();
            if (titleText.length() > 25) {
                titleText = titleText.substring(0, 25) + "...";
            }

            notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.logo_trans)
                    .setContentTitle(titleText)
                    .setContentText(lecture.getType()+" class scheduled in 10 minutes")
                    .setAutoCancel(true)
                    .setShowWhen(false)
                    .setOnlyAlertOnce(false)
                    .setColor(ContextCompat.getColor(context, R.color.select))
                    .setContentIntent(resultPendingIntent)
                    .addAction(R.drawable.ic_google_meet, "Join on Phone", pendingIntentphone)
                    .addAction(R.drawable.ic_google_meet, "Join on PC", pendingIntentPC)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build();

            Intent notificationIntent = new Intent(context, AlarmBroadcast.class);
            notificationIntent.setAction(lecture.getMeet_link());
            notificationIntent.putExtra(AlarmBroadcast.NOTIFICATION_ID, id);
            notificationIntent.putExtra(AlarmBroadcast.NOTIFICATION, notification);
            PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, id,
                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


            // getDay return string containing int values of week staring from 1 for monday to 7 for sunday

            String[] lecTimeDivided = lecture.getTime().split(":");
            Calendar time = Calendar.getInstance();

            int day =  findIndex(Integer.parseInt(lecture.getDay()));
           // Log.i("Details= "+lecture.getSubject()+" "+lecture.getType(), lecture.getDay()+" "+day);
            int hour = Integer.parseInt(lecTimeDivided[0]);
            int minutes =  Integer.parseInt(lecTimeDivided[1]);
            if(minutes<10){
                int diff = 10-minutes;
                minutes = 60-diff;
                if(hour==0){
                    hour = 23;
                    if(day==2)
                        day = findIndex(7);
                    else
                        day = findIndex(Integer.parseInt(lecture.getDay())-1);
                }else
                    hour--;
            }else{
                minutes = minutes-10;
            }
            time.set(Calendar.DAY_OF_WEEK, day);
            time.set(Calendar.HOUR_OF_DAY, hour);
            time.set(Calendar.MINUTE, minutes);
            time.set(Calendar.SECOND, 0);

            if(System.currentTimeMillis()-time.getTimeInMillis()<=480000) {
                Log.i("Scheduled "+lecture.getSubject()+" "+lecture.getType(), "At "+time.getTime()+" id "+id);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent2);
            }
        }
    }

    public static int findIndex(int t) {
        int[] arr = {6, 7, 1, 2, 3, 4, 5};
        int len = arr.length;
        int i = 0;
        while (i < len) {
            if (arr[i]==(t)) {
                return i;
            }
            else {
                i = i + 1;
            }
        }
        return -1;
    }


}
