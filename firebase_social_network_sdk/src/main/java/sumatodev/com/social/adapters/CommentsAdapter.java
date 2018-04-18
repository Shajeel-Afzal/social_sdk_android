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

package sumatodev.com.social.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import sumatodev.com.social.R;
import sumatodev.com.social.adapters.holders.CommentViewHolder;
import sumatodev.com.social.controllers.CommentLikeController;
import sumatodev.com.social.managers.CommentManager;
import sumatodev.com.social.managers.listeners.OnCommentChangedListener;
import sumatodev.com.social.model.Comment;
import sumatodev.com.social.ui.activities.PostDetailsActivity;
import sumatodev.com.social.utils.LogUtil;

/**
 * Created by alexey on 10.05.17.
 */

public class CommentsAdapter extends RecyclerView.Adapter<CommentViewHolder> {
    private static final String TAG = CommentsAdapter.class.getSimpleName();
    private List<Comment> list = new ArrayList<>();
    private Callback callback;
    private PostDetailsActivity activity;
    private int selectedCommentPosition = -1;

    public CommentsAdapter(PostDetailsActivity activity) {
        this.activity = activity;
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent, false);
        return new CommentViewHolder(view, onClickListener());
    }

    private CommentViewHolder.OnClickListener onClickListener() {
        return new CommentViewHolder.OnClickListener() {
            @Override
            public void onLongItemClick(View view, int position) {
                selectedCommentPosition = position;
                if (callback != null) {
                    callback.onLongItemClick(view, position);
                }
            }

            @Override
            public void onLikeClick(CommentLikeController likeController, int position) {
                Comment comment = getItemByPosition(position);
                likeController.handleLikeClickAction(activity, comment);

            }

            @Override
            public void onAuthorClick(int position, View view) {
                if (callback != null) {
                    callback.onAuthorClick(getItemByPosition(position).getAuthorId(), view);
                }
            }

            @Override
            public void onUserClick(String userId) {
                if (callback != null) {
                    callback.onAuthorClick(userId, null);
                }
            }
        };
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        holder.itemView.setLongClickable(true);
        holder.bindData(getItemByPosition(position));
    }

    public Comment getItemByPosition(int position) {
        return list.get(position);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setList(List<Comment> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void cleanSelectedPosition() {
        selectedCommentPosition = -1;
    }

    public void removeComment() {
        list.remove(selectedCommentPosition);
        notifyItemRemoved(selectedCommentPosition);
    }

    public void updateComment() {
        if (selectedCommentPosition != -1) {
            Comment comment = getItemByPosition(selectedCommentPosition);
            CommentManager.getInstance(activity).getSingleCommentValue(comment.getPostId(), comment.getId(),
                    commentChangedListener(selectedCommentPosition));
        }
    }

    private OnCommentChangedListener commentChangedListener(final int position) {
        return new OnCommentChangedListener() {
            @Override
            public void onObjectChanged(Comment obj) {
                list.set(position, obj);
                notifyItemChanged(position);
            }

            @Override
            public void onError(String errorText) {
                LogUtil.logDebug(TAG, errorText);
            }
        };
    }

    public interface Callback {
        void onLongItemClick(View view, int position);

        void onAuthorClick(String authorId, View view);
    }
}
