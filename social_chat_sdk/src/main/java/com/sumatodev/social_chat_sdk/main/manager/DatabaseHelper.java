package com.sumatodev.social_chat_sdk.main.manager;


import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sumatodev.social_chat_sdk.Constants;
import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.enums.Consts;
import com.sumatodev.social_chat_sdk.main.model.Message;
import com.sumatodev.social_chat_sdk.main.utils.FileUtil;
import com.sumatodev.social_chat_sdk.main.utils.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;


public class DatabaseHelper {

    public static final String TAG = DatabaseHelper.class.getSimpleName();

    private static DatabaseHelper instance;

    private Context context;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private FirebaseAuth firebaseAuth;
    private Map<ValueEventListener, DatabaseReference> activeListeners = new HashMap<>();


    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }

        return instance;
    }

    public DatabaseHelper(Context context) {
        this.context = context;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public void init() {
        database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        storage = FirebaseStorage.getInstance();

//        Sets the maximum time to retry upload operations if a failure occurs.
        storage.setMaxUploadRetryTimeMillis(Constants.Database.MAX_UPLOAD_RETRY_MILLIS);
    }

    public DatabaseReference getDatabaseReference() {
        return database.getReference();
    }

    public void closeListener(ValueEventListener listener) {
        if (activeListeners.containsKey(listener)) {
            DatabaseReference reference = activeListeners.get(listener);
            reference.removeEventListener(listener);
            activeListeners.remove(listener);
            LogUtil.logDebug(TAG, "closeListener(), listener was removed: " + listener);
        } else {
            LogUtil.logDebug(TAG, "closeListener(), listener not found :" + listener);
        }
    }

    public void closeAllActiveListeners() {
        for (ValueEventListener listener : activeListeners.keySet()) {
            DatabaseReference reference = activeListeners.get(listener);
            reference.removeEventListener(listener);
        }

        activeListeners.clear();
    }


    public void sendMessage() {

    }




    public String generatePostId() {
        DatabaseReference databaseReference = database.getReference();
        return databaseReference.child("posts").push().getKey();
    }

    public String generateMessageId(String current_uid, String other_uid) {
        DatabaseReference databaseReference = database.getReference();
        return databaseReference.child(Consts.MESSAGES_REF).child(current_uid)
                .child(other_uid).push().getKey();
    }


    public void sendNewMessage(Message message) {

        try {
            DatabaseReference databaseReference = database.getReference();

            Map messageMap = new ObjectMapper().convertValue(message, Map.class);


        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }



    public UploadTask uploadImage(Uri imageUri, String imageTitle) {
        StorageReference storageRef = storage.getReferenceFromUrl(context.getResources().getString(R.string.storage_link));
        StorageReference riversRef = storageRef.child("images/" + imageTitle);
        // Create file metadata including the content type
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCacheControl("max-age=7776000, Expires=7776000, public, must-revalidate")
                .build();

        Log.d(TAG, "compressedImage: " + Arrays.toString(getCompressedImage(imageUri)));

        return riversRef.putBytes(getCompressedImage(imageUri), metadata);
    }

    public byte[] getCompressedImage(Uri imageUri) {

        try {
            File mImageFile = FileUtil.from(context, imageUri);

            Bitmap bitmap = new Compressor(context)
                    .setQuality(75)
                    .setCompressFormat(Bitmap.CompressFormat.WEBP)
                    .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES).getAbsolutePath())
                    .compressToBitmap(mImageFile);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        }
        return null;
    }

}
