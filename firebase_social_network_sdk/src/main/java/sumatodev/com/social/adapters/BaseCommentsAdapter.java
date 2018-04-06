package sumatodev.com.social.adapters;

import android.support.v7.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

import sumatodev.com.social.managers.CommentManager;
import sumatodev.com.social.managers.listeners.OnCommentChangedListener;
import sumatodev.com.social.model.Comment;
import sumatodev.com.social.ui.activities.BaseActivity;
import sumatodev.com.social.utils.LogUtil;

/**
 * Created by Ali on 06/04/2018.
 */

public abstract class BaseCommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = BaseCommentsAdapter.class.getSimpleName();

    protected List<Comment> commentList = new LinkedList<>();
    protected BaseActivity activity;
    protected int selectedCommentPosition = -1;

    public BaseCommentsAdapter(BaseActivity activity) {
        this.activity = activity;
    }

    protected void cleanSelectedPostInformation() {
        selectedCommentPosition = -1;
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return commentList.get(position).getItemType().getTypeCode();
    }

    public Comment getItemByPosition(int position) {
        return commentList.get(position);
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
                commentList.set(position, obj);
                notifyItemChanged(position);
            }

            @Override
            public void onError(String errorText) {
                LogUtil.logDebug(TAG, errorText);
            }
        };
    }
}
