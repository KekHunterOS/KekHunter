package com.offsec.nethunter.utils;

public class CheckForRoot {

    public static boolean isRoot() {
        ShellExecuter exe = new ShellExecuter();
        return !exe.Executer("su -c 'id'").isEmpty();
    }

    public static boolean isBusyboxInstalled() {
        return !NhPaths.BUSYBOX.isEmpty();
    }
}

