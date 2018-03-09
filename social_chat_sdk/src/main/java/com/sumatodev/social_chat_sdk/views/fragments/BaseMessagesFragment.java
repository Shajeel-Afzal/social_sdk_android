package com.sumatodev.social_chat_sdk.views.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.chat_ui.commons.ImageLoader;
import com.sumatodev.social_chat_sdk.chat_ui.messages.MessagesListAdapter;
import com.sumatodev.social_chat_sdk.main.model.Message;
import com.sumatodev.social_chat_sdk.main.utils.AppUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Ali on 09/03/2018.
 */

public class BaseMessagesFragment extends Fragment implements MessagesListAdapter.SelectionListener,
        MessagesListAdapter.OnLoadMoreListener {

    private static final int TOTAL_MESSAGES_COUNT = 100;

    protected final String senderId = "0";
    protected ImageLoader imageLoader;
    protected MessagesListAdapter<Message> messagesAdapter;

    private Menu menu;
    private int selectionCount;
    private Date lastLoadedDate;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Picasso.with(getActivity()).load(url).into(imageView);
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        // messagesAdapter.addToStart(MessagesFixtures.getTextMessage(), true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        inflater.inflate(R.menu.chat_actions_menu, menu);
        onSelectionChanged(0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_delete) {
            messagesAdapter.deleteSelectedMessages();

        } else if (i == R.id.action_copy) {
            messagesAdapter.copySelectedMessagesText(getActivity(), getMessageStringFormatter(), true);
            AppUtils.showToast(getActivity(), R.string.copied_message, true);

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        if (totalItemsCount < TOTAL_MESSAGES_COUNT) {
            loadMessages();
        }
    }

    @Override
    public void onSelectionChanged(int count) {
        this.selectionCount = count;
        menu.findItem(R.id.action_delete).setVisible(count > 0);
        menu.findItem(R.id.action_copy).setVisible(count > 0);
    }

    protected void loadMessages() {
        new Handler().postDelayed(new Runnable() { //imitation of internet connection
            @Override
            public void run() {
                //ArrayList<Message> messages = MessagesFixtures.getMessages(lastLoadedDate);
                //lastLoadedDate = messages.get(messages.size() - 1).getCreatedAt();
                //messagesAdapter.addToEnd(messages, false);
            }
        }, 1000);
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
}
