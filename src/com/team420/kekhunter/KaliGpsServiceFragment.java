package com.team420.kekhunter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.team420.kekhunter.gps.KaliGPSUpdates;
import com.team420.kekhunter.gps.LocationUpdateService;
import com.team420.kekhunter.utils.NhPaths;
import com.team420.kekhunter.utils.ShellExecuter;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.io.File;


public class KaliGpsServiceFragment extends Fragment implements KaliGPSUpdates.Receiver {

    private static final String TAG = "KaliGpsServiceFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private KaliGPSUpdates.Provider gpsProvider = null;
    private TextView gpsTextView;
    private Context context;
    private boolean wantKismet = false;
    private boolean wantHelpView = true;
    private boolean reattachedToRunningService = false;
    private Switch switch_gps_provider = null;
    private Switch switch_gpsd = null;
    private Button button_launch_app = null;

    public KaliGpsServiceFragment() {
    }

    public static KaliGpsServiceFragment newInstance(int sectionNumber) {
        KaliGpsServiceFragment fragment = new KaliGpsServiceFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.gps, container, false);

        return rootView;
    }

    private void setCheckedQuietly(CompoundButton button, boolean state) {
        button.setTag("quiet");
        button.setChecked(state);
        button.setTag(null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gpsTextView = view.findViewById(R.id.gps_textview);
        TextView gpsHelpView = view.findViewById(R.id.gps_help);
        switch_gps_provider = view.findViewById(R.id.switch_gps_provider);
        switch_gpsd = view.findViewById(R.id.switch_gpsd);
        button_launch_app = view.findViewById(R.id.gps_button_launch_app);
        // TODO: make this text dynamic so we can launch other apps besides Kismet
        button_launch_app.setText("Launch Kismet in NH Terminal");
        if(!wantHelpView)
            gpsHelpView.setVisibility(View.GONE);
        Log.d(TAG, "reattachedToRunningService: " + reattachedToRunningService);
        if(reattachedToRunningService) {
            // gpsTextView.append("Service already running\n");
            setCheckedQuietly(switch_gps_provider, true);
        }

        // check if gpsd is already running
        check_gpsd();

        switch_gps_provider.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(switch_gps_provider.getTag() != null)
                    return;
                Log.d(TAG, "switch_gps_provider clicked: " + isChecked);
                if(isChecked) {
                    startGpsProvider();
                } else {
                    stopGpsProvider();
                }
            }
        });

        switch_gpsd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(switch_gpsd.getTag() != null)
                    return;
                Log.d(TAG, "switch_gpsd clicked: " + isChecked);
                if (isChecked) {
                    startChrootGpsd();
                } else {
                    stopChrootGpsd();
                }
            }
        });

        button_launch_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!switch_gps_provider.isChecked()) {
                    gpsTextView.append("Android GPS Provider not running!\n");
                    switch_gps_provider.setChecked(true);
                    startGpsProvider();
                }
                if (!switch_gpsd.isChecked()) {
                    gpsTextView.append("chroot gpsd not running!\n");
                    switch_gpsd.setChecked(true);
                    startChrootGpsd();
                }
                wantKismet = true;
                gpsTextView.append("Kismet will launch after next position received.  Waiting...\n");
            }
        });

    }

    private void startGpsProvider() {
        if (gpsProvider != null) {
            gpsTextView.append("Starting Android GPS Publisher\n");
            gpsTextView.append("GPS NMEA messages will be sent to udp://127.0.0.1:" + NhPaths.GPS_PORT + "\n");
            gpsProvider.onLocationUpdatesRequested(KaliGpsServiceFragment.this);
        }
    }

    private void stopGpsProvider() {
        if (gpsProvider != null) {
            gpsTextView.append("Stopping Android GPS Publisher\n");
            gpsProvider.onStopRequested();
        }
    }

    private void startChrootGpsd() {
        gpsTextView.append("Starting gpsd in Kali chroot\n");
        // do this in a thread because it takes a second or two and lags the UI
        new Thread(() -> {
            ShellExecuter exe = new ShellExecuter();
            String command = "su -c '" + NhPaths.APP_SCRIPTS_PATH + File.separator + "bootkali start_gpsd " + String.valueOf(NhPaths.GPS_PORT) + "'";
            Log.d(TAG, command);
            String response = exe.RunAsRootOutput(command);
            Log.d(TAG, "Response = " + response);
        }).start();
    }

    private void stopChrootGpsd() {
        gpsTextView.append("Stopping gpsd in Kali chroot\n");
        // do this in a thread because it takes a second or two and lags the UI
        new Thread(() -> {
            ShellExecuter exe = new ShellExecuter();
            String command = "su -c '" + NhPaths.APP_SCRIPTS_PATH + File.separator + "stop-gpsd'";
            Log.d(TAG, command);
            exe.RunAsRootOutput(command);
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if(LocationUpdateService.isInstanceCreated()) {
            // a LocationUpdateService is already running
            setCheckedQuietly(switch_gps_provider, true);
            // make sure it has a handle to this fragment so it can display updates
            reattachedToRunningService = this.gpsProvider.onReceiverReattach(this);
        } else
        {
            setCheckedQuietly(switch_gps_provider, false);
        }

        // check if gpsd is already running
        check_gpsd();
    }

    private void check_gpsd() {
        ShellExecuter exe = new ShellExecuter();
        String command = "pgrep gpsd";
        Log.d(TAG, "command = " + command);
        String response = exe.RunAsRootOutput(command);
        Log.d(TAG, "response = '" + response + "'");
        if(response.length() > 0)
            setCheckedQuietly(switch_gpsd, true);
        else
            setCheckedQuietly(switch_gpsd, false);
    }

    @Override
    public void onAttach(Context context) {
        if (context instanceof KaliGPSUpdates.Provider) {
            this.gpsProvider = (KaliGPSUpdates.Provider) context;
            reattachedToRunningService = this.gpsProvider.onReceiverReattach(this);
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // we've already granted permissions, make the nag message go away
            wantHelpView = false;
        }

        super.onAttach(context);
    }

    @Override
    public void onPositionUpdate(String nmeaSentences) {
        CharSequence charSequence = gpsTextView.getText();
        int lineCnt = 0;
        int i;
        for(i=charSequence.length()-1; i >= 0; i--) {
            if (charSequence.charAt(i) == '\n')
                lineCnt++;
            if (lineCnt >= 20)
                break;
        }
        // delete anything more than X lines previous so this doesn't get huge
        if(i > 0) {
            gpsTextView.getEditableText().delete(0, i);
        }

        gpsTextView.append(nmeaSentences + "\n");
        if(wantKismet) {
            wantKismet = false;
            gpsTextView.append("Launching kismet in NetHunter Terminal\n");
            startKismet();
        }
    }

    @Override
    public void onFirstPositionUpdate() {
    }

    private void startKismet() {
        try {
            Intent intent =
                    new Intent("com.team420.kekterm.RUN_SCRIPT_NH");
            intent.addCategory(Intent.CATEGORY_DEFAULT);

            intent.putExtra("com.team420.kekterm.iInitialCommand", "/usr/bin/start-kismet");
            startActivity(intent);
        } catch (Exception e) {
            NhPaths.showMessage(context, getString(R.string.toast_install_terminal));
        }
    }
}

