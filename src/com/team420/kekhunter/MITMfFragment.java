package com.team420.kekhunter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.team420.kekhunter.databinding.MitmfGeneralBinding;
import com.team420.kekhunter.databinding.MitmfInjectBinding;
import com.team420.kekhunter.databinding.MitmfResponderBinding;
import com.team420.kekhunter.databinding.MitmfSpoofBinding;
import com.team420.kekhunter.utils.NhPaths;
import com.team420.kekhunter.utils.ShellExecuter;
import hilled.pwnterm.bridge.Bridge;

import java.util.ArrayList;
import java.util.List;

public class MITMfFragment extends Fragment {


  View.OnClickListener checkBoxListener;
  private TabsPagerAdapter tabsPagerAdapter;

  public interface CommandProvider {
    void getCommands(StringBuilder stringBuilder);
  }

  // ^^ \\
  // static String CommandComposed = "";
  private static final ArrayList<String> CommandComposed = new ArrayList<>();

  /* All MITMf Provider Command Variables */

  String M_Responder; // --responder
  String M_Responder_Analyze; // --analyze
  String M_Responder_Fingerprint; // --fingerprint
  String M_Responder_Downgrade; // --lm
  String M_Responder_NBTNS; // --nbtns
  String M_Responder_WPAD; // --wpad
  String M_Responder_WRedir; // --wredir


  private Context context;
  private Activity activity;
  private static final String ARG_SECTION_NUMBER = "section_number";

  public static MITMfFragment newInstance(int sectionNumber) {
    MITMfFragment fragment = new MITMfFragment();
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
    cleanCmd();
    View rootView = inflater.inflate(R.layout.mitmf, container, false);
    tabsPagerAdapter = new TabsPagerAdapter(getChildFragmentManager());

    ViewPager mViewPager = rootView.findViewById(R.id.pagerMITMF);
    mViewPager.setAdapter(tabsPagerAdapter);

    mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
        activity.invalidateOptionsMenu();
      }
    });
    setHasOptionsMenu(true);

    return rootView;
  }

  /* Start execution menu */
  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.mitmf, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.mitmf_menu_start_service:
        start();
        return true;
      case R.id.mitmf_menu_stop_service:
        stop();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void start() {
    StringBuilder sb = new StringBuilder();
    List<CommandProvider> commandProviders = tabsPagerAdapter.getCommandProviders();
    for (CommandProvider provider : commandProviders) {
      provider.getCommands(sb);
    }

    run_cmd("mitmf " + sb.toString());
    NhPaths.showMessage(context, "MITMf Started!");
  }

  private void stop() {
    ShellExecuter exe = new ShellExecuter();
    String[] command = new String[1];
    exe.RunAsRoot(command);
    NhPaths.showMessage(context, "MITMf Stopped!");
  }
  /* Stop execution menu */

  //public static class TabsPagerAdapter extends FragmentPagerAdapter {
  private static class TabsPagerAdapter extends FragmentPagerAdapter {

    private List<CommandProvider> commandProviders = new ArrayList<>(5);

    TabsPagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int i) {
      switch (i) {
        case 0:
          MITMfGeneral fragment = new MITMfGeneral();
          addCommandProvider(fragment);
          return fragment;
        case 1:
          MITMfResponder responder = new MITMfResponder();
          addCommandProvider(responder);
          return responder;
        case 2:
          MITMfInject fragment2 = new MITMfInject();
          addCommandProvider(fragment2);
          return fragment2;
        case 3:
          MITMfSpoof spoofFragment = new MITMfSpoof();
          addCommandProvider(spoofFragment);
          return spoofFragment;
        case 4:
          return new MITMfConfigFragment();
        default:
          return null;
      }
    }

    private void addCommandProvider(CommandProvider fragment) {
      if (!commandProviders.contains(fragment)) {
        commandProviders.add(fragment);
      }
    }

    private List<CommandProvider> getCommandProviders() {
      return commandProviders;
    }

    @Override
    public Parcelable saveState() {
      return null;
    }

    @Override
    public int getCount() {
      return 5;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      switch (position) {
        case 1:
          return "Provider Settings";
        case 2:
          return "Inject Settings";
        case 3:
          return "Spoof Settings";
        case 4:
          return "MITMf Configuration";
        default:
          return "General Settings";
      }
    }
  }
  /* Stop Tabs */

  public static class MITMfGeneral extends Fragment implements CommandProvider {

    private ArrayAdapter<CharSequence> interfaceAdapter;
    private int interfaceSelection;
    private Context context;

    MitmfGeneralBinding generalBinding;
    MITMFViewModel mViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      context = getContext();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      generalBinding = MitmfGeneralBinding.inflate(inflater, container, false);
      mViewModel = new MITMFViewModel();
      generalBinding.setViewModel(mViewModel);

      // Optional Presets Spinner
      Spinner interfaceSpinner = generalBinding.mitmfInterface;
      interfaceAdapter = ArrayAdapter.createFromResource(context,
        R.array.mitmf_interface_array, android.R.layout.simple_spinner_item);
      interfaceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      interfaceSpinner.setAdapter(interfaceAdapter);
      interfaceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
          MITMfGeneral.this.interfaceSelection = pos;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
          MITMfGeneral.this.interfaceSelection = 0;
        }
      });

      // ScreenShotter Interval Time
      generalBinding.mitmfScreenInterval.setText("10");

      return generalBinding.getRoot();
    }

    @Override
    public void getCommands(StringBuilder stringBuilder) {
      stringBuilder
        .append(" -i ")
        .append(interfaceAdapter.getItem(interfaceSelection))
        .append(generalBinding.mitmfJskey.isChecked() ? " --jskeylogger" : "")
        .append(generalBinding.mitmfFerretng.isChecked() ? " --ferretng" : "")
        .append(generalBinding.mitmfBrowserprofile.isChecked() ? " --browserprofiler" : "")
        .append(generalBinding.mitmfFilepwn.isChecked() ? " --filepwn" : "")
        .append(generalBinding.mitmfSmb.isChecked() ? " --smbauth" : "")
        .append(generalBinding.mitmfSmbTrap.isChecked() ? " --smbtrap" : "")
        .append(generalBinding.mitmfSslstrip.isChecked() ? " --hsts" : "")
        .append(generalBinding.mitmfAppPoison.isChecked() ? " --appoison" : "")
        .append(generalBinding.mitmfUpsidedown.isChecked() ? " --upsidedownternet" : "")
        .append(generalBinding.mitmfScreenshotter.isChecked() ?
          " --screen --interval " + generalBinding.mitmfScreenInterval.getText().toString() :
          "");

    }
  }

  public static class MITMfInject extends Fragment implements CommandProvider {

    MitmfInjectBinding injectBinding;
    MITMFViewModel mViewModel = new MITMFViewModel();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      injectBinding = MitmfInjectBinding.inflate(inflater, container, false);
      injectBinding.setViewModel(mViewModel);
      return injectBinding.getRoot();
    }


    @Override
    public void getCommands(StringBuilder stringBuilder) {
      if (injectBinding.mitmfEnableinject.isChecked()) {
        String injectRateSeconds = injectBinding.mitmfInjectRateseconds.getText().toString();
        String injectTimes = injectBinding.mitmfInjectTimesText.getText().toString();
        String injectJSURL = injectBinding.mitmfInjectjsUrl.getText().toString();
        String injectHtmlUrl = injectBinding.mitmfInjecthtmlUrl.getText().toString();
        String injectHtmlPay = injectBinding.mitmfInjecthtmlpayText.getText().toString();
        String mitmfInjectIp = injectBinding.mitmfInjectIpText.getText().toString();
        String injectNoIp = injectBinding.mitmfInjectNoipText.getText().toString();

        stringBuilder
          .append(" --inject")
          .append(!injectRateSeconds.isEmpty() ? " --rate-limit " + injectRateSeconds : "")
          .append(!injectTimes.isEmpty() ? " --count-limit " + injectTimes : "")
          .append(injectBinding.mitmfInjectPreservecache.isChecked() ? " --preserve-cache" : "")
          .append(injectBinding.mitmfInjectOnceperdomain.isChecked() ? " --per-domain" : "")
          .append(!injectJSURL.isEmpty() ? " --js-url " + injectJSURL : "")
          .append(!injectHtmlUrl.isEmpty() ? " --html-url " + injectHtmlUrl : "")
          .append(!injectHtmlPay.isEmpty() ? " --html-payload " + injectHtmlPay : "")
          .append(!mitmfInjectIp.isEmpty() ? " --white-ips " + mitmfInjectIp : "")
          .append(!injectNoIp.isEmpty() ? " --black-ips " + injectNoIp : "")
        ;
      }

    }
  }


  public static class MITMfSpoof extends Fragment implements CommandProvider {

    private int spoofOption;
    private ArrayAdapter<CharSequence> redirectAdapter;
    private MitmfSpoofBinding spoofBinding;
    private int arpModeOption;
    private MITMFViewModel viewModel;
    private Context context;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      context = getContext();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      spoofBinding = MitmfSpoofBinding.inflate(inflater, container, false);
      viewModel = new MITMFViewModel();
      spoofBinding.setViewModel(viewModel);


      // Redirect Spinner
      final Spinner redirectSpinner = spoofBinding.mitmfSpoofRedirectspin;
      redirectAdapter = ArrayAdapter.createFromResource(context,
        R.array.mitmf_spoof_type, android.R.layout.simple_spinner_item);
      redirectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      redirectSpinner.setAdapter(redirectAdapter);
      redirectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
          String selectedItemText = parent.getItemAtPosition(pos).toString();
          Log.d("Selected: ", selectedItemText);
          spoofOption = pos;
          if (pos == 3) { /*dhcp*/
            viewModel.setShellShockEnabled(true);
          } else {
            viewModel.setShellShockEnabled(false);
          }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
          //Another interface callback
        }
      });

      // ARP Mode Spinner
      ArrayAdapter<CharSequence> arpAdapter = ArrayAdapter.createFromResource(context,
        R.array.mitmf_spoof_arpmode, android.R.layout.simple_spinner_item);
      arpAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      spoofBinding.mitmfSpoofArpmodespin.setAdapter(arpAdapter);
      spoofBinding.mitmfSpoofArpmodespin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
          String selectedItemText = parent.getItemAtPosition(pos).toString();
          Log.d("Slected: ", selectedItemText);
          arpModeOption = pos;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
          //Another interface callback
        }
      });


      return spoofBinding.getRoot();
    }

    @Override
    public void getCommands(StringBuilder stringBuilder) {
      if (spoofBinding.mitmfEnablespoof.isChecked() && spoofOption != 0) {
        stringBuilder.append(" --spoof");
        if (spoofOption == 1) {
          stringBuilder.append(" --arp");
        } else if (spoofOption == 2) {
          stringBuilder.append(" --icmp");
        } else if (spoofOption == 3) {
          stringBuilder.append(" --dhcp");
        } else if (spoofOption == 4) {
          stringBuilder.append(" --dns");
        }

        if (arpModeOption == 1) {
          stringBuilder.append(" --arpmode req");
        } else if (arpModeOption == 2) {
          stringBuilder.append(" --arpmode rep");
        }

        String gateway = spoofBinding.mitmfSpoofGatewayText.getText().toString();
        String targets = spoofBinding.mitmfSpoofTargetsText.getText().toString();
        String shellShock = spoofBinding.mitmfSpoofShellshockText.getText().toString();
        stringBuilder.append(!gateway.isEmpty() ? " --gateway " + gateway : "")
          .append(!targets.isEmpty() ? " --targets " + targets : "")
          .append(spoofBinding.mitmfSpoofShellshock.isChecked() &&
            !shellShock.isEmpty() ? " --shellshock " + shellShock : "");
      }

    }
  }


  public static class MITMfResponder extends Fragment implements CommandProvider {

    private MitmfResponderBinding responderBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      responderBinding = MitmfResponderBinding.inflate(inflater, container, false);
      MITMFViewModel viewModel = new MITMFViewModel();
      responderBinding.setViewModel(viewModel);

      return responderBinding.getRoot();
    }

    @Override
    public void getCommands(StringBuilder stringBuilder) {
      if (responderBinding.mitmfResponder.isChecked()) {
        stringBuilder.append(" --responder")
          .append(responderBinding.mitmfResponderAnalyze.isChecked() ? " --analyze" : "")
          .append(responderBinding.mitmfResponderFingerprint.isChecked() ?
            " --fingerprint" : "")
          .append(responderBinding.mitmfResponderLM.isChecked() ? " --lm" : "")
          .append(responderBinding.mitmfResponderNBTNS.isChecked() ? " --nbtns" : "")
          .append(responderBinding.mitmfResponderWPAD.isChecked() ? " --wpad" : "")
          .append(responderBinding.mitmfResponderWREDIR.isChecked() ? " --wredir" : "")
        ;
      }
    }
  }

  public static class MITMfConfigFragment extends Fragment {
    private Context context;
    private String configFilePath;
    final ShellExecuter exe = new ShellExecuter();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      context = getContext();
      configFilePath = NhPaths.CHROOT_PATH() + "/etc/mitmf/mitmf.conf";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      final View rootView = inflater.inflate(R.layout.source_short, container, false);

      String description = getResources().getString(R.string.mitmf_config);
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

  private static void cleanCmd() {
    for (int j = CommandComposed.size() - 1; j >= 0; j--) {
      CommandComposed.remove(j);
    }
  }

  public void run_cmd(String cmd) {
    Intent intent = Bridge.createExecuteIntent("/data/data/hilled.pwnterm/files/usr/bin/kali", cmd);
    startActivity(intent);
  }

}
