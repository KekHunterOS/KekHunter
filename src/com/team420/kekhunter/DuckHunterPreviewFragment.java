package com.team420.kekhunter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.team420.kekhunter.AsyncTask.DuckHuntAsyncTask;
import com.team420.kekhunter.utils.NhPaths;

public class DuckHunterPreviewFragment extends Fragment {
    private DuckHuntAsyncTask duckHuntAsyncTask;
    private String duckyInputFile;
    private String duckyOutputFile;
    private TextView previewSource;
    private Activity activity;
    private boolean isReceiverRegistered;
    private PreviewDuckyBroadcastReceiver previewDuckyBroadcastReceiver = new PreviewDuckyBroadcastReceiver();

    public DuckHunterPreviewFragment(String inFilePath, String outFilePath){
        this.duckyInputFile = inFilePath;
        this.duckyOutputFile = outFilePath;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.duck_hunter_preview, container, false);
        previewSource = rootView.findViewById(R.id.source);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isReceiverRegistered){
            activity.registerReceiver(previewDuckyBroadcastReceiver, new IntentFilter(BuildConfig.APPLICATION_ID + ".PREVIEWDUCKY"));
            isReceiverRegistered = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isReceiverRegistered) {
            activity.unregisterReceiver(previewDuckyBroadcastReceiver);
            isReceiverRegistered = false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        previewSource = null;
    }

    public class PreviewDuckyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra("ACTION")){
                case "PREVIEWDUCKY":
                    activity.sendBroadcast(new Intent().putExtra("ACTION", "SHOULDCONVERT").putExtra("SHOULDCONVERT", false).setAction(BuildConfig.APPLICATION_ID + ".SHOULDCONVERT"));
                    duckHuntAsyncTask = new DuckHuntAsyncTask(DuckHuntAsyncTask.CONVERT);
                    duckHuntAsyncTask.setListener(new DuckHuntAsyncTask.DuckHuntAsyncTaskListener() {
                        @Override
                        public void onAsyncTaskPrepare() {
                            previewSource.setText("Loading wait...");
                        }

                        @Override
                        public void onAsyncTaskFinished(Object result) {
                            duckHuntAsyncTask = new DuckHuntAsyncTask(DuckHuntAsyncTask.READ_PREVIEW);
                            duckHuntAsyncTask.setListener(new DuckHuntAsyncTask.DuckHuntAsyncTaskListener() {
                                @Override
                                public void onAsyncTaskPrepare() {
                                }

                                @Override
                                public void onAsyncTaskFinished(Object result) {
                                    previewSource.setText(result.toString());
                                }
                            });
                            duckHuntAsyncTask.execute("cat " + duckyOutputFile);
                        }
                    });
                    duckHuntAsyncTask.execute("sh " + NhPaths.APP_SCRIPTS_PATH + "/duckyconverter -i " + duckyInputFile + " -o " + duckyOutputFile + " -l " + DuckHunterFragment.lang, intent.getBooleanExtra("SHOULDCONVERT", true));
                    break;
            }
        }
    }
}
