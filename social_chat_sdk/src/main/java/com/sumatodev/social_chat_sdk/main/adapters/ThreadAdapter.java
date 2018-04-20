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
    public static int selectedItemPosition = -1;


    public ThreadAdapter() {
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ThreadsHolder(inflater.inflate(R.layout.message_threads_list, parent, false), callBack());
    }

    private ThreadsHolder.CallBack callBack() {
        return new ThreadsHolder.CallBack() {
            @Override
            public void onItemClick(String userKey, View view) {
                if (callback != null) {
                    callback.onItemClick(userKey, view);
                }
            }

            @Override
            public void onItemLongClick(int position, View view) {
                if (callback != null) {
                    selectedItemPosition = position;
                    callback.onItemLongClick(position, view);
                }
            }
        };
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


    public void cleanSelectedPosition() {
        selectedItemPosition = -1;
    }

    public void removeItem() {
        list.remove(selectedItemPosition);
        callback.onListChanged(list.size());
        notifyItemRemoved(selectedItemPosition);
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
