package com.sumatodev.social_chat_sdk.main.adapters.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.listeners.OnObjectChangedListener;
import com.sumatodev.social_chat_sdk.main.manager.MessagesManager;
import com.sumatodev.social_chat_sdk.main.model.Profile;
import com.sumatodev.social_chat_sdk.main.model.ThreadsModel;
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

    public ThreadsHolder(View view, OnClickListener onClickListener) {
        this(view, onClickListener, true);
    }

    public ThreadsHolder(View itemView, final OnClickListener onClickListener, boolean isAuthorNeeded) {
        super(itemView);
        this.context = itemView.getContext();

        userName_m = itemView.findViewById(R.id.userName_m);
        userImage_m = itemView.findViewById(R.id.userImage_m);
        lastMessage_m = itemView.findViewById(R.id.lastMessage_m);
        lastSeen_m = itemView.findViewById(R.id.lastSeen_m);
        status_m = itemView.findViewById(R.id.status_m);
        notificationItems = itemView.findViewById(R.id.notificationItems);

        userImage_m.setVisibility(isAuthorNeeded ? View.VISIBLE : View.INVISIBLE);

        messagesManager = MessagesManager.getInstance(context.getApplicationContext());


        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                    onClickListener.onItemClick(getAdapterPosition(),v);
                }
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = getAdapterPosition();
                if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                    onClickListener.onItemLongPressed(getAdapterPosition(),v);
                }
                return true;
            }
        });
    }

    public void bindData(ThreadsModel threadsModel) {
        Log.d(TAG, "threads key: " + threadsModel.getThreadKey());

        if (threadsModel.getThreadKey() != null) {
            messagesManager.getProfileValue(context.getApplicationContext(), threadsModel.getThreadKey(), onProfileChangedListener());
        }
    }

    private OnObjectChangedListener<Profile> onProfileChangedListener() {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void onObjectChanged(final Profile obj) {
                if (obj != null) {
                    userName_m.setText(obj.getUsername());

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


    public interface OnClickListener {
        void onItemClick(int position, View view);

        void onItemLongPressed(int position, View view);
    }
}
