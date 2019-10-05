package com.offsec.nethunter.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.offsec.nethunter.AppNavHomeActivity;
import com.offsec.nethunter.BuildConfig;
import com.offsec.nethunter.utils.CheckForRoot;
import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.SharePrefTag;
import com.offsec.nethunter.utils.ShellExecuter;

// IntentService class for keep checking the campatibaility every time user switch back to the app.
public class CompatCheckService extends IntentService {

    private static String message = "";
    private int RESULTCODE = -1;
    private SharedPreferences sharedPreferences;
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
        // if no resultCode passed by ChrootManagerFragment, then set RESULTCODE to -1;
        if (intent != null){
            RESULTCODE = intent.getIntExtra("RESULTCODE", -1);
        }

        // run checkCompat function, and sendbroadcast back to Main activity if user fails the compat check.
        if (!checkCompat()) {
            getApplicationContext().sendBroadcast(new Intent()
                    .putExtra("message", message)
                    .setAction(BuildConfig.APPLICATION_ID + ".CHECKCOMPAT"));
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getApplicationContext().getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);
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
        // First, check if root access is acquired.
        if (!CheckForRoot.isRoot()) {
            message = "Root permission is required!!";
            return false;
        }
        // Secondly, check if busybox is present.
        if (!CheckForRoot.isBusyboxInstalled()) {
            message = "No busybox is detected, please make sure you have busybox installed!!";
            return false;
        }
        // Lastly, check if nethunter terminal app has been installed.
        if (getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.offsec.nhterm") == null) {
            message = "Nethunter terminal is not installed yet.";
            return false;
        }

        // All of code below will always be executed every time this service is started.
        // Check if selinux is in permissive mode.
        new ShellExecuter().RunAsRootOutput("[ ! \"$(getenforce | grep Permissive)\" ] && setenforce 0");

        // Check only for the first installation, find out the possible chroot folder and point the chroot path to it.
        String[] chrootDirs = new ShellExecuter().RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/chrootmgr -c \"findchroot\"").split("\\n");
        if (sharedPreferences.getString(SharePrefTag.CHROOT_ARCH_SHAREPREF_TAG, null) == null) {
            sharedPreferences.edit().putString(SharePrefTag.CHROOT_ARCH_SHAREPREF_TAG, chrootDirs[0]).apply();
            sharedPreferences.edit().putString(SharePrefTag.CHROOT_PATH_SHAREPREF_TAG, NhPaths.NH_SYSTEM_PATH + "/" + chrootDirs[0]).apply();
        }

        // Check chroot status, push notification to user and disable all the fragments if chroot is not yet up.
        if (RESULTCODE == -1){
            if ((new ShellExecuter().RunAsRootReturnValue(NhPaths.APP_SCRIPTS_PATH + "/chrootmgr -c \"status\" -p " + NhPaths.CHROOT_PATH()) != 0)){
                startService(new Intent(getApplicationContext(), NotificationChannelService.class).setAction(NotificationChannelService.REMINDMOUNTCHROOT));
                getApplicationContext().sendBroadcast(new Intent()
                        .putExtra("ENABLEFRAGMENT", false)
                        .setAction(AppNavHomeActivity.NethunterReceiver.CHECKCHROOT));
            } else {
                getApplicationContext().sendBroadcast(new Intent()
                        .putExtra("ENABLEFRAGMENT", true)
                        .setAction(AppNavHomeActivity.NethunterReceiver.CHECKCHROOT));
            }
        } else {
            if (RESULTCODE != 0) {
                startService(new Intent(getApplicationContext(), NotificationChannelService.class).setAction(NotificationChannelService.REMINDMOUNTCHROOT));
                getApplicationContext().sendBroadcast(new Intent()
                        .putExtra("ENABLEFRAGMENT", false)
                        .setAction(AppNavHomeActivity.NethunterReceiver.CHECKCHROOT));
            } else {
                getApplicationContext().sendBroadcast(new Intent()
                        .putExtra("ENABLEFRAGMENT", true)
                        .setAction(AppNavHomeActivity.NethunterReceiver.CHECKCHROOT));
            }
        }

        return true;
    }
}
