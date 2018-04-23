package com.layer.xdk.ui.message.response;


import java.util.HashMap;
import java.util.Map;

/**
 * POJO that represents the JSON object in a V2 response summary message part.
 */
public class ResponseSummaryMetadataV2 extends HashMap<String, Map<String, SummaryStateMetadata>> {
    public static final String MIME_TYPE = "application/vnd.layer.responsesummary-v2+json";
}
