package sumatodev.com.social.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sumatodev.com.social.R;
import sumatodev.com.social.managers.ProfileManager;
import sumatodev.com.social.managers.listeners.OnObjectChangedListener;
import sumatodev.com.social.model.Friends;
import sumatodev.com.social.model.UsersPublic;

/**
 * Created by Ali on 11/04/2018.
 */

public class NamesAdapter extends ArrayAdapter<Friends> {

    private List<Friends> objects;
    private Context context;
    private ProfileManager profileManager;
    private Filter filter;

    public NamesAdapter(@NonNull Context context, @NonNull List<Friends> objects) {
        super(context, 0, objects);
        this.context = context;
        this.objects = objects;
        profileManager = ProfileManager.getInstance(context);
    }

    @Override
    public int getCount() {
        return objects.size();
    }


    @Nullable
    @Override
    public Friends getItem(int position) {
        return objects.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.names_tag_list_layout, parent, false);
        }

        TextView userName = convertView.findViewById(R.id.userName);

        Friends friends = objects.get(position);
        if (friends != null) {

            profileManager.getUserPublicValue(context.getApplicationContext(), friends.getId(),
                    publicOnObjectChangedListener(userName));
        }
        return convertView;
    }


    @NonNull
    @Override
    public Filter getFilter() {
        if (filter == null)
            filter = new AppFilter<>(objects);
        return filter;
    }

    private class AppFilter<T> extends Filter {

        private ArrayList<T> sourceObjects;

        public AppFilter(List<T> objects) {
            sourceObjects = new ArrayList<T>();
            synchronized (this) {
                sourceObjects.addAll(objects);
            }
        }

        @Override
        protected FilterResults performFiltering(CharSequence chars) {
            String filterSeq = chars.toString().toLowerCase();
            FilterResults result = new FilterResults();
            if (filterSeq != null && filterSeq.length() > 0) {
                ArrayList<T> filter = new ArrayList<T>();

                for (T object : sourceObjects) {
                    // the filtering itself:
                    if (object.toString().toLowerCase().contains(filterSeq))
                        filter.add(object);
                }
                result.count = filter.size();
                result.values = filter;
            } else {
                // add all objects
                synchronized (this) {
                    result.values = sourceObjects;
                    result.count = sourceObjects.size();
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            // NOTE: this function is *always* called from the UI thread.
            ArrayList<T> filtered = (ArrayList<T>) results.values;
            notifyDataSetChanged();
            clear();
            for (int i = 0, l = filtered.size(); i < l; i++)
                add((Friends) filtered.get(i));
            notifyDataSetInvalidated();
        }
    }

    private OnObjectChangedListener<UsersPublic> publicOnObjectChangedListener(final TextView username) {
        return new OnObjectChangedListener<UsersPublic>() {
            @Override
            public void onObjectChanged(UsersPublic obj) {
                if (obj != null) {
                    Log.d("NamesAdapter", "Name: " + obj.getUsername());
                    username.setText(obj.getUsername());
                }
            }
        };
    }
}
