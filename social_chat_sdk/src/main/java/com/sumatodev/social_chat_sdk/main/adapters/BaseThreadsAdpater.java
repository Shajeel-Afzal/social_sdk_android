package com.sumatodev.social_chat_sdk.main.adapters;

import android.support.v7.widget.RecyclerView;

import com.sumatodev.social_chat_sdk.main.model.ThreadsModel;
import com.sumatodev.social_chat_sdk.views.activities.BaseActivity;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ali on 13/03/2018.
 */

public abstract class BaseThreadsAdpater extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = BaseThreadsAdpater.class.getSimpleName();

    protected List<ThreadsModel> threadsList = new LinkedList<>();
    protected BaseActivity activity;
    protected int selectedItemPosition = -1;

    public BaseThreadsAdpater(BaseActivity activity) {
        this.activity = activity;
    }

    protected void cleanSelectedItemInformation() {
        selectedItemPosition = -1;
    }

    @Override
    public int getItemCount() {
        return threadsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return threadsList.get(position).getItemType().getTypeCode();
    }

    protected ThreadsModel getItemByPosition(int position) {
        return threadsList.get(position);
    }
}
