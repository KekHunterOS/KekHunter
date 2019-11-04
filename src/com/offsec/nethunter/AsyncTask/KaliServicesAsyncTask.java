package com.offsec.nethunter.AsyncTask;

import android.os.AsyncTask;
import android.util.Log;

import com.offsec.nethunter.ChrootManagerFragment;
import com.offsec.nethunter.SQL.KaliServicesSQL;
import com.offsec.nethunter.models.KaliServicesModel;
import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KaliServicesAsyncTask extends AsyncTask<List<KaliServicesModel>, Void, List<KaliServicesModel>> {
	private KaliServicesAsyncTaskListener listener;
	private int actionCode;
	private int position;
	private int originalPositionIndex;
	private int targetPositionIndex;
	private ArrayList<Integer> selectedPositionsIndex;
	private ArrayList<Integer> selectedTargetIds;
	private ArrayList<String> dataArrayList;
	private KaliServicesSQL kaliServicesSQL;
	public static final int GETITEMSTATUS = 0;
	public static final int START_SERVICE_FOR_ITEM = 1;
	public static final int STOP_SERVICE_FOR_ITEM = 2;
	public static final int EDITDATA = 3;
	public static final int ADDDATA = 4;
	public static final int DELETEDATA = 5;
	public static final int MOVEDATA = 6;
	public static final int BACKUPDATA = 7;
	public static final int RESTOREDATA = 8;
	public static final int RESETDATA = 9;
	public static final int UPDATE_RUNONCHROOTSTART_SCRIPTS = 10;

	public KaliServicesAsyncTask (int actionCode){
		this.actionCode = actionCode;
	}

	public KaliServicesAsyncTask (int actionCode, int position){
		this.actionCode = actionCode;
		this.position = position;
	}

	public KaliServicesAsyncTask (int actionCode, int position, ArrayList<String> dataArrayList, KaliServicesSQL kaliServicesSQL){
		this.actionCode = actionCode;
		this.position = position;
		this.dataArrayList = dataArrayList;
		this.kaliServicesSQL = kaliServicesSQL;
	}

	public KaliServicesAsyncTask (int actionCode, ArrayList<Integer> selectedPositionsIndex, ArrayList<Integer> selectedTargetIds, KaliServicesSQL kaliServicesSQL){
		this.actionCode = actionCode;
		this.selectedPositionsIndex = selectedPositionsIndex;
		this.selectedTargetIds = selectedTargetIds;
		this.kaliServicesSQL = kaliServicesSQL;
	}

	public KaliServicesAsyncTask (int actionCode, int originalPositionIndex, int targetPositionIndex, KaliServicesSQL kaliServicesSQL){
		this.actionCode = actionCode;
		this.originalPositionIndex = originalPositionIndex;
		this.targetPositionIndex = targetPositionIndex;
		this.kaliServicesSQL = kaliServicesSQL;
	}

	public KaliServicesAsyncTask (int actionCode, KaliServicesSQL kaliServicesSQL){
		this.actionCode = actionCode;
		this.kaliServicesSQL = kaliServicesSQL;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		ChrootManagerFragment.isAsyncTaskRunning = true;
		if (listener != null) {
			listener.onAsyncTaskPrepare();
		}
	}

	@Override
	protected List<KaliServicesModel> doInBackground(List<KaliServicesModel>... copyOfkaliServicesModelList) {
		List<KaliServicesModel> kaliServicesModelList;
		switch (actionCode) {
			case GETITEMSTATUS:
				kaliServicesModelList = copyOfkaliServicesModelList[0];
				if (kaliServicesModelList != null){
					for (int i = 0; i < kaliServicesModelList.size(); i++) {
						kaliServicesModelList.get(i).setStatus(new ShellExecuter().RunAsRootReturnValue(NhPaths.BUSYBOX + " ps -o pid,comm | grep '" + kaliServicesModelList.get(i).getCommandforCheckServiceStatus() + "'") == 0?"[+] Service is running":"[-] Service is NOT running");
					}
				}
				break;
			case START_SERVICE_FOR_ITEM:
				kaliServicesModelList = copyOfkaliServicesModelList[0];
				if (kaliServicesModelList != null){
					kaliServicesModelList.get(position).setStatus(new ShellExecuter().RunAsChrootReturnValue(kaliServicesModelList.get(position).getCommandforStartService()) == 0?"[+] Service is running":"[-] Service is NOT running");
				}
				break;
			case STOP_SERVICE_FOR_ITEM:
				kaliServicesModelList = copyOfkaliServicesModelList[0];
				if (kaliServicesModelList != null){
					kaliServicesModelList.get(position).setStatus(new ShellExecuter().RunAsChrootReturnValue(kaliServicesModelList.get(position).getCommandforStopService()) == 0?"[-] Service is NOT running":"[+] Service is running");
				}
				break;
			case EDITDATA:
				kaliServicesModelList = copyOfkaliServicesModelList[0];
				if (kaliServicesModelList != null){
					kaliServicesModelList.get(position).setServiceName(dataArrayList.get(0));
					kaliServicesModelList.get(position).setCommandforStartService(dataArrayList.get(1));
					kaliServicesModelList.get(position).setCommandforStopService(dataArrayList.get(2));
					kaliServicesModelList.get(position).setCommandforCheckServiceStatus(dataArrayList.get(3));
					kaliServicesModelList.get(position).setRunOnChrootStart(dataArrayList.get(4));
					updateRunOnChrootStartScripts(kaliServicesModelList);
					kaliServicesSQL.editData(position, dataArrayList);
				}
				break;
			case ADDDATA:
				kaliServicesModelList = copyOfkaliServicesModelList[0];
				if (kaliServicesModelList != null){

					kaliServicesModelList.add(position - 1, new KaliServicesModel(
							dataArrayList.get(0),
							dataArrayList.get(1),
							dataArrayList.get(2),
							dataArrayList.get(3),
							dataArrayList.get(4),
							""));
					if (dataArrayList.get(4).equals("1")){
						updateRunOnChrootStartScripts(kaliServicesModelList);
					}
					kaliServicesSQL.addData(position, dataArrayList);
				}
				break;
			case DELETEDATA:
				kaliServicesModelList = copyOfkaliServicesModelList[0];
				if (kaliServicesModelList != null){
					Collections.sort(selectedPositionsIndex, Collections.<Integer>reverseOrder());
					for (Integer selectedPosition: selectedPositionsIndex) {
						int i = selectedPosition;
						kaliServicesModelList.remove(i);
					}
					kaliServicesSQL.deleteData(selectedTargetIds);
				}
				break;
			case MOVEDATA:
				kaliServicesModelList = copyOfkaliServicesModelList[0];
				if (kaliServicesModelList != null){
					KaliServicesModel tempKaliServicesModel = new KaliServicesModel(
							kaliServicesModelList.get(originalPositionIndex).getServiceName(),
							kaliServicesModelList.get(originalPositionIndex).getCommandforStartService(),
							kaliServicesModelList.get(originalPositionIndex).getCommandforStopService(),
							kaliServicesModelList.get(originalPositionIndex).getCommandforCheckServiceStatus(),
							kaliServicesModelList.get(originalPositionIndex).getRunOnChrootStart(),
							kaliServicesModelList.get(originalPositionIndex).getStatus()
					);
					kaliServicesModelList.remove(originalPositionIndex);
					if (originalPositionIndex < targetPositionIndex) {
						targetPositionIndex = targetPositionIndex - 1;
					}
					kaliServicesModelList.add(targetPositionIndex, tempKaliServicesModel);
					kaliServicesSQL.moveData(originalPositionIndex, targetPositionIndex);
				}
				break;
			case BACKUPDATA:
				break;
			case RESTOREDATA:
				kaliServicesModelList = copyOfkaliServicesModelList[0];
				if (kaliServicesModelList != null) {
					kaliServicesModelList.clear();
					kaliServicesModelList = kaliServicesSQL.bindData((ArrayList<KaliServicesModel>)kaliServicesModelList);
				}
				break;
			case RESETDATA:
				break;
			case UPDATE_RUNONCHROOTSTART_SCRIPTS:
				kaliServicesModelList = copyOfkaliServicesModelList[0];
				if (kaliServicesModelList != null){
					kaliServicesModelList.get(position).setServiceName(dataArrayList.get(0));
					kaliServicesModelList.get(position).setCommandforStartService(dataArrayList.get(1));
					kaliServicesModelList.get(position).setCommandforStopService(dataArrayList.get(2));
					kaliServicesModelList.get(position).setCommandforCheckServiceStatus(dataArrayList.get(3));
					kaliServicesModelList.get(position).setRunOnChrootStart(dataArrayList.get(4));
					kaliServicesSQL.editData(position, dataArrayList);
					updateRunOnChrootStartScripts(kaliServicesModelList);
				}
				break;
		}
		return copyOfkaliServicesModelList[0];
	}

	@Override
	protected void onPostExecute(List<KaliServicesModel> kaliServicesModelList) {
		super.onPostExecute(kaliServicesModelList);
		if (listener != null) {
			listener.onAsyncTaskFinished(kaliServicesModelList);
		}
		ChrootManagerFragment.isAsyncTaskRunning = false;
	}

	public void setListener(KaliServicesAsyncTaskListener listener) {
		this.listener = listener;
	}

	public interface KaliServicesAsyncTaskListener {
		void onAsyncTaskPrepare();
		void onAsyncTaskFinished(List<KaliServicesModel> kaliServicesModelList);
	}

	private void updateRunOnChrootStartScripts (List<KaliServicesModel> kaliServicesModelList) {
		StringBuilder tmpStringBuilder = new StringBuilder();
		for (int i = 0; i < kaliServicesModelList.size(); i++) {
			if (kaliServicesModelList.get(i).getRunOnChrootStart().equals("1")) {
				tmpStringBuilder.append(kaliServicesModelList.get(i).getCommandforStartService() + "\\n");
			}
		}
		new ShellExecuter().RunAsRootOutput("echo \"" + tmpStringBuilder.toString() + "\" > " + NhPaths.APP_SCRIPTS_PATH + "/kaliservices");
	}
}
