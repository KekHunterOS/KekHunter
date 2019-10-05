package com.offsec.nethunter.service;

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

import com.offsec.nethunter.AppNavHomeActivity;
import com.offsec.nethunter.BuildConfig;
import com.offsec.nethunter.ChrootManagerFragment;
import com.offsec.nethunter.KaliServicesFragment;
import com.offsec.nethunter.R;
import com.offsec.nethunter.utils.CheckForRoot;
import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class RunAtBootService extends IntentService {

    private static final String CHROOT_INSTALLED_TAG = "CHROOT_INSTALLED_TAG";
    private static final String TAG = "Nethunter: Startup";
    private final ShellExecuter exe = new ShellExecuter();
    boolean isAllFine = true;
    private NotificationCompat.Builder n = null;
    private HashMap<String, String> hashMap = new HashMap<>();
    private NhPaths nhPaths;

    public RunAtBootService(){
        super("RunAtBootService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Create notification channel first.
        createNotificationChannel();
        nhPaths = NhPaths.getInstance(getApplicationContext());

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

    @Override
    protected void onHandleIntent(@NonNull Intent intent) {

        //1. Check root -> 2. Check Busybox -> 3. run nethunter init.d files. -> Push notifications.
        String isOK = "OK.";
        doNotification("Doing boot checks...");

        hashMap.put("ROOT", "No root access is granted.");
        hashMap.put("BUSYBOX", "No busybox is found.");
        hashMap.put("KALICHROOT", "kali Chroot is not yet installed.");

        if (CheckForRoot.isRoot()) {
            hashMap.put("ROOT", isOK);
        }

        if (CheckForRoot.isBusyboxInstalled()) {
            hashMap.put("BUSYBOX", isOK);
        }

        exe.RunAsRootOutput(NhPaths.BUSYBOX + " run-parts " + NhPaths.APP_INITD_PATH);
        if (exe.RunAsRootReturnValue(NhPaths.APP_SCRIPTS_PATH + "/chrootmgr -c \"status\"") == 0){
            // remove possible vnc locks (if the phone is rebooted with the vnc server running)
            exe.RunAsRootOutput("rm -rf " + NhPaths.CHROOT_PATH() + "/tmp/.X1*");
            hashMap.put("KALICHROOT", isOK);
        }

        String resultMsg = "Boot completed.\nEveryting is fine and Kali Chroot has been started!";
        for(Map.Entry<String, String> entry: hashMap.entrySet()){
            if (!entry.getValue().equals(isOK)){
                isAllFine = false;
                resultMsg = "Make sure the above requirements are met.";
                break;
            }
        }

        doNotification(
                "Root: " + hashMap.get("ROOT") + "\n" +
                "Busybox: " + hashMap.get("BUSYBOX") + "\n" +
                "Kali Chroot: " + hashMap.get("KALICHROOT") + "\n" +
                resultMsg);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (nhPaths != null) {
            nhPaths.onDestroy();
        }
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    AppNavHomeActivity.BOOT_CHANNEL_ID,
                    "Nethunter Boot Check Service",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(serviceChannel);
        }
    }
}
