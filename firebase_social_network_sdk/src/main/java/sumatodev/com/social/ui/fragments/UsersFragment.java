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

import cz.kinst.jakub.view.SimpleStatefulLayout;
import sumatodev.com.social.R;
import sumatodev.com.social.adapters.holders.UsersHolder;
import sumatodev.com.social.managers.FirebaseUtils;
import sumatodev.com.social.model.UsersPublic;

/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends BaseFragment {


    private static final String TAG = UsersFragment.class.getSimpleName();
    SimpleStatefulLayout mStatefulLayout;
    RecyclerView recycleView;

    private FirebaseRecyclerAdapter<UsersPublic, UsersHolder> adapter;

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


        Query dataQuery = FirebaseUtils.getUserPublicInfoRef();
        FirebaseRecyclerOptions<UsersPublic> options = new FirebaseRecyclerOptions.Builder<UsersPublic>()
                .setQuery(dataQuery, UsersPublic.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<UsersPublic, UsersHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final UsersHolder holder, @SuppressLint("RecyclerView") final int position,
                                            @NonNull final UsersPublic model) {

                holder.setData(model);
                Log.d(TAG, "userKey " + model.getId());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openProfile(model.getId(), v);
                    }
                });
            }

            @Override
            public UsersHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new UsersHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.follow_request_list, parent, false));
            }

            @Override
            public int getItemCount() {
                return super.getItemCount();
            }
        };

        recycleView.setAdapter(adapter);
    }

    private void setupLinearLayout() {

        recycleView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recycleView.setLayoutManager(layoutManager);
    }


    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
