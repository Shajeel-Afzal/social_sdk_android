package sumatodev.com.social.adapters.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import sumatodev.com.social.R;
import sumatodev.com.social.managers.ProfileManager;
import sumatodev.com.social.managers.listeners.OnObjectChangedListener;
import sumatodev.com.social.model.Friends;
import sumatodev.com.social.model.UsersPublic;
import sumatodev.com.social.views.RoundedCornersTransform;

public class FollowHolder extends RecyclerView.ViewHolder {

    private static final String TAG = FollowHolder.class.getSimpleName();
    private ImageView userImage;
    private TextView userName;
    public ImageButton acceptBtn;
    public ImageButton rejectBtn;


    private final ProfileManager profileManager;

    public FollowHolder(View itemView, final OnClickListener onClickListener) {
        super(itemView);

        profileManager = ProfileManager.getInstance(itemView.getContext().getApplicationContext());

        userImage = itemView.findViewById(R.id.userImage);
        userName = itemView.findViewById(R.id.userName);
        acceptBtn = itemView.findViewById(R.id.acceptBtn);
        rejectBtn = itemView.findViewById(R.id.rejectBtn);


        if (onClickListener != null) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onClickListener.onItemClick(position, v);
                    }
                }
            });
        }
    }


    public void bindData(Friends friends) {


        if (friends.getId() != null) {
            profileManager.getUserPublicProfile(friends.getId(), onObjectChangedListener(userName, userImage));
        }
    }

    private OnObjectChangedListener<UsersPublic> onObjectChangedListener(final TextView userName, final ImageView userImage) {
        return new OnObjectChangedListener<UsersPublic>() {
            @Override
            public void onObjectChanged(final UsersPublic obj) {
                if (obj != null) {

                    userName.setText(capitalize(obj.getUsername()));

                    if (obj.getPhotoUrl() != null && !obj.getPhotoUrl().isEmpty()) {
                        Picasso.get().load(obj.getPhotoUrl())
                                .transform(new RoundedCornersTransform())
                                .placeholder(R.drawable.imageview_user_thumb)
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .into(userImage, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Picasso.get().load(obj.getPhotoUrl())
                                                .placeholder(R.drawable.user_thumb)
                                                .transform(new RoundedCornersTransform())
                                                .into(userImage);
                                    }
                                });
                    }
                }
            }
        };
    }

    private static String capitalize(String input) {

        if (input != null) {
            String[] words = input.toLowerCase().split(" ");
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < words.length; i++) {
                String word = words[i];

                if (i > 0 && word.length() > 0) {
                    builder.append(" ");
                }

                String cap = word.substring(0, 1).toUpperCase() + word.substring(1);
                builder.append(cap);
            }
            return builder.toString();
        } else {
            return null;
        }
    }


    public interface OnClickListener {

        void onItemClick(int position, View view);
    }
}

