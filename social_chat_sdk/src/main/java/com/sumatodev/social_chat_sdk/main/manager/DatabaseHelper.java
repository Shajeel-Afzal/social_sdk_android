package com.sumatodev.social_chat_sdk.main.manager;


import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
import com.sumatodev.social_chat_sdk.main.enums.Consts;
import com.sumatodev.social_chat_sdk.main.listeners.OnDataChangedListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnMessageListChangedListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnMessageSentListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnObjectChangedListener;
import com.sumatodev.social_chat_sdk.main.model.InputMessage;
import com.sumatodev.social_chat_sdk.main.model.Message;
import com.sumatodev.social_chat_sdk.main.model.MessageListResult;
import com.sumatodev.social_chat_sdk.main.model.Profile;
import com.sumatodev.social_chat_sdk.main.model.Status;
import com.sumatodev.social_chat_sdk.main.model.ThreadsModel;
import com.sumatodev.social_chat_sdk.main.model.UsersPublic;
import com.sumatodev.social_chat_sdk.main.utils.FileUtil;
import com.sumatodev.social_chat_sdk.main.utils.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

    public void sendNewMessage(InputMessage inputMessage, final OnMessageSentListener onMessageSentListener) {

        try {
            Message message = new Message();
            message.setText(inputMessage.getText());
            message.setFromUserId(getCurrentUser());

            if (message.getId() == null) {
                message.setId(generateMessageId(getCurrentUser(), inputMessage.getUid()));
            }
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


    public ValueEventListener getThreadsList(final OnDataChangedListener<ThreadsModel> onDataChangedListener) {
        DatabaseReference reference = database.getReference(Consts.MESSAGES_REF).child(getCurrentUser());
        ValueEventListener valueEventListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {
                    List<ThreadsModel> list = new ArrayList<>();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        ThreadsModel model = new ThreadsModel(child.getKey());
                        list.add(model);
                    }
                    onDataChangedListener.onListChanged(list);
                }
                // Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
                //onDataChangedListener.onListChanged(getThreads(objectMap));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onDataChangedListener.onCancel(context.getString(R.string.permission_denied_error));
            }
        });

        activeListeners.put(valueEventListener, reference);
        return valueEventListener;
    }

    private List<ThreadsModel> getThreads(Map<String, Object> objectMap) {
        List<ThreadsModel> list = new ArrayList<>();
        if (objectMap != null) {
            for (String keys : objectMap.keySet()) {
                ThreadsModel model = new ThreadsModel();
                model.setThreadKey(keys);

                list.add(model);
            }
        }
        return list;
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


    public ValueEventListener getUsersPublicProfile(String userKey, final OnObjectChangedListener<UsersPublic> listener) {

        DatabaseReference databaseReference = getDatabaseReference().child("users_public").child(userKey);
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UsersPublic profile = dataSnapshot.getValue(UsersPublic.class);
                listener.onObjectChanged(profile);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getProfileSingleValue(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
        activeListeners.put(valueEventListener, databaseReference);
        return valueEventListener;
    }

    public ValueEventListener getMessageList(final String userKey, final OnMessageListChangedListener<Message> listener, final long date) {

        DatabaseReference databaseReference = database.getReference(Consts.MESSAGES_REF).child(getCurrentUser()).child(userKey);

        Query chatQuery;
        if (date == 0) {
            chatQuery = databaseReference.limitToLast(Constants.Message.MESSAGE_AMOUNT_ON_PAGE).orderByChild("createdAt");
        } else {
            chatQuery = databaseReference.limitToLast(Constants.Message.MESSAGE_AMOUNT_ON_PAGE).endAt(date).orderByChild("createdAt");
        }

        ValueEventListener eventListener = chatQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Object> hashMap = (HashMap<String, Object>) dataSnapshot.getValue();
                MessageListResult result = parceMessageList(hashMap);

                if (result.getMessages().isEmpty() && result.isMoreDataAvailable()) {
                    getMessageList(userKey, listener, result.getLastItemCreatedDate() - 1);
                } else {
                    listener.onListChanged(parceMessageList(hashMap));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getMessageList(), onCancelled", new Exception(databaseError.getMessage()));
                listener.onCanceled(context.getString(R.string.permission_denied_error));
            }
        });

        activeListeners.put(eventListener, databaseReference);
        return eventListener;
    }

    private MessageListResult parceMessageList(HashMap<String, Object> hashMap) {
        MessageListResult result = new MessageListResult();
        List<Message> list = new ArrayList<>();
        boolean isMoreDataAvailable = true;
        long lastItemCreatedDate = 0;

        if (hashMap != null) {
            isMoreDataAvailable = Constants.Message.MESSAGE_AMOUNT_ON_PAGE == hashMap.size();

            for (String key : hashMap.keySet()) {
                Object obj = hashMap.get(key);
                if (obj instanceof Map) {
                    Map<String, Object> mapObj = (Map<String, Object>) obj;

                    long createdDate = (long) mapObj.get("createdAt");

                    if (lastItemCreatedDate == 0 || lastItemCreatedDate > createdDate) {
                        lastItemCreatedDate = createdDate;
                    }

                    Message message = new Message();
                    message.setId(key);
                    message.setText((String) mapObj.get("text"));
                    message.setCreatedAt(createdDate);
                    message.setFromUserId((String) mapObj.get("fromUserId"));

                    list.add(message);
                }
            }

            Collections.sort(list, new Comparator<Message>() {
                @Override
                public int compare(Message o1, Message o2) {
                    return ((Long) o2.getCreatedAt()).compareTo((Long) o1.getCreatedAt());
                }
            });

            result.setMessages(list);
            result.setLastItemCreatedDate(lastItemCreatedDate);
            result.setMoreDataAvailable(isMoreDataAvailable);
        }
        return result;
    }

    public ValueEventListener getLastMessage(String userKey, final OnObjectChangedListener<Message> onObjectChangedListener) {

        DatabaseReference databaseReference = database.getReference(Consts.MESSAGES_REF).child(getCurrentUser());
        Query query = databaseReference.child(userKey).limitToLast(1);

        ValueEventListener valueEventListener = query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Message message = child.getValue(Message.class);
                    onObjectChangedListener.onObjectChanged(message);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        activeListeners.put(valueEventListener, databaseReference);
        return valueEventListener;
    }

    public Task<Void> removeMessage(String messageId, String userKey) {
        DatabaseReference reference = database.getReference(Consts.MESSAGES_REF)
                .child(getCurrentUser()).child(userKey).child(messageId);

        return reference.removeValue();
    }

    public Task<Void> removeConversation(String userKey) {

        DatabaseReference reference = database.getReference(Consts.MESSAGES_REF)
                .child(getCurrentUser()).child(userKey);

        return reference.removeValue();
    }

    public void checkOnlineStatus(boolean status) {
        DatabaseReference reference = database.getReference(Consts.USER_PUBLIC_REF)
                .child(getCurrentUser()).child(Consts.STATUS_REF);

        reference.setValue(new Status(status));
    }

}