package sumatodev.com.social.adapters.holders;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import sumatodev.com.social.R;
import sumatodev.com.social.adapters.BaseCommentsAdapter;
import sumatodev.com.social.enums.ItemType;
import sumatodev.com.social.managers.CommentManager;
import sumatodev.com.social.managers.listeners.OnCommentListChangedListener;
import sumatodev.com.social.model.Comment;
import sumatodev.com.social.model.CommentListResult;
import sumatodev.com.social.ui.activities.PostDetailsActivity;
import sumatodev.com.social.utils.PreferencesUtil;

/**
 * Created by Ali on 06/04/2018.
 */

public class CommentAdapter extends BaseCommentsAdapter {

    public static final String TAG = CommentAdapter.class.getSimpleName();

    private Callback callback;
    private boolean isLoading = false;
    private boolean isMoreDataAvailable = true;
    private long lastLoadedItemCreatedDate;
    private SwipeRefreshLayout swipeContainer;
    private PostDetailsActivity mainActivity;
    private String postId;

    public CommentAdapter(PostDetailsActivity activity, String postId) {
        super(activity);
        this.mainActivity = activity;
        this.postId = postId;
        initRefreshLayout();
        setHasStableIds(true);

        Log.d(TAG,"Comment Adapter Called: ");
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
        if (viewType == ItemType.ITEM.getTypeCode()) {
            return new CommentViewHolder(inflater.inflate(R.layout.comment_list_item, parent, false), null);
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
                        commentList.add(new Comment(ItemType.LOAD));
                        notifyItemInserted(commentList.size());
                        loadNext(lastLoadedItemCreatedDate - 1);
                    } else {
                        mainActivity.showFloatButtonRelatedSnackBar(R.string.internet_connection_failed);
                    }
                }
            });
        }

        Log.d(TAG, "Comments : " + getItemByPosition(position).getId());
        if (getItemViewType(position) != ItemType.LOAD.getTypeCode()) {
            ((CommentViewHolder) holder).bindData(commentList.get(position));
        }
    }

    private void addList(List<Comment> list) {
        this.commentList.addAll(list);
        notifyDataSetChanged();
        isLoading = false;
    }

    public void loadFirstPage() {
        loadNext(0);
    }

    private void loadNext(final long nextItemCreatedDate) {

        if (!PreferencesUtil.isPostWasLoadedAtLeastOnce(mainActivity) && !activity.hasInternetConnection()) {
            mainActivity.showFloatButtonRelatedSnackBar(R.string.internet_connection_failed);
            hideProgress();
            callback.onListLoadingFinished();
            return;
        }

        OnCommentListChangedListener<Comment> commentListChangedListener = new OnCommentListChangedListener<Comment>() {
            @Override
            public void onListChanged(CommentListResult result) {
                lastLoadedItemCreatedDate = result.getLastItemCreatedDate();
                isMoreDataAvailable = result.isMoreDataAvailable();
                List<Comment> list = result.getComments();

                if (nextItemCreatedDate == 0) {
                    commentList.clear();
                    notifyDataSetChanged();
                    //swipeContainer.setRefreshing(false);
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

        CommentManager.getInstance(activity).getComments(postId, commentListChangedListener, nextItemCreatedDate);
    }

    private void hideProgress() {
        if (!commentList.isEmpty() && getItemViewType(commentList.size() - 1) == ItemType.LOAD.getTypeCode()) {
            commentList.remove(commentList.size() - 1);
            notifyItemRemoved(commentList.size() - 1);
        }
    }

    public void removeSelectedComment() {
        commentList.remove(selectedCommentPosition);
        notifyItemRemoved(selectedCommentPosition);
    }

    @Override
    public long getItemId(int position) {
        return getItemByPosition(position).getId().hashCode();
    }

    public interface Callback {
        void onItemClick(Comment comment, View view);

        void onListLoadingFinished();

        void onAuthorClick(String authorId, View view);

        void onCanceled(String message);
    }
}
