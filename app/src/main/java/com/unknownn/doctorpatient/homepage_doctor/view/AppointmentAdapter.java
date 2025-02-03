package com.unknownn.doctorpatient.homepage_doctor.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.unknownn.doctorpatient.R;
import com.unknownn.doctorpatient.databinding.EachAppointmentBinding;
import com.unknownn.doctorpatient.homepage_doctor.model.Appointment;
import com.unknownn.doctorpatient.others.ItemClickListener;

public class AppointmentAdapter extends ListAdapter<Appointment, AppointmentAdapter.ViewHolder> {

    private final Context mContext;
    private final boolean showDoctorData;
    private final ItemClickListener<Appointment> clickListener;
    @SuppressWarnings("FieldCanBeLocal")
    private Appointment curItem;

    public AppointmentAdapter(Context mContext, boolean showDoctorData, DiffUtil.ItemCallback<Appointment> diffCallback, ItemClickListener<Appointment> clickListener) {
        super(diffCallback);
        this.mContext = mContext;
        this.showDoctorData = showDoctorData;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final EachAppointmentBinding binding = EachAppointmentBinding.inflate(LayoutInflater.from(mContext), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        curItem = getItem(position);

        holder.binding.tvDayDD.setText( curItem.getDayDD() );
        holder.binding.tvMonthDayName.setText( curItem.getDateMmDayName() );

        Glide.with(mContext)
                .load(showDoctorData ? curItem.getDoctorImage() : curItem.getPatientImage())
                .timeout(30*1000)
                .placeholder(R.drawable.doctor_icon)
                .into(holder.binding.ivProfile);

        holder.binding.tvName.setText( showDoctorData ? curItem.getDoctorName() : curItem.getPatientName() );
        holder.binding.tvInfo.setText( showDoctorData ? curItem.getDoctorSpeciality() : curItem.getPatientDescription() );
        holder.binding.tvTime.setText( curItem.getTime() );

        holder.binding.ivDetails.setOnClickListener(v -> clickListener.onItemClick( getItem(holder.getAdapterPosition()) ));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final EachAppointmentBinding binding;
        public ViewHolder(@NonNull EachAppointmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
