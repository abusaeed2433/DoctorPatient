package com.unknownn.doctorpatient.fragments.patient_home.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.unknownn.doctorpatient.R;
import com.unknownn.doctorpatient.databinding.EachSpecialityDesignBinding;
import com.unknownn.doctorpatient.fragments.patient_home.model.Speciality;

import java.util.Set;
import java.util.TreeSet;

public class SpecialityAdapter extends ListAdapter<Speciality, SpecialityAdapter.ViewHolder> {

    private final Context mContext;
    @SuppressWarnings("FieldCanBeLocal")
    private Speciality curItem;

    private final ItemClickListener clickListener;
    private final Set<Integer> selectedPositions = new TreeSet<>();

    protected SpecialityAdapter(Context mContext, ItemClickListener clickListener) {
        super(diffCallback);
        this.mContext = mContext;
        this.clickListener = clickListener;
        selectedPositions.add(0);
    }

    private static final DiffUtil.ItemCallback<Speciality> diffCallback = new DiffUtil.ItemCallback<Speciality>() {
        @Override
        public boolean areItemsTheSame(@NonNull Speciality oldItem, @NonNull Speciality newItem) {
            return oldItem.getName().equals(newItem.getName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Speciality oldItem, @NonNull Speciality newItem) {
            return oldItem.getName().equals(newItem.getName()) && oldItem.getImageUrl().equals(newItem.getImageUrl());
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final EachSpecialityDesignBinding binding = EachSpecialityDesignBinding.inflate(LayoutInflater.from(mContext),parent,false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        curItem = getItem(position);

        Glide.with(mContext)
                .load(curItem.getImageUrl())
                .timeout(30*1000)
                .placeholder(R.drawable.medicine)
                .into(holder.binding.specialtyIcon);
        holder.binding.specialtyText.setText(curItem.getName());

        if(selectedPositions.contains(position)){
            holder.binding.llHolder.setBackgroundResource(R.drawable.selected_speciality);
        }
        else{
            holder.binding.llHolder.setBackgroundResource(R.drawable.speciality_back);
        }

        holder.itemView.setOnClickListener(v -> {
           final int curPosition = holder.getAdapterPosition();

           final boolean removed = selectedPositions.contains(curPosition);

           if(removed){
               selectedPositions.remove(curPosition);
           }
           else{
               selectedPositions.add(curPosition);
           }

           notifyItemChanged(curPosition);
            Log.d("Speciality item clicked", "onBindViewHolder: "+curPosition);
           clickListener.onItemClicked(getItem(curPosition), removed);
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final EachSpecialityDesignBinding binding;
        public ViewHolder(@NonNull EachSpecialityDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface ItemClickListener{
        void onItemClicked(Speciality speciality, boolean removed);
    }

}
