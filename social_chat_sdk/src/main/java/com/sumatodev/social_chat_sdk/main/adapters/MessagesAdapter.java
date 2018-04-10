package com.sumatodev.social_chat_sdk.main.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.adapters.holders.ChatMyHolder;
import com.sumatodev.social_chat_sdk.main.adapters.holders.ChatUserHolder;
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
            return new ChatMyHolder(inflater.inflate(R.layout.messages_my_textview, parent, false), onMyClick());
        } else if (viewType == USER_TEXT_VIEW) {
            return new ChatUserHolder(inflater.inflate(R.layout.messages_user_textview, parent, false), onUsersClick());
        } else if (viewType == MY_IMAGE_VIEW) {
            return new ChatMyHolder(inflater.inflate(R.layout.messages_my_imageview, parent, false), onMyClick());
        } else if (viewType == USERS_IMAGE_VIEW) {
            return new ChatUserHolder(inflater.inflate(R.layout.message_user_imageview, parent, false), onUsersClick());
        }
        return new LoadViewHolder(inflater.inflate(R.layout.loading_view, parent, false));
    }


    private ChatMyHolder.OnClickListener onMyClick() {
        return new ChatMyHolder.OnClickListener() {
            @Override
            public void onItemLongClick(int position, View view) {
                selectedMessagePosition = position;
                if (callback != null) {
                    callback.onItemLongClick(position, view);
                }
            }

            @Override
            public void onImageClick(int position, View view) {
                if (callback != null) {
                    callback.onImageClick(getItemByPosition(position).getImageUrl(), view);
                }
            }
        };
    }

    private ChatUserHolder.OnClickListener onUsersClick() {
        return new ChatUserHolder.OnClickListener() {
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

            @Override
            public void onImageClick(int position, View view) {
                if (callback != null) {
                    callback.onImageClick(getItemByPosition(position).getImageUrl(), view);
                }
            }
        };
    }

    @Override
    public int getItemViewType(int position) {
        Message message = getItemByPosition(position);
        if (message.getMessageType() != null) {
            if (message.getMessageType().equals("text") && message.getFromUserId().equals(activity.getCurrent_uid())) {
                return MY_TEXT_VIEW;
            } else {
                return USER_TEXT_VIEW;
            }
        } else if (message.getMessageType() != null) {
            if (message.getMessageType().equals("image") && message.getFromUserId().equals(activity.getCurrent_uid())) {
                return MY_IMAGE_VIEW;
            } else {
                return USERS_IMAGE_VIEW;
            }
        }
        return list.get(position).getItemType().getTypeCode();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = list.get(position);
        switch (holder.getItemViewType()) {
            case MY_TEXT_VIEW:
                ((ChatMyHolder) holder).itemView.setLongClickable(true);
                ((ChatMyHolder) holder).bindTextData(message);
                break;
            case USER_TEXT_VIEW:
                ((ChatUserHolder) holder).itemView.setLongClickable(true);
                ((ChatUserHolder) holder).bindTextData(message);
                break;
            case MY_IMAGE_VIEW:
                ((ChatMyHolder) holder).itemView.setLongClickable(true);
                ((ChatMyHolder) holder).bindImageData(message);
                break;
            case USERS_IMAGE_VIEW:
                ((ChatUserHolder) holder).itemView.setLongClickable(true);
                ((ChatUserHolder) holder).bindImageData(message);
                break;
        }
    }


    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
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
        callback.onListChanged(list.size());
        notifyDataSetChanged();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void onItemLongClick(int position, View view);

        void onUserImageClick(String userKey, View view);

        void onImageClick(String imageUrl, View view);

        void onListChanged(int messageCounts);
    }
}
