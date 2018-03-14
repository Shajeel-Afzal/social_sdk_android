package com.sumatodev.social_chat_sdk.views.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.sumatodev.chatkit.messages.MessageInput;
import com.sumatodev.chatkit.messages.MessagesList;
import com.sumatodev.chatkit.messages.MessagesListAdapter;
import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.data.model.InputMessage;
import com.sumatodev.social_chat_sdk.main.data.model.Message;
import com.sumatodev.social_chat_sdk.main.listeners.OnMessageChangedListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnMessageSentListener;
import com.sumatodev.social_chat_sdk.main.utils.AppUtils;

public class ChatActivity extends ChatBaseActivity implements MessageInput.InputListener,
        MessageInput.AttachmentsListener, OnMessageSentListener {

    private static final String TAG = ChatActivity.class.getSimpleName();

    public static final String USER_KEY = "MessageActivity.userKey";
    private String userKey;
    private MessagesList messagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (getIntent() != null) {
            userKey = getIntent().getStringExtra(USER_KEY);
        }

        this.messagesList = findViewById(R.id.messagesList);
        initAdapter();

        MessageInput input = findViewById(R.id.input);
        input.setInputListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        OnMessageChangedListener<Message> onMessageChangedListener = new OnMessageChangedListener<Message>() {
            @Override
            public void OnListChanged(Message message) {
                messagesAdapter.addToStart(message, true);
            }

            @Override
            public void onCancel(String message) {

            }
        };

        messagesManager.getChatList(userKey, onMessageChangedListener);
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
            // Toast.makeText(this, "message send", Toast.LENGTH_SHORT).show();
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
                                message.getUser().getName() + " avatar click",
                                false);
                    }
                });
        this.messagesList.setAdapter(super.messagesAdapter);
    }
}
