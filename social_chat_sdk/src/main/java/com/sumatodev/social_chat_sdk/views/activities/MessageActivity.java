package com.sumatodev.social_chat_sdk.views.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.chat_ui.messages.MessageInput;
import com.sumatodev.social_chat_sdk.chat_ui.messages.MessagesList;
import com.sumatodev.social_chat_sdk.chat_ui.messages.MessagesListAdapter;
import com.sumatodev.social_chat_sdk.main.model.Message;
import com.sumatodev.social_chat_sdk.main.utils.AppUtils;
import com.sumatodev.social_chat_sdk.main.utils.MessagesFixtures;

public class MessageActivity extends BaseMessagesActivity implements MessageInput.InputListener {

    public static void open(Context context) {
        context.startActivity(new Intent(context, MessageActivity.class));
    }

    public static final String USER_KEY = "MessageActivity.userKey";
    private String userKey = null;
    private MessagesList messagesList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_message);

        if (getIntent() != null) {
            userKey = getIntent().getStringExtra(USER_KEY);
        }


        this.messagesList = findViewById(R.id.messagesList);
        initAdapter();

        MessageInput input = findViewById(R.id.input);
        input.setInputListener(this);
    }

    @Override
    public boolean onSubmit(CharSequence input) {
        //super.messagesAdapter.addToStart(MessagesFixtures.getTextMessage(input.toString()), true);


        return true;
    }

    private void initAdapter() {
        super.messagesAdapter = new MessagesListAdapter<>(super.senderId, super.imageLoader);
        super.messagesAdapter.enableSelectionMode(this);
        super.messagesAdapter.setLoadMoreListener(this);
        super.messagesAdapter.registerViewClickListener(R.id.messageUserAvatar,
                new MessagesListAdapter.OnMessageViewClickListener<Message>() {
                    @Override
                    public void onMessageViewClick(View view, Message message) {
                        AppUtils.showToast(MessageActivity.this,
                                message.getUser().getName() + " avatar click",
                                false);
                    }
                });
        this.messagesList.setAdapter(super.messagesAdapter);
    }
}
