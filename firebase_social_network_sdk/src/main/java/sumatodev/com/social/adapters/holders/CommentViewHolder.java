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
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import sumatodev.com.social.R;
import sumatodev.com.social.controllers.CommentLikeController;
import sumatodev.com.social.managers.CommentManager;
import sumatodev.com.social.managers.ProfileManager;
import sumatodev.com.social.managers.listeners.OnObjectChangedListener;
import sumatodev.com.social.managers.listeners.OnObjectExistListener;
import sumatodev.com.social.model.Comment;
import sumatodev.com.social.model.Like;
import sumatodev.com.social.model.Mention;
import sumatodev.com.social.model.Profile;
import sumatodev.com.social.utils.FormatterUtil;
import sumatodev.com.social.views.ExpandableTextView;
import sumatodev.com.social.views.mention.Mentionable;

/**
 * Created by alexey on 10.05.17.
 */

public class CommentViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = CommentViewHolder.class.getSimpleName();
    private final ImageView avatarImageView;
    private final TextView commentTextView;
    private final TextView dateTextView;
    private final ProfileManager profileManager;
    private Context context;
    private TextView likes_count;
    private ImageView like_action;
    private ViewGroup commentLikeContainer;
    private CommentLikeController commentLikeController;
    private CommentManager commentManager;
    private TextView authorName;
    private final int orange;
    private OnClickListener clickListener;

    public CommentViewHolder(View itemView, final OnClickListener onClickListener) {
        super(itemView);

        this.context = itemView.getContext();
        this.clickListener = onClickListener;
        profileManager = ProfileManager.getInstance(itemView.getContext().getApplicationContext());
        commentManager = CommentManager.getInstance(itemView.getContext().getApplicationContext());
        this.orange = ContextCompat.getColor(context, R.color.mentions_default_color);

        avatarImageView = itemView.findViewById(R.id.avatarImageView);
        commentTextView = itemView.findViewById(R.id.commentText);
        dateTextView = itemView.findViewById(R.id.dateTextView);
        like_action = itemView.findViewById(R.id.likesImageView);
        likes_count = itemView.findViewById(R.id.likes_count);
        authorName = itemView.findViewById(R.id.authorName);
        commentLikeContainer = itemView.findViewById(R.id.commentLikeContainer);

        if (onClickListener != null) {
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onClickListener.onLongItemClick(v, position);
                        return true;
                    }

                    return false;
                }
            });

            commentLikeContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onClickListener.onLikeClick(commentLikeController, position);
                    }
                }
            });

            avatarImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onClickListener.onAuthorClick(position, v);
                    }
                }
            });
        }
    }

    public void bindData(Comment comment) {

        Log.d(TAG, "Comments: " + comment.getId());
        commentLikeController = new CommentLikeController(context, comment, likes_count, like_action, true);

        final String authorId = comment.getAuthorId();
        if (authorId != null)
            profileManager.getProfileSingleValue(authorId, createOnProfileChangeListener(avatarImageView, authorName));

        if (comment.getText() != null) {
            commentTextView.setText(comment.getText());

            if (comment.getMentions() != null) {
                highlightMentions(comment.getText(), commentTextView, comment.getMentions());
            }
        }

        if (comment.getLikesCount() > 0) {
            likes_count.setText(String.valueOf(comment.getLikesCount()));
        }

        CharSequence date = FormatterUtil.getRelativeTimeSpanString(context, comment.getCreatedDate());
        dateTextView.setText(date);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            commentManager.hasCurrentUserLikeComment(comment, onObjectExistListener());
        }
    }

    private OnObjectChangedListener<Profile> createOnProfileChangeListener(final ImageView avatarImageView,
                                                                           final TextView authorName) {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                String userName = obj.getUsername();
                //fillComment(userName, comment, expandableTextView);
                authorName.setText(userName);

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

    /**
     * Highlights all the {@link Mentionable}s in the test {@link Comment}.
     */
    private void highlightMentions(String commentText, TextView commentTextView, final List<Mentionable> mentions) {
        if (commentText != null && mentions != null && !mentions.isEmpty()) {
            final Spannable spannable = new SpannableString(commentText);

            for (final Mentionable mention : mentions) {
                if (mention != null) {
                    final int start = mention.getMentionOffset();
                    final int end = start + mention.getMentionLength();

                    if (commentText.length() >= end) {
                        spannable.setSpan(new ForegroundColorSpan(orange), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        spannable.setSpan(new ClickableSpan() {
                            @Override
                            public void onClick(View widget) {
                                if (clickListener != null && mention.getUserId() != null) {
                                    clickListener.onUserClick(mention.getUserId());
                                }
                            }

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                ds.setUnderlineText(false);
                            }
                        }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


                        commentTextView.setMovementMethod(LinkMovementMethod.getInstance());
                        commentTextView.setText(spannable);
                    } else {
                        //Something went wrong.  The expected text that we're trying to highlight does not
                        // match the actual text at that position.
                        Log.w("Mentions Sample", "Mention lost. [" + mention + "]");
                    }
                }
            }

        }
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
        void onLongItemClick(View view, int position);

        void onLikeClick(CommentLikeController likeController, int position);

        void onAuthorClick(int position, View view);

        void onUserClick(String userId);
    }
}