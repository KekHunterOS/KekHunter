package com.offsec.nethunter.service;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.offsec.nethunter.AppNavHomeActivity;
import com.offsec.nethunter.BuildConfig;
import com.offsec.nethunter.R;
import com.offsec.nethunter.utils.CheckForRoot;
import com.offsec.nethunter.utils.PermissionCheck;

public class CompatCheckService extends IntentService {

    private static String message = "";
    public CompatCheckService(){
        super("CompatCheckService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (!checkCompat()) {
            getApplicationContext().sendBroadcast(new Intent()
                    .putExtra("message", message)
                    .setAction(BuildConfig.APPLICATION_ID + ".CHECKCOMPAT"));
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

    }

    private boolean checkCompat() {
        if (!CheckForRoot.isRoot()) {
            message = "Root permission is required!!";
            return false;
        }
        // Secondly, check if busybox is present.
        if (!CheckForRoot.isBusyboxInstalled()) {
            message = "No busybox is detected, please make sure you have busybox installed!!";
            return false;
        }
        // Thirdly, check if nethunter terminal app has been installed.
        if (getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.offsec.nhterm") == null) {
            message = "Nethunter terminal is not installed yet.";
            return false;
        }
        return true;
    }


}
