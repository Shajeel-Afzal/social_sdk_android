package com.sumatodev.social_chat_sdk.views.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.chat_ui.messages.MessageInput;
import com.sumatodev.social_chat_sdk.chat_ui.messages.MessagesList;
import com.sumatodev.social_chat_sdk.chat_ui.messages.MessagesListAdapter;
import com.sumatodev.social_chat_sdk.main.model.Message;
import com.sumatodev.social_chat_sdk.main.utils.AppUtils;
import com.sumatodev.social_chat_sdk.main.utils.MessagesFixtures;
import com.sumatodev.social_chat_sdk.views.activities.MessageActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends BaseMessagesFragment implements MessageInput.InputListener, MessageInput.AttachmentsListener {

    public static void open(Context context) {
        context.startActivity(new Intent(context, MessageActivity.class));
    }

    private MessagesList messagesList;

    public MessageFragment() {
        // Required empty public constructor
    }

    public static MessageFragment newInstance() {

        Bundle args = new Bundle();
        MessageFragment fragment = new MessageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        this.messagesList = view.findViewById(R.id.messagesList);
        initAdapter();

        MessageInput input = view.findViewById(R.id.input);
        input.setInputListener(this);

        return view;
    }

    @Override
    public boolean onSubmit(CharSequence input) {
        super.messagesAdapter.addToStart(
                MessagesFixtures.getTextMessage(input.toString()), true);
        return true;
    }

    @Override
    public void onAddAttachments() {
        super.messagesAdapter.addToStart(
                MessagesFixtures.getImageMessage(), true);
    }

    private void initAdapter() {
        super.messagesAdapter = new MessagesListAdapter<>(super.senderId, super.imageLoader);
        super.messagesAdapter.enableSelectionMode(this);
        super.messagesAdapter.setLoadMoreListener(this);
        super.messagesAdapter.registerViewClickListener(R.id.messageUserAvatar,
                new MessagesListAdapter.OnMessageViewClickListener<Message>() {
                    @Override
                    public void onMessageViewClick(View view, Message message) {
                        AppUtils.showToast(getContext(),
                                message.getUser().getName() + " avatar click",
                                false);
                    }
                });
        this.messagesList.setAdapter(super.messagesAdapter);
    }
}
