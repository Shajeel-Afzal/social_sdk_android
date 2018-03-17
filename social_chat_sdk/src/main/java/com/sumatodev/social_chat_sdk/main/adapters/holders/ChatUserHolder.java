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
import com.sumatodev.social_chat_sdk.main.adapters.ChatAdpater;
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
    public ImageView status;
    public LinearLayout textLayout;
    private MessagesManager messagesManager;
    private ChatAdpater.Callback callback;


    public ChatUserHolder(View itemView, final ChatAdpater.Callback callback) {
        super(itemView);

        this.callback = callback;
        this.context = itemView.getContext();

        userImage_c = itemView.findViewById(R.id.userImage_c);
        messageText = itemView.findViewById(R.id.messageText);
        textTime = itemView.findViewById(R.id.textTime);
        status = itemView.findViewById(R.id.status);
        textLayout = itemView.findViewById(R.id.textLayout);


        messagesManager = MessagesManager.getInstance(context.getApplicationContext());

        if (callback != null) {
            textLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        callback.onLongItemClick(v, position);
                        return true;
                    }
                    return false;
                }
            });

        }
    }


    public void bindData(final Message message) {

        final String messageKey = message.getId();
        if (messageKey != null) {
            messageText.setText(message.getText());
            textTime.setText(DateUtils.formatDateTime(context, (long) message.getCreatedAt(),
                    DateUtils.FORMAT_SHOW_TIME));


            if (message.getFromUserId() != null) {
                messagesManager.getProfileSingleValue(message.getFromUserId(), createProfileChangeListener(userImage_c));
            }


            userImage_c.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (message.getFromUserId() != null) {
                        callback.onAuthorClick(message.getFromUserId(), v);
                    }
                }
            });
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
