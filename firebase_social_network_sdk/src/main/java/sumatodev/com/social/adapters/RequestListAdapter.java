package sumatodev.com.social.adapters;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import sumatodev.com.social.R;
import sumatodev.com.social.adapters.holders.RequestsHolder;
import sumatodev.com.social.listeners.OnRequestItemListener;
import sumatodev.com.social.model.Follow;
import sumatodev.com.social.utils.InternetStatus;

/**
 * Created by Ali on 17/02/2018.
 */

public class RequestListAdapter extends FirebaseRecyclerAdapter<Follow, RequestsHolder> {

    private static final String TAG = RequestListAdapter.class.getName();
    private OnRequestItemListener onRequestItemListener;

    public RequestListAdapter(@NonNull FirebaseRecyclerOptions<Follow> options) {
        super(options);
    }


    @Override
    protected void onBindViewHolder(@NonNull RequestsHolder holder, int position, @NonNull final Follow model) {
        if (model.getId() != null) {
            holder.setData(model.getId());


            holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (InternetStatus.getInstance(v.getContext()).isOnline()) {
                        onRequestItemListener.onAcceptClick(model.getId());
                    } else {
                        Toast.makeText(v.getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            holder.rejectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (InternetStatus.getInstance(v.getContext()).isOnline()) {
                        onRequestItemListener.onRejectClick(model.getId());
                    } else {
                        Toast.makeText(v.getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (InternetStatus.getInstance(v.getContext()).isOnline()) {
                        onRequestItemListener.onItemClick(v, model.getId());
                    } else {
                        Toast.makeText(v.getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public RequestsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RequestsHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.follow_request_list, parent, false));
    }

    public void setOnRequestItemListener(OnRequestItemListener onRequestItemListener) {
        this.onRequestItemListener = onRequestItemListener;
    }


}
