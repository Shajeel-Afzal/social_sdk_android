package com.layer.xdk.ui.message.response.crdt;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.layer.xdk.ui.util.Log;

import java.lang.reflect.Type;

public class ORSetDeserializer implements JsonDeserializer<ORSet> {

    @Override
    public ORSet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        String type = json.getAsJsonObject().get("type").getAsString();
        if (type != null) {
            switch (type) {
                case FirstWriterWinsRegister.TYPE:
                    return context.deserialize(json, FirstWriterWinsRegister.class);
                case LastWriterWinsRegister.TYPE:
                    return context.deserialize(json, LastWriterWinsRegister.class);
                case LastWriterWinsNullableRegister.TYPE:
                    return context.deserialize(json, LastWriterWinsNullableRegister.class);
                case StandardORSet.TYPE:
                    return context.deserialize(json, StandardORSet.class);
            }
        }
        if (Log.isLoggable(Log.WARN)) {
            Log.w("Unknown type during OR-Set deserialization. JSON: " + json.getAsString());
        }

        return null;
    }
}
