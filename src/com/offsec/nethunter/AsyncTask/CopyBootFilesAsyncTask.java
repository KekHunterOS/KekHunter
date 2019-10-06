package com.offsec.nethunter.AsyncTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.offsec.nethunter.AppNavHomeActivity;
import com.offsec.nethunter.BuildConfig;
import com.offsec.nethunter.ChrootManagerFragment;
import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CopyBootFilesAsyncTask extends AsyncTask<String, String, String>{

    private final String COPY_ASSETS_TAG = "COPY_ASSETS_TAG";
    private File sdCardDir;
    private File scriptsDir;
    private File etcDir;
    private String buildTime;
    private Boolean shouldRun;
    private final WeakReference<ProgressDialog> progressDialogRef;
    private CopyBootFilesAsyncTaskListener listener;
    private String result = "";
    private SharedPreferences prefs;
    private final WeakReference<Context> context;
    private final WeakReference<Activity> activity;

    public CopyBootFilesAsyncTask(Context context, Activity activity, ProgressDialog progressDialog){
        this.context = new WeakReference<>(context);
        this.activity = new WeakReference<>(activity);
        this.progressDialogRef = new WeakReference<>(progressDialog);
        this.sdCardDir = new File(NhPaths.APP_SD_FILES_PATH);
        this.scriptsDir = new File(NhPaths.APP_SCRIPTS_PATH);
        this.etcDir = new File(NhPaths.APP_INITD_PATH);
        this.prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd KK:mm:ss a zzz",
                Locale.US);
        this.buildTime = sdf.format(BuildConfig.BUILD_TIME);
        this.shouldRun = true;
    }

    @Override
    protected void onPreExecute() {
        // Check if it is a new build and inflates the nethunter files again if yes.
        if (!prefs.getString(COPY_ASSETS_TAG, buildTime).equals(buildTime) || !sdCardDir.isDirectory() || !scriptsDir.isDirectory() || !etcDir.isDirectory()) {
            ProgressDialog progressDialog = progressDialogRef.get();
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setTitle("New app build detected:");
            progressDialog.setMessage("Coping new files...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        } else {
            Log.d(COPY_ASSETS_TAG, "FILES NOT COPIED");
            shouldRun = false;
        }
        super.onPreExecute();
        if (listener != null) {
            listener.onAsyncTaskPrepare();
        }
    }

    @Override
    protected String doInBackground(String ...strings) {
        // setup
        if(shouldRun){
            List<String> bootkali_list = new ArrayList<>();
            bootkali_list.add("bootkali");
            bootkali_list.add("bootkali_init");
            bootkali_list.add("bootkali_login");
            bootkali_list.add("bootkali_bash");
            bootkali_list.add("chrootmgr");
            bootkali_list.add("duckyconverter");
            bootkali_list.add("killkali");

            Log.d(COPY_ASSETS_TAG, "COPYING FILES....");
            // 1:1 copy (recursive) of the assets/{scripts, etc, wallpapers} folders to /data/data/...
            publishProgress("Doing app files update. (init.d and filesDir).");
            assetsToFiles(NhPaths.APP_PATH, "", "data");
            // 1:1 copy (recursive) of the configs to  /sdcard...
            publishProgress("Doing sdcard files update. (nh_files).");
            assetsToFiles(NhPaths.SD_PATH, "", "sdcard");
            publishProgress("Fixing permissions for new files");
            ShellExecuter exe = new ShellExecuter();
            exe.RunAsRoot(new String[]{"chmod -R 700 " + NhPaths.APP_SCRIPTS_PATH + "/*", "chmod -R 700 " + NhPaths.APP_INITD_PATH + "/*"});
            SharedPreferences.Editor ed = prefs.edit();
            ed.putString(COPY_ASSETS_TAG, buildTime);
            ed.apply();

            publishProgress("Checking for SYMLINKS to bootkali....");
            try {
                MakeSYSWriteable();
                // Loop over bootkali list (e.g. bootkali | bootkali_bash | bootkali_env)
                for (String temp : bootkali_list) {

                    String sys_temp = "/system/bin/" + temp;

                    // Define each as a new file
                    File filePath = new File(sys_temp);

                    // If the symlink is not found, then go create one!
                    if(!isSymlink(filePath)) {
                        //publishProgress("Creating symlink for " + temp);
                        NotFound(temp);
                    }
                }
                MakeSYSReadOnly();
            } catch (IOException e) {
                e.printStackTrace();
            }

            publishProgress("Checking for chroot....");
            String command = "if [ -d " + NhPaths.CHROOT_PATH() + " ];then echo 1; fi"; //check the dir existence
            final String _res = exe.RunAsRootOutput(command);
            if (_res.equals("1")) {
                ed = prefs.edit();
                ed.putBoolean(AppNavHomeActivity.CHROOT_INSTALLED_TAG, true);
                ed.commit();
                publishProgress("Chroot Found!");
                // @Re4son @kimocoder @yesimxev is it still necessnary to remount /data partition and chmod +s the chroot /usr/bin/sudo?
                // Mount suid /data && fix sudo
                /*publishProgress(exe.RunAsRootOutput(NhPaths.BUSYBOX + " mount -o remount,suid /data && chmod +s " +
                        NhPaths.CHROOT_PATH() + "/usr/bin/sudo" +
                        " && echo \"Initial setup done!\""));*/
            } else {
                publishProgress("Chroot not Found, install it in Chroot Manager");
            }

            // This is required to install the additional apks in Android Oreo and newer
            // We can no longer install user apps through TWRP so we copy them across and install them here
            // Get the list of *.apk files in /sdcard/nh_files/cache/apk and install them using "pm install"
            publishProgress("Installing additional apps....");
            String ApkCachePath= NhPaths.APP_SD_FILES_PATH + "/cache/apk/";
            ArrayList<String> filenames = FetchFiles(ApkCachePath);
            int i, x;

            for (String object: filenames) {
                if (object.contains(".apk")){
                    String apk = ApkCachePath + object;
                    //publishProgress("Installing additional apps.\nThe device may be unresponsive for a few minutes\nInstalling " + object);
                    ShellExecuter install = new ShellExecuter();
                    install.RunAsRoot(new String[]{"mv " + apk + " /data/local/tmp/ && pm install /data/local/tmp/" + object + " && rm -f /data/local/tmp/" + object});
                }
            }
        }
        return result;
    }

    private Boolean pathIsAllowed(String path, String copyType) {
        // never copy images, sounds or webkit
        if (!path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit")) {
            if (copyType.equals("sdcard")) {
                if (path.equals("")) {
                    return true;
                } else return path.startsWith(NhPaths.NH_SD_FOLDER_NAME);
            }
            if (copyType.equals("data")) {
                if (path.equals("")) {
                    return true;
                } else if (path.startsWith("scripts")) {
                    return true;
                } else if (path.startsWith("wallpapers")) {
                    return true;
                } else return path.startsWith("etc");
            }
            return false;
        }
        return false;
    }

    // now this only copies the folders: scripts, etc , wallpapers to /data/data...
    private void assetsToFiles(String TARGET_BASE_PATH, String path, String copyType) {
        AssetManager assetManager = context.get().getAssets();
        String[] assets;
        try {
            // Log.i("tag", "assetsTo" + copyType +"() "+path);
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(TARGET_BASE_PATH, path);
            } else {
                String fullPath = TARGET_BASE_PATH + "/" + path;
                // Log.i("tag", "path="+fullPath)

                File dir = new File(fullPath);
                if (!dir.exists() && pathIsAllowed(path, copyType)) { // copy those dirs
                    if (!dir.mkdirs()) {
                        Log.i("tag", "could not create dir " + fullPath);
                    }
                }
                for (String asset : assets) {
                    String p;
                    if (path.equals("")) {
                        p = "";
                    } else {
                        p = path + "/";
                    }
                    if (pathIsAllowed(path, copyType)) {
                        assetsToFiles(TARGET_BASE_PATH, p + asset, copyType);
                    }
                }

            }
        } catch (IOException ex) {
            Log.e("tag", "I/O Exception", ex);
        }
    }

    private void copyFile(String TARGET_BASE_PATH, String filename) {
        AssetManager assetManager = context.get().getAssets();
        InputStream in;
        OutputStream out;
        String newFileName = null;
        try {
            // Log.i("tag", "copyFile() "+filename);
            in = assetManager.open(filename);
            newFileName = TARGET_BASE_PATH + "/" + filename;
            /* rename the file name if its suffix is either -arm64 or -armhf before copying the file.*/
            out = new FileOutputStream(renameAssetIfneeded(newFileName));
            byte[] buffer = new byte[8092];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.e("tag", "Exception in copyFile() of " + newFileName);
            Log.e("tag", "Exception in copyFile() " + e.toString());
        }

    }

    // Check for symlink for bootkali
    // http://stackoverflow.com/questions/813710/java-1-6-determine-symbolic-links/813730#813730
    private boolean isSymlink(File file) throws IOException {
        if (file == null)
            throw new NullPointerException("File must not be null");
        File canon;
        if (file.getParent() == null) {
            canon = file;
        } else {
            File canonDir = file.getParentFile().getCanonicalFile();
            canon = new File(canonDir, file.getName());
        }
        return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
    }

    private void MakeSYSWriteable(){
        ShellExecuter exe = new ShellExecuter();
        Log.d(COPY_ASSETS_TAG, "Making /system writeable for symlink");
        exe.RunAsRoot(new String[]{"mount -o rw,remount,rw /system"});
    }

    private void MakeSYSReadOnly(){
        ShellExecuter exe = new ShellExecuter();
        Log.d(COPY_ASSETS_TAG, "Making /system readonly for symlink");
        exe.RunAsRoot(new String[]{"mount -o ro,remount,ro /system"});
    }

    private void NotFound(String filename){
        ShellExecuter exe = new ShellExecuter();
        Log.d(COPY_ASSETS_TAG, "Symlinking " + filename);
        Log.d(COPY_ASSETS_TAG, "command output: ln -s " + NhPaths.APP_SCRIPTS_PATH + "/" + filename + " /system/bin/" + filename);
        exe.RunAsRoot(new String[]{"ln -s " + NhPaths.APP_SCRIPTS_PATH + "/" + filename + " /system/bin/" + filename});
    }

    // Get a list of files from a directory
    private ArrayList<String> FetchFiles(String folder) {

        ArrayList<String> filenames = new ArrayList<String>();
        String path = folder;
        File directory = new File(path);

        if (directory.exists()) {
            File[] files = directory.listFiles();

            for (int i = 0; i < files.length; i++) {
                String file_name = files[i].getName();
                filenames.add(file_name);
            }
        }
        return filenames;
    }

    // This rename the filename which suffix is either [name]-arm64 or [name]-armhf to [name] according to the user's CPU ABI.
    private String renameAssetIfneeded(String asset){
        if (asset.matches("^.*-arm64$")){
            if (Build.CPU_ABI.equals("arm64-v8a")){
                return (asset.replaceAll("-arm64$",""));
            }
        } else if (asset.matches("^.*-armeabi$")){
            if (!Build.CPU_ABI.equals("arm64-v8a")){
                return (asset.replaceAll("-armeabi$",""));
            }
        }
        return asset;
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        super.onProgressUpdate(progress);
        if (progressDialogRef.get() != null)
            progressDialogRef.get().setMessage(progress[0]);
    }

    @Override
    protected void onPostExecute(String objects) {
        super.onPostExecute(objects);
        if (progressDialogRef.get() != null && progressDialogRef.get().isShowing())
            progressDialogRef.get().dismiss();
        if (listener != null) {
            listener.onAsyncTaskFinished(result);
        }

    }
    public void setListener(CopyBootFilesAsyncTaskListener listener) {
        this.listener = listener;
    }

    public interface CopyBootFilesAsyncTaskListener {
        void onAsyncTaskPrepare();
        void onAsyncTaskFinished(Object result);
    }
}