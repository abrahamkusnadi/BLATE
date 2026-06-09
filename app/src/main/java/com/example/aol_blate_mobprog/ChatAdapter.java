package com.example.aol_blate_mobprog;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aol_blate_mobprog.models.Chat;
import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    ArrayList<Chat> items;
    Context context;

    public ChatAdapter(ArrayList<Chat> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvMessage.setText(item.getMessage());

        // logic gambar string, masih error, karena tetep harus filenya dari local laptop kita satu satu
        // coba aja masukin nama gambar sesuai sama imageString yang lu buat, harusnya ga error
        String imageName = item.getImageStr();
        int defImage = R.drawable.ic_launcher_background;

        if (imageName != null && !imageName.isEmpty()) {
            // pakae .toLowerCase() karena nama file drawable harus huruf kecil semua
            int resId = holder.itemView.getContext().getResources().getIdentifier(
                    imageName.toLowerCase(),
                    "drawable",
                    holder.itemView.getContext().getPackageName()
            );
            // pasang gambar, jika resId 0 / ga nemu, pakai default
            holder.ivAvatar.setImageResource(resId != 0 ? resId : defImage);
        } else {
            // datanya kosong dari Firebase, pakai default
            holder.ivAvatar.setImageResource(defImage);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatDetailActivity.class);
            intent.putExtra("userName", item.getName());
            intent.putExtra("userImage", item.getImageStr());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMessage;
        ImageView ivAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
        }
    }
}