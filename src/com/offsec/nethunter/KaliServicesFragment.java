package com.offsec.nethunter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.offsec.nethunter.AsyncTask.KaliServicesAsyncTask;
import com.offsec.nethunter.RecyclerViewAdapter.KaliServiceRecycleViewAdapterTitles;
import com.offsec.nethunter.RecyclerViewAdapter.KaliServicesRecycleViewAdapterDeleteItems;
import com.offsec.nethunter.RecyclerViewData.KaliServicesData;
import com.offsec.nethunter.SQL.KaliServicesSQL;
import com.offsec.nethunter.utils.NhPaths;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class KaliServicesFragment extends Fragment {
    private static final String TAG = "KaliServicesFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private View fragmentView;
    private Activity activity;
    private Context context;
    private RecyclerView recyclerViewServiceTitle;
    private Button refreshButton;
    private Button addButton;
    private Button deleteButton;
    private Button moveButton;
    private KaliServiceRecycleViewAdapterTitles kaliServiceRecycleViewAdapterTitles;
    private KaliServicesSQL kaliServicesSQL;
    private KaliServicesData kaliServicesData;
    private KaliServicesAsyncTask kaliServicesAsyncTask;
    private int targetPositionId;

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
        //First to get the stored data from SQL, and bind it to KaliServicesData;
        this.activity = getActivity();
        this.context = getContext();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup parent, Bundle savedInstanceState) {
        if (fragmentView != null){
            NhPaths.showMessage(context, "HOHO");
            return fragmentView;
        }
        View view = inflater.inflate(R.layout.kaliservices, parent, false);
        recyclerViewServiceTitle = view.findViewById(R.id.f_kaliservices_recycleviewServiceTitle);
        refreshButton = view.findViewById(R.id.f_kaliservices_refreshButton);
        addButton = view.findViewById(R.id.f_kaliservices_addItemButton);
        deleteButton = view.findViewById(R.id.f_kaliservices_deleteItemButton);
        moveButton = view.findViewById(R.id.f_kaliservices_moveItemButton);
        fragmentView = view;
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.kaliServicesSQL = new KaliServicesSQL(context);
        this.kaliServicesData = new KaliServicesData(context, kaliServicesSQL.getData());
        kaliServiceRecycleViewAdapterTitles = new KaliServiceRecycleViewAdapterTitles(context, kaliServicesData);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerViewServiceTitle.setLayoutManager(linearLayoutManager);
        recyclerViewServiceTitle.setAdapter(kaliServiceRecycleViewAdapterTitles);

        kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.CHECK_SERVICE);
        kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServiceTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished() {
                kaliServiceRecycleViewAdapterTitles.notifyDataSetChanged();
            }
        });
        kaliServicesAsyncTask.execute(kaliServicesData);
        onRefreshItemSetup();
        onAddItemSetup();
        onDeleteItemSetup();
        onMoveItemSetup();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.kaliservices, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View promptView = inflater.inflate(R.layout.kaliservices_custom_dialog_view, null);
        final TextView titleTextView = promptView.findViewById(R.id.f_kaliservices_adb_tv_title1);
        final EditText storedpathEditText = promptView.findViewById(R.id.f_kaliservices_adb_et_storedpath);

        switch (item.getItemId()){
            case R.id.f_kaliservices_menu_backupDB:
                titleTextView.setText("Full path to where you want to save the database:");
                storedpathEditText.setText(NhPaths.APP_SD_SQLBACKUP_PATH + "/FragmentKaliService");
                final AlertDialog.Builder adbBackup = new AlertDialog.Builder(activity);
                adbBackup.setView(promptView);
                adbBackup.setCancelable(true);
                adbBackup.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                adbBackup.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                final AlertDialog adBackup = adbBackup.create();
                adBackup.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        final Button buttonOK = adBackup.getButton(DialogInterface.BUTTON_POSITIVE);
                        buttonOK.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (kaliServicesSQL.backupData(storedpathEditText.getText().toString())){
                                    dialog.dismiss();
                                }
                            }
                        });
                    }
                });
                adBackup.show();
                break;
            case R.id.f_kaliservices_menu_restoreDB:
                titleTextView.setText("Full path of the db file from where you want to restore:");
                storedpathEditText.setText(NhPaths.APP_SD_SQLBACKUP_PATH + "/FragmentKaliService");
                final AlertDialog.Builder adbRestore = new AlertDialog.Builder(activity);
                adbRestore.setView(promptView);
                adbRestore.setCancelable(true);
                adbRestore.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                adbRestore.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                final AlertDialog adRestore = adbRestore.create();
                adRestore.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        final Button buttonOK = adRestore.getButton(DialogInterface.BUTTON_POSITIVE);
                        buttonOK.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (kaliServicesSQL.restoreData(storedpathEditText.getText().toString())){
                                    kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.CHECK_SERVICE);
                                    kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServiceTaskListener() {
                                        @Override
                                        public void onAsyncTaskPrepare() {
                                            kaliServicesData.resetService(kaliServicesSQL.getData());
                                        }

                                        @Override
                                        public void onAsyncTaskFinished() {
                                            dialog.dismiss();
                                            kaliServiceRecycleViewAdapterTitles.notifyDataSetChanged();
                                        }
                                    });
                                    kaliServicesAsyncTask.execute(kaliServicesData);
                                }
                            }
                        });
                    }
                });
                adRestore.show();
                break;
            case R.id.f_kaliservices_menu_ResetToDefault:
                kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.RESET_SERVICE);
                kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServiceTaskListener() {
                    @Override
                    public void onAsyncTaskPrepare() {

                    }

                    @Override
                    public void onAsyncTaskFinished() {
                        kaliServiceRecycleViewAdapterTitles.notifyDataSetChanged();
                    }
                });
                kaliServicesAsyncTask.execute(kaliServicesData, kaliServicesSQL);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.CHECK_SERVICE);
        kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServiceTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished() {
                kaliServiceRecycleViewAdapterTitles.notifyDataSetChanged();
            }
        });
        kaliServicesAsyncTask.execute(kaliServicesData);
    }

    private void onRefreshItemSetup(){
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.CHECK_SERVICE);
                kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServiceTaskListener() {
                    @Override
                    public void onAsyncTaskPrepare() {
                        setAllButtonDisable(false);
                    }

                    @Override
                    public void onAsyncTaskFinished() {
                        kaliServiceRecycleViewAdapterTitles.notifyDataSetChanged();
                        setAllButtonDisable(true);
                    }
                });
                kaliServicesAsyncTask.execute(kaliServicesData);
            }
        });
    }

    private void onAddItemSetup(){
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View promptViewAdd = inflater.inflate(R.layout.kaliservices_add_dialog_view, null);
                final EditText titleEditText = promptViewAdd.findViewById(R.id.f_kaliservices_add_adb_et_title);
                final EditText startCmdEditText = promptViewAdd.findViewById(R.id.f_kaliservices_add_adb_et_startcommand);
                final EditText stopCmdEditText = promptViewAdd.findViewById(R.id.f_kaliservices_add_adb_et_stopcommand);
                final EditText checkstatusEditText = promptViewAdd.findViewById(R.id.f_kaliservices_add_adb_et_checkstatuscommand);
                final CheckBox runOnBootCheckbox = promptViewAdd.findViewById(R.id.f_kaliservices_add_adb_checkbox_runonboot);
                final FloatingActionButton readmeButton1 = promptViewAdd.findViewById(R.id.f_kaliservices_add_btn_info_fab1);
                final FloatingActionButton readmeButton2 = promptViewAdd.findViewById(R.id.f_kaliservices_add_btn_info_fab2);
                final FloatingActionButton readmeButton3 = promptViewAdd.findViewById(R.id.f_kaliservices_add_btn_info_fab3);
                final FloatingActionButton readmeButton4 = promptViewAdd.findViewById(R.id.f_kaliservices_add_btn_info_fab4);
                final Spinner insertPositions = promptViewAdd.findViewById(R.id.f_kaliservices_add_adb_spr_positions);
                final Spinner insertTitles = promptViewAdd.findViewById(R.id.f_kaliservices_add_adb_spr_titles);
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, kaliServicesData.ServiceName);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                startCmdEditText.setText("service <servicename> start");
                stopCmdEditText.setText("service <servicename> stop");
                checkstatusEditText.setText("<servicename>");

                readmeButton1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder adb = new AlertDialog.Builder(activity);
                        adb.setTitle("HOW TO USE:")
                                .setMessage(getString(R.string.kaliservices_howto_startservice))
                                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                        AlertDialog ad = adb.create();
                        ad.setCancelable(true);
                        ad.show();
                    }
                });

                readmeButton2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder adb = new AlertDialog.Builder(activity);
                        adb.setTitle("HOW TO USE:")
                                .setMessage(getString(R.string.kaliservices_howto_stopservice))
                                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                        AlertDialog ad = adb.create();
                        ad.setCancelable(true);
                        ad.show();
                    }
                });

                readmeButton3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder adb = new AlertDialog.Builder(activity);
                        adb.setTitle("HOW TO USE:")
                                .setMessage(getString(R.string.kaliservices_howto_checkservice))
                                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                        AlertDialog ad = adb.create();
                        ad.setCancelable(true);
                        ad.show();
                    }
                });

                readmeButton4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder adb = new AlertDialog.Builder(activity);
                        adb.setTitle("HOW TO USE:")
                                .setMessage(getString(R.string.kaliservices_howto_runServiceOnBoot))
                                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                        AlertDialog ad = adb.create();
                        ad.setCancelable(true);
                        ad.show();
                    }
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
                            targetPositionId = kaliServicesData.ServiceName.size() + 1;
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

                final AlertDialog.Builder adbAdd = new AlertDialog.Builder(activity);
                adbAdd.setView(promptViewAdd);
                adbAdd.setCancelable(true);
                adbAdd.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                adbAdd.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                //If you want the dialog to stay open after clicking OK, you need to do it this way...
                final AlertDialog adAdd = adbAdd.create();
                adAdd.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        final Button buttonAdd = adAdd.getButton(DialogInterface.BUTTON_POSITIVE);
                        final ArrayList<String> tempData = new ArrayList<>();
                        buttonAdd.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (titleEditText.getText().toString().isEmpty()){
                                    NhPaths.showMessage(context, "Title cannot be empty");
                                } else if (startCmdEditText.getText().toString().isEmpty()){
                                    NhPaths.showMessage(context, "Start Command cannot be empty");
                                } else if (stopCmdEditText.getText().toString().isEmpty()){
                                    NhPaths.showMessage(context, "Stop Command cannot be empty");
                                } else if (checkstatusEditText.getText().toString().isEmpty()){
                                    NhPaths.showMessage(context, "Check Status Command cannot be empty");
                                } else {
                                    kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.ADD_SERVICE);
                                    kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServiceTaskListener() {
                                        @Override
                                        public void onAsyncTaskPrepare() {
                                            tempData.add(Integer.toString(targetPositionId));
                                            tempData.add(titleEditText.getText().toString());
                                            tempData.add(startCmdEditText.getText().toString());
                                            tempData.add(stopCmdEditText.getText().toString());
                                            tempData.add(checkstatusEditText.getText().toString());
                                            tempData.add(runOnBootCheckbox.isChecked()?"1":"0");
                                            kaliServicesSQL.addData(targetPositionId, tempData);
                                        }

                                        @Override
                                        public void onAsyncTaskFinished() {
                                            kaliServiceRecycleViewAdapterTitles.notifyDataSetChanged();
                                            NhPaths.showMessage(context, "Successfully added.");
                                            adAdd.dismiss();
                                        }
                                    });
                                    kaliServicesAsyncTask.execute(kaliServicesData, targetPositionId -1, tempData);
                                }
                            }
                        });
                    }
                });
                adAdd.show();
            }
        });
    }

    private void onDeleteItemSetup(){
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View promptViewDelete = inflater.inflate(R.layout.kaliservices_delete_dialog_view, null, false);
                final RecyclerView recyclerViewDeleteItem = promptViewDelete.findViewById(R.id.f_kaliservices_delete_recycleview);
                final KaliServicesRecycleViewAdapterDeleteItems kaliServicesRecycleViewAdapterDeleteItems = new KaliServicesRecycleViewAdapterDeleteItems(context, kaliServicesData);

                LinearLayoutManager linearLayoutManagerDelete = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
                recyclerViewDeleteItem.setLayoutManager(linearLayoutManagerDelete);
                recyclerViewDeleteItem.setAdapter(kaliServicesRecycleViewAdapterDeleteItems);

                final AlertDialog.Builder adbDelete = new AlertDialog.Builder(activity);
                adbDelete.setMessage("Select the service you want to remove: ");
                adbDelete.setView(promptViewDelete);
                adbDelete.setCancelable(true);
                adbDelete.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                adbDelete.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                //If you want the dialog to stay open after clicking OK, you need to do it this way...
                final AlertDialog adDelete = adbDelete.create();
                adDelete.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        final Button buttonDelete = adDelete.getButton(DialogInterface.BUTTON_POSITIVE);
                        buttonDelete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
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
                                    kaliServicesSQL.deleteData(selectedTargetIds);
                                    kaliServicesData.deleteService(selectedPosition);
                                    kaliServiceRecycleViewAdapterTitles.notifyDataSetChanged();
                                    NhPaths.showMessage(context, "Successfully deleted " + selectedPosition.size() + " items.");
                                    adDelete.dismiss();
                                } else NhPaths.showMessage(context, "Nothing to be deleted.");
                            }
                        });
                    }
                });
                adDelete.show();
            }
        });
    }

    private void onMoveItemSetup(){
        moveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View promptViewMove = inflater.inflate(R.layout.kaliservices_move_dialog_view, null, false);
                final Spinner titlesBefore = promptViewMove.findViewById(R.id.f_kaliservices_move_adb_spr_titlesbefore);
                final Spinner titlesAfter = promptViewMove.findViewById(R.id.f_kaliservices_move_adb_spr_titlesafter);
                final Spinner actions = promptViewMove.findViewById(R.id.f_kaliservices_move_adb_spr_actions);
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, kaliServicesData.ServiceName);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                titlesBefore.setAdapter(arrayAdapter);
                titlesAfter.setAdapter(arrayAdapter);
                final AlertDialog.Builder adbMove = new AlertDialog.Builder(activity);
                adbMove.setView(promptViewMove);
                adbMove.setCancelable(true);
                adbMove.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                adbMove.setPositiveButton("Move", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                final AlertDialog adMove = adbMove.create();
                adMove.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        final Button buttonMove = adMove.getButton(DialogInterface.BUTTON_POSITIVE);
                        buttonMove.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int originalPositionIndex = titlesBefore.getSelectedItemPosition();
                                int targetPositionIndex = titlesAfter.getSelectedItemPosition();
                                if (originalPositionIndex == targetPositionIndex ||
                                        (actions.getSelectedItemPosition() == 0 && targetPositionIndex == (originalPositionIndex + 1)) ||
                                        (actions.getSelectedItemPosition() == 1 && targetPositionIndex == (originalPositionIndex - 1))) {
                                    NhPaths.showMessage(context, "You are moving the item to the same position, nothing to be moved.");
                                } else {
                                    if (actions.getSelectedItemPosition() == 1) targetPositionIndex += 1;
                                    kaliServicesSQL.moveData(originalPositionIndex, targetPositionIndex);
                                    kaliServicesData.moveService(originalPositionIndex, targetPositionIndex);
                                    kaliServiceRecycleViewAdapterTitles.notifyDataSetChanged();
                                    NhPaths.showMessage(context, "Successfully moved item.");
                                    adMove.dismiss();
                                }
                            }
                        });
                    }
                });
                adMove.show();
            }
        });
    }

    private void setAllButtonDisable(boolean needToEnable) {
        refreshButton.setEnabled(needToEnable);
        addButton.setEnabled(needToEnable);
        deleteButton.setEnabled(needToEnable);
        moveButton.setEnabled(needToEnable);
    }
}
