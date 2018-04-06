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

import java.util.LinkedList;
import java.util.List;

import sumatodev.com.social.managers.PostManager;
import sumatodev.com.social.managers.listeners.OnPostChangedListener;
import sumatodev.com.social.model.Post;
import sumatodev.com.social.ui.activities.BaseActivity;
import sumatodev.com.social.utils.LogUtil;

public abstract class BasePostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = BasePostsAdapter.class.getSimpleName();

    public static final int IMAGE_VIEW = 1;
    public static final int TEXT_VIEW = 2;
    public static final int TEXT_IMAGE_VIEW = 3;
    public static final int TEXT_COLORED_VIEW = 4;

    protected List<Post> postList = new LinkedList<>();
    protected BaseActivity activity;
    protected int selectedPostPosition = -1;

    public BasePostsAdapter(BaseActivity activity) {
        this.activity = activity;
    }

    protected void cleanSelectedPostInformation() {
        selectedPostPosition = -1;
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    @Override
    public int getItemViewType(int position) {
        String postType = postList.get(position).getPostType();
        int bg_color = postList.get(position).getPostStyle() != null ? postList.get(position).getPostStyle().bg_color : 0;
        if (postType != null && postType.equals("text")) {
            if (bg_color == 0) {
                return TEXT_VIEW;
            } else {
                return TEXT_COLORED_VIEW;
            }
        } else if (postType != null && postType.equals("image")) {
            return IMAGE_VIEW;
        } else if (postType != null && postType.equals("text_image")) {
            return TEXT_IMAGE_VIEW;
        }
        return postList.get(position).getItemType().getTypeCode();

    }

    public Post getItemByPosition(int position) {
        return postList.get(position);
    }

    private OnPostChangedListener createOnPostChangeListener(final int postPosition) {
        return new OnPostChangedListener() {
            @Override
            public void onObjectChanged(Post obj) {
                postList.set(postPosition, obj);
                notifyItemChanged(postPosition);
            }

            @Override
            public void onError(String errorText) {
                LogUtil.logDebug(TAG, errorText);
            }
        };
    }

    public void updateSelectedPost() {
        if (selectedPostPosition != -1) {
            Post selectedPost = getItemByPosition(selectedPostPosition);
            PostManager.getInstance(activity).getSinglePostValue(selectedPost.getId(), createOnPostChangeListener(selectedPostPosition));
        }
    }
}
