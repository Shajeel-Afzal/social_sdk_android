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
import com.sumatodev.social_chat_sdk.main.listeners.OnObjectChangedListener;
import com.sumatodev.social_chat_sdk.main.manager.MessagesManager;
import com.sumatodev.social_chat_sdk.main.model.Message;
import com.sumatodev.social_chat_sdk.main.model.UsersPublic;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Ali on 09/04/2018.
 */

public class ChatHolder extends RecyclerView.ViewHolder {


    private static final String TAG = ChatHolder.class.getSimpleName();
    public CircleImageView userImage_c;
    public TextView messageText;
    public TextView textTime;
    public CircleImageView status;
    public LinearLayout textLayout;
    private ImageView messageImage;
    private ProgressBar progressBar;
    private MessagesManager messagesManager;
    private Context context;

    public ChatHolder(View itemView, final OnClickListener onClickListener) {
        super(itemView);
        this.context = itemView.getContext();

        userImage_c = itemView.findViewById(R.id.userImage_c);
        messageText = itemView.findViewById(R.id.messageText);
        textTime = itemView.findViewById(R.id.textTime);
        status = itemView.findViewById(R.id.status);
        textLayout = itemView.findViewById(R.id.textLayout);
        messageImage = itemView.findViewById(R.id.messageImage);
        progressBar = itemView.findViewById(R.id.progressBar);

        messagesManager = MessagesManager.getInstance(context.getApplicationContext());


        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = getAdapterPosition();
                if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                    onClickListener.onItemLongClick(position, v);
                    return true;
                }
                return false;
            }
        });


        if (userImage_c != null) {
            userImage_c.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                        onClickListener.onUserImageClick(position, v);
                    }
                }
            });
        }

    }


    public void bindMyData(Message message) {

        textTime.setText(DateUtils.formatDateTime(context, (long) message.getCreatedAt(),
                DateUtils.FORMAT_SHOW_TIME));

        if (message.getText() != null) {
            messageText.setVisibility(View.VISIBLE);
            messageText.setText(message.getText());
        }
    }

    public void bindMyImageData(final Message message) {

        textTime.setText(DateUtils.formatDateTime(context, (long) message.getCreatedAt(),
                DateUtils.FORMAT_SHOW_TIME));

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


        }

    }

    public void bindUsersData(Message message) {

        textTime.setText(DateUtils.formatDateTime(context, (long) message.getCreatedAt(),
                DateUtils.FORMAT_SHOW_TIME));


        if (message.getText() != null) {
            messageText.setVisibility(View.VISIBLE);
            messageText.setText(message.getText());
        }

        if (message.getFromUserId() != null) {
            messagesManager.getUsersPublicProfile(context.getApplicationContext(), message.getFromUserId(),
                    createProfileChangeListener(userImage_c));
        }
    }


    public void bindUserImageData(Message message) {

        textTime.setText(DateUtils.formatDateTime(context, (long) message.getCreatedAt(),
                DateUtils.FORMAT_SHOW_TIME));


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
        }

        if (message.getFromUserId() != null) {
            messagesManager.getUsersPublicProfile(context.getApplicationContext(), message.getFromUserId(),
                    createProfileChangeListener(userImage_c));
        }
    }

    private OnObjectChangedListener<UsersPublic> createProfileChangeListener(final ImageView authorImageView) {
        return new OnObjectChangedListener<UsersPublic>() {
            @Override
            public void onObjectChanged(final UsersPublic obj) {
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

    public interface OnClickListener {
        void onItemLongClick(int position, View view);

        void onUserImageClick(int position, View view);
    }
}
