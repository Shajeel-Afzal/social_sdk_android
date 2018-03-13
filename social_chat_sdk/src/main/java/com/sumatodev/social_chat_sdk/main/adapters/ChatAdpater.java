package com.sumatodev.social_chat_sdk.main.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.adapters.holders.ChatMyHolder;
import com.sumatodev.social_chat_sdk.main.adapters.holders.ChatUserHolder;
import com.sumatodev.social_chat_sdk.main.adapters.holders.LoadViewHolder;
import com.sumatodev.social_chat_sdk.main.listeners.OnChatItemListener;
import com.sumatodev.social_chat_sdk.main.model.Message;

/**
 * Created by Ali on 12/03/2018.
 */

public class ChatAdpater extends FirebaseRecyclerAdapter<Message, RecyclerView.ViewHolder> {

    private static final String TAG = ChatAdpater.class.getName();
    private final static int MY_VIEW = 0;
    private final static int USER_VIEW = 1;

    private String current_uid;
    private Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public ChatAdpater(@NonNull FirebaseRecyclerOptions<Message> options) {
        super(options);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            current_uid = firebaseUser.getUid();
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull Message model) {

        switch (holder.getItemViewType()) {
            case MY_VIEW:
                ((ChatMyHolder) holder).setData(model);
                break;
            case USER_VIEW:
                ((ChatUserHolder) holder).bindData(model);
                break;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == MY_VIEW) {
            return new ChatMyHolder(inflater.inflate(R.layout.messages_my_view, parent, false));
        } else if (viewType == USER_VIEW) {
            return new ChatUserHolder(inflater.inflate(R.layout.messages_user_view, parent, false),
                    createOnChatItemListener());
        } else {
            return new LoadViewHolder(inflater.inflate(R.layout.loading_view, parent, false));
        }
    }

    private OnChatItemListener createOnChatItemListener() {
        return new OnChatItemListener() {
            @Override
            public void onTextClick(int position, View view) {
                if (callback != null) {
                    callback.onTextClick(getRef(position).getKey(), view);
                }
            }

            @Override
            public void onAuthorClick(int position, View view) {
                if (callback != null) {
                    callback.onAuthorClick(getRef(position).getKey(), view);
                }
            }
        };
    }


    @Override
    public int getItemViewType(int position) {
        String user = getItem(position).getFromUserId();
        if (user != null && user.equalsIgnoreCase(current_uid)) {
            return MY_VIEW;
        } else {
            return USER_VIEW;
        }
    }

    public interface Callback {
        void onTextClick(String key, View view);

        void onAuthorClick(String authorId, View view);
    }
}
