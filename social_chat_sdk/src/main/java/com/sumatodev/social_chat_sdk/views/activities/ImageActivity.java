package com.sumatodev.social_chat_sdk.views.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.sumatodev.social_chat_sdk.R;

public class ImageActivity extends AppCompatActivity implements View.OnClickListener {

    protected static final int IMAGE_PICKED_KEY = 555;
    private static final String TAG = ImageActivity.class.getSimpleName();
    private ActionBar actionBar;
    private ImageView imageView;
    private ImageButton sendBtn;
    private ProgressBar progressBar;
    private String imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        findViews();
        initActionBar();

        if (getIntent() != null) {
            imageUri = getIntent().getStringExtra("imageUrl");
            Log.d(TAG, "ImageUrl: " + imageUri);
        }
        loadImage();
    }

    private void findViews() {
        imageView = findViewById(R.id.imageView);
        sendBtn = findViewById(R.id.sendBtn);
        progressBar = findViewById(R.id.progressBar);

        sendBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == sendBtn) {
            setResult(Activity.RESULT_OK);
            ImageActivity.this.finish();
        }
    }

    private void initActionBar() {
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void loadImage() {
        progressBar.setVisibility(View.VISIBLE);

        if (imageUri == null) {
            return;
        }

        Glide.with(this)
                .load(Uri.parse(imageUri))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .fitCenter()
                .listener(new RequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(imageView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
