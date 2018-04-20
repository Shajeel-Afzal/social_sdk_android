package sumatodev.com.social.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import sumatodev.com.social.R;
import sumatodev.com.social.model.UsersPublic;

/**
 * Suggestions Adapter.
 *
 * @author Miguel Catalan Bañuls
 */
public class SearchAdapter extends BaseAdapter implements Filterable {

    private ArrayList<UsersPublic> data;
    private List<UsersPublic> suggestions;
    private Drawable suggestionIcon;
    private LayoutInflater inflater;
    private boolean ellipsize;
    private Context context;

    public SearchAdapter(Context context, List<UsersPublic> suggestions, boolean ellipsize) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        data = new ArrayList<>();
        this.suggestions = suggestions;
        this.ellipsize = ellipsize;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (!TextUtils.isEmpty(constraint)) {

                    // Retrieve the autocomplete results.
                    List<UsersPublic> searchData = new ArrayList<>();

                    for (UsersPublic string : suggestions) {
                        if (string.getUsername().toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                            searchData.add(string);
                        }
                    }

                    // Assign the data to the FilterResults
                    filterResults.values = searchData;
                    filterResults.count = searchData.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.values != null) {
                    data = (ArrayList<UsersPublic>) results.values;
                    notifyDataSetChanged();
                }
            }
        };
        return filter;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public UsersPublic getItemByPosition(int position) {
        return data.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final SuggestionsViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.search_items, parent, false);
            viewHolder = new SuggestionsViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (SuggestionsViewHolder) convertView.getTag();
        }

        UsersPublic usersPublic = (UsersPublic) getItem(position);

        viewHolder.textView.setText(usersPublic.getUsername());
        Picasso.get().load(usersPublic.getPhotoUrl())
                .into(viewHolder.imageView);
        if (usersPublic.getId() != null) {
            Glide.with(context).load(usersPublic.getPhotoUrl())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .crossFade()
                    .error(R.drawable.user_thumbnail)
                    .into(viewHolder.imageView);
        }


        if (ellipsize) {
            viewHolder.textView.setSingleLine();
            viewHolder.textView.setEllipsize(TextUtils.TruncateAt.END);
        }

        return convertView;
    }

    public class SuggestionsViewHolder {

        TextView textView;
        CircleImageView imageView;

        public SuggestionsViewHolder(View convertView) {
            textView = convertView.findViewById(R.id.textView);
            imageView = convertView.findViewById(R.id.imageView);
        }
    }
}