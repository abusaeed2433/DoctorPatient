package com.unknownn.doctorpatient.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.unknownn.doctorpatient.R;
import com.unknownn.doctorpatient.others.AvDoctor;
import com.unknownn.doctorpatient.others.Doctor;
import com.unknownn.doctorpatient.others.ItemClickListener;

public class AvAdapter extends ListAdapter<Doctor, AvAdapter.ViewHolder> {

    private final ItemClickListener listener;
    private final Context mContext;

    private Doctor curDoctor;

    public AvAdapter(Context mContext, ItemClickListener listener) {
        super(callBack);
        this.mContext = mContext;
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Doctor> callBack = new DiffUtil.ItemCallback<Doctor>() {
        @Override
        public boolean areItemsTheSame(@NonNull Doctor oldItem, @NonNull Doctor newItem) {
            return oldItem.getUid().equals(newItem.getUid());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Doctor oldItem, @NonNull Doctor newItem) {
            return oldItem.getName().equals(newItem.getName()) && oldItem.getSpeciality().equals(newItem.getSpeciality()) && oldItem.getImageUrl().equals(newItem.getImageUrl());
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_doctor_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        curDoctor = getItem(position);

        holder.tvName.setText(curDoctor.getName());
        holder.tvSpeciality.setText(curDoctor.getSpecialityMessage());

        Glide.with(mContext)
                .load(curDoctor.getImageUrl())
                .timeout(30 * 1000)
                .placeholder(R.drawable.doctor_icon)
                .into(holder.ivProfile);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(getItem(holder.getAdapterPosition())));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final ImageView ivProfile;
        private final TextView tvName;
        private final TextView tvSpeciality;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvName = itemView.findViewById(R.id.tvName);
            tvSpeciality = itemView.findViewById(R.id.tvSpeciality);

        }

    }

}
