package com.sumatodev.social_chat_sdk.views.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.adapters.ChatAdpater;
import com.sumatodev.social_chat_sdk.main.enums.Consts;
import com.sumatodev.social_chat_sdk.main.listeners.OnMessageSentListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnObjectChangedListener;
import com.sumatodev.social_chat_sdk.main.manager.MessagesManager;
import com.sumatodev.social_chat_sdk.main.model.Message;
import com.sumatodev.social_chat_sdk.main.model.Profile;
import com.sumatodev.social_chat_sdk.main.utils.MessageInput;
import com.sumatodev.social_chat_sdk.main.utils.RoundedCornersTransform;

public class MessageActivity extends BaseActivity implements MessageInput.InputListener, OnMessageSentListener {

    private static final String TAG = MessageActivity.class.getSimpleName();

    private LinearLayoutManager layoutManager;

    public static void open(Context context) {
        context.startActivity(new Intent(context, MessageActivity.class));
    }

    public static final String USER_KEY = "MessageActivity.userKey";
    private String userKey;
    private MessagesManager messagesManager;
    private ActionBar actionBar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ChatAdpater chatAdpater;

    private TextView userName_c;
    private TextView status_c;
    private ImageView userImage_c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_message);

        if (getIntent() != null) {
            userKey = getIntent().getStringExtra(USER_KEY);
        }

        setupActionBar();

        messagesManager = MessagesManager.getInstance(this);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        setupLinearLayout();
       // initAdapter();

        MessageInput input = findViewById(R.id.input);
        input.setInputListener(this);

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
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadProfile();
        chatAdpater.startListening();
    }

    @Override
    public boolean onSubmit(CharSequence input) {

        Message message = new Message(getCurrent_uid(), userKey, input.toString());
       // messagesManager.sendNewMessage(message, MessageActivity.this);

        return true;
    }

    @Override
    public void onMessageSent(boolean success, String message) {
        if (success) {
            Log.e(TAG, "successfully send");
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void initAdapter() {

        progressBar.setVisibility(View.GONE);
        Query query = FirebaseDatabase.getInstance().getReference(Consts.MESSAGES_REF)
                .child(getCurrent_uid()).child(userKey);

        FirebaseRecyclerOptions<Message> options = new FirebaseRecyclerOptions.Builder<Message>()
                .setQuery(query, Message.class)
                .build();

        chatAdpater = new ChatAdpater(options);
        chatAdpater.setCallback(new ChatAdpater.Callback() {
            @Override
            public void onTextClick(String key, View view) {
                Toast.makeText(getApplicationContext(), "item clicked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthorClick(String authorId, View view) {
                Toast.makeText(getApplicationContext(), "image clicked", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(chatAdpater);

        chatAdpater.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                layoutManager.smoothScrollToPosition(recyclerView, null, chatAdpater.getItemCount());
            }
        });
    }

    private void setupLinearLayout() {
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (chatAdpater != null) {
            chatAdpater.stopListening();
        }

    }

    private void loadProfile() {
        messagesManager.getProfileValue(this, userKey, onProfileChangedListener());
    }

    private OnObjectChangedListener<Profile> onProfileChangedListener() {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                fillProfileData(obj);
            }
        };
    }

    private void fillProfileData(final Profile profile) {
        if (profile != null && actionBar != null) {

            userName_c.setText(profile.getUsername());
            Picasso.with(MessageActivity.this).load(profile.getPhotoUrl())
                    .placeholder(R.drawable.imageview_user_thumb)
                    .transform(new RoundedCornersTransform())
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(userImage_c, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            Picasso.with(MessageActivity.this).load(profile.getPhotoUrl())
                                    .placeholder(R.drawable.imageview_user_thumb)
                                    .transform(new RoundedCornersTransform())
                                    .into(userImage_c);
                        }
                    });
        }
    }


}
