package sumatodev.com.social.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import sumatodev.com.social.R;
import sumatodev.com.social.adapters.UsersPostAdapter;
import sumatodev.com.social.enums.PostStatus;
import sumatodev.com.social.enums.ProfileStatus;
import sumatodev.com.social.managers.PostManager;
import sumatodev.com.social.managers.ProfileManager;
import sumatodev.com.social.managers.listeners.OnObjectExistListener;
import sumatodev.com.social.model.Post;
import sumatodev.com.social.ui.activities.LinkActivity;
import sumatodev.com.social.ui.activities.PostDetailsActivity;

public class UsersPostFragment extends BaseFragment {


    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private ProfileManager profileManager;
    private UsersPostAdapter usersPostAdapter;

    public static UsersPostFragment newInstance() {
        Bundle args = new Bundle();
        UsersPostFragment fragment = new UsersPostFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileManager = ProfileManager.getInstance(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users_post, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progressBar);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        initContentView();
    }

    private void initContentView() {

        usersPostAdapter = new UsersPostAdapter(getActivity());
        usersPostAdapter.setCallback(new UsersPostAdapter.Callback() {
            @Override
            public void onItemClick(final Post post, final View view) {
                PostManager.getInstance(getActivity()).isPostExistSingleValue(post.getId(), new OnObjectExistListener<Post>() {
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
            public void onListLoadingFinished() {
                progressBar.setVisibility(View.GONE);
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
            public void onCanceled(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onLinkClick(String linkUrl) {
                if (!linkUrl.isEmpty()) {
                    openUrlActivity(linkUrl);
                }
            }

            @Override
            public void onPostsListChanged(int postsCount) {

            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.setAdapter(usersPostAdapter);
        usersPostAdapter.loadFirstPage();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

    }

    @SuppressLint("RestrictedApi")
    private void openPostDetailsActivity(Post post, View v) {
        Intent intent = new Intent(getActivity(), PostDetailsActivity.class);
        intent.putExtra(PostDetailsActivity.POST_ID_EXTRA_KEY, post.getId());

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            View imageView = v.findViewById(R.id.postImageView);
            View authorImageView = v.findViewById(R.id.authorImageView);

            if (imageView != null && authorImageView != null) {

                ActivityOptions options = ActivityOptions.
                        makeSceneTransitionAnimation(getActivity(),
                                new android.util.Pair<>(authorImageView, getString(R.string.post_author_image_transition_name)),
                                new android.util.Pair<>(authorImageView, getString(R.string.post_author_image_transition_name))
                        );
                startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST, options.toBundle());
            } else if (imageView != null) {
                ActivityOptions options = ActivityOptions.
                        makeSceneTransitionAnimation(getActivity(),
                                new android.util.Pair<>(imageView, getString(R.string.post_image_transition_name))
                        );
                startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST, options.toBundle());
            } else if (authorImageView != null) {
                ActivityOptions options = ActivityOptions.
                        makeSceneTransitionAnimation(getActivity(),
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

    public void openUrlActivity(String linkUrl) {
        Intent intent = new Intent(getActivity(), LinkActivity.class);
        intent.putExtra(LinkActivity.URL_REF, linkUrl);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PostDetailsActivity.UPDATE_POST_REQUEST:
                    if (data != null) {
                        PostStatus postStatus = (PostStatus) data.getSerializableExtra(PostDetailsActivity.POST_STATUS_EXTRA_KEY);
                        if (postStatus.equals(PostStatus.REMOVED)) {
                            usersPostAdapter.removeSelectedPost();
                            showSnackBar(R.string.message_post_was_removed);
                        } else if (postStatus.equals(PostStatus.UPDATED)) {
                            usersPostAdapter.updateSelectedPost();
                        }
                    }
                    break;
            }
        }
    }
}
