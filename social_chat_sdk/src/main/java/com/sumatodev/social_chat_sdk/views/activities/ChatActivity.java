package com.sumatodev.social_chat_sdk.views.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.adapters.ChatAdpater;
import com.sumatodev.social_chat_sdk.main.listeners.OnMessageSentListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnObjectChangedListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnTaskCompleteListener;
import com.sumatodev.social_chat_sdk.main.manager.MessagesManager;
import com.sumatodev.social_chat_sdk.main.model.InputMessage;
import com.sumatodev.social_chat_sdk.main.model.Message;
import com.sumatodev.social_chat_sdk.main.model.UsersPublic;
import com.sumatodev.social_chat_sdk.main.utils.FormatterUtil;
import com.sumatodev.social_chat_sdk.main.utils.MessageInput;
import com.sumatodev.social_chat_sdk.main.utils.RoundedCornersTransform;


import cz.kinst.jakub.view.SimpleStatefulLayout;

public class ChatActivity extends BaseActivity implements MessageInput.InputListener,
        View.OnClickListener, OnMessageSentListener {

    private static final String TAG = ChatActivity.class.getSimpleName();

    public static final String USER_KEY = "MessageActivity.userKey";
    private String userKey;

    private ActionBar actionBar;
    private TextView userName_c;
    private TextView status_c;
    private ImageView userImage_c;
    private MessagesManager messagesManager;

    private SimpleStatefulLayout mStatefulLayout;
    private ActionMode actionMode;
    private ChatAdpater adpater;
    private SwipeRefreshLayout swipeContainer;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (getIntent() != null) {
            userKey = getIntent().getStringExtra(USER_KEY);
        }

        messagesManager = MessagesManager.getInstance(this);
        setupActionBar();

        MessageInput input = findViewById(R.id.input);
        input.setInputListener(this);
        recyclerView = findViewById(R.id.recyclerView);
        swipeContainer = findViewById(R.id.swipeContainer);
        mStatefulLayout = findViewById(R.id.stateful_view);


        initRecyclerView();
    }


    @Override
    public void onClick(View v) {

        if (v == userImage_c) {
            Toast.makeText(this, "open Profile", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupActionBar() {
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {

                View view = View.inflate(this, R.layout.chat_toolbar, null);
                actionBar.setCustomView(view);
                userName_c = view.findViewById(R.id.userName_c);
                status_c = view.findViewById(R.id.status_c);
                userImage_c = view.findViewById(R.id.userImage_c);

                userImage_c.setOnClickListener(this);
                messagesManager.getUsersPublicProfile(ChatActivity.this, userKey,
                        createProfileChangeListener(userName_c, userImage_c));
            }
        }
    }

    private OnObjectChangedListener<UsersPublic> createProfileChangeListener(final TextView userName, final ImageView authorImageView) {
        return new OnObjectChangedListener<UsersPublic>() {
            @Override
            public void onObjectChanged(final UsersPublic obj) {

                if (obj.getUsername() != null) {
                    userName.setText(obj.getUsername());
                }
                if (obj.getStatus() != null) {
                    status_c.setVisibility(View.VISIBLE);
                    if (obj.getStatus().isOnline) {
                        status_c.setText(R.string.isOnline);
                    } else {
                        CharSequence lastSeen = FormatterUtil.getRelativeTimeSpanString(ChatActivity.this,
                                obj.getStatus().lastSeen);

                        status_c.setText(lastSeen);
                    }
                }
                if (obj.getPhotoUrl() != null) {

                    Picasso.with(ChatActivity.this).load(obj.getPhotoUrl())
                            .placeholder(R.drawable.imageview_user_thumb)
                            .transform(new RoundedCornersTransform())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(authorImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {

                                    Picasso.with(ChatActivity.this).load(obj.getPhotoUrl())
                                            .placeholder(R.drawable.imageview_user_thumb)
                                            .transform(new RoundedCornersTransform())
                                            .into(authorImageView);
                                }
                            });
                }
            }
        };
    }

    @Override
    public boolean onSubmit(CharSequence input) {

        InputMessage inputMessage = new InputMessage(input.toString(), userKey);
        messagesManager.sendNewMessage(inputMessage, this);

        return true;
    }

    @Override
    public void onMessageSent(boolean success, String message) {
        if (success) {
            Log.d(TAG, "message send");
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void initRecyclerView() {

        mStatefulLayout.showProgress();

        adpater = new ChatAdpater(this, userKey, swipeContainer);
        adpater.setCallback(new ChatAdpater.Callback() {
            @Override
            public void onLongItemClick(View view, int postion) {
                Message selectedMessage = adpater.getItemByPosition(postion);
                startActionMode(selectedMessage);
            }

            @Override
            public void onListLoadingFinished() {
                mStatefulLayout.showContent();
            }

            @Override
            public void onAuthorClick(String authorId, View view) {
                if (hasInternetConnection()) {
                    Toast.makeText(ChatActivity.this, "image clicked", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCanceled(String message) {
                mStatefulLayout.showEmpty();
                mStatefulLayout.setEmptyText(message);
            }
        });

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adpater);
        adpater.loadFirstPage();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }


    private void startActionMode(Message selectedMessage) {
        if (actionMode != null) {
            return;
        }

        if (selectedMessage.getId() != null) {
            actionMode = startSupportActionMode(new ActionModeCallback(selectedMessage));
            ;
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {

        Message selectedMessage;
        int position;

        public ActionModeCallback(Message selectedMessage) {
            this.selectedMessage = selectedMessage;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.chat_actions_menu, menu);
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
                removeMessage(selectedMessage.getId(), mode, position);
                return true;
            } else if (id == R.id.action_copy) {
                copySelectedMessage(selectedMessage.getText());
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
        }
    }

    private void removeMessage(String messageId, final ActionMode mode, int position) {
        showProgress(R.string.deleting_message);

        messagesManager.removeMessage(messageId, userKey, new OnTaskCompleteListener() {
            @Override
            public void onTaskComplete(boolean success) {
                if (success) {
                    hideProgress();
                    mode.finish();
                }
            }
        });
    }

    private void copySelectedMessage(String messageText) {
        if (messageText != null) {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("copied to clipboard", messageText);
            if (clipboardManager != null) {
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getApplicationContext(), "copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        messagesManager.checkOnlineStatus(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        messagesManager.checkOnlineStatus(false);
        messagesManager.closeListeners(this);
    }
}
