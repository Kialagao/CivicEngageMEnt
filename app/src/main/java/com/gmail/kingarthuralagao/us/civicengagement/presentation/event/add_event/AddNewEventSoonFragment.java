package com.gmail.kingarthuralagao.us.civicengagement.presentation.event.add_event;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gmail.kingarthuralagao.us.civilengagement.databinding.IncludeAddEventHappeningSoonBinding;

public class AddNewEventSoonFragment extends Fragment {

    public static AddNewEventSoonFragment newInstance() {
        AddNewEventSoonFragment fragment = new AddNewEventSoonFragment();
        return fragment;
    }

    private IncludeAddEventHappeningSoonBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = IncludeAddEventHappeningSoonBinding.inflate(getLayoutInflater());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}