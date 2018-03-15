package com.sumatodev.social_chat_sdk.views.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.chatkit_ui.messages.MessageInput;
import com.sumatodev.social_chat_sdk.chatkit_ui.messages.MessagesList;
import com.sumatodev.social_chat_sdk.chatkit_ui.messages.MessagesListAdapter;
import com.sumatodev.social_chat_sdk.main.data.model.InputMessage;
import com.sumatodev.social_chat_sdk.main.data.model.Message;
import com.sumatodev.social_chat_sdk.main.data.model.MessageListResult;
import com.sumatodev.social_chat_sdk.main.listeners.OnMessageChangedListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnMessageListChangedListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnMessageSentListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnObjectChangedListener;
import com.sumatodev.social_chat_sdk.main.model.UsersPublic;
import com.sumatodev.social_chat_sdk.main.utils.AppUtils;
import com.sumatodev.social_chat_sdk.main.utils.RoundedCornersTransform;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends ChatBaseActivity implements MessageInput.InputListener,
        MessageInput.AttachmentsListener, OnMessageSentListener, View.OnClickListener, MessagesListAdapter.OnLoadMoreListener {

    private static final String TAG = ChatActivity.class.getSimpleName();

    public static final String USER_KEY = "MessageActivity.userKey";
    private String userKey;
    private MessagesList messagesList;

    private ActionBar actionBar;
    private TextView userName_c;
    private TextView status_c;
    private ImageView userImage_c;


    private long lastLoadedItemCreatedDate;
    private boolean isMoreDataAvailable = true;
    private List<Message> messageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (getIntent() != null) {
            userKey = getIntent().getStringExtra(USER_KEY);
        }

        setupActionBar();

        this.messagesList = findViewById(R.id.messagesList);
        initAdapter();

        MessageInput input = findViewById(R.id.input);
        input.setInputListener(this);


        loadList();
    }

    protected void loadList() {

        OnMessageChangedListener<Message> onMessageChangedListener = new OnMessageChangedListener<Message>() {
            @Override
            public void OnListChanged(Message message) {
                if (message != null) {
                    messagesAdapter.addToStart(message, true);
                    lastLoadedItemCreatedDate = message.getCreatedAt().getTime();
                }
            }

            @Override
            public void onCancel(String message) {

            }
        };
        messagesManager.getChatList(userKey, onMessageChangedListener);
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

                userName_c.setOnClickListener(this);
                super.messagesManager.getUsersPublicProfile(userKey, createProfileChangeListener(userName_c, userImage_c));
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
                if (obj.getPhotoUrl() != null) {

                    Picasso.with(ChatActivity.this).load(obj.getPhotoUrl())
                            .placeholder(R.drawable.imageview_user_thumb)
                            .transform(new RoundedCornersTransform())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(userImage_c, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {

                                    Picasso.with(ChatActivity.this).load(obj.getPhotoUrl())
                                            .placeholder(R.drawable.imageview_user_thumb)
                                            .transform(new RoundedCornersTransform())
                                            .into(userImage_c);
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

        //super.messagesAdapter.addToStart(input.toString(), true);
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

    @Override
    public void onAddAttachments() {

    }

    private void initAdapter() {
        super.messagesAdapter = new MessagesListAdapter<>(getCurrent_uid(), super.imageLoader);
        super.messagesAdapter.enableSelectionMode(this);
        super.messagesAdapter.setLoadMoreListener(this);
        super.messagesAdapter.registerViewClickListener(R.id.messageUserAvatar,
                new MessagesListAdapter.OnMessageViewClickListener<Message>() {
                    @Override
                    public void onMessageViewClick(View view, Message message) {
                        AppUtils.showToast(getApplicationContext(),
                                message.getUser().getId() + " avatar click",
                                false);
                    }
                });
        this.messagesList.setAdapter(super.messagesAdapter);
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        Log.d(TAG, "page: " + page + " totalItems: " + totalItemsCount);

        loadMessages();

    }


    protected void loadMessages() {
        new Handler().postDelayed(new Runnable() { //imitation of internet connection
            @Override
            public void run() {
                //ArrayList<Message> messages = MessagesFixtures.getMessages(lastLoadedDate);
                //lastLoadedDate = messages.get(messages.size() - 1).getCreatedAt();
                //messagesAdapter.addToEnd(messages, false);
                Toast.makeText(ChatActivity.this, "load more called", Toast.LENGTH_SHORT).show();
                messagesManager.getMessages(userKey, onMessageListChangedListener,
                        lastLoadedItemCreatedDate - 1);

            }
        }, 1000);
    }

    OnMessageListChangedListener<Message> onMessageListChangedListener = new OnMessageListChangedListener<Message>() {
        @Override
        public void onListChanged(MessageListResult result) {

            List<Message> messages = result.getMessages();
            if (!messages.isEmpty()) {
                messagesAdapter.addToEnd(messages, false);
            }
        }

        @Override
        public void onCancel(String message) {

        }
    };
}
