package com.offsec.nethunter.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.offsec.nethunter.BuildConfig;

import java.io.File;


public class NhPaths implements SharedPreferences.OnSharedPreferenceChangeListener{
    private static final String TAG = "NhPaths";
    private static NhPaths instance;
    private SharedPreferences sharedPreferences;

    public static String APP_PATH;
    public static String APP_INITD_PATH;
    public static String APP_SCRIPTS_PATH;
    public static String NH_SD_FOLDER_NAME;
    public static String SD_PATH;
    public static String APP_SD_FILES_PATH;
    public static String BASE_PATH;
    public static String NH_SYSTEM_PATH;
    public static String ARCH_FOLDER;
    public static String CHROOT_PATH;
    //public static String CHROOT_DIR;
    public static String CHROOT_SD_PATH;
    public static String CHROOT_EXEC;
    public static String APP_SD_SQLBACKUP_PATH;
    public static String OLD_CHROOT_PATH;
    public static String BUSYBOX;

    private NhPaths(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        APP_PATH                        = context.getApplicationContext().getFilesDir().getPath();
        APP_INITD_PATH                  = APP_PATH + "/etc/init.d";
        APP_SCRIPTS_PATH                = APP_PATH + "/scripts";
        SD_PATH                         = getSdcardPath();
        NH_SD_FOLDER_NAME               = "nh_files";
        APP_SD_FILES_PATH               = SD_PATH + "/" + NH_SD_FOLDER_NAME;
        APP_SD_SQLBACKUP_PATH           = APP_SD_FILES_PATH + "/nh_sql_backups";
        BASE_PATH                       = "/data/local";
        NH_SYSTEM_PATH                  = BASE_PATH + "/nhsystem";
        ARCH_FOLDER                     = "/kali-arm64";
        CHROOT_PATH                     = NH_SYSTEM_PATH + ARCH_FOLDER;
        //CHROOT_DIR                      = sharedPreferences.getString("chroot_dir", NH_SYSTEM_PATH + "/kali-arm64");
        CHROOT_SD_PATH                  = "/sdcard";
        CHROOT_EXEC                     = "/usr/bin/sudo";
        OLD_CHROOT_PATH                 = "/data/local/kali-armhf";
        BUSYBOX                         = getBusyboxPath();
    }

    public synchronized static NhPaths getInstance(Context context) {
        if (instance == null) {
            instance = new NhPaths(context);
        }
        return instance;
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("chroot_path")){
            CHROOT_PATH = sharedPreferences.getString(key, NH_SYSTEM_PATH + "/kali-arm64");
        }
    }

    private static String getSdcardPath(){
        return Environment.getExternalStorageDirectory().toString();
    }


    private static String getBusyboxPath(){
        String[] BB_PATHS = {
                "/data/adb/magisk/busybox",
                "/sbin/.magisk/busybox/busybox",
                "/system/xbin/busybox_nh",
                "/sbin/busybox_nh",
                "/system/bin/busybox",
                "/data/local/bin/busybox",
                "/system/xbin/busybox",
        };
        for (String BB_PATH : BB_PATHS) {
            File busybox = new File(BB_PATH);
            if (busybox.exists()) {
                return BB_PATH;
            }
        }
        return null;
    }

    public static String makeTermTitle(String title) {
        return "echo -ne \"\\033]0;" + title + "\\007\" && clear;";
    }

    public void onDestroy(){
        if (sharedPreferences != null){
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    public static void showMessage(Context context, String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    public static void showMessage_long(Context context, String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}
