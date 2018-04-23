package com.layer.xdk.ui.conversationitem;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import android.content.Context;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Identity;
import com.layer.xdk.ui.conversation.ConversationItemFormatter;
import com.layer.xdk.ui.conversation.adapter.ConversationItemModel;
import com.layer.xdk.ui.conversation.adapter.viewholder.ConversationItemVHModel;
import com.layer.xdk.ui.identity.IdentityFormatter;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.message.image.cache.ImageCacheWrapper;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class ConversationItemViewModelTest {
    private static final String CONVERSATION_TITLE = "P2, P3";
    private static final String CONVERSATION_SUBTITLE = "The last message";
    private static final String CONVERSATION_TIMESTAMP = "3 hours ago";

    @Mock
    ConversationItemFormatter mConversationItemFormatter;
    @Mock
    IdentityFormatter mIdentityFormatter;
    @Mock
    ImageCacheWrapper mImageCacheWrapper;
    @Mock
    Conversation mConversation;
    @Mock
    Context mContext;
    @Mock
    LayerClient mLayerClient;
    @Mock
    Identity mParticipant1, mParticipant2, mParticipant3;
    @Mock
    MessageModel mLastMessageModel;

    ConversationItemModel mConversationItemModel;
    ConversationItemVHModel mViewModel;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(mConversationItemFormatter.getConversationTitle(any(Conversation.class))).thenReturn(CONVERSATION_TITLE);
        when(mConversationItemFormatter.getLastMessagePreview(any(Conversation.class), any(
                MessageModel.class))).thenReturn(CONVERSATION_SUBTITLE);
        when(mConversationItemFormatter.getTimeStamp(any(Conversation.class))).thenReturn(CONVERSATION_TIMESTAMP);

        when(mConversation.getTotalUnreadMessageCount()).thenReturn(5);

        Set<Identity> participants = new HashSet<>();
        participants.addAll(Arrays.asList(mParticipant1, mParticipant2, mParticipant3));
        when(mConversation.getParticipants()).thenReturn(participants);

        when(mLayerClient.newConversation(any(Identity.class))).thenReturn(mConversation);
        when(mLayerClient.getAuthenticatedUser()).thenReturn(mParticipant1);


        mConversationItemModel = new ConversationItemModel(mConversation, mLastMessageModel, mParticipant1);
        mViewModel = new ConversationItemVHModel(mIdentityFormatter, mImageCacheWrapper,
                mConversationItemFormatter);
        mViewModel.setItem(mConversationItemModel);
    }

    @Test
    public void testGetTitle() {
        assertThat(mViewModel.getSubtitle(), is(CONVERSATION_SUBTITLE));
    }

    @Test
    public void testGetSubtitle() {
        assertThat(mViewModel.getSubtitle(), is(CONVERSATION_SUBTITLE));
    }

    @Test
    public void testAccessoryText() {
        assertThat(mViewModel.getAccessoryText(), is(CONVERSATION_TIMESTAMP));
    }

    @Test
    public void testIsUnread() {
        assertThat(mViewModel.isSecondaryState(), is(true));
    }

    @Test
    public void testGetParticipantsDoesNotContainAuthenticatedUser() {
        assertThat(mViewModel.getIdentities().contains(mParticipant1), is(false));
        assertThat(mViewModel.getIdentities().contains(mParticipant2), is(true));
        assertThat(mViewModel.getIdentities().contains(mParticipant3), is(true));
    }
}
