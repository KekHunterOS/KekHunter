package com.offsec.nethunter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.offsec.nethunter.RecyclerViewAdapter.CustomCommandsRecycleViewAdapter;
import com.offsec.nethunter.SQL.CustomCommandsSQL;
import com.offsec.nethunter.viewmodels.CustomCommandsViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public class CustomCommandsFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "CustomCommandsFragment";
    private CustomCommandsRecycleViewAdapter customCommandsRecycleViewAdapterTitles;
    private Context context;
    private static int targetPositionId;

    public CustomCommandsFragment() {

    }

    public static CustomCommandsFragment newInstance(int sectionNumber) {
        CustomCommandsFragment fragment = new CustomCommandsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getContext();
        setHasOptionsMenu(true);
        CustomCommandsSQL.getInstance(getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {//this runs BEFORE the ui is available
        return inflater.inflate(R.layout.customcommands, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        CustomCommandsViewModel customCommandsViewModel = ViewModelProviders.of(this).get(CustomCommandsViewModel.class);
        customCommandsViewModel.init(context);
        //customCommandsViewModel.getLiveDataCustomCommandsModelList().observe(this, customCommandsModelList -> customCommandsRecycleViewAdapterTitles.notifyDataSetChanged());

        /*
        recyclerViewServiceTitle = view.findViewById(R.id.f_kaliservices_recycleviewServiceTitle);
        customCommandsRecycleViewAdapterTitles = new KaliServiceRecycleViewAdapterTitles(context, customCommandsViewModel.getLiveDataCustomCommandsModelList().getValue());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerViewServiceTitle.setLayoutManager(linearLayoutManager);
        recyclerViewServiceTitle.setAdapter(customCommandsRecycleViewAdapterTitles);


         */
    }
}
