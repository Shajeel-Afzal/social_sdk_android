package com.sumatodev.social_chat_sdk.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.adapters.ThreadAdapter;
import com.sumatodev.social_chat_sdk.main.model.ThreadsModel;

import cz.kinst.jakub.view.SimpleStatefulLayout;

public class ThreadsActivity extends BaseActivity {

    private static final String TAG = ThreadsActivity.class.getSimpleName();
    private SimpleStatefulLayout mStatefulLayout;
    private RecyclerView recycleView;
    private ThreadAdapter threadAdapter;
    private ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_threads);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Conversations");
        }
        findViews();

        setupLinearLayout();

        initRecyclerView();
    }

    private void findViews() {
        mStatefulLayout = findViewById(R.id.stateful_view);
        recycleView = findViewById(R.id.recycler_view);
    }


    private void initRecyclerView() {
        //mStatefulLayout.showProgress();

        threadAdapter = new ThreadAdapter(this, null);
        threadAdapter.setCallback(new ThreadAdapter.Callback() {
            @Override
            public void onItemClick(ThreadsModel threadsModel, View view) {
                if (threadsModel.getThreadKey() != null) {
                    openChatActivity(threadsModel);
                }
            }

            @Override
            public void onItemLongClick(ThreadsModel threadsModel, View view) {

            }
        });
        recycleView.setAdapter(threadAdapter);
        threadAdapter.loadFirstPage();
    }

    private void openChatActivity(ThreadsModel threadsModel) {
        Intent intent = new Intent(ThreadsActivity.this, MessageActivity.class);
        intent.putExtra(MessageActivity.USER_KEY, threadsModel.getThreadKey());
        startActivity(intent);
    }


    private void setupLinearLayout() {

        recycleView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recycleView.setLayoutManager(layoutManager);
    }

}
