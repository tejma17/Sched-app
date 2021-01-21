package com.tejma.sched;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class OpenLinks extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_links);

        Intent intent = getIntent();
        Uri data = intent.getData();

        if(isConnection()) {
            String UID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(UID);
            databaseReference.child("Link").setValue(data.toString());
            Toast.makeText(this, "Success: Visit web-sched.web.app", Toast.LENGTH_LONG).show();
        }else
            Toast.makeText(this, "Failed: Check your internet connection", Toast.LENGTH_SHORT).show();

        finish();

    }

    public boolean isConnection(){
        ConnectivityManager connectivityManager = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}