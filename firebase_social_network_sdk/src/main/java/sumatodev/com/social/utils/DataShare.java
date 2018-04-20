package sumatodev.com.social.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Ali on 30/03/2018.
 */

public class DataShare {

    private Target loadtarget;
    Context context;
    String DataUrl; // set your message
    String ImageUrl;


    public DataShare(Context context, String dataUrl) {
        this.context = context;
        DataUrl = dataUrl;

    }

    public void ShareAndLoadImage(String url) {  //url is your image url

        if (loadtarget == null) loadtarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                // do something with the Bitmap
                try {
                    handleLoadedBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onBitmapFailed(Exception e,Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }

        };

        Picasso.get().load(url).into(loadtarget);
    }

    public Uri handleLoadedBitmap(Bitmap b) throws IOException {
        // do something here
// file donwload temporary
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
        if (null != b) {
            Log.i("Data", "=>" + b);
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            b.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            Uri data = Uri.fromFile(file);
            Share(context, data);

        }
        return Uri.fromFile(file);

    }

    public void Share(Context context, Uri bmpUri) {
        Intent intent = new Intent();
        intent .setAction(Intent.ACTION_SEND);

        Log.i("bmpurl", "=>" + bmpUri);

        intent .putExtra(Intent.EXTRA_TEXT, DataUrl);
        intent .putExtra(Intent.EXTRA_SUBJECT, "Subject"); // set your subject here
        intent .putExtra(Intent.EXTRA_STREAM, bmpUri);

        intent .setType("image/*");

        if (!TextUtils.isEmpty(bmpUri + "")) {
            context.startActivity(Intent.createChooser(intent , "Share Image"));
        }

    }
}
