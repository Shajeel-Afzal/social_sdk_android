package com.sumatodev.social_chat_sdk.main.adapters;

import android.support.v4.widget.SwipeRefreshLayout;
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
import com.sumatodev.social_chat_sdk.main.listeners.OnMessageListChangedListener;
import com.sumatodev.social_chat_sdk.main.manager.MessagesManager;
import com.sumatodev.social_chat_sdk.main.model.Message;
import com.sumatodev.social_chat_sdk.main.model.MessageListResult;
import com.sumatodev.social_chat_sdk.main.utils.PreferencesUtil;
import com.sumatodev.social_chat_sdk.views.activities.ChatActivity;

import java.util.List;

/**
 * Created by Ali on 12/03/2018.
 */

public class ChatAdpater extends ChatBaseAdapter {

    private static final String TAG = ChatAdpater.class.getName();
    private final static int MY_VIEW = 0;
    private final static int USER_VIEW = 1;

    private String userKey;

    private Callback callback;
    private boolean isLoading = false;
    private boolean isMoreDataAvailable = true;
    private long lastLoadedItemCreatedDate;
    private SwipeRefreshLayout swipeContainer;
    private ChatActivity chatActivity;

    public ChatAdpater(ChatActivity activity, String userKey, SwipeRefreshLayout swipeContainer) {
        super(activity);
        this.chatActivity = activity;
        this.userKey = userKey;
        this.swipeContainer = swipeContainer;
        initRefreshLayout();
        setHasStableIds(true);
    }

    private void initRefreshLayout() {
        if (swipeContainer != null) {
            this.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    onRefreshAction();
                }
            });
        }
    }

    private void onRefreshAction() {
        if (activity.hasInternetConnection()) {
            loadFirstPage();
            cleanSelectedMessageInformation();
        } else {
            swipeContainer.setRefreshing(false);
            chatActivity.showSnackBar(R.string.internet_connection_failed);
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == MY_VIEW) {
            return new ChatMyHolder(inflater.inflate(R.layout.messages_my_view, parent, false), callback);
        } else if (viewType == USER_VIEW) {
            return new ChatUserHolder(inflater.inflate(R.layout.messages_user_view, parent, false),
                    callback);
        }
        return new LoadViewHolder(inflater.inflate(R.layout.loading_view, parent, false));

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading) {
            android.os.Handler mHandler = activity.getWindow().getDecorView().getHandler();
            mHandler.post(new Runnable() {
                public void run() {
                    //change adapter contents
                    if (activity.hasInternetConnection()) {
                        isLoading = true;
                        messageList.add(new Message(ItemType.LOAD));
                        notifyItemInserted(messageList.size());
                        loadNext(lastLoadedItemCreatedDate - 1);
                    } else {
                        chatActivity.showSnackBar(R.string.internet_connection_failed);
                    }
                }
            });
        }

        if (getItemViewType(position) != ItemType.LOAD.getTypeCode()) {
            if (holder.getItemViewType() == MY_VIEW) {
                ((ChatMyHolder) holder).textLayout.setLongClickable(true);
                ((ChatMyHolder) holder).setData(messageList.get(position));
            } else if (holder.getItemViewType() == USER_VIEW) {
                ((ChatUserHolder) holder).textLayout.setLongClickable(true);
                ((ChatUserHolder) holder).bindData(messageList.get(position));
            }
            Log.d(TAG, "index: " + position + " id: " + messageList.get(position).getId());
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

        if (!PreferencesUtil.isPostWasLoadedAtLeastOnce(chatActivity) && !activity.hasInternetConnection()) {
            activity.showSnackBar(R.string.internet_connection_failed);
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
                    swipeContainer.setRefreshing(false);
                }

                hideProgress();

                if (!list.isEmpty()) {
                    addList(list);
                    if (!PreferencesUtil.isPostWasLoadedAtLeastOnce(activity)) {
                        PreferencesUtil.setPostWasLoadedAtLeastOnce(activity, true);
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
        };

        MessagesManager.getInstance(activity).getMessageList(activity, userKey, onMessageListChangedListener, nextItemCreatedDate);
    }

    private void hideProgress() {
        if (!messageList.isEmpty() && getItemViewType(messageList.size() - 1) == ItemType.LOAD.getTypeCode()) {
            messageList.remove(messageList.size() - 1);
            notifyItemRemoved(messageList.size() - 1);
        }
    }

    public void removeSelectedPost() {
        messageList.remove(selectedMessagePosition);
        notifyItemRemoved(selectedMessagePosition);
    }

    @Override
    public long getItemId(int position) {
        return getItemByPosition(position).getId().hashCode();
    }

    public interface Callback {
        void onLongItemClick(View view, int postion);

        void onListLoadingFinished();

        void onAuthorClick(String authorId, View view);

        void onCanceled(String message);

        void onImageClick(String imageUrl);
    }
}
