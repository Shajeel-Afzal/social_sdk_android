package com.layer.xdk.ui.message.response;


import android.net.Uri;
import android.support.annotation.NonNull;

import com.layer.xdk.ui.message.response.crdt.OrOperationResult;

import java.util.List;
import java.util.UUID;

/**
 * Metadata for a choice response message.
 */
@SuppressWarnings("WeakerAccess")
public class ChoiceResponseMetadata extends ResponseMetadata {

    private transient final String mStatusText;

    /**
     * @param messageIdToRespondTo full ID of the message this is in response to
     * @param partIdToRespondTo UUID of the message part this is in response to
     * @param statusText text to use for the status message part
     * @param results list of OR set operations that resulted in the desired state
     */
    public ChoiceResponseMetadata(@NonNull Uri messageIdToRespondTo,
            @NonNull UUID partIdToRespondTo, @NonNull String statusText,
            List<OrOperationResult> results) {
        super(messageIdToRespondTo, partIdToRespondTo, results);
        mStatusText = statusText;
    }

    /**
     * @return text to be displayed in the status message
     */
    @NonNull
    public String getStatusText() {
        return mStatusText;
    }
}
