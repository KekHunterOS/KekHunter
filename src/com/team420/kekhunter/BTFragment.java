package com.team420.kekhunter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.team420.kekhunter.utils.NhPaths;
import com.team420.kekhunter.utils.ShellExecuter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class BTFragment extends Fragment {

    private ViewPager mViewPager;
    private SharedPreferences sharedpreferences;
    private NhPaths nh;
    private Context context;
    private Activity activity;
    private static final String ARG_SECTION_NUMBER = "section_number";

    public static BTFragment newInstance(int sectionNumber) {
        BTFragment fragment = new BTFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.bt, container, false);
        BTFragment.TabsPagerAdapter tabsPagerAdapter = new TabsPagerAdapter(getChildFragmentManager());

        mViewPager = rootView.findViewById(R.id.pagerBt);
        mViewPager.setAdapter(tabsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                activity.invalidateOptionsMenu();
            }
        });
        sharedpreferences = activity.getSharedPreferences("com.team420.kekhunter", Context.MODE_PRIVATE);
        setHasOptionsMenu(true);
        return rootView;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuinflater) {
        menuinflater.inflate(R.menu.bt, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setup:
                RunSetup();
                return true;
            case R.id.update:
                RunUpdate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void SetupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        sharedpreferences = activity.getSharedPreferences("com.team420.kekhunter", Context.MODE_PRIVATE);
        builder.setTitle("Welcome to Bluetooth Arsenal!");
        builder.setMessage("In order to make sure everything is working, an initial setup needs to be done.");
        builder.setPositiveButton("Check & Install", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which) {
                RunSetup();
                sharedpreferences.edit().putBoolean("setup_done", true).apply();
            }
        });
        builder.show();

    }

    public void RunSetup() {
        sharedpreferences = activity.getSharedPreferences("com.team420.kekhunter", Context.MODE_PRIVATE);
                intentClickListener_NH("echo -ne \"\\033]0;BT Arsenal Setup\\007\" && clear;if [[ -f /usr/bin/hciconfig && -f /usr/bin/l2ping && " +
                        "-f /usr/bin/fang && -f /usr/bin/blueranger &&-f /usr/bin/bluelog && -f /usr/bin/sdptool && -f /usr/bin/spooftooph && -f /usr/bin/sox ]];then echo \"All packages are installed!\"; else " +
                        "apt-get update && apt-get install bluetooth bluez bluez-tools bluez-obexd libbluetooth3 sox spooftooph " +
                        "libbluetooth-dev redfang bluelog blueranger -y;fi; if [[ -f /usr/bin/carwhisperer && -f /usr/bin/rfcomm_scan ]];then echo \"All scripts are installed!\"; else " +
                        "git clone https://github.com/yesimxev/carwhisperer-0.2 /root/carwhisperer;" +
                        "cd /root/carwhisperer;make && make install;git clone https://github.com/yesimxev/bt_audit /root/bt_audit;cd /root/bt_audit/src;make;" +
                        "cp rfcomm_scan /usr/bin/;fi; echo \"Everything is installed! Closing in 3secs..\"; sleep 3 && exit ");
                sharedpreferences.edit().putBoolean("setup_done", true).apply();
    }

    public void RunUpdate() {
        sharedpreferences = activity.getSharedPreferences("com.team420.kekhunter", Context.MODE_PRIVATE);
        intentClickListener_NH("echo -ne \"\\033]0;BT Arsenal Update\\007\" && clear;apt-get update && apt-get install bluetooth bluez bluez-tools bluez-obexd libbluetooth3 sox spooftooph " +
                "libbluetooth-dev redfang bluelog blueranger -y;if [[ -f /usr/bin/carwhisperer && -f /usr/bin/rfcomm_scan ]];then cd /root/carwhisperer/;git pull && make && make install;cd /root/bt_audit; git pull; cd src && make;" +
                "cp rfcomm_scan /usr/bin/;fi; echo \"Done! Closing in 3secs..\"; sleep 3 && exit ");
        sharedpreferences.edit().putBoolean("setup_done", true).apply();
    }

    public static class TabsPagerAdapter extends FragmentPagerAdapter {

        TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new MainFragment();
                case 1:
                    return new ToolsFragment();
                case 2:
                    return new SpoofFragment();
                default:
                    return new CWFragment();
            }
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 3:
                    return "Carwhisperer";
                case 2:
                    return "Spoof";
                case 1:
                    return "Tools";
                case 0:
                    return "Main Page";
                default:
                    return "";
            }
        }
    }

    public static class MainFragment extends BTFragment {
        private Context context;
        private NhPaths nh;
        final ShellExecuter exe = new ShellExecuter();
        private String selected_iface;
        String selected_addr;
        String selected_class;
        String selected_name;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = getContext();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.bt_main, container, false);
            SharedPreferences sharedpreferences = context.getSharedPreferences("com.team420.kekhunter", Context.MODE_PRIVATE);

            //First run
            Boolean setupdone = sharedpreferences.getBoolean("setup_done", false);
            if (!setupdone.equals(true))
                SetupDialog();

            final TextView DBUSstatus = rootView.findViewById(R.id.DBUSstatus);
            final TextView BTstatus = rootView.findViewById(R.id.BTstatus);
            final TextView HCIstatus = rootView.findViewById(R.id.HCIstatus);
            final Spinner ifaces = rootView.findViewById(R.id.hci_interface);

            //Bluetooth interfaces
            final String[] outputHCI = {""};
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    outputHCI[0] = exe.RunAsRootOutput("bootkali custom_cmd hciconfig | grep hci | cut -d: -f1");
                }
            });
            final ArrayList<String> hciIfaces = new ArrayList<>();
            if (outputHCI[0].equals("")) {
                hciIfaces.add("None");
                ifaces.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, hciIfaces));
            } else {
                final String[] ifacesArray = outputHCI[0].split("\n");
                ifaces.setAdapter(new ArrayAdapter(getContext(),android.R.layout.simple_list_item_1, ifacesArray));
            }

            ifaces.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                    selected_iface = parentView.getItemAtPosition(pos).toString();
                    sharedpreferences.edit().putInt("selected_iface", ifaces.getSelectedItemPosition()).apply();
                }
                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });

            //Refresh Status
            ImageButton RefreshStatus = rootView.findViewById(R.id.refreshStatus);
            RefreshStatus.setOnClickListener(v -> {
                refresh(rootView);
            });
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    refresh(rootView);
                }
            });

            final Switch dbusSwitch = rootView.findViewById(R.id.dbus_switch);
            final Switch btSwitch = rootView.findViewById(R.id.bt_switch);
            final Switch hciSwitch = rootView.findViewById(R.id.hci_switch);

            dbusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            exe.RunAsRoot(new String[]{"bootkali custom_cmd service dbus start"});
                            DBUSstatus.setText("Running");
                        } else {
                            exe.RunAsRoot(new String[]{"bootkali custom_cmd service dbus stop"});
                            DBUSstatus.setText("Stopped");
                        }
                }
            });
            btSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String dbus_statusCMD = exe.RunAsRootOutput("bootkali custom_cmd service dbus status | grep dbus");
                    if (dbus_statusCMD.equals("dbus is running.")) {
                        if (isChecked) {
                            exe.RunAsRoot(new String[]{"bootkali custom_cmd service bluetooth start"});
                            BTstatus.setText("Running");
                        } else {
                            exe.RunAsRoot(new String[]{"bootkali custom_cmd service bluetooth stop"});
                            BTstatus.setText("Stopped");
                        }
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "Enable dbus service first!", Toast.LENGTH_SHORT).show();
                        btSwitch.setChecked(false);
                    }
                }
            });
            hciSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        exe.RunAsRoot(new String[]{"bootkali custom_cmd hciconfig " + selected_iface + " up noscan"});
                        HCIstatus.setText("Up");
                    } else {
                        exe.RunAsRoot(new String[]{"bootkali custom_cmd hciconfig " + selected_iface + " down"});
                        HCIstatus.setText("Down");
                    }
                }
            });

            //Scanning
            Button StartScanButton = rootView.findViewById(R.id.start_scan);
            final TextView BTtime = rootView.findViewById(R.id.bt_time);
            ListView targets = rootView.findViewById(R.id.targets);
            ShellExecuter exe = new ShellExecuter();
            File ScanLog = new File(nh.CHROOT_PATH() + "/root/blue.log");
            StartScanButton.setOnClickListener( v -> {
                if (!selected_iface.equals("None")) {
                    String hci_current = exe.RunAsRootOutput("bootkali custom_cmd hciconfig "+ selected_iface + " | grep \"UP RUNNING\" | cut -f2 -d$'\\t'");
                    if (hci_current.equals("UP RUNNING ")) {
                        final String scantime = BTtime.getText().toString();
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        final ArrayList<String> scanning = new ArrayList<>();
                                        scanning.add("Scanning..");
                                        targets.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, scanning));
                                    }
                                });
                                exe.RunAsRoot(new String[]{"bootkali custom_cmd rm /root/blue.log"});
                                exe.RunAsRoot(new String[]{"bootkali custom_cmd timeout " + scantime + " bluelog -i " + selected_iface + " -ncqo /root/blue.log;hciconfig " + selected_iface + " noscan"});
                                 getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String outputScanLog = exe.RunAsRootOutput("cat " + ScanLog);
                                        final String[] targetsArray = outputScanLog.split("\n");
                                        ArrayAdapter targetsadapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, targetsArray);
                                        if (!outputScanLog.equals("")) {
                                            targets.setAdapter(targetsadapter);
                                        } else {
                                            final ArrayList<String> notargets = new ArrayList<>();
                                            notargets.add("No devices found");
                                            targets.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, notargets));
                                        }
                                    }
                                });
                            }
                        });
                    } else
                        Toast.makeText(getActivity().getApplicationContext(), "Interface is down!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getActivity().getApplicationContext(), "No interface selected!", Toast.LENGTH_SHORT).show();
                }
            });

            //Target selection
            targets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String selected_target = targets.getItemAtPosition(i).toString();
                    if (selected_target.equals("No devices found"))
                        Toast.makeText(getActivity().getApplicationContext(), "No target!", Toast.LENGTH_SHORT).show();
                    else {
                        selected_addr = exe.RunAsRootOutput("echo " + selected_target + " | cut -d , -f 1");
                        selected_class = exe.RunAsRootOutput("echo " + selected_target + " | cut -d , -f 2");
                        selected_name = exe.RunAsRootOutput("echo " + selected_target + " | cut -d , -f 3");
                        PreferencesData.saveString(context, "selected_address", selected_addr);
                        PreferencesData.saveString(context, "selected_class", selected_class);
                        PreferencesData.saveString(context, "selected_name", selected_name);
                        Toast.makeText(getActivity().getApplicationContext(), "Target selected!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            return rootView;
        }

        private void refresh(View BTFragment) {

            final TextView DBUSstatus = BTFragment.findViewById(R.id.DBUSstatus);
            final TextView BTstatus = BTFragment.findViewById(R.id.BTstatus);
            final TextView HCIstatus = BTFragment.findViewById(R.id.HCIstatus);
            final Switch dbusSwitch = BTFragment.findViewById(R.id.dbus_switch);
            final Switch btSwitch = BTFragment.findViewById(R.id.bt_switch);
            final Switch hciSwitch = BTFragment.findViewById(R.id.hci_switch);
            final Spinner ifaces = BTFragment.findViewById(R.id.hci_interface);
            SharedPreferences sharedpreferences = context.getSharedPreferences("com.team420.kekhunter", Context.MODE_PRIVATE);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String dbus_statusCMD = exe.RunAsRootOutput("bootkali custom_cmd service dbus status | grep dbus");
                    if (dbus_statusCMD.equals("dbus is running.")) {
                        DBUSstatus.setText("Running");
                        dbusSwitch.setChecked(true);
                    }
                    else {
                        DBUSstatus.setText("Stopped");
                        dbusSwitch.setChecked(false);
                    }
                    String bt_statusCMD = exe.RunAsRootOutput("bootkali custom_cmd service bluetooth status | grep bluetooth");
                    if (bt_statusCMD.equals("bluetooth is running.")) {
                        BTstatus.setText("Running");
                        btSwitch.setChecked(true);
                    }
                    else {
                        BTstatus.setText("Stopped");
                        btSwitch.setChecked(false);
                    }
                    String hci_statusCMD = exe.RunAsRootOutput("bootkali custom_cmd hciconfig "+ selected_iface + " | grep \"UP RUNNING\" | cut -f2 -d$'\\t'");
                    if (hci_statusCMD.equals("UP RUNNING ")) {
                        HCIstatus.setText("Up");
                        hciSwitch.setChecked(true);
                    }
                    else {
                        HCIstatus.setText("Down");
                        hciSwitch.setChecked(false);
                    }
                    String outputHCI = exe.RunAsRootOutput("bootkali custom_cmd hciconfig | grep hci | cut -d: -f1");
                    final ArrayList<String> hciIfaces = new ArrayList<>();
                    if (outputHCI.equals("")) {
                        hciIfaces.add("None");
                        ifaces.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, hciIfaces));
                    } else {
                        final String[] ifacesArray = outputHCI.split("\n");
                        ifaces.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, ifacesArray));
                        Integer lastiface = sharedpreferences.getInt("selected_iface", 0);
                        ifaces.setSelection(lastiface);
                    }
                }
            });
        }
    }

    final void intentClickListener_NH(final String command) {
        try {
            Intent intent =
                    new Intent("com.team420.kekterm.RUN_SCRIPT_NH");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("com.team420.kekterm.iInitialCommand", command);
            startActivity(intent);
        } catch (Exception e) {
            nh.showMessage(context, getString(R.string.toast_install_terminal));

        }
    }

    public static class ToolsFragment extends BTFragment {
        private Context context;
        private Activity activity;
        final ShellExecuter exe = new ShellExecuter();
        private String reverse = "";
        private String flood = "";

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = getContext();
            activity = getActivity();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.bt_tools, container, false);
            final EditText hci_interface = rootView.findViewById(R.id.hci_interface);
            CheckBox floodCheckBox = rootView.findViewById(R.id.l2ping_flood);
            CheckBox reverseCheckBox = rootView.findViewById(R.id.l2ping_reverse);

            //Target address
            final EditText sdp_address = rootView.findViewById(R.id.sdp_address);

            //Set target
            Button SetTarget = rootView.findViewById(R.id.set_target);

            SetTarget.setOnClickListener( v -> {
                String selected_addr = PreferencesData.getString(context, "selected_address", "");
                sdp_address.setText(selected_addr);
            });

            //L2ping
            Button StartL2ping = rootView.findViewById(R.id.start_l2ping);
            final EditText l2ping_Size = rootView.findViewById(R.id.l2ping_size);
            final EditText l2ping_Count = rootView.findViewById(R.id.l2ping_count);
            final EditText redfang_Range = rootView.findViewById(R.id.redfang_range);
            final EditText redfang_Log = rootView.findViewById(R.id.redfang_log);

            // Checkbox for flood and reverse ping
            floodCheckBox.setOnClickListener( v -> {
                if (floodCheckBox.isChecked())
                    flood = " -f ";
                else
                    flood = "";
            });
            reverseCheckBox.setOnClickListener( v -> {
                if (reverseCheckBox.isChecked())
                    reverse = " -r ";
                else
                    reverse = "";
            });

            StartL2ping.setOnClickListener( v -> {
                String l2ping_target = sdp_address.getText().toString();
                if (!l2ping_target.equals("")) {
                    String l2ping_size = l2ping_Size.getText().toString();
                    String l2ping_count = l2ping_Count.getText().toString();
                    String l2ping_interface = hci_interface.getText().toString();
                    intentClickListener_NH("echo -ne \"\\033]0;Pinging BT device\\007\" && clear;l2ping -i " + l2ping_interface + " -s " + l2ping_size + " -c " + l2ping_count + flood + reverse + " " + l2ping_target + " && echo \"\nPinging done, closing in 3 secs..\";sleep 3 && exit");
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "No target address!", Toast.LENGTH_SHORT).show();
                }
            });

            //RFComm_scan
            Button StartRFCommscan = rootView.findViewById(R.id.start_rfcommscan);

            StartRFCommscan.setOnClickListener( v -> {
                String sdp_target = sdp_address.getText().toString();
                if (!sdp_target.equals(""))
                    intentClickListener_NH("echo -ne \"\\033]0;RFComm Scan\\007\" && clear;rfcomm_scan " + sdp_target);
                else
                    Toast.makeText(getActivity().getApplicationContext(), "No target address!", Toast.LENGTH_SHORT).show();
            });

            //Redfang
            Button StartRedfang = rootView.findViewById(R.id.start_redfang);

            StartRedfang.setOnClickListener( v -> {
                String redfang_range = redfang_Range.getText().toString();
                String redfang_logfile = redfang_Log.getText().toString();
                if (!redfang_range.equals(""))
                    intentClickListener_NH("echo -ne \"\\033]0;Redfang\\007\" && clear;fang -r " + redfang_range + " -o " + redfang_logfile);
                else
                    Toast.makeText(getActivity().getApplicationContext(), "No target range!", Toast.LENGTH_SHORT).show();
            });

            //Blueranger
            Button StartBlueranger = rootView.findViewById(R.id.start_blueranger);
            StartBlueranger.setOnClickListener( v -> {
                String blueranger_target = sdp_address.getText().toString();
                String blueranger_interface = hci_interface.getText().toString();
                if (!blueranger_target.equals(""))
                    intentClickListener_NH("echo -ne \"\\033]0;Blueranger\\007\" && clear;blueranger " + blueranger_interface + " " + blueranger_target);
                else
                    Toast.makeText(getActivity().getApplicationContext(), "No target address!", Toast.LENGTH_SHORT).show();
            });

            //Start SDP Tool
            Button StartSDPButton = rootView.findViewById(R.id.start_sdp);
            StartSDPButton.setOnClickListener( v -> {
                Toast.makeText(getContext(), "Discovery started..\nCheck the output below", Toast.LENGTH_SHORT).show();
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        startSDPtool(rootView);
                    }
                });
            });
            return rootView;
        }

        private void startSDPtool(View BTFragment) {

            final EditText sdp_address = BTFragment.findViewById(R.id.sdp_address);
            final EditText hci_interface = BTFragment.findViewById(R.id.hci_interface);
            final TextView output = BTFragment.findViewById(R.id.SDPoutput);
            ShellExecuter exe = new ShellExecuter();
            String sdp_target = sdp_address.getText().toString();
            String sdp_interface = hci_interface.getText().toString();

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!sdp_target.equals("")) {
                        String CMDout = exe.RunAsRootOutput("bootkali custom_cmd sdptool -i " + sdp_interface + " browse " + sdp_target + " | sed '/^\\[/d' | sed '/^Linux/d'");
                        output.setText(CMDout);
                    } else
                        Toast.makeText(getActivity().getApplicationContext(), "No target address!", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    public static class SpoofFragment extends BTFragment {
        private Context context;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = getContext();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.bt_spoof, container, false);

            //Selected iface
            final EditText spoof_interface = rootView.findViewById(R.id.spoof_interface);

            //Target address
            final EditText targetAddress = rootView.findViewById(R.id.targetAddress);

            //Target Class
            final EditText targetClass = rootView.findViewById(R.id.targetClass);

            //Target Name
            final EditText targetName = rootView.findViewById(R.id.targetName);

            //Set target
            Button SetTarget = rootView.findViewById(R.id.set_target);

            SetTarget.setOnClickListener(v -> {
                String selected_address = PreferencesData.getString(context, "selected_address", "");
                String selected_class = PreferencesData.getString(context, "selected_class", "");
                String selected_name = PreferencesData.getString(context, "selected_name", "");
                targetAddress.setText(selected_address);
                targetClass.setText(selected_class);
                targetName.setText(selected_name);
            });

            //Refresh
            Button RefreshStatus = rootView.findViewById(R.id.refreshSpoof);
            RefreshStatus.setOnClickListener(v -> {
                refreshSpoof(rootView);
            });

            //Apply
            Button ApplySpoof = rootView.findViewById(R.id.apply_spoof);

            ApplySpoof.setOnClickListener(v -> {
                String target_interface = spoof_interface.getText().toString();
                String target_address = " -a " + targetAddress.getText().toString();
                String target_class = " -c " + targetClass.getText().toString();
                String target_name = " -n \"" + targetName.getText().toString() + "\"";
                if (target_class.equals(" -c ")) target_class = "";
                if (target_name.equals(" -n \"\"")) target_name = "";
                if (target_address.equals(" -a ") && target_name.equals("") && target_class.equals("")) {
                    Toast.makeText(getActivity().getApplicationContext(), "Please enter at least one parameter!", Toast.LENGTH_SHORT).show();
                } else {
                    final String target_classname = target_class + target_name;
                    if (!target_address.equals(" -a ")) {
                        intentClickListener_NH("echo -ne \"\\033]0;Spoofing Bluetooth\\007\" && clear;echo \"Spooftooph started..\";spooftooph -i " + target_interface + target_address + "; sleep 2 && hciconfig " + target_interface + " up && spooftooph -i " + target_interface + target_classname + " && echo \"\nBringing interface up with hciconfig..\n\nClass/Name changed, closing in 3 secs..\";sleep 3 && exit");
                    } else {
                        intentClickListener_NH("echo -ne \"\\033]0;Spoofing Bluetooth\\007\" && clear;echo \"Spooftooph started..\";spooftooph -i " + target_interface + target_classname + " && echo \"\nClass/Name changed, closing in 3 secs..\";sleep 3 && exit");
                    }
                }
            });
            return rootView;
        }

        private void refreshSpoof(View BTFragment) {

            ShellExecuter exe = new ShellExecuter();
            final EditText spoof_interface = BTFragment.findViewById(R.id.spoof_interface);
            final TextView currentAddress = BTFragment.findViewById(R.id.currentAddress);
            final TextView currentClass = BTFragment.findViewById(R.id.currentClass);
            final TextView currentClassType = BTFragment.findViewById(R.id.currentClassType);
            final TextView currentName = BTFragment.findViewById(R.id.currentName);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String selectedIface = spoof_interface.getText().toString();
                    String currentAddress_CMD = exe.RunAsRootOutput("bootkali custom_cmd hciconfig " + selectedIface + " | awk '/Address/ { print $3 }'");
                    if (!currentAddress_CMD.equals("")) {
                        currentAddress.setText(currentAddress_CMD);

                        String currentClassCMD = exe.RunAsRootOutput("bootkali custom_cmd hciconfig " + selectedIface + " -a | awk '/Class:/ { print $2 }' | sed '/^Class:/d'");
                        currentClass.setText(currentClassCMD);

                        String currentClassTypeCMD = exe.RunAsRootOutput("bootkali custom_cmd hciconfig " + selectedIface + " -a | awk '/Device Class:/ { print $3, $4, $5 }'");
                        currentClassType.setText(currentClassTypeCMD);

                        String currentNameCMD = exe.RunAsRootOutput("bootkali custom_cmd hciconfig " + selectedIface + " -a | grep Name | cut -d\\\' -f2");
                        currentName.setText(currentNameCMD);
                    } else
                        Toast.makeText(getActivity().getApplicationContext(), "Interface is down!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static class CWFragment extends BTFragment {
        private Context context;
        private String selected_mode;
        private NhPaths nh;
        final ShellExecuter exe = new ShellExecuter();

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = getContext();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.bt_carwhisperer, container, false);

            //Selected iface
            final EditText cw_interface = rootView.findViewById(R.id.hci_interface);

            //Target address
            final EditText cw_address = rootView.findViewById(R.id.hci_address);

            //Set target
            Button SetTarget = rootView.findViewById(R.id.set_target);

            SetTarget.setOnClickListener( v -> {
                String selected_address = PreferencesData.getString(context, "selected_address", "");
                cw_address.setText(selected_address);
            });

            //Channel
            final EditText hci_channel = rootView.findViewById(R.id.hci_channel);

            //CW Mode
            Spinner cwmode = rootView.findViewById(R.id.cwmode);
            final ArrayList<String> modes = new ArrayList<>();
            modes.add("Listen");
            modes.add("Inject");
            cwmode.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, modes));
            cwmode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                    selected_mode = parentView.getItemAtPosition(pos).toString();
                }
                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });

            //Listening
            final EditText listenfilename = rootView.findViewById(R.id.listenfilename);

            //Injecting
            final EditText injectfilename = rootView.findViewById(R.id.injectfilename);
            final Button injectfilebrowse = rootView.findViewById(R.id.injectfilebrowse);

            injectfilebrowse.setOnClickListener( v -> {
                Intent intent = new Intent();
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("audio/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select audio file"),1001);
                });

            //Launch
            Button StartCWButton = rootView.findViewById(R.id.start_cw);
            StartCWButton.setOnClickListener( v -> {
                String cw_iface = cw_interface.getText().toString();
                String cw_target = cw_address.getText().toString();
                if (!cw_target.equals("")) {
                    String cw_channel = hci_channel.getText().toString();
                    String cw_listenfile = listenfilename.getText().toString();
                    String cw_injectfile = injectfilename.getText().toString();

                    if (selected_mode.equals("Listen")) {
                        intentClickListener_NH("echo -ne \"\\033]0;Listening BT audio\\007\" && clear;echo \"Carwhisperer starting..\nReturn to NetHunter to kill, or to listen live!\"$'\n';carwhisperer " + cw_iface + " /root/carwhisperer/in.raw /sdcard/rec.raw " + cw_target + " " + cw_channel + " && echo \"Converting to wav to target directory..\";sox -t raw -r 8000 -e signed -b 16 /sdcard/rec.raw -r 8000 -b 16 /sdcard/" + cw_listenfile + ";echo Done! || echo \"No convert file!\"");
                    } else if (selected_mode.equals("Inject")) {
                        intentClickListener_NH("echo -ne \"\\033]0;Injecting BT audio\\007\" && clear;echo \"Carwhisperer starting..\";length=$(($(soxi -D " + cw_injectfile + " | cut -d. -f1)+8));sox " + cw_injectfile + " -r 8000 -b 16 -c 1 tempi.raw && timeout $length " +
                                "carwhisperer " + cw_iface + " tempi.raw tempo.raw " + cw_target + " " + cw_channel + "; rm tempi.raw && rm tempo.raw;echo \"\nInjection done, closing in 3 secs..\";sleep 3 && exit");
                    }
                } else
                    Toast.makeText(getActivity().getApplicationContext(), "No target address!", Toast.LENGTH_SHORT).show();
            });

            //Kill
            Button StopCWButton = rootView.findViewById(R.id.stop_cw);
            StopCWButton.setOnClickListener( v -> {
                    exe.RunAsRoot(new String[]{"bootkali custom_cmd pkill carwhisperer"});
                    Toast.makeText(getActivity().getApplicationContext(), "Killed", Toast.LENGTH_SHORT).show();
                    });

            //Stream or play audio
            ImageButton PlayAudioButton = rootView.findViewById(R.id.play_audio);
            ImageButton StopAudioButton = rootView.findViewById(R.id.stop_audio);
            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 20000, AudioTrack.MODE_STREAM);
            PlayAudioButton.setOnClickListener( v -> {
                File cw_listenfile = new File(nh.SD_PATH + "/rec.raw");
                if (cw_listenfile.length() == 0) {
                    Toast.makeText(getContext(), "File not found!", Toast.LENGTH_SHORT).show();
                } else {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            InputStream s = null;
                            try {
                                s = new FileInputStream(cw_listenfile);
                            } catch (NullPointerException | FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            audioTrack.play();
                            // Reading data.
                            byte[] data = new byte[200];
                            int n = 0;
                            try {
                                while ((n = s.read(data)) != -1)
                                    synchronized (audioTrack) {
                                        audioTrack.write(data, 0, n);
                                    }
                            } catch (IOException e) {
                            }
                        }
                    });
                }
            });
            StopAudioButton.setOnClickListener(v -> {
                        audioTrack.pause();
                        audioTrack.flush();
            });
            return rootView;
        }
    }

    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 1001) {
                if (resultCode == Activity.RESULT_OK) {
                    ShellExecuter exe = new ShellExecuter();
                    EditText injectfilename = getActivity().findViewById(R.id.injectfilename);
                    String FilePath = data.getData().getPath();
                    FilePath = exe.RunAsRootOutput("echo " + FilePath + " | sed -e 's/\\/document\\/primary:/\\/sdcard\\//g' ");
                    injectfilename.setText(FilePath);
                }
            }
        }

    public static class PreferencesData {

        public static void saveString(Context context, String key, String value) {
            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            sharedPrefs.edit().putString(key, value).apply();
        }

        public static String getString(Context context, String key, String defaultValue) {
            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            return sharedPrefs.getString(key, defaultValue);
        }
    }

}