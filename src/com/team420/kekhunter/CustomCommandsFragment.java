package com.team420.kekhunter;

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

import com.team420.kekhunter.RecyclerViewAdapter.CustomCommandsRecyclerViewAdapter;
import com.team420.kekhunter.RecyclerViewAdapter.CustomCommandsRecyclerViewAdapterDeleteItems;
import com.team420.kekhunter.RecyclerViewData.CustomCommandsData;
import com.team420.kekhunter.SQL.CustomCommandsSQL;
import com.team420.kekhunter.models.CustomCommandsModel;
import com.team420.kekhunter.utils.NhPaths;
import com.team420.kekhunter.viewmodels.CustomCommandsViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CustomCommandsFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "CustomCommandsFragment";
    private CustomCommandsRecyclerViewAdapter customCommandsRecyclerViewAdapter;
    private Context context;
    private Activity activity;
    private Button addButton;
    private Button deleteButton;
    private Button moveButton;
    private static int targetPositionId;

    public CustomCommandsFragment() {

    }

    public static CustomCommandsFragment newInstance(int sectionNumber) {
        CustomCommandsFragment fragment = new CustomCommandsFragment();
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {//this runs BEFORE the ui is available
        return inflater.inflate(R.layout.customcommands, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CustomCommandsViewModel customCommandsViewModel = ViewModelProviders.of(this).get(CustomCommandsViewModel.class);
        customCommandsViewModel.init(context);
        customCommandsViewModel.getLiveDataCustomCommandsModelList().observe(this, customCommandsModelList -> customCommandsRecyclerViewAdapter.notifyDataSetChanged());

        customCommandsRecyclerViewAdapter = new CustomCommandsRecyclerViewAdapter(context, customCommandsViewModel.getLiveDataCustomCommandsModelList().getValue());
        RecyclerView recyclerView = view.findViewById(R.id.f_customcommands_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(customCommandsRecyclerViewAdapter);

        addButton = view.findViewById(R.id.f_customcommands_addItemButton);
        deleteButton = view.findViewById(R.id.f_customcommands_deleteItemButton);
        moveButton = view.findViewById(R.id.f_customcommands_moveItemButton);

        onAddItemSetup();
        onDeleteItemSetup();
        onMoveItemSetup();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.custom_commands, menu);
        final MenuItem searchItem = menu.findItem(R.id.f_customcommands_action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnSearchClickListener(v -> menu.setGroupVisible(R.id.f_customcommands_menu_group1, false));
        searchView.setOnCloseListener(() -> {
            menu.setGroupVisible(R.id.f_customcommands_menu_group1, true);
            return false;
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                customCommandsRecyclerViewAdapter.getFilter().filter(newText);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final ViewGroup nullParent = null;
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View promptView = inflater.inflate(R.layout.customcommands_custom_dialog_view, nullParent);
        final TextView titleTextView = promptView.findViewById(R.id.f_customcommands_adb_tv_title1);
        final EditText storedpathEditText = promptView.findViewById(R.id.f_customcommands_adb_et_storedpath);

        switch (item.getItemId()){
            case R.id.f_customcommands_menu_backupDB:
                titleTextView.setText("Full path to where you want to save the database:");
                storedpathEditText.setText(NhPaths.APP_SD_SQLBACKUP_PATH + "/FragmentCustomCommands");
                AlertDialog.Builder adbBackup = new AlertDialog.Builder(activity);
                adbBackup.setView(promptView);
                adbBackup.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                adbBackup.setPositiveButton("OK", (dialog, which) -> { });
                final AlertDialog adBackup = adbBackup.create();
                adBackup.setOnShowListener(dialog -> {
                    final Button buttonOK = adBackup.getButton(DialogInterface.BUTTON_POSITIVE);
                    buttonOK.setOnClickListener(v -> {
                        String returnedResult = CustomCommandsData.getInstance().backupData(CustomCommandsSQL.getInstance(context), storedpathEditText.getText().toString());
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
            case R.id.f_customcommands_menu_restoreDB:
                titleTextView.setText("Full path of the db file from where you want to restore:");
                storedpathEditText.setText(NhPaths.APP_SD_SQLBACKUP_PATH + "/FragmentCustomCommands");
                AlertDialog.Builder adbRestore = new AlertDialog.Builder(activity);
                adbRestore.setView(promptView);
                adbRestore.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                adbRestore.setPositiveButton("OK", (dialog, which) -> { });
                final AlertDialog adRestore = adbRestore.create();
                adRestore.setOnShowListener(dialog -> {
                    final Button buttonOK = adRestore.getButton(DialogInterface.BUTTON_POSITIVE);
                    buttonOK.setOnClickListener(v -> {
                        String returnedResult = CustomCommandsData.getInstance().restoreData(CustomCommandsSQL.getInstance(context), storedpathEditText.getText().toString());
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
            case R.id.f_customcommands_menu_ResetToDefault:
                CustomCommandsData.getInstance().resetData(CustomCommandsSQL.getInstance(context));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        addButton = null;
        deleteButton = null;
        moveButton = null;
        customCommandsRecyclerViewAdapter = null;
    }

    private void onAddItemSetup() {
        addButton.setOnClickListener(v -> {
            final ViewGroup nullParent = null;
            List<CustomCommandsModel> customCommandsModelList = CustomCommandsData.getInstance().customCommandsModelListFull;
            if (customCommandsModelList == null) return;
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View promptViewAdd = inflater.inflate(R.layout.customcommands_add_dialog_view, nullParent);
            final EditText commandLabelEditText = promptViewAdd.findViewById(R.id.f_customcommands_add_adb_et_label);
            final EditText commandEditText = promptViewAdd.findViewById(R.id.f_customcommands_add_adb_et_command);
            final Spinner sendToSpinner = promptViewAdd.findViewById(R.id.f_customcommands_add_adb_spr_sendto);
            final Spinner execModeSpinner = promptViewAdd.findViewById(R.id.f_customcommands_add_adb_spr_execmode);
            final CheckBox runOnBootCheckbox = promptViewAdd.findViewById(R.id.f_customcommands_add_adb_checkbox_runonboot);
            final Spinner insertPositions = promptViewAdd.findViewById(R.id.f_customcommands_add_adb_spr_positions);
            final Spinner insertLabels = promptViewAdd.findViewById(R.id.f_customcommands_add_adb_spr_labels);


            ArrayList<String> commandLabelArrayList = new ArrayList<>();
            for (CustomCommandsModel customCommandsModel: customCommandsModelList){
                commandLabelArrayList.add(customCommandsModel.getCommandLabel());
            }

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, commandLabelArrayList);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            insertPositions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    //if Insert to Top
                    if (position == 0) {
                        insertLabels.setVisibility(View.INVISIBLE);
                        targetPositionId = 1;
                        //if Insert to Bottom
                    } else if (position == 1) {
                        insertLabels.setVisibility(View.INVISIBLE);
                        targetPositionId = customCommandsModelList.size() + 1;
                        //if Insert Before
                    } else if (position == 2) {
                        insertLabels.setVisibility(View.VISIBLE);
                        insertLabels.setAdapter(arrayAdapter);
                        insertLabels.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
                        insertLabels.setVisibility(View.VISIBLE);
                        insertLabels.setAdapter(arrayAdapter);
                        insertLabels.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
                    if (commandLabelEditText.getText().toString().isEmpty()){
                        NhPaths.showMessage(context, "Label cannot be empty");
                    } else if (commandEditText.getText().toString().isEmpty()){
                        NhPaths.showMessage(context, "Command String cannot be empty");
                    } else {
                        ArrayList<String> dataArrayList = new ArrayList<>();
                        dataArrayList.add(commandLabelEditText.getText().toString());
                        dataArrayList.add(commandEditText.getText().toString());
                        dataArrayList.add(sendToSpinner.getSelectedItem().toString());
                        dataArrayList.add(execModeSpinner.getSelectedItem().toString());
                        dataArrayList.add(runOnBootCheckbox.isChecked()?"1":"0");
                        CustomCommandsData.getInstance().addData(targetPositionId, dataArrayList, CustomCommandsSQL.getInstance(context));
                        adAdd.dismiss();
                    }
                });
            });
            adAdd.show();
        });
    }

    private void onDeleteItemSetup() {
        deleteButton.setOnClickListener(v -> {
            final ViewGroup nullParent = null;
            List<CustomCommandsModel> customCommandsModelList = CustomCommandsData.getInstance().customCommandsModelListFull;
            if (customCommandsModelList == null) return;
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View promptViewDelete = inflater.inflate(R.layout.customcommands_delete_dialog_view, nullParent, false);
            final RecyclerView recyclerViewDeleteItem = promptViewDelete.findViewById(R.id.f_customcommands_delete_recyclerview);
            CustomCommandsRecyclerViewAdapterDeleteItems customCommandsRecyclerViewAdapterDeleteItems = new CustomCommandsRecyclerViewAdapterDeleteItems(context, customCommandsModelList);

            LinearLayoutManager linearLayoutManagerDelete = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            recyclerViewDeleteItem.setLayoutManager(linearLayoutManagerDelete);
            recyclerViewDeleteItem.setAdapter(customCommandsRecyclerViewAdapterDeleteItems);

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
                            CheckBox box = viewHolder.itemView.findViewById(R.id.f_customcommands_recyclerview_dialog_chkbox);
                            if (box.isChecked()){
                                selectedPosition.add(i);
                                selectedTargetIds.add(i+1);
                            }
                        }
                    }
                    if (selectedPosition.size() != 0) {
                        CustomCommandsData.getInstance().deleteData(selectedPosition, selectedTargetIds, CustomCommandsSQL.getInstance(context));
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

    private void onMoveItemSetup() {
        moveButton.setOnClickListener(v -> {
            final ViewGroup nullParent = null;
            List<CustomCommandsModel> customCommandsModelList = CustomCommandsData.getInstance().customCommandsModelListFull;
            if (customCommandsModelList == null) return;
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View promptViewMove = inflater.inflate(R.layout.customcommands_move_dialog_view, nullParent, false);
            final Spinner titlesBefore = promptViewMove.findViewById(R.id.f_customcommands_move_adb_spr_labelsbefore);
            final Spinner titlesAfter = promptViewMove.findViewById(R.id.f_customcommands_move_adb_spr_labelsafter);
            final Spinner actions = promptViewMove.findViewById(R.id.f_customcommands_move_adb_spr_actions);

            ArrayList<String> commandLabelArrayList = new ArrayList<>();
            for (CustomCommandsModel customCommandsModel: customCommandsModelList){
                commandLabelArrayList.add(customCommandsModel.getCommandLabel());
            }

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, commandLabelArrayList);
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
                        CustomCommandsData.getInstance().moveData(originalPositionIndex, targetPositionIndex, CustomCommandsSQL.getInstance(context));
                        NhPaths.showMessage(context, "Successfully moved item.");
                        adMove.dismiss();
                    }
                });
            });
            adMove.show();
        });
    }
}
