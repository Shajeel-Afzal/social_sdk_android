/*
 *  Copyright 2017 Rozdoum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package sumatodev.com.social.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
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
import sumatodev.com.social.enums.ItemType;
import sumatodev.com.social.managers.PostManager;
import sumatodev.com.social.managers.listeners.OnPostListChangedListener;
import sumatodev.com.social.model.Post;
import sumatodev.com.social.model.PostListResult;
import sumatodev.com.social.utils.PreferencesUtil;
import sumatodev.com.social.utils.Utils;

/**
 * Created by Kristina on 10/31/16.
 */

public class PostsAdapter extends BasePostsAdapter {
    public static final String TAG = PostsAdapter.class.getSimpleName();

    private Callback callback;
    private boolean isMoreDataAvailable = true;
    private long lastLoadedItemCreatedDate;
    private SwipeRefreshLayout swipeContainer;
    private Context mainActivity;

    public PostsAdapter(final FragmentActivity activity, SwipeRefreshLayout swipeContainer) {
        super(activity);
        this.mainActivity = activity;
        this.swipeContainer = swipeContainer;
        initRefreshLayout();
        setHasStableIds(true);
    }

    private void initRefreshLayout() {
        if (swipeContainer != null) {
            this.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    onRefreshAction();
                }
            });
        }
    }

    private void onRefreshAction() {
        if (Utils.hasInternetConnection(context)) {
            loadFirstPage();
            cleanSelectedPostInformation();
        } else {
            swipeContainer.setRefreshing(false);
            Toast.makeText(mainActivity, R.string.internet_connection_failed, Toast.LENGTH_SHORT).show();
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
                if (callback != null) {
                    selectedPostPosition = position;
                    callback.onItemClick(getItemByPosition(position), view);
                }
            }

            @Override
            public void onLikeClick(LikeController likeController, int position) {
                Post post = getItemByPosition(position);
                likeController.handleLikeClickAction(context, post);
            }

            @Override
            public void onAuthorClick(int position, View view) {
                if (callback != null) {
                    callback.onAuthorClick(getItemByPosition(position).getAuthorId(), view);
                }
            }

            @Override
            public void onShareClick(int position, View view) {

            }

            @Override
            public void onLinkClick(String linkUrl) {
                if (callback != null) {
                    callback.onLinkClick(linkUrl);
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
        if (position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading) {
            android.os.Handler mHandler = context.getWindow().getDecorView().getHandler();
            mHandler.post(new Runnable() {
                public void run() {
                    //change adapter contents
                    if (Utils.hasInternetConnection(context)) {
                        isLoading = true;
                        postList.add(new Post(ItemType.LOAD));
                        notifyItemInserted(postList.size());
                        loadNext(lastLoadedItemCreatedDate - 1);
                    } else {
                        Toast.makeText(mainActivity, R.string.internet_connection_failed, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
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

    private void addList(List<Post> list) {
        this.postList.addAll(list);
        callback.onPostsListChanged(list.size());
        notifyDataSetChanged();
        isLoading = false;
    }

    public void loadFirstPage() {
        loadNext(0);
        PostManager.getInstance(mainActivity.getApplicationContext()).clearNewPostsCounter();
    }

    private void loadNext(final long nextItemCreatedDate) {

        if (!PreferencesUtil.isPostWasLoadedAtLeastOnce(mainActivity) && !Utils.hasInternetConnection(context)) {
            Toast.makeText(mainActivity, R.string.internet_connection_failed, Toast.LENGTH_LONG).show();
            hideProgress();
            callback.onListLoadingFinished();
            return;
        }

        OnPostListChangedListener<Post> onPostsDataChangedListener = new OnPostListChangedListener<Post>() {
            @Override
            public void onListChanged(PostListResult result) {
                lastLoadedItemCreatedDate = result.getLastItemCreatedDate();
                isMoreDataAvailable = result.isMoreDataAvailable();
                List<Post> list = result.getPosts();

                if (nextItemCreatedDate == 0) {
                    postList.clear();
                    notifyDataSetChanged();
                    swipeContainer.setRefreshing(false);
                }

                hideProgress();

                if (!list.isEmpty()) {
                    addList(list);

                    if (!PreferencesUtil.isPostWasLoadedAtLeastOnce(mainActivity)) {
                        PreferencesUtil.setPostWasLoadedAtLeastOnce(mainActivity, true);
                    }
                } else {
                    isLoading = false;
                }

                callback.onListLoadingFinished();
            }

            @Override
            public void onCanceled(String message) {
                callback.onCanceled(message);
            }
        };

        PostManager.getInstance(context).getPostsList(onPostsDataChangedListener, nextItemCreatedDate);

    }

    private void hideProgress() {
        if (!postList.isEmpty() && getItemViewType(postList.size() - 1) == ItemType.LOAD.getTypeCode()) {
            postList.remove(postList.size() - 1);
            notifyItemRemoved(postList.size() - 1);
        }
    }

    public void removeSelectedPost() {
        postList.remove(selectedPostPosition);
        callback.onPostsListChanged(postList.size());
        notifyItemRemoved(selectedPostPosition);
    }

    @Override
    public long getItemId(int position) {
        return getItemByPosition(position).getId().hashCode();
    }

    public interface Callback {
        void onItemClick(Post post, View view);

        void onListLoadingFinished();

        void onAuthorClick(String authorId, View view);

        void onCanceled(String message);

        void onLinkClick(String linkUrl);

        void onPostsListChanged(int postsCount);
    }
}
