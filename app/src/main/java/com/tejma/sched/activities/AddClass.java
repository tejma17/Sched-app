package com.tejma.sched.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
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
        spinnerL.setAdapter(new ArrayAdapter<String>(AddClass.this,
                android.R.layout.simple_spinner_dropdown_item,
                daysofweek));

        spinnerP.setAdapter(new ArrayAdapter<String>(AddClass.this,
                android.R.layout.simple_spinner_dropdown_item,
                daysofweek));

        spinnerT.setAdapter(new ArrayAdapter<String>(AddClass.this,
                android.R.layout.simple_spinner_dropdown_item,
                daysofweek));

        timeL.setOnClickListener(this::onTimeClick);
        timeP.setOnClickListener(this::onTimeClick);
        timeT.setOnClickListener(this::onTimeClick);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        reminderL.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isRemindL = b;
            }
        });


        reminderP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isRemindP = b;
            }
        });

        reminderT.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isRemindT = b;
            }
        });

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



        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareMap();
                String json = gson.toJson(courses);
                sharedPreferences.edit().putString("Lectures", json).apply();
                sharedPreferences.edit().putString("Notification", "NO").apply();
                Toast.makeText(AddClass.this, "Course Saved", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }


    void prepareMap(){
        if(!timeL.getText().toString().equals("Set Time"))
        courses.add(new Lecture(name.getText().toString(), code.getText().toString(),
                faculty.getText().toString(), type1.getText().toString(),
                timeL.getText().toString(), String.valueOf(dayL), meet.getText().toString(),
                classroom.getText().toString(), isRemindL));
        if(!timeT.getText().toString().equals("Set Time"))
        courses.add(new Lecture(name.getText().toString(), code.getText().toString(),
                faculty.getText().toString(), type2.getText().toString(),
                timeT.getText().toString(), String.valueOf(dayT), meet.getText().toString(),
                classroom.getText().toString(), isRemindT));
        if(!timeP.getText().toString().equals("Set Time"))
        courses.add(new Lecture(name.getText().toString(), code.getText().toString(),
                faculty.getText().toString(), type3.getText().toString(),
                timeP.getText().toString(), String.valueOf(dayP), meet.getText().toString(),
                classroom.getText().toString(), isRemindP));
    }

    private void onTimeClick(View view) {
        TextView textView = (TextView) view;
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(AddClass.this, new TimePickerDialog.OnTimeSetListener() {
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
