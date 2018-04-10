package com.sumatodev.social_chat_sdk.main.adapters.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.model.Message;


/**
 * Created by Ali on 13/02/2018.
 */

public class ChatMyHolder extends RecyclerView.ViewHolder {

    public TextView messageText;
    public TextView textTime;
    private Context context;
    public LinearLayout textLayout;
    public ImageView messageImage;
    private ProgressBar progressBar;


    public ChatMyHolder(View itemView, final OnClickListener onClickListener) {
        super(itemView);
        this.context = itemView.getContext();

        messageText = itemView.findViewById(R.id.messageText);
        textTime = itemView.findViewById(R.id.textTime);
        textLayout = itemView.findViewById(R.id.textLayout);
        messageImage = itemView.findViewById(R.id.messageImage);
        progressBar = itemView.findViewById(R.id.progressBar);

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = getAdapterPosition();
                if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                    onClickListener.onItemLongClick(position, v);
                    return true;
                }
                return false;
            }
        });

        if (messageImage != null) {
            messageImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                        onClickListener.onImageClick(position, v);
                    }
                }
            });
        }
    }

    public void bindTextData(final Message model) {

        if (model != null) {

            textTime.setText(DateUtils.formatDateTime(context, (long) model.getCreatedAt(),
                    DateUtils.FORMAT_SHOW_TIME));


            messageText.setText(model.getText());

        }

    }

    public void bindImageData(Message message) {

        textTime.setText(DateUtils.formatDateTime(context, (long) message.getCreatedAt(),
                DateUtils.FORMAT_SHOW_TIME));


        progressBar.setVisibility(View.VISIBLE);

        Glide.with(context)
                .load(message.getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .fitCenter()
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(messageImage);


    }


    public interface OnClickListener {
        void onItemLongClick(int position, View view);

        void onImageClick(int position, View view);
    }
}
