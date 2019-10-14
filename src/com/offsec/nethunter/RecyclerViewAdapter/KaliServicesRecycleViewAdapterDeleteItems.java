package com.offsec.nethunter.RecyclerViewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.offsec.nethunter.R;
import com.offsec.nethunter.RecyclerViewData.KaliServicesData;


//Define recycleView adapter for listing different textView content.
public class KaliServicesRecycleViewAdapterDeleteItems extends RecyclerView.Adapter<KaliServicesRecycleViewAdapterDeleteItems.ItemViewHolder>{

	private static final String TAG = "KaliServiceRecycleViewChild";
	private Context context;
	private KaliServicesData kaliServicesData;

	public KaliServicesRecycleViewAdapterDeleteItems(Context context, KaliServicesData kaliServicesData){
		this.context = context;
		this.kaliServicesData = kaliServicesData;
	}

	@NonNull
	@Override
	public KaliServicesRecycleViewAdapterDeleteItems.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		View view = LayoutInflater.from(context).inflate(R.layout.kaliservices_recycleview_dialog_delete, viewGroup, false);
		return new KaliServicesRecycleViewAdapterDeleteItems.ItemViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final ItemViewHolder itemViewHolder, int i) {
		itemViewHolder.checkBox.setText(kaliServicesData.ServiceName.get(i));
	}

	@Override
	public int getItemCount() {
		return kaliServicesData.ServiceName.size();
	}

	class ItemViewHolder extends RecyclerView.ViewHolder{
		private CheckBox checkBox;
		private ItemViewHolder(View view){
			super(view);
			checkBox = view.findViewById(R.id.f_kaliservices_recycleview_dialog_chkbox);
		}
	}

}