package com.sumatodev.social_chat_sdk.main.adapters.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.adapters.ThreadAdapter;
import com.sumatodev.social_chat_sdk.main.listeners.OnObjectChangedListener;
import com.sumatodev.social_chat_sdk.main.manager.MessagesManager;
import com.sumatodev.social_chat_sdk.main.model.Message;
import com.sumatodev.social_chat_sdk.main.model.ThreadsModel;
import com.sumatodev.social_chat_sdk.main.model.UsersPublic;
import com.sumatodev.social_chat_sdk.main.utils.RoundedCornersTransform;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Ali on 14/02/2018.
 */

public class ThreadsHolder extends RecyclerView.ViewHolder {

    private static final String TAG = ThreadsHolder.class.getSimpleName();
    private ImageView userImage_m;
    private TextView userName_m;
    private TextView lastMessage_m;
    private TextView lastSeen_m;
    private CircleImageView status_m;
    private TextView notificationItems;
    private Context context;
    private MessagesManager messagesManager;
    private ThreadAdapter.Callback callback;


    public ThreadsHolder(View itemView, final ThreadAdapter.Callback callback) {
        super(itemView);
        this.context = itemView.getContext();
        this.callback = callback;

        messagesManager = MessagesManager.getInstance(context.getApplicationContext());

        userName_m = itemView.findViewById(R.id.userName_m);
        userImage_m = itemView.findViewById(R.id.userImage_m);
        lastMessage_m = itemView.findViewById(R.id.lastMessage_m);
        lastSeen_m = itemView.findViewById(R.id.lastSeen_m);
        status_m = itemView.findViewById(R.id.status_m);
        notificationItems = itemView.findViewById(R.id.notificationItems);


        if (callback != null) {
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        callback.onItemLongClick(position, v);
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    public void bindData(final ThreadsModel threadsModel) {
        Log.d(TAG, "threads key: " + threadsModel.getThreadKey());

        if (threadsModel.getThreadKey() != null) {
            messagesManager.getUsersPublicProfile(context.getApplicationContext(), threadsModel.getThreadKey(),
                    onProfileChangedListener());


            messagesManager.getLastMessage(context.getApplicationContext(), threadsModel.getThreadKey(),
                    messageChangedListener());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callback != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                        callback.onItemClick(threadsModel.getThreadKey(), v);
                    }
                }
            });
        }
    }

    private OnObjectChangedListener<UsersPublic> onProfileChangedListener() {
        return new OnObjectChangedListener<UsersPublic>() {
            @Override
            public void onObjectChanged(final UsersPublic obj) {
                if (obj != null) {
                    userName_m.setText(obj.getUsername());

                    if (obj.getStatus() != null) {
                        if (obj.getStatus().isOnline) {
                            status_m.setImageResource(R.drawable.online_status);
                        } else {
                            status_m.setImageResource(R.drawable.offline_status);
                        }
                    }
                    Picasso.with(context).load(obj.getPhotoUrl())
                            .placeholder(R.drawable.imageview_user_thumb)
                            .transform(new RoundedCornersTransform())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(userImage_m, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {

                                    Picasso.with(context).load(obj.getPhotoUrl())
                                            .placeholder(R.drawable.imageview_user_thumb)
                                            .transform(new RoundedCornersTransform())
                                            .into(userImage_m);
                                }
                            });

                }
            }
        };
    }

    private OnObjectChangedListener<Message> messageChangedListener() {
        return new OnObjectChangedListener<Message>() {
            @Override
            public void onObjectChanged(Message obj) {
                if (obj != null) {
                    lastMessage_m.setText(obj.getText());
                    lastSeen_m.setText(DateUtils.formatDateTime(context, (long) obj.getCreatedAt(),
                            DateUtils.FORMAT_SHOW_TIME));
                }
            }
        };
    }
}
