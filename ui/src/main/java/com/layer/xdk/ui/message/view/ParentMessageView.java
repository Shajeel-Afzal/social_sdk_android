package com.layer.xdk.ui.message.view;


import android.support.annotation.NonNull;
import android.view.View;

import com.layer.xdk.ui.message.adapter.MessageModelAdapter;
import com.layer.xdk.ui.message.model.MessageModel;

/**
 * Allows a view to handle inner view inflations when used with a
 * {@link MessageModelAdapter} instance. Primarily used for complex
 * {@link MessageModel} views that can contain {@link MessageModel}s and need to lay those out
 * inside a certain view.
 */
public interface ParentMessageView {

    /**
     * Inflate views needed to display the {@link MessageModel} passed in the arguments. Do NOT
     * store the model or update data based on the model in this method.
     *
     * @param model model whose child(ren) should be used for view inflation
     * @param longClickListener listener to optionally set on child layouts that should trigger an
     *                          item long click in the adapter
     */
    <T extends MessageModel> void inflateChildLayouts(@NonNull T model,
            @NonNull View.OnLongClickListener longClickListener);
}
