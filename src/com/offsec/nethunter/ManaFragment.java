package com.offsec.nethunter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

//import android.app.Fragment;
//import android.support.v4.app.FragmentActivity;

public class ManaFragment extends Fragment {

    private ViewPager mViewPager;

    private Integer selectedScriptIndex = 0;
    private final CharSequence[] scripts = {"mana-nat-full", "mana-nat-simple", "mana-nat-bettercap", "mana-nat-simple-bdf", "hostapd-wpe", "hostapd-wpe-karma"};
    private static final String TAG = "ManaFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private String configFilePath;
    private Context context;
    private Activity activity;

    public static ManaFragment newInstance(int sectionNumber) {
        ManaFragment fragment = new ManaFragment();
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
        configFilePath = NhPaths.APP_SD_FILES_PATH + "/configs/hostapd-karma.conf";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.mana, container, false);
        TabsPagerAdapter tabsPagerAdapter = new TabsPagerAdapter(getChildFragmentManager());

        mViewPager = rootView.findViewById(R.id.pagerMana);
        mViewPager.setAdapter(tabsPagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                //actionBar.setSelectedNavigationItem(position);
                activity.invalidateOptionsMenu();
            }
        });

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mana, menu);
    }


    public void onPrepareOptionsMenu(Menu menu) {
        int pageNum = mViewPager.getCurrentItem();
        if (pageNum == 0) {
            menu.findItem(R.id.source_button).setVisible(true);
        } else {
            menu.findItem(R.id.source_button).setVisible(false);
        }
        activity.invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.start_service:
                startMana();
                return true;
            case R.id.stop_service:
                stopMana();
                return true;
            case R.id.source_button:
                Intent i = new Intent(activity, EditSourceActivity.class);
                i.putExtra("path", configFilePath);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startMana() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Script to execute:");
        builder.setPositiveButton("Start", (dialog, which) -> {
            switch (selectedScriptIndex) {
                // launching mana on the terminal so it doesnt die suddenly
                case 0:
                    NhPaths.showMessage(context, "Starting MANA NAT FULL");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        intentClickListener_NH(NhPaths.makeTermTitle("MANA-FULL") + "/usr/share/mana-toolkit/run-mana/start-nat-full-lollipop.sh");
                    } else {
                        intentClickListener_NH(NhPaths.makeTermTitle("MANA-FULL") + "/usr/share/mana-toolkit/run-mana/start-nat-full-kitkat.sh");
                    }
                    break;
                case 1:
                    NhPaths.showMessage(context, "Starting MANA NAT SIMPLE");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        intentClickListener_NH(NhPaths.makeTermTitle("MANA-SIMPLE") + "/usr/share/mana-toolkit/run-mana/start-nat-simple-lollipop.sh");
                    } else {
                        intentClickListener_NH(NhPaths.makeTermTitle("MANA-SIMPLE") + "/usr/share/mana-toolkit/run-mana/start-nat-simple-kitkat.sh");
                    }
                    break;
                case 2:
                    NhPaths.showMessage(context, "Starting MANA Bettercap");
                    intentClickListener_NH(NhPaths.makeTermTitle("MANA-BETTERCAP") + "/usr/bin/start-nat-transproxy-lollipop.sh");
                    break;
                case 3:
                    NhPaths.showMessage(context, "Starting MANA NAT SIMPLE && BDF");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        intentClickListener_NH(NhPaths.makeTermTitle("MANA-BDF") + "/usr/share/mana-toolkit/run-mana/start-nat-simple-bdf-lollipop.sh");
                    } else {
                        intentClickListener_NH(NhPaths.makeTermTitle("MANA-BDF") + "/usr/share/mana-toolkit/run-mana/start-nat-simple-bdf-kitkat.sh");
                    }
                    // we wait ~10 secs before launching msf
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    NhPaths.showMessage(context, "Starting MSF with BDF resource.rc");
                                    intentClickListener_NH(NhPaths.makeTermTitle("MSF") + "msfconsole -q -r /usr/share/bdfproxy/bdfproxy_msf_resource.rc");
                                }
                            }, 10000);
                    break;
                case 4:
                    NhPaths.showMessage(context, "Starting HOSTAPD-WPE");
                    intentClickListener_NH(NhPaths.makeTermTitle("HOSTAPD-WPE") + "ifconfig wlan1 up && /usr/bin/hostapd-wpe /sdcard/nh_files/configs/hostapd-wpe.conf");
                    break;
                case 5:
                    NhPaths.showMessage(context, "Starting HOSTAPD-WPE with Karma");
                    intentClickListener_NH(NhPaths.makeTermTitle("HOSTAPD-WPE-KARMA") + "ifconfig wlan1 up && /usr/bin/hostapd-wpe -k /sdcard/nh_files/configs/hostapd-wpe.conf");
                    break;
                default:
                    NhPaths.showMessage(context, "Invalid script!");
                    return;
            }
            NhPaths.showMessage(context, getString(R.string.attack_launched));
        });
        builder.setNegativeButton("Quit", (dialog, which) -> {
        });
        builder.setSingleChoiceItems(scripts, selectedScriptIndex, (dialog, which) -> selectedScriptIndex = which);
        builder.show();

    }

    private void stopMana() {
        ShellExecuter exe = new ShellExecuter();
        String[] command = new String[1];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            command[0] = "su -c '" + NhPaths.APP_SCRIPTS_PATH + "/bootkali mana-lollipop stop'";
        } else {
            command[0] = "su -c '" + NhPaths.APP_SCRIPTS_PATH + "/bootkali mana-kitkat stop'";
        }
        exe.RunAsRoot(command);
        NhPaths.showMessage(context, "Mana Stopped");
    }

    public class TabsPagerAdapter extends FragmentPagerAdapter {


        TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public int getCount() {
            return 8;
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new HostapdFragment();
                case 1:
                    return new HostapdFragmentWPE();
                case 2:
                    return new DhcpdFragment();
                case 3:
                    return new DnsspoofFragment();
                case 4:
                    return new ManaNatFullFragment();
                case 5:
                    return new ManaNatSimpleFragment();
                case 6:
                    return new ManaNatBettercapFragment();
                case 7:
                    return new BdfProxyConfigFragment();
                default:
                    return new ManaStartNatSimpleBdfFragment();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "hostapd-karma.conf";
                case 1:
                    return "hostapd-wpe.conf";
                case 2:
                    return "dhcpd.conf";
                case 3:
                    return "dnsspoof.conf";
                case 4:
                    return "nat-mana-full";
                case 5:
                    return "nat-mana-simple";
                case 6:
                    return "nat-mana-bettercap";
                case 7:
                    return "bdfproxy.cfg";
                default:
                    return "mana-nat-simple-bdf";
            }
        }
    } //end class


    public static class HostapdFragment extends Fragment {

        private Context context;
        private String configFilePath;
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = getContext();
            configFilePath = NhPaths.APP_SD_FILES_PATH + "/configs/hostapd-karma.conf";
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.mana_hostapd, container, false);
            Button button = rootView.findViewById(R.id.updateButton);
            loadOptions(rootView);
            button.setOnClickListener(v -> {
                ShellExecuter exe = new ShellExecuter();
                File file = new File(configFilePath);
                String source = null;
                try {
                    source = Files.toString(file, Charsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (getView() == null) {
                    return;
                }
                EditText ifc = getView().findViewById(R.id.ifc);
                EditText bssid = getView().findViewById(R.id.bssid);
                EditText ssid = getView().findViewById(R.id.ssid);
                EditText channel = getView().findViewById(R.id.channel);
                EditText enableKarma = getView().findViewById(R.id.enable_karma);
                EditText karmaLoud = getView().findViewById(R.id.karma_loud);
                // FIXED BY BINKYBEAR <3
                if (source != null) {
                    source = source.replaceAll("(?m)^interface=(.*)$", "interface=" + ifc.getText().toString());
                    source = source.replaceAll("(?m)^bssid=(.*)$", "bssid=" + bssid.getText().toString());
                    source = source.replaceAll("(?m)^ssid=(.*)$", "ssid=" + ssid.getText().toString());
                    source = source.replaceAll("(?m)^channel=(.*)$", "channel=" + channel.getText().toString());
                    source = source.replaceAll("(?m)^enable_mana=(.*)$", "enable_mana=" + enableKarma.getText().toString());
                    source = source.replaceAll("(?m)^mana_loud=(.*)$", "mana_loud=" + karmaLoud.getText().toString());

                    exe.SaveFileContents(source, configFilePath);
                    NhPaths.showMessage(context, "Source updated");
                }

            });
            return rootView;
        }


        private void loadOptions(View rootView) {

            final EditText ifc = rootView.findViewById(R.id.ifc);
            final EditText bssid = rootView.findViewById(R.id.bssid);
            final EditText ssid = rootView.findViewById(R.id.ssid);
            final EditText channel = rootView.findViewById(R.id.channel);
            final EditText enableKarma = rootView.findViewById(R.id.enable_karma);
            final EditText karmaLoud = rootView.findViewById(R.id.karma_loud);

            new Thread(() -> {
                ShellExecuter exe = new ShellExecuter();
                String text = exe.ReadFile_SYNC(configFilePath);

                String regExpatInterface = "^interface=(.*)$";
                Pattern patternIfc = Pattern.compile(regExpatInterface, Pattern.MULTILINE);
                final Matcher matcherIfc = patternIfc.matcher(text);

                String regExpatbssid = "^bssid=(.*)$";
                Pattern patternBssid = Pattern.compile(regExpatbssid, Pattern.MULTILINE);
                final Matcher matcherBssid = patternBssid.matcher(text);

                String regExpatssid = "^ssid=(.*)$";
                Pattern patternSsid = Pattern.compile(regExpatssid, Pattern.MULTILINE);
                final Matcher matcherSsid = patternSsid.matcher(text);

                String regExpatChannel = "^channel=(.*)$";
                Pattern patternChannel = Pattern.compile(regExpatChannel, Pattern.MULTILINE);
                final Matcher matcherChannel = patternChannel.matcher(text);

                String regExpatEnableKarma = "^enable_mana=(.*)$";
                Pattern patternEnableKarma = Pattern.compile(regExpatEnableKarma, Pattern.MULTILINE);
                final Matcher matcherEnableKarma = patternEnableKarma.matcher(text);

                String regExpatKarmaLoud = "^mana_loud=(.*)$";
                Pattern patternKarmaLoud = Pattern.compile(regExpatKarmaLoud, Pattern.MULTILINE);
                final Matcher matcherKarmaLoud = patternKarmaLoud.matcher(text);

                ifc.post(new Runnable() {
                    @Override
                    public void run() {
                    /*
                     * Interface
                     */
                        if (matcherIfc.find()) {
                            String ifcValue = matcherIfc.group(1);
                            ifc.setText(ifcValue);
                        }
                    /*
                     * bssid
                     */
                        if (matcherBssid.find()) {
                            String bssidVal = matcherBssid.group(1);
                            bssid.setText(bssidVal);
                        }
                    /*
                     * ssid
                     */
                        if (matcherSsid.find()) {
                            String ssidVal = matcherSsid.group(1);
                            ssid.setText(ssidVal);
                        }
                    /*
                     * channel
                     */
                        if (matcherChannel.find()) {
                            String channelVal = matcherChannel.group(1);
                            channel.setText(channelVal);
                        }
                    /*
                     * enable_mana
                     */
                        if (matcherEnableKarma.find()) {
                            String enableKarmaVal = matcherEnableKarma.group(1);
                            enableKarma.setText(enableKarmaVal);
                        }
                   /*
                   * mana_loud
                   */
                        if (matcherKarmaLoud.find()) {
                            String karmaLoudVal = matcherKarmaLoud.group(1);
                            karmaLoud.setText(karmaLoudVal);
                        }
                    }
                });
            }).start();
        }

    }

    public static class HostapdFragmentWPE extends Fragment {

        private Context context;
        private String configFilePath;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = getContext();
            configFilePath = NhPaths.APP_SD_FILES_PATH + "/configs/hostapd-wpe.conf";
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.mana_hostapd_wpe, container, false);

            Button button = rootView.findViewById(R.id.wpe_updateButton);
            Button gencerts = rootView.findViewById(R.id.wpe_generate_certs);
            loadOptions(rootView);

            gencerts.setOnClickListener(v -> {
                Intent intent =
                        new Intent("com.offsec.nhterm.RUN_SCRIPT_NH");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.putExtra("com.offsec.nhterm.iInitialCommand", "cd /usr/share/hostapd-wpe/certs && ./bootstrap");
                startActivity(intent);
            });

            button.setOnClickListener(v -> {
                ShellExecuter exe = new ShellExecuter();
                File file = new File(configFilePath);
                String source = null;
                try {
                    source = Files.toString(file, Charsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (getView() == null) {
                    return;
                }
                EditText ifc = getView().findViewById(R.id.wpe_ifc);
                EditText bssid = getView().findViewById(R.id.wpe_bssid);
                EditText ssid = getView().findViewById(R.id.wpe_ssid);
                EditText channel = getView().findViewById(R.id.wpe_channel);
                EditText privatekey = getView().findViewById(R.id.wpe_private_key);

                if (source != null) {
                    source = source.replaceAll("(?m)^interface=(.*)$", "interface=" + ifc.getText().toString());
                    source = source.replaceAll("(?m)^bssid=(.*)$", "bssid=" + bssid.getText().toString());
                    source = source.replaceAll("(?m)^ssid=(.*)$", "ssid=" + ssid.getText().toString());
                    source = source.replaceAll("(?m)^channel=(.*)$", "channel=" + channel.getText().toString());
                    source = source.replaceAll("(?m)^private_key_passwd=(.*)$", "private_key_passwd=" + privatekey.getText().toString());

                    exe.SaveFileContents(source, configFilePath);
                    NhPaths.showMessage(context, "Source updated");
                }

            });
            return rootView;
        }


        private void loadOptions(View rootView) {

            final EditText ifc = rootView.findViewById(R.id.wpe_ifc);
            final EditText bssid = rootView.findViewById(R.id.wpe_bssid);
            final EditText ssid = rootView.findViewById(R.id.wpe_ssid);
            final EditText channel = rootView.findViewById(R.id.wpe_channel);
            final EditText privatekey = rootView.findViewById(R.id.wpe_private_key);

            new Thread(() -> {
                ShellExecuter exe = new ShellExecuter();
                Log.d("exe: ", configFilePath);
                String text = exe.ReadFile_SYNC(configFilePath);

                String regExpatInterface = "^interface=(.*)$";
                Pattern patternIfc = Pattern.compile(regExpatInterface, Pattern.MULTILINE);
                final Matcher matcherIfc = patternIfc.matcher(text);

                String regExpatbssid = "^bssid=(.*)$";
                Pattern patternBssid = Pattern.compile(regExpatbssid, Pattern.MULTILINE);
                final Matcher matcherBssid = patternBssid.matcher(text);

                String regExpatssid = "^ssid=(.*)$";
                Pattern patternSsid = Pattern.compile(regExpatssid, Pattern.MULTILINE);
                final Matcher matcherSsid = patternSsid.matcher(text);

                String regExpatChannel = "^channel=(.*)$";
                Pattern patternChannel = Pattern.compile(regExpatChannel, Pattern.MULTILINE);
                final Matcher matcherChannel = patternChannel.matcher(text);

                String regExpatEnablePrivateKey = "^private_key_passwd=(.*)$";
                Pattern patternEnablePrivateKey = Pattern.compile(regExpatEnablePrivateKey, Pattern.MULTILINE);
                final Matcher matcherPrivateKey = patternEnablePrivateKey.matcher(text);

                ifc.post(new Runnable() {
                    @Override
                    public void run() {
                    /*
                     * Interface
                     */
                        if (matcherIfc.find()) {
                            String ifcValue = matcherIfc.group(1);
                            ifc.setText(ifcValue);
                        }
                    /*
                     * bssid
                     */
                        if (matcherBssid.find()) {
                            String bssidVal = matcherBssid.group(1);
                            bssid.setText(bssidVal);
                        }
                    /*
                     * ssid
                     */
                        if (matcherSsid.find()) {
                            String ssidVal = matcherSsid.group(1);
                            ssid.setText(ssidVal);
                        }
                    /*
                     * channel
                     */
                        if (matcherChannel.find()) {
                            String channelVal = matcherChannel.group(1);
                            channel.setText(channelVal);
                        }
                    /*
                     * Private Key File
                     */
                        if (matcherPrivateKey.find()) {
                            String PrivateKeyVal = matcherPrivateKey.group(1);
                            privatekey.setText(PrivateKeyVal);
                        }
                    }
                });
            }).start();
        }

    }

    public static class DhcpdFragment extends Fragment {

        final ShellExecuter exe = new ShellExecuter();
        private Context context;
        private String configFilePath;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = getContext();
            configFilePath = NhPaths.CHROOT_PATH + "/etc/dhcp/dhcpd.conf";
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.source_short, container, false);

            String description = getResources().getString(R.string.mana_dhcpd);
            TextView desc = rootView.findViewById(R.id.description);
            desc.setText(description);

            EditText source = rootView.findViewById(R.id.source);
            exe.ReadFile_ASYNC(configFilePath, source);
            Button button = rootView.findViewById(R.id.update);
            button.setOnClickListener(v -> {
                Boolean isSaved = exe.SaveFileContents(source.getText().toString(), configFilePath);
                if (isSaved) {
                    NhPaths.showMessage(context, "Source updated");
                } else {
                    NhPaths.showMessage(context, "Source not updated");
                }
            });
            return rootView;
        }
    }

    public static class DnsspoofFragment extends Fragment {
        private Context context;
        private String configFilePath;
        final ShellExecuter exe = new ShellExecuter();

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = getContext();
            configFilePath = NhPaths.CHROOT_PATH + "/etc/mana-toolkit/dnsspoof.conf";
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.source_short, container, false);
            String description = getResources().getString(R.string.mana_dnsspoof);
            TextView desc = rootView.findViewById(R.id.description);
            desc.setText(description);

            EditText source = rootView.findViewById(R.id.source);
            exe.ReadFile_ASYNC(configFilePath, source);

            Button button = rootView.findViewById(R.id.update);
            button.setOnClickListener(v -> {
                if (getView() == null) {
                    return;
                }
                EditText source1 = getView().findViewById(R.id.source);
                String newSource = source1.getText().toString();
                exe.SaveFileContents(newSource, configFilePath);
                NhPaths.showMessage(context, "Source updated");
            });
            return rootView;
        }
    }

    public static class ManaNatFullFragment extends Fragment {
        private Context context;
        private String configFilePath;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = getContext();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                configFilePath = NhPaths.CHROOT_PATH + "/usr/share/mana-toolkit/run-mana/start-nat-full-lollipop.sh";
            } else {
                configFilePath = NhPaths.CHROOT_PATH + "/usr/share/mana-toolkit/run-mana/start-nat-full-kitkat.sh";
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.source_short, container, false);
            TextView desc = rootView.findViewById(R.id.description);

            desc.setText(getResources().getString(R.string.mana_nat_full));

            EditText source = rootView.findViewById(R.id.source);
            ShellExecuter exe = new ShellExecuter();
            exe.ReadFile_ASYNC(configFilePath, source);
            Button button = rootView.findViewById(R.id.update);
            button.setOnClickListener(v -> {
                if (getView() == null) {
                    return;
                }
                EditText source1 = getView().findViewById(R.id.source);
                String newSource = source1.getText().toString();
                ShellExecuter exe1 = new ShellExecuter();
                exe1.SaveFileContents(newSource, configFilePath);
                NhPaths.showMessage(context, "Source updated");
            });
            return rootView;
        }
    }

    public static class ManaNatSimpleFragment extends Fragment {
        private Context context;
        private String configFilePath;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = getContext();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                configFilePath = NhPaths.CHROOT_PATH + "/usr/share/mana-toolkit/run-mana/start-nat-simple-lollipop.sh";
            } else {
                configFilePath = NhPaths.CHROOT_PATH + "/usr/share/mana-toolkit/run-mana/start-nat-simple-kitkat.sh";
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.source_short, container, false);

            String description = getResources().getString(R.string.mana_nat_simple);
            TextView desc = rootView.findViewById(R.id.description);
            desc.setText(description);


            EditText source = rootView.findViewById(R.id.source);
            ShellExecuter exe = new ShellExecuter();
            exe.ReadFile_ASYNC(configFilePath, source);

            Button button = rootView.findViewById(R.id.update);
            button.setOnClickListener(v -> {
                if (getView() == null) {
                    return;
                }
                EditText source1 = getView().findViewById(R.id.source);
                String newSource = source1.getText().toString();
                ShellExecuter exe1 = new ShellExecuter();
                exe1.SaveFileContents(newSource, configFilePath);
                NhPaths.showMessage(context, "Source updated");
            });
            return rootView;
        }
    }

    public static class ManaNatBettercapFragment extends Fragment {
        private Context context;
        private String configFilePath;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = getContext();
            configFilePath = NhPaths.CHROOT_PATH + "/usr/bin/start-nat-transproxy-lollipop.sh";
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.source_short, container, false);



            String description = getResources().getString(R.string.mana_bettercap_description);
            TextView desc = rootView.findViewById(R.id.description);
            desc.setText(description);

            EditText source = rootView.findViewById(R.id.source);
            ShellExecuter exe = new ShellExecuter();
            exe.ReadFile_ASYNC(configFilePath, source);

            Button button = rootView.findViewById(R.id.update);
            button.setOnClickListener(v -> {
                if (getView() == null) {
                    return;
                }
                EditText source1 = getView().findViewById(R.id.source);
                String newSource = source1.getText().toString();
                ShellExecuter exe1 = new ShellExecuter();
                exe1.SaveFileContents(newSource, configFilePath);
                NhPaths.showMessage(context, "Source updated");
            });
            return rootView;
        }
    }

    public static class BdfProxyConfigFragment extends Fragment {
        private Context context;
        private String configFilePath;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = getContext();
            configFilePath = NhPaths.APP_SD_FILES_PATH + "/configs/bdfproxy.cfg";
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.source_short, container, false);

            String description = getResources().getString(R.string.bdfproxy_cfg);
            TextView desc = rootView.findViewById(R.id.description);
            desc.setText(description);
            // use the good one?
            Log.d("BDFPATH", configFilePath);
            EditText source = rootView.findViewById(R.id.source);
            ShellExecuter exe = new ShellExecuter();
            exe.ReadFile_ASYNC(configFilePath, source);

            Button button = rootView.findViewById(R.id.update);
            button.setOnClickListener(v -> {
                if (getView() == null) {
                    return;
                }
                EditText source1 = getView().findViewById(R.id.source);
                String newSource = source1.getText().toString();
                ShellExecuter exe1 = new ShellExecuter();
                exe1.SaveFileContents(newSource, configFilePath);
                NhPaths.showMessage(context, "Source updated");
            });
            return rootView;
        }
    }

    public static class ManaStartNatSimpleBdfFragment extends Fragment {
        private Context context;
        private String configFilePath;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = getContext();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                configFilePath = NhPaths.CHROOT_PATH + "/usr/share/mana-toolkit/run-mana/start-nat-simple-bdf-lollipop.sh";
            } else {
                configFilePath = NhPaths.CHROOT_PATH + "/usr/share/mana-toolkit/run-mana/start-nat-simple-bdf-kitkat.sh";
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.source_short, container, false);

            String description = getResources().getString(R.string.mana_nat_simple_bdf);
            TextView desc = rootView.findViewById(R.id.description);
            desc.setText(description);
            EditText source = rootView.findViewById(R.id.source);
            ShellExecuter exe = new ShellExecuter();
            exe.ReadFile_ASYNC(configFilePath, source);
            Button button = rootView.findViewById(R.id.update);
            button.setOnClickListener(v -> {
                if (getView() == null) {
                    return;
                }
                EditText source1 = getView().findViewById(R.id.source);
                String newSource = source1.getText().toString();
                ShellExecuter exe1 = new ShellExecuter();
                exe1.SaveFileContents(newSource, configFilePath);
                NhPaths.showMessage(context, "Source updated");
            });
            return rootView;
        }
    }

    private void intentClickListener_NH(final String command) {
        try {
            Intent intent =
                    new Intent("com.offsec.nhterm.RUN_SCRIPT_NH");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("com.offsec.nhterm.iInitialCommand", command);
            startActivity(intent);
        } catch (Exception e) {
            NhPaths.showMessage(context, getString(R.string.toast_install_terminal));
        }
    }
}
