package com.team420.kekhunter;

import android.app.Activity;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.team420.kekhunter.AsyncTask.DuckHuntAsyncTask;
import com.team420.kekhunter.utils.NhPaths;
import com.team420.kekhunter.utils.SharePrefTag;

import java.util.HashMap;

public class DuckHunterFragment extends Fragment {
  private static SharedPreferences sharedpreferences;
  // Language vars
  private static HashMap<String, String> map = new HashMap<>();
  public static String lang = "us"; // Set US as default language
  private static String[] keyboardLayoutString;
  private static final String ARG_SECTION_NUMBER = "section_number";
  private static final String TAG = "DuckHunterFragment";
  private Context context;
  private Activity activity;
  private Menu menu;
  private ViewPager mViewPager;
  private String duckyInputFile;
  private String duckyOutputFile;
  private boolean isReceiverRegistered;
  private boolean shouldconvert = true;
  private DuckHuntBroadcastReceiver duckHuntBroadcastReceiver = new DuckHuntBroadcastReceiver();

  public static DuckHunterFragment newInstance(int sectionNumber) {
    DuckHunterFragment fragment = new DuckHunterFragment();
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
    duckyInputFile = NhPaths.APP_SD_FILES_PATH + "/modules/ducky_in.txt";
    duckyOutputFile = NhPaths.APP_SD_FILES_PATH + "/modules/ducky_out.sh";
    map.put("American English", "us");
    map.put("Turkish", "tr");
    map.put("Swedish", "sv");
    map.put("Slovenian", "si");
    map.put("Russian", "ru");
    map.put("Portuguese", "pt");
    map.put("Norwegian", "no");
    map.put("Croatian", "hr");
    map.put("United Kingdom", "gb");
    map.put("French", "fr");
    map.put("Finland", "fi");
    map.put("Spain", "es");
    map.put("Danish", "dk");
    map.put("German", "de");
    map.put("Candian", "ca");
    map.put("Canadian Multilingual Standard", "cm");
    map.put("Brazil", "br");
    map.put("Belgian", "be");
    map.put("Hungarian", "hu");
    keyboardLayoutString = map.keySet().toArray(new String[0]);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.duck_hunter, container, false);
    setHasOptionsMenu(true);
    TabsPagerAdapter tabsPagerAdapter = new TabsPagerAdapter(getChildFragmentManager());
    mViewPager = rootView.findViewById(R.id.pagerDuckHunter);
    mViewPager.setAdapter(tabsPagerAdapter);
    mViewPager.setOffscreenPageLimit(1);
    mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
        if (position == 1) {
          menu.findItem(R.id.duckConvertAttack).setVisible(true);
          setLang();
          if (shouldconvert)
            activity.sendBroadcast(new Intent().putExtra("ACTION", "WRITEDUCKY").setAction(BuildConfig.APPLICATION_ID + ".WRITEDUCKY"));
        } else
          menu.findItem(R.id.duckConvertAttack).setVisible(false);
      }
    });

    sharedpreferences = activity.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

    if (!sharedpreferences.contains("DuckHunterLanguageIndex")) {
      for (int i = 0; i < keyboardLayoutString.length; i++) {
        if ("us".equals(map.get(keyboardLayoutString[i]))) {
          sharedpreferences.edit().putInt("DuckHunterLanguageIndex", i).apply();
          break;
        }
      }
    }

    return rootView;
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
    this.menu = menu;
    inflater.inflate(R.menu.duck_hunter, menu);
    menu.findItem(R.id.duckConvertAttack).setVisible(false);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    DuckHuntAsyncTask duckHuntAsyncTask;
    switch (item.getItemId()) {
      case R.id.duckConvertAttack:
        duckHuntAsyncTask = new DuckHuntAsyncTask(DuckHuntAsyncTask.ATTACK);
        duckHuntAsyncTask.setListener(new DuckHuntAsyncTask.DuckHuntAsyncTaskListener() {
          @Override
          public void onAsyncTaskPrepare() {
            NhPaths.showMessage(context, "Launching Attack");
          }

          @Override
          public void onAsyncTaskFinished(Object result) {
            if (!(boolean) result) {
              NhPaths.showMessage_long(context, "HID interfaces are not enabled or something wrong with the permission of /dev/hidg*, make sure they are enabled and permissions are granted as 666");
            }
          }
        });
        duckHuntAsyncTask.execute("sh " + duckyOutputFile);
        return true;
      case R.id.chooseLanguage:
        openLanguageDialog();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    if (!isReceiverRegistered) {
      activity.registerReceiver(duckHuntBroadcastReceiver, new IntentFilter(BuildConfig.APPLICATION_ID + ".SHOULDCONVERT"));
      isReceiverRegistered = true;
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (isReceiverRegistered) {
      activity.unregisterReceiver(duckHuntBroadcastReceiver);
      isReceiverRegistered = false;
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    menu = null;
    mViewPager = null;
  }

  private static void setLang() {
    int keyboardLayoutIndex = sharedpreferences.getInt("DuckHunterLanguageIndex", 0);
    lang = map.get(keyboardLayoutString[keyboardLayoutIndex]);
  }

  private void openLanguageDialog() {
    int keyboardLayoutIndex = sharedpreferences.getInt("DuckHunterLanguageIndex", 0);
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setTitle("Language:");
    builder.setPositiveButton("OK", (dialog, which) -> {
      if (mViewPager.getCurrentItem() == 1) {
        if (getView() == null) {
          return;
        }
        setLang();
        activity.sendBroadcast(new Intent().putExtra("ACTION", "WRITEDUCKY").setAction(BuildConfig.APPLICATION_ID + ".WRITEDUCKY"));
      }
    });
    builder.setSingleChoiceItems(keyboardLayoutString, keyboardLayoutIndex, (dialog, which) -> {
      Editor editor = sharedpreferences.edit();
      editor.putInt("DuckHunterLanguageIndex", which);
      editor.putString(SharePrefTag.DUCKHUNTER_LANG_SHAREPREF_TAG, map.get(keyboardLayoutString[which]));
      editor.apply();
    });
    builder.show();
  }

  public class TabsPagerAdapter extends FragmentStatePagerAdapter {

    TabsPagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int i) {
      switch (i) {
        case 1:
          return new DuckHunterPreviewFragment(duckyInputFile, duckyOutputFile);
        default:
          return new DuckHunterConvertFragment(duckyInputFile, duckyOutputFile);
      }
    }

    @Override
    public Parcelable saveState() {
      return null;
    }

    @Override
    public int getCount() {
      return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      switch (position) {
        case 1:
          return "Preview";
        default:
          return "Convert";
      }
    }
  }

  public class DuckHuntBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      switch (intent.getStringExtra("ACTION")) {
        case "SHOULDCONVERT":
          shouldconvert = intent.getBooleanExtra("SHOULDCONVERT", true);
          break;
      }
    }
  }
}

