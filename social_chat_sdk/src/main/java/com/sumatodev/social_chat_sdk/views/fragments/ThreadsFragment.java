package com.sumatodev.social_chat_sdk.views.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.adapters.ThreadAdapter;
import com.sumatodev.social_chat_sdk.main.listeners.OnDataChangedListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnTaskCompleteListener;
import com.sumatodev.social_chat_sdk.main.manager.MessagesManager;
import com.sumatodev.social_chat_sdk.main.model.ThreadsModel;
import com.sumatodev.social_chat_sdk.views.activities.ChatActivity;

import org.michaelbel.bottomsheet.BottomSheet;

import java.util.List;

import cz.kinst.jakub.view.SimpleStatefulLayout;

public class ThreadsFragment extends BaseFragment {


    private static final String TAG = ThreadsFragment.class.getSimpleName();
    private SimpleStatefulLayout mStatefulLayout;
    private RecyclerView recycleView;

    private ActionBar actionBar;
    private LinearLayoutManager layoutManager;
    private ThreadAdapter threadAdapter;
    private MessagesManager messagesManager;
    private Activity activity;

    public ThreadsFragment() {
    }

    public static ThreadsFragment newInstance() {

        Bundle args = new Bundle();
        ThreadsFragment fragment = new ThreadsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            activity = getActivity();
        }
        actionBar = getActionBar();
        messagesManager = MessagesManager.getInstance(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_threads, container, false);
        findViews(view);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.conversations);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupLinearLayout();

        if (hasInternetConnection()) {
            loadThreadsList();
        } else {
            mStatefulLayout.showOffline();
        }

    }

    private void findViews(View view) {
        mStatefulLayout = view.findViewById(R.id.stateful_view);
        recycleView = view.findViewById(R.id.recycler_view);
    }

    private void loadThreadsList() {
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
                showBottomMenu(model);
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
        messagesManager.getThreadsList(activity, threadsModelOnDataChangedListener());

    }


    OnDataChangedListener<ThreadsModel> threadsModelOnDataChangedListener() {

        return new OnDataChangedListener<ThreadsModel>() {
            @Override
            public void onListChanged(List<ThreadsModel> list) {
                threadAdapter.setList(list);
                mStatefulLayout.showContent();

            }

            @Override
            public void inEmpty(Boolean empty) {
                if (empty) {
                    mStatefulLayout.showEmpty();
                } else {
                    mStatefulLayout.showEmpty();
                }
            }

            @Override
            public void onCancel(String message) {
                mStatefulLayout.showEmpty();
                mStatefulLayout.setEmptyText(message);
            }

        };
    }

    private void showBottomMenu(final ThreadsModel model) {

        BottomSheet.Builder builder = new BottomSheet.Builder(activity);
        builder.setMenu(R.menu.threads_action_menu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        removeConversation(model);
                        break;
                    case 1:
                        dialog.dismiss();
                        break;
                }
            }
        }).show();

    }

    private void removeConversation(final ThreadsModel model) {
        showProgress(R.string.deleting_conversation);

        messagesManager.removeConversation(model.getThreadKey(), new OnTaskCompleteListener() {
            @Override
            public void onTaskComplete(boolean success) {
                if (success) {
                    hideProgress();
                }
            }
        });
    }

    private void setupLinearLayout() {
        layoutManager = new LinearLayoutManager(activity);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recycleView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        recycleView.setLayoutManager(layoutManager);
    }


    private void openChatActivity(String userKey) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(ChatActivity.USER_KEY, userKey);
        startActivity(intent);
    }
}
