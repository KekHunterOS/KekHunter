package com.team420.kekhunter.gps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.team420.kekhunter.AppNavHomeActivity;
import com.team420.kekhunter.KaliGpsServiceFragment;
import com.team420.kekhunter.R;
import com.team420.kekhunter.utils.NhPaths;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.Date;

public class LocationUpdateService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static LocationUpdateService instance = null;

    private KaliGPSUpdates.Receiver updateReceiver;
    public static final String CHANNEL_ID = "NethunterLocationUpdateChannel";
    public static final int NOTIFY_ID = 1004;
    private static final String TAG = "LocationUpdateService";
    private GoogleApiClient apiClient = null;
    private DatagramSocket dSock = null;
    private InetAddress udpDestAddr = null;
    private static final String notificationTitle = "GPS Provider running";
    private static final String notificationText = "Sending GPS data to udp://127.0.0.1:" + NhPaths.GPS_PORT;
    private String lastLocationSourceReceived = "None";
    private String lastLocationSourcePublished = "None";
    private double lastLocationLatitude = 0.0;
    private double lastLocationLongitude = 0.0;
    private int lastLocationSats = 0;
    private double lastLocationAccuracy = 0.0;
    private Date lastLocationTime = new Date();
    private String lastNotificationText = null;
    private Handler timerTaskHandler = null;
    private Handler resetListenersTimerTaskHandler = null;

    private final IBinder binder = new ServiceBinder();

    // this allows us to check if there is already a LocationUpdateService running without actually attaching to it
    public static boolean isInstanceCreated() {
        return (instance != null);
    }

    // this gets called if we launch via an Intent or from the command line, instead of the app
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        requestUpdates(null); // request updates, but no widget to receive them as we don't have the app running
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        instance = this;
        initTimers();
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "NethunterPersistentChannelService",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    /**
     * Formats the number of satellites from the #Location into a
     * string.  In case #LocationManager.NETWORK_PROVIDER is used, it
     * returns the faked value "1", because some software refuses to
     * work with a "0" or an empty value.
     */
    public String formatSatellites(Location location) {
        int satellites = 0;
        Bundle bundle = location.getExtras();
        // this doesn't work on all phones
        if(bundle != null)
            satellites = bundle.getInt("satellites");
        if(satellites > 4)
            return "" + satellites;

        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            if(satellites == 0)
                return "";
            else
                return "" + satellites;
        }
        // We aren't using pure GPS.  Fake this variable
        return "4";
    }

    /**
     * Formats the altitude from the #Location into a string, with a
     * second unit field ("M" for meters).  If the altitude is
     * unknown, it returns two empty fields.
     */
    public String formatAltitude(Location location) {
        String s = "";
        if (location.hasAltitude())
            s += String.format("%.4f,M", location.getAltitude());
        else
            s += ",";
        return s;
    }

    /**
     * Calculates the NMEA checksum of the specified string.  Pass the
     * portion of the line between '$' and '*' here.
     */
    private String checksum(String s) {
        int checksum = 0;

        for (int i = 0; i < s.length(); i++)
            checksum = checksum ^ s.charAt(i);

        String hex = Integer.toHexString(checksum);
        if (hex.length() == 1)
            hex = "0" + hex;
        return ("*" + hex.toUpperCase());
    }

    /**
     * Formats the time from the #Location into a string.
     */
    @SuppressLint("DefaultLocale")
    public static String formatTime(Location location) {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("HHmmss");
        return dtf.print(new DateTime(location.getTime()));
    }

    /**
     * Formats the surface position (latitude and longitude) from the
     * #Location into a string.
     */
    public static String formatPosition(Location location) {
        double latitude = location.getLatitude();
        char nsSuffix = latitude < 0 ? 'S' : 'N';
        latitude = Math.abs(latitude);

        double longitude = location.getLongitude();
        char ewSuffix = longitude < 0 ? 'W' : 'E';
        longitude = Math.abs(longitude);
        @SuppressLint("DefaultLocale") String lat = String.format("%02d%02d.%04d,%c",
                (int) latitude,
                (int) (latitude * 60) % 60,
                (int) (latitude * 60 * 10000) % 10000,
                nsSuffix);
        @SuppressLint("DefaultLocale") String lon = String.format("%03d%02d.%04d,%c",
                (int) longitude,
                (int) (longitude * 60) % 60,
                (int) (longitude * 60 * 10000) % 10000,
                ewSuffix);
        return lat + "," + lon;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG,"onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Google API Client connection failed");
    }


    public class ServiceBinder extends Binder {
        public LocationUpdateService getService() {
            return LocationUpdateService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        this.startService(intent);
        return binder;
    }

    public class MyConnectionCallback implements GoogleApiClient.ConnectionCallbacks
    {

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Log.d(TAG,"onConnected (MyConnectionCallback)");
            startLocationUpdates();
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    }
    public void requestUpdates(KaliGPSUpdates.Receiver receiver) {
        Log.d(TAG, "In requestUpdates");
        if(receiver != null)
            this.updateReceiver = receiver;
        if (apiClient == null) {
            apiClient = new GoogleApiClient.Builder(LocationUpdateService.this, this, this)
                    .addApi(LocationServices.API)
                    .build();
        }
        if (!apiClient.isConnected()) {
            apiClient.registerConnectionCallbacks(new MyConnectionCallback());
            apiClient.connect();
        }
    }

    public void stopUpdates() {
        Log.d(TAG, "In stopUpdates");

        stopSelf();
    }

    private boolean locationUpdatesStarted = false;
    public void startLocationUpdates() {
        if(locationUpdatesStarted)
            return;
        locationUpdatesStarted = true;
        Log.d(TAG, "In startLocationUpdates");

        final LocationRequest lr = LocationRequest.create()
                .setExpirationDuration(1000 * 3600 * 2) /*2 hrs*/
                .setFastestInterval(100L)
                .setInterval(1000L / 2L) /*2 hz updates*/
                .setMaxWaitTime(600L)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        Log.d(TAG, "Requesting permissions marshmallow");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // register with Location services, so we can construct fake NMEA data
            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, lr, locationListener);

            // try to register for actual NMEA data straight from the GPS
            LocationManager locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
            try { // reference: https://stackoverflow.com/questions/57975969/accessing-nmea-on-android-api-level-24-when-compiled-for-target-api-level-29
                //noinspection JavaReflectionMemberAccess
                Method addNmeaListener =
                        LocationManager.class.getMethod("addNmeaListener", GpsStatus.NmeaListener.class);
                addNmeaListener.invoke(locationManager, nmeaListener);
                Log.d(TAG, "addNmeaListener success");
            } catch (Exception exception) {
                // oh well
            }
        }

        // turn on a Persistent Notification so we can continue to get location updates even when backgrounded
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        // TODO have this result intent open the NH app
        Intent resultIntent = new Intent(this, AppNavHomeActivity.class);
        resultIntent.putExtra("menuFragment", R.id.gps_item);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setAutoCancel(false)
                .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                .setContentText(notificationText)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setContentTitle(notificationTitle)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(resultPendingIntent);
        Notification notification = builder.build();
        notificationManagerCompat.notify(NOTIFY_ID, notification);

        this.startForeground(NOTIFY_ID, notification);
        // start a timer that will update our Notification every second
        Log.d(TAG, "starting Notification Update Timer");
        startTimers();
    }

    private void initTimers() {

        timerTaskHandler = new Handler();
        resetListenersTimerTaskHandler = new Handler();

    }

    private void startTimers() {
        // cancel any previous
        stopTimers();
        timerTask.run();
        // Android will stop sending us updates two hours after the request.
        // So, every hour, we will make a new request
        resetListenersTimerTaskHandler.postDelayed(resetListenersTimerTask, 3600*1000);
        // resetListenersTimerTask.run();
    }

    private void stopTimers() {
        timerTaskHandler.removeCallbacks(timerTask);
        resetListenersTimerTaskHandler.removeCallbacks(resetListenersTimerTask);
    }

    private Runnable resetListenersTimerTask = new Runnable() {
        @Override
        public void run() {
            try { // reset our listeners
                Log.d(TAG, "Restarting listeners");
                stopLocationUpdates();
                requestUpdates(null);
            } finally {
            }
        }
    };

    private Runnable timerTask = new Runnable() {
        @Override
        public void run() {
            try {
                if (lastLocationLatitude != 0.0)
                    updateNotification();
            } finally {
                // once per second
                timerTaskHandler.postDelayed(timerTask, 1000);
            }
        }
    };

    private void updateNotification() {
        Date now = new Date();
        long age = (now.getTime() - lastLocationTime.getTime()) / 1000;
        String ageStr = "?";
        if(age <= 10)
            ageStr = "current";
        else if(age < 60)
            ageStr = "" + age + "s";
        else if(age < 3600)
            ageStr = "" + (age/60) + "m";
        else
            ageStr = "" + (age/3600) + "h";
        String updatedText = String.format("Latitude: %1.5f  Longitude: %1.5f  +/- %1.1fm  Source: %s  Age: %s  Satellites: %d",
                lastLocationLatitude, lastLocationLongitude, lastLocationAccuracy,
                lastLocationSourcePublished, ageStr, lastLocationSats);

        // Log.d(TAG, "Notification Update: " + updatedText);
        // we're not actually going to set this text, but we are going to use it to see if anything has changed since last update
        // if nothing has changed, we won't update the notification
        if(updatedText.equals(lastNotificationText))
            return;
        lastNotificationText = updatedText;

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        // TODO have this result intent open the NH app
        Intent resultIntent = new Intent(this, AppNavHomeActivity.class);
        resultIntent.putExtra("menuFragment", R.id.gps_item);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.gps_notification);
        contentView.setTextViewText(R.id.gps_notification_latitude, String.format("%1.5f", lastLocationLatitude));
        contentView.setTextViewText(R.id.gps_notification_longitude, String.format("%1.5f", lastLocationLongitude));
        contentView.setTextViewText(R.id.gps_notification_accuracy, String.format("%1.1fm", lastLocationAccuracy));

        contentView.setTextViewText(R.id.gps_notification_source, lastLocationSourcePublished);
        contentView.setTextViewText(R.id.gps_notification_age, ageStr);
        if(lastLocationSourcePublished.equals("GPS"))
            contentView.setTextViewText(R.id.gps_notification_sats, String.format("%d", lastLocationSats));
        else
            contentView.setTextViewText(R.id.gps_notification_sats, "-");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setAutoCancel(false)
                .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                .setContent(contentView)
                // .setContentText("contentText")
                // .setStyle(new NotificationCompat.BigTextStyle().bigText(updatedText))
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(contentView)
                .setContentTitle(notificationTitle)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(resultPendingIntent);
        Notification notification = builder.build();
        notificationManagerCompat.notify(NOTIFY_ID, notification);
        Log.d(TAG, "Notification Sent: " + updatedText);
    }

    private final GpsStatus.NmeaListener nmeaListener = new GpsStatus.NmeaListener() {
        @Override
        public void onNmeaReceived(long l, String s) {
            if(!s.startsWith("$GPGGA")) {
                // if we're using the real GPS as our source, go ahead and send these extra information strings to gpsd
                if("GPS".equals(lastLocationSourcePublished))
                    sendUdpPacket(s);
                return;
            }
            String[] fields = s.split(",");
            int fixType = 0;
            // int sats = 0;
            try {
                fixType = Integer.parseInt(fields[6]);
                // sats = Integer.parseInt(fields[7]);
            } catch (NumberFormatException ignored) {
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
            if(fixType == 0)
                return;
            // Log.d(TAG, "sats = " + sats);
            Log.d(TAG, "Real NMEA: " + s);
            lastLocationSourceReceived = "NmeaListener";
            publishLocation(s, "GPS");
        }
    };

    private boolean firstupdate = true;
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            String nmeaSentence = nmeaSentenceFromLocation(location);

            Log.d(TAG, "Constructed NMEA: "+nmeaSentence);
            // we will only publish these constructed sentences if we aren't currently getting real ones from the NmeaListener
            if(lastLocationSourceReceived.equals("LocationListener"))
                publishLocation(nmeaSentence, "Network");
            lastLocationSourceReceived = "LocationListener";
        }
    };

    private void publishLocation(String nmeaSentence, String source) {
        // Workaround to allow network operations in main thread
        if (android.os.Build.VERSION.SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        try {
            String[] fields = nmeaSentence.split(",");
            String latStr = fields[2];
            String ns = fields[3];
            String lonStr = fields[4];
            String ew = fields[5];
            int sats = Integer.parseInt(fields[7]);
            double accuracy = Float.parseFloat(fields[8]) * 19.0; // why 19.0?  see https://gitlab.com/gpsd/gpsd/-/blob/master/libgpsd_core.c, P_UERE_NO_DGPS

            int latDeg = Integer.parseInt(latStr.substring(0, 2));
            double latMin = Float.parseFloat(latStr.substring(2));
            double lat = latDeg + latMin/60.0;
            int lonDeg = Integer.parseInt(lonStr.substring(0, 3));
            double lonMin = Float.parseFloat(lonStr.substring(3));
            double lon = lonDeg + lonMin/60.0;
            if(ns.toUpperCase().equals("S"))
                lat *= -1;
            if(ew.toUpperCase().equals("W"))
                lon *= -1;

            lastLocationLatitude = lat;
            lastLocationLongitude = lon;
            lastLocationSats = sats;
            lastLocationAccuracy = accuracy;
            lastLocationTime = new Date();
            lastLocationSourcePublished = source;
        } catch (NumberFormatException ignored) {
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }

        if (updateReceiver != null) {
            if (firstupdate) {
                firstupdate = false;
                updateReceiver.onFirstPositionUpdate();
            }
            updateReceiver.onPositionUpdate(nmeaSentence);
        }

        sendUdpPacket(nmeaSentence);
    }


    private void sendUdpPacket(String nmeaSentence) {
        if(udpDestAddr == null)
        {
            try {
                udpDestAddr = Inet4Address.getByName("127.0.0.1");
            } catch (UnknownHostException e) {
                Log.d(TAG, "UnknownHostException: " + e.toString());
            }
        }

        if(dSock == null) {
            try {
                // SocketAddress destAddr = new InetSocketAddress("127.0.0.1", NhPaths.GPS_PORT);
                dSock = new DatagramSocket();
            } catch (final java.net.SocketException e) {
                Log.d(TAG, "SocketException: " + e.toString());
                dSock = null;
            }
        }

        if(dSock != null && udpDestAddr != null) {
            try {
                // dSock.setBroadcast(true);
                nmeaSentence += "\n";
                byte[] buf = nmeaSentence.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, udpDestAddr, NhPaths.GPS_PORT);
                dSock.send(packet);
                // dSock.close();
            } catch (final IOException e) {
                Log.d(TAG, "IOException: " + e.toString());
                dSock = null;
                udpDestAddr = null;
            }
        }
    }

    private String nmeaSentenceFromLocation(Location location) {

//       from: https://github.com/ya-isakov/blue-nmea-mirror/blob/master/src/Source.java
        String time = formatTime(location);
        String position = formatPosition(location);
        String accuracy = String.format("%.4f", location.getAccuracy()/19.0); // why 19.0?  see https://gitlab.com/gpsd/gpsd/-/blob/master/libgpsd_core.c, P_UERE_NO_DGPS
        String innerSentence = String.format("GPGGA,%s,%s,1,%s,%s,%s,,,,", time, position, formatSatellites(location),
                                             accuracy, formatAltitude(location));

//        Adds checksum and initial $
        String checksum = checksum(innerSentence);
        return "$" + innerSentence + checksum;
    }

    private void stopLocationUpdates() {
        locationUpdatesStarted = false;

        // unregister our NmeaListener
        LocationManager locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        try { // reference: https://stackoverflow.com/questions/57975969/accessing-nmea-on-android-api-level-24-when-compiled-for-target-api-level-29
            //noinspection JavaReflectionMemberAccess
            Method removeNmeaListener =
                    LocationManager.class.getMethod("removeNmeaListener", GpsStatus.NmeaListener.class);
            removeNmeaListener.invoke(locationManager, nmeaListener);
            Log.d(TAG, "removeNmeaListener success");
        } catch (Exception exception) {
            // oh well
        }

        // unregister with LocationServices
        if (apiClient != null && apiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, locationListener);
            apiClient.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "OnDestroy");
        instance = null;
        firstupdate = true;

        // stop our Notification update timer
        stopTimers();

        stopLocationUpdates();

        super.onDestroy();
    }
}

