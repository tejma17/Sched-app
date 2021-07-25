package com.tejma.sched.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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

public class EditClass extends AppCompatActivity {

    EditText name, code, faculty, meet, classroom, lectureType;
    TextView time;
    ImageButton back;
    Button submit;
    ArrayList<Lecture> lecturesSaved;
    int updatedDay;
    Lecture liveLecture;
    boolean shouldRemind;
    Spinner spinner;
    SwitchMaterial reminder;
    CheckBox checkBox;
    String lecName,  lecFaculty, lecCode, lecMeet;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_class);


        String[] info = getIntent().getStringExtra("Lecture").split("GAP");

        sharedPreferences = getSharedPreferences("Classes", MODE_PRIVATE);
        String jsonIn = sharedPreferences.getString("Lectures", null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Lecture>>(){}.getType();
        lecturesSaved = gson.fromJson(jsonIn, type);

        if(lecturesSaved==null){
            lecturesSaved = new ArrayList<>();
        }

        for(Lecture lecture: lecturesSaved){
            if(lecture.getDay().equals(info[3])
                    && lecture.getType().equals(info[2])
                    && lecture.getTime().equals(info[1])
                    && lecture.getCode().equals(info[0])){
                liveLecture = lecture;
                Log.d("CLASSES", liveLecture.getSubject()+" "+liveLecture.getTime());
                break;
            }
        }

        back = findViewById(R.id.back);
        name = findViewById(R.id.subject);
        code = findViewById(R.id.code);
        time = findViewById(R.id.time);
        checkBox = findViewById(R.id.meetCommon);
        lectureType = findViewById(R.id.type);
        faculty = findViewById(R.id.faculty);
        meet = findViewById(R.id.meetLink);
        classroom = findViewById(R.id.classroomLink);
        submit = findViewById(R.id.submit);
        spinner = findViewById(R.id.daySpinner);
        reminder = findViewById(R.id.reminder);

        checkBox.setText("Common for all classes of "+ liveLecture.getSubject());

        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        ArrayList<String> daysofweek = new ArrayList<String>(Arrays.asList(daysOfWeek));

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        spinner.setAdapter(new ArrayAdapter<String>(EditClass.this,
                android.R.layout.simple_spinner_dropdown_item,
                daysofweek));

        name.setText(liveLecture.getSubject());
        code.setText(liveLecture.getCode());
        faculty.setText(liveLecture.getFaculty());
        meet.setText(liveLecture.getMeet_link());
        classroom.setText(liveLecture.getClassroom_link());
        lectureType.setText(liveLecture.getType());
        spinner.setSelection(Integer.parseInt(liveLecture.getDay())-1);
        time.setText(liveLecture.getTime());
        reminder.setChecked(liveLecture.isNotified());
        shouldRemind = liveLecture.isNotified();

        lecName = liveLecture.getSubject();
        lecCode = liveLecture.getCode();
        lecFaculty = liveLecture.getFaculty();
        lecMeet = liveLecture.getMeet_link();

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
                Toast.makeText(EditClass.this, "Course Updated", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }



    void prepareMap(){
        lecturesSaved.remove(liveLecture);

        lecturesSaved.add(new Lecture(name.getText().toString(), code.getText().toString(),
                faculty.getText().toString(), lectureType.getText().toString(),
                time.getText().toString(), String.valueOf(updatedDay), meet.getText().toString(),
                classroom.getText().toString(), shouldRemind));

        if(!name.getText().toString().equals(lecName)){
            changeField("name");
        }
        if(!code.getText().toString().equals(lecCode)){
            changeField("code");
        }
        if(checkBox.isChecked() && !meet.getText().toString().equals(lecMeet)){
            changeField("meet");
        }
        changeField("classroom");
    }

    private void changeField(String field) {
        switch (field) {
            case "name":
                for (Lecture lecture : lecturesSaved) {
                    if (lecture.getSubject().equals(lecName))
                        lecture.setSubject(name.getText().toString());
                }
                break;
            case "code":
                for (Lecture lecture : lecturesSaved) {
                    if (lecture.getCode().equals(lecCode))
                        lecture.setCode(code.getText().toString());
                }
                break;
            case "meet":
                for (Lecture lecture : lecturesSaved) {
                    if (lecture.getSubject().equals(lecName))
                        lecture.setMeet_link(meet.getText().toString().trim());
                }
                break;
        }

        for(Lecture lecture: lecturesSaved){
            if(lecture.getSubject().equals(lecName))
                lecture.setClassroom_link(classroom.getText().toString().trim());
        }
    }

    private void onTimeClick(View view) {
        TextView textView = (TextView) view;
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(EditClass.this, new TimePickerDialog.OnTimeSetListener() {
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