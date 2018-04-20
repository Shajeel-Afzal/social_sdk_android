package sumatodev.com.social.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.sumatodev.social_chat_sdk.main.utils.RoundedCornersTransform;

import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import sumatodev.com.social.R;
import sumatodev.com.social.model.UsersPublic;

public class UsersAdapter extends RecyclerArrayAdapter<UsersPublic, UsersAdapter.UsersAdapterViewHolder> {

    /**
     * {@link Context}.
     */
    private final Context context;

    /**
     * Current search string typed by the user.  It is used highlight the query in the
     * search results.  Ex: @bill.
     */
    private String currentQuery;

    /**
     * {@link ForegroundColorSpan}.
     */
    private final ForegroundColorSpan colorSpan;

    public UsersAdapter(Context context) {
        this.context = context;
        final int orange = ContextCompat.getColor(context, R.color.mentions_default_color);
        this.colorSpan = new ForegroundColorSpan(orange);
    }


    /**
     * Setter for what user has queried.
     */
    public void setCurrentQuery(final String currentQuery) {
        if (!TextUtils.isEmpty(currentQuery)) {
            this.currentQuery = currentQuery.toLowerCase(Locale.US);
        }
    }

    @Override
    public UsersAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(context).inflate(R.layout.search_items, parent, false);
        return new UsersAdapterViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final UsersAdapterViewHolder holder, int position) {

        final UsersPublic mentionsUser = getItem(position);

        if (mentionsUser != null && !TextUtils.isEmpty(mentionsUser.getUsername())) {
            holder.textView.setText(mentionsUser.getUsername(), TextView.BufferType.SPANNABLE);
            highlightSearchQueryInUserName(holder.textView.getText());
            if (!TextUtils.isEmpty(mentionsUser.getPhotoUrl())) {
                holder.imageView.setVisibility(View.VISIBLE);

                Picasso.get().load(mentionsUser.getPhotoUrl())
                        .placeholder(com.sumatodev.social_chat_sdk.R.drawable.imageview_user_thumb)
                        .transform(new RoundedCornersTransform())
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(holder.imageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {

                                Picasso.get().load(mentionsUser.getPhotoUrl())
                                        .placeholder(com.sumatodev.social_chat_sdk.R.drawable.imageview_user_thumb)
                                        .transform(new RoundedCornersTransform())
                                        .into(holder.imageView);
                            }
                        });
            } else {
                holder.imageView.setVisibility(View.GONE);
            }
        }

    }

    /**
     * Highlights the current search text in the mentions list.
     */
    private void highlightSearchQueryInUserName(CharSequence userName) {
        if (!TextUtils.isEmpty(currentQuery)) {
            int searchQueryLocation = userName.toString().toLowerCase(Locale.US).indexOf(currentQuery);

            if (searchQueryLocation != -1) {
                Spannable userNameSpannable = (Spannable) userName;
                userNameSpannable.setSpan(
                        colorSpan,
                        searchQueryLocation,
                        searchQueryLocation + currentQuery.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }


    public class UsersAdapterViewHolder extends RecyclerView.ViewHolder {

        TextView textView;
        CircleImageView imageView;

        public UsersAdapterViewHolder(View convertView) {
            super(convertView);
            textView = convertView.findViewById(R.id.textView);
            imageView = convertView.findViewById(R.id.imageView);


        }
    }
}
