package com.tejma.sched.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;
import com.tejma.sched.Utils.CreateNotification;
import com.tejma.sched.fragments.Home;
import com.tejma.sched.R;
import com.tejma.sched.Utils.AlarmBroadcast;
import com.tejma.sched.Utils.OnClearFromRecentService;
import com.tejma.sched.fragments.Settings;

public class Navigation extends AppCompatActivity {

    SpaceNavigationView spaceNavigationView;
    SpaceOnClickListener spaceOnClickListener;
    boolean homeActive = true;
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);


        spaceNavigationView = findViewById(R.id.spaceView);
        spaceNavigationView.initWithSaveInstanceState(savedInstanceState);
        spaceNavigationView.addSpaceItem(new SpaceItem("HOME", R.drawable.ic_round_home_24));
        spaceNavigationView.addSpaceItem(new SpaceItem("SETTINGS", R.drawable.ic_round_settings_24));

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new Home()).commit();
        }

        checkUpdate();

        spaceOnClickListener = new SpaceOnClickListener() {
            @Override
            public void onCentreButtonClick() {
                Pair[] pairs = new Pair[1];
                pairs[0] = new Pair<View, String>(spaceNavigationView, "ADDCLICK");
                startActivity(new Intent(getApplicationContext(), AddClass.class),
                        ActivityOptions.makeSceneTransitionAnimation(Navigation.this).toBundle());
            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {
                switch (itemIndex){
                    case 0:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container,
                                        new Home()).commit();
                        homeActive = true;
                        break;
                    case 1:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container,
                                        new Settings()).commit();
                        homeActive = false;
                        break;
                }
            }

            @Override
            public void onItemReselected(int itemIndex, String itemName) {

            }
        };

        spaceNavigationView.setSpaceOnClickListener(spaceOnClickListener);
    }


    @Override
    public void onStart() {
        super.onStart();
        createChannel();
        IntentFilter filter = new IntentFilter("tracks");
        registerReceiver(broadcastReceiver, filter);
        startService(new Intent(getBaseContext(), OnClearFromRecentService.class));

    }

    public void createChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(CreateNotification.CHANNEL_ID, "Class Alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLockscreenVisibility( Notification.VISIBILITY_PUBLIC);
            notificationManager = getSystemService(NotificationManager.class);
            if(notificationManager!=null){
                notificationManager.createNotificationChannel(notificationChannel);

            }
        }
    }

    @Override
    public void onBackPressed() {
        if(!homeActive){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container,
                            new Home()).commit();
            homeActive = true;
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String link = intent.getExtras().getString("actionName");
            Log.i("LINK IS", link+"SSS");
            Log.i("TAG", "IN HOME");
            if(link.contains("meet.google.com/")) {
                if (link.startsWith("meet.google.com/"))
                    link = "https://" + link;
                Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                startActivity(intent1);
                notificationManager.cancel(intent.getIntExtra(AlarmBroadcast.NOTIFICATION_ID, 100));
            }
        }
    };


    private void checkUpdate() {
        // Creates instance of the manager.
        AppUpdateManager appUpdateManager;
        appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());

        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // For a flexible update, use AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo, AppUpdateType.FLEXIBLE, this, 100);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode != RESULT_OK) {
                Log.i("TAG", "Update flow failed! Result code: " + resultCode);
            }
        }
    }
}