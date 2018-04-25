package com.sumatodev.social_chat_sdk.main.adapters;

import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.adapters.holders.ChatHolder;
import com.sumatodev.social_chat_sdk.main.adapters.holders.LoadViewHolder;
import com.sumatodev.social_chat_sdk.main.enums.ItemType;
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
    private SwipeRefreshLayout swipeContainer;
    private BaseActivity mainActivity;
    private String userKey;


    public ChatAdapter(final BaseActivity activity, SwipeRefreshLayout swipeContainer, String userKey) {
        super(activity);
        this.mainActivity = activity;
        this.swipeContainer = swipeContainer;
        this.userKey = userKey;
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
            cleanSelectedPostInformation();
        } else {
            swipeContainer.setRefreshing(false);
            mainActivity.showSnackBar(R.string.internet_connection_failed);
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == ItemType.ITEM.getTypeCode()) {
            return new ChatHolder(inflater.inflate(R.layout.messages_user_textview, parent, false));
        } else {
            return new LoadViewHolder(inflater.inflate(R.layout.loading_view, parent, false));
        }
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
                        messageList.add(new Message(ItemType.LOAD));
                        notifyItemInserted(messageList.size());
                        loadNext(lastLoadedItemCreatedDate - 1);
                    } else {
                        mainActivity.showSnackBar(R.string.internet_connection_failed);
                    }
                }
            });


        }

        if (getItemViewType(position) != ItemType.LOAD.getTypeCode()) {
            ((ChatHolder) holder).bindTextData(messageList.get(position));
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
                    swipeContainer.setRefreshing(false);
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
        };

        MessagesManager.getInstance(activity).getMessageList(userKey, onMessageListChangedListener, nextItemCreatedDate);
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

    @Override
    public long getItemId(int position) {
        return getItemByPosition(position).getId().hashCode();
    }

    public interface Callback {
        void onItemClick(Message post, View view);

        void onListLoadingFinished();

        void onAuthorClick(String authorId, View view);

        void onCanceled(String message);
    }
}
