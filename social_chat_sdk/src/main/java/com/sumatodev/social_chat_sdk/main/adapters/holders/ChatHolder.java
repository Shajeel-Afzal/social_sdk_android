package com.sumatodev.social_chat_sdk.main.adapters.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.listeners.OnObjectChangedListener;
import com.sumatodev.social_chat_sdk.main.manager.MessagesManager;
import com.sumatodev.social_chat_sdk.main.model.Message;
import com.sumatodev.social_chat_sdk.main.model.UsersPublic;

public class ChatHolder extends RecyclerView.ViewHolder {

    private Context context;

    public ImageView userImage_c;
    public TextView messageText;
    public TextView textTime;
    public ImageView status;
    public LinearLayout textLayout;
    private ImageView messageImage;
    private ProgressBar progressBar;
    private MessagesManager messagesManager;

    public ChatHolder(View itemView) {
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

    private OnObjectChangedListener<UsersPublic> createProfileChangeListener(final ImageView authorImageView) {
        return new OnObjectChangedListener<UsersPublic>() {
            @Override
            public void onObjectChanged(final UsersPublic obj) {
                if (obj.getPhotoUrl() != null) {

                    Picasso.get()
                            .load(obj.getPhotoUrl())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(authorImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get()
                                            .load(obj.getPhotoUrl())
                                            .into(authorImageView);
                                }
                            });
                }
            }
        };
    }
}
