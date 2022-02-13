package com.team420.kekhunter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.*;
import android.webkit.WebView;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.team420.kekhunter.utils.NhPaths;
import com.team420.kekhunter.utils.ShellExecuter;
import hilled.pwnterm.bridge.Bridge;

public class SETFragment extends Fragment {

  private ViewPager mViewPager;
  private SharedPreferences sharedpreferences;
  private NhPaths nh;
  private Context context;
  private Activity activity;
  private static final String ARG_SECTION_NUMBER = "section_number";

  public static SETFragment newInstance(int sectionNumber) {
    SETFragment fragment = new SETFragment();
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

    View rootView = inflater.inflate(R.layout.set, container, false);
    SETFragment.TabsPagerAdapter tabsPagerAdapter = new TabsPagerAdapter(getChildFragmentManager());

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
    builder.setTitle("Welcome to SET!");
    builder.setMessage("In order to make sure everything is working, an initial setup needs to be done.");
    builder.setPositiveButton("Check & Install", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        RunSetup();
        sharedpreferences.edit().putBoolean("set_setup_done", true).apply();
      }
    });
    builder.show();

  }

  public void RunSetup() {
    sharedpreferences = activity.getSharedPreferences("com.team420.kekhunter", Context.MODE_PRIVATE);
    run_cmd("echo -ne \"\\033]0;SET Setup\\007\" && clear;if [[ -d /root/setoolkit ]]; then echo 'SET is already installed'" +
      ";else git clone https://github.com/yesimxev/social-engineer-toolkit /root/setoolkit && echo 'Successfully installed SET!';fi; echo 'Closing in 3secs..'; sleep 3 && exit ");
    sharedpreferences.edit().putBoolean("set_setup_done", true).apply();
  }

  public void RunUpdate() {
    sharedpreferences = activity.getSharedPreferences("com.team420.kekhunter", Context.MODE_PRIVATE);
    run_cmd("echo -ne \"\\033]0;SET Update\\007\" && clear;if [[ -d /root/setoolkit ]]; then cd /root/setoolkit && git pull && echo 'Succesfully updated SET! Closing in 3secs..';else echo 'Please run SETUP first!';fi; sleep 3 && exit ");
    sharedpreferences.edit().putBoolean("set_setup_done", true).apply();
  }

  public static class TabsPagerAdapter extends FragmentPagerAdapter {

    TabsPagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int i) {
      switch (i) {
        default:
          return new SETFragment.MainFragment();
      }
    }

    @Override
    public Parcelable saveState() {
      return null;
    }

    @Override
    public int getCount() {
      return 1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      switch (position) {
        default:
          return "Email Template";
      }
    }
  }

  public static class MainFragment extends SETFragment {
    private Context context;
    private NhPaths nh;
    final ShellExecuter exe = new ShellExecuter();
    private String selected_template;
    private String template_src;
    private String template_tempfile;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

      View rootView = inflater.inflate(R.layout.set_main, container, false);
      SharedPreferences sharedpreferences = context.getSharedPreferences("com.team420.kekhunter", Context.MODE_PRIVATE);
      EditText PhishName = rootView.findViewById(R.id.set_name);
      EditText PhishSubject = rootView.findViewById(R.id.set_subject);

      //First run
      Boolean setupdone = sharedpreferences.getBoolean("set_setup_done", false);
      if (!setupdone.equals(true))
        SetupDialog();

      //Templates spinner
      String[] templates = new String[]{"Messenger", "Facebook", "Twitter"};
      Spinner template_spinner = rootView.findViewById(R.id.set_template);
      template_spinner.setAdapter(new ArrayAdapter<>(getContext(),
        android.R.layout.simple_list_item_1, templates));

      //Select Template
      WebView myBrowser = rootView.findViewById(R.id.mybrowser);
      template_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
          selected_template = parentView.getItemAtPosition(pos).toString();
          if (selected_template.equals("Messenger")) {
            template_src = nh.APP_SD_FILES_PATH + "/configs/set-messenger.html";
            template_tempfile = "set-messenger.html";
            PhishSubject.setText(PhishName.getText() + " sent you a message on Messenger.");
          } else if (selected_template.equals("Facebook")) {
            template_src = nh.APP_SD_FILES_PATH + "/configs/set-facebook.html";
            template_tempfile = "set-facebook.html";
            PhishSubject.setText(PhishName.getText() + " sent you a message on Facebook.");
          } else if (selected_template.equals("Twitter")) {
            template_src = nh.APP_SD_FILES_PATH + "/configs/set-twitter.html";
            template_tempfile = "set-twitter.html";
            PhishSubject.setText(PhishName.getText() + " sent you a Direct Message on Twitter!");
          }
          myBrowser.clearCache(true);
          myBrowser.loadUrl(template_src);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {
        }
      });

      Button ResetTemplate = rootView.findViewById(R.id.reset_template);
      Button SaveTemplate = rootView.findViewById(R.id.save_template);
      Button LaunchSET = rootView.findViewById(R.id.start_set);

      //Refresh Template
      Button RefreshPreview = rootView.findViewById(R.id.refreshPreview);
      RefreshPreview.setOnClickListener(v -> {
        refresh(rootView);
      });

      //Reset Template
      ResetTemplate.setOnClickListener(v -> {
        myBrowser.clearCache(true);
        myBrowser.loadUrl(template_src);
      });

      //Save Template
      SaveTemplate.setOnClickListener(v -> {
        refresh(rootView);
        final String template_path = "/sdcard/" + template_tempfile;
        String template_final = "/root/setoolkit/src/templates/" + selected_template + ".template";
        String phish_subject = PhishSubject.getText().toString();
        exe.RunAsChrootOutput("echo 'SUBJECT=\"" + phish_subject + "\"' > " + template_final + " && echo 'HTML=\"' >> " + template_final +
          " && cat " + template_path + " >> " + template_final + " && echo '\\nEND\"' >> " + template_final);
        Toast.makeText(getActivity().getApplicationContext(), "Successfully saved to SET templates folder", Toast.LENGTH_SHORT).show();
      });

      //Launch SET
      LaunchSET.setOnClickListener(v -> {
        run_cmd("echo -ne \"\\033]0;SET\\007\" && clear;cd /root/setoolkit && ./setoolkit");
      });

      return rootView;
    }

    private void refresh(View SETFragment) {
      WebView myBrowser = SETFragment.findViewById(R.id.mybrowser);
      final String template_path = nh.SD_PATH + "/" + template_tempfile;

      //Setting fields
      EditText PhishLink = SETFragment.findViewById(R.id.set_link);
      EditText PhishName = SETFragment.findViewById(R.id.set_name);
      EditText PhishPic = SETFragment.findViewById(R.id.set_pic);
      EditText PhishSubject = SETFragment.findViewById(R.id.set_subject);

      exe.RunAsRoot(new String[]{"cp " + template_src + " " + nh.SD_PATH});

      String phish_link = PhishLink.getText().toString();
      String phish_name = PhishName.getText().toString();
      String phish_pic = PhishPic.getText().toString();

      if (selected_template.equals("Messenger")) {
        PhishSubject.setText(PhishName.getText() + " sent you a message on Messenger.");
      } else if (selected_template.equals("Facebook")) {
        PhishSubject.setText(PhishName.getText() + " sent you a message on Facebook.");
      } else if (selected_template.equals("Twitter")) {
        PhishSubject.setText(PhishName.getText() + " sent you a Direct Message on Twitter!");
      }

      if (!phish_link.equals("")) {
        if (phish_link.contains("&"))
          phish_link = exe.RunAsRootOutput("sed 's/\\&/\\\\\\&/g' <<< \"" + phish_link + "\"");
        phish_link = exe.RunAsRootOutput("sed 's|\\/|\\\\\\/|g' <<< \"" + phish_link + "\"");
        exe.RunAsRoot(new String[]{"sed -i 's/https\\:\\/\\/www.google.com/" + phish_link + "/g' " + template_path});
      }
      if (!phish_name.equals(""))
        exe.RunAsRoot(new String[]{"sed -i 's/E Corp/" + phish_name + "/g' " + template_path});
      if (!phish_pic.equals("")) {
        if (phish_pic.contains("&")) phish_pic = exe.RunAsRootOutput("sed 's/\\&/\\\\\\&/g' <<< \"" + phish_pic + "\"");
        exe.RunAsRoot(new String[]{"sed -i \"s|id=\\\"set\\\".*|id=\\\"set\\\" src=\\\"" + phish_pic + "\\\" width=\\\"72\\\">|\" " + template_path});
      }
      myBrowser.clearCache(true);
      myBrowser.loadUrl(template_path);
    }
  }

  public void run_cmd(String cmd) {
    Intent intent = Bridge.createExecuteIntent("/data/data/hilled.pwnterm/files/usr/bin/kali", cmd);
    startActivity(intent);
  }

}
