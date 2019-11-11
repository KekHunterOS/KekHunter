package com.offsec.nethunter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.offsec.nethunter.RecyclerViewAdapter.KaliServicesRecycleViewAdapter;
import com.offsec.nethunter.RecyclerViewAdapter.KaliServicesRecycleViewAdapterDeleteItems;
import com.offsec.nethunter.RecyclerViewData.KaliServicesData;
import com.offsec.nethunter.SQL.KaliServicesSQL;
import com.offsec.nethunter.models.KaliServicesModel;
import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.viewmodels.KaliServicesViewModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class KaliServicesFragment extends Fragment {
    private static final String TAG = "KaliServicesFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private Activity activity;
    private Context context;
    private Button refreshButton;
    private Button addButton;
    private Button deleteButton;
    private Button moveButton;
    private KaliServicesRecycleViewAdapter kaliServicesRecycleViewAdapter;
    private static int targetPositionId;

    public static KaliServicesFragment newInstance(int sectionNumber) {
        KaliServicesFragment fragment = new KaliServicesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.context = getContext();
        this.activity = getActivity();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.kaliservices, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        KaliServicesViewModel kaliServicesViewModel = ViewModelProviders.of(this).get(KaliServicesViewModel.class);
        kaliServicesViewModel.init(context);
        kaliServicesViewModel.getLiveDataKaliServicesModelList().observe(this, kaliServicesModelList -> kaliServicesRecycleViewAdapter.notifyDataSetChanged());

        kaliServicesRecycleViewAdapter = new KaliServicesRecycleViewAdapter(context, kaliServicesViewModel.getLiveDataKaliServicesModelList().getValue());
        RecyclerView recyclerViewServiceTitle = view.findViewById(R.id.f_kaliservices_recycleviewServiceTitle);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerViewServiceTitle.setLayoutManager(linearLayoutManager);
        recyclerViewServiceTitle.setAdapter(kaliServicesRecycleViewAdapter);

        refreshButton = view.findViewById(R.id.f_kaliservices_refreshButton);
        addButton = view.findViewById(R.id.f_kaliservices_addItemButton);
        deleteButton = view.findViewById(R.id.f_kaliservices_deleteItemButton);
        moveButton = view.findViewById(R.id.f_kaliservices_moveItemButton);

        onRefreshItemSetup();
        onAddItemSetup();
        onDeleteItemSetup();
        onMoveItemSetup();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.kaliservices, menu);
        final MenuItem searchItem = menu.findItem(R.id.f_kaliservices_action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnSearchClickListener(v -> menu.setGroupVisible(R.id.f_kaliservices_menu_group1, false));
        searchView.setOnCloseListener(() -> {
            menu.setGroupVisible(R.id.f_kaliservices_menu_group1, true);
            return false;
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                kaliServicesRecycleViewAdapter.getFilter().filter(newText);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final ViewGroup nullParent = null;
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View promptView = inflater.inflate(R.layout.kaliservices_custom_dialog_view, nullParent);
        final TextView titleTextView = promptView.findViewById(R.id.f_kaliservices_adb_tv_title1);
        final EditText storedpathEditText = promptView.findViewById(R.id.f_kaliservices_adb_et_storedpath);

        switch (item.getItemId()){
            case R.id.f_kaliservices_menu_backupDB:
                titleTextView.setText("Full path to where you want to save the database:");
                storedpathEditText.setText(NhPaths.APP_SD_SQLBACKUP_PATH + "/FragmentKaliServices");
                AlertDialog.Builder adbBackup = new AlertDialog.Builder(activity);
                adbBackup.setView(promptView);
                adbBackup.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                adbBackup.setPositiveButton("OK", (dialog, which) -> { });
                final AlertDialog adBackup = adbBackup.create();
                adBackup.setOnShowListener(dialog -> {
                    final Button buttonOK = adBackup.getButton(DialogInterface.BUTTON_POSITIVE);
                    buttonOK.setOnClickListener(v -> {
                        String returnedResult = KaliServicesData.getInstance().backupData(KaliServicesSQL.getInstance(context), storedpathEditText.getText().toString());
                        if (returnedResult == null){
                            NhPaths.showMessage(context, "db is successfully backup to " + storedpathEditText.getText().toString());
                        } else {
                            dialog.dismiss();
                            new AlertDialog.Builder(context).setTitle("Failed to backup the DB.").setMessage(returnedResult).create().show();
                        }
                        dialog.dismiss();
                    });
                });
                adBackup.show();
                break;
            case R.id.f_kaliservices_menu_restoreDB:
                titleTextView.setText("Full path of the db file from where you want to restore:");
                storedpathEditText.setText(NhPaths.APP_SD_SQLBACKUP_PATH + "/FragmentKaliServices");
                AlertDialog.Builder adbRestore = new AlertDialog.Builder(activity);
                adbRestore.setView(promptView);
                adbRestore.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                adbRestore.setPositiveButton("OK", (dialog, which) -> { });
                final AlertDialog adRestore = adbRestore.create();
                adRestore.setOnShowListener(dialog -> {
                    final Button buttonOK = adRestore.getButton(DialogInterface.BUTTON_POSITIVE);
                    buttonOK.setOnClickListener(v -> {
                        String returnedResult = KaliServicesData.getInstance().restoreData(KaliServicesSQL.getInstance(context), storedpathEditText.getText().toString());
                        if (returnedResult == null) {
                            NhPaths.showMessage(context, "db is successfully restored to " + storedpathEditText.getText().toString());
                        } else {
                            dialog.dismiss();
                            new AlertDialog.Builder(context).setTitle("Failed to restore the DB.").setMessage(returnedResult).create().show();
                        }
                        dialog.dismiss();
                    });
                });
                adRestore.show();
                break;
            case R.id.f_kaliservices_menu_ResetToDefault:
                KaliServicesData.getInstance().resetData(KaliServicesSQL.getInstance(context));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        KaliServicesData.getInstance().refreshData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        refreshButton = null;
        addButton = null;
        deleteButton = null;
        moveButton = null;
        kaliServicesRecycleViewAdapter = null;
    }

    private void onRefreshItemSetup(){
        refreshButton.setOnClickListener(v -> KaliServicesData.getInstance().refreshData());
    }

    private void onAddItemSetup(){
        addButton.setOnClickListener(v -> {
            final ViewGroup nullParent = null;
            List<KaliServicesModel> kaliServicesModelList = KaliServicesData.getInstance().kaliServicesModelListFull;
            if (kaliServicesModelList == null) return;
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View promptViewAdd = inflater.inflate(R.layout.kaliservices_add_dialog_view, nullParent);
            final EditText titleEditText = promptViewAdd.findViewById(R.id.f_kaliservices_add_adb_et_title);
            final EditText startCmdEditText = promptViewAdd.findViewById(R.id.f_kaliservices_add_adb_et_startcommand);
            final EditText stopCmdEditText = promptViewAdd.findViewById(R.id.f_kaliservices_add_adb_et_stopcommand);
            final EditText checkstatusCmdEditText = promptViewAdd.findViewById(R.id.f_kaliservices_add_adb_et_checkstatuscommand);
            final CheckBox runOnChrootStartCheckbox = promptViewAdd.findViewById(R.id.f_kaliservices_add_adb_checkbox_runonboot);
            final FloatingActionButton readmeButton1 = promptViewAdd.findViewById(R.id.f_kaliservices_add_btn_info_fab1);
            final FloatingActionButton readmeButton2 = promptViewAdd.findViewById(R.id.f_kaliservices_add_btn_info_fab2);
            final FloatingActionButton readmeButton3 = promptViewAdd.findViewById(R.id.f_kaliservices_add_btn_info_fab3);
            final FloatingActionButton readmeButton4 = promptViewAdd.findViewById(R.id.f_kaliservices_add_btn_info_fab4);
            final Spinner insertPositions = promptViewAdd.findViewById(R.id.f_kaliservices_add_adb_spr_positions);
            final Spinner insertTitles = promptViewAdd.findViewById(R.id.f_kaliservices_add_adb_spr_titles);

            ArrayList<String> serviceNameArrayList = new ArrayList<>();
            for (KaliServicesModel kaliServicesModel: kaliServicesModelList){
                serviceNameArrayList.add(kaliServicesModel.getServiceName());
            }

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, serviceNameArrayList);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            startCmdEditText.setText("service <servicename> start");
            stopCmdEditText.setText("service <servicename> stop");
            checkstatusCmdEditText.setText("<servicename>");

            readmeButton1.setOnClickListener(view -> {
                AlertDialog.Builder adb = new AlertDialog.Builder(activity);
                adb.setTitle("HOW TO USE:")
                        .setMessage(getString(R.string.kaliservices_howto_startservice))
                        .setNegativeButton("Close", (dialogInterface, i) -> dialogInterface.dismiss());
                final AlertDialog ad = adb.create();
                ad.setCancelable(true);
                ad.show();
            });

            readmeButton2.setOnClickListener(view -> {
                AlertDialog.Builder adb = new AlertDialog.Builder(activity);
                adb.setTitle("HOW TO USE:")
                        .setMessage(getString(R.string.kaliservices_howto_stopservice))
                        .setNegativeButton("Close", (dialogInterface, i) -> dialogInterface.dismiss());
                final AlertDialog ad = adb.create();
                ad.setCancelable(true);
                ad.show();
            });

            readmeButton3.setOnClickListener(view -> {
                AlertDialog.Builder adb = new AlertDialog.Builder(activity);
                adb.setTitle("HOW TO USE:")
                        .setMessage(getString(R.string.kaliservices_howto_checkservice))
                        .setNegativeButton("Close", (dialogInterface, i) -> dialogInterface.dismiss());
                final AlertDialog ad = adb.create();
                ad.setCancelable(true);
                ad.show();
            });

            readmeButton4.setOnClickListener(view -> {
                AlertDialog.Builder adb = new AlertDialog.Builder(activity);
                adb.setTitle("HOW TO USE:")
                        .setMessage(getString(R.string.kaliservices_howto_runServiceOnBoot))
                        .setNegativeButton("Close", (dialogInterface, i) -> dialogInterface.dismiss());
                final AlertDialog ad = adb.create();
                ad.setCancelable(true);
                ad.show();
            });

            insertPositions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    //if Insert to Top
                    if (position == 0) {
                        insertTitles.setVisibility(View.INVISIBLE);
                        targetPositionId = 1;
                        //if Insert to Bottom
                    } else if (position == 1) {
                        insertTitles.setVisibility(View.INVISIBLE);
                        targetPositionId = kaliServicesModelList.size() + 1;
                        //if Insert Before
                    } else if (position == 2) {
                        insertTitles.setVisibility(View.VISIBLE);
                        insertTitles.setAdapter(arrayAdapter);
                        insertTitles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                targetPositionId = position + 1;
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                        //if Insert After
                    } else {
                        insertTitles.setVisibility(View.VISIBLE);
                        insertTitles.setAdapter(arrayAdapter);
                        insertTitles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                targetPositionId = position + 2;
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            AlertDialog.Builder adbAdd = new AlertDialog.Builder(activity);
            adbAdd.setPositiveButton("OK", (dialog, which) -> { });
            final AlertDialog adAdd = adbAdd.create();
            adAdd.setView(promptViewAdd);
            adAdd.setCancelable(true);
            //If you want the dialog to stay open after clicking OK, you need to do it this way...
            adAdd.setOnShowListener(dialog -> {
                final Button buttonAdd = adAdd.getButton(DialogInterface.BUTTON_POSITIVE);
                buttonAdd.setOnClickListener(v1 -> {
                    if (titleEditText.getText().toString().isEmpty()){
                        NhPaths.showMessage(context, "Title cannot be empty");
                    } else if (startCmdEditText.getText().toString().isEmpty()){
                        NhPaths.showMessage(context, "Start Command cannot be empty");
                    } else if (stopCmdEditText.getText().toString().isEmpty()){
                        NhPaths.showMessage(context, "Stop Command cannot be empty");
                    } else if (checkstatusCmdEditText.getText().toString().isEmpty()){
                        NhPaths.showMessage(context, "Check Status Command cannot be empty");
                    } else {
                        ArrayList<String> dataArrayList = new ArrayList<>();
                        dataArrayList.add(titleEditText.getText().toString());
                        dataArrayList.add(startCmdEditText.getText().toString());
                        dataArrayList.add(stopCmdEditText.getText().toString());
                        dataArrayList.add(checkstatusCmdEditText.getText().toString());
                        dataArrayList.add(runOnChrootStartCheckbox.isChecked()?"1":"0");
                        KaliServicesData.getInstance().addData(targetPositionId, dataArrayList, KaliServicesSQL.getInstance(context));
                        adAdd.dismiss();
                    }
                });
            });
            adAdd.show();
        });
    }

    private void onDeleteItemSetup(){
        deleteButton.setOnClickListener(v -> {
            final ViewGroup nullParent = null;
            List<KaliServicesModel> kaliServicesModelList = KaliServicesData.getInstance().kaliServicesModelListFull;
            if (kaliServicesModelList == null) return;
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View promptViewDelete = inflater.inflate(R.layout.kaliservices_delete_dialog_view, nullParent, false);
            final RecyclerView recyclerViewDeleteItem = promptViewDelete.findViewById(R.id.f_kaliservices_delete_recycleview);
            KaliServicesRecycleViewAdapterDeleteItems kaliServicesRecycleViewAdapterDeleteItems = new KaliServicesRecycleViewAdapterDeleteItems(context, kaliServicesModelList);

            LinearLayoutManager linearLayoutManagerDelete = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            recyclerViewDeleteItem.setLayoutManager(linearLayoutManagerDelete);
            recyclerViewDeleteItem.setAdapter(kaliServicesRecycleViewAdapterDeleteItems);

            AlertDialog.Builder adbDelete = new AlertDialog.Builder(activity);
            adbDelete.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            adbDelete.setPositiveButton("Delete", (dialog, which) -> { });
            final AlertDialog adDelete = adbDelete.create();
            adDelete.setMessage("Select the service you want to remove: ");
            adDelete.setView(promptViewDelete);
            adDelete.setCancelable(true);
            //If you want the dialog to stay open after clicking OK, you need to do it this way...
            adDelete.setOnShowListener(dialog -> {
                final Button buttonDelete = adDelete.getButton(DialogInterface.BUTTON_POSITIVE);
                buttonDelete.setOnClickListener(v1 -> {
                    RecyclerView.ViewHolder viewHolder;
                    ArrayList<Integer> selectedPosition = new ArrayList<>();
                    ArrayList<Integer> selectedTargetIds = new ArrayList<>();
                    for (int i = 0; i < recyclerViewDeleteItem.getChildCount(); i++) {
                        viewHolder = recyclerViewDeleteItem.findViewHolderForAdapterPosition(i);
                        if (viewHolder != null){
                            CheckBox box = viewHolder.itemView.findViewById(R.id.f_kaliservices_recycleview_dialog_chkbox);
                            if (box.isChecked()){
                                selectedPosition.add(i);
                                selectedTargetIds.add(i+1);
                            }
                        }
                    }
                    if (selectedPosition.size() != 0) {
                        KaliServicesData.getInstance().deleteData(selectedPosition, selectedTargetIds, KaliServicesSQL.getInstance(context));
                        NhPaths.showMessage(context, "Successfully deleted " + selectedPosition.size() + " items.");
                        adDelete.dismiss();
                    } else {
                        NhPaths.showMessage(context, "Nothing to be deleted.");
                    }
                });
            });
            adDelete.show();
        });
    }

    private void onMoveItemSetup(){
        moveButton.setOnClickListener(v -> {
            final ViewGroup nullParent = null;
            List<KaliServicesModel> kaliServicesModelList = KaliServicesData.getInstance().kaliServicesModelListFull;
            if (kaliServicesModelList == null) return;
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View promptViewMove = inflater.inflate(R.layout.kaliservices_move_dialog_view, nullParent, false);
            final Spinner titlesBefore = promptViewMove.findViewById(R.id.f_kaliservices_move_adb_spr_titlesbefore);
            final Spinner titlesAfter = promptViewMove.findViewById(R.id.f_kaliservices_move_adb_spr_titlesafter);
            final Spinner actions = promptViewMove.findViewById(R.id.f_kaliservices_move_adb_spr_actions);

            ArrayList<String> serviceNameArrayList = new ArrayList<>();
            for (KaliServicesModel kaliServicesModel: kaliServicesModelList){
                serviceNameArrayList.add(kaliServicesModel.getServiceName());
            }

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, serviceNameArrayList);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            titlesBefore.setAdapter(arrayAdapter);
            titlesAfter.setAdapter(arrayAdapter);

            AlertDialog.Builder adbMove = new AlertDialog.Builder(activity);
            adbMove.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            adbMove.setPositiveButton("Move", (dialog, which) -> { });
            final AlertDialog adMove = adbMove.create();
            adMove.setView(promptViewMove);
            adMove.setCancelable(true);
            adMove.setOnShowListener(dialog -> {
                final Button buttonMove = adMove.getButton(DialogInterface.BUTTON_POSITIVE);
                buttonMove.setOnClickListener(v1 -> {
                    int originalPositionIndex = titlesBefore.getSelectedItemPosition();
                    int targetPositionIndex = titlesAfter.getSelectedItemPosition();
                    if (originalPositionIndex == targetPositionIndex ||
                            (actions.getSelectedItemPosition() == 0 && targetPositionIndex == (originalPositionIndex + 1)) ||
                            (actions.getSelectedItemPosition() == 1 && targetPositionIndex == (originalPositionIndex - 1))) {
                        NhPaths.showMessage(context, "You are moving the item to the same position, nothing to be moved.");
                    } else {
                        if (actions.getSelectedItemPosition() == 1) targetPositionIndex += 1;
                        KaliServicesData.getInstance().moveData(originalPositionIndex, targetPositionIndex, KaliServicesSQL.getInstance(context));
                        NhPaths.showMessage(context, "Successfully moved item.");
                        adMove.dismiss();
                    }
                });
            });
            adMove.show();
        });
    }
}
