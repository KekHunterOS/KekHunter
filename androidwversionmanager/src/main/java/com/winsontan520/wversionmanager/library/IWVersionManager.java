package com.winsontan520.wversionmanager.library;

import android.Manifest;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;

public interface IWVersionManager {
    /**
     * Displays a dialog asking user to rate your app
     */
    public void askForRate();

    /**
     * Initiates check for updates that may result in displaying a dialog with release notes
     * and options to update now, remind user about it later or ignore that version
     */
    public void checkVersion();

    /**
     * Initiates check for updates that may result in displaying a dialog with release notes
     * and options to update now, remind user about it later or ignore that version
     * @param forced <code>true</code> if you want to ignore reminder timer,
     *               <code>false</code> otherwise
     */
    public void checkVersion(boolean forced);

    /**
     * @param icon Drawable of icon in dialog
     */
    public void setIcon(@Nullable Drawable icon);

    /**
     * @param title Title of dialog
     */
    public void setTitle(@Nullable String title);

    /**
     * @param message Message of dialog
     */
    public void setMessage(@Nullable String message);

    /**
     * Used to choose if Android's Download Manager should be used to download file at URL
     * from JSON file to Downloads folder (off by default) instead of opening a link in a browser
     * @param value <code>true</code> to use Download Manager, <code>false</code> otherwise
     */
    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void useDownloadManager(boolean value);

    /**
     * Setting this to <code>true</code> when {@link #useDownloadManager(boolean)}
     * is also <code>true</code> will automatically fire APK installation dialog after download completes.
     * This requires Unknown Sources to be enabled (globally on Nougat and lower,
     * for your app specifically in Oreo or higher). If permission is not granted,
     * system's Download app will be launched.
     * @param value <code>true</code> to enable auto install, <code>false</code> otherwise
     */
    @RequiresPermission(anyOf = {Manifest.permission.REQUEST_INSTALL_PACKAGES, Manifest.permission.INSTALL_PACKAGES})
    public void installAfterDownload(boolean value);

    /**
     * @return message of dialog
     */
    public @NonNull String getMessage();

    /**
     * @return title of dialog
     */
    public @NonNull String getTitle();

    /**
     * @return drawable of icon
     */
    public @NonNull Drawable getIcon();

    /**
     * @return url to execute when update now button clicked. Default value is the link in google play based on app package name.
     */
    public @NonNull String getUpdateUrl();

    /**
     * @param updateUrl Set url to execute when update now button clicked
     */
    public void setUpdateUrl(@Nullable String updateUrl);

    /**
     * @return url which should return update content in json format
     */
    public @Nullable String getVersionContentUrl();

    /**
     * @param versionContentUrl Set the update content url
     */
    public void setVersionContentUrl(@NonNull String versionContentUrl);

    /**
     * @param minutes Set reminder time in minutes when remind me later button clicked
     */
    public void setReminderTimer(int minutes);

    /**
     * @return reminder timer in minutes
     */
    public int getReminderTimer();

    /**
     * @return current version code
     */
    public int getCurrentVersionCode();

    /**
     * @return version code which will be ignored
     */
    public int getIgnoreVersionCode();

    /**
     * @return whenever last received update data was telling that its blocking (app won't
     * function properly without it for long, for example due to server side changes).
     */
    public boolean isBlockingUpdate();

    /**
     * @return value telling if update dialog will be cancelable or not
     */
    public boolean isDialogCancelable();

    /**
     * @param dialogCancelable sets if update dialog should be cancelable or not
     */
    public void setDialogCancelable(boolean dialogCancelable);

    /**
     * @return CustomTagHandler object
     */
    public @Nullable CustomTagHandler getCustomTagHandler();

    /**
     * @param customTagHandler Set your own custom tag handler
     */
    public void setCustomTagHandler(@Nullable CustomTagHandler customTagHandler);

    /**
     * @param listener Set your own callback listener when receiving response from server.
     *                 Must implement {@link OnReceiveListener} interface.
     */
    public void setOnReceiveListener(@Nullable OnReceiveListener listener);
}