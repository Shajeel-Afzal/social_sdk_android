package com.layer.xdk.ui.identity.adapter.viewholder;

import com.layer.sdk.messaging.Identity;
import com.layer.xdk.ui.fourpartitem.adapter.viewholder.FourPartItemVHModel;
import com.layer.xdk.ui.identity.IdentityFormatter;
import com.layer.xdk.ui.identity.adapter.IdentityItemModel;
import com.layer.xdk.ui.util.DateFormatter;
import com.layer.xdk.ui.message.image.cache.ImageCacheWrapper;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

public class IdentityItemVHModel extends FourPartItemVHModel<IdentityItemModel> {

    @SuppressWarnings("WeakerAccess")
    protected DateFormatter mDateFormatter;

    @Inject
    public IdentityItemVHModel(IdentityFormatter identityFormatter,
            ImageCacheWrapper imageCacheWrapper,
            DateFormatter dateFormatter) {
        super(identityFormatter, imageCacheWrapper);
        mDateFormatter = dateFormatter;
    }

    @Override
    public String getTitle() {
        return getIdentityFormatter().getDisplayName(getItem().getIdentity());
    }

    @Override
    public String getSubtitle() {
        return getIdentityFormatter().getSecondaryInfo(getItem().getIdentity());
    }

    @Override
    public String getAccessoryText() {
        return mDateFormatter.formatTimeDay(getItem().getIdentity().getLastSeenAt());
    }

    @Override
    public boolean isSecondaryState() {
        return false;
    }

    @Override
    public Set<Identity> getIdentities() {
        return Collections.singleton(getItem().getIdentity());
    }
}
