package com.layer.xdk.ui.testactivity;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Identity;
import com.layer.xdk.ui.DaggerXdkUiTestComponent;
import com.layer.xdk.ui.FakeXdkUiModule;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.XdkUiTestComponent;
import com.layer.xdk.ui.conversation.ConversationItemFormatter;
import com.layer.xdk.ui.conversation.adapter.ConversationItemModel;
import com.layer.xdk.ui.conversation.adapter.viewholder.ConversationItemVHModel;
import com.layer.xdk.ui.databinding.TestActivityFourPartItemBinding;
import com.layer.xdk.ui.mock.MockConversation;
import com.layer.xdk.ui.fourpartitem.FourPartItemStyle;

public class ConversationItemTestActivity extends Activity {

    private ConversationItemFormatter mConversationItemFormatter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        XdkUiTestComponent component = DaggerXdkUiTestComponent.builder()
                .fakeXdkUiModule(new FakeXdkUiModule(this)).build();

        mConversationItemFormatter = component.conversationItemFormatter();
        LayerClient layerClient = component.layerClient();

        Identity authenticatedUser = layerClient.getAuthenticatedUser();
        Conversation conversation = new MockConversation(authenticatedUser, 3);

        TestActivityFourPartItemBinding binding = DataBindingUtil.setContentView(this, R.layout.test_activity_four_part_item);
        FourPartItemStyle style = new FourPartItemStyle(this, null, 0);

        ConversationItemVHModel viewHolderModel = component.conversationItemViewModel();
        ConversationItemModel itemModel = new ConversationItemModel(conversation, null, authenticatedUser);
        viewHolderModel.setItem(itemModel);

        binding.testFourPartItem.avatar.setImageCacheWrapper(viewHolderModel.getImageCacheWrapper());
        binding.testFourPartItem.avatar.setIdentityFormatter(viewHolderModel.getIdentityFormatter());
        binding.testFourPartItem.avatar.setParticipants(conversation.getParticipants());
        binding.testFourPartItem.setStyle(style);
        binding.testFourPartItem.setViewHolderModel(viewHolderModel);
    }
}
