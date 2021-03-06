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

package sumatodev.com.social.ui.activities;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Transition;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.codewaves.youtubethumbnailview.ThumbnailLoader;
import com.codewaves.youtubethumbnailview.ThumbnailView;
import com.codewaves.youtubethumbnailview.downloader.OembedVideoInfoDownloader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.klinker.android.link_builder.Link;
import com.klinker.android.link_builder.LinkBuilder;
import com.klinker.android.link_builder.TouchableMovementMethod;
import com.percolate.caffeine.ViewUtils;

import java.util.HashMap;
import java.util.List;

import sumatodev.com.social.R;
import sumatodev.com.social.adapters.CommentsAdapter;
import sumatodev.com.social.adapters.NamesAdapter;
import sumatodev.com.social.adapters.SearchAdapter;
import sumatodev.com.social.adapters.UsersAdapter;
import sumatodev.com.social.controllers.LikeController;
import sumatodev.com.social.dialogs.EditCommentDialog;
import sumatodev.com.social.enums.PostStatus;
import sumatodev.com.social.enums.ProfileStatus;
import sumatodev.com.social.listeners.CustomTransitionListener;
import sumatodev.com.social.listeners.RecyclerItemClickListener;
import sumatodev.com.social.managers.CommentManager;
import sumatodev.com.social.managers.PostManager;
import sumatodev.com.social.managers.ProfileManager;
import sumatodev.com.social.managers.UsersManager;
import sumatodev.com.social.managers.listeners.OnDataChangedListener;
import sumatodev.com.social.managers.listeners.OnObjectChangedListener;
import sumatodev.com.social.managers.listeners.OnObjectExistListener;
import sumatodev.com.social.managers.listeners.OnPostChangedListener;
import sumatodev.com.social.managers.listeners.OnTaskCompleteListener;
import sumatodev.com.social.model.Comment;
import sumatodev.com.social.model.Like;
import sumatodev.com.social.model.Mention;
import sumatodev.com.social.model.Post;
import sumatodev.com.social.model.Profile;
import sumatodev.com.social.model.UsersPublic;
import sumatodev.com.social.ui.fragments.EditCommentFragment;
import sumatodev.com.social.utils.AnimationUtils;
import sumatodev.com.social.utils.FormatterUtil;
import sumatodev.com.social.utils.Regex;
import sumatodev.com.social.utils.Utils;
import sumatodev.com.social.views.mention.Mentionable;
import sumatodev.com.social.views.mention.Mentions;
import sumatodev.com.social.views.mention.QueryListener;
import sumatodev.com.social.views.mention.SuggestionsListener;

import static java.sql.DriverManager.println;

public class PostDetailsActivity extends BaseActivity implements EditCommentDialog.CommentDialogCallback,
        SuggestionsListener, QueryListener {

    public static final String POST_ID_EXTRA_KEY = "PostDetailsActivity.POST_ID_EXTRA_KEY";
    public static final String AUTHOR_ANIMATION_NEEDED_EXTRA_KEY = "PostDetailsActivity.AUTHOR_ANIMATION_NEEDED_EXTRA_KEY";
    private static final int TIME_OUT_LOADING_COMMENTS = 30000;
    public static final int UPDATE_POST_REQUEST = 1;
    public static final String POST_STATUS_EXTRA_KEY = "PostDetailsActivity.POST_STATUS_EXTRA_KEY";
    private static final String TAG = PostDetailsActivity.class.getSimpleName();

    private EditText commentEditText;
    @Nullable
    private Post post;
    private ScrollView scrollView;
    private ViewGroup likesContainer;
    private ImageView likesImageView;
    private TextView commentsLabel;
    private TextView likeCounterTextView;
    private TextView commentsCountTextView;
    private TextView watcherCounterTextView;
    private TextView authorTextView;
    private TextView dateTextView;
    private ImageView authorImageView;
    private ProgressBar progressBar;
    private ImageView postImageView;
    private TextView titleTextView;
    private ProgressBar commentsProgressBar;
    private RecyclerView commentsRecyclerView;
    private TextView warningCommentsTextView;
    private RelativeLayout imageContainer;
    private Button sendButton;

    private boolean attemptToLoadComments = false;

    private MenuItem complainActionMenuItem;
    private MenuItem editActionMenuItem;
    private MenuItem deleteActionMenuItem;
    private MenuItem commentsOnOffAction;

    private String postId;

    private LinearLayout thumbnailView;
    private TextView thumbnailText;
    private ThumbnailView thumbnail;

    private FrameLayout textLayout;
    private PostManager postManager;
    private CommentManager commentManager;
    private ProfileManager profileManager;
    private LikeController likeController;
    private boolean postRemovingProcess = false;
    private boolean isPostExist;
    private boolean authorAnimationInProgress = false;

    private boolean isAuthorAnimationRequired;
    private CommentsAdapter commentsAdapter;
    private ActionMode mActionMode;
    private boolean isEnterTransitionFinished = false;

    private Mentions mentions;
    private UsersAdapter usersAdapter;
    private RelativeLayout parentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        profileManager = ProfileManager.getInstance(this);
        postManager = PostManager.getInstance(this);
        commentManager = CommentManager.getInstance(this);

        isAuthorAnimationRequired = getIntent().getBooleanExtra(AUTHOR_ANIMATION_NEEDED_EXTRA_KEY, false);
        postId = getIntent().getStringExtra(POST_ID_EXTRA_KEY);

        incrementWatchersCount();

        parentView = findViewById(R.id.parentView);
        titleTextView = findViewById(R.id.titleTextView);
        postImageView = findViewById(R.id.postImageView);
        progressBar = findViewById(R.id.progressBar);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        scrollView = findViewById(R.id.scrollView);
        commentsLabel = findViewById(R.id.commentsLabel);
        commentEditText = findViewById(R.id.commentEditText);
        likesContainer = findViewById(R.id.likesContainer);
        likesImageView = findViewById(R.id.likesImageView);
        authorImageView = findViewById(R.id.authorImageView);
        authorTextView = findViewById(R.id.authorTextView);
        likeCounterTextView = findViewById(R.id.likeCounterTextView);
        commentsCountTextView = findViewById(R.id.commentsCountTextView);
        watcherCounterTextView = findViewById(R.id.watcherCounterTextView);
        dateTextView = findViewById(R.id.dateTextView);
        commentsProgressBar = findViewById(R.id.commentsProgressBar);
        warningCommentsTextView = findViewById(R.id.warningCommentsTextView);
        imageContainer = findViewById(R.id.imageContainer);
        sendButton = findViewById(R.id.sendButton);
        textLayout = findViewById(R.id.textLayout);
        thumbnail = findViewById(R.id.thumbnail);
        thumbnailView = findViewById(R.id.thumbnailView);
        thumbnailText = findViewById(R.id.thumbnailLink);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isAuthorAnimationRequired) {
            authorImageView.setScaleX(0);
            authorImageView.setScaleY(0);

            // Add a listener to get noticed when the transition ends to animate the fab button
            getWindow().getSharedElementEnterTransition().addListener(new CustomTransitionListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onTransitionEnd(Transition transition) {
                    super.onTransitionEnd(transition);
                    //disable execution for exit transition
                    if (!isEnterTransitionFinished) {
                        isEnterTransitionFinished = true;
                        AnimationUtils.showViewByScale(authorImageView)
                                .setListener(authorAnimatorListener)
                                .start();
                    }
                }
            });
        }

        initRecyclerView();

        postManager.getPost(this, postId, createOnPostChangeListener());
        postImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageDetailScreen();
            }
        });

        commentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean valid = charSequence.toString().trim().length() > 0;
                if (post != null && post.getCommentStatus() != null) {
                    if (post.getCommentStatus().commentStatus) {
                        sendButton.setEnabled(valid);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

                boolean valid = editable.toString().trim().length() > 0;
                if (valid) {
//                    String[] words = editable.toString().split("[ \\.]");
//                    for (String word : words) {
//                        if (word.length() > 0 && word.charAt(0) == '@') {
//                            System.out.println(word);
//                            showNameSuggestions(word);
//                        }
//                    }
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasInternetConnection()) {
                    ProfileStatus profileStatus = ProfileManager.getInstance(PostDetailsActivity.this).checkProfile();

                    if (profileStatus.equals(ProfileStatus.PROFILE_CREATED)) {
                        sendComment();
                    } else {
                        doAuthorization(profileStatus);
                    }
                } else {
                    showSnackBar(R.string.internet_connection_failed);
                }
            }
        });

        commentsCountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollToFirstComment();
            }
        });

        View.OnClickListener onAuthorClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (post != null) {
                    if (checkInternetConnection()) {
                        ProfileStatus profileStatus = profileManager.checkProfile();
                        if (profileStatus.equals(ProfileStatus.PROFILE_CREATED)) {
                            openProfileActivity(post.getAuthorId(), v);
                        } else {
                            doAuthorization(profileStatus);
                        }
                    }
                }
            }
        };

        authorImageView.setOnClickListener(onAuthorClickListener);
        authorTextView.setOnClickListener(onAuthorClickListener);

        if (hasImage(postImageView) || hasImage(postImageView) && hasImage(authorImageView)) {
            supportPostponeEnterTransition();
        }

        showNameSuggestions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        postManager.closeListeners(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    hideKeyBoard();
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isAuthorAnimationRequired) {
            if (!authorAnimationInProgress) {
                ViewPropertyAnimator hideAuthorAnimator = sumatodev.com.social.utils.AnimationUtils.hideViewByScale(authorImageView);
                hideAuthorAnimator.setListener(authorAnimatorListener);
                hideAuthorAnimator.withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        PostDetailsActivity.super.onBackPressed();
                    }
                });
            }

        } else {
            super.onBackPressed();
        }
    }

    private void initRecyclerView() {

        commentsAdapter = new CommentsAdapter(PostDetailsActivity.this);
        commentsAdapter.setCallback(new CommentsAdapter.Callback() {
            @Override
            public void onLongItemClick(View view, int position) {
                Comment selectedComment = commentsAdapter.getItemByPosition(position);
                startActionMode(selectedComment);
            }

            @Override
            public void onAuthorClick(String authorId, View view) {
                openProfileActivity(authorId, view);
            }
        });
        commentsRecyclerView.setAdapter(commentsAdapter);
        commentsRecyclerView.setNestedScrollingEnabled(false);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.addItemDecoration(new DividerItemDecoration(commentsRecyclerView.getContext(),
                ((LinearLayoutManager) commentsRecyclerView.getLayoutManager()).getOrientation()));

        commentManager.getComments(postId, createOnCommentsChangedDataListener());

    }

    private void startActionMode(Comment selectedComment) {
        if (mActionMode != null) {
            return;
        }
        //check access to modify or remove post
        if (hasAccessToEditComment(selectedComment.getAuthorId()) || hasAccessToModifyPost()) {
            mActionMode = startSupportActionMode(new ActionModeCallback(selectedComment));
        }
    }

    private OnPostChangedListener createOnPostChangeListener() {
        return new OnPostChangedListener() {
            @Override
            public void onObjectChanged(Post obj) {
                if (obj != null) {
                    post = obj;
                    afterPostLoaded();
                } else if (!postRemovingProcess) {
                    isPostExist = false;
                    Intent intent = getIntent();
                    setResult(RESULT_OK, intent.putExtra(POST_STATUS_EXTRA_KEY, PostStatus.REMOVED));
                    showPostWasRemovedDialog();
                }
            }

            @Override
            public void onError(String errorText) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PostDetailsActivity.this);
                builder.setMessage(errorText);
                builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.setCancelable(false);
                builder.show();
            }
        };
    }

    private void afterPostLoaded() {
        isPostExist = true;
        initLikes();
        updateLayout();
        fillPostFields();
        updateCounters();
        initLikeButtonState();
        updateOptionMenuVisibility();
        updateCommentStatus();
    }

    private void updateCommentStatus() {
        if (post != null && post.getCommentStatus() != null) {
            if (post.getCommentStatus().commentStatus) {
                sendButton.setEnabled(true);
                commentEditText.setHint(R.string.comment_text_hint);
                commentEditText.setEnabled(true);
                commentEditText.setClickable(true);
                commentEditText.setLongClickable(true);
                commentEditText.setFocusable(true);
                commentEditText.setFocusableInTouchMode(true);
                commentEditText.requestFocus();
            } else {
                sendButton.setEnabled(false);
                commentEditText.setHint("comment status is off");
                commentEditText.setEnabled(false);
                commentEditText.setClickable(false);
                commentEditText.setLongClickable(false);
                commentEditText.setFocusable(false);
                commentEditText.clearFocus();
            }
        }
    }

    private void incrementWatchersCount() {
        postManager.incrementWatchersCount(postId);
        Intent intent = getIntent();
        setResult(RESULT_OK, intent.putExtra(POST_STATUS_EXTRA_KEY, PostStatus.UPDATED));
    }

    private void showPostWasRemovedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PostDetailsActivity.this);
        builder.setMessage(R.string.error_post_was_removed);
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void fillPostFields() {
        if (post != null) {
            if (post.getTitle() != null) {
                titleTextView.setVisibility(View.VISIBLE);
                titleTextView.setText(post.getTitle());

                Link link = new Link(Regex.WEB_URL_PATTERN)
                        .setTextColor(Color.BLUE).setOnClickListener(new Link.OnClickListener() {
                            @Override
                            public void onClick(String s) {
                                if (!s.isEmpty()) {
                                    openUrlActivity(s);
                                }
                            }
                        });

                LinkBuilder.on(titleTextView).addLink(link).build();
                titleTextView.setMovementMethod(TouchableMovementMethod.getInstance());
            } else {
                titleTextView.setVisibility(View.GONE);
            }

            if (post.getLink() != null) {

                thumbnailView.setVisibility(View.VISIBLE);
                thumbnailText.setText(post.getLink());

                Link link = new Link(Regex.WEB_URL_PATTERN)
                        .setTextColor(Color.BLUE).setOnClickListener(new Link.OnClickListener() {
                            @Override
                            public void onClick(String s) {
                                openYouTubeUrl(s);
                            }
                        });

                LinkBuilder.on(thumbnailText).addLink(link).build();
                thumbnailText.setMovementMethod(TouchableMovementMethod.getInstance());

                ThumbnailLoader.initialize().setVideoInfoDownloader(new OembedVideoInfoDownloader());
                thumbnail.loadThumbnail(post.getLink());
                thumbnail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openYouTubeUrl(post.getLink());
                    }
                });


            }

            if (post.getImagePath() != null) {
                imageContainer.setVisibility(View.VISIBLE);
                loadPostDetailsImage();
            }

            loadAuthorImage();
        }
    }


    private void openYouTubeUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void updateLayout() {
        if (post != null) {
            if (post.getPostStyle() != null) {
                int bg_color = post.getPostStyle().bg_color;
                if (bg_color == 0) {

                    textLayout.setBackgroundColor(Color.TRANSPARENT);
                    titleTextView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                            , ViewGroup.LayoutParams.WRAP_CONTENT));
                    titleTextView.setTextColor(getResources().getColor(R.color.primary_dark_text));
                    titleTextView.setTextSize(18);
                    titleTextView.setGravity(Gravity.START | Gravity.TOP);
                    titleTextView.setTypeface(Typeface.DEFAULT);

                } else {
                    final float scale = getResources().getDisplayMetrics().density;
                    int pixels = (int) (180 * scale + 0.5f);

                    textLayout.setBackgroundColor(bg_color);
                    titleTextView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                            , pixels));
                    titleTextView.setTextColor(Color.WHITE);
                    titleTextView.setTextSize(24);
                    titleTextView.setGravity(Gravity.CENTER);
                    titleTextView.setMaxLines(3);
                    titleTextView.setTypeface(Typeface.DEFAULT_BOLD);
                }
            }
        }
    }

    private void loadPostDetailsImage() {
        if (post == null) {
            return;
        }

        String imageUrl = post.getImagePath();
        int width = Utils.getDisplayWidth(this);
        int height = (int) getResources().getDimension(R.dimen.post_detail_image_height);
        Glide.with(this)
                .load(imageUrl)
                .centerCrop()
                .override(width, height)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_stub)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        scheduleStartPostponedTransition(postImageView);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        scheduleStartPostponedTransition(postImageView);
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .crossFade()
                .into(postImageView);
    }

    private void scheduleStartPostponedTransition(final ImageView imageView) {
        imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                supportStartPostponedEnterTransition();
                return true;
            }
        });
    }

    private void loadAuthorImage() {
        if (post != null && post.getAuthorId() != null) {
            profileManager.getProfileSingleValue(post.getAuthorId(), createProfileChangeListener());
        }
    }

    private void updateCounters() {
        if (post == null) {
            return;
        }

        long commentsCount = post.getCommentsCount();
        commentsCountTextView.setText(String.valueOf(commentsCount));
        commentsLabel.setText(String.format(getString(R.string.label_comments), commentsCount));
        likeCounterTextView.setText(String.valueOf(post.getLikesCount()));
        likeController.setUpdatingLikeCounter(false);

        watcherCounterTextView.setText(String.valueOf(post.getWatchersCount()));

        CharSequence date = FormatterUtil.getRelativeTimeSpanStringShort(this, post.getCreatedDate());
        dateTextView.setText(date);

        if (commentsCount == 0) {
            commentsLabel.setVisibility(View.GONE);
            commentsProgressBar.setVisibility(View.GONE);
        } else if (commentsLabel.getVisibility() != View.VISIBLE) {
            commentsLabel.setVisibility(View.VISIBLE);
        }
    }

    private OnObjectChangedListener<Profile> createProfileChangeListener() {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                if (obj == null)
                    return;

                if (obj.getPhotoUrl() != null) {
                    Glide.with(PostDetailsActivity.this)
                            .load(obj.getPhotoUrl())
                            .placeholder(R.drawable.user_thumbnail)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .crossFade()
                            .into(authorImageView);
                }

                authorTextView.setText(obj.getUsername());
            }
        };
    }

    private OnDataChangedListener<Comment> createOnCommentsChangedDataListener() {
        attemptToLoadComments = true;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (attemptToLoadComments) {
                    commentsProgressBar.setVisibility(View.GONE);
                    warningCommentsTextView.setVisibility(View.VISIBLE);
                }
            }
        }, TIME_OUT_LOADING_COMMENTS);


        return new OnDataChangedListener<Comment>() {
            @Override
            public void onListChanged(List<Comment> list) {
                attemptToLoadComments = false;
                commentsProgressBar.setVisibility(View.GONE);
                commentsRecyclerView.setVisibility(View.VISIBLE);
                warningCommentsTextView.setVisibility(View.GONE);
                commentsAdapter.setList(list);
            }

            @Override
            public void inEmpty(Boolean empty, String error) {

            }
        };
    }

    private void openImageDetailScreen() {
        if (post != null) {
            Intent intent = new Intent(this, ImageDetailActivity.class);
            intent.putExtra(ImageDetailActivity.IMAGE_URL_EXTRA_KEY, post.getImagePath());
            startActivity(intent);
        }
    }


    private void openProfileActivity(String userId, View view) {

        Intent intent = new Intent(PostDetailsActivity.this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_ID_EXTRA_KEY, userId);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view != null) {

            ActivityOptions options = ActivityOptions.
                    makeSceneTransitionAnimation(PostDetailsActivity.this,
                            new android.util.Pair<>(view, getString(R.string.post_author_image_transition_name)));
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }

    }

    private OnObjectExistListener<Like> createOnLikeObjectExistListener() {
        return new OnObjectExistListener<Like>() {
            @Override
            public void onDataChanged(boolean exist) {
                likeController.initLike(exist);
            }
        };
    }

    private void initLikeButtonState() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && post != null) {
            postManager.hasCurrentUserLike(this, post.getId(),
                    firebaseUser.getUid(), createOnLikeObjectExistListener());
        }
    }

    private void initLikes() {
        likeController = new LikeController(this, post, likeCounterTextView, likesImageView, false);

        likesContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPostExist) {
                    likeController.handleLikeClickAction(PostDetailsActivity.this, post);
                }
            }
        });

        //long click for changing animation
        likesContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (likeController.getLikeAnimationType() == LikeController.AnimationType.BOUNCE_ANIM) {
                    likeController.setLikeAnimationType(LikeController.AnimationType.COLOR_ANIM);
                } else {
                    likeController.setLikeAnimationType(LikeController.AnimationType.BOUNCE_ANIM);
                }

                Snackbar snackbar = Snackbar
                        .make(likesContainer, "Animation was changed", Snackbar.LENGTH_LONG);

                snackbar.show();
                return true;
            }
        });
    }

    private void sendComment() {
        if (post == null) {
            return;
        }


        String commentText = commentEditText.getText().toString();

        Comment comment = new Comment();
        comment.setText(commentText);
        comment.setPostId(post.getId());
        comment.setMentions(mentions.getInsertedMentions());
        comment.setAuthorId(getCurrentUser());

        if (commentText.length() > 0 && isPostExist) {
            commentManager.createOrUpdateComment(comment, new OnTaskCompleteListener() {
                @Override
                public void onTaskComplete(boolean success) {
                    if (success) {
                        commentsAdapter.cleanSelectedPosition();
                        scrollToFirstComment();
                    }
                }
            });
            Log.d(TAG, "MentionList: " + mentions.getInsertedMentions());
//            final List<Mentionable> mentionables = mentions.getInsertedMentions();
//            for (Mentionable mention : mentionables) {
//                Log.d(TAG, "Position of 1st Character in EditText " + mention.getMentionOffset());
//                Log.d(TAG, "Text " + mention.getMentionName());
//                Log.d(TAG, "Length " + mention.getMentionLength());
//            }

            commentEditText.setText(null);
            commentEditText.clearFocus();
            hideKeyBoard();
        }
    }

    private void scrollToFirstComment() {
        if (post != null && post.getCommentsCount() > 0) {
            scrollView.smoothScrollTo(0, commentsLabel.getTop());
        }
    }

    private void hideKeyBoard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private boolean hasAccessToEditComment(String commentAuthorId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null && commentAuthorId.equals(currentUser.getUid());
    }

    private boolean hasAccessToModifyPost() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null && post != null && post.getAuthorId().equals(currentUser.getUid());
    }

    private void updateOptionMenuVisibility() {
        if (editActionMenuItem != null && deleteActionMenuItem != null && commentsOnOffAction != null && hasAccessToModifyPost()) {
            editActionMenuItem.setVisible(true);
            deleteActionMenuItem.setVisible(true);
            commentsOnOffAction.setVisible(true);
        }

        if (complainActionMenuItem != null && post != null && !post.isHasComplain()) {
            complainActionMenuItem.setVisible(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.post_details_menu, menu);
        complainActionMenuItem = menu.findItem(R.id.complain_action);
        editActionMenuItem = menu.findItem(R.id.edit_post_action);
        deleteActionMenuItem = menu.findItem(R.id.delete_post_action);
        commentsOnOffAction = menu.findItem(R.id.comments_action);

        if (post != null) {
            updateOptionMenuVisibility();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isPostExist) {
            return super.onOptionsItemSelected(item);
        }

        // Handle item selection
        int i = item.getItemId();
        if (i == R.id.complain_action) {
            doComplainAction();
            return true;
        } else if (i == R.id.edit_post_action) {
            if (hasAccessToModifyPost()) {
                openEditPostActivity();
            }
            return true;
        } else if (i == R.id.delete_post_action) {
            if (hasAccessToModifyPost()) {
                attemptToRemovePost();
            }
            return true;
        } else if (i == R.id.comments_action) {
            if (hasAccessToModifyPost()) {
                setCommentsAction();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void setCommentsAction() {
        if (checkInternetConnection()) {
            ProfileStatus profileStatus = profileManager.checkProfile();

            if (profileStatus.equals(ProfileStatus.PROFILE_CREATED)) {
                openCommentsActionDialoag();
            } else {
                doAuthorization(profileStatus);
            }
        }
    }


    private void doComplainAction() {
        if (checkInternetConnection()) {
            ProfileStatus profileStatus = profileManager.checkProfile();

            if (profileStatus.equals(ProfileStatus.PROFILE_CREATED)) {
                openComplainDialog();
            } else {
                doAuthorization(profileStatus);
            }
        }
    }

    private void attemptToRemovePost() {
        if (hasInternetConnection()) {
            if (!postRemovingProcess) {
                openConfirmDeletingDialog();
            }
        } else {
            showSnackBar(R.string.internet_connection_failed);
        }
    }

    private void removePost() {
        postManager.removePost(post, new OnTaskCompleteListener() {
            @Override
            public void onTaskComplete(boolean success) {
                if (success) {
                    Intent intent = getIntent();
                    setResult(RESULT_OK, intent.putExtra(POST_STATUS_EXTRA_KEY, PostStatus.REMOVED));
                    finish();
                } else {
                    postRemovingProcess = false;
                    showSnackBar(R.string.error_fail_remove_post);
                }

                hideProgress();
            }
        });

        showProgress(R.string.removing);
        postRemovingProcess = true;
    }

    private void openEditPostActivity() {
        if (hasInternetConnection()) {
            Intent intent = new Intent(PostDetailsActivity.this, EditPostActivity.class);
            intent.putExtra(EditPostActivity.POST_EXTRA_KEY, post);
            startActivityForResult(intent, EditPostActivity.EDIT_POST_REQUEST);
        } else {
            showSnackBar(R.string.internet_connection_failed);
        }
    }

    private void openConfirmDeletingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_deletion_post)
                .setNegativeButton(R.string.button_title_cancel, null)
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removePost();
                    }
                });

        builder.create().show();
    }

    private void openCommentsActionDialoag() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.switch_layout, null);
        builder.setView(view);

        Switch statusBtn = view.findViewById(R.id.switchBtn);

        if (post != null && post.getCommentStatus() != null) {
            statusBtn.setChecked(post.getCommentStatus().commentStatus);
        }

        statusBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setCommentStatus(true);
                } else {
                    setCommentStatus(false);
                }
            }
        });

        builder.setPositiveButton("Close", null);
        builder.create().show();
    }

    private void setCommentStatus(boolean value) {
        if (post != null) {
            commentManager.setCommentsState(post.getId(), value, new OnTaskCompleteListener() {
                @Override
                public void onTaskComplete(boolean success) {
                    if (success) {
                        Log.d(TAG, "Status Updated");
                    } else {
                        Log.d(TAG, "Status Failed");
                    }
                }
            });
        }
    }

    private void openComplainDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_complain)
                .setMessage(R.string.complain_text)
                .setNegativeButton(R.string.button_title_cancel, null)
                .setPositiveButton(R.string.add_complain, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        addComplain();
                    }
                });

        builder.create().show();
    }

    private void addComplain() {
        postManager.addComplain(post);
        complainActionMenuItem.setVisible(false);
        showSnackBar(R.string.complain_sent);
    }

    private void removeComment(String commentId, final ActionMode mode, final int position) {
        showProgress();
        commentManager.removeComment(commentId, postId, new OnTaskCompleteListener() {
            @Override
            public void onTaskComplete(boolean success) {
                commentsAdapter.removeComment();
                hideProgress();
                mode.finish(); // Action picked, so close the CAB
                showSnackBar(R.string.message_comment_was_removed);
            }
        });
    }

    private void openEditCommentDialog(Comment comment) {
        EditCommentDialog editCommentFragment = new EditCommentDialog();
        Bundle args = new Bundle();
//        args.putString(EditCommentDialog.COMMENT_TEXT_KEY, comment.getText());
//        args.putString(EditCommentDialog.COMMENT_ID_KEY, comment.getId());
        args.putSerializable(EditCommentDialog.COMMENT_KEY, comment);

        editCommentFragment.setArguments(args);
        editCommentFragment.show(getFragmentManager(), EditCommentFragment.TAG);
    }

    @Override
    public void onCommentChanged(HashMap<String, Object> hashMap) {
        //updateComment(newText, commentId);
        showProgress();
        commentManager.updateSingleComment(postId, hashMap, new OnTaskCompleteListener() {
            @Override
            public void onTaskComplete(boolean success) {
                commentsAdapter.updateComment();
                hideProgress();
                showSnackBar(R.string.message_comment_was_edited);
            }
        });

    }

    private void updateComment(String newText, String commentId) {
        showProgress();
        commentManager.updateComment(commentId, newText, postId, new OnTaskCompleteListener() {
            @Override
            public void onTaskComplete(boolean success) {
                commentsAdapter.updateComment();
                hideProgress();
                showSnackBar(R.string.message_comment_was_edited);
            }
        });
    }

    private boolean hasImage(@NonNull ImageView view) {
        Drawable drawable = view.getDrawable();
        boolean hasImage = (drawable != null);

        if (hasImage && (drawable instanceof BitmapDrawable)) {
            hasImage = ((BitmapDrawable) drawable).getBitmap() != null;
        }

        return hasImage;
    }


    private class ActionModeCallback implements ActionMode.Callback {
        Comment selectedComment;
        int position;

        ActionModeCallback(Comment selectedComment) {
            this.selectedComment = selectedComment;
        }

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.comment_context_menu, menu);

            menu.findItem(R.id.editMenuItem).setVisible(hasAccessToEditComment(selectedComment.getAuthorId()));

            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int i = item.getItemId();
            if (i == R.id.editMenuItem) {
                openEditCommentDialog(selectedComment);
                mode.finish(); // Action picked, so close the CAB
                return true;
            } else if (i == R.id.deleteMenuItem) {
                removeComment(selectedComment.getId(), mode, position);
                return true;
            } else {
                return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }

    }

    private void showNameSuggestions() {

        mentions = new Mentions.Builder(this, commentEditText)
                .suggestionsListener(this)
                .queryListener(this)
                .build();

        final RecyclerView mentionsList = findViewById(R.id.mentions_list);
        mentionsList.setLayoutManager(new LinearLayoutManager(this));
        mentionsList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        usersAdapter = new UsersAdapter(this);
        mentionsList.setAdapter(usersAdapter);

        // set on item click listener
        mentionsList.addOnItemTouchListener(new RecyclerItemClickListener(this,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(final View view, final int position) {
                        final UsersPublic user = usersAdapter.getItem(position);
                        /*
                         * We are creating a mentions object which implements the
                         * <code>Mentionable</code> interface this allows the library to set the offset
                         * and length of the mention.
                         */
                        if (user != null) {
                            final Mention mention = new Mention();
                            mention.setMentionName(user.getUsername());
                            mention.setUserId(user.getId());
                            mentions.insertMention(mention);
                        }

                    }
                }));

    }


    @Override
    public void displaySuggestions(boolean display) {
        if (display) {
            ViewUtils.showView(this, R.id.mentions_list_layout);
        } else {
            ViewUtils.hideView(this, R.id.mentions_list_layout);
        }
    }

    @Override
    public void onQueryReceived(final String query) {

        UsersManager.getInstance(this).getUsersList(this, query.toLowerCase(),
                new OnDataChangedListener<UsersPublic>() {
                    @Override
                    public void onListChanged(List<UsersPublic> list) {
                        if (!list.isEmpty()) {
                            usersAdapter.clear();
                            usersAdapter.setCurrentQuery(query);
                            usersAdapter.addAll(list);
                            showMentionsList(true);
                        } else {
                            showMentionsList(false);
                        }
                    }

                    @Override
                    public void inEmpty(Boolean empty, String error) {

                    }
                });
    }

    private void showMentionsList(boolean display) {
        ViewUtils.showView(this, R.id.mentions_list_layout);
        if (display) {
            ViewUtils.showView(this, R.id.mentions_list);
            ViewUtils.hideView(this, R.id.mentions_empty_view);
        } else {
            ViewUtils.hideView(this, R.id.mentions_list);
            ViewUtils.showView(this, R.id.mentions_empty_view);
        }
    }

    public void showFloatButtonRelatedSnackBar(int messageId) {
        showSnackBar(messageId);
    }

    Animator.AnimatorListener authorAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            authorAnimationInProgress = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            authorAnimationInProgress = false;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            authorAnimationInProgress = false;
        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

}
