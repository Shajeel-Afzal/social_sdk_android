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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import sumatodev.com.social.R;
import sumatodev.com.social.managers.PostManager;
import sumatodev.com.social.managers.listeners.OnPostCreatedListener;
import sumatodev.com.social.model.Post;
import sumatodev.com.social.utils.LogUtil;
import sumatodev.com.social.utils.NotificationView;
import sumatodev.com.social.utils.ValidationUtil;

public class CreatePostActivity extends PickImageActivity implements OnPostCreatedListener {
    private static final String TAG = CreatePostActivity.class.getSimpleName();
    public static final int CREATE_NEW_POST_REQUEST = 11;
    public static final String POST_DATA_KEY = "CreatePostActivity.POST_DATA_KEY";

    protected ImageView imageView;
    protected ProgressBar progressBar;
    protected EditText titleEditText;

    protected PostManager postManager;
    protected boolean creatingPost = false;
    private NotificationView notificationView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_post_activity);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        postManager = PostManager.getInstance(CreatePostActivity.this);

        titleEditText = findViewById(R.id.titleEditText);
        progressBar = findViewById(R.id.progressBar);

        imageView = findViewById(R.id.imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectImageClick(v);
            }
        });

        titleEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (titleEditText.hasFocus() && titleEditText.getError() != null) {
                    titleEditText.setError(null);
                    return true;
                }
                return false;
            }
        });

        notificationView = new NotificationView(this);
    }

    @Override
    public ProgressBar getProgressView() {
        return progressBar;
    }

    @Override
    public ImageView getImageView() {
        return imageView;
    }

    @Override
    public void onImagePikedAction() {
        loadImageToImageView();
    }

    protected void attemptCreatePost() {
        // Reset errors.
        titleEditText.setError(null);
        if (!validate()) {
            hideKeyboard();
            Toast.makeText(this, "post can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = titleEditText.getText().toString().trim();

        View focusView = null;
        boolean cancel = false;

        if (!ValidationUtil.isPostTitleValid(title)) {
            titleEditText.setError(getString(R.string.error_post_title_length));
            focusView = titleEditText;
            cancel = true;
        }


        if (!cancel) {
            creatingPost = true;
            hideKeyboard();
            savePost(title);
        } else if (focusView != null) {
            focusView.requestFocus();
        }
    }

    protected void savePost(String title) {
        showProgress(R.string.message_creating_post);
        Post post = new Post();
        if (!title.isEmpty()) {
            post.setTitle(title);
        }
        if (imageUri != null) {
            post.setImagePath(String.valueOf(imageUri));
        }
        post.setAuthorId(FirebaseAuth.getInstance().getCurrentUser().getUid());

        //postManager.createOrUpdatePostWithImage(imageUri, CreatePostActivity.this, post);

        Intent intent = new Intent();
        intent.putExtra(POST_DATA_KEY, post);
        setResult(RESULT_OK, intent);
        CreatePostActivity.this.finish();
        hideProgress();
    }

    @Override
    public void onPostSaved(boolean success) {
        hideProgress();

        if (success) {
            setResult(RESULT_OK);
            CreatePostActivity.this.finish();
            LogUtil.logDebug(TAG, "Post was created");
        } else {
            creatingPost = false;
            showSnackBar(R.string.error_fail_create_post);
            LogUtil.logDebug(TAG, "Failed to create a post");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_post_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int i = item.getItemId();
        if (i == R.id.post) {
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

    private boolean validate() {
        boolean valid = true;
        String title = titleEditText.getText().toString().trim();

        if (title.isEmpty() && imageUri == null) {
            valid = false;
        } else if (!title.isEmpty() || imageUri != null) {
            valid = true;
        }

        return valid;
    }
}
