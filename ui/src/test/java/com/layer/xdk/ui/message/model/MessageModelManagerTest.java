package com.layer.xdk.ui.message.model;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.mock;

import android.content.Context;

import com.layer.sdk.LayerClient;
import com.layer.xdk.ui.identity.IdentityFormatter;
import com.layer.xdk.ui.message.LegacyMimeTypes;
import com.layer.xdk.ui.message.button.ButtonMessageModel;
import com.layer.xdk.ui.message.carousel.CarouselMessageModel;
import com.layer.xdk.ui.message.choice.ChoiceMessageModel;
import com.layer.xdk.ui.message.file.FileMessageModel;
import com.layer.xdk.ui.message.image.ImageMessageModel;
import com.layer.xdk.ui.message.image.cache.ImageCacheWrapper;
import com.layer.xdk.ui.message.link.LinkMessageModel;
import com.layer.xdk.ui.message.location.LocationMessageModel;
import com.layer.xdk.ui.message.product.ProductMessageModel;
import com.layer.xdk.ui.message.receipt.ReceiptMessageModel;
import com.layer.xdk.ui.message.response.ResponseMessageModel;
import com.layer.xdk.ui.message.status.StatusMessageModel;
import com.layer.xdk.ui.message.text.TextMessageModel;
import com.layer.xdk.ui.util.DateFormatter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class MessageModelManagerTest {

    @Mock
    LayerClient mLayerClient;

    @Mock
    Context mContext;

    @Mock
    IdentityFormatter mIdentityFormatter;

    @Mock
    DateFormatter mDateFormatter;

    @Mock
    ImageCacheWrapper mImageCacheWrapper;

    MessageModelManager mManager;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mManager = new MessageModelManager(mContext, mLayerClient, mIdentityFormatter,
                mDateFormatter, mImageCacheWrapper);
    }

    @Test
    public void testDefaultModels() {
        assertThat(mManager.hasModel(TextMessageModel.ROOT_MIME_TYPE)).isTrue();
        assertThat(mManager.hasModel(LegacyMimeTypes.LEGACY_TEXT_MIME_TYPE)).isTrue();
        assertThat(mManager.hasModel(ImageMessageModel.ROOT_MIME_TYPE)).isTrue();
        assertThat(mManager.hasModel(LegacyMimeTypes.LEGACY_SINGLE_PART_MIME_TYPES)).isTrue();
        assertThat(mManager.hasModel(LegacyMimeTypes.LEGACY_THREE_PART_MIME_TYPES)).isTrue();
        assertThat(mManager.hasModel(LocationMessageModel.ROOT_MIME_TYPE)).isTrue();
        assertThat(mManager.hasModel(LegacyMimeTypes.LEGACY_LOCATION_MIME_TYPE)).isTrue();
        assertThat(mManager.hasModel(LinkMessageModel.ROOT_MIME_TYPE)).isTrue();
        assertThat(mManager.hasModel(FileMessageModel.ROOT_MIME_TYPE)).isTrue();
        assertThat(mManager.hasModel(ButtonMessageModel.ROOT_MIME_TYPE)).isTrue();
        assertThat(mManager.hasModel(ChoiceMessageModel.MIME_TYPE)).isTrue();
        assertThat(mManager.hasModel(CarouselMessageModel.MIME_TYPE)).isTrue();
        assertThat(mManager.hasModel(ProductMessageModel.MIME_TYPE)).isTrue();
        assertThat(mManager.hasModel(StatusMessageModel.MIME_TYPE)).isTrue();
        assertThat(mManager.hasModel(ReceiptMessageModel.MIME_TYPE)).isTrue();
        assertThat(mManager.hasModel(ResponseMessageModel.MIME_TYPE)).isTrue();
    }

    @Test
    public void testModelRegistration() {
        MessageModel mockModel = mock(MessageModel.class);
        String modelIdentifier = "SampleIdentifier";
        mManager.registerModel(modelIdentifier, mockModel.getClass());

        assertThat(mManager.hasModel(modelIdentifier)).isTrue();
    }

    @Test
    public void testModelRemoval() {
        MessageModel mockModel = mock(MessageModel.class);
        String modelIdentifier = "SampleIdentifier";
        mManager.registerModel(modelIdentifier, mockModel.getClass());

        assertThat(mManager.hasModel(TextMessageModel.ROOT_MIME_TYPE)).isTrue();
        assertThat(mManager.hasModel(modelIdentifier)).isTrue();

        mManager.remove(TextMessageModel.ROOT_MIME_TYPE);
        mManager.remove(modelIdentifier);

        assertThat(mManager.hasModel(TextMessageModel.ROOT_MIME_TYPE)).isFalse();
        assertThat(mManager.hasModel(modelIdentifier)).isFalse();

        // Ensure a default is still there
        assertThat(mManager.hasModel(LegacyMimeTypes.LEGACY_TEXT_MIME_TYPE)).isTrue();
    }
}
