package com.sumatodev.social_chat_sdk.main.adapters;

import android.support.v7.widget.RecyclerView;

import com.sumatodev.social_chat_sdk.main.model.Message;
import com.sumatodev.social_chat_sdk.views.activities.BaseActivity;

import java.util.LinkedList;
import java.util.List;

public abstract class ChatBaseAdapter extends RecyclerView.Adapter {

    public static final String TAG = ChatBaseAdapter.class.getSimpleName();


    protected List<Message> messageList = new LinkedList<>();
    protected BaseActivity activity;
    protected int selectedMessagePosition = -1;

    public ChatBaseAdapter(BaseActivity activity) {
        this.activity = activity;
    }

    protected void cleanSelectedPostInformation() {
        selectedMessagePosition = -1;
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messageList != null ? messageList.get(position).getMessageType().getMessageType() : 0;
    }

    protected Message getItemByPosition(int position) {
        return messageList.get(position);
    }
}
