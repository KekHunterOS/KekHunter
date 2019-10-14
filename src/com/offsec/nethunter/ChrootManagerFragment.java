package com.offsec.nethunter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.offsec.nethunter.AsyncTask.ChrootManagerAsynctask;
import com.offsec.nethunter.service.CompatCheckService;
import com.offsec.nethunter.service.NotificationChannelService;
import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.SharePrefTag;
import com.offsec.nethunter.utils.ShellExecuter;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;


public class ChrootManagerFragment extends Fragment {

    public static final String TAG = "ChrootManager";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String IMAGE_SERVER = "images.offensive-security.com";
    private static String ARCH = "";
    private static String MINORFULL = "";
    private final ShellExecuter x = new ShellExecuter();
    private TextView mountStatsTextView;
    private TextView baseChrootPathTextView;
    private TextView resultViewerLoggerTextView;
    private TextView kaliFolderTextView;
    private Button kaliFolderEditButton;
    private Button mountChrootButton;
    private Button unmountChrootButton;
    private Button installChrootButton;
    private Button addMetaPkgButton;
    private Button removeChrootButton;
    private Button backupChrootButton;
    private SharedPreferences sharedPreferences;
    private ChrootManagerAsynctask chrootManagerAsynctask;
    private Intent backPressedintent = new Intent();
    private static final int IS_MOUNTED = 0;
    private static final int IS_UNMOUNTED = 1;
    private static final int NEED_TO_INSTALL = 2;
    public static boolean isAsyncTaskRunning = false;
    private Context context;
    private Activity activity;


    public static ChrootManagerFragment newInstance(int sectionNumber) {
        ChrootManagerFragment fragment = new ChrootManagerFragment();
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
        View rootView = inflater.inflate(R.layout.chroot_manager, container, false);
        sharedPreferences = activity.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        baseChrootPathTextView = rootView.findViewById(R.id.f_chrootmanager_base_path_tv);
        mountStatsTextView = rootView.findViewById(R.id.f_chrootmanager_mountresult_tv);
        resultViewerLoggerTextView = rootView.findViewById(R.id.f_chrootmanager_viewlogger);
        kaliFolderTextView = rootView.findViewById(R.id.f_chrootmanager_kalifolder_tv);
        kaliFolderEditButton = rootView.findViewById(R.id.f_chrootmanager_edit_btn);
        mountChrootButton = rootView.findViewById(R.id.f_chrootmanager_mount_btn);
        unmountChrootButton = rootView.findViewById(R.id.f_chrootmanager_unmount_btn);
        installChrootButton = rootView.findViewById(R.id.f_chrootmanager_install_btn);
        addMetaPkgButton = rootView.findViewById(R.id.f_chrootmanager_addmetapkg_btn);
        removeChrootButton = rootView.findViewById(R.id.f_chrootmanager_removechroot_btn);
        backupChrootButton = rootView.findViewById(R.id.f_chrootmanager_backupchroot_btn);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        resultViewerLoggerTextView.setMovementMethod(new ScrollingMovementMethod());
        kaliFolderTextView.setClickable(true);
        kaliFolderTextView.setText(sharedPreferences.getString(SharePrefTag.CHROOT_ARCH_SHAREPREF_TAG, NhPaths.ARCH_FOLDER));
        LinearLayoutCompat kaliViewFolderlinearLayout = view.findViewById(R.id.f_chrootmanager_viewholder);
        kaliViewFolderlinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(activity)
                        .setMessage(baseChrootPathTextView.getText().toString() +
                                kaliFolderTextView.getText().toString())
                        .create().show();
            }
        });
        setEditButton();
        setStopKaliButton();
        setStartKaliButton();
        setInstallChrootButton();
        setRemoveChrootButton();
        setAddMetaPkgButton();
        setBackupChrootButton();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isAsyncTaskRunning){
            showBanner();
            compatCheck();
        }
    }

    private void setEditButton(){
        kaliFolderEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder adb = new AlertDialog.Builder(activity);
                AlertDialog ad = adb.create();
                LinearLayout ll = new LinearLayout(activity);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                ll.setOrientation(LinearLayout.VERTICAL);
                ll.setLayoutParams(layoutParams);
                EditText chrootPathEditText = new EditText(activity);
                TextView availableChrootPathextview = new TextView(activity);
                LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                editTextParams.setMargins(58,0,58,0);
                chrootPathEditText.setText(sharedPreferences.getString(SharePrefTag.CHROOT_ARCH_SHAREPREF_TAG, ""));
                chrootPathEditText.setSingleLine();
                chrootPathEditText.setLayoutParams(editTextParams);
                availableChrootPathextview.setLayoutParams(editTextParams);
                availableChrootPathextview.setTextColor(getResources().getColor(R.color.clearTitle));
                availableChrootPathextview.setText("\n List of available folder(s) in\n\"" + NhPaths.NH_SYSTEM_PATH + "/\":\n\n");
                File chrootDir = new File(NhPaths.NH_SYSTEM_PATH);
                int count = 0;
                for (File file: chrootDir.listFiles()){
                    if (file.isDirectory()){
                        count += 1;
                        availableChrootPathextview.append("    " + count + ". " + file.getName() + "\n");
                    }
                }
                ll.addView(chrootPathEditText);
                ll.addView(availableChrootPathextview);
                ad.setCancelable(true);
                ad.setTitle("Setup Chroot Path");
                ad.setMessage("The Chroot Path is prefixed to \n\"/data/local/nhsystem/\"\n\n" +
                            "Just put the basename of your Kali Chroot Folder:");
                ad.setView(ll);
                ad.setButton(Dialog.BUTTON_POSITIVE, "Apply", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (chrootPathEditText.getText().toString().matches("^\\.(.*$)|^\\.\\.(.*$)|^/+(.*$)|^.*/+(.*$)|^$")){
                                NhPaths.showMessage(activity, "Invilad Name, please try again.");
                            } else {
                                NhPaths.ARCH_FOLDER = chrootPathEditText.getText().toString();
                                kaliFolderTextView.setText(NhPaths.ARCH_FOLDER);
                                sharedPreferences.edit().putString(SharePrefTag.CHROOT_ARCH_SHAREPREF_TAG, NhPaths.ARCH_FOLDER).commit();
                                sharedPreferences.edit().putString(SharePrefTag.CHROOT_PATH_SHAREPREF_TAG, NhPaths.CHROOT_PATH()).commit();
                                compatCheck();
                            }
                            dialogInterface.dismiss();
                        }
                    });
                ad.show();
            }
        });
    }

    private void setStartKaliButton() {
        mountChrootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chrootManagerAsynctask = new ChrootManagerAsynctask(ChrootManagerAsynctask.MOUNT_CHROOT);
                chrootManagerAsynctask.setListener(new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
                    @Override
                    public void onAsyncTaskPrepare() {
                        setAllButtonEnable(false);
                    }

                    @Override
                    public void onAsyncTaskProgressUpdate(int progress) {

                    }

                    @Override
                    public void onAsyncTaskFinished(int resultCode, ArrayList<String> resultString) {
                        if (resultCode == 0){
                            setButtonVisibilty(IS_MOUNTED);
                            setMountStatsTextView(IS_MOUNTED);
                            setAllButtonEnable(true);
                            compatCheck();
                            context.startService(new Intent(context, NotificationChannelService.class).setAction(NotificationChannelService.USENETHUNTER));
                        }
                    }
                });
                chrootManagerAsynctask.execute(resultViewerLoggerTextView);
            }
        });
    }

    private void setStopKaliButton(){
        unmountChrootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chrootManagerAsynctask = new ChrootManagerAsynctask(ChrootManagerAsynctask.UNMOUNT_CHROOT);
                chrootManagerAsynctask.setListener(new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
                    @Override
                    public void onAsyncTaskPrepare() {
                        setAllButtonEnable(false);
                    }

                    @Override
                    public void onAsyncTaskProgressUpdate(int progress) {

                    }

                    @Override
                    public void onAsyncTaskFinished(int resultCode, ArrayList<String> resultString) {
                        if (resultCode == 0){
                            setMountStatsTextView(IS_UNMOUNTED);
                            setButtonVisibilty(IS_UNMOUNTED);
                            setAllButtonEnable(true);
                            compatCheck();
                        }
                    }
                });
                chrootManagerAsynctask.execute(resultViewerLoggerTextView);
            }
        });
    }

    private void setInstallChrootButton(){
        installChrootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder adb = new AlertDialog.Builder(activity);
                AlertDialog ad = adb.create();
                LinearLayout ll = new LinearLayout(activity);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                ll.setOrientation(LinearLayout.VERTICAL);
                ll.setLayoutParams(layoutParams);
                Button downloadButton1 = new Button(activity);
                Button restoreButton2 = new Button(activity);
                LinearLayout.LayoutParams optionButtonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                optionButtonParams.setMargins(58,0,58,0);
                downloadButton1.setText("DOWNLOAD LATEST KALI CHROOT FROM OFFICIAL IMAGE");
                downloadButton1.setLayoutParams(optionButtonParams);
                restoreButton2.setText("RESTORE FROM LOCAL STORAGE\n(.TAR.GZ /.TAR.XZ)");
                restoreButton2.setLayoutParams(optionButtonParams);
                ll.addView(downloadButton1);
                ll.addView(restoreButton2);
                ad.setView(ll);
                ad.show();
                downloadButton1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ad.dismiss();
                        AlertDialog.Builder adb1 = new AlertDialog.Builder(activity);
                        View promtDownloadView = getLayoutInflater().inflate(R.layout.chroot_manager_download_diaglog, null);
                        EditText storepathEditText = promtDownloadView.findViewById(R.id.f_chrootmanager_storepath_et);
                        Spinner archSpinner = promtDownloadView.findViewById(R.id.f_chrootmanager_arch_adb_spr);
                        Spinner minorfullSpinner = promtDownloadView.findViewById(R.id.f_chrootmanager_minorfull_adb_spr);
                        storepathEditText.setText(sharedPreferences.getString(SharePrefTag.CHROOT_DEFAULT_STORE_DOWNLOAD_SHAREPREF_TAG, NhPaths.SD_PATH + "/Download"));
                        adb1.setView(promtDownloadView);
                        adb1.setMessage("Select the options below:");
                        adb1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                File downloadDir = new File(storepathEditText.getText().toString());
                                if (downloadDir.isDirectory() && downloadDir.canWrite()) {
                                    sharedPreferences.edit().putString(SharePrefTag.CHROOT_DEFAULT_STORE_DOWNLOAD_SHAREPREF_TAG, downloadDir.getAbsolutePath()).apply();
                                    if (archSpinner.getSelectedItemPosition() == 0) {
                                        ARCH = "arm64";
                                    } else ARCH = "armhf";
                                    if (minorfullSpinner.getSelectedItemPosition() == 0){
                                        MINORFULL = "full";
                                    } else MINORFULL = "minimal";
                                    String targetDownloadFileName = "kalifs-" + ARCH + "-" + MINORFULL + ".tar.xz";
                                    if (new File(downloadDir.getAbsoluteFile() + "/" + targetDownloadFileName).exists()){
                                        AlertDialog.Builder adb3 = new AlertDialog.Builder(activity);
                                        adb3.setMessage(downloadDir.getAbsoluteFile() + "/" + targetDownloadFileName + " exists. Do you want to overwrite it?");
                                        adb3.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                context.startService(new Intent(context, NotificationChannelService.class).setAction(NotificationChannelService.DOWNLOADING));
                                                startDownloadChroot(targetDownloadFileName, downloadDir);
                                            }
                                        });
                                        adb3.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        });
                                        adb3.create().show();
                                    } else {

                                        context.startService(new Intent(context, NotificationChannelService.class).setAction(NotificationChannelService.DOWNLOADING));
                                        startDownloadChroot(targetDownloadFileName, downloadDir);
                                        }
                                } else {
                                    NhPaths.showMessage_long(context, downloadDir.getAbsolutePath() + " is not a Directory or cannot be accessed.");
                                    dialogInterface.dismiss();
                                }
                            }
                        });
                        adb1.create().show();
                    }
                });
                restoreButton2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder adb2 = new AlertDialog.Builder(activity);
                        LinearLayout ll = new LinearLayout(activity);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        ll.setOrientation(LinearLayout.VERTICAL);
                        ll.setLayoutParams(layoutParams);
                        TextView chrootTarHintTextView = new TextView(activity);
                        EditText chrootTarFileEditText = new EditText(activity);
                        LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        editTextParams.setMargins(58,40,58,0);
                        chrootTarHintTextView.setText("Type the full path of your Kali Chroot tarball file:");
                        chrootTarHintTextView.setLayoutParams(editTextParams);
                        chrootTarFileEditText.setText(sharedPreferences.getString(SharePrefTag.CHROOT_DEFAULT_BACKUP_SHAREPREF_TAG, ""));
                        chrootTarFileEditText.setLayoutParams(editTextParams);
                        ll.addView(chrootTarHintTextView);
                        ll.addView(chrootTarFileEditText);
                        adb2.setView(ll);
                        adb2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                sharedPreferences.edit().putString(SharePrefTag.CHROOT_DEFAULT_BACKUP_SHAREPREF_TAG, chrootTarFileEditText.getText().toString()).apply();
                                chrootManagerAsynctask = new ChrootManagerAsynctask(ChrootManagerAsynctask.INSTALL_CHROOT);
                                chrootManagerAsynctask.setListener(new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
                                    @Override
                                    public void onAsyncTaskPrepare() {

                                        context.startService(new Intent(context, NotificationChannelService.class).setAction(NotificationChannelService.INSTALLING));
                                        broadcastBackPressedIntent(false);
                                        setAllButtonEnable(false);
                                    }

                                    @Override
                                    public void onAsyncTaskProgressUpdate(int progress) {

                                    }

                                    @Override
                                    public void onAsyncTaskFinished(int resultCode, ArrayList<String> resultString) {
                                        broadcastBackPressedIntent(true);
                                        setAllButtonEnable(true);
                                        compatCheck();
                                    }
                                });
                                chrootManagerAsynctask.execute(resultViewerLoggerTextView, chrootTarFileEditText.getText().toString(), NhPaths.CHROOT_PATH());
                            }
                        });
                        AlertDialog ad2 = adb2.create();
                        ad2.show();
                        ad.cancel();
                    }
                });
            }
        });
    }

    private void setRemoveChrootButton(){
        removeChrootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder adb = new AlertDialog.Builder(activity)
                        .setTitle("Warning!")
                        .setMessage("Are you sure to remove the below Kali Chroot folder?\n" + NhPaths.CHROOT_PATH())
                        .setPositiveButton("I'm sure.", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                AlertDialog.Builder adb = new AlertDialog.Builder(activity)
                                    .setTitle("Warning!")
                                    .setMessage("This is your last chance!")
                                    .setPositiveButton("Just do it.", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            chrootManagerAsynctask = new ChrootManagerAsynctask(ChrootManagerAsynctask.REMOVE_CHROOT);
                                            chrootManagerAsynctask.setListener(new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
                                                @Override
                                                public void onAsyncTaskPrepare() {
                                                    broadcastBackPressedIntent(false);
                                                    setAllButtonEnable(false);
                                                }

                                                @Override
                                                public void onAsyncTaskProgressUpdate(int progress) {

                                                }

                                                @Override
                                                public void onAsyncTaskFinished(int resultCode, ArrayList<String> resultString) {
                                                    broadcastBackPressedIntent(true);
                                                    setAllButtonEnable(true);
                                                    compatCheck();
                                                }
                                            });
                                            chrootManagerAsynctask.execute(resultViewerLoggerTextView);
                                        }
                                    })
                                    .setNegativeButton("Okay, I'm sorry.", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    });
                                adb.create().show();
                            }
                        })
                        .setNegativeButton("Forget it.", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                adb.create().show();
            }
        });
    }

    private void startDownloadChroot(String targetDownloadFileName, File downloadDir){
        ProgressDialog progressDialog = new ProgressDialog(activity);
        chrootManagerAsynctask = new ChrootManagerAsynctask(ChrootManagerAsynctask.DOWNLOAD_CHROOT);
        chrootManagerAsynctask.setListener(new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {
                broadcastBackPressedIntent(false);
                setAllButtonEnable(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setTitle("Downloading " + targetDownloadFileName);
                progressDialog.setMessage("Please do NOT kill the app or clear recent apps..");
                progressDialog.setProgress(0);
                progressDialog.setMax(100);
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            public void onAsyncTaskProgressUpdate(int progress) {
                if (progress == 100) {
                    progressDialog.dismiss();
                } else {
                    progressDialog.setProgress(progress);
                }
            }

            @Override
            public void onAsyncTaskFinished(int resultCode, ArrayList<String> resultString) {
                broadcastBackPressedIntent(true);
                setAllButtonEnable(true);
                if (resultCode == 0) {
                    chrootManagerAsynctask = new ChrootManagerAsynctask(ChrootManagerAsynctask.INSTALL_CHROOT);
                    chrootManagerAsynctask.setListener(new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
                        @Override
                        public void onAsyncTaskPrepare() {
                            context.startService(new Intent(context, NotificationChannelService.class).setAction(NotificationChannelService.INSTALLING));
                            broadcastBackPressedIntent(false);
                            setAllButtonEnable(false);
                        }

                        @Override
                        public void onAsyncTaskProgressUpdate(int progress) {

                        }

                        @Override
                        public void onAsyncTaskFinished(int resultCode, ArrayList<String> resultString) {
                            broadcastBackPressedIntent(true);
                            setAllButtonEnable(true);
                            compatCheck();
                        }
                    });
                    chrootManagerAsynctask.execute(resultViewerLoggerTextView, downloadDir.getAbsolutePath() + "/" + targetDownloadFileName, NhPaths.CHROOT_PATH());
                } else {
                    progressDialog.dismiss();
                }
            }
        });
        chrootManagerAsynctask.execute(resultViewerLoggerTextView, IMAGE_SERVER, "/nethunter/" + targetDownloadFileName, downloadDir.getAbsolutePath() + "/" + targetDownloadFileName);
    }

    private void setAddMetaPkgButton() {
        addMetaPkgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //for now, we'll hardcode packages in the dialog view.  At some point we'll want to grab them automatically.
                AlertDialog.Builder adb = new AlertDialog.Builder(activity);
                adb.setTitle("Metapackage Install & Upgrade");
                LayoutInflater inflater = activity.getLayoutInflater();
                @SuppressLint("InflateParams") final ScrollView sv = (ScrollView) inflater.inflate(R.layout.metapackagechooser, null);
                adb.setView(sv);
                Button metapackageButton = sv.findViewById(R.id.metapackagesWeb);
                metapackageButton.setOnClickListener(v -> {
                    String metapackagesURL = "http://tools.kali.org/kali-metapackages";
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(metapackagesURL));
                    startActivity(browserIntent);
                });
                adb.setPositiveButton(R.string.InstallAndUpdateButtonText, (dialog, which) -> {
                    StringBuilder sb = new StringBuilder();
                    CheckBox cb;
                    // now grab all the checkboxes in the dialog and check their status
                    // thanks to "user2" for a 2-line sample of how to get the dialog's view:  http://stackoverflow.com/a/13959585/3035127
                    AlertDialog d = (AlertDialog) dialog;
                    LinearLayout ll = d.findViewById(R.id.metapackageLinearLayout);
                    int children = Objects.requireNonNull(ll).getChildCount();
                    for (int cnt = 0; cnt < children; cnt++) {
                        if (ll.getChildAt(cnt) instanceof CheckBox) {
                            cb = (CheckBox) ll.getChildAt(cnt);
                            if (cb.isChecked()) {
                                sb.append(cb.getText()).append(" ");
                            }
                        }
                    }
                    try {
                        Intent intent = new Intent("com.offsec.nhterm.RUN_SCRIPT_NH");
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.putExtra("com.offsec.nhterm.iInitialCommand", NhPaths.makeTermTitle("Updating") + "apt update && apt install " + sb.toString() + " -y && echo \"(You can close the terminal now)\n\"");
                        startActivity(intent);
                    } catch (Exception e) {
                        NhPaths.showMessage(context, getString(R.string.toast_install_terminal));
                    }


                });
                AlertDialog ad = adb.create();
                ad.setCancelable(true);
                ad.show();
            }
        });
    }

    private void setBackupChrootButton() {
        backupChrootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog ad = new AlertDialog.Builder(activity).create();
                EditText backupFullPathEditText = new EditText(activity);
                LinearLayout ll = new LinearLayout(activity);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                ll.setOrientation(LinearLayout.VERTICAL);
                ll.setLayoutParams(layoutParams);
                LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                editTextParams.setMargins(58,40,58,0);
                backupFullPathEditText.setLayoutParams(editTextParams);
                ll.addView(backupFullPathEditText);
                ad.setView(ll);
                ad.setTitle("Backup Chroot");
                ad.setMessage("* It is strongly suggested to create your backup chroot as tar.gz format just for faster process but bigger file size.\n\nbackup \"" + NhPaths.CHROOT_PATH() + "\" to:" );
                backupFullPathEditText.setText(sharedPreferences.getString(SharePrefTag.CHROOT_DEFAULT_BACKUP_SHAREPREF_TAG, ""));
                ad.setButton(Dialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sharedPreferences.edit().putString(SharePrefTag.CHROOT_DEFAULT_BACKUP_SHAREPREF_TAG, backupFullPathEditText.getText().toString()).apply();
                        if (new File(backupFullPathEditText.getText().toString()).exists()){
                            ad.dismiss();
                            AlertDialog ad2 = new AlertDialog.Builder(activity).create();
                            ad2.setMessage("File exists already, do you want ot overwrite it anyway?");
                            ad2.setButton(Dialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    chrootManagerAsynctask = new ChrootManagerAsynctask(ChrootManagerAsynctask.BACKUP_CHROOT);
                                    chrootManagerAsynctask.setListener(new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
                                        @Override
                                        public void onAsyncTaskPrepare() {
                                            context.startService(new Intent(context, NotificationChannelService.class).setAction(NotificationChannelService.BACKINGUP));
                                            broadcastBackPressedIntent(false);
                                            setAllButtonEnable(false);
                                        }

                                        @Override
                                        public void onAsyncTaskProgressUpdate(int progress) {

                                        }

                                        @Override
                                        public void onAsyncTaskFinished(int resultCode, ArrayList<String> resultString) {
                                            broadcastBackPressedIntent(true);
                                            setAllButtonEnable(true);
                                        }
                                    });
                                    chrootManagerAsynctask.execute(resultViewerLoggerTextView, NhPaths.CHROOT_PATH(), backupFullPathEditText.getText().toString());
                                }
                            });
                            ad2.show();
                        } else {
                            chrootManagerAsynctask = new ChrootManagerAsynctask(ChrootManagerAsynctask.BACKUP_CHROOT);
                            chrootManagerAsynctask.setListener(new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
                                @Override
                                public void onAsyncTaskPrepare() {
                                    context.startService(new Intent(context, NotificationChannelService.class).setAction(NotificationChannelService.BACKINGUP));
                                    broadcastBackPressedIntent(false);
                                    setAllButtonEnable(false);
                                }

                                @Override
                                public void onAsyncTaskProgressUpdate(int progress) {

                                }

                                @Override
                                public void onAsyncTaskFinished(int resultCode, ArrayList<String> resultString) {
                                    broadcastBackPressedIntent(true);
                                    setAllButtonEnable(true);
                                }
                            });
                            chrootManagerAsynctask.execute(resultViewerLoggerTextView, NhPaths.CHROOT_PATH(), backupFullPathEditText.getText().toString());
                        }
                    }
                });
                ad.show();
            }
        });
    }

    private void showBanner() {
        chrootManagerAsynctask = new ChrootManagerAsynctask(ChrootManagerAsynctask.ISSUE_BANNER);
        chrootManagerAsynctask.execute(resultViewerLoggerTextView, getResources().getString(R.string.aboutchroot));
    }

    private void compatCheck() {
        chrootManagerAsynctask = new ChrootManagerAsynctask(ChrootManagerAsynctask.CHECK_CHROOT);
        chrootManagerAsynctask.setListener(new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() { }

            @Override
            public void onAsyncTaskProgressUpdate(int progress) { }

            @Override
            public void onAsyncTaskFinished(int resultCode, ArrayList<String> resultString) {
                setButtonVisibilty(resultCode);
                setMountStatsTextView(resultCode);
                setAllButtonEnable(true);
                context.startService(new Intent(context, CompatCheckService.class).putExtra("RESULTCODE", resultCode));
            }
        });
        chrootManagerAsynctask.execute(resultViewerLoggerTextView, sharedPreferences.getString(SharePrefTag.CHROOT_PATH_SHAREPREF_TAG, ""));
    }

    private void setMountStatsTextView(int MODE) {
        if (MODE == IS_MOUNTED) {
            mountStatsTextView.setTextColor(Color.GREEN);
            mountStatsTextView.setText("Kali Chroot is now running!");
        } else if  (MODE == IS_UNMOUNTED) {
            mountStatsTextView.setTextColor(Color.CYAN);
            mountStatsTextView.setText("Kali Chroot has not yet started!");
        } else if  (MODE == NEED_TO_INSTALL) {
            mountStatsTextView.setTextColor(Color.parseColor("#D81B60"));
            mountStatsTextView.setText("Kali Chroot is not yet installed!");
        }
    }

    private void setButtonVisibilty(int MODE) {
        switch (MODE) {
            case IS_MOUNTED:
                mountChrootButton.setVisibility(View.GONE);
                unmountChrootButton.setVisibility(View.VISIBLE);
                installChrootButton.setVisibility(View.GONE);
                addMetaPkgButton.setVisibility(View.VISIBLE);
                removeChrootButton.setVisibility(View.GONE);
                backupChrootButton.setVisibility(View.GONE);
                break;
            case IS_UNMOUNTED:
                mountChrootButton.setVisibility(View.VISIBLE);
                unmountChrootButton.setVisibility(View.GONE);
                installChrootButton.setVisibility(View.GONE);
                addMetaPkgButton.setVisibility(View.GONE);
                removeChrootButton.setVisibility(View.VISIBLE);
                backupChrootButton.setVisibility(View.VISIBLE);
                break;
            case NEED_TO_INSTALL:
                mountChrootButton.setVisibility(View.GONE);
                unmountChrootButton.setVisibility(View.GONE);
                installChrootButton.setVisibility(View.VISIBLE);
                addMetaPkgButton.setVisibility(View.GONE);
                removeChrootButton.setVisibility(View.GONE);
                backupChrootButton.setVisibility(View.GONE);
                break;
        }
    }

    private void setAllButtonEnable(boolean isEnable){
        mountChrootButton.setEnabled(isEnable);
        unmountChrootButton.setEnabled(isEnable);
        installChrootButton.setEnabled(isEnable);
        addMetaPkgButton.setEnabled(isEnable);
        removeChrootButton.setEnabled(isEnable);
        kaliFolderEditButton.setEnabled(isEnable);
        backupChrootButton.setEnabled(isEnable);
    }

    private void broadcastBackPressedIntent(Boolean isEnabled){
        backPressedintent.setAction(AppNavHomeActivity.NethunterReceiver.BACKPRESSED);
        backPressedintent.putExtra("isEnable", isEnabled);
        context.sendBroadcast(backPressedintent);
        setHasOptionsMenu(isEnabled);
    }
}
