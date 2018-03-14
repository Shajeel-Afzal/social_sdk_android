package com.sumatodev.social_chat_sdk.views.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.sumatodev.chatkit.commons.ImageLoader;
import com.sumatodev.chatkit.messages.MessagesListAdapter;
import com.sumatodev.social_chat_sdk.main.data.model.Message;
import com.sumatodev.social_chat_sdk.main.manager.MessagesManager;

import java.util.Date;

/**
 * Created by Ali on 14/03/2018.
 */

public class ChatBaseActivity extends BaseActivity
        implements MessagesListAdapter.SelectionListener,
        MessagesListAdapter.OnLoadMoreListener  {

    private static final String TAG = ChatActivity.class.getSimpleName();

    private static final int TOTAL_MESSAGES_COUNT = 20;

    protected ImageLoader imageLoader;
    protected MessagesListAdapter<Message> messagesAdapter;

    private Menu menu;
    private int selectionCount;
    private Date lastLoadedDate;
    protected MessagesManager messagesManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        messagesManager = MessagesManager.getInstance(ChatBaseActivity.this);

        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Picasso.with(ChatBaseActivity.this).load(url).into(imageView);
            }
        };
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {

    }

    @Override
    public void onSelectionChanged(int count) {

    }
}
