package com.sumatodev.social_chat_sdk.main.adapters.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.adapters.ChatAdpater;
import com.sumatodev.social_chat_sdk.main.model.Message;

/**
 * Created by Ali on 13/02/2018.
 */

public class ChatMyHolder extends RecyclerView.ViewHolder {

    public TextView messageText;
    public TextView textTime;
    private Context context;
    public LinearLayout textLayout;
    private ChatAdpater.Callback callback;


    public ChatMyHolder(View itemView, ChatAdpater.Callback callback) {
        super(itemView);
        this.callback = callback;
        bindViews(itemView);
        this.context = itemView.getContext();
    }

    private void bindViews(View itemView) {
        messageText = itemView.findViewById(R.id.messageText);
        textTime = itemView.findViewById(R.id.textTime);
        textLayout = itemView.findViewById(R.id.textLayout);

        if (callback != null) {
            textLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        callback.onLongItemClick(v, position);
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    public void setData(Message model) {

        if (model != null) {
            messageText.setText(model.getText());
            textTime.setText(DateUtils.formatDateTime(context, (long) model.getCreatedAt(),
                    DateUtils.FORMAT_SHOW_TIME));
        }

    }


}
