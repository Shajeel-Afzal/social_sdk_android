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

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import sumatodev.com.social.R;
import sumatodev.com.social.adapters.holders.LoadViewHolder;
import sumatodev.com.social.adapters.holders.PostHolders;
import sumatodev.com.social.controllers.LikeController;
import sumatodev.com.social.enums.ItemType;
import sumatodev.com.social.managers.PostManager;
import sumatodev.com.social.managers.listeners.OnPostListChangedListener;
import sumatodev.com.social.model.Post;
import sumatodev.com.social.model.PostListResult;
import sumatodev.com.social.ui.activities.MainActivity;
import sumatodev.com.social.utils.PreferencesUtil;

/**
 * Created by Kristina on 10/31/16.
 */

public class PostsAdapter extends BasePostsAdapter {
    public static final String TAG = PostsAdapter.class.getSimpleName();


    private Callback callback;
    private boolean isLoading = false;
    private boolean isMoreDataAvailable = true;
    private long lastLoadedItemCreatedDate;
    private SwipeRefreshLayout swipeContainer;
    private MainActivity mainActivity;


    public PostsAdapter(final MainActivity activity, SwipeRefreshLayout swipeContainer) {
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
        if (activity.hasInternetConnection()) {
            loadFirstPage();
            cleanSelectedPostInformation();
        } else {
            swipeContainer.setRefreshing(false);
            mainActivity.showFloatButtonRelatedSnackBar(R.string.internet_connection_failed);
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TEXT_VIEW || viewType == TEXT_COLORED_VIEW) {
            return new PostHolders.TextHolder(inflater.inflate(R.layout.post_type_text, parent, false), callback);
        } else if (viewType == IMAGE_VIEW) {
            return new PostHolders.ImageHolder(inflater.inflate(R.layout.post_type_image, parent, false), callback);
        } else if (viewType == TEXT_IMAGE_VIEW) {
            return new PostHolders.TextImageHolder(inflater.inflate(R.layout.post_type_text_image, parent, false), callback);
        } else {
            return new LoadViewHolder(inflater.inflate(R.layout.loading_view, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading) {
            android.os.Handler mHandler = activity.getWindow().getDecorView().getHandler();
            mHandler.post(new Runnable() {
                public void run() {
                    //change adapter contents
                    if (activity.hasInternetConnection()) {
                        isLoading = true;
                        postList.add(new Post(ItemType.LOAD));
                        notifyItemInserted(postList.size());
                        loadNext(lastLoadedItemCreatedDate - 1);
                    } else {
                        mainActivity.showFloatButtonRelatedSnackBar(R.string.internet_connection_failed);
                    }
                }
            });


        }

        Log.d(TAG, "index: " + position + " id: " + postList.get(position).getId());

        switch (holder.getItemViewType()) {
            case TEXT_VIEW:
                ((PostHolders.TextHolder) holder).bindData(getItemByPosition(position));
                break;
            case TEXT_COLORED_VIEW:
                ((PostHolders.TextHolder) holder).bindColoredPostData(getItemByPosition(position));
                break;
            case IMAGE_VIEW:
                ((PostHolders.ImageHolder) holder).bindData(getItemByPosition(position));

                break;
            case TEXT_IMAGE_VIEW:
                ((PostHolders.TextImageHolder) holder).bindData(getItemByPosition(position));

                break;
        }

//        if (getItemViewType(position) != ItemType.LOAD.getTypeCode()) {
//            ((PostViewHolder) holder).bindData(postList.get(position));
//
//            Log.d(TAG, "index: " + position + " id: " + postList.get(position).getId());
//        }
    }

    private void addList(List<Post> list) {
        this.postList.addAll(list);
        notifyDataSetChanged();
        isLoading = false;
    }

    public void loadFirstPage() {
        loadNext(0);
        PostManager.getInstance(mainActivity.getApplicationContext()).clearNewPostsCounter();
    }

    private void loadNext(final long nextItemCreatedDate) {

        if (!PreferencesUtil.isPostWasLoadedAtLeastOnce(mainActivity) && !activity.hasInternetConnection()) {
            mainActivity.showFloatButtonRelatedSnackBar(R.string.internet_connection_failed);
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

        PostManager.getInstance(activity).getPostsList(onPostsDataChangedListener, nextItemCreatedDate);
    }

    private void hideProgress() {
        if (!postList.isEmpty() && getItemViewType(postList.size() - 1) == ItemType.LOAD.getTypeCode()) {
            postList.remove(postList.size() - 1);
            notifyItemRemoved(postList.size() - 1);
        }
    }

    public void removeSelectedPost(int postSelectedPosition) {
        postList.remove(postSelectedPosition);
        notifyItemRemoved(postSelectedPosition);
    }

    @Override
    public long getItemId(int position) {
        return getItemByPosition(position).getId().hashCode();
    }

    public interface Callback {
        void onItemClick(int position, View view);

        void onImageClick(int position, View view);

        void onListLoadingFinished();

        void onAuthorClick(int position, View view);

        void onCanceled(String message);

        void onLikeClick(LikeController likeController, int position);
    }
}
