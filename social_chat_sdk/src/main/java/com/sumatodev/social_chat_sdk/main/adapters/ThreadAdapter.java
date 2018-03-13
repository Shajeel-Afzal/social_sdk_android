package com.sumatodev.social_chat_sdk.main.adapters;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.adapters.holders.LoadViewHolder;
import com.sumatodev.social_chat_sdk.main.adapters.holders.ThreadsHolder;
import com.sumatodev.social_chat_sdk.main.enums.ItemType;
import com.sumatodev.social_chat_sdk.main.listeners.OnThreadsListChangedListener;
import com.sumatodev.social_chat_sdk.main.manager.DatabaseHelper;
import com.sumatodev.social_chat_sdk.main.model.ThreadListResult;
import com.sumatodev.social_chat_sdk.main.model.ThreadsModel;
import com.sumatodev.social_chat_sdk.views.activities.ThreadsActivity;

import java.util.List;

/**
 * Created by Ali on 13/03/2018.
 */

public class ThreadAdapter extends BaseThreadsAdpater {


    private boolean isLoading = false;
    private boolean isMoreDataAvailable = true;
    private long lastLoadedItemCreatedDate;
    private SwipeRefreshLayout swipeContainer;
    private ThreadsActivity threadsActivity;
    private Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public ThreadAdapter(ThreadsActivity activity, SwipeRefreshLayout swipeContainer) {
        super(activity);
        this.threadsActivity = activity;
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
            cleanSelectedItemInformation();
        } else {
            swipeContainer.setRefreshing(false);
            threadsActivity.showSnackBar(R.string.internet_connection_failed);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == ItemType.ITEM.getTypeCode()) {
            return new ThreadsHolder(inflater.inflate(R.layout.message_threads_list, parent, false), onClickListener);
        } else {
            return new LoadViewHolder(inflater.inflate(R.layout.loading_view, parent, false));
        }
    }

    ThreadsHolder.OnClickListener onClickListener = new ThreadsHolder.OnClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            if (callback!=null){
                selectedItemPosition = position;
                callback.onItemClick(getItemByPosition(position),view);
            }
        }

        @Override
        public void onItemLongPressed(int position, View view) {

        }
    };

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading) {
            android.os.Handler mHandler = activity.getWindow().getDecorView().getHandler();
            mHandler.post(new Runnable() {
                public void run() {
                    //change adapter contents
                    if (activity.hasInternetConnection()) {
                        isLoading = true;
                        threadsList.add(new ThreadsModel(ItemType.LOAD));
                        notifyItemInserted(threadsList.size());
                        loadNext();
                    } else {
                        threadsActivity.showSnackBar(R.string.internet_connection_failed);
                    }
                }
            });


        }

        if (getItemViewType(position) != ItemType.LOAD.getTypeCode()) {
            ((ThreadsHolder) holder).bindData(threadsList.get(position));
        }
    }

    private void addList(List<ThreadsModel> list) {
        this.threadsList.addAll(list);
        notifyDataSetChanged();
        isLoading = false;
    }

    public void loadFirstPage() {
        loadNext();
    }

    private void loadNext() {


        OnThreadsListChangedListener<ThreadsModel> onThreadDataChangedListener = new OnThreadsListChangedListener<ThreadsModel>() {
            @Override
            public void onListChanged(ThreadListResult result) {
                lastLoadedItemCreatedDate = result.getLastItemCreatedDate();
                isMoreDataAvailable = result.isMoreDataAvailable();
                List<ThreadsModel> list = result.getThreads();

                if (!list.isEmpty()) {
                    addList(list);

                } else {
                    isLoading = false;
                }

            }

            @Override
            public void onCanceled(String message) {

            }
        };


        DatabaseHelper.getInstance(activity).getThreadsList(onThreadDataChangedListener);
    }

    private void hideProgress() {
        if (!threadsList.isEmpty() && getItemViewType(threadsList.size() - 1) == ItemType.LOAD.getTypeCode()) {
            threadsList.remove(threadsList.size() - 1);
            notifyItemRemoved(threadsList.size() - 1);
        }
    }

    public void removeSelectedPost() {
        threadsList.remove(selectedItemPosition);
        notifyItemRemoved(selectedItemPosition);
    }

    @Override
    public long getItemId(int position) {
        return getItemByPosition(position).getThreadKey().hashCode();
    }

    public interface Callback {
        void onItemClick(ThreadsModel threadsModel, View view);

        void onItemLongClick(ThreadsModel threadsModel, View view);
    }
}
