package com.layer.xdk.ui.recyclerview;

/**
 * Listens for clicks on an item in a RecyclerView.
 */
public interface OnItemClickListener<T> {
    /**
     * Alerts the listener to item clicks.
     *
     * @param item The item clicked.
     */
    void onItemClick(T item);
}
