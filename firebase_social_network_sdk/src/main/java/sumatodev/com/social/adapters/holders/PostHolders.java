package sumatodev.com.social.adapters.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import sumatodev.com.social.Constants;
import sumatodev.com.social.R;
import sumatodev.com.social.adapters.PostsAdapter;
import sumatodev.com.social.controllers.LikeController;
import sumatodev.com.social.managers.PostManager;
import sumatodev.com.social.managers.ProfileManager;
import sumatodev.com.social.managers.listeners.OnObjectChangedListener;
import sumatodev.com.social.managers.listeners.OnObjectExistListener;
import sumatodev.com.social.model.Like;
import sumatodev.com.social.model.Post;
import sumatodev.com.social.model.Profile;
import sumatodev.com.social.utils.FormatterUtil;
import sumatodev.com.social.utils.Utils;

/**
 * Created by Ali on 03/04/2018.
 */

public class PostHolders {
    public static final String TAG = PostHolders.class.getSimpleName();


    public static class TextHolder extends RecyclerView.ViewHolder {

        private Context context;
        private TextView titleTextView;
        private TextView likeCounterTextView;
        private ImageView likesImageView;
        private TextView commentsCountTextView;
        private TextView watcherCounterTextView;
        private TextView dateTextView;
        private ImageView authorImageView;
        private TextView authorName;
        private ViewGroup likeViewGroup;
        private ImageView postShare;

        private ProfileManager profileManager;
        private PostManager postManager;
        private PostsAdapter.Callback callback;
        private LikeController likeController;

        public TextHolder(View view, final PostsAdapter.Callback callback) {
            super(view);
            this.context = view.getContext();
            this.callback = callback;

            authorName = view.findViewById(R.id.author_mame_tv);
            likeCounterTextView = view.findViewById(R.id.likeCounterTextView);
            likesImageView = view.findViewById(R.id.likesImageView);
            commentsCountTextView = view.findViewById(R.id.commentsCountTextView);
            watcherCounterTextView = view.findViewById(R.id.watcherCounterTextView);
            dateTextView = view.findViewById(R.id.dateTextView);
            titleTextView = view.findViewById(R.id.titleTextView);
            authorImageView = view.findViewById(R.id.authorImageView);
            likeViewGroup = view.findViewById(R.id.likesContainer);
            postShare = view.findViewById(R.id.postShare);

            profileManager = ProfileManager.getInstance(context.getApplicationContext());
            postManager = PostManager.getInstance(context.getApplicationContext());

            if (callback != null) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            callback.onItemClick(getAdapterPosition(), v);
                        }
                    }
                });

                authorImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            callback.onAuthorClick(getAdapterPosition(), v);
                        }
                    }
                });

                likeViewGroup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            callback.onLikeClick(likeController, getAdapterPosition());
                        }
                    }
                });
            }
        }


        public void bindData(Post post) {

            likeController = new LikeController(context, post, likeCounterTextView, likesImageView, true);

            if (post.getTitle() != null) {

                String title = removeNewLinesDividers(post.getTitle());
                titleTextView.setText(title);

            }

            likeCounterTextView.setText(String.valueOf(post.getLikesCount()));
            commentsCountTextView.setText(String.valueOf(post.getCommentsCount()));
            watcherCounterTextView.setText(String.valueOf(post.getWatchersCount()));

            CharSequence date = FormatterUtil.getRelativeTimeSpanStringShort(context, post.getCreatedDate());
            dateTextView.setText(date);

            if (post.getAuthorId() != null) {
                profileManager.getProfileSingleValue(post.getAuthorId(),
                        createProfileChangeListener(context, authorImageView, authorName));
            }

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                postManager.hasCurrentUserLikeSingleValue(post.getId(), firebaseUser.getUid(),
                        createOnLikeObjectExistListener());
            }

        }

        private OnObjectExistListener<Like> createOnLikeObjectExistListener() {
            return new OnObjectExistListener<Like>() {
                @Override
                public void onDataChanged(boolean exist) {
                    likeController.initLike(exist);
                }
            };
        }
    }

    public static class ImageHolder extends RecyclerView.ViewHolder {

        private Context context;
        private ImageView postImageView;
        private TextView likeCounterTextView;
        private ImageView likesImageView;
        private TextView commentsCountTextView;
        private TextView watcherCounterTextView;
        private TextView dateTextView;
        private ImageView authorImageView;
        private ViewGroup likeViewGroup;
        private FrameLayout imageLayout;
        private TextView authorName;
        private ImageView postShare;
        private ProgressBar progressBar;

        private ProfileManager profileManager;
        private PostManager postManager;
        private PostsAdapter.Callback callback;
        private LikeController likeController;

        public ImageHolder(View view, final PostsAdapter.Callback callback) {
            super(view);
            this.context = view.getContext();
            this.callback = callback;

            authorName = view.findViewById(R.id.author_mame_tv);
            postImageView = view.findViewById(R.id.postImageView);
            likeCounterTextView = view.findViewById(R.id.likeCounterTextView);
            likesImageView = view.findViewById(R.id.likesImageView);
            commentsCountTextView = view.findViewById(R.id.commentsCountTextView);
            watcherCounterTextView = view.findViewById(R.id.watcherCounterTextView);
            dateTextView = view.findViewById(R.id.dateTextView);
            authorImageView = view.findViewById(R.id.authorImageView);
            likeViewGroup = view.findViewById(R.id.likesContainer);
            imageLayout = view.findViewById(R.id.imageLayout);
            postShare = view.findViewById(R.id.postShare);
            progressBar = view.findViewById(R.id.progressBar);

            profileManager = ProfileManager.getInstance(context.getApplicationContext());
            postManager = PostManager.getInstance(context.getApplicationContext());


            if (callback != null) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            callback.onItemClick(getAdapterPosition(), v);
                        }
                    }
                });

                postImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            callback.onImageClick(getAdapterPosition(), v);
                        }
                    }
                });

                authorImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            callback.onAuthorClick(getAdapterPosition(), v);
                        }
                    }
                });

                likeViewGroup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            callback.onLikeClick(likeController, getAdapterPosition());
                        }
                    }
                });
            }
        }

        public void bindData(Post post) {

            likeController = new LikeController(context, post, likeCounterTextView, likesImageView, true);

            likeCounterTextView.setText(String.valueOf(post.getLikesCount()));
            commentsCountTextView.setText(String.valueOf(post.getCommentsCount()));
            watcherCounterTextView.setText(String.valueOf(post.getWatchersCount()));

            CharSequence date = FormatterUtil.getRelativeTimeSpanStringShort(context, post.getCreatedDate());
            dateTextView.setText(date);

            if (post.getImagePath() != null) {

                String imageUrl = post.getImagePath();
                int width = Utils.getDisplayWidth(context);
                int height = (int) context.getResources().getDimension(R.dimen.post_detail_image_height);

                // Displayed and saved to cache image, as needs for post detail.
                Glide.with(context)
                        .load(imageUrl)
                        .centerCrop()
                        .override(width, height)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .crossFade()
                        .error(R.drawable.ic_stub)
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(postImageView);
            }

            if (post.getAuthorId() != null) {
                profileManager.getProfileSingleValue(post.getAuthorId(),
                        createProfileChangeListener(context, authorImageView, authorName));
            }

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                postManager.hasCurrentUserLikeSingleValue(post.getId(), firebaseUser.getUid(), createOnLikeObjectExistListener());
            }
        }

        private OnObjectExistListener<Like> createOnLikeObjectExistListener() {
            return new OnObjectExistListener<Like>() {
                @Override
                public void onDataChanged(boolean exist) {
                    likeController.initLike(exist);
                }
            };
        }
    }


    public static class TextImageHolder extends RecyclerView.ViewHolder {

        private Context context;
        private ImageView postImageView;
        private TextView titleTextView;
        private TextView likeCounterTextView;
        private ImageView likesImageView;
        private TextView commentsCountTextView;
        private TextView watcherCounterTextView;
        private TextView dateTextView;
        private ImageView authorImageView;
        private ViewGroup likeViewGroup;
        private FrameLayout imageLayout;
        private TextView authorName;
        private ImageView postShare;
        private ProgressBar progressBar;

        private ProfileManager profileManager;
        private PostManager postManager;
        private PostsAdapter.Callback callback;
        private LikeController likeController;

        public TextImageHolder(View view, final PostsAdapter.Callback callback) {
            super(view);
            this.context = view.getContext();
            this.callback = callback;

            authorName = view.findViewById(R.id.author_mame_tv);
            postImageView = view.findViewById(R.id.postImageView);
            likeCounterTextView = view.findViewById(R.id.likeCounterTextView);
            likesImageView = view.findViewById(R.id.likesImageView);
            commentsCountTextView = view.findViewById(R.id.commentsCountTextView);
            watcherCounterTextView = view.findViewById(R.id.watcherCounterTextView);
            dateTextView = view.findViewById(R.id.dateTextView);
            titleTextView = view.findViewById(R.id.titleTextView);
            authorImageView = view.findViewById(R.id.authorImageView);
            likeViewGroup = view.findViewById(R.id.likesContainer);
            imageLayout = view.findViewById(R.id.imageLayout);
            postShare = view.findViewById(R.id.postShare);
            progressBar = view.findViewById(R.id.progressBar);

            profileManager = ProfileManager.getInstance(context.getApplicationContext());
            postManager = PostManager.getInstance(context.getApplicationContext());


            if (callback != null) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            callback.onItemClick(getAdapterPosition(), v);
                        }
                    }
                });

                postImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            callback.onImageClick(getAdapterPosition(), v);
                        }
                    }
                });

                authorImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            callback.onAuthorClick(getAdapterPosition(), v);
                        }
                    }
                });

                likeViewGroup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            callback.onLikeClick(likeController, getAdapterPosition());
                        }
                    }
                });
            }
        }

        public void bindData(Post post) {

            likeController = new LikeController(context, post, likeCounterTextView, likesImageView, true);


            if (post.getTitle() != null) {

                String title = removeNewLinesDividers(post.getTitle());
                titleTextView.setText(title);

            }

            likeCounterTextView.setText(String.valueOf(post.getLikesCount()));
            commentsCountTextView.setText(String.valueOf(post.getCommentsCount()));
            watcherCounterTextView.setText(String.valueOf(post.getWatchersCount()));

            CharSequence date = FormatterUtil.getRelativeTimeSpanStringShort(context, post.getCreatedDate());
            dateTextView.setText(date);

            if (post.getImagePath() != null) {

                String imageUrl = post.getImagePath();
                int width = Utils.getDisplayWidth(context);
                int height = (int) context.getResources().getDimension(R.dimen.post_detail_image_height);

                // Displayed and saved to cache image, as needs for post detail.
                Glide.with(context)
                        .load(imageUrl)
                        .centerCrop()
                        .override(width, height)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .crossFade()
                        .error(R.drawable.ic_stub)
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(postImageView);
            }

            if (post.getAuthorId() != null) {
                profileManager.getProfileSingleValue(post.getAuthorId(), createProfileChangeListener(context, authorImageView, authorName));
            }

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                postManager.hasCurrentUserLikeSingleValue(post.getId(), firebaseUser.getUid(), createOnLikeObjectExistListener());
            }
        }

        private OnObjectExistListener<Like> createOnLikeObjectExistListener() {
            return new OnObjectExistListener<Like>() {
                @Override
                public void onDataChanged(boolean exist) {
                    likeController.initLike(exist);
                }
            };
        }
    }

    private static String removeNewLinesDividers(String text) {
        int decoratedTextLength = text.length() < Constants.Post.MAX_TEXT_LENGTH_IN_LIST ?
                text.length() : Constants.Post.MAX_TEXT_LENGTH_IN_LIST;
        return text.substring(0, decoratedTextLength).replaceAll("\n", " ").trim();
    }


    private static OnObjectChangedListener<Profile> createProfileChangeListener(final Context context,
                                                                                final ImageView authorImageView,
                                                                                final TextView authorName) {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void onObjectChanged(final Profile obj) {
                if (obj.getPhotoUrl() != null) {

                    Glide.with(context)
                            .load(obj.getPhotoUrl())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .crossFade()
                            .into(authorImageView);
                }

                authorName.setText(obj.getUsername());
            }
        };
    }
}
