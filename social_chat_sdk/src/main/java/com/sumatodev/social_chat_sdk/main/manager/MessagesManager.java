package com.sumatodev.social_chat_sdk.main.manager;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.sumatodev.social_chat_sdk.ChatApplicationHelper;
import com.sumatodev.social_chat_sdk.main.enums.UploadImagePrefix;
import com.sumatodev.social_chat_sdk.main.listeners.OnDataChangedListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnMessageSentListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnObjectChangedListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnTaskCompleteListener;
import com.sumatodev.social_chat_sdk.main.model.InputMessage;
import com.sumatodev.social_chat_sdk.main.model.Message;
import com.sumatodev.social_chat_sdk.main.model.Profile;
import com.sumatodev.social_chat_sdk.main.model.ThreadsModel;
import com.sumatodev.social_chat_sdk.main.model.UsersPublic;
import com.sumatodev.social_chat_sdk.main.utils.ImageUtil;
import com.sumatodev.social_chat_sdk.main.utils.LogUtil;
import com.sumatodev.social_chat_sdk.main.utils.NotificationView;


/**
 * Created by Ali on 09/03/2018.
 */

public class MessagesManager extends FirebaseListenersManager {

    private static final String TAG = MessagesManager.class.getSimpleName();
    private static MessagesManager instance;
    private DatabaseHelper databaseHelper;
    private Context context;

    public static MessagesManager getInstance(Context context) {
        if (instance == null) {
            instance = new MessagesManager(context);
        }
        return instance;
    }

    public MessagesManager(Context context) {
        this.context = context;
        databaseHelper = ChatApplicationHelper.getDatabaseHelper();
    }


    public void sendNewMessage(final InputMessage inputMessage, final OnMessageSentListener onMessageSentListener) {

        final Message message = new Message();

        if (message.getId() == null) {
            message.setId(databaseHelper.generateMessageId(databaseHelper.getCurrentUser(), inputMessage.getUid()));
        }

        if (inputMessage.getText() != null) {
            message.setText(inputMessage.getText());
        }

        if (inputMessage.getImageUrl() != null) {
            final String imageTitle = ImageUtil.generateImageTitle(UploadImagePrefix.MESSAGE, message.getId());
            UploadTask uploadTask = databaseHelper.uploadImage(inputMessage.getImageUrl(), imageTitle);

            NotificationView.getInstance(context).setNotification(true, "Uploading Image");
            if (uploadTask != null) {
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        onMessageSentListener.onMessageSent(false, e.getMessage());

                        NotificationView.getInstance(context).setNotification(false, "Failed Uploading Image");
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        LogUtil.logDebug(TAG, "successful upload image, image url: " + String.valueOf(downloadUrl));

                        if (downloadUrl != null) {
                            message.setImageUrl(downloadUrl.toString());

                            databaseHelper.sendMessage(message, inputMessage.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        NotificationView.getInstance(context).setNotification(false, "Uploading Image Successful");
                                        onMessageSentListener.onMessageSent(true, "successful");
                                    } else {
                                        onMessageSentListener.onMessageSent(false, task.getException().getMessage());
                                    }
                                }
                            });
                        }
                    }
                });
            }
        } else {
            databaseHelper.sendMessage(message, inputMessage.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        onMessageSentListener.onMessageSent(true, "successful");
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    onMessageSentListener.onMessageSent(false, e.getMessage());
                }
            });
        }
    }

    public void getProfileValue(Context activityContext, String id, final OnObjectChangedListener<Profile> listener) {
        ValueEventListener valueEventListener = databaseHelper.getProfile(id, listener);
        addListenerToMap(activityContext, valueEventListener);
    }

    public void getProfileSingleValue(String id, final OnObjectChangedListener<Profile> listener) {
        databaseHelper.getProfileSingleValue(id, listener);
    }

    public void getUsersPublicProfile(Context context, String userKey, OnObjectChangedListener<UsersPublic> listener) {
        ValueEventListener valueEventListener = ChatApplicationHelper.getDatabaseHelper().getUsersPublicProfile(userKey, listener);
        addListenerToMap(context, valueEventListener);

    }

    public void getMessageList(Context context, String userKey, OnDataChangedListener<Message> listener) {
        ValueEventListener valueEventListener = ChatApplicationHelper.getDatabaseHelper().getMessageList(userKey, listener);
        addListenerToMap(context, valueEventListener);
//        ChatApplicationHelper.getDatabaseHelper().getMessageList(userKey, listener, date);
    }

    public void getChatList(String userKey, OnDataChangedListener<Message> onMessageListChangedListener) {
        ChatApplicationHelper.getDatabaseHelper().getChatList(userKey, onMessageListChangedListener);
    }

    public void removeMessage(String messageId, String userKey, final OnTaskCompleteListener onTaskCompleteListener) {

        DatabaseHelper helper = ChatApplicationHelper.getDatabaseHelper();
        helper.removeMessage(messageId, userKey).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                onTaskCompleteListener.onTaskComplete(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onTaskCompleteListener.onTaskComplete(false);
                LogUtil.logError(TAG, "removeMessage()", e);
            }
        });
    }

    public void removeConversation(String userKey, final OnTaskCompleteListener onTaskCompleteListener) {
        DatabaseHelper helper = ChatApplicationHelper.getDatabaseHelper();
        helper.removeConversation(userKey).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                onTaskCompleteListener.onTaskComplete(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onTaskCompleteListener.onTaskComplete(false);
            }
        });
    }

    public void getThreadsList(Context context, OnDataChangedListener<ThreadsModel> changedListener) {
        ValueEventListener valueEventListener = ChatApplicationHelper.getDatabaseHelper().getThreadsList(changedListener);
        addListenerToMap(context, valueEventListener);
    }

    public void getConversationsList(OnDataChangedListener<ThreadsModel> onDataChangedListener) {
        ChatApplicationHelper.getDatabaseHelper().getConversationsList(onDataChangedListener);
    }

    public void getLastMessage(Context context, String userKey, OnObjectChangedListener<Message> onObjectChangedListener) {
        ValueEventListener valueEventListener = ChatApplicationHelper.getDatabaseHelper().getLastMessage(userKey
                , onObjectChangedListener);

        addListenerToMap(context, valueEventListener);
    }

    public void checkOnlineStatus(boolean status) {
        databaseHelper.checkOnlineStatus(status);
    }
}
