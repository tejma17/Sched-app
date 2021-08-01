package com.tejma.sched.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tejma.sched.R;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        TextView testLink = findViewById(R.id.test_share);
        TextView shareFeature = findViewById(R.id.pc_share);

        findViewById(R.id.back).setOnClickListener(view -> finish());

        shareFeature.setMovementMethod(LinkMovementMethod.getInstance());

        testLink.setMovementMethod(LinkMovementMethod.getInstance());
    }
}