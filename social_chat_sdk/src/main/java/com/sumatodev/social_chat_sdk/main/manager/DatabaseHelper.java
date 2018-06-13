package com.sumatodev.social_chat_sdk.main.manager;


import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.Task;
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
import com.sumatodev.social_chat_sdk.ChatApplicationHelper;
import com.sumatodev.social_chat_sdk.Constants;
import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.main.enums.Consts;
import com.sumatodev.social_chat_sdk.main.enums.ItemType;
import com.sumatodev.social_chat_sdk.main.enums.MessageType;
import com.sumatodev.social_chat_sdk.main.listeners.OnDataChangedListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnMessageListChangedListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnObjectChangedListener;
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


    public Task<Void> sendMessage(Message message, String uid) {

        message.setFromUserId(getCurrentUser());

        Map messageMap = new ObjectMapper().convertValue(message, Map.class);

        String currentUser = uid + "/" + getCurrentUser();
        String chatUser = getCurrentUser() + "/" + uid;

        Map<String, Object> messageUserMap = new HashMap<>();
        messageUserMap.put(currentUser + "/" + message.getId(), messageMap);
        messageUserMap.put(chatUser + "/" + message.getId(), messageMap);


        DatabaseReference reference = database.getReference();
        return reference.child(Consts.MESSAGES_REF).updateChildren(messageUserMap);
    }


    public String generateMessageId(String current_uid, String other_uid) {
        DatabaseReference databaseReference = database.getReference();
        return databaseReference.child(Consts.MESSAGES_REF).child(current_uid)
                .child(other_uid).push().getKey();
    }


    public void getConversationsList(final OnDataChangedListener<ThreadsModel> onDataChangedListener) {
        DatabaseReference reference = database.getReference(Consts.MESSAGES_REF).child(getCurrentUser());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    List<ThreadsModel> list = new ArrayList<>();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        ThreadsModel model = new ThreadsModel(child.getKey());
                        list.add(model);
                    }
                    onDataChangedListener.onListChanged(list);
                } else {
                    onDataChangedListener.inEmpty(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onDataChangedListener.onCancel(context.getString(R.string.permission_denied_error));
            }
        });
    }

    public ValueEventListener getThreadsList(final OnDataChangedListener<ThreadsModel> onDataChangedListener) {
        DatabaseReference reference = database.getReference(Consts.MESSAGES_REF).child(getCurrentUser());
        ValueEventListener valueEventListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    final List<ThreadsModel> list = new ArrayList<>();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        ThreadsModel model = new ThreadsModel(child.getKey());
                        list.add(model);
                    }

                    onDataChangedListener.onListChanged(list);
                } else {
                    onDataChangedListener.inEmpty(true);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onDataChangedListener.onCancel(context.getString(R.string.permission_denied_error));
            }
        });

        activeListeners.put(valueEventListener, reference);
        return valueEventListener;
    }

    public UploadTask uploadImage(Uri imageUri, String imageTitle) {
        StorageReference storageRef = storage.getReferenceFromUrl(ChatApplicationHelper.getFirebaseBucketUrl());

        StorageReference riversRef = storageRef.child("chat_image/" + imageTitle);
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

    public ValueEventListener getMessageList(final String userKey, final OnMessageListChangedListener<Message> listene, long date) {

        DatabaseReference databaseReference = database.getReference(Consts.MESSAGES_REF).child(getCurrentUser())
                .child(userKey);

        Query postsQuery;
        if (date == 0) {
            postsQuery = databaseReference.limitToLast(Constants.Message.MESSAGE_AMOUNT_ON_PAGE).orderByChild("createdAt");
        } else {
            postsQuery = databaseReference.limitToLast(Constants.Message.MESSAGE_AMOUNT_ON_PAGE).endAt(date).orderByChild("createdAt");
        }


        ValueEventListener valueEventListener = postsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Map<String, Object> objectMap = (Map<String, Object>) dataSnapshot.getValue();
                    MessageListResult result = parceMessageList(objectMap);

                    if (result.getMessages().isEmpty() && result.isMoreDataAvailable()) {
                        getMessageList(userKey, listene, result.getLastItemCreatedDate() - 1);
                    } else {
                        listene.onListChanged(parceMessageList(objectMap));
                    }
                } else {
                    listene.isEmpty(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listene.onCanceled(databaseError.getMessage());
            }
        });

        activeListeners.put(valueEventListener, databaseReference);
        return valueEventListener;
    }

    private MessageListResult parceMessageList(Map<String, Object> objectMap) {
        MessageListResult result = new MessageListResult();
        List<Message> list = new ArrayList<>();
        boolean isMoreDataAvailable = true;
        long lastItemCreatedDate = 0;
        if (objectMap != null) {
            isMoreDataAvailable = Constants.Message.MESSAGE_AMOUNT_ON_PAGE == objectMap.size();

            for (String key : objectMap.keySet()) {
                Object obj = objectMap.get(key);
                if (obj instanceof Map) {
                    Map<String, Object> mapObj = (Map<String, Object>) obj;

                    long createdDate = (long) mapObj.get("createdAt");

                    if (lastItemCreatedDate == 0 || lastItemCreatedDate > createdDate) {
                        lastItemCreatedDate = createdDate;
                    }

                    Message message = new Message();
                    message.setId(key);
                    message.setCreatedAt(createdDate);

                    String from = (String) mapObj.get("fromUserId");
                    message.setFromUserId(from);
                    if (from.equalsIgnoreCase(getCurrentUser())) {
                        message.setMessageType(MessageType.SENT);
                    } else {
                        message.setMessageType(MessageType.RECEIVE);
                    }
                    if (mapObj.containsKey("text")) {
                        message.setText((String) mapObj.get("text"));
                        message.setItemType(ItemType.TEXT);
                    }
                    if (mapObj.containsKey("imageUrl")) {
                        message.setImageUrl((String) mapObj.get("imageUrl"));
                        message.setItemType(ItemType.IMAGE);
                    }


                    list.add(message);
                }
            }

            Collections.sort(list, new Comparator<Message>() {
                @Override
                public int compare(Message lhs, Message rhs) {
                    return ((Long) rhs.getCreatedAt()).compareTo((long) lhs.getCreatedAt());
                }
            });

            result.setMessages(list);
            result.setLastItemCreatedDate(lastItemCreatedDate);
            result.setMoreDataAvailable(isMoreDataAvailable);
        }

        return result;
    }


    public void getChatList(String userKey, final OnDataChangedListener<Message> onMessageListChangedListener) {

        DatabaseReference databaseReference = database.getReference(Consts.MESSAGES_REF).child(getCurrentUser()).child(userKey);

        final List<Message> list = new ArrayList<>();
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    Message message = dataSnapshot.getValue(Message.class);
                    list.add(message);
                }

                Collections.sort(list, new Comparator<Message>() {
                    @Override
                    public int compare(Message o1, Message o2) {
                        return ((Long) o2.getCreatedAt()).compareTo((Long) o1.getCreatedAt());
                    }
                });

                onMessageListChangedListener.onListChanged(list);
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
                onMessageListChangedListener.onCancel(databaseError.getMessage());
            }
        });
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