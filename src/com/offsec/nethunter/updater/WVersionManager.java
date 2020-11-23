package com.offsec.nethunter.updater;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import android.content.Intent;
import android.os.Build;

import androidx.annotation.Nullable;

import com.offsec.nethunter.BuildConfig;
import com.offsec.nethunter.service.NotificationChannelService;

import android.content.ComponentName;
import android.content.ContentResolver;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.util.Log;

import android.app.DownloadManager;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.view.Display;
import android.widget.Toast;

import androidx.core.content.FileProvider;


public class WVersionManager extends Context implements IWVersionManager {
    private static final String TAG = WVersionManager.class.getSimpleName();

    private static final int MODE_CHECK_VERSION = 100;
    private static final int MODE_ASK_FOR_RATE = 200;

    public static final String CHANNEL_ID = "Updater";
    private static final String PREF_IGNORE_VERSION_CODE = "w.ignore.version.code";
    private static final String PREF_REMINDER_TIME = "w.reminder.time";
    private static final String PREF_IS_BLOCKING = "w.is_blocking_update";
    private static final String APK_MIME_TYPE = "application/vnd.android.package-archive";
    private static final String PLAY_STORE_URI = "market://details?id=";

    private Activity mActivity;
    private Drawable mIcon;
    private String mTitle;
    private String mMessage;
    private String mUpdateUrl;
    private String mVersionContentUrl;
    private boolean mDialogCancelable = true;
    private boolean mUseDownloadManager = false;
    private boolean mAutoInstall = false;
    private boolean mIsBlocking = false;
    private int mReminderTimer;
    private int mVersionCode;
    private int mMode = MODE_CHECK_VERSION; // Default mode
    private AlertDialogButtonListener mDialogListener;
    private OnReceiveListener mOnReceiveListener;
    private CustomTagHandler mCustomTagHandler;

    public WVersionManager(Activity act) {
        this.mActivity = act;
        this.mDialogListener = new AlertDialogButtonListener();
        this.mCustomTagHandler = new CustomTagHandler();
        this.mIsBlocking = PreferenceManager.getDefaultSharedPreferences(mActivity)
                .getBoolean(PREF_IS_BLOCKING, false);
        if (mIsBlocking)
            mDialogCancelable = false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.winsontan520.wversionmanager.IWVersionManager#checkVersion()
     */
    @Override
    public void checkVersion() {
        checkVersion(false);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.winsontan520.wversionmanager.IWVersionManager#checkVersion(boolean)
     */
    @Override
    public void checkVersion(boolean forced) {
        mMode = MODE_CHECK_VERSION;
        String versionContentUrl = getVersionContentUrl();
        Calendar c = Calendar.getInstance();
            VersionContentLoader loader = new VersionContentLoader();
            Bundle bundle = new Bundle();
            bundle.putString(VersionContentLoader.KEY_URL, getVersionContentUrl());
            mActivity.getLoaderManager().initLoader(934213, bundle, loader);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.winsontan520.wversionmanager.IWVersionManager#askForRate()
     */
    @Override
    public void askForRate() {
        mMode = MODE_ASK_FOR_RATE;
        showDialog();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.winsontan520.wversionmanager.IWVersionManager#setIcon(android.graphics.drawable.Drawable)
     */
    @Override
    public void setIcon(Drawable icon) {
        this.mIcon = icon;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.winsontan520.wversionmanager.IWVersionManager#setMessage(java.lang.String)
     */
    @Override
    public void setMessage(String message) {
        this.mMessage = message;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.winsontan520.wversionmanager.IWVersionManager#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.winsontan520.wversionmanager.IWVersionManager#useDownloadManager(boolean)
     */
    @Override
    public void useDownloadManager(boolean value) {
        mUseDownloadManager = value;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.winsontan520.wversionmanager.IWVersionManager#installAfterDownload(boolean)
     */
    @Override
    public void installAfterDownload(boolean value) {
        mAutoInstall = value;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.winsontan520.wversionmanager.IWVersionManager#getMessage()
     */
    @Override
    public @NonNull String getMessage() {
        String defaultMessage = null;
        switch (mMode) {
            case MODE_CHECK_VERSION:
                defaultMessage = ">What\\'s new in this version:";
                break;
            case MODE_ASK_FOR_RATE:
                defaultMessage = "Please rate us!";
                break;
        }

        return mMessage != null ? mMessage : defaultMessage;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.winsontan520.wversionmanager.IWVersionManager#getTitle()
     */
    @Override
    public @NonNull String getTitle() {
        String defaultTitle = null;
        switch (mMode) {
            case MODE_CHECK_VERSION:
                defaultTitle = "New Update Available!";
                break;
            case MODE_ASK_FOR_RATE:
                defaultTitle = "Rate this app";
                break;
            default:
                defaultTitle = "";
        }
        return mTitle != null ? mTitle : defaultTitle;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.winsontan520.wversionmanager.IWVersionManager#getIcon()
     */
    @Override
    public @NonNull Drawable getIcon() {
        return mIcon != null ? mIcon : getDefaultAppIcon();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.winsontan520.wversionmanager.IWVersionManager#getUpdateUrl()
     */
    @Override
    public @NonNull String getUpdateUrl() {
        return mUpdateUrl != null ? mUpdateUrl : getGooglePlayStoreUrl();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.winsontan520.wversionmanager.IWVersionManager#setUpdateUrl(java.lang.String)
     */
    @Override
    public void setUpdateUrl(String updateUrl) {
        this.mUpdateUrl = updateUrl;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.winsontan520.wversionmanager.IWVersionManager#isBlockingUpdate()
     */
    @Override
    public boolean isBlockingUpdate() {
        return mIsBlocking;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.winsontan520.wversionmanager.IWVersionManager#getVersionContentUrl()
     */
    @Override
    public String getVersionContentUrl() {
        return mVersionContentUrl;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.winsontan520.wversionmanager.IWVersionManager#setVersionContentUrl(java.lang.String)
     */
    @Override
    public void setVersionContentUrl(@NonNull String versionContentUrl) {
        this.mVersionContentUrl = versionContentUrl;
    }

    @Override
    public void setReminderTimer(int minutes) {

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.winsontan520.wversionmanager.IWVersionManager#setVersionContentUrl(java.lang.String)
     */
    @Override
    public int getReminderTimer() {
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.winsontan520.wversionmanager.IWVersionManager#setReminderTimer(int)
     */

    /*
     * (non-Javadoc)
     *
     * @see
     * com.winsontan520.wversionmanager.IWVersionManager#getCurrentVersionCode()
     */
    @Override
    public int getCurrentVersionCode() {
        int currentVersionCode = 0;
        PackageInfo pInfo;
        try {
            pInfo = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0);
            currentVersionCode = pInfo.versionCode;
        } catch (NameNotFoundException e) {
            // return 0
        }
        return currentVersionCode;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.winsontan520.wversionmanager.IWVersionManager#getIgnoreVersionCode()
     */
    @Override
    public int getIgnoreVersionCode() {
        return PreferenceManager.getDefaultSharedPreferences(mActivity).getInt(PREF_IGNORE_VERSION_CODE, 1);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.winsontan520.wversionmanager.IWVersionManager#getCustomTagHandler()
     */
    @Override
    public CustomTagHandler getCustomTagHandler() {
        return mCustomTagHandler;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.winsontan520.wversionmanager.IWVersionManager#setCustomTagHandler
     * (com.winsontan520.wversionmanager.WVersionManager.CustomTagHandler)
     */
    @Override
    public void setCustomTagHandler(CustomTagHandler customTagHandler) {
        this.mCustomTagHandler = customTagHandler;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.winsontan520.wversionmanager.IWVersionManager#isDialogCancelable
     * (com.winsontan520.wversionmanager.OnReceiveListener)
     */
    @Override
    public boolean isDialogCancelable() {
        return mDialogCancelable;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.winsontan520.wversionmanager.IWVersionManager#setDialogCancelable
     * (com.winsontan520.wversionmanager.OnReceiveListener)
     */
    @Override
    public void setDialogCancelable(boolean dialogCancelable) {
        mDialogCancelable = dialogCancelable;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.winsontan520.wversionmanager.IWVersionManager#setOnReceiveListener
     * (com.winsontan520.wversionmanager.OnReceiveListener)
     */
    @Override
    public void setOnReceiveListener(OnReceiveListener listener) {
        this.mOnReceiveListener = listener;
    }

    private void showDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

        builder.setIcon(getIcon());
        builder.setTitle(getTitle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setMessage(Html.fromHtml(getMessage(), Html.FROM_HTML_MODE_LEGACY, null, getCustomTagHandler()));
        } else {
            builder.setMessage(Html.fromHtml(getMessage(),null, getCustomTagHandler()));
        }

        switch (mMode) {
            case MODE_CHECK_VERSION:
                builder.setPositiveButton("Update", mDialogListener);
                builder.setNeutralButton("Later", mDialogListener);
                //builder.setNegativeButton(R.string.wvm_button_close, mDialogListener);
                break;
            case MODE_ASK_FOR_RATE:
                builder.setPositiveButton("Ok", mDialogListener);
                builder.setNegativeButton("Not now", mDialogListener);
                break;
            default:
                return;
        }

        builder.setCancelable(isDialogCancelable());

        AlertDialog dialog = builder.create();
        if (mActivity != null && !mActivity.isFinishing()) {
            dialog.show();
        }
    }

    private void updateNow(String url) {
        if (url != null && (!mUseDownloadManager || url.startsWith(PLAY_STORE_URI))) {
            try {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                mActivity.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "is update url correct?" + e);
            }
        } else if (url != null) {
            Uri downloadUri = Uri.parse(url);

            String title = downloadUri.getLastPathSegment();
            final String filePath = Environment.getExternalStorageDirectory() + "/" +
                    Environment.DIRECTORY_DOWNLOADS + "/" + downloadUri.getLastPathSegment();
            final Uri fileUri = Uri.parse("file://" + filePath);

            // Build Download Manager's request
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setTitle(title);
            request.setMimeType(APK_MIME_TYPE);
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationUri(fileUri);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

            // Start downloading

            final DownloadManager manager = (DownloadManager) mActivity.getSystemService(Context.DOWNLOAD_SERVICE);
            manager.enqueue(request);

            // Handle initiating downloaded APK install
            Toast.makeText(mActivity, "Download started …", Toast.LENGTH_SHORT).show();
            boolean apkPermission = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                apkPermission = mActivity.getPackageManager().canRequestPackageInstalls();
            } else {
                try {
                    apkPermission = Settings.Secure.getInt(
                            mActivity.getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) == 1;
                } catch (Settings.SettingNotFoundException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
            if (mAutoInstall && apkPermission) {
                BroadcastReceiver onComplete = new BroadcastReceiver() {
                    public void onReceive(Context ctx, Intent intent) {
                        Intent install = new Intent(Intent.ACTION_VIEW);
                        install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            install.setDataAndType(FileProvider.getUriForFile(mActivity,
                                    mActivity.getApplicationContext().getPackageName() + ".UpdateFileProvider",
                                    new File(filePath)), APK_MIME_TYPE);
                            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } else {
                            install.setDataAndType(fileUri,APK_MIME_TYPE);
                        }
                        Toast.makeText(mActivity, "Download completed, installing …", Toast.LENGTH_SHORT).show();
                        mActivity.startActivity(install);
                        mActivity.unregisterReceiver(this);
                        mActivity.finish();
                    }
                };
                mActivity.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            } else if (mAutoInstall && !apkPermission) {
                mActivity.startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
            }
        }
    }

    @SuppressLint("ApplySharedPref")
    private void ignoreThisVersion() {
        PreferenceManager.getDefaultSharedPreferences(mActivity).edit()
                .putInt(PREF_IGNORE_VERSION_CODE, mVersionCode).commit();
    }

    private void remindMeLater(int reminderTimer) {
        Calendar c = Calendar.getInstance();
        long currentTimeStamp = c.getTimeInMillis();

        c.add(Calendar.MINUTE, reminderTimer);
        long reminderTimeStamp = c.getTimeInMillis();

        if (BuildConfig.DEBUG) {
            Log.v(TAG, "currentTimeStamp=" + currentTimeStamp);
            Log.v(TAG, "reminderTimeStamp=" + reminderTimeStamp);
        }
        setReminderTime(reminderTimeStamp);
    }

    @SuppressLint("ApplySharedPref")
    private void setReminderTime(long reminderTimeStamp) {
        PreferenceManager.getDefaultSharedPreferences(mActivity).edit()
                .putLong(PREF_REMINDER_TIME + mMode, reminderTimeStamp).commit();
    }

    private long getReminderTime() {
        return PreferenceManager.getDefaultSharedPreferences(mActivity).getLong(
                PREF_REMINDER_TIME + mMode, 0);
    }

    private String getGooglePlayStoreUrl() {
        // currently google play is using package name as id
        return PLAY_STORE_URI + mActivity.getApplicationInfo().packageName;
    }

    private Drawable getDefaultAppIcon() {
        return mActivity.getApplicationInfo().loadIcon(mActivity.getPackageManager());
    }

    @Override
    public AssetManager getAssets() {
        return null;
    }

    @Override
    public Resources getResources() {
        return null;
    }

    @Override
    public PackageManager getPackageManager() {
        return null;
    }

    @Override
    public ContentResolver getContentResolver() {
        return null;
    }

    @Override
    public Looper getMainLooper() {
        return null;
    }

    @Override
    public Context getApplicationContext() {
        return null;
    }

    @Override
    public void setTheme(int resid) {

    }

    @Override
    public Resources.Theme getTheme() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public String getPackageName() {
        return null;
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return null;
    }

    @Override
    public String getPackageResourcePath() {
        return null;
    }

    @Override
    public String getPackageCodePath() {
        return null;
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return null;
    }

    @Override
    public boolean moveSharedPreferencesFrom(Context sourceContext, String name) {
        return false;
    }

    @Override
    public boolean deleteSharedPreferences(String name) {
        return false;
    }

    @Override
    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        return null;
    }

    @Override
    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        return null;
    }

    @Override
    public boolean deleteFile(String name) {
        return false;
    }

    @Override
    public File getFileStreamPath(String name) {
        return null;
    }

    @Override
    public File getDataDir() {
        return null;
    }

    @Override
    public File getFilesDir() {
        return null;
    }

    @Override
    public File getNoBackupFilesDir() {
        return null;
    }

    @Nullable
    @Override
    public File getExternalFilesDir(@Nullable String type) {
        return null;
    }

    @Override
    public File[] getExternalFilesDirs(String type) {
        return new File[0];
    }

    @Override
    public File getObbDir() {
        return null;
    }

    @Override
    public File[] getObbDirs() {
        return new File[0];
    }

    @Override
    public File getCacheDir() {
        return null;
    }

    @Override
    public File getCodeCacheDir() {
        return null;
    }

    @Nullable
    @Override
    public File getExternalCacheDir() {
        return null;
    }

    @Override
    public File[] getExternalCacheDirs() {
        return new File[0];
    }

    @Override
    public File[] getExternalMediaDirs() {
        return new File[0];
    }

    @Override
    public String[] fileList() {
        return new String[0];
    }

    @Override
    public File getDir(String name, int mode) {
        return null;
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        return null;
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, @Nullable DatabaseErrorHandler errorHandler) {
        return null;
    }

    @Override
    public boolean moveDatabaseFrom(Context sourceContext, String name) {
        return false;
    }

    @Override
    public boolean deleteDatabase(String name) {
        return false;
    }

    @Override
    public File getDatabasePath(String name) {
        return null;
    }

    @Override
    public String[] databaseList() {
        return new String[0];
    }

    @Override
    public Drawable getWallpaper() {
        return null;
    }

    @Override
    public Drawable peekWallpaper() {
        return null;
    }

    @Override
    public int getWallpaperDesiredMinimumWidth() {
        return 0;
    }

    @Override
    public int getWallpaperDesiredMinimumHeight() {
        return 0;
    }

    @Override
    public void setWallpaper(Bitmap bitmap) throws IOException {

    }

    @Override
    public void setWallpaper(InputStream data) throws IOException {

    }

    @Override
    public void clearWallpaper() throws IOException {

    }

    @Override
    public void startActivity(Intent intent) {

    }

    @Override
    public void startActivity(Intent intent, @Nullable Bundle options) {

    }

    @Override
    public void startActivities(Intent[] intents) {

    }

    @Override
    public void startActivities(Intent[] intents, Bundle options) {

    }

    @Override
    public void startIntentSender(IntentSender intent, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {

    }

    @Override
    public void startIntentSender(IntentSender intent, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, @Nullable Bundle options) throws IntentSender.SendIntentException {

    }

    @Override
    public void sendBroadcast(Intent intent) {

    }

    @Override
    public void sendBroadcast(Intent intent, @Nullable String receiverPermission) {

    }

    @Override
    public void sendOrderedBroadcast(Intent intent, @Nullable String receiverPermission) {

    }

    @Override
    public void sendOrderedBroadcast(@NonNull Intent intent, @Nullable String receiverPermission, @Nullable BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

    }

    @Override
    public void sendBroadcastAsUser(Intent intent, UserHandle user) {

    }

    @Override
    public void sendBroadcastAsUser(Intent intent, UserHandle user, @Nullable String receiverPermission) {

    }

    @Override
    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, @Nullable String receiverPermission, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

    }

    @Override
    public void sendStickyBroadcast(Intent intent) {

    }

    @Override
    public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

    }

    @Override
    public void removeStickyBroadcast(Intent intent) {

    }

    @Override
    public void sendStickyBroadcastAsUser(Intent intent, UserHandle user) {

    }

    @Override
    public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle user, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

    }

    @Override
    public void removeStickyBroadcastAsUser(Intent intent, UserHandle user) {

    }

    @Nullable
    @Override
    public Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter) {
        return null;
    }

    @Nullable
    @Override
    public Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter, int flags) {
        return null;
    }

    @Nullable
    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, @Nullable String broadcastPermission, @Nullable Handler scheduler) {
        return null;
    }

    @Nullable
    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, @Nullable String broadcastPermission, @Nullable Handler scheduler, int flags) {
        return null;
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {

    }

    @Nullable
    @Override
    public ComponentName startService(Intent service) {
        return null;
    }

    @Nullable
    @Override
    public ComponentName startForegroundService(Intent service) {
        return null;
    }

    @Override
    public boolean stopService(Intent service) {
        return false;
    }

    @Override
    public boolean bindService(Intent service, @NonNull ServiceConnection conn, int flags) {
        return false;
    }

    @Override
    public void unbindService(@NonNull ServiceConnection conn) {

    }

    @Override
    public boolean startInstrumentation(@NonNull ComponentName className, @Nullable String profileFile, @Nullable Bundle arguments) {
        return false;
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        return null;
    }

    @Nullable
    @Override
    public String getSystemServiceName(@NonNull Class<?> serviceClass) {
        return null;
    }

    @Override
    public int checkPermission(@NonNull String permission, int pid, int uid) {
        return PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public int checkCallingPermission(@NonNull String permission) {
        return PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public int checkCallingOrSelfPermission(@NonNull String permission) {
        return PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public int checkSelfPermission(@NonNull String permission) {
        return PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void enforcePermission(@NonNull String permission, int pid, int uid, @Nullable String message) {

    }

    @Override
    public void enforceCallingPermission(@NonNull String permission, @Nullable String message) {

    }

    @Override
    public void enforceCallingOrSelfPermission(@NonNull String permission, @Nullable String message) {

    }

    @Override
    public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {

    }

    @Override
    public void revokeUriPermission(Uri uri, int modeFlags) {

    }

    @Override
    public void revokeUriPermission(String toPackage, Uri uri, int modeFlags) {

    }

    @Override
    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        return PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public int checkCallingUriPermission(Uri uri, int modeFlags) {
        return PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
        return PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public int checkUriPermission(@Nullable Uri uri, @Nullable String readPermission, @Nullable String writePermission, int pid, int uid, int modeFlags) {
        return PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void enforceUriPermission(Uri uri, int pid, int uid, int modeFlags, String message) {

    }

    @Override
    public void enforceCallingUriPermission(Uri uri, int modeFlags, String message) {

    }

    @Override
    public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags, String message) {

    }

    @Override
    public void enforceUriPermission(@Nullable Uri uri, @Nullable String readPermission, @Nullable String writePermission, int pid, int uid, int modeFlags, @Nullable String message) {

    }

    @Override
    public Context createPackageContext(String packageName, int flags) throws NameNotFoundException {
        return null;
    }

    @Override
    public Context createContextForSplit(String splitName) throws NameNotFoundException {
        return null;
    }

    @Override
    public Context createConfigurationContext(@NonNull Configuration overrideConfiguration) {
        return null;
    }

    @Override
    public Context createDisplayContext(@NonNull Display display) {
        return null;
    }

    @Override
    public Context createDeviceProtectedStorageContext() {
        return null;
    }

    @Override
    public boolean isDeviceProtectedStorage() {
        return false;
    }

    private class AlertDialogButtonListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:
                    switch (mMode) {
                        case MODE_CHECK_VERSION:
                            updateNow(getUpdateUrl());
                            break;
                        case MODE_ASK_FOR_RATE:
                            mActivity.startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(getGooglePlayStoreUrl())));
                            break;
                    }
                    break;
                case AlertDialog.BUTTON_NEUTRAL:
                    remindMeLater(getReminderTimer());
                    break;
                case AlertDialog.BUTTON_NEGATIVE:
                    break;
            }
        }
    }

    private class VersionContentLoader extends Context implements LoaderManager.LoaderCallbacks<VersionContentData> {
        static final String KEY_URL = "url";
        @NonNull
        @Override
        public Loader<VersionContentData> onCreateLoader(int id, @Nullable Bundle args) {
            String url = args != null ? args.getString(KEY_URL) : null;
            return new VersionContentRequest(mActivity, url);
        }

        @Override
        @SuppressLint("ApplySharedPref")
        public void onLoadFinished(@NonNull Loader<VersionContentData> loader, VersionContentData data) {
            mVersionCode = 0;
            int statusCode = data.getStatusCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Response invalid. status code = " + statusCode);
                if (mOnReceiveListener != null) {
                    mOnReceiveListener.onReceive(statusCode, false, null);
                }
            } else {
                String result = data.getResult();
                try {
                    if (!result.startsWith("{")) { // for response who append with unknown char
                        result = result.substring(1);
                    }
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "status = " + statusCode);
                        Log.d(TAG, "result = " + result);
                    }

                    // json format from server:
                    JSONObject json = (JSONObject) new JSONTokener(result).nextValue();
                    mVersionCode = json.optInt("version_code");
                    mIsBlocking = json.optBoolean("is_blocking", false);
                    PreferenceManager.getDefaultSharedPreferences(mActivity).edit()
                            .putBoolean(PREF_IS_BLOCKING, mIsBlocking).commit();
                    if (mIsBlocking)
                        mDialogCancelable = false;

                    // show default dialog if no listener is set OR it returned true
                    if (mOnReceiveListener == null ||
                            mOnReceiveListener.onReceive(statusCode, mIsBlocking, result)) {
                        int currentVersionCode = getCurrentVersionCode();
                        if (currentVersionCode < mVersionCode
                                && mVersionCode != getIgnoreVersionCode()) {
                            // Use download URL from JSON if available
                            String downloadUrl = json.optString("download_url");
                            if (downloadUrl != null && downloadUrl.startsWith("http")) {
                                mUpdateUrl = downloadUrl;
                            }
                            // set dialog message and show update dialog
                            String content = json.optString("content");
                            setMessage(content);
                            showDialog();
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "does your server response have valid json format?");
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
            mActivity.getLoaderManager().destroyLoader(loader.getId());
        }

        @Override
        public void onLoaderReset(@NonNull Loader<VersionContentData> loader) { }

        @Override
        public AssetManager getAssets() {
            return null;
        }

        @Override
        public Resources getResources() {
            return null;
        }

        @Override
        public PackageManager getPackageManager() {
            return null;
        }

        @Override
        public ContentResolver getContentResolver() {
            return null;
        }

        @Override
        public Looper getMainLooper() {
            return null;
        }

        @Override
        public Context getApplicationContext() {
            return null;
        }

        @Override
        public void setTheme(int resid) {

        }

        @Override
        public Resources.Theme getTheme() {
            return null;
        }

        @Override
        public ClassLoader getClassLoader() {
            return null;
        }

        @Override
        public String getPackageName() {
            return null;
        }

        @Override
        public ApplicationInfo getApplicationInfo() {
            return null;
        }

        @Override
        public String getPackageResourcePath() {
            return null;
        }

        @Override
        public String getPackageCodePath() {
            return null;
        }

        @Override
        public SharedPreferences getSharedPreferences(String name, int mode) {
            return null;
        }

        @Override
        public boolean moveSharedPreferencesFrom(Context sourceContext, String name) {
            return false;
        }

        @Override
        public boolean deleteSharedPreferences(String name) {
            return false;
        }

        @Override
        public FileInputStream openFileInput(String name) throws FileNotFoundException {
            return null;
        }

        @Override
        public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
            return null;
        }

        @Override
        public boolean deleteFile(String name) {
            return false;
        }

        @Override
        public File getFileStreamPath(String name) {
            return null;
        }

        @Override
        public File getDataDir() {
            return null;
        }

        @Override
        public File getFilesDir() {
            return null;
        }

        @Override
        public File getNoBackupFilesDir() {
            return null;
        }

        @Nullable
        @Override
        public File getExternalFilesDir(@Nullable String type) {
            return null;
        }

        @Override
        public File[] getExternalFilesDirs(String type) {
            return new File[0];
        }

        @Override
        public File getObbDir() {
            return null;
        }

        @Override
        public File[] getObbDirs() {
            return new File[0];
        }

        @Override
        public File getCacheDir() {
            return null;
        }

        @Override
        public File getCodeCacheDir() {
            return null;
        }

        @Nullable
        @Override
        public File getExternalCacheDir() {
            return null;
        }

        @Override
        public File[] getExternalCacheDirs() {
            return new File[0];
        }

        @Override
        public File[] getExternalMediaDirs() {
            return new File[0];
        }

        @Override
        public String[] fileList() {
            return new String[0];
        }

        @Override
        public File getDir(String name, int mode) {
            return null;
        }

        @Override
        public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
            return null;
        }

        @Override
        public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, @Nullable DatabaseErrorHandler errorHandler) {
            return null;
        }

        @Override
        public boolean moveDatabaseFrom(Context sourceContext, String name) {
            return false;
        }

        @Override
        public boolean deleteDatabase(String name) {
            return false;
        }

        @Override
        public File getDatabasePath(String name) {
            return null;
        }

        @Override
        public String[] databaseList() {
            return new String[0];
        }

        @Override
        public Drawable getWallpaper() {
            return null;
        }

        @Override
        public Drawable peekWallpaper() {
            return null;
        }

        @Override
        public int getWallpaperDesiredMinimumWidth() {
            return 0;
        }

        @Override
        public int getWallpaperDesiredMinimumHeight() {
            return 0;
        }

        @Override
        public void setWallpaper(Bitmap bitmap) throws IOException {

        }

        @Override
        public void setWallpaper(InputStream data) throws IOException {

        }

        @Override
        public void clearWallpaper() throws IOException {

        }

        @Override
        public void startActivity(Intent intent) {

        }

        @Override
        public void startActivity(Intent intent, @Nullable Bundle options) {

        }

        @Override
        public void startActivities(Intent[] intents) {

        }

        @Override
        public void startActivities(Intent[] intents, Bundle options) {

        }

        @Override
        public void startIntentSender(IntentSender intent, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {

        }

        @Override
        public void startIntentSender(IntentSender intent, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, @Nullable Bundle options) throws IntentSender.SendIntentException {

        }

        @Override
        public void sendBroadcast(Intent intent) {

        }

        @Override
        public void sendBroadcast(Intent intent, @Nullable String receiverPermission) {

        }

        @Override
        public void sendOrderedBroadcast(Intent intent, @Nullable String receiverPermission) {

        }

        @Override
        public void sendOrderedBroadcast(@NonNull Intent intent, @Nullable String receiverPermission, @Nullable BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

        }

        @Override
        public void sendBroadcastAsUser(Intent intent, UserHandle user) {

        }

        @Override
        public void sendBroadcastAsUser(Intent intent, UserHandle user, @Nullable String receiverPermission) {

        }

        @Override
        public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, @Nullable String receiverPermission, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

        }

        @Override
        public void sendStickyBroadcast(Intent intent) {

        }

        @Override
        public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

        }

        @Override
        public void removeStickyBroadcast(Intent intent) {

        }

        @Override
        public void sendStickyBroadcastAsUser(Intent intent, UserHandle user) {

        }

        @Override
        public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle user, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

        }

        @Override
        public void removeStickyBroadcastAsUser(Intent intent, UserHandle user) {

        }

        @Nullable
        @Override
        public Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter) {
            return null;
        }

        @Nullable
        @Override
        public Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter, int flags) {
            return null;
        }

        @Nullable
        @Override
        public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, @Nullable String broadcastPermission, @Nullable Handler scheduler) {
            return null;
        }

        @Nullable
        @Override
        public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, @Nullable String broadcastPermission, @Nullable Handler scheduler, int flags) {
            return null;
        }

        @Override
        public void unregisterReceiver(BroadcastReceiver receiver) {

        }

        @Nullable
        @Override
        public ComponentName startService(Intent service) {
            return null;
        }

        @Nullable
        @Override
        public ComponentName startForegroundService(Intent service) {
            return null;
        }

        @Override
        public boolean stopService(Intent service) {
            return false;
        }

        @Override
        public boolean bindService(Intent service, @NonNull ServiceConnection conn, int flags) {
            return false;
        }

        @Override
        public void unbindService(@NonNull ServiceConnection conn) {

        }

        @Override
        public boolean startInstrumentation(@NonNull ComponentName className, @Nullable String profileFile, @Nullable Bundle arguments) {
            return false;
        }

        @Override
        public Object getSystemService(@NonNull String name) {
            return null;
        }

        @Nullable
        @Override
        public String getSystemServiceName(@NonNull Class<?> serviceClass) {
            return null;
        }

        @Override
        public int checkPermission(@NonNull String permission, int pid, int uid) {
            return PackageManager.PERMISSION_GRANTED;
        }

        @Override
        public int checkCallingPermission(@NonNull String permission) {
            return PackageManager.PERMISSION_GRANTED;
        }

        @Override
        public int checkCallingOrSelfPermission(@NonNull String permission) {
            return PackageManager.PERMISSION_GRANTED;
        }

        @Override
        public int checkSelfPermission(@NonNull String permission) {
            return PackageManager.PERMISSION_GRANTED;
        }

        @Override
        public void enforcePermission(@NonNull String permission, int pid, int uid, @Nullable String message) {

        }

        @Override
        public void enforceCallingPermission(@NonNull String permission, @Nullable String message) {

        }

        @Override
        public void enforceCallingOrSelfPermission(@NonNull String permission, @Nullable String message) {

        }

        @Override
        public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {

        }

        @Override
        public void revokeUriPermission(Uri uri, int modeFlags) {

        }

        @Override
        public void revokeUriPermission(String toPackage, Uri uri, int modeFlags) {

        }

        @Override
        public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
            return PackageManager.PERMISSION_GRANTED;
        }

        @Override
        public int checkCallingUriPermission(Uri uri, int modeFlags) {
            return PackageManager.PERMISSION_GRANTED;
        }

        @Override
        public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
            return PackageManager.PERMISSION_GRANTED;
        }

        @Override
        public int checkUriPermission(@Nullable Uri uri, @Nullable String readPermission, @Nullable String writePermission, int pid, int uid, int modeFlags) {
            return PackageManager.PERMISSION_GRANTED;
        }

        @Override
        public void enforceUriPermission(Uri uri, int pid, int uid, int modeFlags, String message) {

        }

        @Override
        public void enforceCallingUriPermission(Uri uri, int modeFlags, String message) {

        }

        @Override
        public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags, String message) {

        }

        @Override
        public void enforceUriPermission(@Nullable Uri uri, @Nullable String readPermission, @Nullable String writePermission, int pid, int uid, int modeFlags, @Nullable String message) {

        }

        @Override
        public Context createPackageContext(String packageName, int flags) throws NameNotFoundException {
            return null;
        }

        @Override
        public Context createContextForSplit(String splitName) throws NameNotFoundException {
            return null;
        }

        @Override
        public Context createConfigurationContext(@NonNull Configuration overrideConfiguration) {
            return null;
        }

        @Override
        public Context createDisplayContext(@NonNull Display display) {
            return null;
        }

        @Override
        public Context createDeviceProtectedStorageContext() {
            return null;
        }

        @Override
        public boolean isDeviceProtectedStorage() {
            return false;
        }
    }

    private static class VersionContentRequest extends AsyncTaskLoader<VersionContentData> {
        private String mUrl;

        VersionContentRequest(Context context, String url) {
            super(context);
            mUrl = url;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Nullable
        @Override
        public VersionContentData loadInBackground() {
            int statusCode = -1;
            String responseBody = null;
            ByteArrayOutputStream out = null;

            try {
                URL url = new URL(mUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Connection", "close");
                connection.setConnectTimeout(6000);
                connection.setRequestMethod("GET");

                statusCode = connection.getResponseCode();

                if (statusCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));

                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        result.append(line);
                    }
                    br.close();
                    connection.disconnect();
                    responseBody = result.toString();
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                    }
                }
            }
            return new VersionContentData(statusCode, responseBody);
        }
    }

    static class VersionContentData {
        private String result;
        private int statusCode;

        VersionContentData(int statusCode, String result) {
            this.statusCode = statusCode;
            this.result = result;
        }

        String getResult() {
            return result;
        }

        int getStatusCode() {
            return statusCode;
        }
    }
}
