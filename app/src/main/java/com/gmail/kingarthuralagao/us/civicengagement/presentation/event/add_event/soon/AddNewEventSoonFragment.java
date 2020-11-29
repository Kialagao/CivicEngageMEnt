package com.gmail.kingarthuralagao.us.civicengagement.presentation.event.add_event.soon;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import com.gmail.kingarthuralagao.us.civicengagement.CivicEngagementApp;
import com.gmail.kingarthuralagao.us.civicengagement.core.utils.Utils;
import com.gmail.kingarthuralagao.us.civicengagement.data.model.event.Event;
import com.gmail.kingarthuralagao.us.civicengagement.data.model.event.EventBuilder;
import com.gmail.kingarthuralagao.us.civicengagement.data.model.timezone.TimeZone;
import com.gmail.kingarthuralagao.us.civicengagement.presentation.event.add_event.CausesDialogFragment;
import com.gmail.kingarthuralagao.us.civicengagement.presentation.event.add_event.RangeTimePickerDialogFragment;
import com.gmail.kingarthuralagao.us.civilengagement.BuildConfig;
import com.gmail.kingarthuralagao.us.civilengagement.databinding.IncludeAddEventHappeningSoonBinding;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class AddNewEventSoonFragment extends Fragment implements RangeTimePickerDialogFragment.ITimePickerListener {

    public static AddNewEventSoonFragment newInstance() {
        AddNewEventSoonFragment fragment = new AddNewEventSoonFragment();
        return fragment;
    }

    private static IncludeAddEventHappeningSoonBinding binding;
    private static final int AUTOCOMPLETE_LOCATION_REQUEST_CODE = 100;
    private static final String TAG = "AddNewEventSoonFragment";

    // Params for an Event object
    private static Long dateStart;
    private static Long dateEnd;
    private static String timeStart;
    private static String timeEnd;
    private static Place place;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = IncludeAddEventHappeningSoonBinding.inflate(inflater, container, false);
        setUpViews();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpEvents();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_LOCATION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                this.place = place;
                String address = place.getAddress();
                String name = place.getName();
                binding.eventLocationEt.setText(name + "\n" + address);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toasty.error(requireContext(), status.getStatusMessage(), Toast.LENGTH_LONG, true);
            } else if (resultCode == RESULT_CANCELED) {
                Log.i(TAG, "Cancelled");
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onTimeRangeSet(int startHour, int startMinute, int endHour, int endMinute) {
        Log.i(TAG, startHour + ":" + startMinute + " " + endHour + ":" + endMinute);

        String startTime = Utils.buildTimeString(startHour, startMinute);
        String endTime = Utils.buildTimeString(endHour, endMinute);

        binding.eventTimeStartEt.setText(startTime);
        binding.eventTimeEndEt.setText(endTime);

        this.timeStart = startTime;
        this.timeEnd = endTime;
    }

    private void setUpEvents() {
        binding.eventDateEndEt.setOnClickListener(view -> {
            hideKeyboard(binding.eventDateEndEt);
            showDatePickerDialog();
        });

        binding.eventDateStartEt.setOnClickListener(view -> {
            hideKeyboard(binding.eventDateEndEt);
            showDatePickerDialog();
        });

        binding.eventLocationEt.setOnClickListener( view -> {
            hideKeyboard(binding.eventLocationEt);
            initializeLocationSearch();
        });

        binding.eventTimeEndEt.setOnClickListener( view -> {
            hideKeyboard(binding.eventTimeEndEt);
            showTimePickerDialog();
        });

        binding.eventTimeStartEt.setOnClickListener( view -> {
            hideKeyboard(binding.eventTimeEndEt);
            showTimePickerDialog();
        });

        binding.lay.setOnClickListener(view -> hideKeyboard(binding.lay));

        binding.eventNameEt.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        });
    }

    private void setUpViews() {
        binding.eventLocationEt.setFocusable(false);
        binding.eventDateEndEt.setFocusable(false);
        binding.eventDateStartEt.setFocusable(false);
        binding.eventTimeEndEt.setFocusable(false);
        binding.eventTimeStartEt.setFocusable(false);
    }

    private void showTimePickerDialog() {
        RangeTimePickerDialogFragment rangeTimePickerDialogFragment = RangeTimePickerDialogFragment.newInstance();
        rangeTimePickerDialogFragment.show(getChildFragmentManager(), null);
    }

    private void initializeLocationSearch() {
        Places.initialize(requireContext(), BuildConfig.API_KEY);
        Places.createClient(requireContext());

        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.ADDRESS, Place.Field.NAME, Place.Field.LAT_LNG);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(requireContext());
        startActivityForResult(intent, AUTOCOMPLETE_LOCATION_REQUEST_CODE);
    }

    private void showDatePickerDialog() {
        MaterialDatePicker<Pair<Long, Long>> datePicker = MaterialDatePicker.Builder.dateRangePicker().build();
        datePicker.show(getChildFragmentManager(), "");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Log.i(TAG, selection.first + "" + selection.second);
            dateStart = selection.first / 1000;
            dateEnd = selection.second / 1000;
        });

        datePicker.addOnNegativeButtonClickListener(view -> datePicker.dismiss());
    }

    public static String getName() {
        return binding.eventNameEt.getText().toString();
    }

    public static Long getDateEnd() {
        return dateEnd;
    }

    public static String getTimeEnd() {
        return timeEnd;
    }

    public static String getDescription() {
        return binding.eventNotesEt.getText().toString();
    }

    public static Long getDateStart() {
        return dateStart;
    }

    public static String getTimeStart() {
        return timeStart;
    }

    public static Place getPlace() {
        return place;
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        binding.lay.requestFocus();
    }
}