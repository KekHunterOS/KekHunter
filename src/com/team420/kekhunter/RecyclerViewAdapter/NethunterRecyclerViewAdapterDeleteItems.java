package com.team420.kekhunter.RecyclerViewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.team420.kekhunter.R;
import com.team420.kekhunter.RecyclerViewData.KaliServicesData;
import com.team420.kekhunter.RecyclerViewData.NethunterData;
import com.team420.kekhunter.models.NethunterModel;

import java.util.List;


//Define recycleView adapter for listing different textView content.
public class NethunterRecyclerViewAdapterDeleteItems extends RecyclerView.Adapter<NethunterRecyclerViewAdapterDeleteItems.ItemViewHolder>{

	private static final String TAG = "NethunterRecyclerView";
	private Context context;
	private List<NethunterModel> nethunterModelList;

	public NethunterRecyclerViewAdapterDeleteItems(Context context, List<NethunterModel> nethunterModelList){
		this.context = context;
		this.nethunterModelList = nethunterModelList;
	}

	@NonNull
	@Override
	public NethunterRecyclerViewAdapterDeleteItems.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		View view = LayoutInflater.from(context).inflate(R.layout.nethunter_recyclerview_dialog_delete, viewGroup, false);
		return new NethunterRecyclerViewAdapterDeleteItems.ItemViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final ItemViewHolder itemViewHolder, int i) {
		itemViewHolder.checkBox.setText(nethunterModelList.get(i).getTitle());
	}

	@Override
	public int getItemCount() {
		return nethunterModelList.size();
	}

	class ItemViewHolder extends RecyclerView.ViewHolder{
		private CheckBox checkBox;
		private ItemViewHolder(View view){
			super(view);
			checkBox = view.findViewById(R.id.f_nethunter_recyclerview_dialog_chkbox);
		}
	}

}