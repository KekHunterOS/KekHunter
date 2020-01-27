package com.offsec.nethunter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.Arrays;

public class VNCFragment extends Fragment {

    private static final String TAG = "VNCFragment";
    private String xwidth;
    private String xheight;
    private String localhostonly = "";
    private Context context;
    private Activity activity;
    private static final String ARG_SECTION_NUMBER = "section_number";
    private String selected_res;
    private String selected_vncres;
    private String selected_vncresCMD = "";
    private String selected_disp;
    private String selected_ppi;
    private String selected_user;
    private String selected_display;
    private String vnc_passwd;
    private boolean showingAdvanced;
    private boolean localhost;
    private boolean confirm_res;
    private String prevusr = "kali";
    private Integer posu;
    private Integer posd = 0;
    private static final int MIN_UID = 100000;
    private static final int MAX_UID = 101000;
    NhPaths nh; //= new NhPaths();
    String BUSYBOX_NH= nh.getBusyboxPath();

    public VNCFragment() {
    }

    public static VNCFragment newInstance(int sectionNumber) {
        VNCFragment fragment = new VNCFragment();
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
        final View rootView = inflater.inflate(R.layout.vnc_setup, container, false);
        View AdvancedView = rootView.findViewById(R.id.AdvancedView);
        Button Advanced = rootView.findViewById(R.id.AdvancedButton);
        CheckBox localhostCheckBox = rootView.findViewById(R.id.vnc_checkBox);
        SharedPreferences sharedpreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        confirm_res = sharedpreferences.getBoolean("confirm_res", false);
        if (confirm_res) {
            confirmDialog();
        }
        showingAdvanced = sharedpreferences.getBoolean("advanced_visible", false);

        localhost = sharedpreferences.getBoolean("localhost", true);
        if (!localhost) {
            localhostCheckBox.setChecked(false);
        } else {
            localhostCheckBox.setChecked(true);
        }
        AdvancedView.setVisibility(showingAdvanced ? View.VISIBLE : View.INVISIBLE);
        if (showingAdvanced) {
            Advanced.setText("HIDE ADVANCED SETTINGS");
        }
        // Get screen size to pass to VNC
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final int screen_height = displaymetrics.heightPixels;
        final int screen_width = displaymetrics.widthPixels;

        // Because height and width changes on screen rotation, use the largest as width
        if (screen_height > screen_width) {
            xwidth = Integer.toString(screen_height);
            xheight = Integer.toString(screen_width);
        } else {
            xwidth = Integer.toString(screen_width);
            xheight = Integer.toString(screen_height);
        }

        Button SetupVNCButton = rootView.findViewById(R.id.set_up_vnc);
        Button StartVNCButton = rootView.findViewById(R.id.start_vnc);
        Button StopVNCButton = rootView.findViewById(R.id.stop_vnc);
        Button OpenVNCButton = rootView.findViewById(R.id.vncClientStart);
        ImageButton RefreshKeX = rootView.findViewById(R.id.refreshKeX);
        Button AddUserButton = rootView.findViewById(R.id.AddUserButton);
        Button DelUserButton = rootView.findViewById(R.id.DelUserButton);
        Button ResetHDMIButton = rootView.findViewById(R.id.reset_hdmi);
        Button AddResolutionButton = rootView.findViewById(R.id.AddResolutionButton);
        Button DelResolutionButton = rootView.findViewById(R.id.DelResolutionButton);
        Button ApplyResolutionButton = rootView.findViewById(R.id.ApplyResolutionButton);
        Button BackupHDMI = rootView.findViewById(R.id.BackupResolutions);
        Button RestoreHDMI = rootView.findViewById(R.id.RestoreResolutions);
        Button AddVNCResolutionButton = rootView.findViewById(R.id.AddVncResolutionButton);
        Button DelVNCResolutionButton = rootView.findViewById(R.id.DelVncResolutionButton);
        Button BackupVNC = rootView.findViewById(R.id.BackupVncResolutions);
        Button RestoreVNC = rootView.findViewById(R.id.RestoreVncResolutions);

        // Add device resolution to vnc-resolution (only first run)
        ShellExecuter exe = new ShellExecuter();
        File vncResFile = new File(nh.APP_SD_FILES_PATH + "/configs/vnc-resolutions");
        String device_res = xwidth + "x" + xheight;
        if (vncResFile.length() == 0)
            exe.RunAsRoot(new String[]{"su -c echo \"Auto$'\n'" + device_res + "\" > " + vncResFile});

        //HDMI resolution
        File hdmiResFile = new File(nh.APP_SD_FILES_PATH + "/configs/hdmi-resolutions");
        String[] commandRES = {"sh", "-c", "cat " + hdmiResFile};
        String outputRES = exe.Executer(commandRES);
        final String[] resArray = outputRES.split("\n");

        //VNC resolution
        String[] commandVNCRES = {"sh", "-c", "cat " + vncResFile};
        String outputVNCRES = exe.Executer(commandVNCRES);
        final String[] vncresArray = outputVNCRES.split("\n");

        //HDMI Resolution spinner
        Spinner resolution = rootView.findViewById(R.id.resolution);
        ArrayAdapter adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1, resArray);
        resolution.setAdapter(adapter);

        //VNC Resolution spinner
        Spinner vncresolution = rootView.findViewById(R.id.vncresolution);
        ArrayAdapter vncadapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1, vncresArray);
        vncresolution.setAdapter(vncadapter);

        //Users
        File passwd = new File(nh.CHROOT_PATH() + "/etc/passwd");
        String commandUSR = ("echo root && " + BUSYBOX_NH + " awk -F':' -v \"min=" + MIN_UID + "\" -v \"max=" + MAX_UID + "\" '{ if ( $3 >= min && $3 <= max ) print $0}' " + passwd + " | " + BUSYBOX_NH + " cut -d: -f1");
        String outputUSR = exe.RunAsRootOutput(commandUSR);
        final String[] userArray = outputUSR.split("\n");
        Arrays.sort(userArray);

        //Last selected user
        prevusr = sharedpreferences.getString("user", "");

        //Users spinner
        Spinner users = rootView.findViewById(R.id.user);
        ArrayAdapter usersadapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1, userArray);
        users.setAdapter(usersadapter);
        Arrays.sort(userArray);
        posu = usersadapter.getPosition(prevusr);
        users.setSelection(posu);

        //Last selected display
        posd = sharedpreferences.getInt("display", 0);

        //Display spinner
        String[] displaylist = new String[]{"1","2","3","4","5","6","7","8","9","10"};
        Spinner displays = rootView.findViewById(R.id.display);
        ArrayAdapter displayadapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1, displaylist);
        displays.setAdapter(displayadapter);
        displays.setSelection(posd);

        //Select User
        users.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                selected_user = parentView.getItemAtPosition(pos).toString();
                sharedpreferences.edit().putString("user", selected_user).apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        //Select Display
        displays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int posd, long id) {
                selected_display = parentView.getItemAtPosition(posd).toString();
                sharedpreferences.edit().putInt("display", posd).apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        //Select HDMI resolution
        resolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                selected_res = parentView.getItemAtPosition(pos).toString();
                selected_disp = exe.RunAsRootOutput("su -c echo " + selected_res + " | cut -d : -f 1");
                selected_ppi = exe.RunAsRootOutput("su -c echo " + selected_res + " | cut -d : -f 2 | sed 's/ppi//g'");
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        //Select VNC resolution
        vncresolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                selected_vncres = parentView.getItemAtPosition(pos).toString();
                if (!selected_vncres.equals("Auto")) {
                    selected_vncresCMD = "-geometry " + selected_vncres + " ";
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        //Immersion switch
        final Switch immersionSwitch = rootView.findViewById(R.id.immersionSwitch);
        final String immersion = exe.RunAsRootOutput("settings get global policy_control");
        if (immersion.equals("null*"))
            immersionSwitch.setChecked(false);
        else
            immersionSwitch.setChecked(true);

        immersionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    exe.RunAsRoot(new String[]{"settings put global policy_control immersive.full=*"});
                } else {
                    exe.RunAsRoot(new String[]{"settings put global policy_control null*"});
                }
            }
        });

        // Checkbox for localhost only
        if (localhostCheckBox.isChecked())
            localhostonly = "-localhost yes ";
        else
            localhostonly = "-localhost no ";
        View.OnClickListener checkBoxListener = v -> {
            if (localhostCheckBox.isChecked()) {
                localhostonly = "-localhost yes ";
                sharedpreferences.edit().putBoolean("localhost", true).apply();

            } else {
                localhostonly = "-localhost no ";
                sharedpreferences.edit().putBoolean("localhost", false).apply();
            }
        };
        localhostCheckBox.setOnClickListener(checkBoxListener);

        //VNC service checkbox
        File kex_init = new File(nh.APP_PATH + "/etc/init.d/99kex");
        final CheckBox vnc_serviceCheckBox = rootView.findViewById(R.id.vnc_serviceCheckBox);
        final String initfile = exe.RunAsRootOutput("su -c cat " + kex_init);

        if (initfile.contains("vncserver"))
            vnc_serviceCheckBox.setChecked(true);
        else
            vnc_serviceCheckBox.setChecked(false);

        vnc_serviceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    File rootvncpasswd = new File(nh.CHROOT_PATH() + "/root/.vnc/passwd");
                    String vnc_passwd = exe.RunAsRootOutput("su -c cat " + rootvncpasswd);
                    if(!vnc_passwd.equals("")) {
                        String arch = System.getProperty("os.arch");
                        String shebang = "#!/system/bin/sh\n";
                        String kex_prep = "\n# KeX architecture: " + arch + "\n# Commands to run at boot:\nHOME=/root\nUSER=root";
                        String kex_cmd = "";
                        if(arch.equals("aarch64")) {
                            kex_cmd = "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali custom_cmd LD_PRELOAD=/usr/lib/aarch64-linux-gnu/libgcc_s.so.1 vncserver :1 " + localhostonly + " " + selected_vncresCMD + "'";
                        }
                        else {
                            kex_cmd = "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali custom_cmd LD_PRELOAD=/usr/lib/arm-linux-gnueabihf/libgcc_s.so.1 vncserver :1 " + localhostonly + " " + selected_vncresCMD + "'";
                        }
                        String fileContents = shebang + "\n" + kex_prep + "\n" + kex_cmd;
                        exe.RunAsRoot(new String[]{
                                "cat > " + kex_init + " <<s0133717hur75\n" + fileContents + "\ns0133717hur75\n",
                                "chmod 700 " + kex_init
                        });
                    }
                    else {
                        Toast.makeText(getActivity().getApplicationContext(), "Please setup local server first!", Toast.LENGTH_SHORT).show();
                        vnc_serviceCheckBox.setChecked(false);
                    }
                } else
                    exe.RunAsRoot(new String[]{"rm -rf " + kex_init});
            }
        });

        //Server status
        RefreshKeX.setOnClickListener(v -> {
            refreshVNC(rootView);
        });
        refreshVNC(rootView);

        addClickListener(SetupVNCButton, v -> {
            intentClickListener_NH("echo -ne \"\\033]0;Setting up Server\\007\" && clear;echo $'\n'\"Please enter your new VNC server password\"$'\n' && sudo -u " + selected_user + " vncpasswd && sleep 2 && exit"); // since is a kali command we can send it as is
        });
        addClickListener(StartVNCButton, v -> {
            if(selected_user.equals("root")) {
                File rootvncpasswd = new File(nh.CHROOT_PATH() + "/root/.vnc/passwd");
                vnc_passwd = exe.RunAsRootOutput("su -c cat " + rootvncpasswd);
            } else {
                File uservncpasswd = new File(nh.CHROOT_PATH() + "/home/" + selected_user + "/.vnc/passwd");
                vnc_passwd = exe.RunAsRootOutput("su -c cat " + uservncpasswd);
            }
            if(vnc_passwd.equals("")) {
                Toast.makeText(getActivity().getApplicationContext(), "Please setup local server first!", Toast.LENGTH_SHORT).show();
            } else {
                String arch = System.getProperty("os.arch");
                if(arch.equals("aarch64")) {
                    if(selected_user.equals("root")) {
                        intentClickListener_NH("echo -ne \"\\033]0;Starting Server\\007\" && clear;HOME=/root;USER=root;sudo -u root LD_PRELOAD=/usr/lib/aarch64-linux-gnu/libgcc_s.so.1 nohup vncserver :" + selected_display + " " + localhostonly + "-name \"NetHunter KeX\" " + selected_vncresCMD + " >/dev/null 2>&1 </dev/null;echo \"Server started! Closing terminal..\" && sleep 2 && exit");
                    } else {
                        intentClickListener_NH("echo -ne \"\\033]0;Starting Server\\007\" && clear;HOME=/home/" + selected_user + ";USER=" + selected_user + ";sudo -u " + selected_user + " LD_PRELOAD=/usr/lib/aarch64-linux-gnu/libgcc_s.so.1 nohup vncserver :" + selected_display + " " + localhostonly + "-name \"NetHunter KeX\" " + selected_vncresCMD + " >/dev/null 2>&1 </dev/null;echo \"Server started! Closing terminal..\" && sleep 2 && exit");
                    }
                } else {
                    if (selected_user.equals("root")) {
                        intentClickListener_NH("echo -ne \"\\033]0;Starting Server\\007\" && clear;HOME=/root;export USER=root;sudo -u root LD_PRELOAD=/usr/lib/arm-linux-gnueabihf/libgcc_s.so.1 nohup vncserver :" + selected_display + " " + localhostonly + "-name \"NetHunter KeX\" " + selected_vncresCMD + " >/dev/null 2>&1 </dev/null;echo \"Server started! Closing terminal..\" && sleep 2 && exit");
                    } else {
                        intentClickListener_NH("echo -ne \"\\033]0;Starting Server\\007\" && clear;HOME=/home/" + selected_user + ";USER=" + selected_user + ";sudo -u " + selected_user + " LD_PRELOAD=/usr/lib/arm-linux-gnueabihf/libgcc_s.so.1 nohup vncserver :" + selected_display + " " + "-name \"NetHunter KeX\" " + selected_vncresCMD + " >/dev/null 2>&1 </dev/null;echo \"Server started! Closing terminal..\" && sleep 2 && exit");
                    }
                }
                Log.d(TAG, localhostonly);
            }
        });
        addClickListener(StopVNCButton, v -> {
            intentClickListener_NH("echo -ne \"\\033]0;Killing Server\\007\" && clear;sudo -u " + selected_user + " vncserver -kill :" + selected_display + " ;sleep 2 && exit"); // since is a kali command we can send it as is
        });
        addClickListener(OpenVNCButton, v -> {
            intentClickListener_VNC(); // since is a kali command we can send it as is
        });
        addClickListener(Advanced, v -> {
            if (!showingAdvanced) {
                AdvancedView.setVisibility(View.VISIBLE);
                Advanced.setText("HIDE ADVANCED SETTINGS");
                showingAdvanced = true;
                sharedpreferences.edit().putBoolean("advanced_visible", true).apply();
            } else {
                AdvancedView.setVisibility(View.GONE);
                Advanced.setText("SHOW ADVANCED SETTINGS");
                showingAdvanced = false;
                sharedpreferences.edit().putBoolean("advanced_visible", false).apply();
            }
        });
        addClickListener(AddUserButton, v -> {
            intentClickListener_NH("echo -ne \"\\033]0;New User\\007\" && clear;read -p \"Please enter your new username\"$'\n' USER && adduser --firstuid " + MIN_UID + " --lastuid " + MAX_UID + " $USER; groupmod -g $(id -u $USER) $USER; usermod -aG sudo $USER; usermod -aG inet $USER; usermod -aG sockets $USER; echo \"Please refresh your KeX manager, closing in 2 secs\" && sleep 2 && exit");
        });
        addClickListener(DelUserButton, v -> {
            if (selected_user.contains("root")) {
                Toast.makeText(getActivity().getApplicationContext(), "Can't remove root!", Toast.LENGTH_SHORT).show();
            } else {
                intentClickListener_NH("echo -ne \"\\033]0;Removing User\\007\" && clear;deluser -remove-home " + selected_user + " && sleep 2 && exit");
            }
        });
        addClickListener(ResetHDMIButton, v -> {
            intentClickListener_NHSU("wm size reset;wm density reset;am start com.offsec.nethunter/.AppNavHomeActivity -e \":android:show_fragment\" com.offsec.nethunter.VNCFragment;sleep 2 && exit");
            sharedpreferences.edit().putBoolean("confirm_res", false).apply();
        });
        addClickListener(BackupHDMI, v -> {
            exe.RunAsRoot(new String[]{"su -c cp " + hdmiResFile + " " + nh.SD_PATH});
            Toast.makeText(getActivity().getApplicationContext(), "Backup successful!", Toast.LENGTH_SHORT).show();
        });
        addClickListener(RestoreHDMI, v -> {
            String hdmibackup = exe.RunAsRootOutput("su -c cat " + nh.SD_PATH + "/hdmi-resolutions");
            if(hdmibackup.equals("")) {
                Toast.makeText(getActivity().getApplicationContext(), "Backup file not found!", Toast.LENGTH_SHORT).show();
            } else {
                exe.RunAsRoot(new String[]{"su -c cp " + nh.SD_PATH + "/hdmi-resolutions " + hdmiResFile});
                reload();
                Toast.makeText(getActivity().getApplicationContext(), "Restore successful!", Toast.LENGTH_SHORT).show();
            }
        });
        addClickListener(AddResolutionButton, v -> {
            openResolutionDialog();
        });
        addClickListener(ApplyResolutionButton, v -> {
            intentClickListener_NHSU("wm size " + selected_disp + "; wm density " + selected_ppi + ";am start com.offsec.nethunter/.AppNavHomeActivity -e \":android:show_fragment\" com.offsec.nethunter.VNCFragment;sleep 2 && exit");
            sharedpreferences.edit().putBoolean("confirm_res", true).apply();
        });
        addClickListener(DelResolutionButton, v -> {
            if (!selected_res.equals("1080x1920:300ppi")) {
                exe.RunAsRoot(new String[]{"su -c sed -i '/^" + selected_res + "$/d' " + hdmiResFile});
                reload();
            } else
                Toast.makeText(getActivity().getApplicationContext(), "Can't remove default resolution!", Toast.LENGTH_SHORT).show();
        });
        addClickListener(AddVNCResolutionButton, v -> {
            openVNCResolutionDialog();
        });
        addClickListener(DelVNCResolutionButton, v -> {
            if (selected_vncres.equals("Auto")) {
                Toast.makeText(getActivity().getApplicationContext(), "Can't remove default resolution!", Toast.LENGTH_SHORT).show();
            } else if (selected_vncres.equals(device_res)) {
                Toast.makeText(getActivity().getApplicationContext(), "Can't remove device resolution!", Toast.LENGTH_SHORT).show();
            } else {
                exe.RunAsRoot(new String[]{"su -c sed -i '/^" + selected_vncres + "$/d' " + vncResFile});
                reload();
            }
        });
        addClickListener(BackupVNC, v -> {
            exe.RunAsRoot(new String[]{"su -c cp " + vncResFile + " " + nh.SD_PATH});
            Toast.makeText(getActivity().getApplicationContext(), "Backup successful!", Toast.LENGTH_SHORT).show();
        });
        addClickListener(RestoreVNC, v -> {
            String vncbackup = exe.RunAsRootOutput("su -c cat " + nh.SD_PATH + "/vnc-resolutions");
            if(vncbackup.equals("")) {
                Toast.makeText(getActivity().getApplicationContext(), "Backup file not found!", Toast.LENGTH_SHORT).show();
            } else {
                exe.RunAsRoot(new String[]{"su -c cp " + nh.SD_PATH + "/vnc-resolutions " + vncResFile});
                reload();
                Toast.makeText(getActivity().getApplicationContext(), "Restore successful!", Toast.LENGTH_SHORT).show();
            }
        });
        return rootView;
    }

    private void reload() {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, VNCFragment.newInstance(0))
                .addToBackStack(null)
                .commit();
    }

    private void refreshVNC(View VNCFragment) {
        final TextView KeXstatus = VNCFragment.findViewById(R.id.KeXstatus);
        final TextView KeXuser = VNCFragment.findViewById(R.id.KeXuser);
        ShellExecuter exe = new ShellExecuter();
        String kex_userCmd = "";
        String kex_statusCmd = exe.RunAsRootOutput("su -c pidof Xtigervnc");
        if (kex_statusCmd.equals("")) {
            KeXstatus.setText("STOPPED");
            KeXuser.setText("None");
        }
        else {
            KeXstatus.setText("RUNNING");
            kex_userCmd = exe.RunAsRootOutput(BUSYBOX_NH + " ps -w | grep Xtigervnc | grep Xauthority | " + BUSYBOX_NH + " awk '{gsub(/home/,\"\")} {gsub(/\\//,\"\")} {gsub(/.Xauthority/,\"\")} {print $11 $6}'");
            KeXuser.setText(kex_userCmd);
        }
        File passwd = new File(nh.CHROOT_PATH() + "/etc/passwd");
        String commandUSR = ("echo root && " + BUSYBOX_NH + " awk -F':' -v \"min=" + MIN_UID + "\" -v \"max=" + MAX_UID + "\" '{ if ( $3 >= min && $3 <= max ) print $0}' " + passwd + " | " + BUSYBOX_NH + " cut -d: -f1");
        String outputUSR = exe.RunAsRootOutput(commandUSR);
        final String[] userArray = outputUSR.split("\n");
        Arrays.sort(userArray);
        Spinner users = VNCFragment.findViewById(R.id.user);
        ArrayAdapter usersadapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1, userArray);
        users.setAdapter(usersadapter);
        SharedPreferences sharedpreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        posd = sharedpreferences.getInt("display", 0);
        Spinner displays = VNCFragment.findViewById(R.id.display);
        displays.setSelection(posd);
        prevusr = sharedpreferences.getString("user", "");
        posu = usersadapter.getPosition(prevusr);
        users.setSelection(posu);
    }

    private void openResolutionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.resolutiondialog, null);
        builder.setView(dialogView);
        builder.setTitle("Add a new device resolution (vertical)");
        final EditText width = (EditText) dialogView.findViewById(R.id.width);
        final EditText height = (EditText) dialogView.findViewById(R.id.height);
        final EditText density = (EditText) dialogView.findViewById(R.id.density);
        File hdmiResFile = new File(nh.APP_SD_FILES_PATH + "/configs/hdmi-resolutions");
        ShellExecuter exe = new ShellExecuter();
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which) {
                final String add_width = width.getText().toString();
                final String add_height = height.getText().toString();
                final String add_density = density.getText().toString();
                if (add_width.equals("") || add_height.equals("") || add_density.equals("")) {
                    Toast.makeText(getActivity().getApplicationContext(), "Please enter the values!", Toast.LENGTH_SHORT).show();
                    openResolutionDialog();
                } else if (Integer.parseInt(width.getText().toString()) > Integer.parseInt(height.getText().toString())){
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                    builder2.setTitle("Width is bigger than height!");
                    builder2.setMessage("Bigger width is usually only for tablets. Misconfiguration can render the device unresponsive");
                    builder2.setPositiveButton("Keep", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog2, int which) {
                            exe.RunAsRoot(new String[]{"su -c echo " + add_width + "x" + add_height + ":" + add_density + "ppi >> " + hdmiResFile});
                            reload();
                        }
                    });
                    builder2.setNegativeButton("Back", (dialog2, whichButton) -> {
                        openResolutionDialog();
                    });
                    builder2.show();
                } else {
                    exe.RunAsRoot(new String[]{"su -c echo " + add_width + "x" + add_height + ":" + add_density + "ppi >> " + hdmiResFile});
                    reload();
                }
            }
        });
        builder.setNegativeButton("Cancel", (dialog, whichButton) -> {
        });
        builder.show();
    }

    private void openVNCResolutionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.vncresolutiondialog, null);
        builder.setView(dialogView);
        builder.setTitle("Add a new VNC server resolution (horizontal)");
        final EditText width = (EditText) dialogView.findViewById(R.id.width);
        final EditText height = (EditText) dialogView.findViewById(R.id.height);
        File vncResFile = new File(nh.APP_SD_FILES_PATH + "/configs/vnc-resolutions");
        ShellExecuter exe = new ShellExecuter();
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which) {
                final String add_width = width.getText().toString();
                final String add_height = height.getText().toString();
                if (add_width.equals("") || add_height.equals("")) {
                    Toast.makeText(getActivity().getApplicationContext(), "Please enter the values!", Toast.LENGTH_SHORT).show();
                    openResolutionDialog();
                } else {
                    exe.RunAsRoot(new String[]{"su -c echo " + add_width + "x" + add_height + " >> " + vncResFile});
                    reload();
                }
            }
        });
        builder.setNegativeButton("Cancel", (dialog, whichButton) -> {
        });
        builder.show();
    }



    private void confirmDialog() {

        SharedPreferences sharedpreferences = context.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        final AlertDialog.Builder confirmbuilder = new AlertDialog.Builder(getActivity());
        confirmbuilder.setTitle("Do you want to keep the resolution?");
        confirmbuilder.setMessage("Loading..");
        confirmbuilder.setPositiveButton("Keep resolution", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sharedpreferences.edit().putBoolean("confirm_res", false).apply();
                dialogInterface.cancel();
            }
        });
        final AlertDialog alert = confirmbuilder.create();
        alert.show();
        CountDownTimer resetResolution = new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long l) {
                alert.setMessage("Resetting device resolution in "+ l/1000 + " sec");
            }
            @Override
            public void onFinish() {
                ShellExecuter exe = new ShellExecuter();
                exe.RunAsRoot(new String[]{"su -c wm size reset; wm density reset"});
                sharedpreferences.edit().putBoolean("confirm_res", false).apply();
            }
        }.start();
        alert.setButton(alert.BUTTON_POSITIVE,"Keep resolution",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sharedpreferences.edit().putBoolean("confirm_res", false).apply();
                alert.cancel();
                resetResolution.cancel();
            }
        });
    }


    private void addClickListener(Button _button, View.OnClickListener onClickListener) {
        _button.setOnClickListener(onClickListener);
    }

    private void intentClickListener_VNC() {
        try {
            if (getView() == null)
                return;
            Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.offsec.nethunter.kex");
            startActivity(intent);
        } catch (Exception e) {
            Log.d("errorLaunching", e.toString());
            nh.showMessage(context, "NetHunter VNC not found!");
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
            nh.showMessage(context, getString(R.string.toast_install_terminal));

        }
    }

    private void intentClickListener_NHSU(final String command) {
        try {
            Intent intent =
                    new Intent("com.offsec.nhterm.RUN_SCRIPT_SU");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("com.offsec.nhterm.iInitialCommand", command);
            startActivity(intent);
        } catch (Exception e) {
            nh.showMessage(context, getString(R.string.toast_install_terminal));

        }
    }
}