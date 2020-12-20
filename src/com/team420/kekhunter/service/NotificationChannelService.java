package com.team420.kekhunter.service;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.team420.kekhunter.AsyncTask.CustomCommandsAsyncTask;
import com.team420.kekhunter.BuildConfig;
import com.team420.kekhunter.R;

// IntentService Class for pushing nethunter notification
public class NotificationChannelService extends IntentService {
    public static final String CHANNEL_ID = "NethunterNotifyChannel";
    public static final int NOTIFY_ID = 1002;
    public Intent resultIntent = null;
    public PendingIntent resultPendingIntent = null;
    public TaskStackBuilder stackBuilder = null;
    public static final String REMINDMOUNTCHROOT = BuildConfig.APPLICATION_ID + ".REMINDMOUNTCHROOT";
    public static final String USENETHUNTER = BuildConfig.APPLICATION_ID + ".USENETHUNTER";
    public static final String DOWNLOADING = BuildConfig.APPLICATION_ID + ".DOWNLOADING";
    public static final String SEARCHINGUPDATES = BuildConfig.APPLICATION_ID + ".SEARCHINGUPDATES";
    public static final String NOUPDATES = BuildConfig.APPLICATION_ID + ".NOUPDATES";
    public static final String UPDATING = BuildConfig.APPLICATION_ID + ".UPDATING";
    public static final String INSTALLING = BuildConfig.APPLICATION_ID + ".INSTALLING";
    public static final String NOINTERNET = BuildConfig.APPLICATION_ID + ".NOINTERNET";
    public static final String BACKINGUP = BuildConfig.APPLICATION_ID + ".BACKINGUP";
    public static final String CUSTOMCOMMAND_START = BuildConfig.APPLICATION_ID + ".CUSTOMCOMMAND_START";
    public static final String CUSTOMCOMMAND_FINISH = BuildConfig.APPLICATION_ID + ".CUSTOMCOMMAND_FINISH";

    public NotificationChannelService(){
        super("NotificationChannelService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "NethunterChannelService",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null){
            if (intent.getAction() != null){
                NotificationCompat.Builder builder;
                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
                notificationManagerCompat.cancelAll();
                resultIntent = new Intent();
                stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addNextIntentWithParentStack(resultIntent);
                resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                switch (intent.getAction()){
                    case REMINDMOUNTCHROOT:
                        builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setAutoCancel(true)
                                .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Please open nethunter app and navigate to ChrootManager to setup your KaliChroot."))
                                .setContentTitle("KaliChroot is not up or installed")
                                .setContentText("Please navigate to ChrootManager to setup your KaliChroot.")
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setContentIntent(resultPendingIntent);
                        notificationManagerCompat.notify(NOTIFY_ID, builder.build());
                        break;
                    case USENETHUNTER:
                        builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setAutoCancel(true)
                                .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                                .setTimeoutAfter(10000)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Happy hunting!"))
                                .setContentTitle("KaliChroot is UP!")
                                .setContentText("Happy hunting!")
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setContentIntent(resultPendingIntent);
                        notificationManagerCompat.notify(NOTIFY_ID, builder.build());
                        break;
                    case DOWNLOADING:
                        builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setAutoCancel(true)
                                .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                                .setTimeoutAfter(15000)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Please don't kill the app or the download will be cancelled!"))
                                .setContentTitle("Downloading Chroot!")
                                .setContentText("Please don't kill the app or the download will be cancelled!")
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setContentIntent(resultPendingIntent);
                        notificationManagerCompat.notify(NOTIFY_ID, builder.build());
                        break;
                    case SEARCHINGUPDATES:
                        builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setAutoCancel(true)
                                .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                                .setTimeoutAfter(15000)
                                .setContentTitle("Checking...")
                                .setContentText("Searching for updates")
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setContentIntent(resultPendingIntent);
                        notificationManagerCompat.notify(NOTIFY_ID, builder.build());
                        break;
                    case UPDATING:
                        builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setAutoCancel(true)
                                .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                                .setTimeoutAfter(15000)
                                .setContentTitle("Updating")
                                .setContentText("Wait for updater to downlaod the apk")
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setContentIntent(resultPendingIntent);
                        notificationManagerCompat.notify(NOTIFY_ID, builder.build());
                        break;
                    case NOUPDATES:
                        builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setAutoCancel(true)
                                .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                                .setTimeoutAfter(15000)
                                .setContentTitle("Updater")
                                .setContentText("No updates found")
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setContentIntent(resultPendingIntent);
                        notificationManagerCompat.notify(NOTIFY_ID, builder.build());
                        break;
                    case NOINTERNET:
                        builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setAutoCancel(true)
                                .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                                .setTimeoutAfter(15000)
                                .setContentTitle("Updater")
                                .setContentText("No network access!!")
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setContentIntent(resultPendingIntent);
                        notificationManagerCompat.notify(NOTIFY_ID, builder.build());
                        break;
                    case INSTALLING:
                        builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setAutoCancel(true)
                                .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                                .setTimeoutAfter(15000)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Please don't kill the app as it will still keep running on the background! Otherwise you'll need to kill the tar process by yourself."))
                                .setContentTitle("Installing Chroot")
                                .setContentText("Please don't kill the app as it will still keep running on the background! Otherwise you'll need to kill the tar process by yourself.")
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setContentIntent(resultPendingIntent);
                        notificationManagerCompat.notify(NOTIFY_ID, builder.build());
                        break;
                    case BACKINGUP:
                        builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setAutoCancel(true)
                                .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                                .setTimeoutAfter(15000)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Please don't kill the app as it will still keep running on the background! Otherwise you'll need to kill the tar process by yourself."))
                                .setContentTitle("Creating KaliChroot backup to local storage.")
                                .setContentText("Please don't kill the app as it will still keep running on the background! Otherwise you'll need to kill the tar process by yourself.")
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setContentIntent(resultPendingIntent);
                        notificationManagerCompat.notify(NOTIFY_ID, builder.build());
                        break;
                    case CUSTOMCOMMAND_START:
                        builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setAutoCancel(false)
                                .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                                        "Command: \"" + intent.getStringExtra("CMD") +
                                        "\" is being run in background and in " +
                                        intent.getStringExtra("ENV") + " environment."))
                                .setContentTitle("Custom Commands")
                                .setContentText(
                                        "Command: \"" + intent.getStringExtra("CMD") +
                                        "\" is being run in background and in " +
                                        intent.getStringExtra("ENV") + " environment.")
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setContentIntent(resultPendingIntent);
                        notificationManagerCompat.notify(NOTIFY_ID, builder.build());
                        break;
                    case CUSTOMCOMMAND_FINISH:
                        final int returnCode = intent.getIntExtra("RETURNCODE", 0);
                        final String CMD = intent.getStringExtra("CMD");
                        String resultString = "";
                        if (returnCode == CustomCommandsAsyncTask.ANDROID_CMD_SUCCESS) {
                            resultString = "Return success.\nCommand: \"" + CMD + "\" has been executed in android environment.";
                        } else if (returnCode == CustomCommandsAsyncTask.ANDROID_CMD_FAIL) {
                            resultString = "Return error.\nCommand: \"" + CMD + "\" has been executed in android environment.";
                        } else if (returnCode == CustomCommandsAsyncTask.KALI_CMD_SUCCESS) {
                            resultString = "Return success.\nCommand: \"" + CMD + "\" has been executed in Kali chroot environment.";
                        } else if (returnCode == CustomCommandsAsyncTask.KALI_CMD_FAIL) {
                            resultString = "Return error.\nCommand: \"" + CMD + "\" has been executed in Kali chroot environment.";
                        }
                        builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setAutoCancel(false)
                                .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(resultString))
                                .setContentTitle("Custom Commands")
                                .setContentText(resultString)
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setContentIntent(resultPendingIntent);
                        notificationManagerCompat.notify(NOTIFY_ID, builder.build());
                        break;
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }
}
