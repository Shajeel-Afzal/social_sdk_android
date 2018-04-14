package sumatodev.com.social.ui.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import sumatodev.com.social.model.Friends;

/**
 * A simple {@link Fragment} subclass.
 */
public class FollowingFragment extends BaseFragment implements OnRequestItemListener {


    private static final String TAG = FollowingFragment.class.getSimpleName();
    private SimpleStatefulLayout mStatefulLayout;
    private RecyclerView recycleView;

    private String userKey;
    private LinearLayoutManager layoutManager;
    private FollowAdapter followAdapter;
    private ActionBar actionBar;


    public FollowingFragment() {
        // Required empty public constructor
    }

    public static FollowingFragment newInstance(String userId) {

        Bundle args = new Bundle();
        args.putString(Consts.USER_KEY, userId);
        FollowingFragment fragment = new FollowingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userKey = getArguments().getString(Consts.USER_KEY);
            Log.d(TAG, "followingKey: " + userKey);
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
            actionBar.setTitle("Following");
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
            loadFollowingList();
        }
    }

    private void loadFollowingList() {
        mStatefulLayout.showProgress();


        Query query = FirebaseUtils.getFriendsRef().child(userKey).child(Consts.FOLLOWING_LIST_REF);

        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(query, Friends.class)
                .build();

        followAdapter = new FollowAdapter(options);
        followAdapter.setOnRequestItemListener(this);
        recycleView.setAdapter(followAdapter);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
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
        recycleView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recycleView.setLayoutManager(layoutManager);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (checkInternetConnection()) {
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
        openProfile(userKey, view);
    }

    @Override
    public void onAcceptClick(String userKey) {
    }

    @Override
    public void onRejectClick(String userKey) {
    }


}
