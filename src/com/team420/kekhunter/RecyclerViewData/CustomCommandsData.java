package com.team420.kekhunter.RecyclerViewData;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.team420.kekhunter.AsyncTask.CustomCommandsAsyncTask;
import com.team420.kekhunter.SQL.CustomCommandsSQL;
import com.team420.kekhunter.models.CustomCommandsModel;

import java.util.ArrayList;
import java.util.List;

public class CustomCommandsData {
	private static CustomCommandsData instance;
	public static boolean isDataInitiated = false;
	private ArrayList<CustomCommandsModel> customCommandsModelArrayList = new ArrayList<>();
	private MutableLiveData<List<CustomCommandsModel>> data = new MutableLiveData<>();
	public List<CustomCommandsModel> customCommandsModelListFull;
	private List<CustomCommandsModel> copyOfCustomCommandsModelListFull = new ArrayList<>();

	public synchronized static CustomCommandsData getInstance(){
		if (instance == null) {
			instance = new CustomCommandsData();
		}
		return instance;
	}

	public MutableLiveData<List<CustomCommandsModel>> getCustomCommandsModels(Context context){
		if (!isDataInitiated) {
			data.setValue(CustomCommandsSQL.getInstance(context).bindData(customCommandsModelArrayList));
			customCommandsModelListFull = new ArrayList<>(data.getValue());
			isDataInitiated = true;
		}
		return data;
	}

	public MutableLiveData<List<CustomCommandsModel>> getCustomCommandsModels(){
		return data;
	}

	public void runCommandforitem(int position, Context context) {
		CustomCommandsAsyncTask customCommandsAsyncTask = new CustomCommandsAsyncTask(CustomCommandsAsyncTask.RUNCMD, position, context);
		customCommandsAsyncTask.setListener(new CustomCommandsAsyncTask.CustomCommandsAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {

			}

			@Override
			public void onAsyncTaskFinished(List<CustomCommandsModel> customCommandsModelList) {
				updateCustomCommandsModelListFull(customCommandsModelList);
				getCustomCommandsModels().getValue().clear();
				getCustomCommandsModels().getValue().addAll(customCommandsModelList);
				getCustomCommandsModels().postValue(getCustomCommandsModels().getValue());
			}
		});
		customCommandsAsyncTask.execute(getInitCopyOfCustomCommandsModelListFull());
	}

	public void editData(int position, ArrayList<String> dataArrayList, CustomCommandsSQL customCommandsSQL){
		CustomCommandsAsyncTask customCommandsAsyncTask = new CustomCommandsAsyncTask(CustomCommandsAsyncTask.EDITDATA, position, dataArrayList, customCommandsSQL);
		customCommandsAsyncTask.setListener(new CustomCommandsAsyncTask.CustomCommandsAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {

			}

			@Override
			public void onAsyncTaskFinished(List<CustomCommandsModel> customCommandsModelList) {
				updateCustomCommandsModelListFull(customCommandsModelList);
				getCustomCommandsModels().getValue().clear();
				getCustomCommandsModels().getValue().addAll(customCommandsModelList);
				getCustomCommandsModels().postValue(getCustomCommandsModels().getValue());
			}
		});
		customCommandsAsyncTask.execute(getInitCopyOfCustomCommandsModelListFull());
	}

	public void addData(int position, ArrayList<String> dataArrayList, CustomCommandsSQL customCommandsSQL){
		CustomCommandsAsyncTask customCommandsAsyncTask = new CustomCommandsAsyncTask(CustomCommandsAsyncTask.ADDDATA, position, dataArrayList, customCommandsSQL);
		customCommandsAsyncTask.setListener(new CustomCommandsAsyncTask.CustomCommandsAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {

			}

			@Override
			public void onAsyncTaskFinished(List<CustomCommandsModel> customCommandsModelList) {
				updateCustomCommandsModelListFull(customCommandsModelList);
				getCustomCommandsModels().getValue().clear();
				getCustomCommandsModels().getValue().addAll(customCommandsModelList);
				getCustomCommandsModels().postValue(getCustomCommandsModels().getValue());
			}
		});
		customCommandsAsyncTask.execute(getInitCopyOfCustomCommandsModelListFull());
	}

	public void deleteData(ArrayList<Integer> selectedPositionsIndex, ArrayList<Integer> selectedTargetIds, CustomCommandsSQL customCommandsSQL){
		CustomCommandsAsyncTask customCommandsAsyncTask = new CustomCommandsAsyncTask(CustomCommandsAsyncTask.DELETEDATA, selectedPositionsIndex, selectedTargetIds, customCommandsSQL);
		customCommandsAsyncTask.setListener(new CustomCommandsAsyncTask.CustomCommandsAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {

			}

			@Override
			public void onAsyncTaskFinished(List<CustomCommandsModel> customCommandsModelList) {
				updateCustomCommandsModelListFull(customCommandsModelList);
				getCustomCommandsModels().getValue().clear();
				getCustomCommandsModels().getValue().addAll(customCommandsModelList);
				getCustomCommandsModels().postValue(getCustomCommandsModels().getValue());
			}
		});
		customCommandsAsyncTask.execute(getInitCopyOfCustomCommandsModelListFull());
	}

	public void moveData(int originalPositionIndex, int targetPositionIndex, CustomCommandsSQL customCommandsSQL){
		CustomCommandsAsyncTask customCommandsAsyncTask = new CustomCommandsAsyncTask(CustomCommandsAsyncTask.MOVEDATA, originalPositionIndex, targetPositionIndex, customCommandsSQL);
		customCommandsAsyncTask.setListener(new CustomCommandsAsyncTask.CustomCommandsAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {

			}

			@Override
			public void onAsyncTaskFinished(List<CustomCommandsModel> customCommandsModelList) {
				updateCustomCommandsModelListFull(customCommandsModelList);
				getCustomCommandsModels().getValue().clear();
				getCustomCommandsModels().getValue().addAll(customCommandsModelList);
				getCustomCommandsModels().postValue(getCustomCommandsModels().getValue());
			}
		});
		customCommandsAsyncTask.execute(getInitCopyOfCustomCommandsModelListFull());
	}

	public String backupData(CustomCommandsSQL customCommandsSQL, String storedDBpath){
		return customCommandsSQL.backupData(storedDBpath);
	}

	public String restoreData(CustomCommandsSQL customCommandsSQL, String storedDBpath){
		String returnedResult = customCommandsSQL.restoreData(storedDBpath);
		if (returnedResult == null){
			CustomCommandsAsyncTask customCommandsAsyncTask = new CustomCommandsAsyncTask(CustomCommandsAsyncTask.RESTOREDATA, customCommandsSQL);
			customCommandsAsyncTask.setListener(new CustomCommandsAsyncTask.CustomCommandsAsyncTaskListener() {
				@Override
				public void onAsyncTaskPrepare() {

				}

				@Override
				public void onAsyncTaskFinished(List<CustomCommandsModel> customCommandsModelList) {
					updateCustomCommandsModelListFull(customCommandsModelList);
					getCustomCommandsModels().getValue().clear();
					getCustomCommandsModels().getValue().addAll(customCommandsModelList);
					getCustomCommandsModels().postValue(getCustomCommandsModels().getValue());
				}
			});
			customCommandsAsyncTask.execute(getInitCopyOfCustomCommandsModelListFull());
			return null;
		} else {
			return returnedResult;
		}
	}

	public void resetData(CustomCommandsSQL customCommandsSQL){
		customCommandsSQL.resetData();
		CustomCommandsAsyncTask customCommandsAsyncTask = new CustomCommandsAsyncTask(CustomCommandsAsyncTask.RESTOREDATA, customCommandsSQL);
		customCommandsAsyncTask.setListener(new CustomCommandsAsyncTask.CustomCommandsAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {

			}

			@Override
			public void onAsyncTaskFinished(List<CustomCommandsModel> customCommandsModelList) {
				updateCustomCommandsModelListFull(customCommandsModelList);
				getCustomCommandsModels().getValue().clear();
				getCustomCommandsModels().getValue().addAll(customCommandsModelList);
				getCustomCommandsModels().postValue(getCustomCommandsModels().getValue());
			}
		});
		customCommandsAsyncTask.execute(getInitCopyOfCustomCommandsModelListFull());
	}

	public void updateCustomCommandsModelListFull(List<CustomCommandsModel> copyOfCustomCommandsModelList){
		customCommandsModelListFull.clear();
		customCommandsModelListFull.addAll(copyOfCustomCommandsModelList);
	}

	private List<CustomCommandsModel> getInitCopyOfCustomCommandsModelListFull(){
		copyOfCustomCommandsModelListFull.clear();
		copyOfCustomCommandsModelListFull.addAll(customCommandsModelListFull);
		return copyOfCustomCommandsModelListFull;
	}

}
