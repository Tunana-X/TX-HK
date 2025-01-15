package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<Message> messages;

    public ChatAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_message, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ai_message, parent, false);
        }
        return new ChatViewHolder(view,viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.textMessage.setText(message.getText());
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUserMessage() ? 1 : 0;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage;

        public ChatViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            if (viewType == 1) {
                textMessage = itemView.findViewById(R.id.textUserMessage); // 用户消息的 TextView
            } else {
                textMessage = itemView.findViewById(R.id.textAIMessage);  // AI 消息的 TextView
            }
        }
    }

}

// Message 类，表示单条消息的数据结构
class Message {
    private final String text;
    private final boolean isUserMessage;

    public Message(String text, boolean isUserMessage) {
        this.text = text;
        this.isUserMessage = isUserMessage;
    }

    public String getText() {
        return text;
    }

    public boolean isUserMessage() {
        return isUserMessage;
    }
}
