package com.offsec.nethunter;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.util.ConcurrentModificationException;
import java.util.Locale;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class DeAuthWhitelistActivity extends AppCompatActivity {

    private NhPaths nh = new NhPaths();
    private Activity activity;
    private final ShellExecuter exe = new ShellExecuter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.deauth_whitelist);
        if (Build.VERSION.SDK_INT >= 21) {
            // detail for android 5 devices
            getWindow().setStatusBarColor(getResources().getColor(R.color.darkTitle));
        }

        EditText whitelist = findViewById(R.id.deauth_modify);
        whitelist.setText(String.format(Locale.getDefault(),getString(R.string.loading_file), "/sdcard/nh_files/deauth/whitelist.txt"));
        exe.ReadFile_ASYNC("/sdcard/nh_files/deauth/whitelist.txt", whitelist);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        nh.showMessage(activity, "File Loaded");
    }


    public void updatewhitelist(View view) {
        EditText source = findViewById(R.id.deauth_modify);
        String newSource = source.getText().toString();
        Boolean isSaved = exe.SaveFileContents(newSource, "/sdcard/nh_files/deauth/whitelist.txt");
        if (isSaved) {
            nh.showMessage(activity,"Source updated");
        } else {
            nh.showMessage(activity,"Source not updated");
        }
    }
}