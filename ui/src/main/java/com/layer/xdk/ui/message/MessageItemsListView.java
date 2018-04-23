package com.layer.xdk.ui.message;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.TextView;

import com.layer.sdk.LayerClient;
import com.layer.sdk.changes.LayerChange;
import com.layer.sdk.changes.LayerChangeEvent;
import com.layer.sdk.listeners.LayerChangeEventListener;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Identity;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.identity.DefaultIdentityFormatter;
import com.layer.xdk.ui.identity.IdentityFormatter;
import com.layer.xdk.ui.message.adapter.MessageModelAdapter;
import com.layer.xdk.ui.message.adapter.decoration.GroupStartItemDecoration;
import com.layer.xdk.ui.message.adapter.decoration.SubGroupStartItemDecoration;
import com.layer.xdk.ui.typingindicator.TypingIndicatorLayout;
import com.layer.xdk.ui.typingindicator.TypingIndicatorMode;
import com.layer.xdk.ui.util.AnimationUtils;
import com.layer.xdk.ui.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MessageItemsListView extends SwipeRefreshLayout implements LayerChangeEventListener.BackgroundThread.Weak {

    private static final int TYPING_INDICATOR_ANIMATION_DURATION = 250;

    private RecyclerView mMessagesRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private MessageModelAdapter mAdapter;
    private IdentityFormatter mIdentityFormatter;

    private LayerClient mLayerClient;
    private Conversation mConversation;
    private int mNumberOfItemsPerSync = 20;
    private TextView mEmptyListTextView;
    private TextView mTypingIndicatorTextView;

    public MessageItemsListView(Context context) {
        this(context, null);
    }

    public MessageItemsListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(getContext(), R.layout.xdk_ui_message_items_list, this);
        mMessagesRecyclerView = findViewById(R.id.xdk_ui_message_recycler);
        mEmptyListTextView = findViewById(R.id.xdk_ui_messages_recycler_empty_text);
        mTypingIndicatorTextView = findViewById(R.id.xdk_ui_message_item_list_typing_indicator_text);

        mLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mLinearLayoutManager.setReverseLayout(true);
        mMessagesRecyclerView.setHasFixedSize(true);
        mMessagesRecyclerView.setLayoutManager(mLinearLayoutManager);

        mMessagesRecyclerView.addItemDecoration(new GroupStartItemDecoration(context));
        mMessagesRecyclerView.addItemDecoration(new SubGroupStartItemDecoration(context));

        setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                mLayerClient.registerEventListener(MessageItemsListView.this);
                if (mConversation.getHistoricSyncStatus() == Conversation.HistoricSyncStatus.MORE_AVAILABLE) {
                    mConversation.syncMoreHistoricMessages(mNumberOfItemsPerSync);
                }
            }
        });
    }

    public void setAdapter(final MessageModelAdapter adapter) {
        mAdapter = adapter;
        mMessagesRecyclerView.setAdapter(adapter);

        mAdapter.registerAdapterDataObserver(new MessageModelAdapter.NewMessageReceivedObserver() {
            @Override
            public void onNewMessageReceived() {
                if (mEmptyListTextView.getVisibility() == VISIBLE && mAdapter.getItemCount() > 0) {
                    mEmptyListTextView.setVisibility(GONE);
                }
                autoScroll();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                if (mAdapter.getItemCount() == 0) {
                    mEmptyListTextView.setVisibility(VISIBLE);
                }
            }
        });
    }

    public void setInitialLoadComplete(boolean initialLoadComplete) {
        if (initialLoadComplete && mAdapter != null && mAdapter.getItemCount() == 0) {
            mEmptyListTextView.setVisibility(VISIBLE);
        }
    }

    @NonNull
    private IdentityFormatter getIdentityFormatter() {
        if (mIdentityFormatter == null) {
            if (Log.isLoggable(Log.WARN)) {
                Log.w("No IdentityFormatter supplied. Using the DefaultIdentityFormatter");
            }
            mIdentityFormatter = new DefaultIdentityFormatter(getContext());
        }
        return mIdentityFormatter;
    }

    //============================================================================================
    // LayerChangeEventListener.BackgroundThread.Weak Methods
    //============================================================================================

    @Override
    public void onChangeEvent(LayerChangeEvent layerChangeEvent) {
        for (LayerChange change : layerChangeEvent.getChanges()) {
            if (change.getObject() != mConversation) continue;
            if (change.getChangeType() != LayerChange.Type.UPDATE) continue;
            if (!change.getAttributeName().equals("historicSyncStatus")) continue;
            if (change.getNewValue() != Conversation.HistoricSyncStatus.SYNC_PENDING) {
                mLayerClient.unregisterEventListener(this);
            }
            refresh();
        }
    }

    //============================================================================================
    // SwipeRefreshLayout Methods
    //============================================================================================

    /**
     * Refreshes the state of the underlying recycler view
     */
    private void refresh() {
        post(new Runnable() {
            @Override
            public void run() {
                if (mConversation == null) {
                    setRefreshing(false);
                    updateRefreshEnabled();
                    return;
                }
                Conversation.HistoricSyncStatus status = mConversation.getHistoricSyncStatus();
                setRefreshing(status == Conversation.HistoricSyncStatus.SYNC_PENDING);
                updateRefreshEnabled();
            }
        });
    }

    private void updateRefreshEnabled() {
        if (mConversation == null || mConversation.getHistoricSyncStatus()
                == Conversation.HistoricSyncStatus.NO_MORE_AVAILABLE) {
            setEnabled(false);
        }
    }

    //============================================================================================
    // Public Methods
    //============================================================================================

    @SuppressWarnings("unused")
    public int getNumberOfItemsPerSync() {
        return mNumberOfItemsPerSync;
    }

    @SuppressWarnings("unused")
    public void setNumberOfItemsPerSync(int numberOfItemsPerSync) {
        mNumberOfItemsPerSync = numberOfItemsPerSync;
    }

    /**
     * Convenience pass-through to this list's MessagesAdapter.
     *
     * @see MessageModelAdapter#getShouldShowAvatarInOneOnOneConversations()
     */
    @SuppressWarnings("unused")
    public boolean getShouldShowAvatarInOneOnOneConversations() {
        return mAdapter.getShouldShowAvatarInOneOnOneConversations();
    }

    /**
     * Convenience pass-through to this list's MessagesAdapter.
     *
     * @see MessageModelAdapter#setShouldShowAvatarInOneOnOneConversations(boolean)
     */
    @SuppressWarnings("unused")
    public void setShouldShowAvatarInOneOnOneConversations(boolean shouldShowAvatarInOneOnOneConversations) {
        mAdapter.setShouldShowAvatarInOneOnOneConversations(shouldShowAvatarInOneOnOneConversations);
    }

    /**
     * Scrolls if the user is at the end
     */
    private void autoScroll() {
        // Find first since this layout is reversed
        int firstVisiblePosition = mLinearLayoutManager.findFirstVisibleItemPosition();
        if (firstVisiblePosition < 2) {
            mLinearLayoutManager.smoothScrollToPosition(mMessagesRecyclerView, null, 0);
        }
    }

    /**
     * Note that {@link TypingIndicatorMode#INLINE} is managed by this view's {@link MessageModelAdapter}
     * @see MessageModelAdapter#setTypingIndicatorLayout(TypingIndicatorLayout, Set)
     *
     * {@link TypingIndicatorMode#TEXT} is managed by this view
     *
     * @param layout a {@link TypingIndicatorLayout} instance
     * @param users  a {@link Set} of {@link Identity} objects representing the users currently typing
     * @param mode   the {@link TypingIndicatorMode} to be used
     */
    public void setTypingIndicatorLayout(@Nullable TypingIndicatorLayout layout,
                                         @Nullable Set<Identity> users, TypingIndicatorMode mode) {
        switch (mode) {
            case INLINE:
                mTypingIndicatorTextView.setVisibility(GONE);
                mAdapter.setTypingIndicatorLayout(layout, users);
                break;
            case TEXT:
                setupTypingIndicatorText(layout, users);
                mAdapter.setTypingIndicatorLayout(null, null);
                autoScroll();
                break;
            case BOTH:
            default:
                setupTypingIndicatorText(layout, users);
                mAdapter.setTypingIndicatorLayout(layout, users);
        }
    }

    private void setupTypingIndicatorText(@Nullable TypingIndicatorLayout layout, @Nullable Set<Identity> users) {
        if (layout != null && mAdapter.shouldShowTypingIndicator() && users != null
                && users.size() > 0) {
            String typingIndicatorText = getTypingIndicatorText(users);
            if (typingIndicatorText != null) {
                mTypingIndicatorTextView.setText(typingIndicatorText);
                if (mTypingIndicatorTextView.getVisibility() != VISIBLE) {
                    AnimationUtils.animateViewIn(mTypingIndicatorTextView,
                            TYPING_INDICATOR_ANIMATION_DURATION);
                }
                return;
            }
        }

        if (mTypingIndicatorTextView.getVisibility() == VISIBLE) {
            AnimationUtils.animateViewOut(mTypingIndicatorTextView,
                    TYPING_INDICATOR_ANIMATION_DURATION);
        }
    }

    @Nullable
    private String getTypingIndicatorText(@NonNull Set<Identity> users) {
        List<Identity> userList = new ArrayList<>(users);
        Resources resources = getContext().getResources();

        switch (userList.size()) {
            case 0:
                return null;
            case 1:
                return resources.getQuantityString(R.plurals.xdk_ui_typing_indicator_message,
                        1, getIdentityFormatter().getDisplayName(userList.get(0)));
            case 2:
                return resources.getQuantityString(R.plurals.xdk_ui_typing_indicator_message,
                        1, getIdentityFormatter().getDisplayName(userList.get(0)),
                        getIdentityFormatter().getDisplayName(userList.get(1)));
            default:
                return resources.getQuantityString(R.plurals.xdk_ui_typing_indicator_message,
                        1, getIdentityFormatter().getDisplayName(userList.get(0)),
                        getIdentityFormatter().getDisplayName(userList.get(1)), userList.size() - 2);
        }
    }

    /**
     * Sets the conversation for use with the empty view and swipe to refresh.
     *
     * @param layerClient  LayerClient currently in use
     * @param conversation Conversation to display Messages for.
     */
    public void setConversation(LayerClient layerClient, Conversation conversation) {
        mConversation = conversation;
        mLayerClient = layerClient;
        if (conversation != null) {
            mEmptyListTextView.setText(
                    getEmptyConversationHeaderText(getContext(), conversation.getParticipants(),
                            layerClient.getAuthenticatedUser()));
        }
        updateRefreshEnabled();
    }

    /**
     * Sets {@link IdentityFormatter} to be used with the TypingIndicator
     *
     * @param identityFormatter {@link IdentityFormatter} instance to be used
     */
    public void setIdentityFormatter(IdentityFormatter identityFormatter) {
        mIdentityFormatter = identityFormatter;
    }

    private String getEmptyConversationHeaderText(Context context, Set<Identity> participants, Identity authenticatedUser) {

        if (participants.size() == 0 || authenticatedUser == null) return "";
        participants.remove(authenticatedUser);
        List<Identity> participantList = new ArrayList<>(participants);

        String headerText;

        switch (participants.size()) {
            case 0:
                headerText = context.getString(
                        R.string.xdk_ui_empty_conversation_with_zero_participant);
                break;
            case 1:
                headerText = context.getString(
                        R.string.xdk_ui_empty_conversation_with_one_participant,
                        getIdentityFormatter().getDisplayName(participantList.get(0)));
                break;
            case 2:
                headerText = context.getString(
                        R.string.xdk_ui_empty_conversation_with_two_participants,
                        getIdentityFormatter().getDisplayName(participantList.get(0)),
                        getIdentityFormatter().getDisplayName(participantList.get(1)));
                break;
            case 3:
                headerText = context.getString(
                        R.string.xdk_ui_empty_conversation_with_three_participants,
                        getIdentityFormatter().getDisplayName(participantList.get(0)),
                        getIdentityFormatter().getDisplayName(participantList.get(1)));
                break;
            default:
                int remainder = participantList.size() - 2;
                headerText = context.getString(
                        R.string.xdk_ui_empty_conversation_with_many_participants,
                        getIdentityFormatter().getDisplayName(participantList.get(0)),
                        getIdentityFormatter().getDisplayName(participantList.get(1)), remainder);
        }

        return headerText;
    }
}
