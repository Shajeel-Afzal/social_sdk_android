package com.sumatodev.social_chat_sdk.main.manager;


import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sumatodev.social_chat_sdk.Constants;
import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.data.model.InputMessage;
import com.sumatodev.social_chat_sdk.main.data.model.Message;
import com.sumatodev.social_chat_sdk.main.data.model.User;
import com.sumatodev.social_chat_sdk.main.enums.Consts;
import com.sumatodev.social_chat_sdk.main.listeners.OnMessageChangedListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnMessageSentListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnObjectChangedListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnThreadsListChangedListener;
import com.sumatodev.social_chat_sdk.main.model.Profile;
import com.sumatodev.social_chat_sdk.main.model.ThreadListResult;
import com.sumatodev.social_chat_sdk.main.model.ThreadsModel;
import com.sumatodev.social_chat_sdk.main.utils.FileUtil;
import com.sumatodev.social_chat_sdk.main.utils.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.zelory.compressor.Compressor;


public class DatabaseHelper {

    public static final String TAG = DatabaseHelper.class.getSimpleName();
    private static final String network_error = "network error";
    private static final String operation_failed = "operation failed";
    private static final String sending_failed = "sending failed";
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

    public String getCurrentUser() {
        return firebaseAuth.getCurrentUser() == null ? null : firebaseAuth.getCurrentUser().getUid();
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

/*
    public void sendNewMessage(Message message, final OnMessageSentListener onMessageSentListener) {

        try {
            if (message.getId() == null) {
                message.setId(generateMessageId(getCurrentUser(), message.getToUserId()));
            }
            DatabaseReference databaseReference = database.getReference();
            Map messageMap = new ObjectMapper().convertValue(message, Map.class);

            String currentUser = message.getToUserId() + "/" + getCurrentUser();
            String chatUser = getCurrentUser() + "/" + message.getToUserId();

            Map<String, Object> messageUserMap = new HashMap<>();
            messageUserMap.put(currentUser + "/" + message.getId(), messageMap);
            messageUserMap.put(chatUser + "/" + message.getId(), messageMap);

            databaseReference.child(Consts.MESSAGES_REF).updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        onMessageSentListener.onMessageSent(true, "success");
                    } else {
                        switch (databaseError.getCode()) {
                            case DatabaseError.NETWORK_ERROR:
                                onMessageSentListener.onMessageSent(false, network_error);
                                break;
                            case DatabaseError.OPERATION_FAILED:
                                onMessageSentListener.onMessageSent(false, operation_failed);
                                break;
                            default:
                                onMessageSentListener.onMessageSent(false, sending_failed);
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
    */

    public void sendMessage(InputMessage inputMessage, final OnMessageSentListener onMessageSentListener) {

        try {
            String messageKey = generateMessageId(getCurrentUser(), inputMessage.getUid());

            User user = new User(getCurrentUser(), null, null, false);
            Message message = new Message(messageKey, user, inputMessage.getText());

            DatabaseReference databaseReference = database.getReference();
            Map messageMap = new ObjectMapper().convertValue(message, Map.class);

            String currentUser = inputMessage.getUid() + "/" + getCurrentUser();
            String chatUser = getCurrentUser() + "/" + inputMessage.getUid();

            Map<String, Object> messageUserMap = new HashMap<>();
            messageUserMap.put(currentUser + "/" + message.getId(), messageMap);
            messageUserMap.put(chatUser + "/" + message.getId(), messageMap);

            databaseReference.child(Consts.MESSAGES_REF).updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        onMessageSentListener.onMessageSent(true, "success");
                    } else {
                        switch (databaseError.getCode()) {
                            case DatabaseError.NETWORK_ERROR:
                                onMessageSentListener.onMessageSent(false, network_error);
                                break;
                            case DatabaseError.OPERATION_FAILED:
                                onMessageSentListener.onMessageSent(false, operation_failed);
                                break;
                            default:
                                onMessageSentListener.onMessageSent(false, sending_failed);
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }

    public String generateMessageId(String current_uid, String other_uid) {
        DatabaseReference databaseReference = database.getReference();
        return databaseReference.child(Consts.MESSAGES_REF).child(current_uid)
                .child(other_uid).push().getKey();
    }


    public void getChatList(String userKey, final OnMessageChangedListener<Message> listener) {

        final DatabaseReference databaseReference = database.getReference(Consts.MESSAGES_REF)
                .child(getCurrentUser()).child(userKey);
        Query postsQuery;

        postsQuery = databaseReference.limitToLast(Constants.Message.MESSAGE_AMOUNT_ON_PAGE).orderByChild("createdAt");

        postsQuery.keepSynced(true);

        postsQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "datasnapshot messages: " + dataSnapshot.getValue());

                if (dataSnapshot.getValue() != null) {
                    HashMap hashMap = (HashMap) dataSnapshot.getValue();
                    if (hashMap != null) {

                        long createdDate = (long) hashMap.get("createdAt");

                        Message message = new Message();
                        message.setId((String) hashMap.get("id"));
                        message.setText((String) hashMap.get("text"));
                        message.setCreatedAt(new Date(createdDate));

                        HashMap userMap = (HashMap) hashMap.get("user");
                        User user = new User();
                        if (userMap != null) {
                            user.setId((String) userMap.get("id"));

                            Log.d(TAG, "user: " + user.getId());
                        }

                        message.setUser(user);
                        listener.OnListChanged(message);
                    }
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onCancel(databaseError.getMessage());
            }
        });
    }

    public void getThreadsList(final OnThreadsListChangedListener<ThreadsModel> listener) {
        DatabaseReference reference = database.getReference(Consts.MESSAGES_REF).child(getCurrentUser());

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Log.d(TAG, "datasnapshot threads: " + dataSnapshot.getValue());
                Map<String, Object> objectMap = (Map<String, Object>) dataSnapshot.getValue();
                listener.onListChanged(getList(objectMap));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onCanceled(context.getString(R.string.permission_denied_error));
            }
        });
    }

    private ThreadListResult getList(Map<String, Object> value) {
        ThreadListResult listResult = new ThreadListResult();
        List<ThreadsModel> list = new ArrayList<>();

        if (value != null) {

            for (String key : value.keySet()) {

                ThreadsModel model = new ThreadsModel();
                model.setThreadKey(key);

                list.add(model);
            }

            listResult.setThreads(list);
        }
        return listResult;
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

    public ValueEventListener getProfile(String id, final OnObjectChangedListener<Profile> listener) {
        DatabaseReference databaseReference = getDatabaseReference().child("profiles").child(id);
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile profile = dataSnapshot.getValue(Profile.class);
                listener.onObjectChanged(profile);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getProfile(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
        activeListeners.put(valueEventListener, databaseReference);
        return valueEventListener;
    }

    public void getProfileSingleValue(String id, final OnObjectChangedListener<Profile> listener) {
        DatabaseReference databaseReference = getDatabaseReference().child("profiles").child(id);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile profile = dataSnapshot.getValue(Profile.class);
                listener.onObjectChanged(profile);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getProfileSingleValue(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

}
