package com.offsec.nethunter.RecyclerViewData;

import android.content.Context;
import android.widget.Switch;

import androidx.lifecycle.MutableLiveData;

import com.offsec.nethunter.AsyncTask.KaliServicesAsyncTask;
import com.offsec.nethunter.SQL.KaliServicesSQL;
import com.offsec.nethunter.models.KaliServicesModel;
import com.offsec.nethunter.utils.NhPaths;

import java.util.ArrayList;
import java.util.List;

public class KaliServicesData {
	private static KaliServicesData instance;
	public static boolean isDataInitiated = false;
	private ArrayList<KaliServicesModel> kaliServicesModelArrayList = new ArrayList<>();
	private MutableLiveData<List<KaliServicesModel>> data = new MutableLiveData<>();
	public List<KaliServicesModel> kaliServicesModelListFull;
	private List<KaliServicesModel> copyOfKaliServicesModelListFull = new ArrayList<>();

	public synchronized static KaliServicesData getInstance(){
		if (instance == null) {
			instance = new KaliServicesData();
		}
		return instance;
	}

	public MutableLiveData<List<KaliServicesModel>> getKaliServicesModels(Context context){
		if (!isDataInitiated) {
			data.setValue(KaliServicesSQL.getInstance(context).bindData(kaliServicesModelArrayList));
			kaliServicesModelListFull = new ArrayList<>(data.getValue());
			isDataInitiated = true;
		}
		return data;
	}

	public MutableLiveData<List<KaliServicesModel>> getKaliServicesModels(){
		return data;
	}

	public void refreshData(){
		KaliServicesAsyncTask kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.GETITEMSTATUS);
		kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServicesAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {

			}

			@Override
			public void onAsyncTaskFinished(List<KaliServicesModel> kaliServicesModelList) {
				getKaliServicesModels().getValue().clear();
				getKaliServicesModels().getValue().addAll(kaliServicesModelList);
				getKaliServicesModels().postValue(getKaliServicesModels().getValue());
			}
		});
		kaliServicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
	}

	public void startServiceforItem(int position, Switch mSwitch, Context context){
		KaliServicesAsyncTask kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.START_SERVICE_FOR_ITEM, position);
		kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServicesAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {
				mSwitch.setEnabled(false);
			}

			@Override
			public void onAsyncTaskFinished(List<KaliServicesModel> kaliServicesModelList) {
				mSwitch.setEnabled(true);
				mSwitch.setChecked(kaliServicesModelList.get(position).getStatus().startsWith("[+]"));
				getKaliServicesModels().getValue().clear();
				getKaliServicesModels().getValue().addAll(kaliServicesModelList);
				getKaliServicesModels().postValue(getKaliServicesModels().getValue());
				if (!mSwitch.isChecked()) NhPaths.showMessage(context, "Failed starting " + getKaliServicesModels().getValue().get(position).getServiceName() + " service");
			}
		});
		kaliServicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
	}

	public void stopServiceforItem(int position, Switch mSwitch, Context context){
		KaliServicesAsyncTask kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.STOP_SERVICE_FOR_ITEM, position);
		kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServicesAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {
				mSwitch.setEnabled(false);
			}

			@Override
			public void onAsyncTaskFinished(List<KaliServicesModel> kaliServicesModelList) {
				mSwitch.setEnabled(true);
				mSwitch.setChecked(kaliServicesModelList.get(position).getStatus().startsWith("[+]"));
				getKaliServicesModels().getValue().clear();
				getKaliServicesModels().getValue().addAll(kaliServicesModelList);
				getKaliServicesModels().postValue(getKaliServicesModels().getValue());
				if (mSwitch.isChecked()) NhPaths.showMessage(context, "Failed stopping " + getKaliServicesModels().getValue().get(position).getServiceName() + " service");
			}
		});
		kaliServicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
	}

	public void editData(int position, ArrayList<String> dataArrayList, KaliServicesSQL kaliServicesSQL){
		KaliServicesAsyncTask kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.EDITDATA, position, dataArrayList, kaliServicesSQL);
		kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServicesAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {

			}

			@Override
			public void onAsyncTaskFinished(List<KaliServicesModel> kaliServicesModelList) {
				updateKaliServicesModelListFull(kaliServicesModelList);
				getKaliServicesModels().getValue().clear();
				getKaliServicesModels().getValue().addAll(kaliServicesModelList);
				getKaliServicesModels().postValue(getKaliServicesModels().getValue());
			}
		});
		kaliServicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
	}

	public void addData(int position, ArrayList<String> dataArrayList, KaliServicesSQL kaliServicesSQL){
		KaliServicesAsyncTask kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.ADDDATA, position, dataArrayList, kaliServicesSQL);
		kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServicesAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {

			}

			@Override
			public void onAsyncTaskFinished(List<KaliServicesModel> kaliServicesModelList) {
				updateKaliServicesModelListFull(kaliServicesModelList);
				getKaliServicesModels().getValue().clear();
				getKaliServicesModels().getValue().addAll(kaliServicesModelList);
				getKaliServicesModels().postValue(getKaliServicesModels().getValue());
			}
		});
		kaliServicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
	}

	public void deleteData(ArrayList<Integer> selectedPositionsIndex, ArrayList<Integer> selectedTargetIds, KaliServicesSQL kaliServicesSQL){
		KaliServicesAsyncTask kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.DELETEDATA, selectedPositionsIndex, selectedTargetIds, kaliServicesSQL);
		kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServicesAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {

			}

			@Override
			public void onAsyncTaskFinished(List<KaliServicesModel> kaliServicesModelList) {
				updateKaliServicesModelListFull(kaliServicesModelList);
				getKaliServicesModels().getValue().clear();
				getKaliServicesModels().getValue().addAll(kaliServicesModelList);
				getKaliServicesModels().postValue(getKaliServicesModels().getValue());
			}
		});
		kaliServicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
	}

	public void moveData(int originalPositionIndex, int targetPositionIndex, KaliServicesSQL kaliServicesSQL){
		KaliServicesAsyncTask kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.MOVEDATA, originalPositionIndex, targetPositionIndex, kaliServicesSQL);
		kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServicesAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {

			}

			@Override
			public void onAsyncTaskFinished(List<KaliServicesModel> kaliServicesModelList) {
				updateKaliServicesModelListFull(kaliServicesModelList);
				getKaliServicesModels().getValue().clear();
				getKaliServicesModels().getValue().addAll(kaliServicesModelList);
				getKaliServicesModels().postValue(getKaliServicesModels().getValue());
			}
		});
		kaliServicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
	}

	public String backupData(KaliServicesSQL kaliServicesSQL, String storedDBpath){
		return kaliServicesSQL.backupData(storedDBpath);
	}

	public String restoreData(KaliServicesSQL kaliServicesSQL, String storedDBpath){
		String returnedResult = kaliServicesSQL.restoreData(storedDBpath);
		if (returnedResult == null){
			KaliServicesAsyncTask kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.RESTOREDATA, kaliServicesSQL);
			kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServicesAsyncTaskListener() {
				@Override
				public void onAsyncTaskPrepare() {

				}

				@Override
				public void onAsyncTaskFinished(List<KaliServicesModel> kaliServicesModelList) {
					updateKaliServicesModelListFull(kaliServicesModelList);
					getKaliServicesModels().getValue().clear();
					getKaliServicesModels().getValue().addAll(kaliServicesModelList);
					getKaliServicesModels().postValue(getKaliServicesModels().getValue());
					refreshData();
				}
			});
			kaliServicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
			return null;
		} else {
			return returnedResult;
		}
	}

	public void resetData(KaliServicesSQL kaliServicesSQL){
		kaliServicesSQL.resetData();
		KaliServicesAsyncTask kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.RESTOREDATA, kaliServicesSQL);
		kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServicesAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {

			}

			@Override
			public void onAsyncTaskFinished(List<KaliServicesModel> kaliServicesModelList) {
				updateKaliServicesModelListFull(kaliServicesModelList);
				getKaliServicesModels().getValue().clear();
				getKaliServicesModels().getValue().addAll(kaliServicesModelList);
				getKaliServicesModels().postValue(getKaliServicesModels().getValue());
				refreshData();
			}
		});
		kaliServicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
	}

	public void updateRunOnChrootStartServices(int position, ArrayList<String> dataArrayList, KaliServicesSQL kaliServicesSQL) {
		KaliServicesAsyncTask kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.UPDATE_RUNONCHROOTSTART_SCRIPTS, position, dataArrayList, kaliServicesSQL);
		kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServicesAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {

			}

			@Override
			public void onAsyncTaskFinished(List<KaliServicesModel> kaliServicesModelList) {
				updateKaliServicesModelListFull(kaliServicesModelList);
				getKaliServicesModels().getValue().clear();
				getKaliServicesModels().getValue().addAll(kaliServicesModelList);
				getKaliServicesModels().postValue(getKaliServicesModels().getValue());
			}
		});
		kaliServicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
	}

	public void updateKaliServicesModelListFull(List<KaliServicesModel> copyOfKaliServicesModelList){
		kaliServicesModelListFull.clear();
		kaliServicesModelListFull.addAll(copyOfKaliServicesModelList);
	}

	private List<KaliServicesModel> getInitCopyOfKaliServicesModelListFull(){
		copyOfKaliServicesModelListFull.clear();
		copyOfKaliServicesModelListFull.addAll(kaliServicesModelListFull);
		return copyOfKaliServicesModelListFull;
	}

}
