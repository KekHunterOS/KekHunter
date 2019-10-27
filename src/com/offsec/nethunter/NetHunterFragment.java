package com.offsec.nethunter;

import android.app.Activity;
import android.app.AlertDialog;
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
import com.offsec.nethunter.RecyclerViewAdapter.NethunterRecyclerViewAdapter;
import com.offsec.nethunter.RecyclerViewAdapter.NethunterRecyclerViewAdapterDeleteItems;
import com.offsec.nethunter.RecyclerViewData.NethunterData;
import com.offsec.nethunter.SQL.NethunterSQL;
import com.offsec.nethunter.models.NethunterModel;
import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.viewmodels.NethunterViewModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class NetHunterFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private Context context;
    private Activity activity;
    private RecyclerView itemRecyclerView;
    private NethunterRecyclerViewAdapter nethunterRecyclerViewAdapter;
    private NethunterViewModel nethunterViewModel;
    private NethunterSQL nethunterSQL;
    private Button refreshButton;
    private Button addButton;
    private Button deleteButton;
    private Button moveButton;
    private int targetPositionId;

    public static NetHunterFragment newInstance(int sectionNumber) {
        NetHunterFragment fragment = new NetHunterFragment();
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.nethunter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        nethunterSQL = new NethunterSQL(context);
        nethunterViewModel = ViewModelProviders.of(this).get(NethunterViewModel.class);
        nethunterViewModel.init(context);
        nethunterViewModel.getLiveDataNethunterModelList().observe(this, nethunterModelList -> {
            nethunterRecyclerViewAdapter.notifyDataSetChanged();
        });

        itemRecyclerView = view.findViewById(R.id.f_nethunter_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        itemRecyclerView.setLayoutManager(linearLayoutManager);
        nethunterRecyclerViewAdapter = new NethunterRecyclerViewAdapter(context, nethunterViewModel.getLiveDataNethunterModelList().getValue());
        itemRecyclerView.setAdapter(nethunterRecyclerViewAdapter);

        refreshButton = view.findViewById(R.id.f_nethunter_refreshButton);
        addButton = view.findViewById(R.id.f_nethunter_addItemButton);
        deleteButton = view.findViewById(R.id.f_nethunter_deleteItemButton);
        moveButton = view.findViewById(R.id.f_nethunter_moveItemButton);

        onRefreshItemSetup();
        onAddItemSetup();
        onDeleteItemSetup();
        onMoveItemSetup();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.nethunter, menu);
        MenuItem searchItem = menu.findItem(R.id.f_nethunter_action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                nethunterRecyclerViewAdapter.getFilter().filter(newText);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View promptView = inflater.inflate(R.layout.nethunter_custom_dialog_view, null);
        final TextView titleTextView = promptView.findViewById(R.id.f_nethunter_adb_tv_title1);
        final EditText storedpathEditText = promptView.findViewById(R.id.f_nethunter_adb_et_storedpath);
        switch (item.getItemId()) {
            case R.id.f_nethunter_menu_backupDB:
                titleTextView.setText("Full path to where you want to save the database:");
                storedpathEditText.setText(NhPaths.APP_SD_SQLBACKUP_PATH + "/FragmentNethunter");
                final AlertDialog.Builder adbBackup = new AlertDialog.Builder(activity);
                adbBackup.setView(promptView);
                adbBackup.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                adbBackup.setPositiveButton("OK", (dialog, which) -> { });
                final AlertDialog adBackup = adbBackup.create();
                adBackup.setOnShowListener(dialog -> {
                    final Button buttonOK = adBackup.getButton(DialogInterface.BUTTON_POSITIVE);
                    buttonOK.setOnClickListener(v -> {
                        NethunterData.getInstance().backupData(nethunterSQL, storedpathEditText.getText().toString());
                        dialog.dismiss();
                    });
                });
                adBackup.show();
                break;
            case R.id.f_nethunter_menu_restoreDB:
                titleTextView.setText("Full path of the db file from where you want to restore:");
                storedpathEditText.setText(NhPaths.APP_SD_SQLBACKUP_PATH + "/FragmentNethunter");
                final AlertDialog.Builder adbRestore = new AlertDialog.Builder(activity);
                adbRestore.setView(promptView);
                adbRestore.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                adbRestore.setPositiveButton("OK", (dialog, which) -> { });
                final AlertDialog adRestore = adbRestore.create();
                adRestore.setOnShowListener(dialog -> {
                    final Button buttonOK = adRestore.getButton(DialogInterface.BUTTON_POSITIVE);
                    buttonOK.setOnClickListener(v -> {
                        NethunterData.getInstance().restoreData(nethunterSQL, storedpathEditText.getText().toString());
                        dialog.dismiss();
                    });
                });
                adRestore.show();
                break;
            case R.id.f_nethunter_menu_ResetToDefault:
                NethunterData.getInstance().resetData(nethunterSQL);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        NethunterData.getInstance().refreshData();
    }

    private void onRefreshItemSetup(){
        refreshButton.setOnClickListener(v -> NethunterData.getInstance().refreshData());
    }

    private void onAddItemSetup(){
        addButton.setOnClickListener(v -> {
            final List<NethunterModel> nethunterModelList = NethunterData.getInstance().nethunterModelListFull;
            if (nethunterModelList == null) return;
            final LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View promptViewAdd = mInflater.inflate(R.layout.nethunter_add_dialog_view, null);
            final EditText titleEditText = promptViewAdd.findViewById(R.id.f_nethunter_add_adb_et_title);
            final EditText cmdEditText = promptViewAdd.findViewById(R.id.f_nethunter_add_adb_et_command);
            final EditText delimiterEditText = promptViewAdd.findViewById(R.id.f_nethunter_add_adb_et_delimiter);
            final CheckBox runOnCreateCheckbox = promptViewAdd.findViewById(R.id.f_nethunters_add_adb_checkbox_runoncreate);
            final Spinner insertPositions = promptViewAdd.findViewById(R.id.f_nethunter_add_adb_spr_positions);
            final Spinner insertTitles = promptViewAdd.findViewById(R.id.f_nethunter_add_adb_spr_titles);
            ArrayList<String> titleArrayList = new ArrayList<>();
            for (NethunterModel nethunterModel: nethunterModelList){
                titleArrayList.add(nethunterModel.getTitle());
            }
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, titleArrayList);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            final FloatingActionButton readmeButton1 = promptViewAdd.findViewById(R.id.f_nethunter_add_btn_info_fab1);
            final FloatingActionButton readmeButton2 = promptViewAdd.findViewById(R.id.f_nethunter_add_btn_info_fab2);
            final FloatingActionButton readmeButton3 = promptViewAdd.findViewById(R.id.f_nethunter_add_btn_info_fab3);

            readmeButton1.setOnClickListener(view -> {
                AlertDialog.Builder adb = new AlertDialog.Builder(context);
                adb.setTitle("HOW TO USE:")
                        .setMessage(context.getString(R.string.nethunter_howtouse_cmd))
                        .setNegativeButton("Close", (dialogInterface, i) -> dialogInterface.dismiss());
                AlertDialog ad = adb.create();
                ad.setCancelable(true);
                ad.show();
            });

            readmeButton2.setOnClickListener(view -> {
                AlertDialog.Builder adb = new AlertDialog.Builder(context);
                adb.setTitle("HOW TO USE:")
                        .setMessage(context.getString(R.string.nethunter_howtouse_delimiter))
                        .setNegativeButton("Close", (dialogInterface, i) -> dialogInterface.dismiss());
                AlertDialog ad = adb.create();
                ad.setCancelable(true);
                ad.show();
            });

            readmeButton3.setOnClickListener(view -> {
                AlertDialog.Builder adb = new AlertDialog.Builder(context);
                adb.setTitle("HOW TO USE:")
                        .setMessage(context.getString(R.string.nethunter_howtouse_runoncreate))
                        .setNegativeButton("Close", (dialogInterface, i) -> dialogInterface.dismiss());
                AlertDialog ad = adb.create();
                ad.setCancelable(true);
                ad.show();
            });

            delimiterEditText.setText("\\n");
            runOnCreateCheckbox.setChecked(true);

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
                        targetPositionId = nethunterModelList.size() + 1;
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

            AlertDialog.Builder adb = new AlertDialog.Builder(context);
            adb.setPositiveButton("OK", (dialog, which) -> {

            });
            AlertDialog ad = adb.create();
            ad.setView(promptViewAdd);
            ad.setCancelable(true);
            ad.setOnShowListener(dialog -> {
                final Button buttonAdd = ad.getButton(DialogInterface.BUTTON_POSITIVE);
                buttonAdd.setOnClickListener(v1 -> {
                    if (titleEditText.getText().toString().isEmpty()){
                        NhPaths.showMessage(context, "Title cannot be empty");
                    } else if (cmdEditText.getText().toString().isEmpty()){
                        NhPaths.showMessage(context, "Command cannot be empty");
                    } else if (delimiterEditText.getText().toString().isEmpty()){
                        NhPaths.showMessage(context, "Delimiter cannot be empty");
                    } else {
                        ArrayList<String> dataArrayList = new ArrayList<>();
                        dataArrayList.add(titleEditText.getText().toString());
                        dataArrayList.add(cmdEditText.getText().toString());
                        dataArrayList.add(delimiterEditText.getText().toString());
                        dataArrayList.add(runOnCreateCheckbox.isChecked() ? "1" : "0");
                        NethunterData.getInstance().addData(targetPositionId, dataArrayList, nethunterSQL);
                        ad.dismiss();
                    }
                });
            });
            ad.show();
        });
    }

    private void onDeleteItemSetup(){
        deleteButton.setOnClickListener(v -> {
            final List<NethunterModel> nethunterModelList = NethunterData.getInstance().nethunterModelListFull;
            if (nethunterModelList == null) return;
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View promptViewDelete = inflater.inflate(R.layout.nethunter_delete_dialog_view, null, false);
            final RecyclerView recyclerViewDeleteItem = promptViewDelete.findViewById(R.id.f_nethunter_delete_recyclerview);
            final NethunterRecyclerViewAdapterDeleteItems nethunterRecyclerViewAdapterDeleteItems = new NethunterRecyclerViewAdapterDeleteItems(context, nethunterModelList);

            LinearLayoutManager linearLayoutManagerDelete = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            recyclerViewDeleteItem.setLayoutManager(linearLayoutManagerDelete);
            recyclerViewDeleteItem.setAdapter(nethunterRecyclerViewAdapterDeleteItems);

            final AlertDialog.Builder adbDelete = new AlertDialog.Builder(activity);
            adbDelete.setView(promptViewDelete);
            adbDelete.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            adbDelete.setPositiveButton("Delete", (dialog, which) -> { });
            //If you want the dialog to stay open after clicking OK, you need to do it this way...
            final AlertDialog adDelete = adbDelete.create();
            adDelete.setMessage("Select the item you want to remove: ");
            adDelete.setOnShowListener(dialog -> {
                final Button buttonDelete = adDelete.getButton(DialogInterface.BUTTON_POSITIVE);
                buttonDelete.setOnClickListener(v1 -> {
                    RecyclerView.ViewHolder viewHolder;
                    ArrayList<Integer> selectedPosition = new ArrayList<>();
                    ArrayList<Integer> selectedTargetIds = new ArrayList<>();
                    for (int i = 0; i < recyclerViewDeleteItem.getChildCount(); i++) {
                        viewHolder = recyclerViewDeleteItem.findViewHolderForAdapterPosition(i);
                        if (viewHolder != null){
                            CheckBox box = viewHolder.itemView.findViewById(R.id.f_nethunter_recyclerview_dialog_chkbox);
                            if (box.isChecked()){
                                selectedPosition.add(i);
                                selectedTargetIds.add(i+1);
                            }
                        }
                    }
                    if (selectedPosition.size() != 0) {
                        NethunterData.getInstance().deleteData(selectedPosition, selectedTargetIds, nethunterSQL);
                        NhPaths.showMessage(context, "Successfully deleted " + selectedPosition.size() + " items.");
                        adDelete.dismiss();
                    } else NhPaths.showMessage(context, "Nothing to be deleted.");
                });
            });
            adDelete.show();
        });
    }

    private void onMoveItemSetup() {
        moveButton.setOnClickListener(v -> {
            final List<NethunterModel> nethunterModelList = NethunterData.getInstance().nethunterModelListFull;
            if (nethunterModelList == null) return;
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View promptViewMove = inflater.inflate(R.layout.nethunter_move_dialog_view, null, false);
            final Spinner titlesBefore = promptViewMove.findViewById(R.id.f_nethunter_move_adb_spr_titlesbefore);
            final Spinner titlesAfter = promptViewMove.findViewById(R.id.f_nethunter_move_adb_spr_titlesafter);
            final Spinner actions = promptViewMove.findViewById(R.id.f_nethunter_move_adb_spr_actions);
            ArrayList<String> titleArrayList = new ArrayList<>();
            for (NethunterModel nethunterModel: nethunterModelList){
                titleArrayList.add(nethunterModel.getTitle());
            }
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, titleArrayList);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            titlesBefore.setAdapter(arrayAdapter);
            titlesAfter.setAdapter(arrayAdapter);

            final AlertDialog.Builder adbMove = new AlertDialog.Builder(activity);
            adbMove.setView(promptViewMove);
            adbMove.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            adbMove.setPositiveButton("Move", (dialog, which) -> {

            });
            final AlertDialog adMove = adbMove.create();
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
                        NethunterData.getInstance().moveData(originalPositionIndex, targetPositionIndex, nethunterSQL);
                        NhPaths.showMessage(context, "Successfully moved item.");
                        adMove.dismiss();
                    }
                });
            });
            adMove.show();
        });
    }
}
