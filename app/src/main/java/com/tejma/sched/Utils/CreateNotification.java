package com.tejma.sched.Utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.tejma.sched.POJO.Lecture;
import com.tejma.sched.R;
import com.tejma.sched.activities.Navigation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class CreateNotification {

    public static final String CHANNEL_ID = "channelNotify";
    public static  Notification notification;
    public static PendingIntent pendingIntentphone, pendingIntentPC;
    public static AlarmManager alarmManager;
    public static int notifyBefore;
    private static SharedPreferences sharedPreferences;

    public static void createNotification(Context context, Lecture lecture){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){

            int id = lecture.getId();

            sharedPreferences = context.getSharedPreferences("Classes", Context.MODE_PRIVATE);
            notifyBefore = sharedPreferences.getInt("NotifyBefore", 10);


            //intent for alarm manager
            Intent notificationIntent = getNotificationIntent(lecture, context);

            PendingIntent notificationPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), lecture.getId(),
                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


            Calendar time = setTime(lecture, notifyBefore);
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, h:mm a", Locale.getDefault());

            if(System.currentTimeMillis() > time.getTimeInMillis()) {
                time.add(Calendar.WEEK_OF_YEAR, 1);
            }

//            Log.d("nneewwww", sdf.format(time.getTimeInMillis()));

            if (alarmManager == null)
                    alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(),
                        7 * AlarmManager.INTERVAL_DAY, notificationPendingIntent);

//                Log.d("nneewwww", "ALARM CREATED " + notificationPendingIntent.toString());
        }
    }

    public static Calendar setTime(Lecture lecture, int notifyBefore){
        String[] lecTimeDivided = lecture.getTime().split(":");
        Calendar time = Calendar.getInstance();

        // getDay return string containing int values of week staring from 1 for monday to 7 for sunday
        int day =  findIndex(Integer.parseInt(lecture.getDay()));

        int hour = Integer.parseInt(lecTimeDivided[0]);
        int minutes =  Integer.parseInt(lecTimeDivided[1]);

        if(minutes<notifyBefore){
            int diff = notifyBefore-minutes;
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
            minutes = minutes-notifyBefore;
        }
        time.set(Calendar.DAY_OF_WEEK, day);
        time.set(Calendar.HOUR_OF_DAY, hour);
        time.set(Calendar.MINUTE, minutes);
        time.set(Calendar.SECOND, 0);
        return time;
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

    public static Intent getNotificationIntent(Lecture lecture, Context context){
        String titleText = lecture.getSubject();
        if (titleText.length() > 25) {
            titleText = titleText.substring(0, 25) + "...";
        }


        //intent for phone button
        Intent intentPhone = new Intent(context, NotificationActionsReceiver.class)
                .setAction("phone");
        intentPhone.putExtra(AlarmReceiver.NOTIFICATION_ID, lecture.getId());
        intentPhone.putExtra("Link", lecture.getMeet_link());
        pendingIntentphone = PendingIntent.getBroadcast(context, lecture.getId(),
                intentPhone, PendingIntent.FLAG_UPDATE_CURRENT);

        //intent for PC button
        Intent intentPC = new Intent(context, NotificationActionsReceiver.class)
                .setAction("PC");
        intentPC.putExtra(AlarmReceiver.NOTIFICATION_ID, lecture.getId());
        intentPC.putExtra("Link", lecture.getMeet_link());
        pendingIntentPC = PendingIntent.getBroadcast(context, lecture.getId(),
                intentPC, PendingIntent.FLAG_UPDATE_CURRENT);

        //intent for notification onclick
        Intent resultIntent = new Intent(context, Navigation.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(lecture.getId(), PendingIntent.FLAG_UPDATE_CURRENT);

        notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_trans)
                .setContentTitle(titleText)
                .setContentText(lecture.getType()+" class scheduled in "+notifyBefore+" minutes")
                .setAutoCancel(true)
                .setShowWhen(false)
                .setOnlyAlertOnce(false)
                .setColor(ContextCompat.getColor(context, R.color.select))
                .setContentIntent(resultPendingIntent)
                .addAction(R.drawable.ic_google_meet_color, "Join on phone", pendingIntentphone)
                .addAction(R.drawable.ic_google_meet_color, "Join on PC", pendingIntentPC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();


        Intent notificationIntent = new Intent(context, AlarmReceiver.class);
        notificationIntent.setAction("FIRE_ALARM_SCHED");
        notificationIntent.putExtra(AlarmReceiver.NOTIFICATION_ID, new Gson().toJson(lecture));
        notificationIntent.putExtra(AlarmReceiver.NOTIFICATION, notification);

        return notificationIntent;
    }

}
