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
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.sumatodev.social_chat_sdk.views.activities.ChatActivity;
import com.sumatodev.social_chat_sdk.views.activities.ThreadsActivity;

import cz.kinst.jakub.view.SimpleStatefulLayout;
import sumatodev.com.social.R;
import sumatodev.com.social.adapters.PostsByUserAdapter;
import sumatodev.com.social.controllers.FollowController;
import sumatodev.com.social.enums.Consts;
import sumatodev.com.social.enums.PostStatus;
import sumatodev.com.social.enums.ProfileStatus;
import sumatodev.com.social.managers.FirebaseUtils;
import sumatodev.com.social.managers.PostManager;
import sumatodev.com.social.managers.ProfileManager;
import sumatodev.com.social.managers.listeners.OnObjectChangedListener;
import sumatodev.com.social.managers.listeners.OnObjectExistListener;
import sumatodev.com.social.managers.listeners.OnPostCreatedListener;
import sumatodev.com.social.managers.listeners.OnTaskCompleteListener;
import sumatodev.com.social.model.AccountStatus;
import sumatodev.com.social.model.Post;
import sumatodev.com.social.model.Profile;
import sumatodev.com.social.utils.LogUtil;
import sumatodev.com.social.utils.LogoutHelper;

public class ProfileActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener,
        OnPostCreatedListener, View.OnClickListener {
    private static final String TAG = ProfileActivity.class.getSimpleName();
    public static final int CREATE_POST_FROM_PROFILE_REQUEST = 22;
    public static final String USER_ID_EXTRA_KEY = "ProfileActivity.USER_ID_EXTRA_KEY";

    // UI references.
    private AppBarLayout appbar;
    private TextView nameEditText;
    private ImageView imageView;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView postsCounterTextView, userFollowers, userFollowings;
    private TextView postsLabelTextView;
    private SimpleStatefulLayout dataStatefulLayout, parentStatefulLayout;
    private Button followBtn;
    private LinearLayout dataLayout;
    private Button messageBtn;


    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private String currentUserId;
    private String userID;

    private PostsByUserAdapter postsAdapter;
    private SwipeRefreshLayout swipeContainer;
    private ProfileManager profileManager;
    private PostManager postManager;
    private FollowController followController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_message_view);
        findViews();
        setupToolBar();

        if (getIntent() != null) {
            userID = getIntent().getStringExtra(USER_ID_EXTRA_KEY);
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        }

        postManager = PostManager.getInstance(this);
        profileManager = ProfileManager.getInstance(this);
        followController = new FollowController(this, userID, followBtn);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onRefreshAction();
            }
        });

        //supportPostponeEnterTransition();

        loadProfile();

        if (hasInternetConnection()) {
            if (userID != null) {
                profileManager.checkAccountStatus(userID, onObjectChangedListener());
            }
        } else {
            parentStatefulLayout.showOffline();
        }
    }

    private void findViews() {
        parentStatefulLayout = findViewById(R.id.parentStatefulLayout);
        appbar = findViewById(R.id.appbar);
        progressBar = findViewById(R.id.progressBar);
        imageView = findViewById(R.id.imageView);
        postsCounterTextView = findViewById(R.id.postsCounterTextView);
        followBtn = findViewById(R.id.followBtn);
        postsLabelTextView = findViewById(R.id.postsLabelTextView);
        dataStatefulLayout = findViewById(R.id.statefulLayout);
        swipeContainer = findViewById(R.id.swipeContainer);
        userFollowers = findViewById(R.id.userFollowers);
        userFollowings = findViewById(R.id.userFollowings);
        dataLayout = findViewById(R.id.dataLayout);
        messageBtn = findViewById(R.id.messageBtn);

        userFollowers.setOnClickListener(this);
        userFollowings.setOnClickListener(this);
        messageBtn.setOnClickListener(this);
        followBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == userFollowers) {
            if (userID != null) {
                Intent intent = new Intent(ProfileActivity.this, FollowersActivity.class);
                intent.putExtra(FollowersActivity.USER_ID_EXTRA_KEY, userID);
                intent.putExtra(FollowersActivity.REF_TYPE, Consts.FOLLOWERS_LIST_REF);
                startActivity(intent);
            }
        } else if (v == userFollowings) {
            if (userID != null) {
                Intent intent = new Intent(ProfileActivity.this, FollowingActivity.class);
                intent.putExtra(FollowingActivity.USER_ID_EXTRA_KEY, userID);
                intent.putExtra(FollowersActivity.REF_TYPE, Consts.FOLLOWING_LIST_REF);
                startActivity(intent);
            }
        } else if (v == messageBtn) {
            Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
            intent.putExtra(ChatActivity.USER_KEY, userID);
            startActivity(intent);
        } else if (v == followBtn) {
            followController.handleFollowAction(ProfileActivity.this, userID);
        }
    }


    private void setupToolBar() {
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private OnObjectChangedListener<AccountStatus> onObjectChangedListener() {
        return new OnObjectChangedListener<AccountStatus>() {
            @Override
            public void onObjectChanged(AccountStatus obj) {
                if (obj != null) {
                    if (obj.profileStatus.equals(Consts.ACCOUNT_DISABLED)) {
                        parentStatefulLayout.showEmpty();
                        dataStatefulLayout.showEmpty();

                        if (actionBar != null) {
                            actionBar.setTitle(R.string.title_activity_profile);
                        }

                    } else if (obj.profileStatus.equals(Consts.ACCOUNT_ACTIVE)) {
                        parentStatefulLayout.showContent();

                        if (userID != null) {
                            if (userID.equalsIgnoreCase(currentUserId)) {
                                followBtn.setVisibility(View.GONE);
                                messageBtn.setVisibility(View.GONE);
                            }
                        }

                        followController.checkFollowStatus();
                        loadProfile();
                        loadPostsList();

                        if (mGoogleApiClient != null) {
                            mGoogleApiClient.connect();
                        }
                    }
                } else {
                    parentStatefulLayout.showContent();

                    appbar.setVisibility(View.VISIBLE);

                    dataLayout.setVisibility(View.VISIBLE);
                    if (userID != null) {
                        if (userID.equalsIgnoreCase(currentUserId)) {
                            followBtn.setVisibility(View.GONE);
                            messageBtn.setVisibility(View.GONE);
                        }
                        
                    }

                    loadProfile();
                    loadPostsList();

                    if (mGoogleApiClient != null) {
                        mGoogleApiClient.connect();
                    }
                }
            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();
        profileManager.closeListeners(this);


        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage(this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CreatePostActivity.CREATE_NEW_POST_REQUEST:
                    if (data != null) {
                        createNewPost(data);
                    }
                    break;
                case PostDetailsActivity.UPDATE_POST_REQUEST:
                    if (data != null) {
                        PostStatus postStatus = (PostStatus) data.getSerializableExtra(PostDetailsActivity.POST_STATUS_EXTRA_KEY);
                        if (postStatus.equals(PostStatus.REMOVED)) {
                            postsAdapter.removeSelectedPost();

                        } else if (postStatus.equals(PostStatus.UPDATED)) {
                            postsAdapter.updateSelectedPost();
                        }
                    }
                    break;
            }
        }
    }

    private void createNewPost(Intent data) {
        Post post = (Post) data.getSerializableExtra(CreatePostActivity.POST_DATA_KEY);
        if (post != null) {
            postManager.createOrUpdatePostWithImage(this, post, ProfileActivity.this);
        }

    }

    @Override
    public void onPostSaved(boolean success) {
        hideProgress();
        if (success) {
            postsAdapter.loadPosts();
            setResult(RESULT_OK);
            //showFloatButtonRelatedSnackBar(R.string.message_post_was_created);
            LogUtil.logDebug(TAG, "Post was created");
        } else {
            showSnackBar(R.string.error_fail_create_post);
            LogUtil.logDebug(TAG, "Failed to create a post");
        }
    }

    private void onRefreshAction() {
        postsAdapter.loadPosts();
    }

    private void loadPostsList() {
        if (recyclerView == null) {
            dataStatefulLayout.showProgress();

            recyclerView = findViewById(R.id.recycler_view);
            postsAdapter = new PostsByUserAdapter(this, userID);
            postsAdapter.setCallBack(new PostsByUserAdapter.CallBack() {
                @Override
                public void onItemClick(final Post post, final View view) {
                    PostManager.getInstance(ProfileActivity.this).isPostExistSingleValue(post.getId(),
                            new OnObjectExistListener<Post>() {
                                @Override
                                public void onDataChanged(boolean exist) {
                                    if (exist) {
                                        openPostDetailsActivity(post, view);
                                    } else {
                                        showSnackBar(R.string.error_post_was_removed);
                                    }
                                }
                            });
                }

                @Override
                public void onPostsListChanged(int postsCount) {
                    String postsLabel = getResources().getQuantityString(R.plurals.posts_counter_format, postsCount, postsCount);
                    postsCounterTextView.setText(buildCounterSpannable(postsLabel, postsCount));

                    if (postsCount > 0) {
                        postsLabelTextView.setVisibility(View.VISIBLE);
                        dataStatefulLayout.showContent();
                    } else if (postsCount == 0) {
                        dataStatefulLayout.showEmpty();
                    }

                    swipeContainer.setRefreshing(false);
                }

                @Override
                public void onAuthorClick(String authorId, View view) {
                    if (checkInternetConnection()) {
                        ProfileStatus status = profileManager.checkProfile();
                        if (status.equals(ProfileStatus.PROFILE_CREATED)) {
                            openProfileActivity(authorId, view);

                        } else {
                            doAuthorization(status);
                        }
                    }
                }

                @Override
                public void onLinkClick(String linkUrl) {
                    if (!linkUrl.isEmpty()) {
                        openUrlActivity(linkUrl);
                    }
                }

                @Override
                public void onPostLoadingCanceled() {
                    swipeContainer.setRefreshing(false);
                    dataStatefulLayout.setEmptyText("loading canceled");
                    dataStatefulLayout.showEmpty();
                }
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
            recyclerView.setAdapter(postsAdapter);
            postsAdapter.loadPosts();
        }
    }

    private Spannable buildCounterSpannable(String label, int value) {
        SpannableStringBuilder contentString = new SpannableStringBuilder();
        contentString.append(String.valueOf(value));
        contentString.append("\n");
        int start = contentString.length();
        contentString.append(label);
        contentString.setSpan(new TextAppearanceSpan(this, R.style.TextAppearance_Message),
                start, contentString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return contentString;
    }

    private void openProfileActivity(String userId, View view) {

        Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_ID_EXTRA_KEY, userId);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view != null) {

            ActivityOptions options = ActivityOptions.
                    makeSceneTransitionAnimation(ProfileActivity.this,
                            new android.util.Pair<>(view, getString(R.string.post_author_image_transition_name)));
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }

    }

    @SuppressLint("RestrictedApi")
    private void openPostDetailsActivity(Post post, View v) {
        Intent intent = new Intent(ProfileActivity.this, PostDetailsActivity.class);
        intent.putExtra(PostDetailsActivity.POST_ID_EXTRA_KEY, post.getId());
        intent.putExtra(PostDetailsActivity.AUTHOR_ANIMATION_NEEDED_EXTRA_KEY, true);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            View imageView = v.findViewById(R.id.postImageView);
            View authorImageView = v.findViewById(R.id.authorImageView);

            if (imageView != null) {
                ActivityOptions options = ActivityOptions.
                        makeSceneTransitionAnimation(ProfileActivity.this,
                                new android.util.Pair<>(imageView, getString(R.string.post_image_transition_name))
                        );
                startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST, options.toBundle());
            } else if (authorImageView != null) {
                ActivityOptions options = ActivityOptions.
                        makeSceneTransitionAnimation(ProfileActivity.this,
                                new android.util.Pair<>(authorImageView, getString(R.string.post_author_image_transition_name))
                        );
                startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST, options.toBundle());
            } else {
                startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST);
            }

        } else {
            startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST);
        }
    }

    private void loadProfile() {
        profileManager.getProfileValue(ProfileActivity.this, userID, createOnProfileChangedListener());
    }

    private OnObjectChangedListener<Profile> createOnProfileChangedListener() {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                fillUIFields(obj);
            }
        };
    }

    private void fillUIFields(final Profile profile) {
        if (profile != null) {
            if (actionBar != null) {
                actionBar.setTitle(profile.getUsername());
            }

            if (profile.getPhotoUrl() != null) {

                Glide.with(this)
                        .load(profile.getPhotoUrl())
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                scheduleStartPostponedTransition(imageView);
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                scheduleStartPostponedTransition(imageView);
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(imageView);


                checkFollowStatus();

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openImageDetailScreen(profile.getPhotoUrl());
                    }
                });
            } else {
                progressBar.setVisibility(View.GONE);
                imageView.setImageResource(R.drawable.ic_stub);
            }

            int likesCount = (int) profile.getLikesCount();
            String likesLabel = getResources().getQuantityString(R.plurals.likes_counter_format, likesCount, likesCount);
            //likesCountersTextView.setText(buildCounterSpannable(likesLabel, likesCount));

        }
    }

    private void openImageDetailScreen(String profileUrl) {
        Intent intent = new Intent(this, ImageDetailActivity.class);
        intent.putExtra(ImageDetailActivity.IMAGE_URL_EXTRA_KEY, profileUrl);
        startActivity(intent);
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

    private void startMainActivity() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void startEditProfileActivity() {
        if (hasInternetConnection()) {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        } else {
            showSnackBar(R.string.internet_connection_failed);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        LogUtil.logDebug(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void openCreatePostActivity() {
        Intent intent = new Intent(this, CreatePostActivity.class);
        startActivityForResult(intent, CreatePostActivity.CREATE_NEW_POST_REQUEST);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (userID.equals(currentUserId)) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.profile_menu, menu);
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int i = item.getItemId();
        if (i == R.id.editProfile) {
            startEditProfileActivity();
            return true;
        } else if (i == R.id.signOut) {
            LogoutHelper.signOut(mGoogleApiClient, this);
            startMainActivity();
            return true;
        } else if (i == R.id.createPost) {
            if (hasInternetConnection()) {
                openCreatePostActivity();
            } else {
                showSnackBar(R.string.internet_connection_failed);
            }

            return super.onOptionsItemSelected(item);
        } else if (i == R.id.follow_requests) {
            startActivity(new Intent(ProfileActivity.this, RequestsActivity.class));
            return true;
        } else if (i == R.id.threads) {
            startActivity(new Intent(ProfileActivity.this, ThreadsActivity.class));
            return true;
        } else if (i == R.id.action_delete) {
            initAccountDelete();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void initAccountDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account").
                setMessage("Are you sure you want to delete this account..")
                .setNegativeButton(R.string.button_title_cancel, null)
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAccount();
                    }
                });
        builder.create().show();
    }

    private void deleteAccount() {
        profileManager.deleteAccount(new OnTaskCompleteListener() {
            @Override
            public void onTaskComplete(boolean success) {
                if (success) {
                    LogoutHelper.signOut(mGoogleApiClient, ProfileActivity.this);
                    startMainActivity();
                }
            }
        });
    }


    private void checkFollowStatus() {
        FirebaseUtils.getFriendsRef()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            long totalFollowers = dataSnapshot.child(userID).child(Consts.FOLLOWERS_LIST_REF).getChildrenCount();
                            long totalFollowings = dataSnapshot.child(userID).child(Consts.FOLLOWING_LIST_REF).getChildrenCount();

                            String followers = getResources().getQuantityString(R.plurals.user_follower_format,
                                    (int) totalFollowers, totalFollowers);
                            userFollowers.setText(buildCounterSpannable(followers, (int) totalFollowers));

                            String following = getResources().getQuantityString(R.plurals.user_following_format, (int) totalFollowings,
                                    (int) totalFollowings);
                            userFollowings.setText(buildCounterSpannable(following, (int) totalFollowings));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        showError(databaseError);
                    }
                });
    }


    private void showError(DatabaseError databaseError) {
        switch (databaseError.getCode()) {
            case DatabaseError.PERMISSION_DENIED:
                Log.d("TAG", "permission denied");
                Toast.makeText(ProfileActivity.this, "permission denied", Toast.LENGTH_SHORT).show();
                break;
            case DatabaseError.NETWORK_ERROR:
                Log.d("TAG", "network error");
                Toast.makeText(ProfileActivity.this, "network error", Toast.LENGTH_SHORT).show();
                break;
            default:
                Log.d("TAG", "request failed");
                Toast.makeText(ProfileActivity.this, "request failed", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        profileManager.closeListeners(this);
    }
}
