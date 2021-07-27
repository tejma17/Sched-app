package com.tejma.sched.fragments;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tejma.sched.POJO.Lecture;
import com.tejma.sched.R;
import com.tejma.sched.Utils.AlarmReceiver;
import com.tejma.sched.Utils.CreateNotification;
import com.tejma.sched.Utils.ExportTimetable;
import com.tejma.sched.Utils.JsonReader;
import com.tejma.sched.activities.HelpActivity;
import com.tejma.sched.activities.Login;
import com.tejma.sched.activities.ReminderSettings;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class Settings extends Fragment {

    View view;
    Button reset, signout, privacy, reminder, export, importData;
    SharedPreferences sharedPreferences;
    TextView textView;
    RadioButton radioButton;
    final int PICKFILE_RESULT_CODE = 1, STORAGE_CODE = 2;
    List<Lecture> lectures = new ArrayList<>();
    ProgressDialog progressDialog;
    DatabaseReference dbRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        reset = view.findViewById(R.id.Reset);
        export = view.findViewById(R.id.export);
        importData = view.findViewById(R.id.importData);
        signout = view.findViewById(R.id.signout);
        textView = view.findViewById(R.id.version);
        privacy = view.findViewById(R.id.privacy);
        reminder = view.findViewById(R.id.reminder);

        String phone = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber();
        phone = Objects.requireNonNull(phone).substring(phone.length() - 4);
        phone = "Logout (******" + phone + ")";
        signout.setText(phone);

        progressDialog = new ProgressDialog(getContext());

        try {
            PackageInfo pInfo = requireContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            String version = "v " + pInfo.versionName;
            textView.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        sharedPreferences = requireActivity().getSharedPreferences("Classes", Context.MODE_PRIVATE);

        reminder.setOnClickListener(view -> {
            startActivity(new Intent(getContext(), ReminderSettings.class));
        });


        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Sure you want to clear data?")
                        .setMessage("You won't be able to recover it");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlarmManager alarmManager = (AlarmManager) getActivity().getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                        String jsonIn = sharedPreferences.getString("Lectures", null);
                        List<Lecture> tempLectures = new Gson().fromJson(jsonIn, new TypeToken<ArrayList<Lecture>>(){}.getType());
                        for(Lecture lecture : tempLectures){
                            Intent intent = CreateNotification.getNotificationIntent(lecture,
                                    getActivity().getApplicationContext());
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(),
                                    lecture.getId(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
                            alarmManager.cancel(pendingIntent);
                        }

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

        view.findViewById(R.id.help).setOnClickListener(v->{
            startActivity(new Intent(getContext(), HelpActivity.class));
        });

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(requireContext())
                        .setMessage("Sure you want to logout?")
                        .setPositiveButton("Logout", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(getContext(), "Successfully logged out", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getContext(), Login.class));
                            requireActivity().finish();
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

        export.setOnClickListener(v -> {
            ExportTimetable.exportFile(getContext());
        });

        importData.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
            bottomSheetDialog.setContentView(R.layout.bottomsheet_import);
            ImageButton file = bottomSheetDialog.findViewById(R.id.fileButton);
            ImageButton server = bottomSheetDialog.findViewById(R.id.webButton);
            bottomSheetDialog.show();

            file.setOnClickListener(v0 -> {
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission();
                    return;
                }
                showFileChooser();
                bottomSheetDialog.dismiss();
            });

            server.setOnClickListener(v1 -> {
                BottomSheetDialog semDialog = new BottomSheetDialog(getContext());
                semDialog.setContentView(R.layout.sem_choose_dialog);
                Button semVII = semDialog.findViewById(R.id.semVII);
                Button semV = semDialog.findViewById(R.id.semV);
                Button semIII = semDialog.findViewById(R.id.semIII);
                RadioGroup radioGroup = semDialog.findViewById(R.id.radioGP);
                semDialog.show();

                radioButton = semDialog.findViewById(radioGroup.getCheckedRadioButtonId());

                radioGroup.setOnCheckedChangeListener((group, checkedId) -> radioButton = semDialog.findViewById(checkedId));

                progressDialog.setMessage("Importing...");

                semVII.setOnClickListener(v2 -> {
                    progressDialog.show();

                    dbRef = FirebaseDatabase.getInstance().getReference("Timetables/Sem VII/"+radioButton.getText());

                    dbRef.addValueEventListener(valueEventListener);

                    dbRef.addChildEventListener(childEventListener);

                    semDialog.dismiss();
                });

                semV.setOnClickListener(v2 -> {
                    progressDialog.show();


                    dbRef = FirebaseDatabase.getInstance().getReference("Timetables/Sem V/"+radioButton.getText());

                    dbRef.addValueEventListener(valueEventListener);

                    dbRef.addChildEventListener(childEventListener);

                    semDialog.dismiss();
                });

                semIII.setOnClickListener(v2 -> {
                    progressDialog.show();

                    dbRef = FirebaseDatabase.getInstance().getReference("Timetables/Sem III/"+radioButton.getText());

                    dbRef.addValueEventListener(valueEventListener);

                    dbRef.addChildEventListener(childEventListener);

                    semDialog.dismiss();
                });

                bottomSheetDialog.dismiss();
            });


        });

        return view;
    }

    private List<Lecture> getLecture(DataSnapshot dataSnapshot) {
        List<Lecture> lectures = new ArrayList<>();
        int totalClass = (int) dataSnapshot.child("classes").getChildrenCount();
        for(int i=0; i<totalClass; i++){
            Lecture lecture = new Lecture();
            lecture.setCode(dataSnapshot.child("code").getValue().toString());
            lecture.setType("Lecture");
            lecture.setSubject(dataSnapshot.child("subject").getValue().toString());
            lecture.setFaculty(dataSnapshot.child("faculty").getValue().toString());
            lecture.setNotified(Boolean.parseBoolean(dataSnapshot.child("isNotified").getValue().toString()));
            lecture.setClassroom_link(dataSnapshot.child("classroom_link").getValue().toString());
            lecture.setMeet_link(dataSnapshot.child("meet_link").getValue().toString());
            lectures.add(lecture);
        }

        for(int i=0; i<lectures.size(); i++) {
            lectures.get(i).setDay(dataSnapshot.child("classes").child(String.valueOf(i)).child("day").getValue().toString());
            lectures.get(i).setTime(dataSnapshot.child("classes").child(String.valueOf(i)).child("time").getValue().toString());
            if (dataSnapshot.child("classes").child(String.valueOf(i)).hasChild("type"))
                lectures.get(i).setType(dataSnapshot.child("classes").child(String.valueOf(i)).child("type").getValue().toString());

            lectures.get(i).setId(lectures.get(i).hashCode());
            CreateNotification.createNotification(getActivity().getApplicationContext(), lectures.get(i));
        }

        return lectures;
    }


    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            List<Lecture> lectureDB = getLecture(snapshot);
            lectures.addAll(lectureDB);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(lectures.size()!=0) {
                sharedPreferences.edit().putString("Lectures", new Gson().toJson(lectures)).apply();
                sharedPreferences.edit().putString("Notification", "NO").apply();
                Toast.makeText(getContext(), "Import successful. Tip: Hold and delete the unnecessary courses", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Import failed", Toast.LENGTH_SHORT).show();
            }
            dbRef.removeEventListener(childEventListener);
            dbRef.removeEventListener(valueEventListener);
            progressDialog.dismiss();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };




    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Storage permission required", Toast.LENGTH_SHORT).show();
                requestStoragePermission();
            }
        }
    }

    public void showFileChooser() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a .scd file");
        startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICKFILE_RESULT_CODE && resultCode == RESULT_OK) {
            if (data == null) {
                Toast.makeText(getContext(), "NULL", Toast.LENGTH_SHORT).show();
                return;
            }
            Uri fileUri = data.getData();
            String path = "null";
            try {
                path = getPath(getContext(), fileUri);
                Log.d("PATH", path);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            if (!path.equals("null")) {
                File file = new File(path);
                if (!file.getName().contains(".scd")) {
                    Toast.makeText(getContext(), "Unsupported file. Needs file with .scd extension", Toast.LENGTH_SHORT).show();
                    return;
                }

                String json = JsonReader.getJsonFrom(getContext(), file);
                if(json!=null) {
                    sharedPreferences.edit().clear().apply();
                    sharedPreferences.edit().putString("Lectures", json).apply();
                    Toast.makeText(getContext(), "Successfully imported from the file", Toast.LENGTH_SHORT).show();
                    generateNotifications(json);
                } else {
                    Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    private void generateNotifications(String json) {
        ArrayList<Lecture> lectures1 = new Gson().fromJson(json, new TypeToken<ArrayList<Lecture>>(){}.getType());
        for(Lecture lecture : lectures1){
            CreateNotification.createNotification(getActivity().getApplicationContext(), lecture);
        }
    }

    public String getPath(Context context, Uri uri) throws URISyntaxException {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{split[1]};
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
