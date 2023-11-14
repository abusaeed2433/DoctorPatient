package com.unknownn.doctorpatient.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.unknownn.doctorpatient.R;
import com.unknownn.doctorpatient.others.EachMessage;
import com.unknownn.doctorpatient.others.ItemClickable;

public class MessageAdapter extends ListAdapter<EachMessage, RecyclerView.ViewHolder> {

    private final int CHAT_MESSAGE_ID = 1;
    private final int FILE_MESSAGE_ID = 2;
    private final Context mContext;
    private final boolean amIDoctor;
    private final ItemClickable listener;
    private EachMessage curMessage;
    private int gravity = Gravity.CENTER;

    private static final DiffUtil.ItemCallback<EachMessage> callBack = new DiffUtil.ItemCallback<EachMessage>() {
        @Override
        public boolean areItemsTheSame(@NonNull EachMessage oldItem, @NonNull EachMessage newItem) {
            return oldItem.isSame(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull EachMessage oldItem, @NonNull EachMessage newItem) {
            return oldItem.fullySame(newItem);
        }
    };

    public MessageAdapter(Context mContext,boolean amIDoctor, ItemClickable listener) {
        super(callBack);
        this.mContext = mContext;
        this.amIDoctor = amIDoctor;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == CHAT_MESSAGE_ID){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_chat_layout,parent,false);
            return  new ChatViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_chat_file,parent,false);
            return new FileViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        curMessage = getItem(position);

        gravity = curMessage.getGravity(amIDoctor);

        if(getItemViewType(position) == CHAT_MESSAGE_ID){
            ((ChatViewHolder)holder).llHolder.setGravity(gravity);
            ((ChatViewHolder)holder).tvMessage.setGravity(gravity);

            if(curMessage.showText()){
                ((ChatViewHolder)holder).tvMessage.setText(curMessage.getText());
                ((ChatViewHolder)holder).ivMessage.setVisibility(View.GONE);
                ((ChatViewHolder)holder).tvMessage.setVisibility(View.VISIBLE);
            }
            else{
                ((ChatViewHolder)holder).ivMessage.setVisibility(View.VISIBLE);
                ((ChatViewHolder)holder).tvMessage.setVisibility(View.GONE);
                try {
                    Glide.with(mContext)
                            .load(curMessage.getUrl())
                            .placeholder(R.drawable.ic_baseline_image_24)
                            .dontTransform()
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(((ChatViewHolder)holder).ivMessage);
                }catch (Exception ignored){}
            }
        }
        else{
            ((FileViewHolder)holder).tvFileName.setGravity(gravity);
            ((FileViewHolder)holder).llHolder.setGravity(gravity);
            ((FileViewHolder)holder).tvFileName.setText(curMessage.getText());
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClicked(getItem(holder.getAdapterPosition())));

    }

    @Override
    public int getItemViewType(int position) {
        if(getItem(position).isFile()){
            return FILE_MESSAGE_ID;
        }
        return CHAT_MESSAGE_ID;
    }


    static class FileViewHolder extends RecyclerView.ViewHolder{

        private final LinearLayout llHolder;
        private final TextView tvFileName;


        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            llHolder = itemView.findViewById(R.id.ll_holder);
            tvFileName = itemView.findViewById(R.id.tv_file_name);
        }
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvMessage;
        private final ImageView ivMessage;
        private final LinearLayout llHolder;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            llHolder = itemView.findViewById(R.id.ll_holder);
            tvMessage = itemView.findViewById(R.id.tv_message);
            ivMessage = itemView.findViewById(R.id.iv_message);
        }
    }

}
