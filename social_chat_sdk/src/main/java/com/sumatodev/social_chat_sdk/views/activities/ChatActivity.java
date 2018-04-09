package com.sumatodev.social_chat_sdk.views.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.sumatodev.social_chat_sdk.main.adapters.MessagesAdapter;
import com.sumatodev.social_chat_sdk.main.listeners.OnDataChangedListener;
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import cz.kinst.jakub.view.SimpleStatefulLayout;

public class ChatActivity extends PickImageActivity implements MessageInput.InputListener, MessageInput.AttachmentsListener,
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
    private MessagesAdapter messagesAdapter;
    private RecyclerView recyclerView;

    private static final int TIME_OUT_LOADING_MESSAGES = 30000;
    private boolean attemptToLoadMessages = false;

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
        input.setAttachmentsListener(this);

        recyclerView = findViewById(R.id.recyclerView);
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

        return true;
    }

    private void sendImage() {
        if (imageUri != null) {
            InputMessage inputMessage = new InputMessage();
            inputMessage.setUid(userKey);
            inputMessage.setImageUrl(imageUri);
            messagesManager.sendNewMessage(inputMessage, ChatActivity.this);
        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        handleCropImageResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ImageActivity.IMAGE_PICKED_KEY) {
                sendImage();
            }
        }
    }


    private void initRecyclerView() {
        mStatefulLayout.showProgress();

        messagesAdapter = new MessagesAdapter(this);
        messagesAdapter.setCallback(new MessagesAdapter.Callback() {
            @Override
            public void onItemLongClick(int position, View view) {
                Message message = messagesAdapter.getItemByPosition(position);
                startActionMode(message);
            }

            @Override
            public void onUserImageClick(String userKey, View view) {
                if (userKey != null && hasInternetConnection()) {
                    Toast.makeText(ChatActivity.this, userKey, Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerView.setAdapter(messagesAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);

        messagesManager.getChatList(userKey, messageOnDataChangedListener());
    }

    private void openImageDetailActivity(String image) {
        Intent intent = new Intent(ChatActivity.this, ShowImageActivity.class);
        intent.putExtra(ShowImageActivity.IMAGE_URL_EXTRA_KEY, image);
        startActivity(intent);
    }


    private OnDataChangedListener<Message> messageOnDataChangedListener() {

        attemptToLoadMessages = true;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (attemptToLoadMessages) {
                    mStatefulLayout.showContent();
                }
            }
        }, TIME_OUT_LOADING_MESSAGES);

        return new OnDataChangedListener<Message>() {
            @Override
            public void onListChanged(List<Message> list) {
                attemptToLoadMessages = false;
                mStatefulLayout.showContent();
                recyclerView.setVisibility(View.VISIBLE);
                messagesAdapter.setList(list);
                messagesAdapter.cleanSelectedPosition();
            }

            @Override
            public void onCancel(String message) {
                recyclerView.setVisibility(View.GONE);
                mStatefulLayout.showEmpty();
                mStatefulLayout.setEmptyText(message);
            }
        };
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

        ActionModeCallback(Message selectedMessage) {
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
                removeMessage(selectedMessage.getId(), mode);
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

    private void removeMessage(String messageId, final ActionMode mode) {
        showProgress(R.string.deleting_message);

        messagesManager.removeMessage(messageId, userKey, new OnTaskCompleteListener() {
            @Override
            public void onTaskComplete(boolean success) {
                if (success) {
                    messagesAdapter.removeMessage();
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
