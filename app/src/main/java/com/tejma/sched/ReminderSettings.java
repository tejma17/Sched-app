package com.tejma.sched;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.tejma.sched.databinding.ActivityReminderSettingsBinding;

import java.util.Objects;

public class ReminderSettings extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {

    private ActivityReminderSettingsBinding binding;
    private SharedPreferences sharedPreferences;
    private int remindBefore, notiEnabled;
    private String[] reminderArray = { "15 minutes", "10 minutes", "5 minutes", "3 minutes"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReminderSettingsBinding.inflate(getLayoutInflater());
        View view1 = binding.getRoot();
        setContentView(view1);

        sharedPreferences = getSharedPreferences("Classes", MODE_PRIVATE);

        remindBefore = sharedPreferences.getInt("NotifyBefore", 15);
        notiEnabled = sharedPreferences.getInt("NotiEnabled", 1);


        binding.spinner.setOnItemSelectedListener(this);
        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,reminderArray);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinner.setAdapter(aa);
        int pos = remindBefore%3;
        if(remindBefore==3)
            pos = 3;
        binding.spinner.setSelection(pos);


        binding.switch1.setChecked(notiEnabled == 1);
        binding.spinner.setEnabled(notiEnabled==1);


        binding.switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    sharedPreferences.edit().putInt("NotiEnabled", 1).apply();
                    notiEnabled = 1;
                    binding.spinner.setEnabled(true);
                }
                else {
                    sharedPreferences.edit().putInt("NotiEnabled", 0).apply();
                    notiEnabled = 0;
                    binding.spinner.setEnabled(false);
                }
            }
        });


        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        sharedPreferences.edit().putString("Notification", "NO").apply();

    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        remindBefore = Integer.parseInt(reminderArray[position].split(" ")[0]);
        sharedPreferences.edit().putInt("NotifyBefore", remindBefore).apply();
        if(notiEnabled==1)
            Toast.makeText(getApplicationContext(),"Reminder will be shown before "+reminderArray[position] , Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {

    }
}