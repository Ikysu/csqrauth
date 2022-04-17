package com.ikysu.csqrauth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AlertDialogLayout;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.Executor;

public class BioCheck extends AppCompatActivity {
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo  promptInfo;
    private Button authBtn;
    private Button cancelBtn;
    private TextView textView;

    private String TAG = "FFFFFFFFFFFFFFFF";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "BioCheck 1");

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        String org = getIntent().getStringExtra("org");
        String ip = getIntent().getStringExtra("ip");
        String uid = getIntent().getStringExtra("uid");

        Context context = this;

        Log.d(TAG, "BioCheck 2");

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(BioCheck.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(context, errString, Toast.LENGTH_SHORT).show();
                finishAndRemoveTask();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.d(TAG, "Auth success");
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try  {
                            HttpURLConnection urlConnection = null;
                            URL url = new URL("https://cs-qr-auth.iky.su/app/"+uid+"/"+getIntent().getStringExtra("t"));
                            urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setRequestMethod("GET");
                            urlConnection.setConnectTimeout(15000 /* milliseconds */ );
                            urlConnection.connect();

                            Log.d("SSSSSSSSSSSSSSSSSSSS", ""+urlConnection.getResponseCode());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();
                finishAndRemoveTask();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Log.d(TAG, "Auth failed");
            }
        });


        Log.d(TAG, "BioCheck 3");

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Авторизация в "+org)
                .setSubtitle("IP: "+ip)
                .setDescription(uid)
                .setNegativeButtonText("Cancel")
                .setConfirmationRequired(true)
                .build();

        Log.d(TAG, "BioCheck 4");

        //authBtn = findViewById(R.id.btnAuth);
        //cancelBtn = findViewById(R.id.btnCancel);
        //textView = findViewById(R.id.textView);

        //textView.setText(org+": "+ip);

        //authBtn.setEnabled(true);
        //cancelBtn.setEnabled(true);

        //authBtn.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        biometricPrompt.authenticate(promptInfo);
        //    }
        //});

        biometricPrompt.authenticate(promptInfo);

        Log.d(TAG, "BioCheck 5");

    }


}
