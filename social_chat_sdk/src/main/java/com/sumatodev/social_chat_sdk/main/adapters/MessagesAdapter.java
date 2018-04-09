package com.sumatodev.social_chat_sdk.main.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.adapters.holders.ChatHolder;
import com.sumatodev.social_chat_sdk.main.adapters.holders.LoadViewHolder;
import com.sumatodev.social_chat_sdk.main.model.Message;
import com.sumatodev.social_chat_sdk.views.activities.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ali on 09/04/2018.
 */

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = MessagesAdapter.class.getSimpleName();

    private final static int MY_TEXT_VIEW = 0;
    private final static int USER_TEXT_VIEW = 1;
    private final static int MY_IMAGE_VIEW = 3;
    private final static int USERS_IMAGE_VIEW = 4;


    private List<Message> list = new ArrayList<>();
    private BaseActivity activity;
    private Callback callback;
    private int selectedMessagePosition = -1;

    public MessagesAdapter(BaseActivity activity) {
        this.activity = activity;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == MY_TEXT_VIEW) {
            return new ChatHolder(inflater.inflate(R.layout.messages_my_textview, parent, false), onClickListener());
        } else if (viewType == MY_IMAGE_VIEW) {
            return new ChatHolder(inflater.inflate(R.layout.messages_my_imageview, parent, false), onClickListener());
        } else if (viewType == USER_TEXT_VIEW) {
            return new ChatHolder(inflater.inflate(R.layout.messages_user_textview, parent, false), onClickListener());
        } else if (viewType == USERS_IMAGE_VIEW) {
            return new ChatHolder(inflater.inflate(R.layout.message_user_imageview, parent, false), onClickListener());
        } else {
            return new LoadViewHolder(inflater.inflate(R.layout.loading_view, parent, false));
        }
    }

    private ChatHolder.OnClickListener onClickListener() {
        return new ChatHolder.OnClickListener() {
            @Override
            public void onItemLongClick(int position, View view) {
                selectedMessagePosition = position;
                if (callback != null) {
                    callback.onItemLongClick(position, view);
                }
            }

            @Override
            public void onUserImageClick(int position, View view) {
                if (callback != null) {
                    callback.onUserImageClick(getItemByPosition(position).getFromUserId(), view);
                }
            }
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case MY_TEXT_VIEW:
                ((ChatHolder) holder).itemView.setLongClickable(true);
                ((ChatHolder) holder).bindMyData(getItemByPosition(position));
                break;
            case MY_IMAGE_VIEW:
                ((ChatHolder) holder).itemView.setLongClickable(true);
                ((ChatHolder) holder).bindMyImageData(getItemByPosition(position));
                break;
            case USER_TEXT_VIEW:
                ((ChatHolder) holder).itemView.setLongClickable(true);
                ((ChatHolder) holder).bindUsersData(getItemByPosition(position));
                break;
            case USERS_IMAGE_VIEW:
                ((ChatHolder) holder).itemView.setLongClickable(true);
                ((ChatHolder) holder).bindUserImageData(getItemByPosition(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public int getItemViewType(int position) {
        String userKey = getItemByPosition(position).getFromUserId();
        boolean isText = getItemByPosition(position).getText() != null;
        boolean isImage = getItemByPosition(position).getImageUrl() != null;
        if (userKey != null && userKey.equalsIgnoreCase(activity.getCurrent_uid())) {
            if (isText) {
                return MY_TEXT_VIEW;
            } else if (isImage) {
                return MY_IMAGE_VIEW;
            }
        } else if (userKey != null && !userKey.equalsIgnoreCase(activity.getCurrent_uid())) {
            if (isText) {
                return USER_TEXT_VIEW;
            } else if (isImage) {
                return USERS_IMAGE_VIEW;
            }
        }
        return list.get(position).getItemType().getTypeCode();

    }

    public void cleanSelectedPosition() {
        selectedMessagePosition = -1;
    }

    public void removeMessage() {
        list.remove(selectedMessagePosition);
        notifyItemRemoved(selectedMessagePosition);
    }

    public Message getItemByPosition(int position) {
        return list.get(position);
    }

    public void setList(List<Message> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void onItemLongClick(int position, View view);

        void onUserImageClick(String userKey, View view);
    }
}
