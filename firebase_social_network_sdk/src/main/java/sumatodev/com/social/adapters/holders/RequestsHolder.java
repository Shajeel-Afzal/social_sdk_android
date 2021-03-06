package sumatodev.com.social.adapters.holders;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import sumatodev.com.social.R;
import sumatodev.com.social.managers.FirebaseUtils;
import sumatodev.com.social.model.UsersPublic;
import sumatodev.com.social.views.RoundedCornersTransform;


public class RequestsHolder extends RecyclerView.ViewHolder {

    private static final String TAG = RequestsHolder.class.getSimpleName();
    private ImageView userImage;
    private TextView userName;
    public ImageButton acceptBtn;
    public ImageButton rejectBtn;

    public RequestsHolder(View itemView) {
        super(itemView);
        findViews(itemView);
    }

    private void findViews(View itemView) {
        userImage = itemView.findViewById(R.id.userImage);
        userName = itemView.findViewById(R.id.userName);
        acceptBtn = itemView.findViewById(R.id.acceptBtn);
        rejectBtn = itemView.findViewById(R.id.rejectBtn);

    }

    public void setData(final String userKeys) {
        if (userKeys != null) {
            FirebaseUtils.getUserPublicInfoRef().child(userKeys)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                final UsersPublic user = dataSnapshot.getValue(UsersPublic.class);
                                if (user != null) {
                                    userName.setText(capitalize(user.getUsername()));

                                    if (user.getPhotoUrl().isEmpty())
                                        return;
                                    
                                    Picasso.get().load(user.getPhotoUrl())
                                            .transform(new RoundedCornersTransform())
                                            .placeholder(R.drawable.imageview_user_thumb)
                                            .networkPolicy(NetworkPolicy.OFFLINE)
                                            .into(userImage, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    Picasso.get().load(user.getPhotoUrl())
                                                            .placeholder(R.drawable.user_thumb)
                                                            .transform(new RoundedCornersTransform())
                                                            .into(userImage);
                                                }
                                            });

                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            switch (error.getCode()) {
                                case DatabaseError.PERMISSION_DENIED:
                                    Log.d(TAG, "permission denied");
                                    break;
                                case DatabaseError.NETWORK_ERROR:
                                    Log.d(TAG, "network error");
                                    break;
                                default:
                                    Log.d(TAG, "something went wrong...");
                            }
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

