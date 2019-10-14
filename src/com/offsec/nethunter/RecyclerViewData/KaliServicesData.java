package com.offsec.nethunter.RecyclerViewData;

import android.content.Context;
import android.util.Log;

import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.io.File;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;

public class KaliServicesData {
	private final ShellExecuter exe = new ShellExecuter();
	private ArrayList<String> Id;
	public ArrayList<String> ServiceName;
	public ArrayList<String> Status = new ArrayList<>();
	public ArrayList<Boolean> isChecked = new ArrayList<>();
	public ArrayList<String> CommandforStartService;
	public ArrayList<String> CommandforStopService;
	public ArrayList<String> CommandforCheckServiceStatus;
	public ArrayList<String> RunOnChrootStart;

	public KaliServicesData(Context context, ArrayList<ArrayList<String>> Data){
		this.Id = Data.get(0);
		this.ServiceName = Data.get(1);
		this.CommandforStartService = Data.get(2);
		this.CommandforStopService = Data.get(3);
		this.CommandforCheckServiceStatus = Data.get(4);
		this.RunOnChrootStart = Data.get(5);
		for (int i = 0; i < ServiceName.size(); i++) {
			this.Status.add("Service is NOT running");
			this.isChecked.add(false);
		}
	}

	public void updateAllStatus(){
		int returnValue = 0;
        updateRunOnBootService();

		returnValue = exe.RunAsRootReturnValue(NhPaths.APP_SCRIPTS_PATH + "/chrootmgr -c status -p " + NhPaths.CHROOT_PATH());
		if ( returnValue != 0 ) {
			for (int i = 0; i < ServiceName.size(); i++) {
				Status.set(i, "[!] Chroot is NOT being mounted");
			}
			return;
		}
		for (int i = 0; i < ServiceName.size(); i++) {
			returnValue = exe.RunAsRootReturnValue(NhPaths.BUSYBOX + " ps -o pid,comm | grep '" + CommandforCheckServiceStatus.get(i) + "'");
			if ( returnValue == 0){
				Status.set(i, "[+] Service is RUNNING");
				isChecked.set(i, true);
			} else {
				Status.set(i, "[-] Service is NOT running");
				isChecked.set(i, false);
			}
		}
	}

	public void editResult(int targetPositionIndex, ArrayList<String> editData){
		ServiceName.set(targetPositionIndex, editData.get(0));
		CommandforStartService.set(targetPositionIndex, editData.get(1));
		CommandforStopService.set(targetPositionIndex, editData.get(2));
		CommandforCheckServiceStatus.set(targetPositionIndex, editData.get(3));
		RunOnChrootStart.set(targetPositionIndex, editData.get(4));
	}

	public void startService(int targetPositionIndex){
		/*if (!checkChrootExec()){
			Status.set(targetPositionIndex, "[!] Please run 'apt install sudo' inside chroot to install sudo first.");
			isChecked.set(targetPositionIndex,false);
			return;
		}*/
		if (exe.RunAsChrootReturnValue((CommandforStartService.get(targetPositionIndex))) != 0) {
			Status.set(targetPositionIndex, "[-] Somethings wrong after executing the command");
			isChecked.set(targetPositionIndex,false);
			return;
		}
		if ( exe.RunAsRootReturnValue(CommandforCheckServiceStatus.get(targetPositionIndex)) == 0){
			Status.set(targetPositionIndex, "[+] Service is RUNNING");
			isChecked.set(targetPositionIndex, true);
		} else {
			Status.set(targetPositionIndex, "[-] Service is NOT running");
			isChecked.set(targetPositionIndex, false);
		}
	}

	public void stopService(int targetPositionIndex){
		/*if (!checkChrootExec()){
			Status.set(targetPositionIndex, "[!] Please run 'apt install sudo' inside chroot to install sudo first.");
			isChecked.set(targetPositionIndex,false);
			return;
		}*/
		int returnValue = exe.RunAsChrootReturnValue(CommandforStopService.get(targetPositionIndex));
		if (returnValue != 0) {
			Status.set(targetPositionIndex, "[-] Somethings wrong after executing the command");
			isChecked.set(targetPositionIndex,false);
			return;
		}
		returnValue = exe.RunAsRootReturnValue(CommandforCheckServiceStatus.get(targetPositionIndex));
		if ( returnValue == 0){
			Status.set(targetPositionIndex, "[+] Service is RUNNING");
			isChecked.set(targetPositionIndex, true);
		} else {
			Status.set(targetPositionIndex, "[-] Service is NOT running");
			isChecked.set(targetPositionIndex, false);
		}
	}

	public void addService(int targetPositionIndex, ArrayList<String> data) {
		Id.add(targetPositionIndex, data.get(0));
		ServiceName.add(targetPositionIndex, data.get(1));
		CommandforStartService.add(targetPositionIndex, data.get(2));
		CommandforStopService.add(targetPositionIndex, data.get(3));
		CommandforCheckServiceStatus.add(targetPositionIndex, data.get(4));
		RunOnChrootStart.add(targetPositionIndex, data.get(5));
		Status.add(targetPositionIndex, "Service is NOT running");
		isChecked.add(targetPositionIndex, false);
	}

	public void deleteService(ArrayList<Integer> selectedPositionsIndex){
		Collections.sort(selectedPositionsIndex, Collections.<Integer>reverseOrder());
		//Then remove the element start from the largest position number; Because the once we remove one element from arraylist, it will immediately update all index number right after the deleted index number.
		for (Integer selectedPosition: selectedPositionsIndex) {
			int i = selectedPosition;
			Id.remove(i);
			ServiceName.remove(i);
			CommandforStartService.remove(i);
			CommandforStopService.remove(i);
			CommandforCheckServiceStatus.remove(i);
			RunOnChrootStart.remove(i);
			Status.remove(i);
			isChecked.remove(i);
		}
	}

	public void moveService(int originalPositionIndex, Integer targetPositionIndex) {
		//First, store the original data to tempData;
		ArrayList<String> tempData = new ArrayList<>();
		ArrayList<Boolean> tempIsChecked = new ArrayList<>();

		tempData.add(Id.get(originalPositionIndex));
		tempData.add(ServiceName.get(originalPositionIndex));
		tempData.add(CommandforStartService.get(originalPositionIndex));
		tempData.add(CommandforStopService.get(originalPositionIndex));
		tempData.add(CommandforCheckServiceStatus.get(originalPositionIndex));
		tempData.add(RunOnChrootStart.get(originalPositionIndex));
		tempData.add(Status.get(originalPositionIndex));
		tempIsChecked.add(isChecked.get(originalPositionIndex));

		//Second, remove it;
		Id.remove(originalPositionIndex);
		ServiceName.remove(originalPositionIndex);
		CommandforStartService.remove(originalPositionIndex);
		CommandforStopService.remove(originalPositionIndex);
		CommandforCheckServiceStatus.remove(originalPositionIndex);
		RunOnChrootStart.remove(originalPositionIndex);
		Status.remove(originalPositionIndex);
		isChecked.remove(originalPositionIndex);
		//Lastly, add the tempData to the target index.
		if (originalPositionIndex < targetPositionIndex) targetPositionIndex = targetPositionIndex - 1;
		Id.add(targetPositionIndex, tempData.get(0));
		ServiceName.add(targetPositionIndex, tempData.get(1));
		CommandforStartService.add(targetPositionIndex, tempData.get(2));
		CommandforStopService.add(targetPositionIndex, tempData.get(3));
		CommandforCheckServiceStatus.add(targetPositionIndex, tempData.get(4));
		RunOnChrootStart.add(targetPositionIndex, tempData.get(5));
		Status.add(targetPositionIndex, tempData.get(6));
		isChecked.add(targetPositionIndex, tempIsChecked.get(0));
	}

	public void resetService(ArrayList<ArrayList<String>> Data){
		Id.clear();
		ServiceName.clear();
		CommandforStartService.clear();
		CommandforStopService.clear();
		CommandforCheckServiceStatus.clear();
		RunOnChrootStart.clear();
		Status.clear();
		isChecked.clear();
		this.Id = Data.get(0);
		this.ServiceName = Data.get(1);
		this.CommandforStartService = Data.get(2);
		this.CommandforStopService = Data.get(3);
		this.CommandforCheckServiceStatus = Data.get(4);
		this.RunOnChrootStart = Data.get(5);
		for (int i = 0; i < ServiceName.size(); i++) {
			this.Status.add("[-] Service is NOT running");
			this.isChecked.add(false);
		}
	}

	public ArrayList<String> dataToStringArray(int targetPositionIndex){
		ArrayList<String> tempArrayListString = new ArrayList<>();
		tempArrayListString.add(ServiceName.get(targetPositionIndex));
		tempArrayListString.add(CommandforStartService.get(targetPositionIndex));
		tempArrayListString.add(CommandforStopService.get(targetPositionIndex));
		tempArrayListString.add(CommandforCheckServiceStatus.get(targetPositionIndex));
		tempArrayListString.add(RunOnChrootStart.get(targetPositionIndex));
		return tempArrayListString;
	}

	public void updateRunOnBootService(){
        StringBuilder tmpStringBuilder = new StringBuilder();
        for (int i = 0; i < ServiceName.size(); i++) {
            if (RunOnChrootStart.get(i).equals("1")) {
                tmpStringBuilder.append(CommandforStartService.get(i) + "\\n");
            }
        }
        exe.RunAsRootOutput("echo \"" + tmpStringBuilder.toString() + "\" > " + NhPaths.APP_SCRIPTS_PATH + "/kaliservices");
    }

	/*private Boolean checkChrootExec(){
		return new File(NhPaths.CHROOT_PATH() + NhPaths.CHROOT_BASH).exists();
	}*/
}
