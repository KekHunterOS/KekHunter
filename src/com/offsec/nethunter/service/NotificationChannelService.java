package com.offsec.nethunter.service;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.offsec.nethunter.BuildConfig;
import com.offsec.nethunter.R;

// IntentService Class for pushing nethunter notification
public class NotificationChannelService extends IntentService {
    public static final String CHANNEL_ID = "NethunterNotifyChannel";
    public static final int NOTIFY_ID = 1002;

    public static final String REMINDMOUNTCHROOT = BuildConfig.APPLICATION_ID + ".REMINDMOUNTCHROOT";
    public static final String USENETHUNTER = BuildConfig.APPLICATION_ID + ".USENETHUNTER";
    public static final String DOWNLOADING = BuildConfig.APPLICATION_ID + ".DOWNLOADING";
    public static final String INSTALLING = BuildConfig.APPLICATION_ID + ".INSTALLING";
    public static final String BACKINGUP = BuildConfig.APPLICATION_ID + ".BACKINGUP";

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
                switch (intent.getAction()){
                    case REMINDMOUNTCHROOT:
                        notificationManagerCompat.cancelAll();
                        /*Intent resultIntent = new Intent(getApplicationContext(), AppNavHomeActivity.class);
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                        stackBuilder.addNextIntentWithParentStack(resultIntent);
                        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);*/
                        builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setAutoCancel(true)
                                .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Please open nethunter app and navigate to ChrootManager to setup your KaliChroot."))
                                .setContentTitle("KaliChroot is not up or installed")
                                .setContentText("Please navigate to ChrootManager to setup your KaliChroot.")
                                .setPriority(NotificationCompat.PRIORITY_MAX);
                                //.setContentIntent(resultPendingIntent);
                        notificationManagerCompat.notify(NOTIFY_ID, builder.build());
                        break;
                    case USENETHUNTER:
                        notificationManagerCompat.cancelAll();
                        builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setAutoCancel(true)
                                .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                                .setTimeoutAfter(10000)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Happy hunting!"))
                                .setContentTitle("KaliChroot is UP!")
                                .setContentText("Happy hunting!")
                                .setPriority(NotificationCompat.PRIORITY_MAX);
                        notificationManagerCompat.notify(NOTIFY_ID, builder.build());
                        break;
                    case DOWNLOADING:
                        notificationManagerCompat.cancelAll();
                        builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setAutoCancel(true)
                                .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                                .setTimeoutAfter(15000)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Please don't kill the app or the download will be cancelled!"))
                                .setContentTitle("Downloading Chroot!")
                                .setContentText("Please don't kill the app or the download will be cancelled!")
                                .setPriority(NotificationCompat.PRIORITY_MAX);
                        notificationManagerCompat.notify(NOTIFY_ID, builder.build());
                        break;
                    case INSTALLING:
                        notificationManagerCompat.cancelAll();
                        builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setAutoCancel(true)
                                .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                                .setTimeoutAfter(15000)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Please don't kill the app as it will still keep running on the background"))
                                .setContentTitle("Installing Chroot")
                                .setContentText("Please don't kill the app as it will still keep running on the background")
                                .setPriority(NotificationCompat.PRIORITY_MAX);
                        notificationManagerCompat.notify(NOTIFY_ID, builder.build());
                        break;
                    case BACKINGUP:
                        notificationManagerCompat.cancelAll();
                        builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setAutoCancel(true)
                                .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                                .setTimeoutAfter(15000)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Please don't kill the app as it will still keep running on the background"))
                                .setContentTitle("Creating KaliChroot backup to local storage.")
                                .setContentText("Please don't kill the app as it will still keep running on the background")
                                .setPriority(NotificationCompat.PRIORITY_MAX);
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
