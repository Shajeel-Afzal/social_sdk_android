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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import sumatodev.com.social.R;
import sumatodev.com.social.adapters.holders.CommentViewHolder;
import sumatodev.com.social.controllers.CommentLikeController;
import sumatodev.com.social.model.Comment;
import sumatodev.com.social.ui.activities.PostDetailsActivity;

/**
 * Created by alexey on 10.05.17.
 */

public class CommentsAdapter extends RecyclerView.Adapter<CommentViewHolder> {
    private List<Comment> list = new ArrayList<>();
    private Callback callback;
    private PostDetailsActivity activity;

    public CommentsAdapter(PostDetailsActivity activity) {
        this.activity = activity;
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_list_item, parent, false);
        return new CommentViewHolder(view, callback, clickListener());
    }

    private CommentViewHolder.OnClickListener clickListener() {
        return new CommentViewHolder.OnClickListener() {
            @Override
            public void onLikeClick(CommentLikeController likeController, int position) {
                Comment comment = getItemByPosition(position);
                likeController.handleLikeClickAction(activity, comment);
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

    public interface Callback {
        void onLongItemClick(View view, int position);

        void onAuthorClick(String authorId, View view);
    }
}
