package com.team420.kekhunter.service;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.team420.kekhunter.AppNavHomeActivity;
import com.team420.kekhunter.BuildConfig;
import com.team420.kekhunter.ChrootManagerFragment;
import com.team420.kekhunter.KaliServicesFragment;
import com.team420.kekhunter.R;
import com.team420.kekhunter.RecyclerViewData.NethunterData;
import com.team420.kekhunter.utils.CheckForRoot;
import com.team420.kekhunter.utils.NhPaths;
import com.team420.kekhunter.utils.ShellExecuter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class RunAtBootService extends JobIntentService {

    private static final String TAG = "Nethunter: Startup";
    static final int SERVICE_JOB_ID = 1;
    private NotificationCompat.Builder n = null;

    @Override
    public void onCreate() {
        super.onCreate();
        NhPaths.getInstance(getApplicationContext());
        // Create notification channel first.
        createNotificationChannel();
    }

    private void doNotification(String contents) {
        if (n == null) {
            n = new NotificationCompat.Builder(getApplicationContext(), AppNavHomeActivity.BOOT_CHANNEL_ID);
        }
        n.setStyle(new NotificationCompat.BigTextStyle().bigText(contents))
                .setContentTitle(RunAtBootService.TAG)
                .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                .setAutoCancel(true);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(999, n.build());
        }
    }

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, RunAtBootService.class, SERVICE_JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        onHandleIntent(intent);
    }

    protected void onHandleIntent(@NonNull Intent intent) {
        //1. Check root -> 2. Check Busybox -> 3. run nethunter init.d files. -> Push notifications.
        String isOK = "OK.";
        doNotification("Doing boot checks...");

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("ROOT", "No root access is granted.");
        hashMap.put("BUSYBOX", "No busybox is found.");
        hashMap.put("CHROOT", "Chroot is not yet installed.");

        if (CheckForRoot.isRoot()) {
            hashMap.put("ROOT", isOK);
        }

        if (CheckForRoot.isBusyboxInstalled()) {
            hashMap.put("BUSYBOX", isOK);
        }

        ShellExecuter exe = new ShellExecuter();
        exe.RunAsRootOutput(NhPaths.BUSYBOX + " run-parts " + NhPaths.APP_INITD_PATH);
        if (exe.RunAsRootReturnValue(NhPaths.APP_SCRIPTS_PATH + "/chrootmgr -c \"status\"") == 0){
            // remove possible vnc locks (if the phone is rebooted with the vnc server running)
            exe.RunAsRootOutput("rm -rf " + NhPaths.CHROOT_PATH() + "/tmp/.X1*");
            hashMap.put("CHROOT", isOK);
        }

        String resultMsg = "Boot completed.\nEveryting is fine and Chroot has been started!";
        for(Map.Entry<String, String> entry: hashMap.entrySet()){
            if (!entry.getValue().equals(isOK)){
                resultMsg = "Make sure the above requirements are met.";
                break;
            }
        }

        doNotification(
                "Root: " + hashMap.get("ROOT") + "\n" +
                "Busybox: " + hashMap.get("BUSYBOX") + "\n" +
                "Chroot: " + hashMap.get("CHROOT") + "\n" +
                resultMsg);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    AppNavHomeActivity.BOOT_CHANNEL_ID,
                    "Nethunter Boot Check Service",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
