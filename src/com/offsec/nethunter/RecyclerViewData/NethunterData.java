package com.offsec.nethunter.RecyclerViewData;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.offsec.nethunter.AsyncTask.NethunterAsynctask;
import com.offsec.nethunter.SQL.NethunterSQL;
import com.offsec.nethunter.models.NethunterModel;

import java.util.ArrayList;
import java.util.List;

/*
    A singleton repository class for processing the data of nethunter fragment

    Mainly passing a copy of current observed MutableLiveData List<NethunterModel>
    to process the data in background thread and update the MutableLiveData List<NethunterModel>
    by the returned AsyncTask result of the copy List<NethunterModel>.

    Do not pass the observed List<NethunterModel> to AsyncTask as parameter as it should always stay on the main thread.
    Instead you should pass a new copy ArrayList<NethunterModel> of the the observed List<NethunterModel>,
    and update the observed List<NethunterModel> with the returned AsyncTask result of the copy List<NethunterModel>
 */

public class NethunterData {
    private static NethunterData instance;
    public static boolean isDataInitiated = false;
    private ArrayList<NethunterModel> nethunterModelArrayList = new ArrayList<>();
    private MutableLiveData<List<NethunterModel>> data = new MutableLiveData<>();
    public List<NethunterModel> nethunterModelListFull;
    private List<NethunterModel> copyOfNethunterModelListFull = new ArrayList<>();

    public synchronized static NethunterData getInstance(){
        if (instance == null) {
            instance = new NethunterData();
        }
        return instance;
    }

    public MutableLiveData<List<NethunterModel>> getNethunterModels(Context context){
        if (!isDataInitiated) {
            data.setValue(NethunterSQL.getInstance(context).bindData(nethunterModelArrayList));
            nethunterModelListFull = new ArrayList<>(data.getValue());
            isDataInitiated = true;
        }
        return data;
    }

    public MutableLiveData<List<NethunterModel>> getNethunterModels(){
        return data;
    }

    public void refreshData(){
        NethunterAsynctask nethunterAsynctask = new NethunterAsynctask(NethunterAsynctask.GETITEMRESULTS);
        nethunterAsynctask.setListener(new NethunterAsynctask.NethunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<NethunterModel> nethunterModelList) {
                getNethunterModels().getValue().clear();
                getNethunterModels().getValue().addAll(nethunterModelList);
                getNethunterModels().postValue(getNethunterModels().getValue());
            }
        });
        nethunterAsynctask.execute(getInitCopyOfNethunterModelListFull());
    }

    public void runCommandforItem(int position){
        NethunterAsynctask nethunterAsynctask = new NethunterAsynctask(NethunterAsynctask.RUNCMDFORITEM, position);
        nethunterAsynctask.setListener(new NethunterAsynctask.NethunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<NethunterModel> nethunterModelList) {
                getNethunterModels().getValue().clear();
                getNethunterModels().getValue().addAll(nethunterModelList);
                getNethunterModels().postValue(getNethunterModels().getValue());
            }
        });
        nethunterAsynctask.execute(getInitCopyOfNethunterModelListFull());
    }

    public void editData(int position, ArrayList<String> dataArrayList, NethunterSQL nethunterSQL){
        NethunterAsynctask nethunterAsynctask = new NethunterAsynctask(NethunterAsynctask.EDITDATA, position, dataArrayList, nethunterSQL);
        nethunterAsynctask.setListener(new NethunterAsynctask.NethunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<NethunterModel> nethunterModelList) {
                updateNethunterModelListFull(nethunterModelList);
                getNethunterModels().getValue().clear();
                getNethunterModels().getValue().addAll(nethunterModelList);
                getNethunterModels().postValue(getNethunterModels().getValue());
            }
        });
        nethunterAsynctask.execute(getInitCopyOfNethunterModelListFull());
    }

    public void addData(int position, ArrayList<String> dataArrayList, NethunterSQL nethunterSQL){
        NethunterAsynctask nethunterAsynctask = new NethunterAsynctask(NethunterAsynctask.ADDDATA, position, dataArrayList, nethunterSQL);
        nethunterAsynctask.setListener(new NethunterAsynctask.NethunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<NethunterModel> nethunterModelList) {
                updateNethunterModelListFull(nethunterModelList);
                getNethunterModels().getValue().clear();
                getNethunterModels().getValue().addAll(nethunterModelList);
                getNethunterModels().postValue(getNethunterModels().getValue());
            }
        });
        nethunterAsynctask.execute(getInitCopyOfNethunterModelListFull());
    }

    public void deleteData(ArrayList<Integer> selectedPositionsIndex, ArrayList<Integer> selectedTargetIds, NethunterSQL nethunterSQL){
        NethunterAsynctask nethunterAsynctask = new NethunterAsynctask(NethunterAsynctask.DELETEDATA, selectedPositionsIndex, selectedTargetIds, nethunterSQL);
        nethunterAsynctask.setListener(new NethunterAsynctask.NethunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<NethunterModel> nethunterModelList) {
                updateNethunterModelListFull(nethunterModelList);
                getNethunterModels().getValue().clear();
                getNethunterModels().getValue().addAll(nethunterModelList);
                getNethunterModels().postValue(getNethunterModels().getValue());
            }
        });
        nethunterAsynctask.execute(getInitCopyOfNethunterModelListFull());
    }

    public void moveData(int originalPositionIndex, int targetPositionIndex, NethunterSQL nethunterSQL){
        NethunterAsynctask nethunterAsynctask = new NethunterAsynctask(NethunterAsynctask.MOVEDATA, originalPositionIndex, targetPositionIndex, nethunterSQL);
        nethunterAsynctask.setListener(new NethunterAsynctask.NethunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<NethunterModel> nethunterModelList) {
                updateNethunterModelListFull(nethunterModelList);
                getNethunterModels().getValue().clear();
                getNethunterModels().getValue().addAll(nethunterModelList);
                getNethunterModels().postValue(getNethunterModels().getValue());
            }
        });
        nethunterAsynctask.execute(getInitCopyOfNethunterModelListFull());
    }

    public String backupData(NethunterSQL nethunterSQL, String storedDBpath){
        return nethunterSQL.backupData(storedDBpath);
    }

    public String restoreData(NethunterSQL nethunterSQL, String storedDBpath){
        String returnedResult = nethunterSQL.restoreData(storedDBpath);
        if (returnedResult == null){
            NethunterAsynctask nethunterAsynctask = new NethunterAsynctask(NethunterAsynctask.RESTOREDATA, nethunterSQL);
            nethunterAsynctask.setListener(new NethunterAsynctask.NethunterAsynctaskListener() {
                @Override
                public void onAsyncTaskPrepare() {

                }

                @Override
                public void onAsyncTaskFinished(List<NethunterModel> nethunterModelList) {
                    updateNethunterModelListFull(nethunterModelList);
                    getNethunterModels().getValue().clear();
                    getNethunterModels().getValue().addAll(nethunterModelList);
                    getNethunterModels().postValue(getNethunterModels().getValue());
                    refreshData();
                }
            });
            nethunterAsynctask.execute(getInitCopyOfNethunterModelListFull());
            return null;
        } else {
            return returnedResult;
        }
    }

    public void resetData(NethunterSQL nethunterSQL){
        nethunterSQL.resetData();
        NethunterAsynctask nethunterAsynctask = new NethunterAsynctask(NethunterAsynctask.RESTOREDATA, nethunterSQL);
        nethunterAsynctask.setListener(new NethunterAsynctask.NethunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {

            }

            @Override
            public void onAsyncTaskFinished(List<NethunterModel> nethunterModelList) {
                updateNethunterModelListFull(nethunterModelList);
                getNethunterModels().getValue().clear();
                getNethunterModels().getValue().addAll(nethunterModelList);
                getNethunterModels().postValue(getNethunterModels().getValue());
                refreshData();
            }
        });
        nethunterAsynctask.execute(getInitCopyOfNethunterModelListFull());
    }

    public void updateNethunterModelListFull(List<NethunterModel> copyOfNethunterModelList){
        nethunterModelListFull.clear();
        nethunterModelListFull.addAll(copyOfNethunterModelList);
    }

    private List<NethunterModel> getInitCopyOfNethunterModelListFull(){
        copyOfNethunterModelListFull.clear();
        copyOfNethunterModelListFull.addAll(nethunterModelListFull);
        return copyOfNethunterModelListFull;
    }

}
