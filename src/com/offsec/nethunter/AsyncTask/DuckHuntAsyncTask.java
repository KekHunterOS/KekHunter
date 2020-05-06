package com.offsec.nethunter.AsyncTask;

import android.os.AsyncTask;

import com.offsec.nethunter.utils.ShellExecuter;

public class DuckHuntAsyncTask extends AsyncTask<Object, Void, Void> {
    public static final int ATTACK = 1;
    public static final int CONVERT = 2;
    public static final int READ_PREVIEW = 3;
    private ShellExecuter exe = new ShellExecuter();
    private DuckHuntAsyncTaskListener listener;
    private int ActionCode;
    private Object result;

    public DuckHuntAsyncTask(Integer ActionCode) {
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
    protected Void doInBackground(Object... objects) {
        switch (ActionCode) {
            case ATTACK:
                result=true;
                String hidgs[] = {"/dev/hidg0", "/dev/hidg1"};
                for (String hidg : hidgs) {
                    if (!exe.RunAsRootOutput("stat -c '%a' " + hidg).equals("666")){
                        result=false;
                        break;
                    };
                }
                if ((boolean)result) {
                    exe.RunAsRootOutput(objects[0].toString());
                }
                break;
            case CONVERT:
                if ((boolean)objects[1]){
                    result = exe.RunAsRootOutput(objects[0].toString());
                } else
                    result = "";
                break;
            case READ_PREVIEW:
                result = exe.RunAsRootOutput(objects[0].toString());
                break;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void voids) {
        super.onPostExecute(voids);
        if (listener != null) {
            listener.onAsyncTaskFinished(result);
        }

    }

    public void setListener(DuckHuntAsyncTaskListener listener) {
        this.listener = listener;
    }

    public interface DuckHuntAsyncTaskListener {
        void onAsyncTaskPrepare();
        void onAsyncTaskFinished(Object result);
    }
}
