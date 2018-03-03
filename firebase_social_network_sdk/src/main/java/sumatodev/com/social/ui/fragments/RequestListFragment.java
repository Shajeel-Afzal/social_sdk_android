package sumatodev.com.social.ui.fragments;


import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
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
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import cz.kinst.jakub.view.SimpleStatefulLayout;
import sumatodev.com.social.R;
import sumatodev.com.social.adapters.RequestListAdapter;
import sumatodev.com.social.enums.Consts;
import sumatodev.com.social.listeners.OnRequestItemListener;
import sumatodev.com.social.managers.FirebaseUtils;
import sumatodev.com.social.model.UsersThread;
import sumatodev.com.social.ui.activities.ProfileActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestListFragment extends Fragment implements OnRequestItemListener {

    private static final String TAG = RequestListFragment.class.getSimpleName();
    private SimpleStatefulLayout mStatefulLayout;
    private RecyclerView recycleView;

    private String currentUid;
    private LinearLayoutManager layoutManager;
    private boolean mProcessClick = false;
    private RequestListAdapter listAdapter;
    private DatabaseReference mMyDatabaseRef;

    public RequestListFragment() {
        // Required empty public constructor
    }

    public static RequestListFragment newInstance() {

        Bundle args = new Bundle();
        RequestListFragment fragment = new RequestListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUid = firebaseUser.getUid();
            mMyDatabaseRef = FirebaseUtils.getFriendsRef().child(currentUid);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_request_list, container, false);
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
        if (currentUid != null) {
            loadRequestList();
        }
    }

    private void loadRequestList() {
        mStatefulLayout.showProgress();

        Query query = FirebaseUtils.getFriendsRef().child(currentUid).child(Consts.REQUEST_LIST_REF);

        FirebaseRecyclerOptions<UsersThread> options = new FirebaseRecyclerOptions.Builder<UsersThread>()
                .setQuery(query, UsersThread.class)
                .build();

        listAdapter = new RequestListAdapter(options);
        listAdapter.setOnRequestItemListener(this);
        recycleView.setAdapter(listAdapter);

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
    public void onItemClick(View view, String userKey) {
        openProfileActivity(userKey, view);
    }

    @Override
    public void onAcceptClick(final String userKey) {

        mProcessClick = true;
        FirebaseUtils.getFriendsRef()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (mProcessClick) {
                            if (dataSnapshot.child(currentUid).child(Consts.REQUEST_LIST_REF).hasChild(userKey)) {

                                UsersThread userModel = new UsersThread();

                                userModel.setId(userKey);
                                userModel.setCreatedDate(Calendar.getInstance().getTimeInMillis());


                                UsersThread forFollowing = new UsersThread();
                                forFollowing.setId(currentUid);
                                forFollowing.setCreatedDate(Calendar.getInstance().getTimeInMillis());


                                Map userMap = new ObjectMapper().convertValue(userModel, Map.class);
                                Map followingMap  = new ObjectMapper().convertValue(forFollowing, Map.class);

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put(currentUid + "/" + Consts.REQUEST_LIST_REF + "/" + userKey, null);
                                hashMap.put(currentUid + "/" + Consts.FOLLOWERS_LIST_REF + "/" + userKey, userMap);
                                hashMap.put(userKey + "/" + Consts.FOLLOWING_LIST_REF + "/" + currentUid, followingMap);

                                FirebaseUtils.getFriendsRef()
                                        .updateChildren(hashMap, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                if (databaseError == null) {
                                                    Log.d(TAG, "request accepted");
                                                    Toast.makeText(getContext(), "request accepted", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onRejectClick(final String userKey) {

        mProcessClick = true;
        mMyDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mProcessClick) {
                    if (dataSnapshot.child(Consts.REQUEST_LIST_REF).hasChild(userKey)) {
                        mMyDatabaseRef.child(Consts.REQUEST_LIST_REF).child(userKey)
                                .removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError == null) {
                                            Log.d(TAG, "request deleted");
                                            Toast.makeText(getContext(), "request rejected", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onStart() {
        super.onStart();
        listAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (listAdapter != null) {
            listAdapter.stopListening();
        }
    }

    private void showError(DatabaseError databaseError) {
        mProcessClick = false;
        switch (databaseError.getCode()) {
            case DatabaseError.PERMISSION_DENIED:
                Log.d("TAG", "permission denied");
                Toast.makeText(getActivity(), "permission denied", Toast.LENGTH_SHORT).show();
                break;
            case DatabaseError.NETWORK_ERROR:
                Log.d("TAG", "network error");
                Toast.makeText(getActivity(), "network error", Toast.LENGTH_SHORT).show();
                break;
            default:
                Log.d("TAG", "request failed");
                Toast.makeText(getActivity(), "request failed", Toast.LENGTH_SHORT).show();
        }
    }

}
