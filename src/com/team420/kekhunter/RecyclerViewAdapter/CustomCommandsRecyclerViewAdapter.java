package com.team420.kekhunter.RecyclerViewAdapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.team420.kekhunter.R;
import com.team420.kekhunter.RecyclerViewData.CustomCommandsData;
import com.team420.kekhunter.SQL.CustomCommandsSQL;
import com.team420.kekhunter.models.CustomCommandsModel;
import com.team420.kekhunter.utils.NhPaths;

import java.util.ArrayList;
import java.util.List;

public class CustomCommandsRecyclerViewAdapter extends RecyclerView.Adapter<CustomCommandsRecyclerViewAdapter.ItemViewHolder> implements Filterable {
  private static final String TAG = "KaliServiceRecycleView";
  private Context context;
  private List<CustomCommandsModel> customCommandsModelList;

  public CustomCommandsRecyclerViewAdapter(Context context, List<CustomCommandsModel> customCommandsModelList) {
    this.context = context;
    this.customCommandsModelList = customCommandsModelList;
  }

  @NonNull
  @Override
  public CustomCommandsRecyclerViewAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    View view = LayoutInflater.from(context).inflate(R.layout.customcommands_recyclerview_main, viewGroup, false);
    return new CustomCommandsRecyclerViewAdapter.ItemViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull final ItemViewHolder itemViewHolder, int position) {
    itemViewHolder.commandLabelTextView.setText(customCommandsModelList.get(position).getCommandLabel());
    itemViewHolder.execEnvTextView.setText(customCommandsModelList.get(position).getRuntimeEnv());
    itemViewHolder.execModeTextView.setText(customCommandsModelList.get(position).getExecutionMode());
    itemViewHolder.runOnBootTextView.setText(customCommandsModelList.get(position).getRunOnBoot().equals("1") ? "yes" : "no");
    itemViewHolder.commandLabelTextView.setOnLongClickListener(v -> {
      final ViewGroup nullParent = null;
      final LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      final View promptViewEdit = mInflater.inflate(R.layout.customcommands_edit_dialog_view, nullParent);
      final EditText commandLabelEditText = promptViewEdit.findViewById(R.id.f_customcommands_edit_adb_et_commandlabel);
      final EditText commandEditText = promptViewEdit.findViewById(R.id.f_customcommands_edit_adb_et_command);
      final Spinner sendToSpinner = promptViewEdit.findViewById(R.id.f_customcommands_edit_adb_spr_sendto);
      final Spinner execModeSpinner = promptViewEdit.findViewById(R.id.f_customcommands_edit_adb_spr_execmode);
      final MaterialCheckBox runOnBootCheckbox = promptViewEdit.findViewById(R.id.f_customcommands_edit_adb_checkbox_runonboot);

      commandLabelEditText.setText(CustomCommandsData.getInstance().customCommandsModelListFull.get(
        CustomCommandsData.getInstance().customCommandsModelListFull.indexOf(
          customCommandsModelList.get(position))).getCommandLabel());
      commandEditText.setText(CustomCommandsData.getInstance().customCommandsModelListFull.get(
        CustomCommandsData.getInstance().customCommandsModelListFull.indexOf(
          customCommandsModelList.get(position))).getCommand());
      sendToSpinner.setSelection(CustomCommandsData.getInstance().customCommandsModelListFull.get(
        CustomCommandsData.getInstance().customCommandsModelListFull.indexOf(
          customCommandsModelList.get(position))).getRuntimeEnv().equals("android") ? 0 : 1);
      execModeSpinner.setSelection(CustomCommandsData.getInstance().customCommandsModelListFull.get(
        CustomCommandsData.getInstance().customCommandsModelListFull.indexOf(
          customCommandsModelList.get(position))).getExecutionMode().equals("interactive") ? 0 : 1);
      runOnBootCheckbox.setChecked(CustomCommandsData.getInstance().customCommandsModelListFull.get(
        CustomCommandsData.getInstance().customCommandsModelListFull.indexOf(
          customCommandsModelList.get(position))).getRunOnBoot().equals("1"));

      MaterialAlertDialogBuilder adbEdit = new MaterialAlertDialogBuilder(context, R.style.DialogStyle);
      adbEdit.setView(promptViewEdit);
      adbEdit.setCancelable(true);
      adbEdit.setPositiveButton("OK", (dialog, which) -> { });
      final AlertDialog adEdit = adbEdit.create();
      adEdit.setOnShowListener(dialog -> {
        final Button buttonEdit = adEdit.getButton(DialogInterface.BUTTON_POSITIVE);
        buttonEdit.setOnClickListener(v1 -> {
          if (commandLabelEditText.getText().toString().isEmpty()) {
            NhPaths.showMessage(context, "Label cannot be empty");
          } else if (commandEditText.getText().toString().isEmpty()) {
            NhPaths.showMessage(context, "Command string cannot be empty");
          } else {
            ArrayList<String> dataArrayList = new ArrayList<>();
            dataArrayList.add(commandLabelEditText.getText().toString());
            dataArrayList.add(commandEditText.getText().toString());
            dataArrayList.add(sendToSpinner.getSelectedItem().toString());
            dataArrayList.add(execModeSpinner.getSelectedItem().toString());
            dataArrayList.add(runOnBootCheckbox.isChecked() ? "1" : "0");
            CustomCommandsData.getInstance().editData(CustomCommandsData.getInstance().customCommandsModelListFull.indexOf(
              customCommandsModelList.get(position)), dataArrayList, CustomCommandsSQL.getInstance(context));
            adEdit.dismiss();
          }
        });
      });
      adEdit.show();
      return false;
    });
    itemViewHolder.runButton.setOnClickListener(v -> CustomCommandsData.getInstance().runCommandforitem(position, context));
  }

  @Override
  public int getItemCount() {
    return customCommandsModelList.size();
  }

  @Override
  public long getItemId(int position) {
    return super.getItemId(position);
  }

  @Override
  public Filter getFilter() {
    return CustomCommandsModelListFilter;
  }

  private Filter CustomCommandsModelListFilter = new Filter() {

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
      FilterResults results = new FilterResults();
      if (constraint == null || constraint.length() == 0) {
        results.values = new ArrayList<>(CustomCommandsData.getInstance().customCommandsModelListFull);
      } else {
        String filterPattern = constraint.toString().toLowerCase().trim();
        List<CustomCommandsModel> tempCustomCommandsModelList = new ArrayList<>();
        for (CustomCommandsModel customCommandsModel : CustomCommandsData.getInstance().customCommandsModelListFull) {
          if (customCommandsModel.getCommandLabel().toLowerCase().contains(filterPattern)) {
            tempCustomCommandsModelList.add(customCommandsModel);
          }
        }
        results.values = tempCustomCommandsModelList;
      }
      return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
      CustomCommandsData.getInstance().getCustomCommandsModels().getValue().clear();
      CustomCommandsData.getInstance().getCustomCommandsModels().getValue().addAll((List<CustomCommandsModel>) results.values);
      CustomCommandsData.getInstance().getCustomCommandsModels().postValue(CustomCommandsData.getInstance().getCustomCommandsModels().getValue());
    }
  };

  class ItemViewHolder extends RecyclerView.ViewHolder {
    private TextView commandLabelTextView;
    private TextView execEnvTextView;
    private TextView execModeTextView;
    private TextView runOnBootTextView;
    //private Button editButton;
    private Button runButton;

    private ItemViewHolder(View view) {
      super(view);
      commandLabelTextView = view.findViewById(R.id.f_customcommands_recyclerview_tv_cmdlabel);
      execEnvTextView = view.findViewById(R.id.f_customcommands_recyclerview_tv_execenvironment);
      execModeTextView = view.findViewById(R.id.f_customcommands_recyclerview_tv_execmode);
      runOnBootTextView = view.findViewById(R.id.f_customcommands_recyclerview_tv_runonboot);
      //editButton = view.findViewById(R.id.f_customcommands_recyclerview_btn_edit);
      runButton = view.findViewById(R.id.f_customcommands_recyclerview_btn_run);
    }
  }
}
