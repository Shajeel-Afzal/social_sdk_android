package sumatodev.com.social.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import sumatodev.com.social.R;
import sumatodev.com.social.adapters.holders.UsersHolder;
import sumatodev.com.social.model.UsersPublic;
import sumatodev.com.social.ui.fragments.UsersFragment;

public class PublicListAdapter extends RecyclerView.Adapter<UsersHolder> {

    private List<UsersPublic> list = new ArrayList<>();
    private CallBack callBack;

    @Override
    public UsersHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new UsersHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.follow_request_list, parent,
                false), callBack);
    }

    @Override
    public void onBindViewHolder(UsersHolder holder, int position) {
        holder.setData(getItemByPosition(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public UsersPublic getItemByPosition(int position) {
        return list.get(position);
    }

    public void setList(List<UsersPublic> list) {
        this.list = list;
        callBack.onListChanged(list.size());
        notifyDataSetChanged();
    }


    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public interface CallBack {
        void onItemClick(int position, View view);

        void onListChanged(int items);
    }
}
