package com.offsec.nethunter.RecyclerViewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.offsec.nethunter.R;
import com.offsec.nethunter.models.CustomCommandsModel;

import java.util.List;

public class CustomCommandsRecyclerViewAdapterDeleteItems extends RecyclerView.Adapter<CustomCommandsRecyclerViewAdapterDeleteItems.ItemViewHolder>{

	private static final String TAG = "CustomCommandsRVA_Delete";
	private Context context;
	private List<CustomCommandsModel> customCommandsModelList;

	public CustomCommandsRecyclerViewAdapterDeleteItems(Context context, List<CustomCommandsModel> customCommandsModelList){
		this.context = context;
		this.customCommandsModelList = customCommandsModelList;
	}

	@NonNull
	@Override
	public CustomCommandsRecyclerViewAdapterDeleteItems.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		View view = LayoutInflater.from(context).inflate(R.layout.customcommands_recyclerview_dialog_delete, viewGroup, false);
		return new CustomCommandsRecyclerViewAdapterDeleteItems.ItemViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final ItemViewHolder itemViewHolder, int i) {
		itemViewHolder.runOnChrootStartCheckBox.setText(customCommandsModelList.get(i).getCommandLabel());
	}

	@Override
	public int getItemCount() {
		return customCommandsModelList.size();
	}

	class ItemViewHolder extends RecyclerView.ViewHolder{
		private CheckBox runOnChrootStartCheckBox;
		private ItemViewHolder(View view){
			super(view);
			runOnChrootStartCheckBox = view.findViewById(R.id.f_customcommands_recyclerview_dialog_chkbox);
		}
	}

}