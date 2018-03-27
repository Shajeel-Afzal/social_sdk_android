package com.sumatodev.social_chat_sdk.main.adapters.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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
    private ImageView messageImage;
    private ProgressBar progressBar;
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
        messageImage = itemView.findViewById(R.id.messageImage);
        progressBar = itemView.findViewById(R.id.progressBar);


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

            textTime.setText(DateUtils.formatDateTime(context, (long) message.getCreatedAt(),
                    DateUtils.FORMAT_SHOW_TIME));


            if (message.getText() != null) {
                messageText.setVisibility(View.VISIBLE);
                messageText.setText(message.getText());
            }

            if (message.getImageUrl() != null) {
                messageImage.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                Glide.with(context)
                        .load(message.getImageUrl())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .fitCenter()
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(messageImage);

                messageImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callback != null) {
                            callback.onImageClick(message.getImageUrl());
                        }
                    }
                });
            }

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
