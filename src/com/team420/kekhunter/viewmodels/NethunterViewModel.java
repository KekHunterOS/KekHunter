package com.team420.kekhunter.viewmodels;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.team420.kekhunter.RecyclerViewData.NethunterData;
import com.team420.kekhunter.models.NethunterModel;

import java.util.List;

/*
    ViewModel class for nethunter model, this is to be observed the List of NethunterModel class.
    This class should be initiated every time the NethunterFragment is created.
    After the NethunterData singleton is created, it will live until the app dies.
 */
public class NethunterViewModel extends ViewModel {
  private MutableLiveData<List<NethunterModel>> mutableLiveDataNethunterModelList;

  public void init(Context context) {
    if (mutableLiveDataNethunterModelList != null) {
      return;
    }
    NethunterData nethunterData = NethunterData.getInstance();
    if (NethunterData.isDataInitiated) {
      mutableLiveDataNethunterModelList = nethunterData.getNethunterModels();
    } else {
      mutableLiveDataNethunterModelList = nethunterData.getNethunterModels(context);
    }
  }

  public LiveData<List<NethunterModel>> getLiveDataNethunterModelList() {
    return mutableLiveDataNethunterModelList;
  }
}
