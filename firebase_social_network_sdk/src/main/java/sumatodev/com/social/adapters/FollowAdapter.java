package sumatodev.com.social.adapters;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import sumatodev.com.social.R;
import sumatodev.com.social.adapters.holders.FollowHolder;
import sumatodev.com.social.listeners.OnRequestItemListener;
import sumatodev.com.social.model.UsersThread;

/**
 * Created by Ali on 03/03/2018.
 */

public class FollowAdapter extends FirebaseRecyclerAdapter<UsersThread, FollowHolder> {

    private OnRequestItemListener onRequestItemListener;

    public FollowAdapter(@NonNull FirebaseRecyclerOptions<UsersThread> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull FollowHolder holder, int position, @NonNull final UsersThread model) {
        if (model.getId() != null) {
            holder.setData(model.getId());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRequestItemListener.onItemClick(v, model.getId());
                }
            });
        }
    }

    @Override
    public FollowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FollowHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.follow_request_list, parent, false));
    }


    public void setOnRequestItemListener(OnRequestItemListener onRequestItemListener) {
        this.onRequestItemListener = onRequestItemListener;
    }
}
