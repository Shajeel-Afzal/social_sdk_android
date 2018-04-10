package com.sumatodev.social_chat_sdk.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.adapters.ThreadAdapter;
import com.sumatodev.social_chat_sdk.main.listeners.OnDataChangedListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnTaskCompleteListener;
import com.sumatodev.social_chat_sdk.main.manager.MessagesManager;
import com.sumatodev.social_chat_sdk.main.model.ThreadsModel;

import java.util.List;

import cz.kinst.jakub.view.SimpleStatefulLayout;

public class ThreadsActivity extends BaseActivity {

    private static final String TAG = ThreadsActivity.class.getSimpleName();
    private static final int TIME_OUT_LOADING_THREADS = 30000;
    private SimpleStatefulLayout mStatefulLayout;
    private RecyclerView recycleView;
    private ThreadAdapter threadAdapter;
    private ActionBar actionBar;
    private boolean loadingThreadList = false;
    private MessagesManager messagesManager;
    private ActionMode actionMode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_threads);

        findViews();
        setupLinearLayout();
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Conversations");
        }

        messagesManager = MessagesManager.getInstance(this);
        initRecyclerView();
    }

    private void findViews() {
        mStatefulLayout = findViewById(R.id.stateful_view);
        recycleView = findViewById(R.id.recycler_view);
    }


    private void initRecyclerView() {
        mStatefulLayout.showProgress();

        threadAdapter = new ThreadAdapter();
        threadAdapter.setCallback(new ThreadAdapter.Callback() {
            @Override
            public void onItemClick(String userKey, View view) {
                openChatActivity(userKey);
            }

            @Override
            public void onItemLongClick(int position, View view) {
                ThreadsModel model = threadAdapter.getItemByPosition(position);
                startActionMode(model);
            }

            @Override
            public void onListChanged(int threadsCount) {
                if (threadsCount == 0) {
                    mStatefulLayout.showEmpty();
                    mStatefulLayout.setEmptyText("Empty Conversations");
                } else {
                    mStatefulLayout.showContent();
                }
            }
        });

        recycleView.setAdapter(threadAdapter);
        messagesManager.getThreadsList(this, onThreadsDataChangedListener());
    }

    private OnDataChangedListener<ThreadsModel> onThreadsDataChangedListener() {

        loadingThreadList = true;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (loadingThreadList) {
                    mStatefulLayout.showContent();
                }
            }
        }, TIME_OUT_LOADING_THREADS);

        return new OnDataChangedListener<ThreadsModel>() {
            @Override
            public void onListChanged(List<ThreadsModel> list) {
                loadingThreadList = false;
                mStatefulLayout.showContent();
                if (!list.isEmpty()) {
                    threadAdapter.setList(list);
                }
            }

            @Override
            public void onCancel(String message) {
                Toast.makeText(ThreadsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void startActionMode(ThreadsModel model) {
        if (actionMode != null) {
            return;
        }
        if (model.getThreadKey() != null) {
            actionMode = startSupportActionMode(new ActionModeCallback(model));
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {

        ThreadsModel model;
        int position;

        public ActionModeCallback(ThreadsModel model) {
            this.model = model;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.threads_action_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_delete) {
                removeConversation(model.getThreadKey(), mode);
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
        }
    }

    private void removeConversation(String threadKey, final ActionMode mode) {
        showProgress(R.string.deleting_conversation);

        messagesManager.removeConversation(threadKey, new OnTaskCompleteListener() {
            @Override
            public void onTaskComplete(boolean success) {
                if (success) {
                    hideProgress();
                    mode.finish();
                }
            }
        });
    }

    private void openChatActivity(String userKey) {
        Intent intent = new Intent(ThreadsActivity.this, ChatActivity.class);
        intent.putExtra(ChatActivity.USER_KEY, userKey);
        startActivity(intent);
    }


    private void setupLinearLayout() {

        recycleView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycleView.setLayoutManager(layoutManager);
    }

}
