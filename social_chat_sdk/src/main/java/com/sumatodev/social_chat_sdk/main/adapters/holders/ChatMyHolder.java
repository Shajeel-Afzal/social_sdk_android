package com.sumatodev.social_chat_sdk.main.adapters.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.model.Message;

/**
 * Created by Ali on 13/02/2018.
 */

public class ChatMyHolder extends RecyclerView.ViewHolder {

    public TextView messageText;
    public TextView textTime;
    private Context context;


    public ChatMyHolder(View itemView) {
        super(itemView);
        bindViews(itemView);
        this.context = itemView.getContext();
    }

    private void bindViews(View itemView) {
        messageText = itemView.findViewById(R.id.messageText);
        textTime = itemView.findViewById(R.id.textTime);
    }

    public void setData(Message model) {

        messageText.setText(model.getText().trim());
        textTime.setText(DateUtils.formatDateTime(context, (long) model.getCreatedAt(),
                DateUtils.FORMAT_SHOW_TIME));

    }


}
