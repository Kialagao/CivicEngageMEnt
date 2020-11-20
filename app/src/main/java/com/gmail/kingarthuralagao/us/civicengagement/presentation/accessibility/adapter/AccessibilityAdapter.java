package com.gmail.kingarthuralagao.us.civicengagement.presentation.accessibility.adapter;


import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gmail.kingarthuralagao.us.civicengagement.data.model.accessibility.AccessibilityAvailability;
import com.gmail.kingarthuralagao.us.civilengagement.R;
import com.gmail.kingarthuralagao.us.civilengagement.databinding.RowItemAccessibilityAvailabilityBinding;

import java.util.ArrayList;

public class AccessibilityAdapter extends RecyclerView.Adapter<AccessibilityAdapter.ViewHolder> {

    private ArrayList<AccessibilityAvailability> accessibilities;

    public AccessibilityAdapter(ArrayList<AccessibilityAvailability> accessibilityAvailabilityList) {
        accessibilities = accessibilityAvailabilityList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        RowItemAccessibilityAvailabilityBinding binding = RowItemAccessibilityAvailabilityBinding.inflate(inflater, parent, false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int resID;
        if (accessibilities.get(position).getAvailable()) {
            resID = R.drawable.ic_available;
        } else {
            resID = R.drawable.ic_unavailable;
        }

        Glide.with(holder
                .resourceTv.getContext())
                .load(resID)
                .into(holder.availabilityIv);
        holder.getAvailabilityIv();

        holder.getResourceTv().setText(accessibilities.get(position).getResource());
    }

    @Override
    public int getItemCount() {
        return accessibilities.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView resourceTv;
        private ImageView availabilityIv;

        public ViewHolder(@NonNull RowItemAccessibilityAvailabilityBinding binding) {
            super(binding.getRoot());

            resourceTv = binding.resourceTv;
            availabilityIv = binding.availabilityIv;
        }

        public TextView getResourceTv() {
            return resourceTv;
        }

        public ImageView getAvailabilityIv() {
            return availabilityIv;
        }
    }
}