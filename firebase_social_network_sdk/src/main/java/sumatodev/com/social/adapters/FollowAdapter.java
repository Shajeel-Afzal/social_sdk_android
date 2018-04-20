package sumatodev.com.social.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import sumatodev.com.social.R;
import sumatodev.com.social.adapters.holders.FollowHolder;
import sumatodev.com.social.model.Friends;

public class FollowAdapter extends RecyclerView.Adapter<FollowHolder> {


    private static final String TAG = FollowAdapter.class.getSimpleName();
    private List<Friends> list = new ArrayList<>();
    private int selectedItemPosition = -1;
    private CallBack callBack;

    public FollowAdapter() {
    }

    @Override
    public FollowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.follow_request_list, parent, false);
        return new FollowHolder(view, onClickListener());
    }

    private FollowHolder.OnClickListener onClickListener() {
        return new FollowHolder.OnClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                if (callBack != null) {
                    selectedItemPosition = position;
                    callBack.onItemClick(getItemByPosition(position).getId(), view);
                }
            }
        };
    }

    @Override
    public void onBindViewHolder(FollowHolder holder, int position) {
        holder.acceptBtn.setVisibility(View.GONE);
        holder.rejectBtn.setVisibility(View.GONE);
        holder.bindData(getItemByPosition(position));
    }

    public Friends getItemByPosition(int position) {
        return list.get(position);
    }

    public void setList(List<Friends> list) {
        this.list = list;
        callBack.onListChanged(list.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void cleanSelectedPosition() {
        selectedItemPosition = -1;
    }

    public void removeItem() {
        list.remove(selectedItemPosition);
        callBack.onListChanged(list.size());
        notifyItemRemoved(selectedItemPosition);
    }


    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public interface CallBack {
        void onItemClick(String userKey, View view);

        void onListChanged(int items);
    }
}

