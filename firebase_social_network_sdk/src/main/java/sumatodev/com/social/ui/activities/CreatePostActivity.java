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
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import sumatodev.com.social.R;
import sumatodev.com.social.managers.PostManager;
import sumatodev.com.social.managers.listeners.OnPostCreatedListener;
import sumatodev.com.social.model.Post;
import sumatodev.com.social.utils.LogUtil;
import sumatodev.com.social.utils.ValidationUtil;

public class CreatePostActivity extends PickImageActivity implements OnPostCreatedListener, View.OnClickListener {
    private static final String TAG = CreatePostActivity.class.getSimpleName();
    public static final int CREATE_NEW_POST_REQUEST = 11;
    public static final String POST_DATA_KEY = "CreatePostActivity.POST_DATA_KEY";
    public static final String CREATE_POST_INTENT_KEY = "CreatePostActivity.ShareIntentKey";

    protected FrameLayout imageLayout;
    protected ImageButton imageButton;
    protected ImageView imageView;
    protected ProgressBar progressBar;
    protected EditText titleEditText;
    protected Button submitBtn;

    protected PostManager postManager;
    protected boolean creatingPost = false;
    private Intent shareIntent;

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
        imageLayout = findViewById(R.id.imageLayout);
        imageButton = findViewById(R.id.imageButton);
        submitBtn = findViewById(R.id.submitBtn);

        imageView = findViewById(R.id.imageView);

        imageButton.setOnClickListener(new View.OnClickListener() {
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

        submitBtn.setOnClickListener(this);

        initShareIntent();
    }

    private void initShareIntent() {
        // Get intent, action and MIME type
        shareIntent = getIntent();
        String action = shareIntent.getAction();
        String type = shareIntent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                // handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(shareIntent); // Handle single image being sent
            }
        }
    }

    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Update UI to reflect text being shared
        }
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            this.imageUri = imageUri;
            imageLayout.setVisibility(View.VISIBLE);
            loadImageToImageView();
        } else {
            imageLayout.setVisibility(View.GONE);
        }
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
        if (imageUri != null) {
            imageLayout.setVisibility(View.VISIBLE);
        } else {
            imageLayout.setVisibility(View.GONE);
        }
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

        if (Intent.ACTION_SEND.equals(shareIntent.getAction())) {

            postManager.createOrUpdatePostWithImage(this, new OnPostCreatedListener() {
                @Override
                public void onPostSaved(boolean success) {
                    Log.d(TAG,"post send successfully");
                }
            }, post);
            handleSharePost();

        } else {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent.putExtra(POST_DATA_KEY, post));
            CreatePostActivity.this.finish();
            hideProgress();
        }
    }

    private void handleSharePost() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
    public void onClick(View v) {
        if (v == submitBtn) {
            if (!creatingPost) {
                if (hasInternetConnection()) {
                    attemptCreatePost();
                } else {
                    showSnackBar(R.string.internet_connection_failed);
                }
            }
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
