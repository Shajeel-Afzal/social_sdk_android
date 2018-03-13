package com.sumatodev.social_chat_sdk.main.adapters.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.listeners.OnChatItemListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnObjectChangedListener;
import com.sumatodev.social_chat_sdk.main.manager.MessagesManager;
import com.sumatodev.social_chat_sdk.main.model.Message;
import com.sumatodev.social_chat_sdk.main.model.Profile;


/**
 * Created by Ali on 13/02/2018.
 */

public class ChatUserHolder extends RecyclerView.ViewHolder {

    private Context context;

    public ImageView userImage_c;
    public TextView messageText;
    public TextView textTime;
    public LinearLayout textLayout;
    public ImageView status;
    private MessagesManager messagesManager;


    public ChatUserHolder(View view, OnChatItemListener onChatItemListener) {
        this(view, onChatItemListener, true);
    }

    public ChatUserHolder(View itemView, final OnChatItemListener onChatItemListener, boolean isAuthorNeeded) {
        super(itemView);
        this.context = itemView.getContext();

        userImage_c = itemView.findViewById(R.id.userImage_c);
        messageText = itemView.findViewById(R.id.messageText);
        textTime = itemView.findViewById(R.id.textTime);
        status = itemView.findViewById(R.id.status);

        userImage_c.setVisibility(isAuthorNeeded ? View.VISIBLE : View.GONE);
        messagesManager = MessagesManager.getInstance(context.getApplicationContext());

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if (onChatItemListener != null && position != RecyclerView.NO_POSITION) {
                    onChatItemListener.onTextClick(getAdapterPosition(), v);
                }
            }
        });

        userImage_c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if (onChatItemListener != null && position != RecyclerView.NO_POSITION) {
                    onChatItemListener.onAuthorClick(getAdapterPosition(), v);
                }
            }
        });
    }


    public void bindData(Message message) {

        messageText.setText(message.getText());
        textTime.setText(DateUtils.formatDateTime(context, (long) message.getCreatedAt(),
                DateUtils.FORMAT_SHOW_TIME));


        if (message.getFromUserId() != null) {
            messagesManager.getProfileSingleValue(message.getFromUserId(), createProfileChangeListener(userImage_c));
        }
    }

    private OnObjectChangedListener<Profile> createProfileChangeListener(final ImageView authorImageView) {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void onObjectChanged(final Profile obj) {
                if (obj.getPhotoUrl() != null) {

                    Glide.with(context)
                            .load(obj.getPhotoUrl())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .crossFade()
                            .into(authorImageView);
                }
            }
        };
    }
}
