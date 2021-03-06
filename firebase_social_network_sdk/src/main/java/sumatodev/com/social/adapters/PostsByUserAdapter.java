/*
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
 */

package sumatodev.com.social.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import sumatodev.com.social.R;
import sumatodev.com.social.adapters.holders.LoadViewHolder;
import sumatodev.com.social.adapters.holders.PostViewHolder;
import sumatodev.com.social.controllers.LikeController;
import sumatodev.com.social.managers.PostManager;
import sumatodev.com.social.managers.listeners.OnDataChangedListener;
import sumatodev.com.social.model.Post;
import sumatodev.com.social.ui.activities.BaseActivity;
import sumatodev.com.social.utils.Utils;


public class PostsByUserAdapter extends BasePostsAdapter {
    public static final String TAG = PostsByUserAdapter.class.getSimpleName();

    private String userId;
    private CallBack callBack;

    public PostsByUserAdapter(final BaseActivity activity, String userId) {
        super(activity);
        this.userId = userId;
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TEXT_VIEW || viewType == TEXT_COLORED_VIEW || viewType == LINK_VIEW) {
            return new PostViewHolder(inflater.inflate(R.layout.post_type_text, parent, false), createOnClickListener());
        } else if (viewType == IMAGE_VIEW) {
            return new PostViewHolder(inflater.inflate(R.layout.post_type_image, parent, false), createOnClickListener());
        } else if (viewType == TEXT_IMAGE_VIEW) {
            return new PostViewHolder(inflater.inflate(R.layout.post_type_text_image, parent, false), createOnClickListener());
        } else {
            return new LoadViewHolder(inflater.inflate(R.layout.loading_view, parent, false));
        }
    }

    private PostViewHolder.OnClickListener createOnClickListener() {
        return new PostViewHolder.OnClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                if (callBack != null) {
                    selectedPostPosition = position;
                    callBack.onItemClick(getItemByPosition(position), view);
                }
            }

            @Override
            public void onLikeClick(LikeController likeController, int position) {
                Post post = getItemByPosition(position);
                likeController.handleLikeClickAction(context, post);
            }

            @Override
            public void onAuthorClick(int position, View view) {
                if (callBack != null) {
                    callBack.onAuthorClick(getItemByPosition(position).getAuthorId(), view);
                }
            }

            @Override
            public void onShareClick(int position, View view) {

            }

            @Override
            public void onLinkClick(String linkUrl) {
                if (callBack != null) {
                    callBack.onLinkClick(linkUrl);
                }
            }

            @Override
            public void openYoutubeLink(String link) {
                if (!link.isEmpty()) {
                    Utils.openYoutubeLink(link, context);
                }
            }

        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //((PostViewHolder) holder).bindData(messageList.get(position));
        Log.d(TAG, "index: " + position + " id: " + getItemByPosition(position).getId());

        switch (holder.getItemViewType()) {
            case TEXT_VIEW:
                ((PostViewHolder) holder).bindTextPost(getItemByPosition(position));
                break;
            case TEXT_COLORED_VIEW:
                ((PostViewHolder) holder).bindColoredPost(getItemByPosition(position));
                break;
            case LINK_VIEW:
                ((PostViewHolder) holder).bindLink(getItemByPosition(position));
                break;
            case IMAGE_VIEW:
                ((PostViewHolder) holder).bindImagePost(getItemByPosition(position));
                break;
            case TEXT_IMAGE_VIEW:
                ((PostViewHolder) holder).bindTextImagePost(getItemByPosition(position));
                break;
        }
    }

    private void setList(List<Post> list) {
        postList.clear();
        postList.addAll(list);
        notifyDataSetChanged();
    }

    public void loadPosts() {
        if (!Utils.hasInternetConnection(context)) {
            Toast.makeText(context, R.string.internet_connection_failed, Toast.LENGTH_LONG).show();
            callBack.onPostLoadingCanceled();
            return;
        }

        OnDataChangedListener<Post> onPostsDataChangedListener = new OnDataChangedListener<Post>() {
            @Override
            public void onListChanged(List<Post> list) {
                setList(list);
                callBack.onPostsListChanged(list.size());
            }

            @Override
            public void inEmpty(Boolean empty, String error) {
                if (empty) {
                    callBack.onPostsListChanged(0);
                }
            }
        };

        PostManager.getInstance(context).getPostsListByUser(onPostsDataChangedListener, userId);
    }

    public void removeSelectedPost() {
        postList.remove(selectedPostPosition);
        callBack.onPostsListChanged(postList.size());
        notifyItemRemoved(selectedPostPosition);
    }

    public interface CallBack {
        void onItemClick(Post post, View view);

        void onPostsListChanged(int postsCount);

        void onAuthorClick(String authorId, View view);

        void onLinkClick(String linkUrl);

        void onPostLoadingCanceled();
    }
}
