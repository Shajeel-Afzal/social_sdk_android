package com.sumatodev.social_chat_sdk.main.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.adapters.holders.ChatMyHolder;
import com.sumatodev.social_chat_sdk.main.adapters.holders.ChatUserHolder;
import com.sumatodev.social_chat_sdk.main.adapters.holders.LoadViewHolder;
import com.sumatodev.social_chat_sdk.main.enums.ItemType;
import com.sumatodev.social_chat_sdk.main.enums.MessageType;
import com.sumatodev.social_chat_sdk.main.listeners.OnMessageListChangedListener;
import com.sumatodev.social_chat_sdk.main.manager.MessagesManager;
import com.sumatodev.social_chat_sdk.main.model.Message;
import com.sumatodev.social_chat_sdk.main.model.MessageListResult;
import com.sumatodev.social_chat_sdk.main.utils.PreferencesUtil;
import com.sumatodev.social_chat_sdk.views.activities.BaseActivity;

import java.util.List;

public class ChatAdapter extends ChatBaseAdapter {

    public static final String TAG = ChatAdapter.class.getSimpleName();

    private Callback callback;
    private boolean isLoading = false;
    private boolean isMoreDataAvailable = true;
    private long lastLoadedItemCreatedDate;
    private BaseActivity mainActivity;
    private String userKey;


    public ChatAdapter(final BaseActivity activity, String userKey) {
        super(activity);
        this.mainActivity = activity;
        this.userKey = userKey;
        setHasStableIds(true);
    }


    public void setCallback(Callback callback) {
        this.callback = callback;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == MessageType.SENT.getMessageType()) {
            return new ChatMyHolder(inflater.inflate(R.layout.messages_my_textview, parent, false),
                    onMyHolderClickListener());
        } else if (viewType == MessageType.RECEIVE.getMessageType()) {
            return new ChatUserHolder(inflater.inflate(R.layout.messages_user_textview, parent, false),
                    onUserHolderClickListener());
        } else {
            return new LoadViewHolder(inflater.inflate(R.layout.loading_view, parent, false));
        }
    }

    private ChatMyHolder.OnClickListener onMyHolderClickListener() {
        return new ChatMyHolder.OnClickListener() {
            @Override
            public void onItemLongClick(int position, View view) {
                if (callback != null) {
                    selectedMessagePosition = position;
                    callback.onItemLongClick(position, view);
                }
            }

            @Override
            public void onImageClick(int position, View view) {

            }
        };
    }

    private ChatUserHolder.OnClickListener onUserHolderClickListener() {
        return new ChatUserHolder.OnClickListener() {
            @Override
            public void onItemLongClick(int position, View view) {
                if (callback != null) {
                    selectedMessagePosition = position;
                    callback.onItemLongClick(position, view);
                }
            }

            @Override
            public void onUserImageClick(int position, View view) {
                if (callback != null) {
                    callback.onAuthorClick(getItemByPosition(position).getFromUserId(), view);
                }
            }

            @Override
            public void onImageClick(int position, View view) {

            }
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading) {
            android.os.Handler mHandler = activity.getWindow().getDecorView().getHandler();
            mHandler.post(new Runnable() {
                public void run() {
                    //change adapter contents
                    if (activity.hasInternetConnection()) {
                        isLoading = true;
//                        messageList.add(new Message(ItemType.LOAD));
                        notifyItemInserted(messageList.size());
                        loadNext(lastLoadedItemCreatedDate - 1);
                    } else {
                        mainActivity.showSnackBar(R.string.internet_connection_failed);
                    }
                }
            });


        }

        Log.d(TAG, "Item: " + position);
        if (getItemViewType(position) == MessageType.SENT.getMessageType()) {
            ((ChatMyHolder) holder).itemView.setLongClickable(true);
            ((ChatMyHolder) holder).bindTextData(messageList.get(position));
        } else if (getItemViewType(position) == MessageType.RECEIVE.getMessageType()) {
            ((ChatUserHolder) holder).itemView.setLongClickable(true);
            ((ChatUserHolder) holder).bindTextData(messageList.get(position));
        }
    }

    private void addList(List<Message> list) {
        this.messageList.addAll(list);
        notifyDataSetChanged();
        isLoading = false;
    }

    public void loadFirstPage() {
        loadNext(0);
    }


    private void loadNext(final long nextItemCreatedDate) {

        if (!PreferencesUtil.isMessageWasLoadedAtLeastOnce(mainActivity) && !activity.hasInternetConnection()) {
            mainActivity.showSnackBar(R.string.internet_connection_failed);
            hideProgress();
            callback.onListLoadingFinished();
            return;
        }

        OnMessageListChangedListener<Message> onMessageListChangedListener = new OnMessageListChangedListener<Message>() {
            @Override
            public void onListChanged(MessageListResult result) {
                lastLoadedItemCreatedDate = result.getLastItemCreatedDate();
                isMoreDataAvailable = result.isMoreDataAvailable();
                List<Message> list = result.getMessages();

                if (nextItemCreatedDate == 0) {
                    messageList.clear();
                    notifyDataSetChanged();
                }

                hideProgress();

                if (!list.isEmpty()) {
                    addList(list);

                    if (!PreferencesUtil.isMessageWasLoadedAtLeastOnce(mainActivity)) {
                        PreferencesUtil.setMessageWasLoadedAtLeastOnce(mainActivity, true);
                    }
                } else {
                    isLoading = false;
                }

                callback.onListLoadingFinished();
            }

            @Override
            public void onCanceled(String message) {
                callback.onCanceled(message);
            }

            @Override
            public void isEmpty(boolean isEmpty) {
                callback.isEmpty(isEmpty);
            }
        };

        MessagesManager.getInstance(activity).getMessageList(activity, userKey, onMessageListChangedListener, nextItemCreatedDate);
    }


    private void hideProgress() {
        if (!messageList.isEmpty() && getItemViewType(messageList.size() - 1) == ItemType.LOAD.getTypeCode()) {
            messageList.remove(messageList.size() - 1);
            notifyItemRemoved(messageList.size() - 1);
        }
    }

    public void removeSelectedMessage() {
        messageList.remove(selectedMessagePosition);
        notifyItemRemoved(selectedMessagePosition);
    }

    public Message getMessageByPosition(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItemByPosition(position).getId().hashCode();
    }

    public interface Callback {
        void onItemLongClick(int position, View view);

        void onListLoadingFinished();

        void onAuthorClick(String authorId, View view);

        void onCanceled(String message);

        void isEmpty(boolean isEmpty);
    }
}
