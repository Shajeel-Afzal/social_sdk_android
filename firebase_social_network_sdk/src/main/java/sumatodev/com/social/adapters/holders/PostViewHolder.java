/*
 *  Copyright 2017 Rozdoum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package sumatodev.com.social.adapters.holders;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.codewaves.youtubethumbnailview.ThumbnailLoader;
import com.codewaves.youtubethumbnailview.ThumbnailView;
import com.codewaves.youtubethumbnailview.downloader.OembedVideoInfoDownloader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.klinker.android.link_builder.Link;
import com.klinker.android.link_builder.LinkBuilder;
import com.klinker.android.link_builder.TouchableMovementMethod;

import sumatodev.com.social.Constants;
import sumatodev.com.social.R;
import sumatodev.com.social.controllers.LikeController;
import sumatodev.com.social.managers.PostManager;
import sumatodev.com.social.managers.ProfileManager;
import sumatodev.com.social.managers.listeners.OnObjectChangedListener;
import sumatodev.com.social.managers.listeners.OnObjectExistListener;
import sumatodev.com.social.model.Like;
import sumatodev.com.social.model.Post;
import sumatodev.com.social.model.PostStyle;
import sumatodev.com.social.model.Profile;
import sumatodev.com.social.utils.FormatterUtil;
import sumatodev.com.social.utils.Regex;
import sumatodev.com.social.utils.Utils;

/**
 * Created by alexey on 27.12.16.
 */

public class PostViewHolder extends RecyclerView.ViewHolder {
    public static final String TAG = PostViewHolder.class.getSimpleName();

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
    private final TextView authorName;
    private ImageView postShare;
    private FrameLayout textLayout;
    private ProgressBar progressBar;

    private LinearLayout thumbnailView;
    private TextView thumbnailText;
    private ThumbnailView thumbnail;

    private ProfileManager profileManager;
    private PostManager postManager;

    private LikeController likeController;
    private OnClickListener onClickListener;

    public PostViewHolder(View view, final OnClickListener onClickListener) {
        this(view, onClickListener, true);
        this.onClickListener = onClickListener;
    }

    public PostViewHolder(View view, final OnClickListener onClickListener, boolean isAuthorNeeded) {
        super(view);
        this.context = view.getContext();
        this.onClickListener = onClickListener;

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
        textLayout = view.findViewById(R.id.textLayout);
        thumbnail = view.findViewById(R.id.thumbnail);
        thumbnailView = view.findViewById(R.id.thumbnailView);
        thumbnailText = view.findViewById(R.id.thumbnailLink);

        profileManager = ProfileManager.getInstance(context.getApplicationContext());
        postManager = PostManager.getInstance(context.getApplicationContext());


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                    onClickListener.onItemClick(getAdapterPosition(), v);
                }
            }
        });

        likeViewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                    onClickListener.onLikeClick(likeController, position);
                }
            }
        });

        authorImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                    onClickListener.onAuthorClick(getAdapterPosition(), v);
                }
            }
        });

        postShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                    onClickListener.onShareClick(getAdapterPosition(), v);
                }
            }
        });

    }

    public void bindTextPost(Post post) {

        likeController = new LikeController(context, post, likeCounterTextView, likesImageView, true);


        if (post.getTitle() != null) {
            String title = removeNewLinesDividers(post.getTitle());
            titleTextView.setText(title);


            Link link = new Link(Regex.WEB_URL_PATTERN)
                    .setTextColor(Color.BLUE).setOnClickListener(new Link.OnClickListener() {
                        @Override
                        public void onClick(String s) {
                            if (onClickListener != null) {
                                int position = getAdapterPosition();
                                if (position != RecyclerView.NO_POSITION) {
                                    onClickListener.onLinkClick(s);
                                }
                            }
                        }
                    });

            LinkBuilder.on(titleTextView).addLink(link).build();
            titleTextView.setMovementMethod(TouchableMovementMethod.getInstance());
        }

        likeCounterTextView.setText(String.valueOf(post.getLikesCount()));
        commentsCountTextView.setText(String.valueOf(post.getCommentsCount()));
        watcherCounterTextView.setText(String.valueOf(post.getWatchersCount()));

        CharSequence date = FormatterUtil.getRelativeTimeSpanStringShort(context, post.getCreatedDate());
        dateTextView.setText(date);

        if (post.getAuthorId() != null) {
            profileManager.getProfileSingleValue(post.getAuthorId(), createProfileChangeListener(authorImageView, authorName));
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            postManager.hasCurrentUserLikeSingleValue(post.getId(), firebaseUser.getUid(), createOnLikeObjectExistListener());
        }
    }

    public void bindLink(final Post post) {

        likeController = new LikeController(context, post, likeCounterTextView, likesImageView, true);

        if (post.getTitle() != null) {
            textLayout.setVisibility(View.VISIBLE);
            String title = removeNewLinesDividers(post.getTitle());
            titleTextView.setText(title);
        } else {
            textLayout.setVisibility(View.GONE);
        }

        if (post.getLink() != null) {

            thumbnailView.setVisibility(View.VISIBLE);
            thumbnailText.setText(post.getLink());

            Link link = new Link(Regex.WEB_URL_PATTERN)
                    .setTextColor(Color.BLUE).setOnClickListener(new Link.OnClickListener() {
                        @Override
                        public void onClick(String s) {
                            if (onClickListener != null) {
                                int position = getAdapterPosition();
                                if (position != RecyclerView.NO_POSITION) {
                                    onClickListener.onLinkClick(s);
                                }
                            }
                        }
                    });

            LinkBuilder.on(thumbnailText).addLink(link).build();
            thumbnailText.setMovementMethod(TouchableMovementMethod.getInstance());

            ThumbnailLoader.initialize().setVideoInfoDownloader(new OembedVideoInfoDownloader());
            thumbnail.loadThumbnail(post.getLink());
            thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onClickListener.onLinkClick(post.getLink());
                        }
                    }
                }
            });

        }

        likeCounterTextView.setText(String.valueOf(post.getLikesCount()));
        commentsCountTextView.setText(String.valueOf(post.getCommentsCount()));
        watcherCounterTextView.setText(String.valueOf(post.getWatchersCount()));

        CharSequence date = FormatterUtil.getRelativeTimeSpanStringShort(context, post.getCreatedDate());
        dateTextView.setText(date);

        if (post.getAuthorId() != null) {
            profileManager.getProfileSingleValue(post.getAuthorId(), createProfileChangeListener(authorImageView, authorName));
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            postManager.hasCurrentUserLikeSingleValue(post.getId(), firebaseUser.getUid(), createOnLikeObjectExistListener());
        }
    }

    public void bindColoredPost(final Post post) {

        likeController = new LikeController(context, post, likeCounterTextView, likesImageView, true);


        if (post.getTitle() != null) {
            String title = removeNewLinesDividers(post.getTitle());
            titleTextView.setText(title);


            Link link = new Link(Regex.WEB_URL_PATTERN)
                    .setTextColor(Color.BLUE).setOnClickListener(new Link.OnClickListener() {
                        @Override
                        public void onClick(String s) {
                            if (onClickListener != null) {
                                int position = getAdapterPosition();
                                if (position != RecyclerView.NO_POSITION) {
                                    onClickListener.onLinkClick(s);
                                }
                            }
                        }
                    });

            LinkBuilder.on(titleTextView).addLink(link).build();
            titleTextView.setMovementMethod(TouchableMovementMethod.getInstance());


        }


        likeCounterTextView.setText(String.valueOf(post.getLikesCount()));
        commentsCountTextView.setText(String.valueOf(post.getCommentsCount()));
        watcherCounterTextView.setText(String.valueOf(post.getWatchersCount()));

        CharSequence date = FormatterUtil.getRelativeTimeSpanStringShort(context, post.getCreatedDate());
        dateTextView.setText(date);

        if (post.getAuthorId() != null) {
            profileManager.getProfileSingleValue(post.getAuthorId(), createProfileChangeListener(authorImageView, authorName));
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            postManager.hasCurrentUserLikeSingleValue(post.getId(), firebaseUser.getUid(), createOnLikeObjectExistListener());
        }

        int bg_color = post.getPostStyle().bg_color;
        String color = Integer.toHexString(bg_color);
        if (!color.isEmpty()) {
            postManager.isCurrentPostColored(post.getId(), isCurrentPostColored());
        }

        textLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                    onClickListener.onItemClick(getAdapterPosition(), v);
                }
            }
        });
    }

    public void bindImagePost(Post post) {

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
            profileManager.getProfileSingleValue(post.getAuthorId(), createProfileChangeListener(authorImageView, authorName));
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            postManager.hasCurrentUserLikeSingleValue(post.getId(), firebaseUser.getUid(), createOnLikeObjectExistListener());
        }
    }

    public void bindTextImagePost(Post post) {

        likeController = new LikeController(context, post, likeCounterTextView, likesImageView, true);


        if (post.getTitle() != null) {
            String title = removeNewLinesDividers(post.getTitle());
            titleTextView.setText(title);


            Link link = new Link(Regex.WEB_URL_PATTERN)
                    .setTextColor(Color.BLUE).setOnClickListener(new Link.OnClickListener() {
                        @Override
                        public void onClick(String s) {
                            if (onClickListener != null) {
                                int position = getAdapterPosition();
                                if (position != RecyclerView.NO_POSITION) {
                                    onClickListener.onLinkClick(s);
                                }
                            }
                        }
                    });

            LinkBuilder.on(titleTextView).addLink(link).build();
            titleTextView.setMovementMethod(TouchableMovementMethod.getInstance());
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
            profileManager.getProfileSingleValue(post.getAuthorId(), createProfileChangeListener(authorImageView, authorName));
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            postManager.hasCurrentUserLikeSingleValue(post.getId(), firebaseUser.getUid(), createOnLikeObjectExistListener());
        }
    }

    private String removeNewLinesDividers(String text) {
        int decoratedTextLength = text.length() < Constants.Post.MAX_TEXT_LENGTH_IN_LIST ?
                text.length() : Constants.Post.MAX_TEXT_LENGTH_IN_LIST;
        return text.substring(0, decoratedTextLength).replaceAll("\n", " ").trim();
    }

    private OnObjectChangedListener<PostStyle> isCurrentPostColored() {
        return new OnObjectChangedListener<PostStyle>() {
            @Override
            public void onObjectChanged(PostStyle obj) {
                if (obj != null) {

                    final float scale = context.getResources().getDisplayMetrics().density;
                    int pixels = (int) (180 * scale + 0.5f);

                    textLayout.setBackgroundColor(obj.bg_color);
                    titleTextView.setHeight(pixels);
                    titleTextView.setTextColor(Color.WHITE);
                    titleTextView.setTextSize(24);
                    titleTextView.setGravity(Gravity.CENTER);
                    titleTextView.setMaxLines(3);
                    titleTextView.setTypeface(Typeface.DEFAULT_BOLD);
                }


            }
        };
    }

    private OnObjectChangedListener<Profile> createProfileChangeListener(final ImageView authorImageView, final TextView authorName) {
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

    private OnObjectExistListener<Like> createOnLikeObjectExistListener() {
        return new OnObjectExistListener<Like>() {
            @Override
            public void onDataChanged(boolean exist) {
                likeController.initLike(exist);
            }
        };
    }

    public interface OnClickListener {
        void onItemClick(int position, View view);

        void onLikeClick(LikeController likeController, int position);

        void onAuthorClick(int position, View view);

        void onShareClick(int position, View view);

        void onLinkClick(String linkUrl);
    }
}