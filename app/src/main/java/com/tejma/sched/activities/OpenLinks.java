package com.tejma.sched.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tejma.sched.R;

public class OpenLinks extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_links);

        Intent intent = getIntent();
        Uri data = intent.getData();
        String action = intent.getAction();
        String type = intent.getType();

        if(isConnection()) {
            String url = "";
            if (data!=null)
                url = data.toString();

            if (Intent.ACTION_SEND.equals(action) && type != null) {
                if ("text/plain".equals(type)) {
                    String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                    if(URLUtil.isValidUrl(text)){
                        url = text;
                    }else{
                        Toast.makeText(this, "Not a valid URL", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                }
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user!=null) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(user.getUid());
                databaseReference.child("Link").setValue(url);
                Toast.makeText(this, "Success: Visit web-sched.web.app", Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(this, "Login to app first", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(OpenLinks.this, Login.class).putExtra("IntentAction", "OpenLinkSPLIT"+url));
                finish();
            }
        }else
            Toast.makeText(this, "Failed: Check your internet connection", Toast.LENGTH_SHORT).show();

        finish();

    }

    public boolean isConnection(){
        ConnectivityManager connectivityManager = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}