package com.layer.xdk.ui.message.image.cache.requesthandlers;

import android.net.Uri;

import com.layer.xdk.ui.message.MessagePartUtils;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.MessagePart;
import com.layer.sdk.query.Queryable;
import com.squareup.picasso.Request;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.squareup.picasso.Picasso.LoadedFrom;

/**
 * Handles Picasso load requests for Layer MessagePart content.  If the content is not ready
 * (e.g. MessagePart.isContentReady() is `false`), registers a LayerProgressListener, downloads
 * the part, and waits for completion.
 */
public class MessagePartRequestHandler extends com.squareup.picasso.RequestHandler {
    private final LayerClient mLayerClient;

    public MessagePartRequestHandler(LayerClient layerClient) {
        mLayerClient = layerClient;
    }

    @Override
    public boolean canHandleRequest(Request data) {
        Uri uri = data.uri;
        if (!"layer".equals(uri.getScheme())) return false;
        List<String> segments = uri.getPathSegments();
        if (segments.size() != 4) return false;
        if (!segments.get(2).equals("parts")) return false;
        return true;
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        Queryable queryable = mLayerClient.get(request.uri);
        if (!(queryable instanceof MessagePart)) return null;
        MessagePart part = (MessagePart) queryable;
        if (part.isContentReady()) return new Result(part.getDataStream(), LoadedFrom.DISK);
        if (!MessagePartUtils.downloadMessagePart(part, 3, TimeUnit.MINUTES)) return null;
        return new Result(part.getDataStream(), LoadedFrom.NETWORK);
    }
}
