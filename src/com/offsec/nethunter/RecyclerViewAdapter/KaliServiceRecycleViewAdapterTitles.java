package com.offsec.nethunter.RecyclerViewAdapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.offsec.nethunter.AsyncTask.KaliServicesAsyncTask;
import com.offsec.nethunter.R;
import com.offsec.nethunter.RecyclerViewData.KaliServicesData;
import com.offsec.nethunter.SQL.KaliServicesSQL;
import com.offsec.nethunter.utils.NhPaths;

import java.util.ArrayList;

//Define recycleView adapter for listing different textView content.
public class KaliServiceRecycleViewAdapterTitles extends RecyclerView.Adapter<KaliServiceRecycleViewAdapterTitles.ItemViewHolder>{
	private static final String TAG = "KaliServiceRecycleView";
	private Context context;
	private KaliServicesData kaliServicesData;
	private KaliServicesSQL kaliServicesSQL;
	private KaliServicesAsyncTask kaliServicesAsyncTask;

	public KaliServiceRecycleViewAdapterTitles(Context context, KaliServicesData kaliServicesData){
		this.context = context;
		this.kaliServicesData = kaliServicesData;
		this.kaliServicesSQL = new KaliServicesSQL(context);
	}

	@NonNull
	@Override
	public KaliServiceRecycleViewAdapterTitles.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		View view = LayoutInflater.from(context).inflate(R.layout.kaliservices_recycleview_servicetitle, viewGroup, false);

		return new KaliServiceRecycleViewAdapterTitles.ItemViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final ItemViewHolder itemViewHolder, int i) {
		final int position = i;
		final String tempStatus = kaliServicesData.Status.get(i);
		final Spannable tempStatusTextView = new SpannableString(tempStatus);
		tempStatusTextView.setSpan(new ForegroundColorSpan(tempStatus.startsWith("[!]")?Color.CYAN:tempStatus.startsWith("[+]")?Color.GREEN:tempStatus.startsWith("[-]")?Color.parseColor("#D81B60"):Color.LTGRAY),0, tempStatus.length(),0);
		itemViewHolder.nametextView.setText(kaliServicesData.ServiceName.get(i));
		itemViewHolder.statustextView.setText(tempStatusTextView);
		itemViewHolder.runOnBootCheckbox.setChecked(kaliServicesData.RunOnChrootStart.get(i).equals("1"));
		itemViewHolder.mSwitch.setChecked(kaliServicesData.isChecked.get(i));
		itemViewHolder.editbutton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				final View promptViewEdit = mInflater.inflate(R.layout.kaliservices_edit_dialog_view, null);
				final EditText titleEditText = promptViewEdit.findViewById(R.id.f_kaliservices_edit_adb_et_title);
				final EditText startCmdEditText = promptViewEdit.findViewById(R.id.f_kaliservices_edit_adb_et_startcommand);
				final EditText stopCmdEditText = promptViewEdit.findViewById(R.id.f_kaliservices_edit_adb_et_stopcommand);
				final EditText checkstatusEditText = promptViewEdit.findViewById(R.id.f_kaliservices_edit_adb_et_checkstatuscommand);
				final CheckBox runOnBootCheckbox = promptViewEdit.findViewById(R.id.f_kaliservices_edit_adb_checkbox_runonboot);
				final FloatingActionButton readmeButton1 = promptViewEdit.findViewById(R.id.f_kaliservices_edit_btn_info_fab1);
				final FloatingActionButton readmeButton2 = promptViewEdit.findViewById(R.id.f_kaliservices_edit_btn_info_fab2);
				final FloatingActionButton readmeButton3 = promptViewEdit.findViewById(R.id.f_kaliservices_edit_btn_info_fab3);
				final FloatingActionButton readmeButton4 = promptViewEdit.findViewById(R.id.f_kaliservices_edit_btn_info_fab4);

				readmeButton1.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						androidx.appcompat.app.AlertDialog.Builder adb = new androidx.appcompat.app.AlertDialog.Builder(context);
						adb.setTitle("HOW TO USE:")
								.setMessage(context.getString(R.string.kaliservices_howto_startservice))
								.setNegativeButton("Close", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialogInterface, int i) {
										dialogInterface.dismiss();
									}
								});
						androidx.appcompat.app.AlertDialog ad = adb.create();
						ad.setCancelable(true);
						ad.show();
					}
				});

				readmeButton2.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						androidx.appcompat.app.AlertDialog.Builder adb = new androidx.appcompat.app.AlertDialog.Builder(context);
						adb.setTitle("HOW TO USE:")
								.setMessage(context.getString(R.string.kaliservices_howto_stopservice))
								.setNegativeButton("Close", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialogInterface, int i) {
										dialogInterface.dismiss();
									}
								});
						androidx.appcompat.app.AlertDialog ad = adb.create();
						ad.setCancelable(true);
						ad.show();
					}
				});

				readmeButton3.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						androidx.appcompat.app.AlertDialog.Builder adb = new androidx.appcompat.app.AlertDialog.Builder(context);
						adb.setTitle("HOW TO USE:")
								.setMessage(context.getString(R.string.kaliservices_howto_checkservice))
								.setNegativeButton("Close", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialogInterface, int i) {
										dialogInterface.dismiss();
									}
								});
						androidx.appcompat.app.AlertDialog ad = adb.create();
						ad.setCancelable(true);
						ad.show();
					}
				});

				readmeButton4.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						androidx.appcompat.app.AlertDialog.Builder adb = new androidx.appcompat.app.AlertDialog.Builder(context);
						adb.setTitle("HOW TO USE:")
								.setMessage(context.getString(R.string.kaliservices_howto_runServiceOnBoot))
								.setNegativeButton("Close", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialogInterface, int i) {
										dialogInterface.dismiss();
									}
								});
						androidx.appcompat.app.AlertDialog ad = adb.create();
						ad.setCancelable(true);
						ad.show();
					}
				});

				titleEditText.setText(kaliServicesData.ServiceName.get(position));
				startCmdEditText.setText(kaliServicesData.CommandforStartService.get(position));
				stopCmdEditText.setText(kaliServicesData.CommandforStopService.get(position));
				checkstatusEditText.setText(kaliServicesData.CommandforCheckServiceStatus.get(position));
				runOnBootCheckbox.setChecked(kaliServicesData.RunOnChrootStart.get(position).equals("1"));
				final AlertDialog.Builder adbEdit = new AlertDialog.Builder(context);
				adbEdit.setView(promptViewEdit);
				adbEdit.setCancelable(false);
				adbEdit.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				adbEdit.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
				final AlertDialog adEdit = adbEdit.create();
				adEdit.setOnShowListener(new DialogInterface.OnShowListener() {
					@Override
					public void onShow(DialogInterface dialog) {
						final Button buttonEdit = adEdit.getButton(DialogInterface.BUTTON_POSITIVE);
						final ArrayList<String> tempData = new ArrayList<>();
						buttonEdit.setOnClickListener(new View.OnClickListener() {
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
								}else {
									kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.CHECK_SERVICE);
									kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServiceTaskListener() {
										@Override
										public void onAsyncTaskPrepare() {
											tempData.add(titleEditText.getText().toString());
											tempData.add(startCmdEditText.getText().toString());
											tempData.add(stopCmdEditText.getText().toString());
											tempData.add(checkstatusEditText.getText().toString());
											tempData.add(runOnBootCheckbox.isChecked()?"1":"0");
											kaliServicesSQL.editData(position, tempData);
											kaliServicesData.editResult(position, tempData);
										}

										@Override
										public void onAsyncTaskFinished() {
											notifyDataSetChanged();
											NhPaths.showMessage(context, "Successfully edited.");
											adEdit.dismiss();
										}
									});
									kaliServicesAsyncTask.execute(kaliServicesData);
								}
							}
						});
					}
				});
				adEdit.show();

			}
		});

		itemViewHolder.runOnBootCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
				kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.UPDATE_RUNONCHROOTSTART);
				kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServiceTaskListener() {
					@Override
					public void onAsyncTaskPrepare() {
						kaliServicesData.RunOnChrootStart.set(position,(isChecked)?"1":"0");
						kaliServicesSQL.editData(position, kaliServicesData.dataToStringArray(position));
					}

					@Override
					public void onAsyncTaskFinished() {
						notifyDataSetChanged();
					}
				});
				kaliServicesAsyncTask.execute(kaliServicesData);
			}
		});

		itemViewHolder.mSwitch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (itemViewHolder.mSwitch.isChecked()){
					kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.START_SERVICE);
					kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServiceTaskListener() {

						@Override
						public void onAsyncTaskPrepare() {
							itemViewHolder.mSwitch.setEnabled(false);
						}

						@Override
						public void onAsyncTaskFinished() {
							notifyDataSetChanged();
							itemViewHolder.mSwitch.setEnabled(true);
						}
					});
					kaliServicesAsyncTask.execute(kaliServicesData, position);
				} else {
					kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.STOP_SERVICE);
					kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServiceTaskListener() {

						@Override
						public void onAsyncTaskPrepare() {
							itemViewHolder.mSwitch.setEnabled(false);
						}

						@Override
						public void onAsyncTaskFinished() {
							notifyDataSetChanged();
							itemViewHolder.mSwitch.setEnabled(true);
						}
					});
					kaliServicesAsyncTask.execute(kaliServicesData, position);
				}
			}
		});

	}

	@Override
	public int getItemCount() {
		return kaliServicesData.ServiceName.size();
	}

	@Override
	public long getItemId(int position) {
		return super.getItemId(position);
	}

	class ItemViewHolder extends RecyclerView.ViewHolder{
		private TextView nametextView;
		private Button editbutton;
		private Switch mSwitch;
		private CheckBox runOnBootCheckbox;
		private TextView statustextView;

		private ItemViewHolder(View view){
			super(view);
			nametextView = view.findViewById(R.id.f_kaliservices_recycleview_servicetitle_tv);
			editbutton = view.findViewById(R.id.f_kaliservices_recycleview_edit_btn);
			runOnBootCheckbox = view.findViewById(R.id.f_kaliservices_recycleview_runonboot_checkbox);
			mSwitch = view.findViewById(R.id.f_kaliservices_recycleview_switch_toggle);
			statustextView = view.findViewById(R.id.f_kaliservices_recycleview_serviceresult_tv);
		}
	}
}