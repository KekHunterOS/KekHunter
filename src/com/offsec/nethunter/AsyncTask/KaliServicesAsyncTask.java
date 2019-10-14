package com.offsec.nethunter.AsyncTask;

import android.os.AsyncTask;

import com.offsec.nethunter.ChrootManagerFragment;
import com.offsec.nethunter.RecyclerViewData.KaliServicesData;
import com.offsec.nethunter.SQL.KaliServicesSQL;

import java.util.ArrayList;

public class KaliServicesAsyncTask extends AsyncTask<Object, Void, Void> {
	private KaliServiceTaskListener listener;
	private int actionCode;
	public static final int CHECK_SERVICE = 0;
	public static final int START_SERVICE = 1;
	public static final int STOP_SERVICE = 2;
	public static final int ADD_SERVICE = 3;
	public static final int RESET_SERVICE = 4;
	public static final int UPDATE_RUNONCHROOTSTART = 5;

	public KaliServicesAsyncTask(Integer ActionCode){
		this.actionCode = ActionCode;
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
	protected Void doInBackground(Object... objects) {
		switch (actionCode) {
			case CHECK_SERVICE: {
				((KaliServicesData) objects[0]).updateAllStatus();
				break;
			}
			case START_SERVICE: {
				((KaliServicesData) objects[0]).startService(((int) objects[1]));
				((KaliServicesData) objects[0]).updateAllStatus();
				break;
			}
			case STOP_SERVICE: {
				((KaliServicesData) objects[0]).stopService(((int) objects[1]));
				((KaliServicesData) objects[0]).updateAllStatus();
				break;
			}
			case ADD_SERVICE: {
				((KaliServicesData) objects[0]).addService(((int) objects[1]), ((ArrayList<String>) objects[2]));
				((KaliServicesData) objects[0]).updateAllStatus();
				break;
			}
			case RESET_SERVICE:{
				((KaliServicesSQL) objects[1]).resetData();
				((KaliServicesData) objects[0]).resetService(((KaliServicesSQL) objects[1]).getData());
				((KaliServicesData) objects[0]).updateAllStatus();
				break;
			}
			case UPDATE_RUNONCHROOTSTART: {
				((KaliServicesData) objects[0]).updateRunOnBootService();
				break;
			}
			default:
				break;
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void voids) {
		super.onPostExecute(voids);
		if (listener != null) {
			listener.onAsyncTaskFinished();
		}
		ChrootManagerFragment.isAsyncTaskRunning = false;
	}

	public void setListener(KaliServiceTaskListener listener) {
		this.listener = listener;
	}

	public interface KaliServiceTaskListener {
		void onAsyncTaskPrepare();
		void onAsyncTaskFinished();
	}
}
