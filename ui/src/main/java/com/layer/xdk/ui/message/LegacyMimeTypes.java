package com.layer.xdk.ui.message;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LegacyMimeTypes {

    public final static String LEGACY_TEXT_MIME_TYPE = Collections.singleton("text/plain").toString();
    public final static String LEGACY_LOCATION_MIME_TYPE = Collections.singleton("location/coordinate").toString();

    public static final String LEGACY_IMAGE_MIME_TYPE_PREVIEW = "image/jpeg+preview";
    public static final String LEGACY_IMAGE_MIME_TYPE_INFO = "application/json+imageSize";
    public static final String LEGACY_IMAGE_MIME_TYPE_IMAGE_PREFIX = "image/";
    public static final String LEGACY_SINGLE_PART_MIME_TYPES =
            Collections.singleton(LEGACY_IMAGE_MIME_TYPE_IMAGE_PREFIX).toString();
    public static final String LEGACY_THREE_PART_MIME_TYPES;
    static {
        Set<String> threePartMimeTypes = new HashSet<>(3);
        threePartMimeTypes.add(LEGACY_IMAGE_MIME_TYPE_INFO);
        threePartMimeTypes.add(LEGACY_IMAGE_MIME_TYPE_PREVIEW);
        threePartMimeTypes.add(LEGACY_IMAGE_MIME_TYPE_IMAGE_PREFIX);
        LEGACY_THREE_PART_MIME_TYPES = threePartMimeTypes.toString();
    }
}
