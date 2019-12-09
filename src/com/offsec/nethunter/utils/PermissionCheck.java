package com.offsec.nethunter.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class PermissionCheck {
    private static final String TAG = "PermissionCheck";
    private Activity activity;
    private Context context;

    public static final int DEFAULT_PERMISSION_RQCODE = 1;
    public static final int NH_TERM_PERMISSIONS_RQCODE = 2;
    public static final int NH_VNC_PERMISSIONS_RQCODE = 3;
    public static final int NH_VNC_PERMISSIONS_ONFRAGMENTCLICK_RQCODE = 4;

    public static final String[] DEFAULT_PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public static final String[] NH_TERM_PERMISSIONS = {
            "com.offsec.nhterm.permission.RUN_SCRIPT",
            "com.offsec.nhterm.permission.RUN_SCRIPT_SU",
            "com.offsec.nhterm.permission.RUN_SCRIPT_NH",
            "com.offsec.nhterm.permission.RUN_SCRIPT_NH_LOGIN"
    };

    public static final String[] NH_VNC_PERMISSIONS = {
            "com.offsec.nhvnc.permission.OPEN_VNC_CONN"
    };

    public PermissionCheck(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }

    //First check the permissions everytime the app is freshly run.
    public void checkPermissions(String[] PERMISSIONS, int REQUEST_CODE) {
        if (!hasPermissions(context, PERMISSIONS)) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS, REQUEST_CODE);
        }
    }

    private boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isAllPermitted(String[] PERMISSIONS){
        for (String permissions:PERMISSIONS){
            if (ActivityCompat.checkSelfPermission(context, permissions) != PackageManager.PERMISSION_GRANTED){
                Log.e(TAG, "Permissions are NOT all granted.");
                return false;
            }
        }
        Log.d(TAG, "All permissions are granted.");
        return true;
    }

}
