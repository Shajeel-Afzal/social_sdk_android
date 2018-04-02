/*
 *
 * Copyright 2017 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package sumatodev.com.social.adapters.holders;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import sumatodev.com.social.R;
import sumatodev.com.social.adapters.CommentsAdapter;
import sumatodev.com.social.controllers.CommentLikeController;
import sumatodev.com.social.managers.CommentManager;
import sumatodev.com.social.managers.ProfileManager;
import sumatodev.com.social.managers.listeners.OnObjectChangedListener;
import sumatodev.com.social.managers.listeners.OnObjectExistListener;
import sumatodev.com.social.model.Comment;
import sumatodev.com.social.model.Like;
import sumatodev.com.social.model.Profile;
import sumatodev.com.social.utils.FormatterUtil;
import sumatodev.com.social.views.ExpandableTextView;

/**
 * Created by alexey on 10.05.17.
 */

public class CommentViewHolder extends RecyclerView.ViewHolder {

    private final ImageView avatarImageView;
    private final ExpandableTextView commentTextView;
    private final TextView dateTextView;
    private final ProfileManager profileManager;
    private TextView likes_count;
    private TextView like_action;
    private ViewGroup likeContainer;
    private CommentsAdapter.Callback callback;
    private Context context;
    private CommentManager commentManager;
    private CommentLikeController commentLikeController;

    public CommentViewHolder(View itemView, final CommentsAdapter.Callback callback, final OnClickListener onClickListener) {
        super(itemView);

        this.callback = callback;
        this.context = itemView.getContext();
        profileManager = ProfileManager.getInstance(itemView.getContext().getApplicationContext());
        commentManager = CommentManager.getInstance(itemView.getContext().getApplicationContext());

        avatarImageView = itemView.findViewById(R.id.avatarImageView);
        commentTextView = itemView.findViewById(R.id.commentText);
        dateTextView = itemView.findViewById(R.id.dateTextView);
        like_action = itemView.findViewById(R.id.like_action);
        likes_count = itemView.findViewById(R.id.likes_count);
        likeContainer = itemView.findViewById(R.id.likeContainer);

        if (callback != null) {
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
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
        if (onClickListener != null) {
            likeContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onClickListener.onLikeClick(commentLikeController, position);
                    }
                }
            });
        }
    }

    public void bindData(Comment comment) {

        commentLikeController = new CommentLikeController(context, comment, likes_count, like_action, true);

        final String authorId = comment.getAuthorId();
        if (comment.getText() != null) {
            commentTextView.setText(comment.getText());

            CharSequence date = FormatterUtil.getRelativeTimeSpanString(context, comment.getCreatedDate());
            dateTextView.setText(date);

        }

        likes_count.setText(String.valueOf(comment.getLikesCount()));

        if (authorId != null) {
            profileManager.getProfileSingleValue(authorId, createOnProfileChangeListener(commentTextView,
                    avatarImageView, comment.getText()));

            avatarImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onAuthorClick(authorId, v);
                }
            });
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            commentManager.hasCurrentUserLikeComment(comment.getPostId(), comment.getId(), onObjectExistListener());
        }
    }

    private OnObjectChangedListener<Profile> createOnProfileChangeListener(final ExpandableTextView expandableTextView,
                                                                           final ImageView avatarImageView, final String comment) {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                String userName = obj.getUsername();
                fillComment(userName, comment, expandableTextView);

                if (obj.getPhotoUrl() != null) {
                    Glide.with(context)
                            .load(obj.getPhotoUrl())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .crossFade()
                            .error(R.drawable.ic_stub)
                            .into(avatarImageView);
                }
            }
        };
    }

    private void fillComment(String userName, String comment, ExpandableTextView commentTextView) {
        Spannable contentString = new SpannableStringBuilder(userName + "   " + comment);
        contentString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.highlight_text)),
                0, userName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        commentTextView.setText(contentString);
    }

    private OnObjectExistListener<Like> onObjectExistListener() {
        return new OnObjectExistListener<Like>() {
            @Override
            public void onDataChanged(boolean exist) {
                commentLikeController.initLike(exist);
            }
        };
    }

    public interface OnClickListener {
        void onLikeClick(CommentLikeController likeController, int position);
    }
}
