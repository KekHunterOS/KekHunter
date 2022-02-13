package com.team420.kekhunter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.team420.kekhunter.utils.BootKali;
import com.team420.kekhunter.utils.NhPaths;
import com.team420.kekhunter.utils.ShellExecuter;
import hilled.pwnterm.bridge.Bridge;

public class DeAuthFragment extends Fragment {
  private final ShellExecuter exe = new ShellExecuter();
  private Context context;
  private Activity activity;
  private static final String ARG_SECTION_NUMBER = "section_number";

  public static DeAuthFragment newInstance(int sectionNumber) {
    DeAuthFragment fragment = new DeAuthFragment();
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

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    final View rootView = inflater.inflate(R.layout.deauth, container, false);
    SharedPreferences sharedpreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
    setHasOptionsMenu(true);
    final Button scan = rootView.findViewById(R.id.scan_networks);
    final EditText wlan = rootView.findViewById(R.id.wlan_interface);
    final EditText term = rootView.findViewById(R.id.TerminalOutputDeAuth);
    final Button start = rootView.findViewById(R.id.StartDeAuth);
    final EditText pkt = rootView.findViewById(R.id.time);
    final EditText channel = rootView.findViewById(R.id.channel);
    final CheckBox whitelist = rootView.findViewById(R.id.deauth_whitelist);
    final CheckBox white_me = rootView.findViewById(R.id.deauth_me);
    whitelist.setChecked(false);
    start.setOnClickListener(v -> {
      String whitelist_command;
      new BootKali("ip link set " + wlan.getText() + " up");
      try {
        Thread.sleep(1000);
        new BootKali("airmon-ng start  " + wlan.getText()).run_bg();
        Thread.sleep(2000);
        if (whitelist.isChecked()) {
          whitelist_command = "-w /sdcard/nh_files/deauth/whitelist.txt ";
        } else {
          whitelist_command = "";
        }
        run_cmd("echo Press Crtl+C to stop! && mdk3 " + wlan.getText() + "mon d " + whitelist_command + "-c " + channel.getText());
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });
    scan.setOnClickListener(v -> {

      /*TODO: create .sh that executes the commands and puts its output in a file and then read the file in the textview 20/02/17*/
      new BootKali("cp /sdcard/nh_files/deauth/scan.sh /root/scan.sh && chmod +x /root/scan.sh").run_bg();
      String cmd = "./root/scan.sh " + wlan.getText() + " | tr -s [:space:] > /sdcard/nh_files/deauth/output.txt";
      try {
        new BootKali("ip link set " + wlan.getText() + " up").run_bg();
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      new BootKali(cmd).run_bg();
      try {
        Thread.sleep(5000);
        String output = exe.RunAsRootOutput("cat " + NhPaths.APP_SD_FILES_PATH + "/deauth/output.txt").replace("Channel:", "\n Channel:");
        term.setText(output);
      } catch (Exception e) {
        e.printStackTrace();
        term.setText(e.toString());
      }

    });
    whitelist.setOnClickListener(v -> {
      if (whitelist.isChecked()) {
        white_me.setClickable(true);
        String check_me = exe.RunAsRootOutput("grep -q " + getmac(wlan.getText().toString()) + " \"" + NhPaths.APP_SD_FILES_PATH + "/deauth/whitelist.txt\" && echo $?");
        white_me.setChecked(check_me.contains("0"));
      } else {
        white_me.setChecked(false);
        white_me.setClickable(false);
      }
    });
    white_me.setOnClickListener(v -> {
      if (whitelist.isChecked()) {
        if (white_me.isChecked()) {
          if (!wlan.getText().toString().equals("wlan0")) {
            exe.RunAsRootOutput("echo '" + getmac("wlan0") + "' >> " + NhPaths.APP_SD_FILES_PATH + "/deauth/whitelist.txt");
          }
          exe.RunAsRootOutput("echo '" + getmac(wlan.getText().toString()) + "' >> " + NhPaths.APP_SD_FILES_PATH + "/deauth/whitelist.txt");
        } else {
          if (!wlan.getText().toString().equals("wlan0")) {
            exe.RunAsRootOutput("sed -i '/wlan0/d' /sdcard/nh_files/deauth/whitelist.txt");
          }
          exe.RunAsRootOutput("sed -i '/" + getmac(wlan.getText().toString()) + "/d' " + NhPaths.APP_SD_FILES_PATH + "/deauth/whitelist.txt");
        }
      } else {
        white_me.setChecked(false);
      }
    });
    return rootView;
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.deauth, menu);
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.deauth_modify) {
      Intent i = new Intent(activity, DeAuthWhitelistActivity.class);
      startActivity(i);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  public String getmac(final String wlan) {
    final String mac;
    mac = exe.RunAsRootOutput("cat /sys/class/net/" + wlan + "/address");
    return mac;
  }

  public void run_cmd(String cmd) {
    Intent intent = Bridge.createExecuteIntent("/data/data/hilled.pwnterm/files/usr/bin/kali", cmd);
    startActivity(intent);
  }

}
