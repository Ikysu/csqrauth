package com.ikysu.csqrauth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startForegroundService(new Intent(context, NotifyService.class));
    }
}
