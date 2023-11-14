package com.unknownn.doctorpatient.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.unknownn.doctorpatient.R;
import com.unknownn.doctorpatient.others.AvDoctor;
import com.unknownn.doctorpatient.others.ItemClickListener;

public class AvAdapter extends ListAdapter<AvDoctor, AvAdapter.ViewHolder> {

    private final ItemClickListener listener;
    private AvDoctor curDoctor;

    public AvAdapter(ItemClickListener listener) {
        super(callBack);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<AvDoctor> callBack = new DiffUtil.ItemCallback<AvDoctor>() {
        @Override
        public boolean areItemsTheSame(@NonNull AvDoctor oldItem, @NonNull AvDoctor newItem) {
            return oldItem.getUid().equals(newItem.getUid());
        }

        @Override
        public boolean areContentsTheSame(@NonNull AvDoctor oldItem, @NonNull AvDoctor newItem) {
            return oldItem.fullySame(newItem);
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

        holder.ivCall.setOnClickListener(v -> listener.onItemClick(getItem(holder.getAdapterPosition())));
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView tvName;
        private final ImageView ivCall;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_name);
            ivCall = itemView.findViewById(R.id.iv_video_call);

        }

    }

}
