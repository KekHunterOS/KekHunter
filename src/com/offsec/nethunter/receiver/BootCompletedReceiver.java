package com.offsec.nethunter.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.offsec.nethunter.service.RunAtBootService;

public class BootCompletedReceiver extends BroadcastReceiver{

    private static final String TAG = "BootCompletedReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction()!=null){
            Log.d(TAG, "Actions: " + intent.getAction());
            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                ComponentName comp = new ComponentName(context.getPackageName(),
                        RunAtBootService.class.getName());
                RunAtBootService.enqueueWork(context, (intent.setComponent(comp)));
                //Intent serviceIntent = new Intent(context, RunAtBootService.class);
                //context.startService(serviceIntent);
                Log.d(TAG, "Nethunter receive boot_completed intent!!");
            }
        }

    }
}
