package com.offsec.nethunter.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.offsec.nethunter.RecyclerViewData.CustomCommandsData;
import com.offsec.nethunter.models.CustomCommandsModel;

import java.util.List;

/*
    ViewModel class for CustomCommands model, this is to be observed the List of CustomCommandsModel class.
    This class should be initiated every time the CustomCommandsFragment is created.
    After the CustomCommandsData singleton is created, it will live until the app dies.
 */
public class CustomCommandsViewModel extends ViewModel {
    private MutableLiveData<List<CustomCommandsModel>> mutableLiveDataCustomCommandsModelList;

    public void init(Context context){
        if (mutableLiveDataCustomCommandsModelList != null){
            return;
        }
        CustomCommandsData customCommandsData = CustomCommandsData.getInstance();
        if (CustomCommandsData.isDataInitiated) {
            mutableLiveDataCustomCommandsModelList = customCommandsData.getCustomCommandsModels();
        } else {
            mutableLiveDataCustomCommandsModelList = customCommandsData.getCustomCommandsModels(context);
        }
    }

    public LiveData<List<CustomCommandsModel>> getLiveDataCustomCommandsModelList(){
        return mutableLiveDataCustomCommandsModelList;
    }
}
