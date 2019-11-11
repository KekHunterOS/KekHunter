package com.offsec.nethunter.RecyclerViewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.offsec.nethunter.R;
import com.offsec.nethunter.models.KaliServicesModel;

import java.util.List;

public class KaliServicesRecycleViewAdapterDeleteItems extends RecyclerView.Adapter<KaliServicesRecycleViewAdapterDeleteItems.ItemViewHolder>{

	private static final String TAG = "KaliServiceRecycleViewChild";
	private Context context;
	private List<KaliServicesModel> kaliServicesModelList;

	public KaliServicesRecycleViewAdapterDeleteItems(Context context, List<KaliServicesModel> kaliServicesModelList){
		this.context = context;
		this.kaliServicesModelList = kaliServicesModelList;
	}

	@NonNull
	@Override
	public KaliServicesRecycleViewAdapterDeleteItems.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		View view = LayoutInflater.from(context).inflate(R.layout.kaliservices_recyclerview_dialog_delete, viewGroup, false);
		return new KaliServicesRecycleViewAdapterDeleteItems.ItemViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final ItemViewHolder itemViewHolder, int i) {
		itemViewHolder.runOnChrootStartCheckBox.setText(kaliServicesModelList.get(i).getServiceName());
	}

	@Override
	public int getItemCount() {
		return kaliServicesModelList.size();
	}

	class ItemViewHolder extends RecyclerView.ViewHolder{
		private CheckBox runOnChrootStartCheckBox;
		private ItemViewHolder(View view){
			super(view);
			runOnChrootStartCheckBox = view.findViewById(R.id.f_kaliservices_recyclerview_dialog_chkbox);
		}
	}

}