package com.layer.xdk.ui.recyclerview;

/**
 * Listens for long clicks on an item in a RecyclerView.
 */
public interface OnItemLongClickListener<T> {

    /**
     * Alerts the listener to long item clicks.
     *
     * @param item The item long-clicked.
     * @return true if the long-click was handled, false otherwise.
     */
    boolean onItemLongClick(T item);
}
