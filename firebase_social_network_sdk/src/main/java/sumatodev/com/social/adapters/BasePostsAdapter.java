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

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.LinkedList;
import java.util.List;

import sumatodev.com.social.managers.PostManager;
import sumatodev.com.social.managers.UsersManager;
import sumatodev.com.social.managers.listeners.OnObjectExistListener;
import sumatodev.com.social.managers.listeners.OnPostChangedListener;
import sumatodev.com.social.model.Post;
import sumatodev.com.social.utils.LogUtil;

public abstract class BasePostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = BasePostsAdapter.class.getSimpleName();

    public static final int IMAGE_VIEW = 1;
    public static final int TEXT_VIEW = 2;
    public static final int TEXT_IMAGE_VIEW = 3;
    public static final int TEXT_COLORED_VIEW = 4;
    public static final int LINK_VIEW = 5;

    protected List<Post> postList = new LinkedList<>();
    protected FragmentActivity context;
    protected boolean isLoading = false;
    protected int selectedPostPosition = -1;
    protected String currentUid;

    public BasePostsAdapter(FragmentActivity context) {
        this.context = context;
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUid = firebaseUser.getUid();
        }

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
        } else if (postType != null && postType.equals("link")) {
            return LINK_VIEW;
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
            PostManager.getInstance(context).getSinglePostValue(selectedPost.getId(), createOnPostChangeListener(selectedPostPosition));
        }
    }


    void isPostValid(List<Post> list) {
        for (final Post post : list) {
            if (post.getAuthorId() != null && currentUid != null) {
                UsersManager.getInstance(context).isUserFollowing(post.getAuthorId(), currentUid, new OnObjectExistListener() {
                    @Override
                    public void onDataChanged(boolean exist) {
                        if (exist) {
                            postList.add(post);
                            notifyItemInserted(postList.size());
                            isLoading = false;
                        }
                    }
                });
            }
        }
    }
}
