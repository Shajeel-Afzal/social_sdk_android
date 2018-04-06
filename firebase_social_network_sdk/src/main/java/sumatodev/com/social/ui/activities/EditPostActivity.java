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

package sumatodev.com.social.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import sumatodev.com.social.R;
import sumatodev.com.social.enums.PostStatus;
import sumatodev.com.social.managers.PostManager;
import sumatodev.com.social.managers.listeners.OnObjectChangedListener;
import sumatodev.com.social.managers.listeners.OnPostChangedListener;
import sumatodev.com.social.model.Post;
import sumatodev.com.social.model.PostStyle;

public class EditPostActivity extends CreatePostActivity {
    private static final String TAG = EditPostActivity.class.getSimpleName();
    public static final String POST_EXTRA_KEY = "EditPostActivity.POST_EXTRA_KEY";
    public static final int EDIT_POST_REQUEST = 33;

    private Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        post = (Post) getIntent().getSerializableExtra(POST_EXTRA_KEY);
        showProgress();
        updatePostLayout();
        fillUIFields();
    }

    @Override
    protected void onStart() {
        super.onStart();
        addCheckIsPostChangedListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        postManager.closeListeners(this);
    }

    @Override
    public void onPostSaved(boolean success) {
        hideProgress();
        creatingPost = false;

        if (success) {
            setResult(RESULT_OK);
            finish();
        } else {
            showSnackBar(R.string.error_fail_update_post);
        }
    }

    @Override
    protected void savePost(final String title) {
        doSavePost(title);
    }

    private void addCheckIsPostChangedListener() {
        PostManager.getInstance(this).getPost(this, post.getId(), new OnPostChangedListener() {
            @Override
            public void onObjectChanged(Post obj) {
                if (obj == null) {
                    showWarningDialog(getResources().getString(R.string.error_post_was_removed));
                } else {
                    checkIsPostCountersChanged(obj);
                }
            }

            @Override
            public void onError(String errorText) {
                showWarningDialog(errorText);
            }

            private void showWarningDialog(String message) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditPostActivity.this);
                builder.setMessage(message);
                builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openMainActivity();
                        finish();
                    }
                });
                builder.setCancelable(false);
                builder.show();
            }
        });
    }

    private void checkIsPostCountersChanged(Post updatedPost) {
        if (post.getLikesCount() != updatedPost.getLikesCount()) {
            post.setLikesCount(updatedPost.getLikesCount());
        }

        if (post.getCommentsCount() != updatedPost.getCommentsCount()) {
            post.setCommentsCount(updatedPost.getCommentsCount());
        }

        if (post.getWatchersCount() != updatedPost.getWatchersCount()) {
            post.setWatchersCount(updatedPost.getWatchersCount());
        }

        if (post.isHasComplain() != updatedPost.isHasComplain()) {
            post.setHasComplain(updatedPost.isHasComplain());
        }
    }

    private void openMainActivity() {
        Intent intent = new Intent(EditPostActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void doSavePost(String title) {
        showProgress(R.string.message_saving);
        post.setTitle(title);

        String hex = Integer.toHexString(selectedColor);
        if (hex.equals(Integer.toHexString(Color.WHITE))) {
            post.setPostStyle(new PostStyle(0));
        } else {
            post.setPostStyle(new PostStyle(selectedColor));
        }

        if (imageUri != null) {
            post.setImagePath(String.valueOf(imageUri));
            postManager.createOrUpdatePostWithImage(this, EditPostActivity.this, post);
        } else {
            postManager.createOrUpdatePost(post);
            onPostSaved(true);
        }
    }

    private void updatePostLayout() {
        if (post != null) {
            postManager.isCurrentPostColored(post.getId(), new OnObjectChangedListener<PostStyle>() {
                @Override
                public void onObjectChanged(PostStyle obj) {
                    if (obj != null) {

                        if (obj.bg_color == 0) {
                            //default view
                        } else {
                            final float scale = getResources().getDisplayMetrics().density;
                            int pixels = (int) (180 * scale + 0.5f);

                            textLayout.setBackgroundColor(obj.bg_color);
                            titleEditText.setHeight(pixels);
                            titleEditText.setTextColor(Color.WHITE);
                            titleEditText.setTextSize(24);
                            titleEditText.setGravity(Gravity.CENTER);
                            titleEditText.setMaxLines(3);
                            titleEditText.setTypeface(Typeface.DEFAULT_BOLD);

                            colorPicker.setSelectedColor(obj.bg_color);
                        }
                    }
                }
            });
        }
    }

    private void fillUIFields() {
        titleEditText.setText(post.getTitle());

        if (post.getImagePath() != null) {
            loadPostDetailsImage();
        }
        hideProgress();
    }

    private void loadPostDetailsImage() {
        imageLayout.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(post.getImagePath())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .crossFade()
                .centerCrop()
                .error(R.drawable.ic_stub)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int i = item.getItemId();
        if (i == R.id.save) {
            if (!creatingPost) {
                if (hasInternetConnection()) {
                    attemptCreatePost();
                } else {
                    showSnackBar(R.string.internet_connection_failed);
                }
            }

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
