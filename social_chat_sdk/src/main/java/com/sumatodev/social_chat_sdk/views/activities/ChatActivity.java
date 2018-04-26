package com.sumatodev.social_chat_sdk.views.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.adapters.ChatAdapter;
import com.sumatodev.social_chat_sdk.main.listeners.OnMessageSentListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnObjectChangedListener;
import com.sumatodev.social_chat_sdk.main.manager.MessagesManager;
import com.sumatodev.social_chat_sdk.main.model.InputMessage;
import com.sumatodev.social_chat_sdk.main.model.Message;
import com.sumatodev.social_chat_sdk.main.model.UsersPublic;
import com.sumatodev.social_chat_sdk.main.utils.FormatterUtil;
import com.sumatodev.social_chat_sdk.main.utils.MessageInput;
import com.sumatodev.social_chat_sdk.main.utils.RoundedCornersTransform;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import cz.kinst.jakub.view.SimpleStatefulLayout;

public class ChatActivity extends PickImageActivity implements MessageInput.InputListener, View.OnClickListener, MessageInput.AttachmentsListener {


    private static final String TAG = ChatActivity.class.getSimpleName();
    public static final String USER_KEY = "ChatActivity.userKey";
    private String userKey;

    private ActionBar actionBar;
    private TextView userName_c;
    private TextView status_c;
    private ImageView userImage_c;

    private SimpleStatefulLayout mStateful_view;
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
        setupActionBar();

        MessageInput input = findViewById(R.id.input);
        input.setInputListener(this);
        //input.setAttachmentsListener(this);

        intiRecylerView();
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
                        createProfileChangeListener());
            }
        }
    }

    private OnObjectChangedListener<UsersPublic> createProfileChangeListener() {
        return new OnObjectChangedListener<UsersPublic>() {
            @Override
            public void onObjectChanged(final UsersPublic obj) {

                if (obj.getUsername() != null) {
                    userName_c.setText(obj.getUsername());
                }
                if (obj.getStatus() != null) {
                    status_c.setVisibility(View.VISIBLE);
                    if (obj.getStatus().isOnline) {
                        status_c.setText(R.string.isOnline);
                    } else {
                        CharSequence lastSeen = FormatterUtil.getRelativeTimeSpanString(getApplicationContext(),
                                (long) obj.getStatus().lastSeen);

                        status_c.setText(lastSeen);
                    }
                }
                if (obj.getPhotoUrl() != null) {

                    Picasso.get().load(obj.getPhotoUrl())
                            .placeholder(R.drawable.imageview_user_thumb)
                            .transform(new RoundedCornersTransform())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(userImage_c, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {

                                    Picasso.get().load(obj.getPhotoUrl())
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
        messagesManager.sendNewMessage(inputMessage, new OnMessageSentListener() {
            @Override
            public void onMessageSent(boolean success, String message) {

            }
        });

        return true;
    }

    private void fillViews() {
        mStateful_view = findViewById(R.id.stateful_view);
        recyclerView = findViewById(R.id.recyclerView);
    }

    private void intiRecylerView() {

        adapter = new ChatAdapter(ChatActivity.this, userKey);
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
                mStateful_view.setEmptyText(message);
            }

            @Override
            public void isEmpty(boolean isEmpty) {
                if (isEmpty) {
                    mStateful_view.showEmpty();
                }
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

    @Override
    public void onAddAttachments(View view) {
        showMenu(view);
    }

    private void showMenu(View view) {

        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.chat_input_menu, popupMenu.getMenu());

        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int i = item.getItemId();
                if (i == R.id.inputGallery) {
                    onSelectImageClick();
                    return true;
                } else if (i == R.id.inputLocation) {
                    Toast.makeText(ChatActivity.this, "location", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }


    @Override
    protected ProgressBar getProgressView() {
        return null;
    }

    @Override
    protected ImageView getImageView() {
        return null;
    }

    @Override
    protected void onImagePikedAction() {
        startCropImageActivity();
    }

    @Override
    protected void onFinishedCropping() {
        if (imageUri != null) {
            Intent intent = new Intent(ChatActivity.this, ImageActivity.class);
            intent.putExtra("imageUrl", imageUri.toString());
            startActivityForResult(intent, ImageActivity.IMAGE_PICKED_KEY);
            Log.d(TAG, "ImageUrl: " + imageUri);
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
