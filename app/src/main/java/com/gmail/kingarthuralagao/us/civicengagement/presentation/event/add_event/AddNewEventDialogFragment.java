package com.gmail.kingarthuralagao.us.civicengagement.presentation.event.add_event;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gmail.kingarthuralagao.us.civicengagement.data.model.accessibility.Accessibility;
import com.gmail.kingarthuralagao.us.civicengagement.presentation.event.add_event.adapter.AccessibilityCheckboxAdapter;
import com.gmail.kingarthuralagao.us.civilengagement.R;
import com.gmail.kingarthuralagao.us.civilengagement.databinding.DialogAddNewEventBinding;

import java.util.ArrayList;

public class AddNewEventDialogFragment extends DialogFragment {

    public static AddNewEventDialogFragment newInstance() {
        AddNewEventDialogFragment fragment = new AddNewEventDialogFragment();
        return fragment;
    }

    private DialogAddNewEventBinding binding;
    private AddNewEventNowFragment addNewEventNowFragment;
    private AddNewEventSoonFragment addNewEventSoonFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogAddNewEventBinding.inflate(getLayoutInflater());

        addNewEventNowFragment = AddNewEventNowFragment.newInstance();
        addNewEventSoonFragment = AddNewEventSoonFragment.newInstance();

        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_FullScreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getChildFragmentManager()
                .beginTransaction()
                .add(binding.fragmentContainer.getId(), addNewEventNowFragment)
                .add(binding.fragmentContainer.getId(), addNewEventSoonFragment)
                .hide(addNewEventSoonFragment)
                .commit();

        initializeRecyclerView();
        setUpEvents();
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setWindowAnimations(R.style.Theme_Slide);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);


    }

    @Override
    public void onDetach() {
        super.onDetach();


    }

    private void setUpEvents() {
        binding.backArrow.setOnClickListener(view -> dismiss());

        binding.includeHappeningNowHappeningSoon.happeningNowBtn.setOnClickListener(view -> {
            getChildFragmentManager()
                    .beginTransaction()
                    .hide(addNewEventSoonFragment)
                    .show(addNewEventNowFragment)
                    .commit();
            binding.includeHappeningNowHappeningSoon.happeningNowBtn.setEnabled(false);
            binding.includeHappeningNowHappeningSoon.happeningSoonBtn.setEnabled(true);
        });

        binding.includeHappeningNowHappeningSoon.happeningSoonBtn.setOnClickListener(view -> {
            getChildFragmentManager()
                    .beginTransaction()
                    .hide(addNewEventNowFragment)
                    .show(addNewEventSoonFragment)
                    .commit();
            binding.includeHappeningNowHappeningSoon.happeningNowBtn.setEnabled(true);
            binding.includeHappeningNowHappeningSoon.happeningSoonBtn.setEnabled(false);
        });
    }

    private void initializeRecyclerView() {
        ArrayList<Accessibility> accessibilityList = new ArrayList<>();
        accessibilityList.add(new Accessibility("Curb cuts for wheelchair", true));
        accessibilityList.add(new Accessibility("Medic station with supplies", true));
        accessibilityList.add(new Accessibility("Medic station with trained staff", false));
        accessibilityList.add(new Accessibility("Easy access to seating", false));

        AccessibilityCheckboxAdapter accessibilityCheckboxAdapter = new AccessibilityCheckboxAdapter(accessibilityList);

        binding.includeAccessibilityCheckboxes.accessibilitiesRv.setAdapter(accessibilityCheckboxAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.recyclerview_divider_gray, null));

        binding.includeAccessibilityCheckboxes.accessibilitiesRv.addItemDecoration(dividerItemDecoration);
        binding.includeAccessibilityCheckboxes.accessibilitiesRv.setLayoutManager(new LinearLayoutManager(requireContext()));
    }


    /****************************************** Code for making full screen Dialog *******************************/
    /*
    @NonNull
    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
                setupFullHeight(bottomSheetDialog);
            }
        });
        return  dialog;
    }


    private void setupFullHeight(BottomSheetDialog bottomSheetDialog) {
        FrameLayout bottomSheet = (FrameLayout) bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();

        int windowHeight = getWindowHeight();
        if (layoutParams != null) {
            layoutParams.height = windowHeight;
        }
        bottomSheet.setLayoutParams(layoutParams);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private int getWindowHeight() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }*/

    /****************************************** End of code for making full screen Dialog *******************************/
}