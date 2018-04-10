package com.sumatodev.social_chat_sdk.main.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.adapters.holders.ThreadsHolder;
import com.sumatodev.social_chat_sdk.main.model.ThreadsModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ali on 13/03/2018.
 */

public class ThreadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private List<ThreadsModel> list = new ArrayList<>();
    private Callback callback;


    public ThreadAdapter() {
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ThreadsHolder(inflater.inflate(R.layout.message_threads_list, parent, false),
                callback);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ThreadsHolder) holder).itemView.setLongClickable(true);
        ((ThreadsHolder) holder).bindData(getItemByPosition(position));
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public ThreadsModel getItemByPosition(int position) {
        return list.get(position);
    }

    public void setList(List<ThreadsModel> list) {
        this.list = list;
        callback.onListChanged(list.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public interface Callback {
        void onItemClick(String userKey, View view);

        void onItemLongClick(int position, View view);

        void onListChanged(int threadsCount);
    }
}
