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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import cz.kinst.jakub.view.SimpleStatefulLayout;
import sumatodev.com.social.R;
import sumatodev.com.social.adapters.PostsByUserAdapter;
import sumatodev.com.social.enums.Consts;
import sumatodev.com.social.enums.PostStatus;
import sumatodev.com.social.managers.FirebaseUtils;
import sumatodev.com.social.managers.PostManager;
import sumatodev.com.social.managers.ProfileManager;
import sumatodev.com.social.managers.listeners.OnObjectChangedListener;
import sumatodev.com.social.managers.listeners.OnObjectExistListener;
import sumatodev.com.social.managers.listeners.OnPostCreatedListener;
import sumatodev.com.social.model.Post;
import sumatodev.com.social.model.Profile;
import sumatodev.com.social.model.UsersThread;
import sumatodev.com.social.utils.LogUtil;
import sumatodev.com.social.utils.LogoutHelper;
import sumatodev.com.social.utils.NotificationView;

public class ProfileActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener, OnPostCreatedListener, View.OnClickListener {
    private static final String TAG = ProfileActivity.class.getSimpleName();
    public static final int CREATE_POST_FROM_PROFILE_REQUEST = 22;
    public static final String USER_ID_EXTRA_KEY = "ProfileActivity.USER_ID_EXTRA_KEY";

    // UI references.
    private TextView nameEditText;
    private ImageView imageView;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView postsCounterTextView, userFollowers, userFollowings;
    private TextView postsLabelTextView;
    private SimpleStatefulLayout statefulLayout;
    private Button followBtn;

    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private String currentUserId;
    private String userID;

    private PostsByUserAdapter postsAdapter;
    private SwipeRefreshLayout swipeContainer;
    //private TextView likesCountersTextView;
    private ProfileManager profileManager;
    private PostManager postManager;
    private NotificationView notificationView;

    private DatabaseReference mFriendsRef;
    private DatabaseReference mMyDatabaseRef;
    private boolean mProcessClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);
        findViews();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent() != null) {
            userID = getIntent().getStringExtra(USER_ID_EXTRA_KEY);
            Log.d(TAG, "profileKey " + userID);
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        }

        if (userID != null && currentUserId != null) {
            mFriendsRef = FirebaseUtils.getFriendsRef().child(userID);
            mMyDatabaseRef = FirebaseUtils.getFriendsRef().child(currentUserId);
        }

        postManager = PostManager.getInstance(this);
        notificationView = new NotificationView(this);
        // Set up the login form.

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onRefreshAction();
            }
        });

        loadPostsList();
        supportPostponeEnterTransition();
    }

    private void findViews() {
        progressBar = findViewById(R.id.progressBar);
        imageView = findViewById(R.id.imageView);
        nameEditText = findViewById(R.id.nameEditText);
        postsCounterTextView = findViewById(R.id.postsCounterTextView);
        followBtn = findViewById(R.id.followBtn);
        //likesCountersTextView = findViewById(R.id.likesCountersTextView);
        postsLabelTextView = findViewById(R.id.postsLabelTextView);
        statefulLayout = findViewById(R.id.statefulLayout);
        swipeContainer = findViewById(R.id.swipeContainer);
        userFollowers = findViewById(R.id.userFollowers);
        userFollowings = findViewById(R.id.userFollowings);

        userFollowers.setOnClickListener(this);
        userFollowings.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == userFollowers) {
            if (userID != null) {
                Intent intent = new Intent(ProfileActivity.this, FollowersActivity.class);
                intent.putExtra(FollowersActivity.USER_ID_EXTRA_KEY, userID);
                startActivity(intent);
            }
        } else if (v == userFollowings) {
            if (userID != null) {
                Intent intent = new Intent(ProfileActivity.this, FollowingActivity.class);
                intent.putExtra(FollowingActivity.USER_ID_EXTRA_KEY, userID);
                startActivity(intent);
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if (userID != null) {
            if (userID.equalsIgnoreCase(currentUserId)) {
                followBtn.setVisibility(View.GONE);
            }
            checkFriendsStatus();
        }

        loadProfile();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
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
            postManager.createOrUpdatePostWithImage(Uri.parse(post.getImagePath()),
                    ProfileActivity.this, post);
            notificationView.setNotification(true, "Uploading Post");
        }

    }

    @Override
    public void onPostSaved(boolean success) {
        hideProgress();
        if (success) {
            notificationView.setNotification(false, "Uploading Post Successful");
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
            statefulLayout.showProgress();

            recyclerView = findViewById(R.id.recycler_view);
            postsAdapter = new PostsByUserAdapter(this, userID);
            postsAdapter.setCallBack(new PostsByUserAdapter.CallBack() {
                @Override
                public void onItemClick(final Post post, final View view) {
                    PostManager.getInstance(ProfileActivity.this).isPostExistSingleValue(post.getId(), new OnObjectExistListener<Post>() {
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
                    //likesCountersTextView.setVisibility(View.VISIBLE);
                    postsCounterTextView.setVisibility(View.VISIBLE);

                    if (postsCount > 0) {
                        postsLabelTextView.setVisibility(View.VISIBLE);
                        statefulLayout.showContent();
                    } else if (postsCount < 0) {
                        statefulLayout.setEmptyText("no posts to show");
                        statefulLayout.showEmpty();
                    }

                    swipeContainer.setRefreshing(false);
                }

                @Override
                public void onPostLoadingCanceled() {
                    swipeContainer.setRefreshing(false);
                    statefulLayout.setEmptyText("loading canceled");
                    statefulLayout.showEmpty();
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
        contentString.setSpan(new TextAppearanceSpan(this, R.style.TextAppearance_Follow),
                start, contentString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return contentString;
    }

    @SuppressLint("RestrictedApi")
    private void openPostDetailsActivity(Post post, View v) {
        Intent intent = new Intent(ProfileActivity.this, PostDetailsActivity.class);
        intent.putExtra(PostDetailsActivity.POST_ID_EXTRA_KEY, post.getId());
        intent.putExtra(PostDetailsActivity.AUTHOR_ANIMATION_NEEDED_EXTRA_KEY, true);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            View imageView = v.findViewById(R.id.postImageView);

            ActivityOptions options = ActivityOptions.
                    makeSceneTransitionAnimation(ProfileActivity.this,
                            new android.util.Pair<>(imageView, getString(R.string.post_image_transition_name))
                    );
            startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST, options.toBundle());
        } else {
            startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST);
        }
    }

    private void loadProfile() {
        profileManager = ProfileManager.getInstance(this);
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

    private void fillUIFields(Profile profile) {
        if (profile != null) {
            nameEditText.setText(profile.getUsername());

            if (profile.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(profile.getPhotoUrl())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .crossFade()
                        .error(R.drawable.ic_stub)
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
            } else {
                progressBar.setVisibility(View.GONE);
                imageView.setImageResource(R.drawable.ic_stub);
            }

            int likesCount = (int) profile.getLikesCount();
            String likesLabel = getResources().getQuantityString(R.plurals.likes_counter_format, likesCount, likesCount);
            //likesCountersTextView.setText(buildCounterSpannable(likesLabel, likesCount));

        }
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
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    private void checkFriendsStatus() {
        statefulLayout.showProgress();
        FirebaseUtils.getFriendsRef()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(userID).child(Consts.REQUEST_LIST_REF).hasChild(currentUserId)) {
                            followBtn.setText("Requested");
                        } else if (dataSnapshot.child(userID).child(Consts.FOLLOWERS_LIST_REF).hasChild(currentUserId)) {
                            followBtn.setText("Following");
                        } else {
                            followBtn.setText("Follow");
                        }

                        if (dataSnapshot.child(userID).child(Consts.FOLLOWERS_LIST_REF).hasChild(currentUserId)) {
                            followBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    openRemoveFollowing();
                                }
                            });
                        } else {
                            followBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    sendFollowRequest();
                                }
                            });
                        }

                        long totalFollowers = dataSnapshot.child(userID).child(Consts.FOLLOWERS_LIST_REF).getChildrenCount();
                        long totalFollowings = dataSnapshot.child(userID).child(Consts.FOLLOWING_LIST_REF).getChildrenCount();

                        String followers = getResources().getQuantityString(R.plurals.user_follower_format,
                                (int) totalFollowers, totalFollowers);
                        userFollowers.setText(buildCounterSpannable(followers, (int) totalFollowers));

                        String following = getResources().getQuantityString(R.plurals.user_following_format, (int) totalFollowings,
                                (int) totalFollowings);
                        userFollowings.setText(buildCounterSpannable(following, (int) totalFollowings));

                        statefulLayout.showContent();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        switch (error.getCode()) {
                            case DatabaseError.PERMISSION_DENIED:
                                statefulLayout.setEmptyText("No Permission");
                                statefulLayout.showEmpty();
                                break;
                            default:
                                statefulLayout.showEmpty();
                                statefulLayout.setEmptyText("failed to load user profile...");
                        }
                    }
                });
    }


    private void sendFollowRequest() {
        mProcessClick = true;
        mFriendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mProcessClick) {
                    if (dataSnapshot.child(Consts.REQUEST_LIST_REF).hasChild(currentUserId)) {
                        mFriendsRef.child(Consts.REQUEST_LIST_REF).child(currentUserId)
                                .removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError == null) {
                                            Log.d(TAG, "request removed");
                                        } else {
                                            showError(databaseError);
                                        }
                                    }
                                });
                        mProcessClick = false;
                    } else {
                        UsersThread thread = new UsersThread();
                        thread.setId(currentUserId);
                        thread.setCreatedDate(Calendar.getInstance().getTimeInMillis());

                        mFriendsRef.child(Consts.REQUEST_LIST_REF).child(currentUserId)
                                .setValue(thread, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError == null) {
                                            Log.d(TAG, "request send");
                                        } else {
                                            showError(databaseError);
                                        }
                                    }
                                });
                        mProcessClick = false;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showError(databaseError);
            }
        });
    }


    private void openRemoveFollowing() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Remove Following?")
                .setNegativeButton(R.string.button_title_cancel, null)
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removeFollowing();
                    }
                });

        builder.create().show();
    }

    private void removeFollowing() {
        mProcessClick = true;
        mFriendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mProcessClick) {
                    if (dataSnapshot.child(Consts.FOLLOWERS_LIST_REF).hasChild(currentUserId)) {
                        mFriendsRef.child(Consts.FOLLOWERS_LIST_REF).child(currentUserId)
                                .removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError == null) {
                                            Log.d(TAG, "request removed");
                                        } else {
                                            showError(databaseError);
                                        }
                                    }
                                });
                        mProcessClick = false;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showError(databaseError);
            }
        });
    }


    private void showError(DatabaseError databaseError) {
        mProcessClick = false;
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
}
