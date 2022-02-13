package com.team420.kekhunter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.team420.kekhunter.utils.NhPaths;
import com.team420.kekhunter.utils.ShellExecuter;
import hilled.pwnterm.bridge.Bridge;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SettingsFragment extends Fragment {

  private Context context;
  private Activity activity;
  private static final String ARG_SECTION_NUMBER = "section_number";
  private String selected_animation;
  NhPaths nh;
  private SharedPreferences sharedpreferences;

  public SettingsFragment() {
  }

  public static SettingsFragment newInstance(int sectionNumber) {
    SettingsFragment fragment = new SettingsFragment();
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

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    final View rootView = inflater.inflate(R.layout.settings, container, false);
    SharedPreferences sharedpreferences = context.getSharedPreferences("com.team420.kekhunter", Context.MODE_PRIVATE);
    setHasOptionsMenu(true);

    //First run
    Boolean setupdone = sharedpreferences.getBoolean("animation_setup_done", false);
    if (!setupdone.equals(true))
      SetupDialog();

    //Bootanimation spinner
    String[] animations = new String[]{"Classic", "Burning", "New Kali", "ctOS", "Glitch"};
    Spinner animation_spinner = rootView.findViewById(R.id.animation_spinner);
    animation_spinner.setAdapter(new ArrayAdapter<>(getContext(),
      android.R.layout.simple_list_item_1, animations));

    //Select Animation
    final String[] animation_dir = {""};
    animation_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
        final VideoView videoview = rootView.findViewById(R.id.videoView);
        selected_animation = parentView.getItemAtPosition(pos).toString();
        if (selected_animation.equals("Classic")) {
          String path = ("android.resource://" + context.getPackageName() + "/" + R.raw.boot_classic);
          videoview.setVideoURI(Uri.parse(path));
          animation_dir[0] = "src";
          bootanimation_start();
        } else if (selected_animation.equals("Burning")) {
          String path = ("android.resource://" + context.getPackageName() + "/" + R.raw.boot_mk);
          videoview.setVideoURI(Uri.parse(path));
          animation_dir[0] = "src_mk";
          bootanimation_start();
        } else if (selected_animation.equals("New Kali")) {
          String path = ("android.resource://" + context.getPackageName() + "/" + R.raw.boot_kali);
          videoview.setVideoURI(Uri.parse(path));
          animation_dir[0] = "src_kali";
          bootanimation_start();
        } else if (selected_animation.equals("ctOS")) {
          String path = ("android.resource://" + context.getPackageName() + "/" + R.raw.boot_ctos);
          videoview.setVideoURI(Uri.parse(path));
          animation_dir[0] = "src_ctos";
          bootanimation_start();
        } else if (selected_animation.equals("Glitch")) {
          String path = ("android.resource://" + context.getPackageName() + "/" + R.raw.boot_glitch);
          videoview.setVideoURI(Uri.parse(path));
          animation_dir[0] = "src_glitch";
          bootanimation_start();
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parentView) {
      }
    });

    //Convert Checkbox
    CheckBox ConvertCheckbox = rootView.findViewById(R.id.convert);

    //Image and Final size
    EditText ImageWidth = rootView.findViewById(R.id.image_width);
    EditText ImageHeight = rootView.findViewById(R.id.image_height);
    EditText FinalWidth = rootView.findViewById(R.id.final_width);
    EditText FinalHeight = rootView.findViewById(R.id.final_height);
    final Button ImageResMinus = rootView.findViewById(R.id.imageresminus);
    final Button ImageResPlus = rootView.findViewById(R.id.imageresplus);
    final Button FinalResMinus = rootView.findViewById(R.id.finalresminus);
    final Button FinalResPlus = rootView.findViewById(R.id.finalresplus);
    ImageWidth.setEnabled(false);
    ImageHeight.setEnabled(false);
    ImageResMinus.setEnabled(false);
    ImageResPlus.setEnabled(false);
    ConvertCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          ImageWidth.setEnabled(true);
          ImageWidth.setTextColor(Color.parseColor("#FFFFFF"));
          ImageHeight.setEnabled(true);
          ImageHeight.setTextColor(Color.parseColor("#FFFFFF"));
          ImageResMinus.setEnabled(true);
          ImageResMinus.setTextColor(Color.parseColor("#FFFFFF"));
          ImageResPlus.setEnabled(true);
          ImageResPlus.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
          ImageWidth.setEnabled(false);
          ImageWidth.setTextColor(Color.parseColor("#40FFFFFF"));
          ImageHeight.setEnabled(false);
          ImageHeight.setTextColor(Color.parseColor("#40FFFFFF"));
          ImageResMinus.setEnabled(false);
          ImageResMinus.setTextColor(Color.parseColor("#40FFFFFF"));
          ImageResPlus.setEnabled(false);
          ImageResPlus.setTextColor(Color.parseColor("#40FFFFFF"));
        }
      }
    });
    addClickListener(ImageResMinus, v -> {
      String imagewidth = ImageWidth.getText().toString();
      int finalValueIW = Integer.parseInt(imagewidth) - 108;
      ImageWidth.setText(String.valueOf(finalValueIW));
      String imageheight = ImageHeight.getText().toString();
      int finalValueIH = Integer.parseInt(imageheight) - 192;
      ImageHeight.setText(String.valueOf(finalValueIH));
    });
    addClickListener(ImageResPlus, v -> {
      String imagewidth = ImageWidth.getText().toString();
      int finalValueIW = Integer.parseInt(imagewidth) + 108;
      ImageWidth.setText(String.valueOf(finalValueIW));
      String imageheight = ImageHeight.getText().toString();
      int finalValueIH = Integer.parseInt(imageheight) + 192;
      ImageHeight.setText(String.valueOf(finalValueIH));
    });
    addClickListener(FinalResMinus, v -> {
      String finalwidth = FinalWidth.getText().toString();
      int finalValueFW = Integer.parseInt(finalwidth) - 108;
      FinalWidth.setText(String.valueOf(finalValueFW));
      String finalheight = FinalHeight.getText().toString();
      int finalValueFH = Integer.parseInt(finalheight) - 192;
      FinalHeight.setText(String.valueOf(finalValueFH));
    });
    addClickListener(FinalResPlus, v -> {
      String finalwidth = FinalWidth.getText().toString();
      int finalValueFW = Integer.parseInt(finalwidth) + 108;
      FinalWidth.setText(String.valueOf(finalValueFW));
      String finalheight = FinalHeight.getText().toString();
      int finalValueFH = Integer.parseInt(finalheight) + 192;
      FinalHeight.setText(String.valueOf(finalValueFH));
    });

    //Preview Checkbox
    View PreView = rootView.findViewById(R.id.pre_view);
    CheckBox PreviewCheckbox = rootView.findViewById(R.id.preview_checkbox);
    PreviewCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          PreView.setVisibility(View.VISIBLE);
        } else {
          PreView.setVisibility(View.GONE);
        }
      }
    });

    // Screen size
    DisplayMetrics displaymetrics = new DisplayMetrics();
    WindowManager wm = (WindowManager) activity.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    Display disp = wm.getDefaultDisplay();
    int API_LEVEL = android.os.Build.VERSION.SDK_INT;
    if (API_LEVEL >= 17) {
      disp.getRealMetrics(displaymetrics);
    } else {
      disp.getMetrics(displaymetrics);
    }
    final int screen_height = displaymetrics.heightPixels;
    final int screen_width = displaymetrics.widthPixels;
    final String size = screen_width + "x" + screen_height;
    final TextView ScreenSize = rootView.findViewById(R.id.screen_size);
    ScreenSize.setText(size);

    //Bootanimation path
    EditText BootanimationPath = rootView.findViewById(R.id.bootanimation_path);
    ShellExecuter exe = new ShellExecuter();
    String bootanimation_path = exe.RunAsRootOutput("find /vendor /system -name \"*ootanimation.zip\"");
    BootanimationPath.setText(bootanimation_path);

    //Make bootanimation
    Button MakeBootAnimationButton = rootView.findViewById(R.id.make_bootanimation);
    EditText FPS = rootView.findViewById(R.id.fps);
    addClickListener(MakeBootAnimationButton, v -> {
      String resizeCMD;
      String imagesCMD;
      String foldersCMD;
      if (ConvertCheckbox.isChecked()) {
        if (selected_animation.equals("Burning")) foldersCMD = "";
        else foldersCMD = " new/part1 new/part2";
        resizeCMD = " -resize " + ImageWidth.getText().toString() + "x" + ImageHeight.getText().toString() + " ";
        imagesCMD = " mkdir -p new/part0" + foldersCMD + " && echo 'Converting images...'" +
          "&& for i in {0000..0100}; do convert" + resizeCMD + animation_dir[0] + "/part0/$i.jpg new/part0/$i.jpg >/dev/null 2>&1; done; echo \"[+] part0 done\" " +
          "&& if [ -d new/part1 ]; then for i in {0000..0200}; do convert" + resizeCMD + animation_dir[0] + "/part1/$i.jpg new/part1/$i.jpg >/dev/null 2>&1; done; fi; echo '[+] part1 done' " +
          "&& if [ -d new/part2 ]; then for i in {0000..0200}; do convert" + resizeCMD + animation_dir[0] + "/part2/$i.jpg new/part2/$i.jpg >/dev/null 2>&1; done; fi; echo '[+] part2 done' ";
      } else {
        imagesCMD = " mkdir new && cp -r " + animation_dir[0] + "/part* new/";
      }
      String finalRES = FinalWidth.getText().toString() + "x" + FinalHeight.getText().toString();
      String finalFPS = FPS.getText().toString();
      run_cmd("echo -ne \"\\033]0;Building animation\\007\" && clear;cd /root/nethunter-bootanimation &&" + imagesCMD + " && cp " + animation_dir[0] +
        "/desc.txt new/ && sed -i '1s/.*/" + finalRES + " " + finalFPS + "/' new/desc.txt && sed -i 's/x/ /g' new/desc.txt && cd new && zip -0 -FSr -q /sdcard/bootanimation.zip * && cd .. && rm -r new && echo \"Done. Head back to NetHunter to install the bootanimation! Exiting in 3secs..\" && sleep 3 && exit");
    });

    //Install bootanimation
    Button InstallBootAnimationButton = rootView.findViewById(R.id.set_bootanimation);
    addClickListener(InstallBootAnimationButton, v -> {
      File AnimationZip = new File(nh.SD_PATH + "/bootanimation.zip");
      if (AnimationZip.length() == 0)
        Toast.makeText(getActivity().getApplicationContext(), "Bootanimation zip is not created!!", Toast.LENGTH_SHORT).show();
      else {
        run_cmd_android("echo -ne \"\\033]0;Installing animation\\007\" && clear;grep ' / ' /proc/mounts | grep -qv 'rootfs' || grep -q ' /system_root ' /proc/mounts && SYSTEM=/ || SYSTEM=/system " +
          "&& mount -o rw,remount $SYSTEM && cp " + nh.SD_PATH + "/bootanimation.zip " + BootanimationPath.getText().toString() + " " +
          "&& echo \"Done. Please reboot to check the result! Exiting in 3secs..\" && sleep 3 && exit");
      }
    });

    //Backup
    Button BackupButton = rootView.findViewById(R.id.backup);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
    addClickListener(BackupButton, v -> {
      String currentDateandTime = sdf.format(new Date());
      exe.RunAsRoot(new String[]{"cd " + nh.SD_PATH + "/nh_files && tar -czvf /sdcard/nh_files_" + currentDateandTime + ".tar *"});
      Toast.makeText(getActivity().getApplicationContext(), "Backup has been saved to /sdcard/nh_files_" + currentDateandTime, Toast.LENGTH_LONG).show();
    });

    //Restore
    final EditText RestoreFileName = rootView.findViewById(R.id.restorefilename);
    final Button RestoreFileBrowse = rootView.findViewById(R.id.restorefilebrowse);
    RestoreFileBrowse.setOnClickListener(v -> {
      Intent intent = new Intent();
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.setType("application/x-tar");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      startActivityForResult(Intent.createChooser(intent, "Select archive file"), 1001);
    });

    final Button RestoreButton = rootView.findViewById(R.id.restore);
    RestoreButton.setOnClickListener(v -> {
      String RestoreFilePath = RestoreFileName.getText().toString();
      File RestoreFile = new File(RestoreFilePath);
      if (RestoreFile.length() == 0) {
        Toast.makeText(getActivity().getApplicationContext(), "Select a backup file to restore!", Toast.LENGTH_SHORT).show();
      } else {
        exe.RunAsRoot(new String[]{"rm -r " + nh.SD_PATH + "/nh_files/* && tar -xvf " + RestoreFilePath + " -C " + nh.SD_PATH + "/nh_files/"});
        Toast.makeText(getActivity().getApplicationContext(), "nh_files has been successfully restored", Toast.LENGTH_SHORT).show();
      }
    });

    //Uninstall
    final Button UninstallButton = rootView.findViewById(R.id.uninstall_nh);
    File NhSystemApp = new File("/system/app/NetHunter/NetHunter.apk");
    addClickListener(UninstallButton, v -> {
      if (NhSystemApp.length() == 0) {
        Toast.makeText(getActivity().getApplicationContext(), "NetHunter was not flashed as system app! Please remove it from Android settings.", Toast.LENGTH_LONG).show();
      } else {
        run_cmd_android("echo -ne \"\\033]0;Uninstalling NetHunter\\007\" && clear;grep ' / ' /proc/mounts | grep -qv 'rootfs' || grep -q ' /system_root ' /proc/mounts && SYSTEM=/ || SYSTEM=/system " +
          "&& mount -o rw,remount $SYSTEM && rm " + NhSystemApp + " && pm clear com.team420.kekhunter && echo 'Done! Reboot your device to complete the process. Exiting in 3secs..' && sleep 3 && exit");
      }
    });

    //Busybox
    TextView BusyboxVersion = rootView.findViewById(R.id.busybox_version);
    String busybox_ver = exe.RunAsRootOutput("/system/xbin/busybox | head -n1 | cut -c 10-13");
    BusyboxVersion.setText(busybox_ver);

    final String[] busybox_file = {null};

    //Version Spinner
    Spinner busybox_spinner = rootView.findViewById(R.id.bb_spinner);
    String commandBB = ("ls /system/xbin | grep busybox_nh- | cut -f 2 -d '-'");
    String outputBB = exe.RunAsRootOutput(commandBB);
    final String[] bbArray = outputBB.split("\n");
    ArrayAdapter usersadapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, bbArray);
    busybox_spinner.setAdapter(usersadapter);

    //Select Version
    busybox_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
        String selected_version = parentView.getItemAtPosition(pos).toString();
        if (selected_version.equals("1.25")) {
          busybox_file[0] = "busybox_nh-1.25";
        } else if (selected_version.equals("1.32")) {
          busybox_file[0] = "busybox_nh-1.32";
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parentView) {
      }
    });

    //Apply button
    final Button BusyboxButton = rootView.findViewById(R.id.select_bb);
    BusyboxButton.setOnClickListener(v -> {
      File busybox = new File("/system/xbin/" + busybox_file[0]);
      exe.RunAsRoot(new String[]{"if [ \"$(getprop ro.build.system_root_image)\" == \"true\" ]; then export SYSTEM=/; else export SYSTEM=/system;fi;mount -o rw,remount $SYSTEM && rm /system/xbin/busybox_nh;ln -s " + busybox + " /system/xbin/busybox_nh"});
      Toast.makeText(getActivity().getApplicationContext(), "NetHunter BusyBox version has been successfully modified", Toast.LENGTH_SHORT).show();
    });
    final Button BusyboxSystemButton = rootView.findViewById(R.id.system_bb);
    String busybox_system = exe.RunAsRootOutput("/system/xbin/busybox | head -n1 | grep -iF nethunter");
    if (busybox_system.equals("")) {
      BusyboxSystemButton.setEnabled(true);
      BusyboxSystemButton.setTextColor(Color.parseColor("#FFFFFFFF"));
    } else {
      BusyboxSystemButton.setEnabled(false);
      BusyboxSystemButton.setTextColor(Color.parseColor("#40FFFFFF"));
    }
    BusyboxSystemButton.setOnClickListener(v -> {
      exe.RunAsRoot(new String[]{"if [ \"$(getprop ro.build.system_root_image)\" == \"true\" ]; then export SYSTEM=/; else export SYSTEM=/system;fi;mount -o rw,remount $SYSTEM && rm /system/xbin/busybox;ln -s /system/xbin/busybox_nh /system/xbin/busybox"});
      Toast.makeText(getActivity().getApplicationContext(), "Default system BusyBox has been changed", Toast.LENGTH_SHORT).show();
    });
    return rootView;
  }

  private void bootanimation_start() {
    final VideoView videoview = getActivity().findViewById(R.id.videoView);
    videoview.requestFocus();
    videoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      public void onPrepared(MediaPlayer mp) {
        videoview.start();
        mp.setLooping(true);
      }
    });
  }

  public void onActivityResult(int requestCode, int resultCode,
                               Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 1001) {
      if (resultCode == Activity.RESULT_OK) {
        ShellExecuter exe = new ShellExecuter();
        EditText ResotreFileName = getActivity().findViewById(R.id.restorefilename);
        String FilePath = data.getData().getPath();
        FilePath = exe.RunAsRootOutput("echo " + FilePath + " | sed -e 's/\\/document\\/primary:/\\/sdcard\\//g' ");
        ResotreFileName.setText(FilePath);
      }
    }
  }

  public void SetupDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    sharedpreferences = activity.getSharedPreferences("com.team420.kekhunter", Context.MODE_PRIVATE);
    builder.setTitle("Welcome to Settings!");
    builder.setMessage("In order to make sure everything is working, an initial setup needs to be done.");
    builder.setPositiveButton("Check & Install", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        RunSetup();
        sharedpreferences.edit().putBoolean("animation_setup_done", true).apply();
      }
    });
    builder.show();

  }

  public void RunSetup() {
    sharedpreferences = activity.getSharedPreferences("com.team420.kekhunter", Context.MODE_PRIVATE);
    run_cmd("echo -ne \"\\033]0;Bootanimation Setup\\007\" && clear;if [[ -f /usr/bin/convert ]];then echo 'Imagemagick is installed!'; else " +
      "apt-get update && apt-get install imagemagick -y;fi; if [[ -f /root/nethunter-bootanimation ]];then echo 'Nethunter-bootanimation is installed!'; else " +
      "git clone https://gitlab.com/kalilinux/nethunter/apps/kali-nethunter-bootanimation /root/nethunter-bootanimation;fi; echo 'Everything is ready! Closing in 3secs..'; sleep 3 && exit ");
    sharedpreferences.edit().putBoolean("animation_setup_done", true).apply();
  }

  public void RunUpdate() {
    sharedpreferences = activity.getSharedPreferences("com.team420.kekhunter", Context.MODE_PRIVATE);
    run_cmd("echo -ne \"\\033]0;Bootanimation Update\\007\" && clear;apt-get update && apt-get install imagemagick -y;if [[ -d /root/nethunter-bootanimation ]];then cd /root/nethunter-bootanimation;git pull" +
      ";fi; echo 'Done! Closing in 3secs..'; sleep 3 && exit ");
    sharedpreferences.edit().putBoolean("animation_setup_done", true).apply();
  }


  private void addClickListener(Button _button, View.OnClickListener onClickListener) {
    _button.setOnClickListener(onClickListener);
  }

  public void run_cmd(String cmd) {
    Intent intent = Bridge.createExecuteIntent("/data/data/hilled.pwnterm/files/usr/bin/kali", cmd);
    startActivity(intent);
  }

  public void run_cmd_android(String cmd) {
    Intent intent = Bridge.createExecuteIntent("/data/data/hilled.pwnterm/files/usr/bin/android-su", cmd);
    startActivity(intent);
  }

}
