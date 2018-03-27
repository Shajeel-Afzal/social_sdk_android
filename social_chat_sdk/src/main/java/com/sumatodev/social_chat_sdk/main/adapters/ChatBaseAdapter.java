package com.sumatodev.social_chat_sdk.main.adapters;

import android.support.v7.widget.RecyclerView;

import com.sumatodev.social_chat_sdk.main.model.Message;
import com.sumatodev.social_chat_sdk.views.activities.BaseActivity;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ali on 16/03/2018.
 */

public abstract class ChatBaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = ChatBaseAdapter.class.getSimpleName();

    private final static int MY_VIEW = 0;
    private final static int USER_VIEW = 1;


    protected List<Message> messageList = new LinkedList<>();
    protected BaseActivity activity;
    protected int selectedMessagePosition = -1;

    public ChatBaseAdapter(BaseActivity activity) {
        this.activity = activity;
    }

    protected void cleanSelectedMessageInformation() {
        selectedMessagePosition = -1;
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        String userKey = messageList.get(position).getFromUserId();
        if (userKey != null && userKey.equalsIgnoreCase(activity.getCurrent_uid())) {
            return MY_VIEW;
        } else if (userKey != null && !userKey.equalsIgnoreCase(activity.getCurrent_uid())) {
            return USER_VIEW;
        }
        return messageList.get(position).getItemType().getTypeCode();
    }

    public Message getItemByPosition(int position) {
        return messageList.get(position);
    }

}
