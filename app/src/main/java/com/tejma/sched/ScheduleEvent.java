package com.tejma.sched;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tejma.sched.POJO.Lecture;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class ScheduleEvent extends AppCompatActivity {

    EditText name, code, faculty, classroom, meet, lectureType;
    TextView time;
    Button submit;
    ArrayList<Lecture> lecturesSaved;
    int updatedDay;
    boolean shouldRemind;
    Spinner spinner;
    SwitchMaterial reminder;
    String link;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_class);

        Intent intent = getIntent();
        Uri data = intent.getData();
        link = data.toString();

        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            Toast.makeText(this, "Login to app first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ScheduleEvent.this, Login.class).putExtra("IntentAction", "ScheduleSPLIT"+data.toString()));
            finish();
        }

        sharedPreferences = getSharedPreferences("Classes", MODE_PRIVATE);
        String jsonIn = sharedPreferences.getString("Lectures", null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Lecture>>(){}.getType();
        lecturesSaved = gson.fromJson(jsonIn, type);

        if(lecturesSaved==null){
            lecturesSaved = new ArrayList<>();
        }


        name = findViewById(R.id.subject);
        code = findViewById(R.id.code);
        time = findViewById(R.id.time);
        lectureType = findViewById(R.id.type);
        faculty = findViewById(R.id.faculty);
        meet = findViewById(R.id.meetLink);
        classroom = findViewById(R.id.classroomLink);
        submit = findViewById(R.id.submit);
        spinner = findViewById(R.id.daySpinner);
        reminder = findViewById(R.id.reminder);


        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        ArrayList<String> daysofweek = new ArrayList<String>(Arrays.asList(daysOfWeek));


        spinner.setAdapter(new ArrayAdapter<String>(ScheduleEvent.this,
                android.R.layout.simple_spinner_dropdown_item,
                daysofweek));


        meet.setText(link);
        shouldRemind = true;
        reminder.setChecked(true);

        time.setOnClickListener(this::onTimeClick);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updatedDay = findIndex(daysOfWeek, adapterView.getItemAtPosition(i).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        reminder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                shouldRemind = b;
            }
        });


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareMap();
                String json = gson.toJson(lecturesSaved);
                sharedPreferences.edit().putString("Notification", "NO").apply();
                sharedPreferences.edit().putString("Lectures", json).apply();
                Toast.makeText(ScheduleEvent.this, "Course Updated", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ScheduleEvent.this, Navigation.class));
                finish();
            }
        });

    }



    void prepareMap(){
        lecturesSaved.add(new Lecture(name.getText().toString(), code.getText().toString(),
                faculty.getText().toString(), lectureType.getText().toString(),
                time.getText().toString(), String.valueOf(updatedDay), meet.getText().toString(),
                classroom.getText().toString(), shouldRemind));
    }


    private void onTimeClick(View view) {
        TextView textView = (TextView) view;
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(ScheduleEvent.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                String hour = String.valueOf(selectedHour), minute = String.valueOf(selectedMinute);
                if(selectedHour<10)
                    hour = "0"+selectedHour;
                if(selectedMinute<10)
                    minute = "0"+selectedMinute;
                String time = hour + ":" + minute;
                textView.setText(time);
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.show();
    }


    public static int findIndex(String[] arr, String t)
    {
        int len = arr.length;
        int i = 0;
        while (i < len) {
            if (arr[i].equals(t)) {
                return i+1;
            }
            else {
                i = i + 1;
            }
        }
        return -1;
    }
}