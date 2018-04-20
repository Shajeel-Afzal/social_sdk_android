package sumatodev.com.social.ui.fragments;


import android.os.Bundle;
import android.os.Handler;
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

import java.util.List;

import cz.kinst.jakub.view.SimpleStatefulLayout;
import sumatodev.com.social.R;
import sumatodev.com.social.adapters.FollowAdapter;
import sumatodev.com.social.enums.Consts;
import sumatodev.com.social.listeners.OnRequestItemListener;
import sumatodev.com.social.managers.UsersManager;
import sumatodev.com.social.managers.listeners.OnDataChangedListener;
import sumatodev.com.social.model.Friends;

public class FollowFragment extends BaseFragment implements OnRequestItemListener {

    private static final int TIME_OUT_LOADING_LIST = 3000;
    private static final String TAG = FollowFragment.class.getSimpleName();
    public static final String REF_TYPE = "REF_TYPE";
    private SimpleStatefulLayout mStatefulLayout;
    private RecyclerView recycleView;

    private String requestType;
    private String userKey;
    private LinearLayoutManager layoutManager;
    private FollowAdapter frollowAdapter;
    private ActionBar actionBar;
    private boolean loadingList = false;


    public FollowFragment() {
    }

    public static FollowFragment newInstance(String userId, String type) {

        Bundle args = new Bundle();
        args.putString(Consts.USER_KEY, userId);
        args.putString(REF_TYPE, type);
        FollowFragment fragment = new FollowFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userKey = getArguments().getString(Consts.USER_KEY);
            requestType = getArguments().getString(REF_TYPE);
        }
        actionBar = getActionBar();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_following, container, false);
        findViews(view);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
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
        if (userKey != null && requestType != null) {
            if (hasInternetConnection()) {
                loadFollowersList();
            } else {
                mStatefulLayout.showOffline();
            }
        }
    }

    private void loadFollowersList() {
        mStatefulLayout.showProgress();

        frollowAdapter = new FollowAdapter();
        frollowAdapter.setCallBack(new FollowAdapter.CallBack() {
            @Override
            public void onItemClick(String userKey, View view) {
                openProfile(userKey, view);
            }

            @Override
            public void onListChanged(int items) {
                Log.d(TAG, "List Size: " + items);
            }
        });

        recycleView.setAdapter(frollowAdapter);

        if (requestType.equalsIgnoreCase(Consts.FOLLOWING_LIST_REF)) {
            if (actionBar != null) {
                actionBar.setTitle("Followings");
            }
            UsersManager.getInstance(getActivity()).getFriendsList(getActivity(), userKey,
                    Consts.FOLLOWING_LIST_REF, friendsOnDataChangedListener());

        } else if (requestType.equalsIgnoreCase(Consts.FOLLOWERS_LIST_REF)) {
            if (actionBar != null) {
                actionBar.setTitle("Followers");
            }

            UsersManager.getInstance(getActivity()).getFriendsList(getActivity(), userKey,
                    Consts.FOLLOWERS_LIST_REF, friendsOnDataChangedListener());
        }
    }


    OnDataChangedListener<Friends> friendsOnDataChangedListener() {

        return new OnDataChangedListener<Friends>() {
            @Override
            public void onListChanged(List<Friends> list) {
                frollowAdapter.setList(list);
                mStatefulLayout.showContent();

            }

            @Override
            public void inEmpty(Boolean empty, String error) {
                if (empty) {
                    mStatefulLayout.showEmpty();
                } else {
                    mStatefulLayout.showEmpty();
                    mStatefulLayout.setEmptyText(error);
                }
            }

        };
    }


    private void setupLinearLayout() {
        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recycleView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recycleView.setLayoutManager(layoutManager);
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
