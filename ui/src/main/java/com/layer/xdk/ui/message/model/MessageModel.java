package com.layer.xdk.ui.message.model;


import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.net.Uri;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.xdk.ui.identity.IdentityFormatter;
import com.layer.xdk.ui.identity.adapter.IdentityItemModel;
import com.layer.xdk.ui.message.MessagePartUtils;
import com.layer.xdk.ui.message.adapter.MessageGrouping;
import com.layer.xdk.ui.message.adapter.MessageModelAdapter;
import com.layer.xdk.ui.message.image.cache.ImageCacheWrapper;
import com.layer.xdk.ui.message.response.ResponseSummaryMetadataV2;
import com.layer.xdk.ui.repository.MessageSenderRepository;
import com.layer.xdk.ui.util.DateFormatter;
import com.layer.xdk.ui.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class that converts a {@link Message} and its {@link MessagePart}s into a tree of
 * MessageModels and supplies data suitable for display.
 * <p>
 * Key concepts:
 * <ul>
 * <li><b>Node id</b> : A unique identifier specified on the {@link MessagePart}'s mime type
 * arguments as <i>node-id</i></li>
 * <li><b>Root {@link MessagePart}</b> : the {@link MessagePart} of the backing {@link Message}
 * that has the mime type argument of <i>role = root</i></li>
 * <li><b>Child {@link MessagePart}</b> : a {@link MessagePart} that specifies another
 * {@link MessagePart} as its parent by using a <i>parent-node-id</i> equal to another part's
 * <i>node-id</i> in its mime type arguments</li>
 * <li><b>Root {@link MessageModel}</b> : the {@link MessageModel} that acts as the root in a given
 * sub-tree. In case of the root {@link MessagePart}, this is the root model of the entire tree</li>
 * <li><b>Child {@link MessageModel}</b> : a {@link MessageModel} whose <i>parent-node-id</i>
 * corresponds to the <i>node-id</i> of a given sub tree root</li>
 * <li><b>Legacy {@link Message}</b> : a {@link Message} that contains no {@link MessagePart} with
 * a mime type argument of <i>role = root</i></li>
 * <li><b>Legacy {@link MessagePart}</b> : a {@link MessagePart} belonging to a Legacy
 * {@link Message}</li>
 * </ul>
 * </p>
 */
public abstract class MessageModel extends BaseObservable {

    private final Context mContext;
    private final LayerClient mLayerClient;
    // These fields are set directly after instantiation by the MessageModelManager
    private MessageModelManager mMessageModelManager;
    private IdentityFormatter mIdentityFormatter;
    private DateFormatter mDateFormatter;
    private ImageCacheWrapper mImageCacheWrapper;
    private Gson mGson;

    private final Message mMessage;

    // It's safe to cache this since no model will live after a de-auth
    private final Uri mAuthenticatedUserId;
    private final Uri mSenderId;

    private int mParticipantCount;
    private boolean mMyNewestMessage;

    private String mRole;
    private MessagePart mRootMessagePart;
    private MessageModel mParentMessageModel;
    private List<MessagePart> mChildMessageParts;
    private List<MessageModel> mChildMessageModels;

    private MessageSenderRepository mMessageSenderRepository;

    private Action mAction;
    private String mMimeTypeTree;

    private EnumSet<MessageGrouping> mGrouping;

    // Save these purely for deep equals comparisons
    private Map<Identity, Message.RecipientStatus> mRecipientStatuses;
    private Date mMessageUpdatedAt;
    private Set<MessagePart.TransferStatus> mMessagePartTransferStatus;
    private Set<Date> mMessagePartUpdatedAt;
    private byte[] mMessageLocalData;
    private IdentityItemModel mCachedSender;

    public MessageModel(@NonNull Context context, @NonNull LayerClient layerClient,
                        @NonNull Message message) {
        mContext = context.getApplicationContext();
        mLayerClient = layerClient;

        mMessage = message;
        Identity authenticatedUser = layerClient.getAuthenticatedUser();
        mAuthenticatedUserId = authenticatedUser == null ? null : authenticatedUser.getId();
        Identity sender = message.getSender();
        mSenderId = sender == null ? null : sender.getId();

        mParticipantCount = mMessage.getConversation().getParticipants().size();
        mChildMessageModels = new ArrayList<>();
        mMessageLocalData = mMessage.getLocalData();
        cacheMessageDataForDeepEquals();
    }

    /**
     * Parse the supplied {@link MessagePart}
     *
     * @param messagePart a {@link MessagePart} to be parsed
     */
    protected abstract void parse(@NonNull MessagePart messagePart);

    /**
     * Provides the layout resource ID of the view to inflate into the container.
     *
     * @return layout resource ID to inflate into the container. If no layout is associated, 0
     * should be returned.
     */
    @LayoutRes
    public abstract int getViewLayoutId();

    /**
     * Provides the layout resource ID of the container for this model that will be inflated into
     * a ViewHolder. If no layout is associated, 0 should be returned.
     *
     * @return layout resource ID to inflate into the ViewHolder. If no layout is associated, 0
     * should be returned.
     */
    @LayoutRes
    public abstract int getContainerViewLayoutId();

    /**
     * Allows for the optional automatic downloading of large {@link MessagePart}s, if not already
     * downloaded
     *
     * @param messagePart to be downloaded if not ready
     * @return true if the messagePart should be downloaded, false if not
     */
    protected abstract boolean shouldDownloadContentIfNotReady(@NonNull MessagePart messagePart);

    /**
     * Return true if the model has content to display
     *
     * @return true if the model has content available for display
     */
    @Bindable
    public abstract boolean getHasContent();

    /**
     * Provides a {@link String} that is suitable for display as a preview for the root
     * {@link MessagePart} this model represents
     *
     * @return a {@link String} suitable for display as preview
     */
    @Bindable
    @Nullable
    public abstract String getPreviewText();

    /**
     * Provides a {@link String} that can be used as a title by a
     * {@link com.layer.xdk.ui.message.container.MessageContainer} displaying this model
     *
     * @return a {@link String} suitable for display as a title, or null if no title should be shown
     */
    @Nullable
    @Bindable
    public abstract String getTitle();

    /**
     * Provides a {@link String} that can be used as a description by a
     * {@link com.layer.xdk.ui.message.container.MessageContainer} displaying this model
     *
     * @return a {@link String} suitable for display as a description, or null if no description
     * should be shown
     */
    @Nullable
    @Bindable
    public abstract String getDescription();

    /**
     * Provides a {@link String} that can be used as a footer by a
     * {@link com.layer.xdk.ui.message.container.MessageContainer} displaying this model
     *
     * @return a {@link String} suitable for display as a footer, of null if no footer should be
     * shown
     */
    @Nullable
    @Bindable
    public abstract String getFooter();

    /**
     * Process the {@link MessagePart}s starting from the root message part of this sub-tree
     */
    public final void processPartsFromTreeRoot() {
        MessagePart rootMessagePart = MessagePartUtils.getMessagePartWithRoleRoot(getMessage());
        if (rootMessagePart == null) {
            mMimeTypeTree = createLegacyMimeTypeTree();
            processLegacyParts();
        } else {
            // Always download the message's root part
            if (!rootMessagePart.isContentReady()) {
                rootMessagePart.download(null);
            }

            processParts(rootMessagePart);
        }
    }

    /**
     * Handle legacy {@link MessagePart}s
     */
    protected void processLegacyParts() {
        if (Log.isLoggable(Log.ERROR)) {
            Log.e("Message has no message part with role = root and no legacy part handling");
        }
        throw new IllegalArgumentException("Message has no message part with role = root and no"
                + " legacy part handling");
    }

    /**
     * Process the {@link MessagePart}s of the sub-tree with the supplied message part acting
     * as the root of that tree. If overriding, ensure you call the super class implementation
     *
     * @param rootMessagePart the root {@link MessagePart} of the sub-tree
     */
    @CallSuper
    protected void processParts(@NonNull MessagePart rootMessagePart) {
        mRootMessagePart = rootMessagePart;
        setRole(MessagePartUtils.getRole(rootMessagePart));
        if (mRootMessagePart.isContentReady()) {
            parse(mRootMessagePart);
        }

        // Deal with child parts
        processChildParts();

        // Set View type
        mMimeTypeTree = createMimeTypeTree();
    }

    /**
     * Process the child {@link MessagePart}s of this model's root MessagePart
     */
    protected void processChildParts() {
        mChildMessageParts = MessagePartUtils.getChildParts(getMessage(), mRootMessagePart);

        for (MessagePart childMessagePart : mChildMessageParts) {
            if (childMessagePart.isContentReady()) {
                if (parseChildPart(childMessagePart)) {
                    continue;
                }
            } else if (shouldDownloadContentIfNotReady(childMessagePart)) {
                childMessagePart.download(null);
            }

            if (MessagePartUtils.isResponseSummaryPart(childMessagePart)) {
                parseResponseSummary(childMessagePart);
                continue;
            }

            String mimeType = MessagePartUtils.getMimeType(childMessagePart);
            if (mimeType == null) continue;
            MessageModel childModel = mMessageModelManager.getNewModel(mimeType, getMessage());
            childModel.setParentMessageModel(this);
            childModel.processParts(childMessagePart);
            mChildMessageModels.add(childModel);
        }
    }

    /**
     * Override to handle processing of the metadata from a response summary (v2) part. This part
     * has a parent node id equal to this model's root message part.
     *
     * @param metadata parsed metadata from the response summary part
     */
    protected void processResponseSummaryMetadata(@NonNull ResponseSummaryMetadataV2 metadata) {
        // Standard operation is no-op
    }

    /**
     * Parse a response summary part that is a child of this message model. This currently only
     * supports {@link ResponseSummaryMetadataV2} summary objects. Models that use other objects
     * should override this method and handle parsing themselves.
     *
     * @param messagePart part whose role is 'response_summary'
     */
    @SuppressWarnings("WeakerAccess")
    protected void parseResponseSummary(MessagePart messagePart) {
        String mimeType = MessagePartUtils.getMimeType(messagePart);
        if (ResponseSummaryMetadataV2.MIME_TYPE.equals(mimeType)) {
            parseV2ResponseSummary(messagePart);
        } else {
            if (Log.isLoggable(Log.WARN)) {
                Log.w("Unknown response summary version: " + mimeType);
            }
        }
    }

    /**
     * Parse a version 2 response summary and call the
     * {@link MessageModel#processResponseSummaryMetadata(ResponseSummaryMetadataV2)} method.
     *
     * @param messagePart response summary part for a V2 metadata object
     */
    private void parseV2ResponseSummary(MessagePart messagePart) {
        InputStreamReader inputStreamReader = new InputStreamReader(messagePart.getDataStream());
        JsonReader reader = new JsonReader(inputStreamReader);
        ResponseSummaryMetadataV2 responseSummaryMetadata =
                getGson().fromJson(reader, ResponseSummaryMetadataV2.class);
        try {
            inputStreamReader.close();
        } catch (IOException e) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e("Failed to close input stream while parsing response summary", e);
            }
        }
        processResponseSummaryMetadata(responseSummaryMetadata);
    }

    /**
     * <p>
     * Override this method to optionally consume the supplied MessagePart without having the
     * framework attempt to generate a model from it.
     * <p>
     * The method defaults to returning false, upon which the framework will attempt to
     * generate a MessageModel instance from the currently registered MessageModel types.
     * </p>
     *
     * @param childMessagePart a MessagePart that has declared this MessageModel as its parent
     * @return true if the implementing class has consumed this MessagePart
     */
    protected boolean parseChildPart(@NonNull MessagePart childMessagePart) {
        return false;
    }

    private String createMimeTypeTree() {
        StringBuilder sb = new StringBuilder();
        if (mRootMessagePart != null) {
            sb.append(MessagePartUtils.getMimeType(mRootMessagePart));
            sb.append("[");
        }
        boolean prependComma = false;
        if (mChildMessageParts != null) {
            for (MessagePart childPart : mChildMessageParts) {
                if (prependComma) {
                    sb.append(",");
                }
                sb.append(MessagePartUtils.getMimeType(childPart));
                prependComma = true;
            }
        }
        if (mRootMessagePart != null) {
            sb.append("]");
        }
        return sb.toString();
    }

    /**
     * Provide a tree of mime types that correspond to legacy message parts. Usually this should
     * not be overridden. If it is then build the tree by putting the mime types of all message
     * parts in a comma separated string, enclosed by square brackets
     *
     * @return A string representing the mime type tree of all legacy message parts
     */
    protected String createLegacyMimeTypeTree() {
        StringBuilder sb = new StringBuilder();
        boolean prependComma = false;
        for (MessagePart part : getMessage().getMessageParts()) {
            if (prependComma) {
                sb.append(",");
            }
            sb.append(part.getMimeType());
            sb.append("[]");
            prependComma = true;
        }
        return sb.toString();
    }

    /**
     * Provide a tree of mime types that correspond to all the message parts. Usually
     * this should not be overridden. If it is then build the tree as follows
     * 1. The root level parts should be comma separated
     * 2. If a part has children, those mime types should be comma separated and enclosed in
     * square brackets (i.e. []).
     *
     * @return A string representing the mime type tree of all message parts
     */
    @NonNull
    public String getMimeTypeTree() {
        return mMimeTypeTree;
    }

    /**
     * Store data from the message and message parts that will need to be compared when determining
     * what data has changed. We need to cache this data because there is only one message object
     * provided by the SDK so we can't check old vs new at that time.
     */
    private void cacheMessageDataForDeepEquals() {
        mRecipientStatuses = getMessage().getRecipientStatus();
        mMessageUpdatedAt = getMessage().getUpdatedAt();
        Set<MessagePart> messageParts = getMessage().getMessageParts();
        mMessagePartTransferStatus = new HashSet<>(messageParts.size());
        mMessagePartUpdatedAt = new HashSet<>(messageParts.size());
        for (MessagePart messagePart : messageParts) {
            mMessagePartTransferStatus.add(messagePart.getTransferStatus());
            if (messagePart.getUpdatedAt() != null) {
                mMessagePartUpdatedAt.add(messagePart.getUpdatedAt());
            }
        }
        Identity sender = getMessage().getSender();
        if (sender != null) {
            mCachedSender = new IdentityItemModel(sender);
        }
    }

    /**
     * Get the {@link MessagePart} corresponding to the root node of this subtree. This may or
     * may not be the root of the entire model tree.
     *
     * @return message part corresponding to the root node of this model's subtree
     */
    @NonNull
    protected MessagePart getRootMessagePart() {
        return mRootMessagePart;
    }

    /**
     * Get the model that is a parent node to this model in the tree. If null, this model is the
     * root of the entire model tree.
     *
     * @return this models parent node or null if this model is the root of the tree
     */
    @SuppressWarnings("WeakerAccess")
    @Nullable
    public MessageModel getParentMessageModel() {
        return mParentMessageModel;
    }

    /**
     * Walk up the tree to find the root {@link MessageModel}.
     *
     * @return The root model for the tree or this model if it is the root of the tree
     */
    @SuppressWarnings("WeakerAccess")
    @Nullable
    public MessageModel getRootModelForTree() {
        MessageModel treeRoot = this;
        while (true) {
            MessageModel parent = treeRoot.getParentMessageModel();
            if (parent == null) {
                break;
            }
            treeRoot = parent;
        }
        return treeRoot;
    }

    private void setParentMessageModel(@NonNull MessageModel parent) {
        mParentMessageModel = parent;
    }

    /**
     * Provides the {@link MessagePart}s that are children of this model's root message part
     *
     * @return a {@link List<MessagePart>} containing the child message parts
     */
    @SuppressWarnings("unused")
    @Nullable
    protected List<MessagePart> getChildMessageParts() {
        return mChildMessageParts;
    }

    /**
     * Provides the {@link MessageModel}s that correspond to the models generated from the
     * children of this model's root message part
     *
     * @return a {@link List<MessageModel>} containing the child models
     */
    @Nullable
    protected List<MessageModel> getChildMessageModels() {
        return mChildMessageModels;
    }

    /**
     * Add a {@link MessageModel} to the child models of this model
     *
     * @param messageModel a {@link MessageModel} to be added
     */
    @SuppressWarnings("unused")
    protected void addChildMessageModel(MessageModel messageModel) {
        mChildMessageModels.add(messageModel);
    }

    /**
     * Set an {@link Action} that can be launched from this model.
     *
     * @param action an {@link Action} that this model can launch
     * @see Action
     */
    public void setAction(Action action) {
        mAction = action;
    }

    /**
     * Provides an {@link Action} event that this model can launch
     *
     * @return a {@link String} representing the action event
     * @see Action#getEvent()
     */
    @CallSuper
    @Nullable
    public String getActionEvent() {
        return mAction != null ? mAction.getEvent() : null;
    }

    /**
     * Provides data to go along with the {@link Action} event returned from
     * {@link MessageModel#getActionEvent()}
     * <p>
     * If there is no data to be supplied, return an empty {@link JsonObject}
     *
     * @return a {@link JsonObject} containing the data to be used for the {@link Action}
     * @see Action#getData()
     */
    @NonNull
    @CallSuper
    public JsonObject getActionData() {
        return mAction != null ? mAction.getData() : new JsonObject();
    }

    /**
     * Provides information on whether a {@link com.layer.xdk.ui.message.container.MessageContainer}
     * can display metadata or not, depending on whether {@link MessageModel#getTitle()},
     * {@link MessageModel#getDescription()} or {@link MessageModel#getFooter()} are available
     *
     * @return true if there is metadata available to display
     */
    @Bindable
    public boolean getHasMetadata() {
        return (!TextUtils.isEmpty(getTitle()))
                || !TextUtils.isEmpty(getDescription())
                || !TextUtils.isEmpty(getFooter());
    }

    /**
     * Provides the role corresponding to the <b>role</b> mime type attribute present on the
     * {@link MessagePart} backing this model
     *
     * @return a {@link String} representing the role
     */
    @SuppressWarnings("WeakerAccess")
    @Nullable
    protected String getRole() {
        return mRole;
    }

    /**
     * Set a <b>role</b> on this model
     *
     * @param role a {@link String} representing the role of this model
     */
    @SuppressWarnings("WeakerAccess")
    protected void setRole(@Nullable String role) {
        mRole = role;
    }

    /**
     * Provides the child {@link MessageModel}s of this model that have the specified <b>role</b>
     * as returned by their {@link MessageModel#getRole()}
     *
     * @param role a {@link String} specifying the role for which to get child models
     * @return a {@link List<MessageModel>} containing the child models of this model corresponding
     * to the specified <b>role</b>
     */
    @SuppressWarnings("WeakerAccess")
    @NonNull
    protected List<MessageModel> getChildMessageModelsWithRole(@NonNull String role) {
        List<MessageModel> models = new ArrayList<>();
        if (role.equals(mRole)) {
            models.add(this);
        }

        if (mChildMessageModels != null && !mChildMessageModels.isEmpty()) {
            for (MessageModel childModel : mChildMessageModels) {
                if (role.equals(childModel.getRole())) {
                    models.add(childModel);
                }
            }
        }

        return models;
    }

    /**
     * Provides the background color that the
     * {@link com.layer.xdk.ui.message.container.MessageContainer} should use when rendering the
     * view corresponding to this model
     * <p>
     * Default is {@link android.R.color#transparent}
     *
     * @return a color resource Id to use as the background for the display of this model
     */
    @Bindable
    @ColorRes
    public int getBackgroundColor() {
        return android.R.color.transparent;
    }

    /**
     * Is the {@link Message} backing this model from the currently authenticated user
     *
     * @return true if the {@link Message} from the currently authenticated user
     */
    @Bindable
    public final boolean isMessageFromMe() {
        if (mAuthenticatedUserId != null) {
            return mAuthenticatedUserId.equals(mSenderId);
        }
        if (Log.isLoggable(Log.ERROR)) {
            Log.e("Failed to check if message is from me. Authenticated user is null Message: "
                    + getMessage());
        }
        throw new IllegalStateException("Failed to check if message is from me. Authenticated "
                + "user is null Message: " + getMessage());
    }

    /**
     * The {@link MessageModelManager} instance to be used with this model to generate child
     * {@link MessageModel}s
     *
     * @param messageModelManager a {@link MessageModelManager} instance
     */
    public final void setMessageModelManager(@NonNull MessageModelManager messageModelManager) {
        mMessageModelManager = messageModelManager;
    }

    /**
     * @return the {@link Context} supplied at instantiation
     */
    protected Context getAppContext() {
        return mContext;
    }

    /**
     * @return the {@link LayerClient} supplied at instantiation
     */
    protected LayerClient getLayerClient() {
        return mLayerClient;
    }

    /**
     * @return the {@link Message} backing the model tree
     */
    @NonNull
    public final Message getMessage() {
        return mMessage;
    }

    /**
     * @return the {@link Uri} representing the user id of the currently authenticated user,
     */
    @SuppressWarnings("WeakerAccess")
    @Nullable
    protected final Uri getAuthenticatedUserId() {
        return mAuthenticatedUserId;
    }

    /**
     * @return the {@link Uri} representing the user id of the sender of the {@link Message} backing
     * this model tree
     */
    @Nullable
    public final Uri getSenderId() {
        return mSenderId;
    }

    /**
     * @return the number of participants in the {@link com.layer.sdk.messaging.Conversation} that
     * the backing {@link Message} belongs to
     */
    public final int getParticipantCount() {
        return mParticipantCount;
    }

    /**
     * @return a {@link MessageSenderRepository} instance used to send {@link Message}s
     * corresponding to this model type
     */
    // TODO AND-1287 Inject this
    @NonNull
    protected MessageSenderRepository getMessageSenderRepository() {
        if (mMessageSenderRepository == null) {
            mMessageSenderRepository = new MessageSenderRepository(getAppContext(), getLayerClient());
        }
        return mMessageSenderRepository;
    }

    /**
     * @return an {@link IdentityFormatter} instance used to format {@link Identity} objects for
     * display
     */
    protected IdentityFormatter getIdentityFormatter() {
        return mIdentityFormatter;
    }

    /**
     * @param identityFormatter an {@link IdentityFormatter} instance to use for formatting
     *                          {@link Identity} objects for display
     */
    public final void setIdentityFormatter(IdentityFormatter identityFormatter) {
        mIdentityFormatter = identityFormatter;
    }

    /**
     * @return a {@link DateFormatter} instance used to format dates and times
     */
    @SuppressWarnings("unused")
    protected DateFormatter getDateFormatter() {
        return mDateFormatter;
    }

    /**
     * @param dateFormatter a {@link DateFormatter} instance used to format dates and times
     */
    public final void setDateFormatter(DateFormatter dateFormatter) {
        mDateFormatter = dateFormatter;
    }

    /**
     * @return an {@link ImageCacheWrapper} instance used to display images
     */
    public ImageCacheWrapper getImageCacheWrapper() {
        return mImageCacheWrapper;
    }

    /**
     * @param imageCacheWrapper an {@link ImageCacheWrapper} used to display images
     */
    public void setImageCacheWrapper(ImageCacheWrapper imageCacheWrapper) {
        mImageCacheWrapper = imageCacheWrapper;
    }

    /**
     * Get the {@link MessageGrouping} for this model when shown in an adapter. This will only be
     * valid for root models and should be null for inner models. No grouping will be set if this
     * is not passed through a {@link com.layer.xdk.ui.message.adapter.GroupingCalculator}.
     *
     * @return set of grouping values for the root model or null if it is an inner model or none
     * has been set.
     */
    @Nullable
    public EnumSet<MessageGrouping> getGrouping() {
        return mGrouping;
    }

    /**
     * Set the {@link MessageGrouping} value for this model. This is usually used to handle
     * decoration and view state in a {@link MessageModelAdapter}.
     *
     * @param grouping set of groupings
     */
    public void setGrouping(EnumSet<MessageGrouping> grouping) {
        mGrouping = grouping;
    }

    /**
     * @return true if the {@link Message} backing this model is the newest message sent by the
     * currently authenticated user
     */
    public boolean isMyNewestMessage() {
        return mMyNewestMessage;
    }

    /**
     * Set whether the {@link Message} backing this model is the newest message sent by the
     * currently authenticated user.
     *
     * @param myNewestMessage true if the {@link Message} backing this model is the newest message
     *                        sent by the currently authenticated user
     */
    public void setMyNewestMessage(boolean myNewestMessage) {
        mMyNewestMessage = myNewestMessage;
    }

    /**
     * @return a Gson instance to use for {@link MessagePart} parsing
     */
    protected Gson getGson() {
        return mGson;
    }

    /**
     * Set a Gson instance on this model.
     *
     * @param gson Gson instance to use
     */
    public final void setGson(Gson gson) {
        mGson = gson;
    }

    /**
     * Perform an equals check on most properties. Child model equality is checked but parent
     * models are skipped as this will produce infinite recursion.
     * <p>
     * This is primarily used for calculations with {@link android.support.v7.util.DiffUtil}.
     *
     * @param other model to compare to
     * @return true if all properties are equal
     */
    @SuppressWarnings({"SimplifiableIfStatement", "RedundantIfStatement"})
    public boolean deepEquals(@NonNull MessageModel other) {
        if (getGrouping() == null ? other.getGrouping() != null
                : !getGrouping().containsAll(other.getGrouping())) {
            return false;
        }
        if (isMyNewestMessage() != other.isMyNewestMessage()) {
            return false;
        }
        if (getAuthenticatedUserId() == null ? other.getAuthenticatedUserId() != null
                : !getAuthenticatedUserId().equals(other.getAuthenticatedUserId())) {
            return false;
        }
        if (getSenderId() == null ? other.getSenderId() != null
                : !getSenderId().equals(other.getSenderId())) {
            return false;
        }
        if (getParticipantCount() != other.getParticipantCount()) {
            return false;
        }
        if (getRole() == null ? other.getRole() != null : !getRole().equals(other.getRole())) {
            return false;
        }
        if (!getActionData().equals(other.getActionData())) {
            return false;
        }
        if (!getMimeTypeTree().equals(other.getMimeTypeTree())) {
            return false;
        }
        if (getContainerViewLayoutId() != other.getContainerViewLayoutId()) {
            return false;
        }
        if (getViewLayoutId() != other.getViewLayoutId()) {
            return false;
        }
        if (getHasContent() != other.getHasContent()) {
            return false;
        }
        if (getPreviewText() == null ? other.getPreviewText() != null
                : !getPreviewText().equals(other.getPreviewText())) {
            return false;
        }
        if (getTitle() == null ? other.getTitle() != null
                : !getTitle().equals(other.getTitle())) {
            return false;
        }
        if (getDescription() == null ? other.getDescription() != null
                : !getDescription().equals(other.getDescription())) {
            return false;
        }
        if (getFooter() == null ? other.getFooter() != null
                : !getFooter().equals(other.getFooter())) {
            return false;
        }
        if (getHasMetadata() != other.getHasMetadata()) {
            return false;
        }
        if (getBackgroundColor() != other.getBackgroundColor()) {
            return false;
        }
        if (isMessageFromMe() != other.isMessageFromMe()) {
            return false;
        }
        if (getChildMessageModels() == null) {
            if (other.getChildMessageModels() != null) {
                return false;
            }
        } else {
            if (other.getChildMessageModels() == null) {
                return false;
            }
            if (getChildMessageModels().size() != other.getChildMessageModels().size()) {
                return false;
            }
            Iterator<MessageModel> iterator = getChildMessageModels().iterator();
            Iterator<MessageModel> otherIterator = other.getChildMessageModels().iterator();
            while (iterator.hasNext()) {
                if (!iterator.next().deepEquals(otherIterator.next())) {
                    return false;
                }
            }
        }

        if (mMessageUpdatedAt == null ? other.mMessageUpdatedAt != null
                : !mMessageUpdatedAt.equals(other.mMessageUpdatedAt)) {
            return false;
        }
        if (mMessagePartUpdatedAt == null ? other.mMessagePartUpdatedAt != null
                : !mMessagePartUpdatedAt.equals(other.mMessagePartUpdatedAt)) {
            return false;
        }

        if (mRecipientStatuses == null ? other.mRecipientStatuses != null
                : !mRecipientStatuses.equals(other.mRecipientStatuses)) {
            return false;
        }

        if (mMessagePartTransferStatus == null ? other.mMessagePartTransferStatus != null
                : !mMessagePartTransferStatus.equals(other.mMessagePartTransferStatus)) {
            return false;
        }

        if (!Arrays.equals(mMessageLocalData, other.mMessageLocalData)) {
            return false;
        }

        if (mCachedSender == null ? other.mCachedSender != null
                : !mCachedSender.deepEquals(other.mCachedSender)) {
            return false;
        }

        // Don't bother checking parent model as that will infinitely recurse
        return true;
    }
}
