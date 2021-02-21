package com.team420.kekhunter.AsyncTask;

import android.os.AsyncTask;
import android.widget.TextView;

import com.team420.kekhunter.ChrootManagerFragment;
import com.team420.kekhunter.utils.NhPaths;
import com.team420.kekhunter.utils.ShellExecuter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;

public class ChrootManagerAsynctask extends AsyncTask<Object, Integer, Void> {
    private ChrootManagerAsyncTaskListener listener;
    private ShellExecuter exe = new ShellExecuter();
    private int ACTIONCODE;
    private int resultCode;
    private ArrayList<String> resultString = new ArrayList<>();
    public static final int CHECK_CHROOT = 0;
    public static final int MOUNT_CHROOT = 1;
    public static final int UNMOUNT_CHROOT = 2;
    public static final int INSTALL_CHROOT = 3;
    public static final int BACKUP_CHROOT = 4;
    public static final int REMOVE_CHROOT = 5;
    public static final int DOWNLOAD_CHROOT = 6;
    public static final int FIND_CHROOT = 7;
    public static final int ISSUE_BANNER = 8;

    public ChrootManagerAsynctask(Integer ACTIONCODE){
        this.ACTIONCODE = ACTIONCODE;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ChrootManagerFragment.isAsyncTaskRunning = true;
        if (listener != null) {
            listener.onAsyncTaskPrepare();
        }
    }

    @Override
    protected Void doInBackground(Object... objects) {
        switch (ACTIONCODE) {
            case ISSUE_BANNER:
                exe.RunAsRootOutput("echo \"" + objects[1].toString() + "\"", ((TextView)objects[0]));
                break;
            case CHECK_CHROOT:
                resultCode = exe.RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/chrootmgr -c \"status\" -p " + objects[1].toString(), ((TextView)objects[0]));
                break;
            case MOUNT_CHROOT:
                resultCode = exe.RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/bootkali_init", ((TextView)objects[0]));
                exe.RunAsRootOutput("sleep 1 && " + NhPaths.CHROOT_INITD_SCRIPT_PATH, ((TextView)objects[0]));
                break;
            case UNMOUNT_CHROOT:
                resultCode = exe.RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/killkali", ((TextView)objects[0]));
                break;
            case INSTALL_CHROOT:
                resultCode = exe.RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/chrootmgr -c \"restore " + objects[1] + " " + objects[2] + "\"", ((TextView)objects[0]));
                break;
            case REMOVE_CHROOT:
                resultCode = exe.RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/chrootmgr -c \"remove " + NhPaths.CHROOT_PATH() + "\"", ((TextView)objects[0]));
                break;
            case BACKUP_CHROOT:
                resultCode = exe.RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/chrootmgr -c \"backup " + objects[1].toString() + " " + objects[2].toString() + "\"", ((TextView)objects[0]));
                break;
            case FIND_CHROOT:
                resultString.addAll(Arrays.asList(new ShellExecuter().RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/chrootmgr -c \"findchroot\"").split("\\n")));
                break;
            case DOWNLOAD_CHROOT:
                try {
                    exe.RunAsRootOutput("echo \"[!] The Download has been started...Please wait.\"", ((TextView)objects[0]));
                    int count;
                    URL url = new URL("https://" + objects[1].toString() + objects[2].toString());
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    int lengthOfFile = connection.getContentLength();

                    InputStream input = connection.getInputStream();
                    BufferedInputStream reader = new BufferedInputStream(input);
                    BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(objects[3].toString()));

                    byte[] data = new byte[1024];
                    long bytes = -1;

                    while ((count = reader.read(data)) != -1) {
                        bytes += count;
                        int progress = (int) ((bytes / (float) lengthOfFile) * 100);
                        publishProgress(progress);
                        writer.write(data, 0, count);
                    }
                    writer.close();
                    reader.close();
                    exe.RunAsRootOutput("echo \"[+] Download completed.\"", ((TextView)objects[0]));
                } catch (Exception e) {
                    exe.RunAsRootOutput("echo \"[-] " + e.getMessage() + "\"", ((TextView)objects[0]));
                    resultCode = 1;
                }
                break;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        if (listener != null) {
            listener.onAsyncTaskProgressUpdate(progress[0]);
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (listener != null) {
            listener.onAsyncTaskFinished(resultCode, resultString);
        }
        ChrootManagerFragment.isAsyncTaskRunning = false;
    }

    public void setListener(ChrootManagerAsyncTaskListener listener) {
        this.listener = listener;
    }

    public interface ChrootManagerAsyncTaskListener {
        void onAsyncTaskPrepare();
        void onAsyncTaskProgressUpdate(int progress);
        void onAsyncTaskFinished(int resultCode, ArrayList<String> resultString);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}