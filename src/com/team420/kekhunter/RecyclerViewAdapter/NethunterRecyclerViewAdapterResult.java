package com.team420.kekhunter.RecyclerViewAdapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.team420.kekhunter.R;
import com.team420.kekhunter.models.NethunterModel;
import com.team420.kekhunter.utils.NhPaths;
import com.team420.kekhunter.viewmodels.NethunterViewModel;

import java.util.List;

public class NethunterRecyclerViewAdapterResult extends RecyclerView.Adapter<NethunterRecyclerViewAdapterResult.ItemViewHolder>{

    private static final String TAG = "NethunterRecyclerView";
    private String[] resultStrings;
    private Context context;

    public NethunterRecyclerViewAdapterResult(Context context, String[] resultStrings) {
        this.context = context;
        this.resultStrings = resultStrings;
    }

    @NonNull
    @Override
    public NethunterRecyclerViewAdapterResult.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.nethunter_recyclerview_result, parent, false);
        return new NethunterRecyclerViewAdapterResult.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NethunterRecyclerViewAdapterResult.ItemViewHolder holder, int position) {
        holder.resultTextView.setText(resultStrings[position]);
        holder.resultTextView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager cm = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData cData = ClipData.newPlainText("text", holder.resultTextView.getText());
                cm.setPrimaryClip(cData);
                NhPaths.showMessage(context, "Copied to clipboard: " + holder.resultTextView.getText());
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return resultStrings.length;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder{
        private TextView resultTextView;
        private ItemViewHolder(View view) {
            super(view);
            resultTextView = view.findViewById(R.id.f_nethunter_item_result_tv);
        }
    }
}
