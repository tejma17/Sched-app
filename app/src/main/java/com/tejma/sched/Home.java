package com.tejma.sched;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tejma.sched.POJO.Lecture;
import com.tejma.sched.adapters.MyAdapter;
import com.tejma.sched.adapters.OnSwipeTouchListener;
import com.tejma.sched.comparator.CompareByTime;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class Home extends Fragment implements MyAdapter.onNoteListener, View.OnClickListener {

    View view;
    private int today;
    private RecyclerView list;
    private MyAdapter recyclerViewAdapter;
    private MaterialButton[] buttons;
    private MaterialButton selected;
    private int[] ids;
    private int scrollWidth;
    private Gson gson;
    private Type type;
    private HorizontalScrollView hsv;
    private RelativeLayout swipeLayout;
    private LinearLayout animationView;
    private String notificationSent;
    private int notiEnabled;
    private final int REQUEST_CODE = 0;
    private SharedPreferences sharedPreferences;
    private ArrayList<Lecture> lectures = new ArrayList<>();
    private final ArrayList<Lecture> todayLectures = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        ids = new int[]{R.id.day1, R.id.day2, R.id.day3,
                R.id.day4, R.id.day5, R.id.day6, R.id.day7};

        sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences("Classes", Context.MODE_PRIVATE);
        String jsonIn = sharedPreferences.getString("Lectures", null);
        gson = new Gson();
        type = new TypeToken<ArrayList<Lecture>>(){}.getType();
        lectures = gson.fromJson(jsonIn, type);
        if(lectures==null){
            lectures = new ArrayList<>();
        }

        initElements();


        list.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
            @Override
            public void onSwipeLeft() {
                hsv.post(new Runnable() {
                    @Override
                    public void run() {
                        int x, y;
                        int index = Integer.parseInt(selected.getTag().toString())-1;
                        if(index<6){
                            x = buttons[index+1].getLeft();
                            y = buttons[index+1].getTop();
                            hsv.scrollTo(x, y);
                            onClick(buttons[index+1]);
                        }
                    }
                });
            }

            @Override
            public void onSwipeRight() {
                hsv.post(new Runnable() {
                    @Override
                    public void run() {
                        int x, y;
                        int index = Integer.parseInt(selected.getTag().toString())-1;
                        if(index>0) {
                            x = buttons[index - 1].getLeft();
                            y = buttons[index - 1].getTop();
                            hsv.scrollTo(x, y);
                            onClick(buttons[index - 1]);
                        }
                    }
                });
            }

        });


        return view;
    }


    private void checkClasses(int day) {
        todayLectures.clear();
        notificationSent = sharedPreferences.getString("Notification", "NO");
        notiEnabled = sharedPreferences.getInt("NotiEnabled", 1);

        int i = 100;
        boolean flag = false;
        for(Lecture lecture: lectures){

            if(notiEnabled==1 && lecture.isNotified() && notificationSent.equals("NO")) {
                CreateNotification.createNotification(getActivity(), lecture, i);
                i++;
                flag = true;
                //Toast.makeText(getContext(), "Scheduled "+i, Toast.LENGTH_SHORT).show();
            }

            if(Integer.parseInt(lecture.getDay()) == day){
                todayLectures.add(lecture);
            }
        }

        Collections.sort(todayLectures, new CompareByTime());
        if(flag)
            notificationSent = "YES";

        if(todayLectures.size()==0)
            animationView.setVisibility(View.VISIBLE);
        else
            animationView.setVisibility(View.GONE);

        recyclerViewAdapter = new MyAdapter(Objects.requireNonNull(getContext()), selected.getTag().toString(),
                R.layout.class_detail_layout, todayLectures, this);
        list.setAdapter(recyclerViewAdapter);
        sharedPreferences.edit().putString("Notification", notificationSent).apply();
    }


    public void deSelect(MaterialButton button){
        for(MaterialButton but : buttons){
            if(but.getId() != button.getId() && but.isEnabled()) {
                but.setStrokeColorResource(R.color.select);
                but.setBackgroundColor(getResources().getColor(R.color.list));
                but.setTextColor(getResources().getColor(R.color.select));
            }
        }
    }



    private void initElements() {
        list = view.findViewById(R.id.list);
        animationView = view.findViewById(R.id.noSub);

        buttons = new MaterialButton[7];
        for(int i=0; i<buttons.length; i++){
            buttons[i] = view.findViewById(ids[i]);
            buttons[i].setOnClickListener(this);
        }

        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        today = (calendar.get(Calendar.DAY_OF_WEEK));
        if(today>=2)
            today-=1;
        else
            today = 7;

        scrollWidth = buttons[0].getWidth();
        selected = buttons[today-1];
        recyclerViewAdapter = new MyAdapter(Objects.requireNonNull(getContext()), selected.getTag().toString(),
                R.layout.class_detail_layout, todayLectures, this);
        list.setAdapter(recyclerViewAdapter);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        onClick(buttons[today-1]);
        checkClasses(today);

        hsv =  view.findViewById(R.id.days);
        hsv.post(new Runnable() {
            @Override
            public void run() {
                int x, y;
                x = buttons[today-1].getLeft();
                y = buttons[today-1].getTop();
                hsv.scrollTo(x, y);
            }
        });
    }

    @Override
    public void onNoteClick(int position, View v) {
        String info = todayLectures.get(position).getCode()+"GAP"+todayLectures.get(position).getTime()+"GAP"+todayLectures.get(position).getType();
        startActivity(new Intent(getContext(), EditClass.class).putExtra("Lecture", info));
    }

    @Override
    public void onNoteLongClick(int position) {
        Lecture lecture = todayLectures.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select action")
                .setMessage("Choose whether you want to remove the "+ lecture.getSubject()+
                        " course or only its "+lecture.getType()+" class")
                .setPositiveButton("Class", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        todayLectures.remove(lecture);
                        lectures.remove(lecture);
                        recyclerViewAdapter = new MyAdapter(Objects.requireNonNull(getContext()), selected.getTag().toString(),
                                R.layout.class_detail_layout, todayLectures, Home.this);
                        list.setAdapter(recyclerViewAdapter);
                        String json = gson.toJson(lectures);
                        Toast.makeText(getContext(), "Successfully removed class", Toast.LENGTH_SHORT).show();
                        sharedPreferences.edit().putString("Lectures", json).apply();
                        if(todayLectures.size()==0)
                            animationView.setVisibility(View.VISIBLE);
                        else
                            animationView.setVisibility(View.GONE);

                    }
                })
                .setNegativeButton("Course", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for(Iterator<Lecture> iterator = todayLectures.iterator(); iterator.hasNext();){
                    Lecture lecture1 = iterator.next();
                    if(lecture1.getSubject().equals(lecture.getSubject())){
                        iterator.remove();
                    }
                }

                for(Iterator<Lecture> iterator = lectures.iterator(); iterator.hasNext();){
                    Lecture lecture1 = iterator.next();
                    if(lecture1.getSubject().equals(lecture.getSubject())){
                        iterator.remove();
                    }
                }

                recyclerViewAdapter = new MyAdapter(Objects.requireNonNull(getContext()), selected.getTag().toString(),
                        R.layout.class_detail_layout, todayLectures, Home.this);
                list.setAdapter(recyclerViewAdapter);
                String json = gson.toJson(lectures);
                Toast.makeText(getContext(), "Successfully removed course", Toast.LENGTH_SHORT).show();
                sharedPreferences.edit().putString("Lectures", json).apply();

                if(todayLectures.size()==0)
                    animationView.setVisibility(View.VISIBLE);
                else
                    animationView.setVisibility(View.GONE);

            }
        }).show();

    }

    @Override
    public void onClick(View view) {
        MaterialButton materialButton = (MaterialButton) view;
        materialButton.setBackgroundColor(getResources().getColor(R.color.select));
        materialButton.setTextColor(getResources().getColor(R.color.background));
        materialButton.setStrokeColorResource(R.color.select);
        checkClasses(Integer.parseInt(materialButton.getTag().toString()));
        deSelect(materialButton);
        selected = materialButton;
    }

}
