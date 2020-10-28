package com.offsec.nethunter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.offsec.nethunter.AsyncTask.CopyBootFilesAsyncTask;
import com.offsec.nethunter.SQL.CustomCommandsSQL;
import com.offsec.nethunter.SQL.KaliServicesSQL;
import com.offsec.nethunter.SQL.NethunterSQL;
import com.offsec.nethunter.SQL.USBArmorySQL;
import com.offsec.nethunter.gps.KaliGPSUpdates;
import com.offsec.nethunter.gps.LocationUpdateService;
import com.offsec.nethunter.service.CompatCheckService;
import com.offsec.nethunter.utils.CheckForRoot;
import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.PermissionCheck;
import com.offsec.nethunter.utils.SharePrefTag;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Stack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


public class AppNavHomeActivity extends AppCompatActivity implements KaliGPSUpdates.Provider {

    public final static String TAG = "AppNavHomeActivity";
    public static final String CHROOT_INSTALLED_TAG = "CHROOT_INSTALLED_TAG";
    public static final String GPS_BACKGROUND_FRAGMENT_TAG = "BG_FRAGMENT_TAG";
    public static final String BOOT_CHANNEL_ID = "BOOT_CHANNEL";
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView navigationView;
    private CharSequence mTitle = "NetHunter";
    private final Stack<String> titles = new Stack<>();
    private SharedPreferences prefs;
    public static MenuItem lastSelectedMenuItem;
    private boolean locationUpdatesRequested = false;
    private KaliGPSUpdates.Receiver locationUpdateReceiver;
    private NhPaths nhPaths;
    private PermissionCheck permissionCheck;
    private BroadcastReceiver nethunterReceiver;
    public static Boolean isBackPressEnabled = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initiate the NhPaths singleton class, and it will then keep living until the app dies.
        // Also with its sharepreference listener registered, the CHROOT_PATH variable can be updated immediately on sharepreference changes.
        nhPaths = NhPaths.getInstance(getApplicationContext());

        // Initiate the PermissionCheck class.
        permissionCheck = new PermissionCheck(this, getApplicationContext());
        // Register the NetHunter receiver with intent actions.
        nethunterReceiver = new NethunterReceiver();
        IntentFilter AppNavHomeIntentFilter = new IntentFilter();
        AppNavHomeIntentFilter.addAction(NethunterReceiver.CHECKCOMPAT);
        AppNavHomeIntentFilter.addAction(NethunterReceiver.BACKPRESSED);
        AppNavHomeIntentFilter.addAction(NethunterReceiver.CHECKCHROOT);
        AppNavHomeIntentFilter.addAction("ChrootManager");
        this.registerReceiver(nethunterReceiver, new IntentFilter(AppNavHomeIntentFilter));
        // initiate prefs.
        prefs = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        // Start copying the app files to the corresponding path.
        ProgressDialog progressDialog = new ProgressDialog(this);
        CopyBootFilesAsyncTask copyBootFilesAsyncTask = new CopyBootFilesAsyncTask(getApplicationContext(), this, progressDialog);
        copyBootFilesAsyncTask.setListener(new CopyBootFilesAsyncTask.CopyBootFilesAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {
                // Can run other things here.
            }

            @Override
            public void onAsyncTaskFinished(Object result) {

                // Fetch the busybox path again after the busybox_nh is copied.
                NhPaths.BUSYBOX = NhPaths.getBusyboxPath();

                // Now Initiate all SQL singleton in MainActivity so that it can be less lagged when switching fragments,
                // because it takes time to retrieve data from database.
                NethunterSQL.getInstance(getApplicationContext());
                KaliServicesSQL.getInstance(getApplicationContext());
                CustomCommandsSQL.getInstance(getApplicationContext());
                USBArmorySQL.getInstance(getApplicationContext());

                // Setup the default SharePreference value.
                setDefaultSharePreference();

                // After finishing copying app files, we do a compatibility check before allowing user to use it.
                // First, check if the app has gained the root already.
                if (!CheckForRoot.isRoot()){
                    showWarningDialog("NetHunter app cannot be run properly", "Root permission is required!!", true);
                }

                // Secondly, check if busybox is present.
                if (!CheckForRoot.isBusyboxInstalled()){
                    showWarningDialog("NetHunter app cannot be run properly", "No busybox is detected, please make sure you have busybox installed!!", true);
                }

                // Thirdly, check if NetHunter terminal app has been installed.
                if (getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.offsec.nhterm") == null) {
                    showWarningDialog("NetHunter app cannot be run properly", "NetHunter terminal is not installed yet.", true);
                }

                // Lastly, check if all required permissions are granted, if yes, show the view to user.
                if (isAllRequiredPermissionsGranted()){
                    setRootView();
                }
            }
        });
        copyBootFilesAsyncTask.execute();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawers();
            } else {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionCheck.DEFAULT_PERMISSION_RQCODE || requestCode == PermissionCheck.NH_TERM_PERMISSIONS_RQCODE){
            for (int grantResult:grantResults){
                if (grantResult != 0){
                    if (getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.offsec.nhterm") == null) {
                        showWarningDialog("NetHunter app cannot be run properly", "NetHunter Terminal is not installed yet, please install from the store!", true);
                        return;
                    }
                    showWarningDialog("NetHunter app cannot be run properly", "Please grant all the permission requests from outside the app or restart the app to grant the rest of permissions again.", true);
                    return;
                }
            }
            if (isAllRequiredPermissionsGranted()) {
                setRootView();
            }
        }
    }

    @Override
    public void onLocationUpdatesRequested(KaliGPSUpdates.Receiver receiver) {
        locationUpdatesRequested = true;
        this.locationUpdateReceiver = receiver;
        Intent intent = new Intent(getApplicationContext(), LocationUpdateService.class);
        bindService(intent, locationServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private LocationUpdateService locationService;
    private boolean updateServiceBound = false;
    private final ServiceConnection locationServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to Update Service, cast the IBinder and get LocalService instance
            LocationUpdateService.ServiceBinder binder = (LocationUpdateService.ServiceBinder) service;
            locationService = binder.getService();
            updateServiceBound = true;
            if (locationUpdatesRequested) {
                locationService.requestUpdates(locationUpdateReceiver);
            }

        }

        public void onServiceDisconnected(ComponentName arg0) {
            updateServiceBound = false;
        }
    };

    @Override
    public void onBackPressed() {
        //If isBackPressEnable is false then not allow user to press back button.
        if (isBackPressEnabled) {
            super.onBackPressed();
            if (titles.size() > 1) {
                titles.pop();
                mTitle = titles.peek();
            }
            Menu menuNav = navigationView.getMenu();
            int i = 0;
            int mSize = menuNav.size();
            while (i < mSize) {
                if (menuNav.getItem(i).getTitle() == mTitle) {
                    MenuItem _current = menuNav.getItem(i);
                    if (lastSelectedMenuItem != _current) {
                        //remove last
                        lastSelectedMenuItem.setChecked(false);
                        // update for the next
                        lastSelectedMenuItem = _current;
                    }
                    //set checked
                    _current.setChecked(true);
                    i = mSize;
                }
                i++;
            }
            restoreActionBar();
        }
    }

    @Override
    public void onStopRequested() {
        if (locationService != null) {
            locationService.stopUpdates();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (navigationView != null) startService(new Intent(getApplicationContext(), CompatCheckService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lastSelectedMenuItem = null;
        if (nethunterReceiver != null) {
            unregisterReceiver(nethunterReceiver);
        }
        if (nhPaths != null){
            nhPaths.onDestroy();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setRootView(){

        setContentView(R.layout.base_layout);

        //set boot_kali wallpaper as background
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeButtonEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mDrawerLayout = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.navigation_view);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") LinearLayout navigationHeadView = (LinearLayout) inflater.inflate(R.layout.sidenav_header, null);
        navigationView.addHeaderView(navigationHeadView);

        FloatingActionButton readmeButton = navigationHeadView.findViewById(R.id.info_fab);
        readmeButton.setOnTouchListener((v, event) -> {
            //checkUpdate();
            showLicense();
            return false;
        });

        /// moved build info to the menu
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd KK:mm:ss a zzz",
                Locale.US);

        final String buildTime = sdf.format(BuildConfig.BUILD_TIME);
        TextView buildInfo1 = navigationHeadView.findViewById(R.id.buildinfo1);
        TextView buildInfo2 = navigationHeadView.findViewById(R.id.buildinfo2);
        buildInfo1.setText(String.format("Version: %s (%s)", BuildConfig.VERSION_NAME, Build.TAGS));
        buildInfo2.setText(String.format("Built by %s at %s", BuildConfig.BUILD_NAME, buildTime));

        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // detail for android 5 devices
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.darkTitle));
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, NetHunterFragment.newInstance(R.id.nethunter_item))
                .commit();

        // and put the title in the queue for when you need to back through them
        titles.push(navigationView.getMenu().getItem(0).getTitle().toString());
        // disable all fragment first until it passes the compat check.
        navigationView.getMenu().setGroupEnabled(R.id.chrootDependentGroup, false);
        // if the nav bar hasn't been seen, let's show it
        if (!prefs.getBoolean("seenNav", false)) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            SharedPreferences.Editor ed = prefs.edit();
            ed.putBoolean("seenNav", true);
            ed.apply();
        }

        if (lastSelectedMenuItem == null) { // only in the 1st create
            lastSelectedMenuItem = navigationView.getMenu().getItem(0);
            lastSelectedMenuItem.setChecked(true);
        }
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_opened, R.string.drawer_closed);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        startService(new Intent(getApplicationContext(), CompatCheckService.class));
    }

    private void showLicense() {
        // @binkybear here goes the changelog etc... \n\n%s
        String readmeData = String.format("%s\n\n%s\n\n%s",
                getResources().getString(R.string.licenseInfo),
                getResources().getString(R.string.nhwarning),
                getResources().getString(R.string.nhteam));
        final SpannableString readmeText = new SpannableString(readmeData);
        Linkify.addLinks(readmeText, Linkify.WEB_URLS);


        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("README INFO")
                .setMessage(readmeText)
                .setNegativeButton("Close", (dialog, which) -> dialog.cancel()); //nhwarning
        AlertDialog ad = adb.create();
        ad.setCancelable(false);
        if (ad.getWindow() != null) {
            ad.getWindow().getAttributes().windowAnimations = R.style.DialogStyle;
        }
        ad.show();
        ((TextView) Objects.requireNonNull(ad.findViewById(android.R.id.message))).setMovementMethod(LinkMovementMethod.getInstance());

    }

    @SuppressLint("NonConstantResourceId")
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    // only change it if is not the same as the last one
                    if (lastSelectedMenuItem != menuItem) {
                        //remove last
                        lastSelectedMenuItem.setChecked(false);
                        // update for the next
                        lastSelectedMenuItem = menuItem;
                    }
                    //set checked
                    menuItem.setChecked(true);
                    mDrawerLayout.closeDrawers();
                    mTitle = menuItem.getTitle();
                    titles.push(mTitle.toString());

                    FragmentManager fragmentManager = getSupportFragmentManager();
                    int itemId = menuItem.getItemId();
                    switch (itemId) {
                        case R.id.nethunter_item:
                            changeFragment(fragmentManager, NetHunterFragment.newInstance(itemId));
                            break;
                        /*
                        case R.id.kalilauncher_item:
                            fragmentManager
                                    .beginTransaction()
                                    .replace(R.id.container, KaliLauncherFragment.newInstance(itemId))
                                    .addToBackStack(null)
                                    .commit();
                            break;
                        */
                        case R.id.deauth_item:
                            changeFragment(fragmentManager, DeAuthFragment.newInstance(itemId));
                            break;
                        case R.id.kaliservices_item:
                            changeFragment(fragmentManager, KaliServicesFragment.newInstance(itemId));
                            break;
                        case R.id.custom_commands_item:
                            changeFragment(fragmentManager, CustomCommandsFragment.newInstance(itemId));
                            break;
                        case R.id.hid_item:
                            changeFragment(fragmentManager, HidFragment.newInstance(itemId));
                            break;
                        case R.id.duckhunter_item:
                            changeFragment(fragmentManager, DuckHunterFragment.newInstance(itemId));
                            break;
                        case R.id.usbarmory_item:
                            if (new File("/config/usb_gadget/g1").exists()) {
                                changeFragment(fragmentManager, USBArmoryFragment.newInstance(itemId));
                            } else {
                                showWarningDialog("", "Your kernel does not support USB ConfigFS!", false);
                            }
                            break;
                        case R.id.badusb_item:
                            changeFragment(fragmentManager, BadusbFragment.newInstance(itemId));
                            break;
                        case R.id.mana_item:
                            changeFragment(fragmentManager, ManaFragment.newInstance(itemId));
                            break;
                        case R.id.bt_item:
                            changeFragment(fragmentManager, BTFragment.newInstance(itemId));
                            break;
                        case R.id.macchanger_item:
                            changeFragment(fragmentManager, MacchangerFragment.newInstance(itemId));
                            break;
                        case R.id.createchroot_item:
                            changeFragment(fragmentManager, ChrootManagerFragment.newInstance(itemId));
                            break;
                        case R.id.mpc_item:
                            changeFragment(fragmentManager, MPCFragment.newInstance(itemId));
                            break;
                        case R.id.mitmf_item:
                            changeFragment(fragmentManager, MITMfFragment.newInstance(itemId));
                            break;
                        case R.id.vnc_item:
                            if (getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.offsec.nethunter.kex") == null) {
                                showWarningDialog("", "NetHunter KeX is not installed yet, please install from the store!", false);
                            } else {
                                    changeFragment(fragmentManager, VNCFragment.newInstance(itemId));
                            }
                            break;
                        case R.id.searchsploit_item:
                            changeFragment(fragmentManager, SearchSploitFragment.newInstance(itemId));
                            break;
                        case R.id.nmap_item:
                            changeFragment(fragmentManager, NmapFragment.newInstance(itemId));
                            break;
                        case R.id.pineapple_item:
                            changeFragment(fragmentManager, PineappleFragment.newInstance(itemId));
                            break;
                        case R.id.gps_item:
                            changeFragment(fragmentManager, KaliGpsServiceFragment.newInstance(itemId));
                            break;
                        case R.id.settings_item:
                            changeFragment(fragmentManager, SettingsFragment.newInstance(itemId));
                            break;
                    }
                    restoreActionBar();
                    return true;
                });
    }

    public void restoreActionBar() {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeButtonEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(true);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            ab.setTitle(mTitle);
        }
    }

    public void blockActionBar(){
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeButtonEnabled(false);
            ab.setDisplayHomeAsUpEnabled(false);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    public void setDefaultSharePreference () {
        if (prefs.getString(SharePrefTag.DUCKHUNTER_LANG_SHAREPREF_TAG, null) == null) {
            prefs.edit().putString(SharePrefTag.DUCKHUNTER_LANG_SHAREPREF_TAG, "us").apply();
        }
        if (prefs.getString(SharePrefTag.CHROOT_DEFAULT_BACKUP_SHAREPREF_TAG, null) == null) {
            prefs.edit().putString(SharePrefTag.CHROOT_DEFAULT_BACKUP_SHAREPREF_TAG, NhPaths.SD_PATH + "/kalifs-backup.tar.gz").apply();
        }
        if (prefs.getString(SharePrefTag.CHROOT_DEFAULT_STORE_DOWNLOAD_SHAREPREF_TAG, null) == null) {
            prefs.edit().putString(SharePrefTag.CHROOT_DEFAULT_STORE_DOWNLOAD_SHAREPREF_TAG, NhPaths.SD_PATH + "/Download").apply();
        }
    }

    private void changeFragment(FragmentManager fragmentManager, Fragment fragment) {
        fragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private boolean isAllRequiredPermissionsGranted(){
        if (!permissionCheck.isAllPermitted(PermissionCheck.DEFAULT_PERMISSIONS)) {
            permissionCheck.checkPermissions(PermissionCheck.DEFAULT_PERMISSIONS, PermissionCheck.DEFAULT_PERMISSION_RQCODE);
            return false;
        } else if (!permissionCheck.isAllPermitted(PermissionCheck.NH_TERM_PERMISSIONS)) {
            permissionCheck.checkPermissions(PermissionCheck.NH_TERM_PERMISSIONS, PermissionCheck.NH_TERM_PERMISSIONS_RQCODE);
            return false;
        } else if (!permissionCheck.isAllPermitted(PermissionCheck.NH_VNC_PERMISSIONS)) {
            permissionCheck.checkPermissions(PermissionCheck.NH_VNC_PERMISSIONS, PermissionCheck.NH_VNC_PERMISSIONS_RQCODE);
        }

        return true;
    }

    public void showWarningDialog(String title, String message, boolean NeedToExit) {
        android.app.AlertDialog.Builder warningAD = new android.app.AlertDialog.Builder(this);
        warningAD.setCancelable(false);
        warningAD.setTitle(title);
        warningAD.setMessage(message);
        warningAD.setPositiveButton("CLOSE", (dialog, which) -> {
            dialog.dismiss();
            if (NeedToExit)
                System.exit(1);
        });
        warningAD.create().show();
    }

    // Main app broadcastRecevier to response for different actions.
    public class NethunterReceiver extends BroadcastReceiver{
        public static final String CHECKCOMPAT = BuildConfig.APPLICATION_ID + ".CHECKCOMPAT";
        public static final String BACKPRESSED = BuildConfig.APPLICATION_ID + ".BACKPRESSED";
        public static final String CHECKCHROOT = BuildConfig.APPLICATION_ID + ".CHECKCHROOT";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case CHECKCOMPAT:
                        showWarningDialog("NetHunter app cannot be run properly",
                            intent.getStringExtra("message"),
                            true);
                        break;
                    case BACKPRESSED:
                        isBackPressEnabled = (intent.getBooleanExtra("isEnable", true));
                        if (isBackPressEnabled) {
                            restoreActionBar();
                        } else {
                            blockActionBar();
                        }
                        break;
                    case CHECKCHROOT:
                        try{
                            if (intent.getBooleanExtra("ENABLEFRAGMENT", false)){
                                navigationView.getMenu().setGroupEnabled(R.id.chrootDependentGroup, true);
                            } else {
                                navigationView.getMenu().setGroupEnabled(R.id.chrootDependentGroup, false);
                                if (lastSelectedMenuItem.getItemId() != R.id.nethunter_item &&
                                        lastSelectedMenuItem.getItemId() != R.id.createchroot_item) {
                                    FragmentManager fragmentManager = getSupportFragmentManager();
                                    changeFragment(fragmentManager, NetHunterFragment.newInstance(R.id.nethunter_item));
                                }
                            }
                        } catch (Exception e) {
                            if (e.getMessage() != null) {
                                Log.e(AppNavHomeActivity.TAG, e.getMessage());
                            } else {
                                Log.e(AppNavHomeActivity.TAG, "e.getMessage is Null.");
                            }
                        }
                        break;
                }
            }
        }
    }
}

