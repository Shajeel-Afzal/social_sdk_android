package sumatodev.com.social.ui.activities;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import cz.kinst.jakub.view.SimpleStatefulLayout;
import sumatodev.com.social.R;
import sumatodev.com.social.adapters.PostsByUserAdapter;
import sumatodev.com.social.managers.ProfileManager;
import sumatodev.com.social.managers.listeners.OnObjectChangedListener;

public class Profile extends BaseActivity implements View.OnClickListener {

    private static final String TAG = Profile.class.getSimpleName();
    public static final int CREATE_POST_FROM_PROFILE_REQUEST = 22;
    public static final String USER_ID_EXTRA_KEY = "ProfileActivity.USER_ID_EXTRA_KEY";

    // UI references.
    private SimpleStatefulLayout statefulView;
    private LinearLayout profileDetail;
    private ImageView userImage;
    private TextView userName;
    private TextView userLocation;
    private TextView userStatus;
    private LinearLayout btnsLayout;
    private Button followBtn;
    private Button messageBtn;

    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private String currentUserId;
    private String userID;

    private PostsByUserAdapter postsAdapter;
    private SwipeRefreshLayout swipeContainer;
    private TextView likesCountersTextView;
    private ProfileManager profileManager;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);
        findViews();

        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        userID = getIntent().getStringExtra(USER_ID_EXTRA_KEY);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        }


        // loadPostsList();
        supportPostponeEnterTransition();
    }

    private void findViews() {

        statefulView = findViewById(R.id.stateful_view);
        profileDetail = findViewById(R.id.profileDetail);
        userImage = findViewById(R.id.userImage);
        userName = findViewById(R.id.userName);
        userLocation = findViewById(R.id.userLocation);
        userStatus = findViewById(R.id.userStatus);
        btnsLayout = findViewById(R.id.btns_layout);
        followBtn = findViewById(R.id.followBtn);
        messageBtn = findViewById(R.id.messageBtn);
        progressBar = findViewById(R.id.progressBar);

        followBtn.setOnClickListener(this);
        messageBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == followBtn) {
            // Handle clicks for followBtn
        } else if (v == messageBtn) {
            // Handle clicks for messageBtn
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadProfile();
    }

    @Override
    protected void onStop() {
        super.onStop();
        profileManager.closeListeners(this);
    }

    private void loadProfile() {
        profileManager = ProfileManager.getInstance(this);
        profileManager.getProfileValue(Profile.this, userID, createOnProfileChangedListener());
    }

    private OnObjectChangedListener<sumatodev.com.social.model.Profile> createOnProfileChangedListener() {
        return new OnObjectChangedListener<sumatodev.com.social.model.Profile>() {
            @Override
            public void onObjectChanged(sumatodev.com.social.model.Profile obj) {
                fillUIFields(obj);
            }
        };
    }

    private void fillUIFields(sumatodev.com.social.model.Profile profile) {
        if (profile != null) {
            userName.setText(profile.getUsername());

            if (profile.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(profile.getPhotoUrl())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .crossFade()
                        .error(R.drawable.ic_stub)
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                scheduleStartPostponedTransition(userImage);
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                scheduleStartPostponedTransition(userImage);
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(userImage);
            } else {
                progressBar.setVisibility(View.GONE);
                userImage.setImageResource(R.drawable.ic_stub);
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
}

