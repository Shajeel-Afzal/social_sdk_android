package com.sumatodev.social_chat_sdk.views.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


import com.sumatodev.social_chat_sdk.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ThreadsFragment extends Fragment {


    public ThreadsFragment() {
        // Required empty public constructor
    }

    public static ThreadsFragment newInstance() {

        Bundle args = new Bundle();
        ThreadsFragment fragment = new ThreadsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_threads, container, false);
    }


}
