package com.offsec.nethunter.AsyncTask;

import android.os.AsyncTask;

import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

public class MacchangerAsyncTask extends AsyncTask<String, Void, Void> {
    private MacchangerAsyncTaskListener listener;
    public static final int GETHOSTNAME = 0;
    public static final int SETHOSTNAME = 1;
    public static final int SETMAC = 2;
    public static final int GETORIGINMAC = 3;

    private ShellExecuter exe = new ShellExecuter();
    private Object result;
    private int ActionCode;


    public MacchangerAsyncTask(Integer ActionCode) {
        this.ActionCode = ActionCode;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (listener != null) {
            listener.onAsyncTaskPrepare();
        }
    }

    @Override
    protected Void doInBackground(String... strings) {
        switch (ActionCode){
            case GETHOSTNAME:
                result = exe.Executer("getprop net.hostname");
                break;
            case SETHOSTNAME:
                result = exe.Executer("setprop net.hostname " + strings[0]);
                break;
            case GETORIGINMAC:
                result = exe.RunAsRootOutput(NhPaths.APP_SCRIPTS_BIN_PATH + "/macchanger -s " + strings[0] + " | " + NhPaths.BUSYBOX + " awk '/Permanent/ {print $3}'");
                break;
            case SETMAC:
                if (strings[0].matches("^wlan.*$")){
                    result = exe.RunAsRootReturnValue(NhPaths.APP_SCRIPTS_PATH + "/changemac " + strings[0] + " " + strings[1]);
                } else {
                    result = exe.RunAsRootReturnValue(NhPaths.APP_SCRIPTS_BIN_PATH + "/macchanger " + strings[0] + " -m " + strings[1]);
                }
                break;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void voids) {
        if (listener != null) {
            listener.onAsyncTaskFinished(result);
        }
    }

    public void setListener(MacchangerAsyncTaskListener listener) {
        this.listener = listener;
    }

    public interface MacchangerAsyncTaskListener {
        void onAsyncTaskPrepare();
        void onAsyncTaskFinished(Object result);
    }
}
