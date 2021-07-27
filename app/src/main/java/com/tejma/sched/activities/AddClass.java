package com.tejma.sched.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tejma.sched.POJO.Lecture;
import com.tejma.sched.R;
import com.tejma.sched.Utils.AlarmReceiver;
import com.tejma.sched.Utils.CreateNotification;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class AddClass extends AppCompatActivity {

    EditText name, code, faculty, meet, classroom, type1, type2, type3;
    Button submit;
    SwitchMaterial reminderL, reminderT, reminderP;
    boolean isRemindL, isRemindP, isRemindT;
    ImageButton back;
    ArrayList<Lecture> courses;
    TextView timeL, timeP, timeT;
    int dayL, dayP, dayT;
    Spinner spinnerL, spinnerP, spinnerT;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);

        back = findViewById(R.id.back);
        name = findViewById(R.id.subject);
        code = findViewById(R.id.code);
        timeL = findViewById(R.id.timeLecture);
        timeP = findViewById(R.id.timePractical);
        timeT = findViewById(R.id.timeTut);
        faculty = findViewById(R.id.faculty);
        meet = findViewById(R.id.meetLink);
        classroom = findViewById(R.id.classroomLink);
        submit = findViewById(R.id.submit);
        spinnerL = findViewById(R.id.dayLecture);
        spinnerP = findViewById(R.id.dayPractical);
        spinnerT = findViewById(R.id.dayTut);
        reminderL = findViewById(R.id.reminderL);
        reminderT = findViewById(R.id.reminderT);
        reminderP = findViewById(R.id.reminderP);
        type1 = findViewById(R.id.type1);
        type2 = findViewById(R.id.type2);
        type3 = findViewById(R.id.type3);

        isRemindL = isRemindP = isRemindT = true;

        ArrayList<String> daysofweek = new ArrayList<String>();
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        daysofweek.addAll(Arrays.asList(daysOfWeek));
        spinnerL.setAdapter(new ArrayAdapter<>(AddClass.this,
                android.R.layout.simple_spinner_dropdown_item,
                daysofweek));

        spinnerP.setAdapter(new ArrayAdapter<>(AddClass.this,
                android.R.layout.simple_spinner_dropdown_item,
                daysofweek));

        spinnerT.setAdapter(new ArrayAdapter<>(AddClass.this,
                android.R.layout.simple_spinner_dropdown_item,
                daysofweek));

        timeL.setOnClickListener(this::onTimeClick);
        timeP.setOnClickListener(this::onTimeClick);
        timeT.setOnClickListener(this::onTimeClick);

        back.setOnClickListener(view -> onBackPressed());


        reminderL.setOnCheckedChangeListener((compoundButton, b) -> isRemindL = b);


        reminderP.setOnCheckedChangeListener((compoundButton, b) -> isRemindP = b);

        reminderT.setOnCheckedChangeListener((compoundButton, b) -> isRemindT = b);

        spinnerL.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                dayL = findIndex(daysOfWeek, adapterView.getItemAtPosition(i).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                dayP = findIndex(daysOfWeek, adapterView.getItemAtPosition(i).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerT.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                dayT = findIndex(daysOfWeek, adapterView.getItemAtPosition(i).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        sharedPreferences = getSharedPreferences("Classes", MODE_PRIVATE);
        String jsonIn = sharedPreferences.getString("Lectures", null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Lecture>>(){}.getType();
        courses = gson.fromJson(jsonIn, type);
        if(courses == null){
            courses = new ArrayList<>();
        }



        submit.setOnClickListener(view -> {
            prepareMap();
            String json = gson.toJson(courses);
            sharedPreferences.edit().putString("Lectures", json).apply();
            sharedPreferences.edit().putString("Notification", "NO").apply();
            Toast.makeText(AddClass.this, "Course Saved", Toast.LENGTH_SHORT).show();
            finish();
        });

    }


    void prepareMap(){
        Lecture lecture = new Lecture();
        lecture.setSubject(name.getText().toString().trim());
        lecture.setClassroom_link(classroom.getText().toString().trim());
        lecture.setMeet_link(meet.getText().toString().trim());
        lecture.setCode(code.getText().toString().trim());
        lecture.setFaculty(faculty.getText().toString().trim());

        Lecture lectureT = new Lecture();
        lectureT.setSubject(name.getText().toString().trim());
        lectureT.setClassroom_link(classroom.getText().toString().trim());
        lectureT.setMeet_link(meet.getText().toString().trim());
        lectureT.setCode(code.getText().toString().trim());
        lectureT.setFaculty(faculty.getText().toString().trim());

        Lecture lectureP = new Lecture();
        lectureP.setSubject(name.getText().toString().trim());
        lectureP.setClassroom_link(classroom.getText().toString().trim());
        lectureP.setMeet_link(meet.getText().toString().trim());
        lectureP.setCode(code.getText().toString().trim());
        lectureP.setFaculty(faculty.getText().toString().trim());

        Lecture lectureL = new Lecture();
        lectureL.setSubject(name.getText().toString().trim());
        lectureL.setClassroom_link(classroom.getText().toString().trim());
        lectureL.setMeet_link(meet.getText().toString().trim());
        lectureL.setCode(code.getText().toString().trim());
        lectureL.setFaculty(faculty.getText().toString().trim());

        if(!timeL.getText().toString().equals("Set Time")) {
            lectureL.setTime(timeL.getText().toString().trim());
            lectureL.setDay(String.valueOf(dayL));
            lectureL.setNotified(isRemindL);
            lectureL.setType("LECTURE");
            lectureL.setId(lectureL.hashCode());
            courses.add(lectureL);
            CreateNotification.createNotification(getApplicationContext(), lectureL);
        }
        if(!timeT.getText().toString().equals("Set Time")) {
            lectureT.setTime(timeT.getText().toString().trim());
            lectureT.setDay(String.valueOf(dayT));
            lectureT.setNotified(isRemindT);
            lectureT.setType("TUTORIAL");
            lectureT.setId(lectureT.hashCode());
            courses.add(lectureT);
            CreateNotification.createNotification(getApplicationContext(), lectureT);
        }
        if(!timeP.getText().toString().equals("Set Time")){
            lectureP.setTime(timeP.getText().toString().trim());
            lectureP.setDay(String.valueOf(dayP));
            lectureP.setNotified(isRemindP);
            lectureP.setType("PRACTICAL");
            lectureP.setId(lectureP.hashCode());
            courses.add(lectureP);
            CreateNotification.createNotification(getApplicationContext(), lectureP);
        }
    }

    private void onTimeClick(View view) {
        TextView textView = (TextView) view;
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(AddClass.this, (timePicker, selectedHour, selectedMinute) -> {
                String hour1 = String.valueOf(selectedHour), minute1 = String.valueOf(selectedMinute);
                if(selectedHour<10)
                    hour1 = "0"+selectedHour;
                if(selectedMinute<10)
                    minute1 = "0"+selectedMinute;
                String time = hour1 + ":" + minute1;
                textView.setText(time);
            }, hour, minute, true);//Yes 24 hour time
            mTimePicker.show();
    }

    public static int findIndex(String[] arr, String t) {
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
