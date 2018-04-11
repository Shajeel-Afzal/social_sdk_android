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
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.codewaves.youtubethumbnailview.ThumbnailLoader;
import com.codewaves.youtubethumbnailview.ThumbnailLoadingListener;
import com.codewaves.youtubethumbnailview.downloader.OembedVideoInfoDownloader;
import com.klinker.android.link_builder.Link;
import com.klinker.android.link_builder.LinkBuilder;
import com.klinker.android.link_builder.TouchableMovementMethod;

import sumatodev.com.social.R;
import sumatodev.com.social.managers.PostManager;
import sumatodev.com.social.managers.listeners.OnObjectChangedListener;
import sumatodev.com.social.managers.listeners.OnPostChangedListener;
import sumatodev.com.social.model.Post;
import sumatodev.com.social.model.PostStyle;
import sumatodev.com.social.utils.Regex;

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


        thumbnailLink.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 0) {
                    updateVideoThumbnail(s.toString());
                }
            }
        });
    }

    private void updateVideoThumbnail(final String string) {

        ThumbnailLoader.initialize().setVideoInfoDownloader(new OembedVideoInfoDownloader());
        thumbnail.loadThumbnail(string, new ThumbnailLoadingListener() {
            @Override
            public void onLoadingStarted(@NonNull String url, @NonNull View view) {

            }

            @Override
            public void onLoadingComplete(@NonNull String url, @NonNull View view) {

            }

            @Override
            public void onLoadingCanceled(@NonNull String url, @NonNull View view) {
            }

            @Override
            public void onLoadingFailed(@NonNull String url, @NonNull View view, Throwable error) {
                Toast.makeText(EditPostActivity.this, "url not valid", Toast.LENGTH_SHORT).show();
            }
        });

        thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrlActivity(string);
            }
        });
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
    protected void savePost(final String title, String link) {
        doSavePost(title, link);
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

    private void doSavePost(String title, String link) {
        showProgress(R.string.message_saving);

        if (!title.isEmpty()) {
            post.setTitle(title);
        }
        if (!link.isEmpty()) {
            post.setLink(link);
        }

        String hex = Integer.toHexString(selectedColor);
        if (hex.equals(Integer.toHexString(Color.WHITE))) {
            post.setPostStyle(new PostStyle(0));
        } else {
            post.setPostStyle(new PostStyle(selectedColor));
        }

        if (imageUri != null) {
            post.setPostStyle(new PostStyle(0));
            post.setImagePath(String.valueOf(imageUri));
            postManager.createOrUpdatePostWithImage(this, post, EditPostActivity.this);
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

        if (post.getTitle() != null) {
            titleEditText.setText(post.getTitle());
        }

        if (post.getLink() != null) {
            fillLinkData();
        }

        if (post.getImagePath() != null) {
            loadPostDetailsImage();
        }
        hideProgress();
    }

    private void fillLinkData() {
        if (post.getLink() != null) {

            colorPicker.setVisibility(View.GONE);
            textLayout.setMinimumHeight(0);

            imageButton.setVisibility(View.GONE);
            thumbnailView.setVisibility(View.VISIBLE);

            thumbnailLink.setText(post.getLink());

            Link link = new Link(Regex.WEB_URL_PATTERN);

            LinkBuilder.on(thumbnailLink).addLink(link).build();
            thumbnailLink.setMovementMethod(TouchableMovementMethod.getInstance());

            ThumbnailLoader.initialize().setVideoInfoDownloader(new OembedVideoInfoDownloader());
            thumbnail.loadThumbnail(post.getLink());
            thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openUrlActivity(post.getLink());
                }
            });
        }
    }

    private void openUrlActivity(String linkUrl) {
        Intent intent = new Intent(EditPostActivity.this, LinkActivity.class);
        intent.putExtra(LinkActivity.URL_REF, linkUrl);
        startActivity(intent);
    }

    private void loadPostDetailsImage() {
        colorPicker.setVisibility(View.GONE);
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
