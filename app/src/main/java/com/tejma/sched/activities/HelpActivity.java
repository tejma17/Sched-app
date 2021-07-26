package com.tejma.sched.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.tejma.sched.R;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        findViewById(R.id.back).setOnClickListener(view -> finish());

        ((TextView)findViewById(R.id.pc_share)).setMovementMethod(LinkMovementMethod.getInstance());
    }
}