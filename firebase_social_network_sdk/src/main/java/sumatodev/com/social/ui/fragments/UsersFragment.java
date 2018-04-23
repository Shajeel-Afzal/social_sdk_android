package sumatodev.com.social.ui.fragments;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

import cz.kinst.jakub.view.SimpleStatefulLayout;
import sumatodev.com.social.R;
import sumatodev.com.social.adapters.PublicListAdapter;
import sumatodev.com.social.adapters.holders.UsersHolder;
import sumatodev.com.social.managers.UsersManager;
import sumatodev.com.social.managers.listeners.OnDataChangedListener;
import sumatodev.com.social.model.Friends;
import sumatodev.com.social.model.UsersPublic;

/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends BaseFragment {


    private static final String TAG = UsersFragment.class.getSimpleName();
    SimpleStatefulLayout mStatefulLayout;
    RecyclerView recycleView;
    public PublicListAdapter listAdapter;

    public UsersFragment() {
        // Required empty public constructor
    }

    public static UsersFragment newInstance() {

        Bundle args = new Bundle();
        UsersFragment fragment = new UsersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_following, container, false);
        findViews(view);
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

        if (hasInternetConnection()) {
            loadUsers();
        }
    }

    private void loadUsers() {
        mStatefulLayout.showProgress();

        listAdapter = new PublicListAdapter();
        listAdapter.setCallBack(new PublicListAdapter.CallBack() {
            @Override
            public void onItemClick(int position, View view) {
                UsersPublic usersPublic = listAdapter.getItemByPosition(position);
                openProfileActivity(usersPublic.getId());
            }

            @Override
            public void onListChanged(int items) {
                if (items == 0) {
                    mStatefulLayout.showEmpty();
                    mStatefulLayout.setEmptyText("No Users");
                } else {
                    mStatefulLayout.showContent();
                }
            }
        });

        recycleView.setAdapter(listAdapter);
        UsersManager.getInstance(getActivity()).getAllUsersList(getActivity(), publicOnDataChangedListener());

    }

    private OnDataChangedListener<UsersPublic> publicOnDataChangedListener() {
        return new OnDataChangedListener<UsersPublic>() {
            @Override
            public void onListChanged(List<UsersPublic> list) {
                listAdapter.setList(list);
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

        recycleView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recycleView.setLayoutManager(layoutManager);
    }


}
