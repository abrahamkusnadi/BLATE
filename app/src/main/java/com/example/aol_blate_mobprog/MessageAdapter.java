package com.example.aol_blate_mobprog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aol_blate_mobprog.models.Message;
import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private ArrayList<Message> messageList;
    private Context context;
    private String imageStr;

    public MessageAdapter(ArrayList<Message> messageList, Context context, String imageStr) {
        this.messageList = messageList;
        this.context = context;
        this.imageStr = imageStr;
    }

    // Determine if the message is mine (Right) or theirs (Left)
    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).isSentByMe()) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);

        if (holder instanceof SentMessageHolder) {
            ((SentMessageHolder) holder).bind(message);

        } else if (holder instanceof ReceivedMessageHolder) {
            ReceivedMessageHolder receivedHolder = (ReceivedMessageHolder) holder;
            receivedHolder.bind(message);

            int resId = 0;
            if (imageStr != null && !imageStr.isEmpty()) {
                resId = context.getResources().getIdentifier(
                        imageStr.toLowerCase(),
                        "drawable",
                        context.getPackageName()
                );
            }

            receivedHolder.ivAvatar.setImageResource(resId != 0 ? resId : R.drawable.ic_launcher_background);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // Holder for My Messages
    static class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvTime;
        SentMessageHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvMessageContent);
            tvTime = itemView.findViewById(R.id.tvMessageTime);
        }
        void bind(Message message) {
            tvContent.setText(message.getContent());
            tvTime.setText(message.getTime());
        }
    }

    // Holder for Their Messages
    static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvTime;
        ImageView ivAvatar;
        ReceivedMessageHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvMessageContent);
            tvTime = itemView.findViewById(R.id.tvMessageTime);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
        }
        void bind(Message message) {
            tvContent.setText(message.getContent());
            tvTime.setText(message.getTime());
        }
    }
}