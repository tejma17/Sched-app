package com.tejma.sched;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class Settings extends Fragment {

    View view;
    Button rate, reset, share, signout, privacy;
    SharedPreferences sharedPreferences;
    SwitchMaterial dark;
    TextView textView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        rate = view.findViewById(R.id.rate);
        reset = view.findViewById(R.id.Reset);
        dark = view.findViewById(R.id.mode);
        share = view.findViewById(R.id.share);
        signout = view.findViewById(R.id.signout);
        textView = view.findViewById(R.id.version);
        privacy = view.findViewById(R.id.privacy);

        String phone = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber();
        phone = Objects.requireNonNull(phone).substring(phone.length()-4);
        phone = "Logout (******"+phone+")";
        signout.setText(phone);

        try {
            PackageInfo pInfo = Objects.requireNonNull(getContext()).getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            String version = "v "+pInfo.versionName;
            textView.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences("Classes", Context.MODE_PRIVATE);
        int nightModeFlags =
                Objects.requireNonNull(getContext()).getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        String mode = sharedPreferences.getString("Night", "Not Defined");
        if(mode.equals("Not Defined")){
            if(nightModeFlags == Configuration.UI_MODE_NIGHT_YES){
                dark.setChecked(true);
            }
        }else if(mode.equals("true")){
            dark.setChecked(true);
        }


        dark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                sharedPreferences.edit().putString("Night", String.valueOf(b)).apply();
            }
        });


        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("market://details?id=" + Objects.requireNonNull(getContext()).getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                }
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + Objects.requireNonNull(getContext()).getPackageName())));
                }
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                builder.setTitle("Sure you want to clear data?")
                .setMessage("You won't be able to recover it");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sharedPreferences.edit().clear().apply();
                        Toast.makeText(getContext(), "Data Cleared", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                AlertDialog ad = builder.create();
                ad.show();


            }
        });


        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "\nNever miss a class again. Download Sched:\nhttps://play.google.com/store/apps/details?id=com.tejma.sched";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                        .setMessage("Sure you want to logout?")
                        .setPositiveButton("Logout", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(getContext(), "Successfully logged out", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getContext(), Login.class));
                            Objects.requireNonNull(getActivity()).finish();
                        })
                        .setNegativeButton(android.R.string.no, (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });


        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://web-sched.web.app/privacy.html"));
                startActivity(intent);
            }
        });
        return view;
    }
}
