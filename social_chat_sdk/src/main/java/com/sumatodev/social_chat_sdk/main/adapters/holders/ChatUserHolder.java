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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.listeners.OnObjectChangedListener;
import com.sumatodev.social_chat_sdk.main.manager.MessagesManager;
import com.sumatodev.social_chat_sdk.main.model.Message;
import com.sumatodev.social_chat_sdk.main.model.UsersPublic;


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


    public ChatUserHolder(View itemView, final OnClickListener onClickListener) {
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

        if (messageImage != null) {
            messageImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                        onClickListener.onImageClick(position, v);
                    }
                }
            });
        }
    }


    public void bindTextData(final Message message) {

        textTime.setText(DateUtils.formatDateTime(context, (long) message.getCreatedAt(),
                DateUtils.FORMAT_SHOW_TIME));


        if (message.getText() != null) {
            messageText.setText(message.getText());
        }

        if (message.getFromUserId() != null) {
            messagesManager.getUsersPublicProfile(context.getApplicationContext(), message.getFromUserId(),
                    createProfileChangeListener(userImage_c));
        }
    }

    public void bindImageData(Message message) {

        textTime.setText(DateUtils.formatDateTime(context, (long) message.getCreatedAt(),
                DateUtils.FORMAT_SHOW_TIME));


        if (message.getImageUrl() != null) {
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
            messagesManager.getUsersPublicProfile(context, message.getFromUserId(),
                    createProfileChangeListener(userImage_c));
        }

    }

    private OnObjectChangedListener<UsersPublic> createProfileChangeListener(final ImageView authorImageView) {
        return new OnObjectChangedListener<UsersPublic>() {
            @Override
            public void onObjectChanged(final UsersPublic obj) {
                if (obj.getPhotoUrl() != null) {

                    Picasso.get()
                            .load(obj.getPhotoUrl())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.user_thumbnail)
                            .error(R.drawable.user_thumbnail)
                            .into(authorImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get()
                                            .load(obj.getPhotoUrl())
                                            .placeholder(R.drawable.user_thumbnail)
                                            .error(R.drawable.user_thumbnail)
                                            .into(authorImageView);
                                }
                            });
                }
            }
        };
    }


    public interface OnClickListener {
        void onItemLongClick(int position, View view);

        void onUserImageClick(int position, View view);

        void onImageClick(int position, View view);
    }

}
