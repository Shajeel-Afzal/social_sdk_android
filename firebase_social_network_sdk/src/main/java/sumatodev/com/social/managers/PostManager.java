/*
 * Copyright 2017 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package sumatodev.com.social.managers;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;

import sumatodev.com.social.ApplicationHelper;
import sumatodev.com.social.R;
import sumatodev.com.social.enums.UploadImagePrefix;
import sumatodev.com.social.managers.listeners.OnDataChangedListener;
import sumatodev.com.social.managers.listeners.OnObjectChangedListener;
import sumatodev.com.social.managers.listeners.OnObjectExistListener;
import sumatodev.com.social.managers.listeners.OnPostChangedListener;
import sumatodev.com.social.managers.listeners.OnPostCreatedListener;
import sumatodev.com.social.managers.listeners.OnPostListChangedListener;
import sumatodev.com.social.managers.listeners.OnTaskCompleteListener;
import sumatodev.com.social.model.Like;
import sumatodev.com.social.model.Post;
import sumatodev.com.social.model.PostStyle;
import sumatodev.com.social.model.UsersPublic;
import sumatodev.com.social.utils.ImageUtil;
import sumatodev.com.social.utils.LogUtil;
import sumatodev.com.social.utils.NotificationView;

/**
 * Created by Kristina on 10/28/16.
 */

public class PostManager extends FirebaseListenersManager {

    private static final String TAG = PostManager.class.getSimpleName();
    private static PostManager instance;
    private int newPostsCounter = 0;
    private PostCounterWatcher postCounterWatcher;

    private Context context;

    public static PostManager getInstance(Context context) {
        if (instance == null) {
            instance = new PostManager(context);
        }

        return instance;
    }

    private PostManager(Context context) {
        this.context = context;
    }

    public void createOrUpdatePost(Post post) {
        try {
            ApplicationHelper.getDatabaseHelper().createOrUpdatePost(post);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void getPostsList(OnPostListChangedListener<Post> onDataChangedListener, long date) {
        ApplicationHelper.getDatabaseHelper().getPostList(onDataChangedListener, date);
    }

    public void getPostsListByUser(OnDataChangedListener<Post> onDataChangedListener, String userId) {
        ApplicationHelper.getDatabaseHelper().getPostListByUser(onDataChangedListener, userId);
    }

    public void getPost(Context context, String postId, OnPostChangedListener onPostChangedListener) {
        ValueEventListener valueEventListener = ApplicationHelper.getDatabaseHelper().getPost(postId, onPostChangedListener);
        addListenerToMap(context, valueEventListener);
    }

    public void getSinglePostValue(String postId, OnPostChangedListener onPostChangedListener) {
        ApplicationHelper.getDatabaseHelper().getSinglePost(postId, onPostChangedListener);
    }

    public void createOrUpdatePostWithImage(final Context context, final Post post,
                                            final OnPostCreatedListener onPostCreatedListener) {
        // Register observers to listen for when the download is done or if it fails
        DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        if (post.getId() == null) {
            post.setId(databaseHelper.generatePostId());
        }

        if (post.getImagePath() != null) {
            final String imageTitle = ImageUtil.generateImageTitle(UploadImagePrefix.POST, post.getId());
            UploadTask uploadTask = databaseHelper.uploadImage(Uri.parse(post.getImagePath()), imageTitle);

            NotificationView.getInstance(context).setNotification(true, context.getString(R.string.uploading_post_string));

            if (uploadTask != null) {
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        onPostCreatedListener.onPostSaved(false);

                        NotificationView.getInstance(context).setNotification(false, context.getString(R.string.failed_uploading_post_string));
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        LogUtil.logDebug(TAG, "successful upload image, image url: " + String.valueOf(downloadUrl));

                        post.setImagePath(String.valueOf(downloadUrl));
                        post.setImageTitle(imageTitle);
                        createOrUpdatePost(post);

                        NotificationView.getInstance(context).setNotification(false, context.getString(R.string.uploading_post_successful));
                        onPostCreatedListener.onPostSaved(true);

                    }
                });
            }
        } else {

            try {
                ApplicationHelper.getDatabaseHelper().createNewPost(post)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                onPostCreatedListener.onPostSaved(true);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        onPostCreatedListener.onPostSaved(false);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

        }
    }

    public void createIntentPost(final Context context, Post post, final OnPostCreatedListener onPostCreatedListener) {
        DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();

        NotificationView.getInstance(context).setNotification(true,
                context.getResources().getString(R.string.uploading_post_string));
        if (post.getId() == null) {
            post.setId(databaseHelper.generatePostId());
        }
        ApplicationHelper.getDatabaseHelper().createNewPost(post).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                NotificationView.getInstance(context).setNotification(false,
                        context.getResources().getString(R.string.uploading_post_successful));
                onPostCreatedListener.onPostSaved(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                NotificationView.getInstance(context).setNotification(false,
                        context.getResources().getString(R.string.failed_uploading_post_string));
                onPostCreatedListener.onPostSaved(true);
            }
        });
    }

    public Task<Void> removeImage(String imageTitle) {
        final DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        return databaseHelper.removeImage(imageTitle);
    }

    public void removePost(final Post post, final OnTaskCompleteListener onTaskCompleteListener) {
        final DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();

        if (post.getImageTitle() != null) {
            Task<Void> removeImageTask = removeImage(post.getImageTitle());

            removeImageTask.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    databaseHelper.removePost(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            onTaskCompleteListener.onTaskComplete(task.isSuccessful());
                            databaseHelper.updateProfileLikeCountAfterRemovingPost(post);
                            LogUtil.logDebug(TAG, "removePost(), is success: " + task.isSuccessful());
                        }
                    });
                    LogUtil.logDebug(TAG, "removeImage(): success");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    LogUtil.logError(TAG, "removeImage()", exception);
                    onTaskCompleteListener.onTaskComplete(false);
                }
            });
        } else {
            databaseHelper.removePost(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    onTaskCompleteListener.onTaskComplete(task.isSuccessful());
                    databaseHelper.updateProfileLikeCountAfterRemovingPost(post);
                    LogUtil.logDebug(TAG, "removePost(), is success: " + task.isSuccessful());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    onTaskCompleteListener.onTaskComplete(false);
                }
            });
        }
    }

    public void addComplain(Post post) {
        DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        databaseHelper.addComplainToPost(post);
    }

    public void hasCurrentUserLike(Context activityContext, String postId, String userId,
                                   final OnObjectExistListener<Like> onObjectExistListener) {
        DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        ValueEventListener valueEventListener = databaseHelper.hasCurrentUserLike(postId, userId, onObjectExistListener);
        addListenerToMap(activityContext, valueEventListener);
    }

    public void hasCurrentUserLikeSingleValue(String postId, String userId, final OnObjectExistListener<Like> onObjectExistListener) {
        DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        databaseHelper.hasCurrentUserLikeSingleValue(postId, userId, onObjectExistListener);
    }

    public void isCurrentPostColored(String id, OnObjectChangedListener<PostStyle> currentPostColored) {
        ApplicationHelper.getDatabaseHelper().isCurrentPostColored(id, currentPostColored);
    }

    public void isPostExistSingleValue(String postId, final OnObjectExistListener<Post> onObjectExistListener) {
        DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        databaseHelper.isPostExistSingleValue(postId, onObjectExistListener);
    }

    public void incrementWatchersCount(String postId) {
        DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        databaseHelper.incrementWatchersCount(postId);
    }

    public void incrementNewPostsCounter() {
        newPostsCounter++;
        notifyPostCounterWatcher();
    }

    public void clearNewPostsCounter() {
        newPostsCounter = 0;
        notifyPostCounterWatcher();
    }

    public int getNewPostsCounter() {
        return newPostsCounter;
    }

    public void setPostCounterWatcher(PostCounterWatcher postCounterWatcher) {
        this.postCounterWatcher = postCounterWatcher;
    }

    private void notifyPostCounterWatcher() {
        if (postCounterWatcher != null) {
            postCounterWatcher.onPostCounterChanged(newPostsCounter);
        }
    }


    public interface PostCounterWatcher {
        void onPostCounterChanged(int newValue);
    }

    public void getSearchList(Context context, String searchString, OnDataChangedListener<UsersPublic> onDataChangedListener) {
        DatabaseHelper reference = ApplicationHelper.getDatabaseHelper();
        ValueEventListener valueEventListener = reference.getSearchList(searchString, onDataChangedListener);
        addListenerToMap(context, valueEventListener);
    }

}
