package com.offsec.nethunter.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.offsec.nethunter.R;

import androidx.appcompat.app.AlertDialog;

import java.io.File;


public abstract class CheckForRoot {

    public static String BUSYBOX;

    public static boolean isRoot() {
        ShellExecuter exe = new ShellExecuter();
        return !exe.Executer("su -c 'id'").isEmpty();
    }

    public static boolean isBusyboxInstalled() {
        return !NhPaths.BUSYBOX.isEmpty();
    }
}

