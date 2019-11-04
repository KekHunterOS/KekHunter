package com.offsec.nethunter.AsyncTask;

import android.os.AsyncTask;

import com.offsec.nethunter.RecyclerViewData.NethunterData;
import com.offsec.nethunter.SQL.NethunterSQL;
import com.offsec.nethunter.models.NethunterModel;
import com.offsec.nethunter.utils.ShellExecuter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NethunterAsynctask extends AsyncTask<List<NethunterModel>, Void, List<NethunterModel>> {

    private NethunterAsynctaskListener listener;
    private int actionCode;
    private int position;
    private int originalPositionIndex;
    private int targetPositionIndex;
    private ArrayList<Integer> selectedPositionsIndex;
    private ArrayList<Integer> selectedTargetIds;
    private ArrayList<String> dataArrayList;
    private NethunterSQL nethunterSQL;
    public static final int GETITEMRESULTS = 0;
    public static final int RUNCMDFORITEM = 1;
    public static final int EDITDATA = 2;
    public static final int ADDDATA = 3;
    public static final int DELETEDATA = 4;
    public static final int MOVEDATA = 5;
    public static final int BACKUPDATA = 6;
    public static final int RESTOREDATA = 7;
    public static final int RESETDATA = 8;

    private List<NethunterModel> nethunterModelList;

    public NethunterAsynctask (int actionCode){
        this.actionCode = actionCode;
    }

    public NethunterAsynctask (int actionCode, int position){
        this.actionCode = actionCode;
        this.position = position;
    }

    public NethunterAsynctask (int actionCode, int position, ArrayList<String> dataArrayList, NethunterSQL nethunterSQL){
        this.actionCode = actionCode;
        this.position = position;
        this.dataArrayList = dataArrayList;
        this.nethunterSQL = nethunterSQL;
    }

    public NethunterAsynctask (int actionCode, ArrayList<Integer> selectedPositionsIndex, ArrayList<Integer> selectedTargetIds, NethunterSQL nethunterSQL){
        this.actionCode = actionCode;
        this.selectedPositionsIndex = selectedPositionsIndex;
        this.selectedTargetIds = selectedTargetIds;
        this.nethunterSQL = nethunterSQL;
    }

    public NethunterAsynctask (int actionCode, int originalPositionIndex, int targetPositionIndex, NethunterSQL nethunterSQL){
        this.actionCode = actionCode;
        this.originalPositionIndex = originalPositionIndex;
        this.targetPositionIndex = targetPositionIndex;
        this.nethunterSQL = nethunterSQL;
    }

    public NethunterAsynctask (int actionCode, NethunterSQL nethunterSQL){
        this.actionCode = actionCode;
        this.nethunterSQL = nethunterSQL;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (listener != null) {
            listener.onAsyncTaskPrepare();
        }
    }

    @Override
    protected List<NethunterModel> doInBackground(List<NethunterModel>... copyOfnethunterModelList) {
        switch (actionCode) {
            case GETITEMRESULTS:
                nethunterModelList = copyOfnethunterModelList[0];
                if (nethunterModelList != null){
                    for (int i = 0; i < nethunterModelList.size(); i++){
                        nethunterModelList.get(i).setResult(nethunterModelList.get(i).getRunOnCreate().equals("1")?new ShellExecuter().RunAsRootOutput(nethunterModelList.get(i).getCommand()).split("\\n"):"Please click RUN button manually.".split("\\n"));
                    }
                }
                break;
            case RUNCMDFORITEM:
                nethunterModelList = copyOfnethunterModelList[0];
                if (nethunterModelList != null){
                    nethunterModelList.get(position).setResult(new ShellExecuter().RunAsRootOutput(nethunterModelList.get(position).getCommand()).split("\\n"));
                }
                break;
            case EDITDATA:
                nethunterModelList = copyOfnethunterModelList[0];
                if (nethunterModelList != null){
                    nethunterModelList.get(position).setTitle(dataArrayList.get(0));
                    nethunterModelList.get(position).setCommand(dataArrayList.get(1));
                    nethunterModelList.get(position).setDelimiter(dataArrayList.get(2));
                    nethunterModelList.get(position).setRunOnCreate(dataArrayList.get(3));
                    if (dataArrayList.get(3).equals("1")){
                        nethunterModelList.get(position).setResult(new ShellExecuter().RunAsRootOutput(dataArrayList.get(1)).split(dataArrayList.get(2)));
                    }
                    nethunterSQL.editData(position, dataArrayList);
                }
                break;
            case ADDDATA:
                nethunterModelList = copyOfnethunterModelList[0];
                if (nethunterModelList != null){

                    nethunterModelList.add(position - 1, new NethunterModel(
                            dataArrayList.get(0),
                            dataArrayList.get(1),
                            dataArrayList.get(2),
                            dataArrayList.get(3),
                            "".split(dataArrayList.get(2))));
                    if (dataArrayList.get(3).equals("1")){
                        nethunterModelList.get(position - 1).setResult(new ShellExecuter().RunAsRootOutput(dataArrayList.get(1)).split(dataArrayList.get(2)));
                    }
                    nethunterSQL.addData(position, dataArrayList);
                }
                break;
            case DELETEDATA:
                nethunterModelList = copyOfnethunterModelList[0];
                if (nethunterModelList != null){
                    Collections.sort(selectedPositionsIndex, Collections.<Integer>reverseOrder());
                    for (Integer selectedPosition: selectedPositionsIndex) {
                        int i = selectedPosition;
                        nethunterModelList.remove(i);
                    }
                    nethunterSQL.deleteData(selectedTargetIds);
                }
                break;
            case MOVEDATA:
                nethunterModelList = copyOfnethunterModelList[0];
                if (nethunterModelList != null){
                    NethunterModel tempNethunterModel = new NethunterModel(
                            nethunterModelList.get(originalPositionIndex).getTitle(),
                            nethunterModelList.get(originalPositionIndex).getCommand(),
                            nethunterModelList.get(originalPositionIndex).getDelimiter(),
                            nethunterModelList.get(originalPositionIndex).getRunOnCreate(),
                            nethunterModelList.get(originalPositionIndex).getResult()
                    );
                    nethunterModelList.remove(originalPositionIndex);
                    if (originalPositionIndex < targetPositionIndex) {
                        targetPositionIndex = targetPositionIndex - 1;
                    }
                    nethunterModelList.add(targetPositionIndex, tempNethunterModel);
                    nethunterSQL.moveData(originalPositionIndex, targetPositionIndex);
                }
                break;
            case BACKUPDATA:
                break;
            case RESTOREDATA:
                nethunterModelList = copyOfnethunterModelList[0];
                if (nethunterModelList != null) {
                    nethunterModelList.clear();
                    nethunterModelList = nethunterSQL.bindData((ArrayList<NethunterModel>)nethunterModelList);
                }
                break;
            case RESETDATA:
                break;
        }
        return copyOfnethunterModelList[0];
    }

    @Override
    protected void onPostExecute(List<NethunterModel> nethunterModelList) {
        super.onPostExecute(nethunterModelList);
        if (listener != null) {
            listener.onAsyncTaskFinished(nethunterModelList);
        }
    }

    public void setListener(NethunterAsynctaskListener listener) {
        this.listener = listener;
    }

    public interface NethunterAsynctaskListener {
        void onAsyncTaskPrepare();
        void onAsyncTaskFinished(List<NethunterModel> nethunterModelList);
    }
}
