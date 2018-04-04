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

import sumatodev.com.social.Constants;
import sumatodev.com.social.R;
import sumatodev.com.social.controllers.LikeController;
import sumatodev.com.social.managers.PostManager;
import sumatodev.com.social.managers.ProfileManager;
import sumatodev.com.social.managers.listeners.OnObjectChangedListener;
import sumatodev.com.social.managers.listeners.OnObjectExistListener;
import sumatodev.com.social.model.Like;
import sumatodev.com.social.model.Post;
import sumatodev.com.social.model.Profile;
import sumatodev.com.social.utils.FormatterUtil;

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

        private LikeController likeController;

        public TextHolder(View view) {
            super(view);

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
        }


        public void bindData(Post post) {

            if (post.getTitle() != null) {
                titleTextView.setVisibility(View.VISIBLE);
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

//            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//            if (firebaseUser != null) {
//                postManager.hasCurrentUserLikeSingleValue(post.getId(), firebaseUser.getUid(), createOnLikeObjectExistListener());
//            }

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

        public ImageHolder(View view) {
            super(view);

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

    private static OnObjectExistListener<Like> createOnLikeObjectExistListener() {
        return new OnObjectExistListener<Like>() {
            @Override
            public void onDataChanged(boolean exist) {
                //likeController.initLike(exist);
            }
        };
    }


}
