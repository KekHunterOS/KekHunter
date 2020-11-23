package com.offsec.nethunter.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.offsec.nethunter.RecyclerViewData.KaliServicesData;
import com.offsec.nethunter.RecyclerViewData.NethunterData;
import com.offsec.nethunter.models.KaliServicesModel;
import com.offsec.nethunter.models.NethunterModel;

import java.util.List;

/*
    ViewModel class for kaliservices model, this is to be observed the List of KaliServicesModel class.
    This class should be initiated every time the KaliServicesFragment is created.
    After the KaliServicesData singleton is created, it will live until the app dies.
 */
public class KaliServicesViewModel extends ViewModel {
    private MutableLiveData<List<KaliServicesModel>> mutableLiveDataKaliServicesModelList;

    public void init(Context context){
        if (mutableLiveDataKaliServicesModelList != null){
            return;
        }
        KaliServicesData kaliServicesData = KaliServicesData.getInstance();
        if (KaliServicesData.isDataInitiated) {
            mutableLiveDataKaliServicesModelList = kaliServicesData.getKaliServicesModels();
        } else {
            mutableLiveDataKaliServicesModelList = kaliServicesData.getKaliServicesModels(context);
        }
    }

    public LiveData<List<KaliServicesModel>> getLiveDataKaliServicesModelList(){
        return mutableLiveDataKaliServicesModelList;
    }
}
