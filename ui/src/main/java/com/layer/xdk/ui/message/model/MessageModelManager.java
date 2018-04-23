package com.layer.xdk.ui.message.model;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.xdk.ui.identity.IdentityFormatter;
import com.layer.xdk.ui.message.LegacyMimeTypes;
import com.layer.xdk.ui.message.MessagePartUtils;
import com.layer.xdk.ui.message.button.ButtonMessageModel;
import com.layer.xdk.ui.message.carousel.CarouselMessageModel;
import com.layer.xdk.ui.message.choice.ChoiceMessageModel;
import com.layer.xdk.ui.message.file.FileMessageModel;
import com.layer.xdk.ui.message.generic.UnhandledMessageModel;
import com.layer.xdk.ui.message.image.ImageMessageModel;
import com.layer.xdk.ui.message.image.cache.ImageCacheWrapper;
import com.layer.xdk.ui.message.link.LinkMessageModel;
import com.layer.xdk.ui.message.location.LocationMessageModel;
import com.layer.xdk.ui.message.product.ProductMessageModel;
import com.layer.xdk.ui.message.receipt.ReceiptMessageModel;
import com.layer.xdk.ui.message.response.ResponseMessageModel;
import com.layer.xdk.ui.message.response.crdt.ORSet;
import com.layer.xdk.ui.message.response.crdt.ORSetDeserializer;
import com.layer.xdk.ui.message.status.StatusMessageModel;
import com.layer.xdk.ui.message.text.TextMessageModel;
import com.layer.xdk.ui.util.AndroidFieldNamingStrategy;
import com.layer.xdk.ui.util.DateFormatter;
import com.layer.xdk.ui.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * A class responsible for registering {@link MessageModel} classes against unique identifiers
 * and generating instances of {@link MessageModel}s when required.
 *
 * @see MessageModel for key concepts related Root {@link com.layer.sdk.messaging.MessagePart}s
 */
public class MessageModelManager {
    private final Map<String, Constructor<?>> mIdentifierToConstructorMap;

    private final Context mApplicationContext;
    private final LayerClient mLayerClient;
    private final IdentityFormatter mIdentityFormatter;
    private final DateFormatter mDateFormatter;
    private final ImageCacheWrapper mImageCacheWrapper;
    private final Gson mGson;

    /**
     * Create an instance of this class and register the default {@link MessageModel}.
     * The default {@link MessageModel} are
     * <ul>
     * <li>{@link TextMessageModel}</li>
     * <li>{@link ImageMessageModel}</li>
     * <li>{@link LocationMessageModel}</li>
     * <li>{@link LinkMessageModel}</li>
     * <li>{@link FileMessageModel}</li>
     * <li>{@link ButtonMessageModel}</li>
     * <li>{@link ChoiceMessageModel}</li>
     * <li>{@link CarouselMessageModel}</li>
     * <li>{@link ProductMessageModel}</li>
     * <li>{@link StatusMessageModel}</li>
     * <li>{@link ReceiptMessageModel}</li>
     * <li>{@link ResponseMessageModel}</li>
     * </ul>
     *
     * @param applicationContext the application {@link Context}
     * @param layerClient        an instance of {@link LayerClient}
     * @param identityFormatter  an {@link IdentityFormatter} instance used to format
     *                           {@link com.layer.sdk.messaging.Identity}s for display
     * @param dateFormatter      a {@link DateFormatter} instance used to format dates for display
     * @param imageCacheWrapper  an {@link ImageCacheWrapper} instance used to display images
     */
    @Inject
    public MessageModelManager(@NonNull Context applicationContext, @NonNull LayerClient layerClient,
                               @NonNull IdentityFormatter identityFormatter,
                               @NonNull DateFormatter dateFormatter,
                               @NonNull ImageCacheWrapper imageCacheWrapper) {
        mIdentifierToConstructorMap = new HashMap<>();
        mApplicationContext = applicationContext;
        mLayerClient = layerClient;
        mIdentityFormatter = identityFormatter;
        mDateFormatter = dateFormatter;
        mImageCacheWrapper = imageCacheWrapper;
        mGson = new GsonBuilder()
                .registerTypeAdapter(ORSet.class, new ORSetDeserializer())
                .setFieldNamingStrategy(new AndroidFieldNamingStrategy()).create();
        registerDefaultModels();
    }

    private void registerDefaultModels() {
        registerModel(TextMessageModel.ROOT_MIME_TYPE, TextMessageModel.class);
        registerModel(LegacyMimeTypes.LEGACY_TEXT_MIME_TYPE, TextMessageModel.class);
        registerModel(ImageMessageModel.ROOT_MIME_TYPE, ImageMessageModel.class);
        registerModel(LegacyMimeTypes.LEGACY_SINGLE_PART_MIME_TYPES, ImageMessageModel.class);
        registerModel(LegacyMimeTypes.LEGACY_THREE_PART_MIME_TYPES, ImageMessageModel.class);
        registerModel(LocationMessageModel.ROOT_MIME_TYPE, LocationMessageModel.class);
        registerModel(LegacyMimeTypes.LEGACY_LOCATION_MIME_TYPE, LocationMessageModel.class);
        registerModel(LinkMessageModel.ROOT_MIME_TYPE, LinkMessageModel.class);
        registerModel(FileMessageModel.ROOT_MIME_TYPE, FileMessageModel.class);
        registerModel(ButtonMessageModel.ROOT_MIME_TYPE, ButtonMessageModel.class);
        registerModel(ChoiceMessageModel.MIME_TYPE, ChoiceMessageModel.class);
        registerModel(CarouselMessageModel.MIME_TYPE, CarouselMessageModel.class);
        registerModel(ProductMessageModel.MIME_TYPE, ProductMessageModel.class);
        registerModel(StatusMessageModel.MIME_TYPE, StatusMessageModel.class);
        registerModel(ReceiptMessageModel.MIME_TYPE, ReceiptMessageModel.class);
        registerModel(ResponseMessageModel.MIME_TYPE, ResponseMessageModel.class);
        registerModel(ResponseMessageModel.MIME_TYPE_V2, ResponseMessageModel.class);
    }

    /**
     * Register a {@link MessageModel} subclass that can process a {@link Message} that has a
     * root {@link com.layer.sdk.messaging.MessagePart} corresponding to the supplied
     * modelIdentifier. If there is no root message part, then the model identifier should be a
     * comma separated string of all the mime types in the message, wrapped in square brackets.
     *
     * @param modelIdentifier   a {@link String} that identifies the {@link MessageModel} subclass
     *                          that can process it
     * @param messageModelClass the {@link Class} object of the {@link MessageModel} subclass
     * @param <T>               where T extends {@link MessageModel}
     */
    @SuppressWarnings("WeakerAccess")
    public <T extends MessageModel> void registerModel(@NonNull String modelIdentifier, @NonNull Class<T> messageModelClass) {
        try {
            Constructor<?> constructor = messageModelClass.getConstructor(Context.class, LayerClient.class, Message.class);
            mIdentifierToConstructorMap.put(modelIdentifier, constructor);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class does not implement required constructor");
        }
    }

    /**
     * Returns true if there is a {@link MessageModel} that can process a {@link Message} where
     * the {@link com.layer.sdk.messaging.MessagePart}'s mime type matches the supplied model
     * identifier
     *
     * @param modelIdentifier a {@link String} that identifies a {@link MessageModel} that can
     *                        process it
     * @return true if there is a registered {@link MessageModel} corresponding to the modelIdentifier
     */
    @SuppressWarnings("WeakerAccess")
    public boolean hasModel(@NonNull String modelIdentifier) {
        return mIdentifierToConstructorMap.containsKey(modelIdentifier);
    }

    /**
     * Removes the {@link MessageModel} class corresponding to the supplied modelIdentifier,
     * if it is registered
     *
     * @param modelIdentifier a {@link String} that identifies a {@link MessageModel} that can
     *                        process it
     */
    public void remove(@NonNull String modelIdentifier) {
        if (mIdentifierToConstructorMap.containsKey(modelIdentifier)) {
            mIdentifierToConstructorMap.remove(modelIdentifier);
        }
    }

    /**
     * Provides a new instance of a {@link MessageModel} subclass that can process the supplied
     * {@link Message}. The correct {@link MessageModel} type is determined from the mime types
     * of the {@link com.layer.sdk.messaging.MessagePart}s of the supplied {@link Message}.
     *
     * @param message a {@link Message} instance for which a {@link MessageModel} is to be generated
     * @return an instance of {@link MessageModel} or an instance of {@link UnhandledMessageModel}
     * if no {@link MessageModel} can handle the {@link Message}
     */
    @NonNull
    public MessageModel getNewModel(@NonNull Message message) {
        return getNewModel(getModelIdentifier(message), message);
    }

    /**
     * Provide a new instance of {@link MessageModel} that corresponds to the supplied model
     * identifier, and set the supplied {@link Message} on it
     *
     * @param modelIdentifier a {@link String} that identifies a {@link MessageModel} that can
     *                        process a {@link Message} with that {@link MessageModel}
     * @param message         a {@link Message} instance to be set on the generated {@link MessageModel}
     *                        instance
     * @return an instance of a {@link MessageModel} subclass corresponding to the supplied model
     * identifier
     */
    @SuppressWarnings({"WeakerAccess", "unchecked"})
    @NonNull
    public MessageModel getNewModel(@NonNull String modelIdentifier, @NonNull Message message) {
        Throwable exception;
        try {
            Constructor<? extends MessageModel> constructor =
                    (Constructor<? extends MessageModel>)
                            mIdentifierToConstructorMap.get(modelIdentifier);
            if (constructor == null) {
                return new UnhandledMessageModel(mApplicationContext, mLayerClient, message);
            } else {
                MessageModel model = constructor.newInstance(mApplicationContext, mLayerClient,
                        message);
                model.setMessageModelManager(this);
                model.setIdentityFormatter(mIdentityFormatter);
                model.setDateFormatter(mDateFormatter);
                model.setImageCacheWrapper(mImageCacheWrapper);
                model.setGson(mGson);
                return model;
            }
        } catch (IllegalAccessException e) {
            // Handled below
            exception = e;
        } catch (InstantiationException e) {
            // Handled below
            exception = e;
        } catch (InvocationTargetException e) {
            // Handled below
            exception = e;
        }

        if (Log.isLoggable(Log.ERROR)) {
            Log.e("Failed to instantiate a new MessageModel instance. Ensure an appropriate"
                    + " constructor exists.", exception);
        }
        throw new IllegalStateException("Failed to instantiate a new MessageModel instance."
                + " Ensure an appropriate constructor exists.");
    }

    @NonNull
    private static String getModelIdentifier(@NonNull Message message) {
        String rootMimeType = MessagePartUtils.getRootMimeType(message);
        if (rootMimeType == null) {
            // This is a legacy message
            return MessagePartUtils.getLegacyMessageMimeTypes(message);
        }
        return rootMimeType;
    }
}
