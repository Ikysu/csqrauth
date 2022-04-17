package com.ikysu.csqrauth;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotifyService extends Service {

    private static final String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE";
    private String t;

    @Override
    public IBinder onBind(Intent i) {
        return null;
    }

    private String TAG = "DDDDDDDDDDDDDD";
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    private Timer timer;

    private int wait = 2000;

    private Intent intent;

    public void onCreate() {
        super.onCreate();

        t=md5(Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID));

        intent = new Intent(this, BioCheck.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.putExtra("t", t);

    }






    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(ACTION_STOP_SERVICE.equals(intent.getAction())) {
            timer.cancel();
            stopForeground(true);
            stopSelf();
        }else{
            createNotificationChannel();
            Intent notificationIntent = new Intent(this, NotifyService.class);
            notificationIntent.setAction(ACTION_STOP_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Code server QR Auth")
                    .setContentText("Background service: tap to stop it")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
                    .build();


            startForeground(1, notification);

            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask(){
                @Override
                public void run(){
                    doNotifyCall();
                }
            },0,wait);
        }



        return START_STICKY;
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }













    private void doBioCall(String org, String ip, String uid) {
        Log.d(TAG, "Open BIO");
        intent.removeExtra("org");
        intent.removeExtra("ip");
        intent.removeExtra("uid");
        intent.putExtra("org", org);
        intent.putExtra("ip", ip);
        intent.putExtra("uid", uid);
        startActivity(intent);
    }



    public void doNotifyCall() {
        try  {
            JSONArray jsonArray = getJSONObjectFromURL("https://cs-qr-auth.iky.su/notify/"+t);
            if(jsonArray.length()>0){
                JSONObject j = jsonArray.getJSONObject(0);
                if(j.getString("type").equals("new")){
                    doBioCall(j.getString("org"), j.getString("ip"), j.getString("uid"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }














    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static JSONArray getJSONObjectFromURL(String urlString) throws IOException, JSONException {
        HttpURLConnection urlConnection = null;
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(10000 /* milliseconds */);
        urlConnection.setConnectTimeout(15000 /* milliseconds */);
        urlConnection.setDoOutput(true);
        urlConnection.connect();

        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();

        String jsonString = sb.toString();

        return new JSONArray(jsonString);
    }

}
