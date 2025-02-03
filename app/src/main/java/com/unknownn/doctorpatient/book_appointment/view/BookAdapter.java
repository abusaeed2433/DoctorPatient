package com.unknownn.doctorpatient.book_appointment.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.unknownn.doctorpatient.R;
import com.unknownn.doctorpatient.book_appointment.model.SpanItem;
import com.unknownn.doctorpatient.databinding.EachBookItemBinding;
import com.unknownn.doctorpatient.others.ItemClickListener;

public class BookAdapter extends ListAdapter<SpanItem, BookAdapter.ViewHolder> {

    private int highlightedIndex = -1;

    protected BookAdapter() {
        super(diffCallback);
    }

    private static final DiffUtil.ItemCallback<SpanItem> diffCallback = new DiffUtil.ItemCallback<SpanItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull SpanItem oldItem, @NonNull SpanItem newItem) {
            return oldItem.getIndex() == newItem.getIndex();
        }

        @Override
        public boolean areContentsTheSame(@NonNull SpanItem oldItem, @NonNull SpanItem newItem) {
            return oldItem.getSpannableString().equals(newItem.getSpannableString());// && oldItem.isHighlighted() != newItem.isHighlighted();
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final EachBookItemBinding binding = EachBookItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding.tvText.setText( getItem(position).getSpannableString() );

        holder.binding.clHolder.setBackgroundResource(
                position == highlightedIndex ? R.drawable.selected_speciality : R.drawable.shadow_up_back
        );

        holder.binding.clHolder.setOnClickListener(v-> highlightItem(holder.getAdapterPosition()));

    }

    public SpanItem getHighlightedItem(){
        return getItem(highlightedIndex);
    }

    public void highlightItem(int index){
        int prev = highlightedIndex;
        highlightedIndex = index;
        notifyItemChanged(prev);
        notifyItemChanged(highlightedIndex);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final EachBookItemBinding binding;
        public ViewHolder(@NonNull EachBookItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
