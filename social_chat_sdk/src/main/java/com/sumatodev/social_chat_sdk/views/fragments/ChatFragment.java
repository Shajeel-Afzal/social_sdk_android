package com.sumatodev.social_chat_sdk.views.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.adapters.ChatAdapter;
import com.sumatodev.social_chat_sdk.main.listeners.OnMessageSentListener;
import com.sumatodev.social_chat_sdk.main.manager.MessagesManager;
import com.sumatodev.social_chat_sdk.main.model.InputMessage;
import com.sumatodev.social_chat_sdk.main.model.Message;
import com.sumatodev.social_chat_sdk.main.utils.MessageInput;
import com.sumatodev.social_chat_sdk.views.activities.BaseActivity;

import cz.kinst.jakub.view.SimpleStatefulLayout;

public class ChatFragment extends BaseActivity implements MessageInput.InputListener {


    public static final String USER_KEY = "ChatFragment.userKey";
    private String userKey;

    private SimpleStatefulLayout mStateful_view;
    private SwipeRefreshLayout swipeContainer;
    private RecyclerView recyclerView;

    private ChatAdapter adapter;
    private MessagesManager messagesManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_chat);
        fillViews();

        if (getIntent() != null) {
            userKey = getIntent().getStringExtra(USER_KEY);
        }

        messagesManager = MessagesManager.getInstance(this);

        MessageInput input = findViewById(R.id.input);
        input.setInputListener(this);

        intiRecylerView();
    }


    @Override
    public boolean onSubmit(CharSequence input) {

        InputMessage inputMessage = new InputMessage(input.toString(), userKey);
        messagesManager.sendNewMessage(inputMessage, new OnMessageSentListener() {
            @Override
            public void onMessageSent(boolean success, String message) {
                refreshMessageList();
            }
        });

        return true;
    }

    private void fillViews() {
        mStateful_view = findViewById(R.id.stateful_view);
        swipeContainer = findViewById(R.id.swipeContainer);
        recyclerView = findViewById(R.id.recycler_view);
    }

    private void refreshMessageList() {
        adapter.loadFirstPage();
        if (adapter.getItemCount() > 0) {
            recyclerView.scrollToPosition(0);
        }
    }

    private void intiRecylerView() {

        adapter = new ChatAdapter(ChatFragment.this, swipeContainer, userKey);
        adapter.setCallback(new ChatAdapter.Callback() {
            @Override
            public void onItemClick(Message post, View view) {

            }

            @Override
            public void onListLoadingFinished() {
                mStateful_view.showContent();
            }

            @Override
            public void onAuthorClick(String authorId, View view) {

            }

            @Override
            public void onCanceled(String message) {
                mStateful_view.showEmpty();
            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.loadFirstPage();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

    }


}
