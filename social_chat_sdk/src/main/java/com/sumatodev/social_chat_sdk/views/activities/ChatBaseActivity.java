package com.sumatodev.social_chat_sdk.views.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.chatkit_ui.commons.ImageLoader;
import com.sumatodev.social_chat_sdk.chatkit_ui.messages.MessagesListAdapter;
import com.sumatodev.social_chat_sdk.main.data.model.Message;
import com.sumatodev.social_chat_sdk.main.listeners.OnObjectChangedListener;
import com.sumatodev.social_chat_sdk.main.manager.MessagesManager;
import com.sumatodev.social_chat_sdk.main.model.UsersPublic;
import com.sumatodev.social_chat_sdk.main.utils.AppUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Ali on 14/03/2018.
 */

public class ChatBaseActivity extends BaseActivity
        implements MessagesListAdapter.SelectionListener{

    private static final String TAG = ChatActivity.class.getSimpleName();

    private static final int TOTAL_MESSAGES_COUNT = 10;

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
            public void loadImage(ImageView imageView, String userKey) {
                messagesManager.getUsersPublicProfile(userKey, createProfileChangeListener(imageView));
            }
        };

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.chat_actions_menu, menu);
        onSelectionChanged(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_delete) {
            messagesAdapter.deleteSelectedMessages();

        } else if (i == R.id.action_copy) {
            messagesAdapter.copySelectedMessagesText(this, getMessageStringFormatter(), true);
            AppUtils.showToast(this, R.string.copied_message, true);

        }
        return true;
    }

    @Override
    public void onSelectionChanged(int count) {
        this.selectionCount = count;
        menu.findItem(R.id.action_delete).setVisible(count > 0);
        menu.findItem(R.id.action_copy).setVisible(count > 0);
    }


    private MessagesListAdapter.Formatter<Message> getMessageStringFormatter() {
        return new MessagesListAdapter.Formatter<Message>() {
            @Override
            public String format(Message message) {
                String createdAt = new SimpleDateFormat("MMM d, EEE 'at' h:mm a", Locale.getDefault())
                        .format(message.getCreatedAt());

                String text = message.getText();
                if (text == null) text = "[attachment]";

                return String.format(Locale.getDefault(), "%s: %s (%s)",
                        message.getUser().getName(), text, createdAt);
            }
        };
    }

    private OnObjectChangedListener<UsersPublic> createProfileChangeListener(final ImageView authorImageView) {
        return new OnObjectChangedListener<UsersPublic>() {
            @Override
            public void onObjectChanged(final UsersPublic obj) {
                if (obj.getPhotoUrl() != null) {
                    Glide.with(ChatBaseActivity.this)
                            .load(obj.getPhotoUrl())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .crossFade()
                            .into(authorImageView);
                }
            }
        };
    }
}
