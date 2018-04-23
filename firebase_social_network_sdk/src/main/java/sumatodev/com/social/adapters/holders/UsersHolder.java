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
import sumatodev.com.social.adapters.PublicListAdapter;
import sumatodev.com.social.model.UsersPublic;
import sumatodev.com.social.ui.fragments.UsersFragment;
import sumatodev.com.social.views.RoundedCornersTransform;

/**
 * Created by Ali on 14/02/2018.
 */

public class UsersHolder extends RecyclerView.ViewHolder {

    private static final String TAG = RequestsHolder.class.getSimpleName();
    private ImageView userImage;
    private TextView userName;
    private ImageButton acceptBtn;
    private ImageButton rejectBtn;

    public UsersHolder(View itemView, PublicListAdapter.CallBack callBack) {
        super(itemView);
        findViews(itemView, callBack);
    }

    private void findViews(View itemView, final PublicListAdapter.CallBack callBack) {
        userImage = itemView.findViewById(R.id.userImage);
        userName = itemView.findViewById(R.id.userName);
        acceptBtn = itemView.findViewById(R.id.acceptBtn);
        rejectBtn = itemView.findViewById(R.id.rejectBtn);


        if (callBack != null) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        callBack.onItemClick(position, v);
                    }
                }
            });
        }

    }

    public void setData(final UsersPublic model) {
        if (model != null) {

            acceptBtn.setVisibility(View.GONE);
            rejectBtn.setVisibility(View.GONE);

            userName.setText(capitalize(model.getUsername()));
            Picasso.get().load(model.getPhotoUrl())
                    .transform(new RoundedCornersTransform())
                    .placeholder(R.drawable.imageview_user_thumb)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(userImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(model.getPhotoUrl())
                                    .placeholder(R.drawable.user_thumb)
                                    .transform(new RoundedCornersTransform())
                                    .into(userImage);
                        }
                    });
        }
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

}
