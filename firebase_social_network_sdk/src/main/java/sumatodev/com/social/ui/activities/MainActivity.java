package sumatodev.com.social.ui.activities;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.List;

import sumatodev.com.social.R;
import sumatodev.com.social.adapters.PostsAdapter;
import sumatodev.com.social.adapters.SearchAdapter;
import sumatodev.com.social.controllers.LikeController;
import sumatodev.com.social.enums.PostStatus;
import sumatodev.com.social.enums.ProfileStatus;
import sumatodev.com.social.managers.DatabaseHelper;
import sumatodev.com.social.managers.PostManager;
import sumatodev.com.social.managers.ProfileManager;
import sumatodev.com.social.managers.listeners.OnDataChangedListener;
import sumatodev.com.social.managers.listeners.OnObjectExistListener;
import sumatodev.com.social.managers.listeners.OnPostCreatedListener;
import sumatodev.com.social.model.Post;
import sumatodev.com.social.model.UsersPublic;
import sumatodev.com.social.utils.AnimationUtils;
import sumatodev.com.social.utils.DataShare;
import sumatodev.com.social.utils.LogUtil;

public class MainActivity extends BaseActivity implements OnPostCreatedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private MaterialSearchView mSearchView;
    private PostsAdapter postsAdapter;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;

    private ProfileManager profileManager;
    private PostManager postManager;
    private SearchAdapter searchAdapter;
    private int counter;
    private TextView newPostsCounterTextView;
    private PostManager.PostCounterWatcher postCounterWatcher;
    private boolean counterAnimationInProgress = false;


    public static void start(Context context) {
        Intent starter = new Intent(context, MainActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        mSearchView = findViewById(R.id.search_view);
        setSupportActionBar(toolbar);

        profileManager = ProfileManager.getInstance(this);
        postManager = PostManager.getInstance(this);
        initContentView();

        postCounterWatcher = new PostManager.PostCounterWatcher() {
            @Override
            public void onPostCounterChanged(int newValue) {
                updateNewPostCounter();
            }
        };

        postManager.setPostCounterWatcher(postCounterWatcher);

//        setOnLikeAddedListener();
        initSearchBar();
    }

    private void initSearchBar() {
        mSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 0) {
                    postManager.getSearchList(MainActivity.this, newText.toLowerCase(), publicOnDataChangedListener());
                }
                return false;
            }
        });


        mSearchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UsersPublic usersPublic = searchAdapter.getItemByPosition(position);
                if (usersPublic.getId() != null && hasInternetConnection()) {
                    openProfileActivity(usersPublic.getId());
                }
            }
        });

    }

    OnDataChangedListener<UsersPublic> publicOnDataChangedListener() {
        return new OnDataChangedListener<UsersPublic>() {
            @Override
            public void onListChanged(List<UsersPublic> list) {
                if (!list.isEmpty()) {
                    searchAdapter = new SearchAdapter(MainActivity.this, list, true);
                    mSearchView.setAdapter(searchAdapter);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNewPostCounter();

    }


    private void setOnLikeAddedListener() {
        DatabaseHelper.getInstance(this).onNewLikeAddedListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                counter++;
                showSnackBar("You have " + counter + " new likes");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ProfileActivity.CREATE_POST_FROM_PROFILE_REQUEST:
                    if (data != null) {
                        Post post = (Post) data.getSerializableExtra(CreatePostActivity.POST_DATA_KEY);
                        if (post != null) {
                            createNewPost(post);
                        }
                    }
                    break;
                case CreatePostActivity.CREATE_NEW_POST_REQUEST:
                    if (data != null) {
                        Post post = (Post) data.getSerializableExtra(CreatePostActivity.POST_DATA_KEY);
                        if (post != null) {
                            createNewPost(post);
                        }
                    }
                    break;

                case PostDetailsActivity.UPDATE_POST_REQUEST:
                    if (data != null) {
                        PostStatus postStatus = (PostStatus) data.getSerializableExtra(PostDetailsActivity.POST_STATUS_EXTRA_KEY);
                        if (postStatus.equals(PostStatus.REMOVED)) {
                            postsAdapter.removeSelectedPost();
                            showFloatButtonRelatedSnackBar(R.string.message_post_was_removed);
                        } else if (postStatus.equals(PostStatus.UPDATED)) {
                            postsAdapter.updateSelectedPost();
                        }
                    }
                    break;
            }
        }
    }

    private void createNewPost(Post post) {

        postManager.createOrUpdatePostWithImage(this, MainActivity.this, post);

    }

    @Override
    public void onPostSaved(boolean success) {
        hideProgress();
        if (success) {
            refreshPostList();
            //showFloatButtonRelatedSnackBar(R.string.message_post_was_created);
            LogUtil.logDebug(TAG, "Post was created");
        } else {
            showSnackBar(R.string.error_fail_create_post);
            LogUtil.logDebug(TAG, "Failed to create a post");
        }
    }

    private void refreshPostList() {
        postsAdapter.loadFirstPage();
        if (postsAdapter.getItemCount() > 0) {
            recyclerView.scrollToPosition(0);
        }
    }

    private void initContentView() {
        if (recyclerView == null) {
            floatingActionButton = findViewById(R.id.addNewPostFab);

            if (floatingActionButton != null) {
                floatingActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (hasInternetConnection()) {
                            addPostClickAction();
                        } else {
                            showFloatButtonRelatedSnackBar(R.string.internet_connection_failed);
                        }
                    }
                });
            }

            newPostsCounterTextView = findViewById(R.id.newPostsCounterTextView);
            newPostsCounterTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshPostList();
                }
            });

            final ProgressBar progressBar = findViewById(R.id.progressBar);
            SwipeRefreshLayout swipeContainer = findViewById(R.id.swipeContainer);
            recyclerView = findViewById(R.id.recycler_view);
            postsAdapter = new PostsAdapter(this, swipeContainer);
            postsAdapter.setCallback(new PostsAdapter.Callback() {

                @Override
                public void onItemClick(int position, final View view) {
                    final Post post = postsAdapter.getItemByPosition(position);
                    PostManager.getInstance(MainActivity.this).isPostExistSingleValue(post.getId(),
                            new OnObjectExistListener<Post>() {
                                @Override
                                public void onDataChanged(boolean exist) {
                                    if (exist) {
                                        openPostDetailsActivity(post, view);
                                    } else {
                                        showFloatButtonRelatedSnackBar(R.string.error_post_was_removed);
                                    }
                                }
                            });
                }

                @Override
                public void onImageClick(int position, View view) {
                    final Post post = postsAdapter.getItemByPosition(position);
                    PostManager.getInstance(MainActivity.this).isPostExistSingleValue(post.getId(),
                            new OnObjectExistListener<Post>() {
                                @Override
                                public void onDataChanged(boolean exist) {
                                    if (exist) {
                                        openPostDetailsActivity(post, null);
                                    } else {
                                        showFloatButtonRelatedSnackBar(R.string.error_post_was_removed);
                                    }
                                }
                            });
                }

                @Override
                public void onListLoadingFinished() {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onAuthorClick(int position, View view) {
                    final Post post = postsAdapter.getItemByPosition(position);
                    if (checkInternetConnection()) {
                        ProfileStatus status = profileManager.checkProfile();
                        if (status.equals(ProfileStatus.PROFILE_CREATED)) {
                            openProfileActivity(post.getAuthorId(), view);

                        } else {
                            doAuthorization(status);
                        }
                    }
                }

                @Override
                public void onLikeClick(LikeController likeController, int position) {
                    Post post = postsAdapter.getItemByPosition(position);
                    likeController.handleLikeClickAction(MainActivity.this, post);
                }

                @Override
                public void onCanceled(String message) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }

            });

            LinearLayoutManager manager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(manager);
            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
            recyclerView.setAdapter(postsAdapter);
            postsAdapter.loadFirstPage();
            updateNewPostCounter();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    hideCounterView();
                    super.onScrolled(recyclerView, dx, dy);
                }
            });
        }
    }

    private void openShareIntent(Post post) {
        if (post.getImagePath() != null) {
            new DataShare(MainActivity.this, "Share Image").ShareAndLoadImage(post.getImagePath());
        }
    }


    private void hideCounterView() {
        if (!counterAnimationInProgress && newPostsCounterTextView.getVisibility() == View.VISIBLE) {
            counterAnimationInProgress = true;
            AlphaAnimation alphaAnimation = AnimationUtils.hideViewByAlpha(newPostsCounterTextView);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    counterAnimationInProgress = false;
                    newPostsCounterTextView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            alphaAnimation.start();
        }
    }

    private void showCounterView() {
        AnimationUtils.showViewByScaleAndVisibility(newPostsCounterTextView);
    }

    @SuppressLint("RestrictedApi")
    private void openPostDetailsActivity(Post post, View v) {
        Intent intent = new Intent(MainActivity.this, PostDetailsActivity.class);
        intent.putExtra(PostDetailsActivity.POST_ID_EXTRA_KEY, post.getId());

        startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST);
    }


    public void showFloatButtonRelatedSnackBar(int messageId) {
        showSnackBar(floatingActionButton, messageId);
    }

    private void addPostClickAction() {
        ProfileStatus profileStatus = profileManager.checkProfile();

        if (profileStatus.equals(ProfileStatus.PROFILE_CREATED)) {
            openCreatePostActivity();
        } else {
            doAuthorization(profileStatus);
        }
    }

    private void openCreatePostActivity() {
        Intent intent = new Intent(MainActivity.this, CreatePostActivity.class);
        startActivityForResult(intent, CreatePostActivity.CREATE_NEW_POST_REQUEST);
    }

    private void openProfileActivity(String userId) {
        openProfileActivity(userId, null);
    }

    @SuppressLint("RestrictedApi")
    private void openProfileActivity(String userId, View view) {


        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_ID_EXTRA_KEY, userId);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view != null) {

            View authorImageView = view.findViewById(R.id.authorImageView);

            ActivityOptions options = ActivityOptions.
                    makeSceneTransitionAnimation(MainActivity.this,
                            new android.util.Pair<>(authorImageView, getString(R.string.post_author_image_transition_name)));
            startActivityForResult(intent, ProfileActivity.CREATE_POST_FROM_PROFILE_REQUEST, options.toBundle());
        } else {
            startActivityForResult(intent, ProfileActivity.CREATE_POST_FROM_PROFILE_REQUEST);
        }

    }

    private void updateNewPostCounter() {
        Handler mainHandler = new Handler(this.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                int newPostsQuantity = postManager.getNewPostsCounter();

                if (newPostsCounterTextView != null) {
                    if (newPostsQuantity > 0) {
                        showCounterView();

                        String counterFormat = getResources().getQuantityString(R.plurals.new_posts_counter_format, newPostsQuantity, newPostsQuantity);
                        newPostsCounterTextView.setText(String.format(counterFormat, newPostsQuantity));
                    } else {
                        hideCounterView();
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        mSearchView.setMenuItem(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int i = item.getItemId();
        if (i == R.id.profile) {

            if (checkInternetConnection()) {
                ProfileStatus profileStatus = profileManager.checkProfile();
                if (profileStatus.equals(ProfileStatus.PROFILE_CREATED)) {
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    openProfileActivity(userId);
                } else {
                    doAuthorization(profileStatus);
                }
            }
            return true;
        } else if (i == R.id.users) {
            if (checkInternetConnection()) {
                startActivity(new Intent(MainActivity.this, UsersActivity.class));
            }
            return true;
        } else if (i == R.id.action_invite) {
            sendInvitation();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void sendInvitation() {

    }

    @Override
    public void onBackPressed() {
        if (mSearchView.isSearchOpen()) {
            mSearchView.closeSearch();
        } else {
            super.onBackPressed();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        postManager.closeListeners(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        postManager.closeListeners(this);
    }
}
