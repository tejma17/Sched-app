package com.tejma.sched;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Login extends AppCompatActivity {

    private TextView resend, msg;
    private RelativeLayout auto_detect;
    private MaterialButton login;
    private TextInputLayout mobile;
    private EditText otp;
    private String mVerificationId;
    private FirebaseAuth firebaseAuth;
    private final String TAG = "HELLO";
    private String[] intentContent;
    private String intentString;
    private String Mobile, Code;
    private boolean isNew = true;
    private CountDownTimer timer;
    private int count_resends = 0;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        resend = findViewById(R.id.resend);
        msg = findViewById(R.id.msg);
        login = findViewById(R.id.submit_otp);
        mobile = findViewById(R.id.phoneid);
        otp = findViewById(R.id.otpid);
        auto_detect = findViewById(R.id.auto_detect);
        firebaseAuth = FirebaseAuth.getInstance();

        intentString = getIntent().getStringExtra("IntentAction");
        if(intentString!=null)
            intentContent = intentString.split("SPLIT");

        timer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateTimer((int) millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                resend.setEnabled(true);
                resend.setTextColor(getResources().getColor(R.color.select));
                resend.setText("Resend OTP");
            }
        };

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                // Set the verification text based on the credential
                //Toast.makeText(Login.this, "DONE", Toast.LENGTH_SHORT).show();
                if (credential.getSmsCode() != null) {
                    otp.setText(credential.getSmsCode());
                }
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                login.setEnabled(true);
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(Login.this, "Invalid mobile number", Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(Login.this, "Too many requests. Try again later.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, token);
                mVerificationId = verificationId;
                mResendToken = token;
                if(count_resends!=3)
                    updateUI();
                else{
                    login.setEnabled(true);
                    resend.setEnabled(false);
                    isNew = false;
                }
                Toast.makeText(Login.this, "OTP sent successfully", Toast.LENGTH_SHORT).show();
            }
        };

        login.setOnClickListener(v -> {
            login.setEnabled(false);
            if(!isConnection()){
                Toast.makeText(this, "Enable network access.", Toast.LENGTH_SHORT).show();
                return;
            }
            if(isNew){
                Mobile = Objects.requireNonNull(mobile.getEditText()).getText().toString().trim();
                if(mobile.getEditText().getText().toString().trim().isEmpty()){
                    Toast.makeText(Login.this, "Enter a mobile number", Toast.LENGTH_SHORT).show();
                    login.setEnabled(true);
                    return;
                }
                if(Mobile.length()<13){
                    Toast.makeText(this, "Include country code", Toast.LENGTH_SHORT).show();
                    login.setEnabled(true);
                    return;
                }
                startPhoneNumberVerification(Mobile);
            }else {
                Code = otp.getText().toString().trim();
                if(Code.isEmpty()){
                    login.setEnabled(true);
                    Toast.makeText(Login.this, "Enter a otp number", Toast.LENGTH_SHORT).show();
                    return;
                }
                verifyPhoneNumberWithCode(mVerificationId, Code);
            }
        });

        resend.setOnClickListener(v -> {
            count_resends++;
            if(count_resends<=3) {
                int remain = 3-count_resends;
                if(remain==1)
                    Toast.makeText(this, "You have "+remain+" more attempt left", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "You have "+remain+" more attempts left", Toast.LENGTH_SHORT).show();
                resend.setEnabled(false);
                resend.setTextColor(getResources().getColor(R.color.text_hint));
                if(remain != 0) {
                    timer.start();
                } else {
                    resend.setVisibility(View.INVISIBLE);
                }
                resendVerificationCode(Mobile, mResendToken);
            }
        });

    }

    private void updateUI(){
        otp.setVisibility(View.VISIBLE);
        mobile.setVisibility(View.GONE);
        login.setVisibility(View.GONE);
        if(count_resends==0)
            auto_detect.setVisibility(View.VISIBLE);
        else
            login.setVisibility(View.VISIBLE);
        msg.setVisibility(View.VISIBLE);
        resend.setVisibility(View.VISIBLE);
        timer.start();
        login.setEnabled(true);
        msg.setText("Verification code sent to "+Mobile);
        resend.setEnabled(false);
        login.setText("Submit");
        isNew = false;
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        if(intentString!=null){
                            if(intentContent[0].equals("OpenLink")) {
                                startActivity(new Intent(Login.this, OpenLinks.class).setData(Uri.parse(intentContent[1])));
                            }else{
                                startActivity(new Intent(Login.this, ScheduleEvent.class).setData(Uri.parse(intentContent[1])));
                            }
                        }else {
                            boolean isNew = Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getAdditionalUserInfo()).isNewUser();
                            if (isNew) {
                                Toast.makeText(Login.this, "Welcome to Sched", Toast.LENGTH_SHORT).show();
                            } else
                                Toast.makeText(Login.this, "Welcome back", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Login.this, Navigation.class));
                        }
                        finish();
                    } else {
                        FirebaseAuthException e = (FirebaseAuthException) task.getException();
                        otp.setText("");
                        login.setEnabled(true);
                        if(e instanceof FirebaseAuthInvalidCredentialsException)
                            Toast.makeText(this, "Invalid OTP. Resend OTP to try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(30L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(token)// OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void updateTimer(int sec){
        String rem_sec;
        rem_sec = "00:"+sec;
        if(sec<10)
            rem_sec = "00:0"+sec;
        if(sec<25) {
            auto_detect.setVisibility(View.GONE);
            login.setVisibility(View.VISIBLE);
            otp.requestFocus();
        }
        resend.setText("Resend OTP in "+rem_sec);
    }

    @Override
    protected void onStart() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null) {
            startActivity(new Intent(Login.this, Navigation.class));
            finish();
        }
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        if(isNew){
            finish();
        }
        else
            recreate();
    }

    public boolean isConnection(){
        ConnectivityManager connectivityManager = ((ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}