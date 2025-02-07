package com.unknownn.doctorpatient.fragments.patient_chat.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.unknownn.doctorpatient.R;
import com.unknownn.doctorpatient.databinding.EachChatItemBinding;
import com.unknownn.doctorpatient.fragments.patient_chat.model.EachChat;
import com.unknownn.doctorpatient.others.ItemClickListener;

public class ChatAdapter extends ListAdapter<EachChat, ChatAdapter.ViewHolder> {

    private final ItemClickListener<EachChat> clickListener;
    private final Context mContext;
    private final boolean amIDoctor;

    @SuppressWarnings("FieldCanBeLocal")
    private EachChat curItem;

    public ChatAdapter(Context mContext, boolean amIDoctor, ItemClickListener<EachChat> clickListener) {
        super(diffCallback);
        this.mContext = mContext;
        this.amIDoctor = amIDoctor;
        this.clickListener = clickListener;
    }

    private static final DiffUtil.ItemCallback<EachChat> diffCallback = new DiffUtil.ItemCallback<EachChat>() {
        @Override
        public boolean areItemsTheSame(@NonNull EachChat oldItem, @NonNull EachChat newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull EachChat oldItem, @NonNull EachChat newItem) {
            return oldItem.fullyEquals(newItem);
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final EachChatItemBinding binding = EachChatItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        curItem = getItem(position);

        Glide.with(mContext)
                .load(amIDoctor ? curItem.getPatientPic() : curItem.getDoctorPic())
                .timeout(30*1000)
                .placeholder(R.drawable.doctor_icon)
                .into(holder.binding.ivProfile);

        holder.binding.tvName.setText( amIDoctor ? curItem.getPatientName() : curItem.getDoctorName() );
        holder.binding.tvLastMessage.setText( curItem.getLastMessage() );

        holder.binding.llHolder.setOnClickListener(v -> clickListener.onItemClick( getItem(holder.getAdapterPosition()) ));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final EachChatItemBinding binding;
        public ViewHolder(EachChatItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
