package sumatodev.com.social.ui.fragments;


import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import cz.kinst.jakub.view.SimpleStatefulLayout;
import sumatodev.com.social.R;
import sumatodev.com.social.adapters.FollowAdapter;
import sumatodev.com.social.enums.Consts;
import sumatodev.com.social.listeners.OnRequestItemListener;
import sumatodev.com.social.managers.FirebaseUtils;
import sumatodev.com.social.model.Follow;
import sumatodev.com.social.ui.activities.ProfileActivity;

public class FollowersFragment extends BaseFragment implements OnRequestItemListener {

    private static final String TAG = FollowersFragment.class.getSimpleName();
    private SimpleStatefulLayout mStatefulLayout;
    private RecyclerView recycleView;

    private String userKey;
    private LinearLayoutManager layoutManager;
    private FollowAdapter followAdapter;
    private ActionBar actionBar;

    public FollowersFragment() {
    }

    public static FollowersFragment newInstance(String userId) {

        Bundle args = new Bundle();
        args.putString(Consts.USER_KEY, userId);
        FollowersFragment fragment = new FollowersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userKey = getArguments().getString(Consts.USER_KEY);
            Log.d(TAG, "followersKey" + userKey);
        }
        actionBar = getActionBar();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request_list, container, false);
        findViews(view);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Followers");
        }
        return view;
    }

    private void findViews(View view) {
        mStatefulLayout = view.findViewById(R.id.stateful_view);
        recycleView = view.findViewById(R.id.recycleView);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupLinearLayout();
        if (userKey != null && checkInternetConnection()) {
            loadFollowersList();
        }
    }

    private void loadFollowersList() {
        mStatefulLayout.showProgress();


        Query query = FirebaseUtils.getFriendsRef().child(userKey).child(Consts.FOLLOWERS_LIST_REF);

        FirebaseRecyclerOptions<Follow> options = new FirebaseRecyclerOptions.Builder<Follow>()
                .setQuery(query, Follow.class)
                .build();

        followAdapter = new FollowAdapter(options);
        followAdapter.setOnRequestItemListener(this);
        recycleView.setAdapter(followAdapter);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    mStatefulLayout.setEmptyText("empty");
                    mStatefulLayout.showEmpty();
                } else {
                    mStatefulLayout.showContent();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                switch (error.getCode()) {
                    case DatabaseError.PERMISSION_DENIED:
                        mStatefulLayout.setEmptyText("No Permission");
                        mStatefulLayout.showEmpty();
                        break;
                    default:
                        mStatefulLayout.showEmpty();
                        mStatefulLayout.setEmptyText("something went wrong...");
                }
            }
        });
    }

    private void setupLinearLayout() {
        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recycleView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recycleView.setLayoutManager(layoutManager);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (checkInternetConnection()){
            followAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (followAdapter != null) {
            followAdapter.stopListening();
        }
    }

    @Override
    public void onItemClick(View view, String userKey) {
        openProfileActivity(userKey, view);
    }

    @Override
    public void onAcceptClick(String userKey) {
    }

    @Override
    public void onRejectClick(String userKey) {
    }

    private void openProfileActivity(String userId, View view) {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_ID_EXTRA_KEY, userId);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view != null) {

            ActivityOptions options = ActivityOptions.
                    makeSceneTransitionAnimation(getActivity(),
                            new android.util.Pair<>(view, getString(R.string.post_author_image_transition_name)));
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }
}
